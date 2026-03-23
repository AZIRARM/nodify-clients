<?php

namespace Nodify;

require_once __DIR__ . '/Models.php';
require_once __DIR__ . '/Enums.php';

use GuzzleHttp\Client as GuzzleClient;
use GuzzleHttp\Exception\RequestException;
use GuzzleHttp\Promise\PromiseInterface;
use GuzzleHttp\Promise\Utils;
use Psr\Http\Message\ResponseInterface;

use Nodify\Exception\NodifyClientException;
use Nodify\PaginationParams;
use Nodify\AuthResponse;
use Nodify\UserLogin;
use Nodify\UserPost;
use Nodify\UserPassword;
use Nodify\UserRole;
use Nodify\UserParameters;
use Nodify\Plugin;
use Nodify\PluginFile;
use Nodify\Notification;
use Nodify\Language;
use Nodify\Feedback;
use Nodify\FeedbackCharts;
use Nodify\Data;
use Nodify\Node;
use Nodify\ContentNode;
use Nodify\ContentNodePayload;
use Nodify\ContentDisplay;
use Nodify\ContentDisplayCharts;
use Nodify\ContentClick;
use Nodify\ContentClickCharts;
use Nodify\AccessRole;
use Nodify\TreeNode;
use Nodify\LockInfo;
use Nodify\Translation;
use Nodify\Value;
use Nodify\Rule;

class Client
{
    private Config $config;
    private GuzzleClient $httpClient;
    private ?string $authToken = null;

    public function __construct(Config $config)
    {
        $this->config = $config;
        $this->httpClient = new GuzzleClient([
            'base_uri' => $config->getBaseUrl(),
            'timeout' => $config->getTimeout() / 1000,
            'headers' => $this->buildHeaders(),
            'http_errors' => false,
        ]);
    }

    public static function create(Config $config): self
    {
        return new self($config);
    }

    public static function builder(): ConfigBuilder
    {
        return Config::builder();
    }

    private function buildHeaders(): array
    {
        $headers = $this->config->getDefaultHeaders();
        $token = $this->authToken ?? $this->config->getAuthToken();
        if ($token) {
            $headers['Authorization'] = "Bearer {$token}";
        }
        return $headers;
    }

    private function snakeToCamel(string $snakeStr): string
    {
        return lcfirst(str_replace('_', '', ucwords($snakeStr, '_')));
    }

    private function camelToSnake(string $camelStr): string
    {
        return strtolower(preg_replace('/(?<!^)[A-Z]/', '_$0', $camelStr));
    }

    private function objectToArray($obj)
    {
        if ($obj === null) {
            return null;
        }

        if (is_array($obj)) {
            $result = [];
            foreach ($obj as $key => $value) {
                $result[$key] = $this->objectToArray($value);
            }
            return $result;
        }

        if (is_object($obj)) {
            if ($obj instanceof \UnitEnum) {
                return $obj->value;
            }

            $vars = get_object_vars($obj);
            $result = [];

            foreach ($vars as $key => $value) {
                if ($value !== null) {
                    $camelKey = lcfirst(str_replace('_', '', ucwords($key, '_')));
                    $result[$camelKey] = $this->objectToArray($value);
                }
            }

            return $result;
        }

        return $obj;
    }

    private function arrayToObject(array $data, string $className)
    {
        if (!class_exists($className)) {
            return $data;
        }

        $reflection = new \ReflectionClass($className);
        $instance = $reflection->newInstanceWithoutConstructor();

        $properties = $reflection->getProperties(\ReflectionProperty::IS_PUBLIC);
        foreach ($properties as $prop) {
            $snakeKey = $this->camelToSnake($prop->getName());
            $value = $data[$prop->getName()] ?? $data[$snakeKey] ?? null;

            if ($value !== null) {
                $propType = $prop->getType();
                if ($propType && !$propType->isBuiltin()) {
                    $typeName = $propType->getName();

                    if (is_array($value) && $prop->getName() === 'translations') {
                        $array = [];
                        foreach ($value as $item) {
                            $array[] = $this->arrayToObject($item, Translation::class);
                        }
                        $prop->setValue($instance, $array);
                    } elseif (enum_exists($typeName)) {
                        try {
                            $enumValue = $typeName::tryFrom($value);
                            $prop->setValue($instance, $enumValue);
                        } catch (\Error $e) {
                            $prop->setValue($instance, $value);
                        }
                    } elseif (class_exists($typeName)) {
                        $prop->setValue($instance, $this->arrayToObject($value, $typeName));
                    } else {
                        $prop->setValue($instance, $value);
                    }
                } else {
                    $prop->setValue($instance, $value);
                }
            }
        }

        return $instance;
    }

    private function handleResponse(ResponseInterface $response, ?string $responseType = null)
    {
        $statusCode = $response->getStatusCode();
        $body = $response->getBody()->getContents();

        if ($statusCode < 200 || $statusCode >= 300) {
            throw new NodifyClientException(
                "Request failed with status: {$statusCode} - {$body}",
                $statusCode
            );
        }

        if ($statusCode === 204 || $responseType === null) {
            return null;
        }

        if ($responseType === 'string') {
            return $body;
        }

        if ($responseType === 'bool' || $responseType === 'boolean') {
            return strtolower($body) === 'true';
        }

        if ($responseType === 'int' || $responseType === 'integer') {
            return (int)$body;
        }

        $data = json_decode($body, true);

        if ($responseType && class_exists($responseType)) {
            return $this->arrayToObject($data, $responseType);
        }

        return $data;
    }

    private function executeGet(string $path, ?string $responseType = null, ?PaginationParams $params = null)
    {
        $url = $path;
        if ($params) {
            $query = $params->toQueryString();
            if ($query) {
                $url .= '?' . $query;
            }
        }

        $response = $this->httpClient->get($url);
        return $this->handleResponse($response, $responseType);
    }

    private function executeGetList(string $path, string $elementType, ?PaginationParams $params = null): array
    {
        $result = $this->executeGet($path, null, $params);
        if (!is_array($result)) {
            return [];
        }

        $list = [];
        foreach ($result as $item) {
            if (is_array($item) && class_exists($elementType)) {
                $list[] = $this->arrayToObject($item, $elementType);
            } else {
                $list[] = $item;
            }
        }
        return $list;
    }

    private function executePost(string $path, $body, ?string $responseType = null, ?PaginationParams $params = null)
    {
        $url = $path;
        if ($params) {
            $query = $params->toQueryString();
            if ($query) {
                $url .= '?' . $query;
            }
        }

        $jsonBody = $this->objectToArray($body);
        $response = $this->httpClient->post($url, ['json' => $jsonBody]);
        return $this->handleResponse($response, $responseType);
    }

    private function executePostList(string $path, $body, string $elementType, ?PaginationParams $params = null): array
    {
        $result = $this->executePost($path, $body, null, $params);
        if (!is_array($result)) {
            return [];
        }

        $list = [];
        foreach ($result as $item) {
            if (is_array($item) && class_exists($elementType)) {
                $list[] = $this->arrayToObject($item, $elementType);
            } else {
                $list[] = $item;
            }
        }
        return $list;
    }

    private function executePut(string $path, $body = null, ?string $responseType = null, ?PaginationParams $params = null)
    {
        $url = $path;
        if ($params) {
            $query = $params->toQueryString();
            if ($query) {
                $url .= '?' . $query;
            }
        }

        $options = [];
        if ($body !== null) {
            $options['json'] = $this->objectToArray($body);
        }

        $response = $this->httpClient->put($url, $options);
        return $this->handleResponse($response, $responseType);
    }

    private function executeDelete(string $path, ?string $responseType = null, ?PaginationParams $params = null)
    {
        $url = $path;
        if ($params) {
            $query = $params->toQueryString();
            if ($query) {
                $url .= '?' . $query;
            }
        }

        $response = $this->httpClient->delete($url);
        return $this->handleResponse($response, $responseType);
    }

    public function setAuthToken(string $token): void
    {
        $this->authToken = $token;
        $this->httpClient = new GuzzleClient([
            'base_uri' => $this->config->getBaseUrl(),
            'timeout' => $this->config->getTimeout() / 1000,
            'headers' => $this->buildHeaders(),
            'http_errors' => false,
        ]);
    }

    public function getAuthToken(): ?string
    {
        return $this->authToken ?? $this->config->getAuthToken();
    }

    // ==================== Authentication Endpoints ====================

    public function login(string $email, string $password): AuthResponse
    {
        $data = ['email' => $email, 'password' => $password];

        $tempClient = new GuzzleClient([
            'base_uri' => $this->config->getBaseUrl(),
            'timeout' => $this->config->getTimeout() / 1000,
            'http_errors' => false,
        ]);

        $response = $tempClient->post('/authentication/login', ['json' => $data]);

        if ($response->getStatusCode() !== 200) {
            $body = $response->getBody()->getContents();
            throw new NodifyClientException("Login failed: {$response->getStatusCode()} - {$body}", $response->getStatusCode());
        }

        $result = json_decode($response->getBody()->getContents(), true);
        $token = $result['token'] ?? null;

        if ($token) {
            $this->setAuthToken($token);
        }

        return new AuthResponse($token);
    }

    public function logout(): void
    {
        $this->setAuthToken(null);
    }

    // ==================== Health Endpoint ====================

    public function health(): string
    {
        return $this->executeGet('/health', 'string');
    }

    // ==================== Users Endpoints ====================

    public function findAllUsers(): array
    {
        return $this->executeGetList('/v0/users/', UserPost::class);
    }

    public function saveUser(UserPost $user): UserPost
    {
        return $this->executePost('/v0/users/', $user, UserPost::class);
    }

    public function changePassword(UserPassword $passwordData): bool
    {
        return $this->executePost('/v0/users/password', $passwordData, 'bool');
    }

    public function findUserById(string $userId): UserPost
    {
        return $this->executeGet("/v0/users/id/{$userId}", UserPost::class);
    }

    public function findUserByEmail(string $email): UserPost
    {
        return $this->executeGet("/v0/users/email/{$email}", UserPost::class);
    }

    public function deleteUser(string $userId): bool
    {
        return $this->executeDelete("/v0/users/id/{$userId}", 'bool');
    }

    // ==================== User Roles Endpoints ====================

    public function findAllUserRoles(): array
    {
        return $this->executeGetList('/v0/users-roles/', UserRole::class);
    }

    public function saveUserRole(UserRole $role): UserRole
    {
        return $this->executePost('/v0/users-roles/', $role, UserRole::class);
    }

    public function findUserRoleById(string $roleId): UserRole
    {
        return $this->executeGet("/v0/users-roles/id/{$roleId}", UserRole::class);
    }

    public function deleteUserRole(string $roleId): bool
    {
        return $this->executeDelete("/v0/users-roles/id/{$roleId}", 'bool');
    }

    // ==================== User Parameters Endpoints ====================

    public function findAllUserParameters(): array
    {
        return $this->executeGetList('/v0/user-parameters/', UserParameters::class);
    }

    public function saveUserParameters(UserParameters $params): UserParameters
    {
        return $this->executePost('/v0/user-parameters/', $params, UserParameters::class);
    }

    public function findUserParametersByUserId(string $userId): UserParameters
    {
        return $this->executeGet("/v0/user-parameters/user/{$userId}", UserParameters::class);
    }

    public function findUserParametersById(string $paramsId): UserParameters
    {
        return $this->executeGet("/v0/user-parameters/id/{$paramsId}", UserParameters::class);
    }

    public function deleteUserParameters(string $paramsId): bool
    {
        return $this->executeDelete("/v0/user-parameters/id/{$paramsId}", 'bool');
    }

    // ==================== Plugins Endpoints ====================

    public function findNotDeletedPlugins(): array
    {
        return $this->executeGetList('/v0/plugins/', Plugin::class);
    }

    public function savePlugin(Plugin $plugin): Plugin
    {
        return $this->executePost('/v0/plugins/', $plugin, Plugin::class);
    }

    public function importContentNodePlugin(Plugin $plugin): Plugin
    {
        return $this->executePost('/v0/plugins/import', $plugin, Plugin::class);
    }

    public function enablePlugin(string $pluginId): Plugin
    {
        return $this->executePut("/v0/plugins/id/{$pluginId}/enable", null, Plugin::class);
    }

    public function disablePlugin(string $pluginId): Plugin
    {
        return $this->executePut("/v0/plugins/id/{$pluginId}/disable", null, Plugin::class);
    }

    public function activatePlugin(string $pluginId): Plugin
    {
        return $this->executePut("/v0/plugins/id/{$pluginId}/activate", null, Plugin::class);
    }

    public function findPluginById(string $pluginId): Plugin
    {
        return $this->executeGet("/v0/plugins/id/{$pluginId}", Plugin::class);
    }

    public function exportPlugin(string $pluginId): string
    {
        return $this->executeGet("/v0/plugins/id/{$pluginId}/export", 'string');
    }

    public function findDeletedPlugins(): array
    {
        return $this->executeGetList('/v0/plugins/deleteds', Plugin::class);
    }

    public function deletePlugin(string $pluginId): bool
    {
        return $this->executeDelete("/v0/plugins/id/{$pluginId}", 'bool');
    }

    public function deletePluginDefinitively(string $pluginId): bool
    {
        return $this->executeDelete("/v0/plugins/id/{$pluginId}/deleteDefinitively", 'bool');
    }

    // ==================== Plugin Files Endpoints ====================

    public function findAllPluginFiles(?bool $enabled = null): array
    {
        $path = '/v0/plugin-files/';
        if ($enabled !== null) {
            $path .= '?enabled=' . ($enabled ? 'true' : 'false');
        }
        return $this->executeGetList($path, PluginFile::class);
    }

    public function savePluginFile(PluginFile $pluginFile): PluginFile
    {
        return $this->executePost('/v0/plugin-files/', $pluginFile, PluginFile::class);
    }

    public function findPluginFilesByPluginId(string $pluginId): array
    {
        return $this->executeGetList("/v0/plugin-files/plugin/{$pluginId}", PluginFile::class);
    }

    public function findPluginFilesByPluginName(string $name): array
    {
        return $this->executeGetList("/v0/plugin-files/plugin/name/{$name}", PluginFile::class);
    }

    public function findPluginFileById(string $fileId): PluginFile
    {
        return $this->executeGet("/v0/plugin-files/id/{$fileId}", PluginFile::class);
    }

    public function deletePluginFile(string $fileId): bool
    {
        return $this->executeDelete("/v0/plugin-files/id/{$fileId}", 'bool');
    }

    // ==================== Notifications Endpoints ====================

    public function markAllNotificationsAsRead(): array
    {
        return $this->executePostList('/v0/notifications/markAllAsRead', null, Notification::class);
    }

    public function markNotificationAsRead(string $notificationId): Notification
    {
        return $this->executePost("/v0/notifications/id/{$notificationId}/markread", null, Notification::class);
    }

    // ==================== Languages Endpoints ====================

    public function findAllLanguages(): array
    {
        return $this->executeGetList('/v0/languages/', Language::class);
    }

    public function saveLanguage(Language $language): Language
    {
        return $this->executePost('/v0/languages/', $language, Language::class);
    }

    public function findLanguageById(string $languageId): Language
    {
        return $this->executeGet("/v0/languages/id/{$languageId}", Language::class);
    }

    public function deleteLanguage(string $languageId): bool
    {
        return $this->executeDelete("/v0/languages/id/{$languageId}", 'bool');
    }

    // ==================== Feedback Endpoints ====================

    public function findAllFeedback(): array
    {
        return $this->executeGetList('/v0/feedbacks/', Feedback::class);
    }

    public function saveFeedback(Feedback $feedback): Feedback
    {
        return $this->executePost('/v0/feedbacks/', $feedback, Feedback::class);
    }

    public function findFeedbackByVerified(bool $verified): array
    {
        return $this->executeGetList("/v0/feedbacks/verified/" . ($verified ? 'true' : 'false'), Feedback::class);
    }

    public function findFeedbackByUserId(string $userId): array
    {
        return $this->executeGetList("/v0/feedbacks/userId/{$userId}", Feedback::class);
    }

    public function findFeedbackById(string $feedbackId): Feedback
    {
        return $this->executeGet("/v0/feedbacks/id/{$feedbackId}", Feedback::class);
    }

    public function findFeedbackByEvaluation(int $evaluation): array
    {
        return $this->executeGetList("/v0/feedbacks/evaluation/{$evaluation}", Feedback::class);
    }

    public function findFeedbackByContentCode(string $code): array
    {
        return $this->executeGetList("/v0/feedbacks/contentCode/{$code}", Feedback::class);
    }

    public function getContentCharts(): array
    {
        return $this->executeGetList('/v0/feedbacks/charts', FeedbackCharts::class);
    }

    public function deleteFeedback(string $feedbackId): bool
    {
        return $this->executeDelete("/v0/feedbacks/id/{$feedbackId}", 'bool');
    }

    // ==================== Data Endpoints ====================

    public function saveData(Data $data): Data
    {
        return $this->executePost('/v0/datas/', $data, Data::class);
    }

    public function findDataByKey(string $key): Data
    {
        return $this->executeGet("/v0/datas/key/{$key}", Data::class);
    }

    public function findDataByContentCode(string $code, ?PaginationParams $params = null): array
    {
        return $this->executeGetList("/v0/datas/contentCode/{$code}", Data::class, $params);
    }

    public function countDataByContentCode(string $code): int
    {
        return $this->executeGet("/v0/datas/contentCode/{$code}/count", 'int');
    }

    public function deleteAllDataByContentCode(string $code): bool
    {
        return $this->executeDelete("/v0/datas/contentCode/{$code}", 'bool');
    }

    public function deleteDataById(string $dataId): bool
    {
        return $this->executeDelete("/v0/datas/id/{$dataId}", 'bool');
    }

    // ==================== Nodes Endpoints ====================

    public function findAllNodes(): array
    {
        return $this->executeGetList('/v0/nodes/', Node::class);
    }

    public function saveNode(Node $node): Node
    {
        return $this->executePost('/v0/nodes/', $node, Node::class);
    }

    public function importNode(Node $node): Node
    {
        return $this->executePost('/v0/nodes/import', $node, Node::class);
    }

    public function revertNodeVersion(string $code, string $version): Node
    {
        return $this->executePost("/v0/nodes/code/{$code}/version/{$version}/revert", null, Node::class);
    }

    public function deployNodeVersion(string $code, string $version, ?string $environment = null): bool
    {
        $uri = "/v0/nodes/code/{$code}/version/{$version}/deploy";
        if ($environment) {
            $uri .= "?environment={$environment}";
        }
        return $this->executePost($uri, null, 'bool');
    }

    public function publishNode(string $code): Node
    {
        return $this->executePost("/v0/nodes/code/{$code}/publish", null, Node::class);
    }

    public function activateNode(string $code): bool
    {
        return $this->executePost("/v0/nodes/code/{$code}/activate", null, 'bool');
    }

    public function findAllNodesByStatus(string $status): array
    {
        return $this->executeGetList("/v0/nodes/status/{$status}", Node::class);
    }

    public function findPublishedNodes(): array
    {
        return $this->executeGetList('/v0/nodes/published', Node::class);
    }

    public function findParentNodesByStatus(string $status): array
    {
        return $this->executeGetList("/v0/nodes/parent/status/{$status}", Node::class);
    }

    public function findNodesByParentCode(string $code): array
    {
        return $this->executeGetList("/v0/nodes/parent/code/{$code}", Node::class);
    }

    public function findChildrenByCodeAndStatus(string $code, string $status): array
    {
        return $this->executeGetList("/v0/nodes/parent/code/{$code}/status/{$status}", Node::class);
    }

    public function findAllDescendants(string $code): array
    {
        return $this->executeGetList("/v0/nodes/parent/code/{$code}/descendants", Node::class);
    }

    public function findParentOrigin(): array
    {
        return $this->executeGetList('/v0/nodes/origin', Node::class);
    }

    public function getDeletedNodes(?string $parent = null): array
    {
        $uri = '/v0/nodes/deleted';
        if ($parent) {
            $uri .= "?parent={$parent}";
        }
        return $this->executeGetList($uri, Node::class);
    }

    public function findNodesByCode(string $code): array
    {
        return $this->executeGetList("/v0/nodes/code/{$code}", Node::class);
    }

    public function findNodeByCodeAndStatus(string $code, string $status): Node
    {
        return $this->executeGet("/v0/nodes/code/{$code}/status/{$status}", Node::class);
    }

    public function generateTreeView(string $code): TreeNode
    {
        return $this->executeGet("/v0/nodes/code/{$code}/tree-view", TreeNode::class);
    }

    public function checkSlugExistsForNode(string $code, string $slug): bool
    {
        return $this->executeGet("/v0/nodes/code/{$code}/slug/{$slug}/exists", 'bool');
    }

    public function hasContents(string $code): bool
    {
        return $this->executeGet("/v0/nodes/code/{$code}/haveContents", 'bool');
    }

    public function hasChildren(string $code): bool
    {
        return $this->executeGet("/v0/nodes/code/{$code}/haveChilds", 'bool');
    }

    public function exportAllNodes(string $code, ?string $environment = null): string
    {
        $uri = "/v0/nodes/code/{$code}/export";
        if ($environment) {
            $uri .= "?environment={$environment}";
        }
        return $this->executeGet($uri, 'string');
    }

    public function deployNode(string $code, ?string $environment = null): array
    {
        $uri = "/v0/nodes/code/{$code}/deploy";
        if ($environment) {
            $uri .= "?environment={$environment}";
        }
        return $this->executeGetList($uri, Node::class);
    }

    public function deleteNode(string $code): bool
    {
        return $this->executeDelete("/v0/nodes/code/{$code}", 'bool');
    }

    public function deleteNodeDefinitively(string $code): bool
    {
        return $this->executeDelete("/v0/nodes/code/{$code}/deleteDefinitively", 'bool');
    }

    public function deleteNodeVersionDefinitively(string $code, string $version): bool
    {
        return $this->executeDelete("/v0/nodes/code/{$code}/version/{$version}/deleteDefinitively", 'bool');
    }

    // ==================== Content Nodes Endpoints ====================

    public function findAllContentNodes(): array
    {
        return $this->executeGetList('/v0/content-node/', ContentNode::class);
    }

    public function saveContentNode(ContentNode $contentNode): ContentNode
    {
        return $this->executePost('/v0/content-node/', $contentNode, ContentNode::class);
    }

    public function importContentNode(ContentNode $contentNode, ?string $nodeParentCode = null, ?bool $fromFile = null): ContentNode
    {
        $uri = '/v0/content-node/import';
        $params = [];
        if ($nodeParentCode !== null) {
            $params[] = "nodeParentCode={$nodeParentCode}";
        }
        if ($fromFile !== null) {
            $params[] = "fromFile=" . ($fromFile ? 'true' : 'false');
        }
        if (!empty($params)) {
            $uri .= '?' . implode('&', $params);
        }
        return $this->executePost($uri, $contentNode, ContentNode::class);
    }

    public function revertContentNodeVersion(string $code, string $version): ContentNode
    {
        return $this->executePost("/v0/content-node/code/{$code}/version/{$version}/revert", null, ContentNode::class);
    }

    public function deployContentNodeVersion(string $code, string $version, ?string $environment = null): bool
    {
        $uri = "/v0/content-node/code/{$code}/version/{$version}/deploy";
        if ($environment) {
            $uri .= "?environment={$environment}";
        }
        return $this->executePost($uri, null, 'bool');
    }

    public function fillContent(string $code, string $status, ContentNodePayload $payload): ContentNode
    {
        return $this->executePost("/v0/content-node/code/{$code}/status/{$status}/fill", $payload, ContentNode::class);
    }

    public function publishContentNode(string $code, bool $publish): ContentNode
    {
        return $this->executePost("/v0/content-node/code/{$code}/publish/" . ($publish ? 'true' : 'false'), null, ContentNode::class);
    }

    public function activateContentNode(string $code): bool
    {
        return $this->executePost("/v0/content-node/code/{$code}/activate", null, 'bool');
    }

    public function findAllContentNodesByStatus(string $status): array
    {
        return $this->executeGetList("/v0/content-node/status/{$status}", ContentNode::class);
    }

    public function findAllContentNodesByNodeCode(string $code): array
    {
        return $this->executeGetList("/v0/content-node/node/code/{$code}", ContentNode::class);
    }

    public function findContentNodesByNodeCodeAndStatus(string $code, string $status): array
    {
        return $this->executeGetList("/v0/content-node/node/code/{$code}/status/{$status}", ContentNode::class);
    }

    public function getDeletedContentNodes(?string $parent = null): array
    {
        $uri = '/v0/content-node/deleted';
        if ($parent) {
            $uri .= "?parent={$parent}";
        }
        return $this->executeGetList($uri, ContentNode::class);
    }

    public function findAllContentNodesByCode(string $code): array
    {
        return $this->executeGetList("/v0/content-node/code/{$code}", ContentNode::class);
    }

    public function findContentNodeByCodeAndStatus(string $code, string $status): ContentNode
    {
        return $this->executeGet("/v0/content-node/code/{$code}/status/{$status}", ContentNode::class);
    }

    public function checkContentNodeSlugExists(string $code, string $slug): bool
    {
        return $this->executeGet("/v0/content-node/code/{$code}/slug/{$slug}/exists", 'bool');
    }

    public function exportContentNode(string $code, ?string $environment = null): string
    {
        $uri = "/v0/content-node/code/{$code}/export";
        if ($environment) {
            $uri .= "?environment={$environment}";
        }
        return $this->executeGet($uri, 'string');
    }

    public function deployContentNode(string $code, ?string $environment = null): bool
    {
        $uri = "/v0/content-node/code/{$code}/deploy";
        if ($environment) {
            $uri .= "?environment={$environment}";
        }
        return $this->executeGet($uri, 'bool');
    }

    public function deleteContentNode(string $code): bool
    {
        return $this->executeDelete("/v0/content-node/code/{$code}", 'bool');
    }

    public function deleteContentNodeDefinitively(string $code): bool
    {
        return $this->executeDelete("/v0/content-node/code/{$code}/deleteDefinitively", 'bool');
    }

    public function deleteContentNodeVersionDefinitively(string $code, string $version): bool
    {
        return $this->executeDelete("/v0/content-node/code/{$code}/version/{$version}/deleteDefinitively", 'bool');
    }

    // ==================== Content Displays Endpoints ====================

    public function findAllContentDisplays(): array
    {
        return $this->executeGetList('/v0/content-displays/', ContentDisplay::class);
    }

    public function saveContentDisplay(ContentDisplay $contentDisplay): ContentDisplay
    {
        return $this->executePost('/v0/content-displays/', $contentDisplay, ContentDisplay::class);
    }

    public function findContentDisplayById(string $displayId): ContentDisplay
    {
        return $this->executeGet("/v0/content-displays/id/{$displayId}", ContentDisplay::class);
    }

    public function findContentDisplayByContentCode(string $code): ContentDisplay
    {
        return $this->executeGet("/v0/content-displays/contentCode/{$code}", ContentDisplay::class);
    }

    public function getContentDisplayCharts(): array
    {
        return $this->executeGetList('/v0/content-displays/charts', ContentDisplayCharts::class);
    }

    public function deleteContentDisplay(string $displayId): bool
    {
        return $this->executeDelete("/v0/content-displays/id/{$displayId}", 'bool');
    }

    // ==================== Content Clicks Endpoints ====================

    public function findAllContentClicks(): array
    {
        return $this->executeGetList('/v0/content-clicks/', ContentClick::class);
    }

    public function saveContentClick(ContentClick $contentClick): ContentClick
    {
        return $this->executePost('/v0/content-clicks/', $contentClick, ContentClick::class);
    }

    public function findContentClickById(string $clickId): ContentClick
    {
        return $this->executeGet("/v0/content-clicks/id/{$clickId}", ContentClick::class);
    }

    public function findContentClickByContentCode(string $code): array
    {
        return $this->executeGetList("/v0/content-clicks/contentCode/{$code}", ContentClick::class);
    }

    public function getContentClickCharts(): array
    {
        return $this->executeGetList('/v0/content-clicks/charts', ContentClickCharts::class);
    }

    public function deleteContentClick(string $clickId): bool
    {
        return $this->executeDelete("/v0/content-clicks/id/{$clickId}", 'bool');
    }

    // ==================== Access Roles Endpoints ====================

    public function findAllAccessRoles(): array
    {
        return $this->executeGetList('/v0/access-roles/', AccessRole::class);
    }

    public function saveAccessRole(AccessRole $accessRole): AccessRole
    {
        return $this->executePost('/v0/access-roles/', $accessRole, AccessRole::class);
    }

    public function findAccessRoleById(string $roleId): AccessRole
    {
        return $this->executeGet("/v0/access-roles/id/{$roleId}", AccessRole::class);
    }

    public function deleteAccessRole(string $roleId): bool
    {
        return $this->executeDelete("/v0/access-roles/id/{$roleId}", 'bool');
    }

    // ==================== Charts Endpoints ====================

    public function getCharts(): TreeNode
    {
        return $this->executeGet('/v0/charts/', TreeNode::class);
    }

    // ==================== Locks Endpoints ====================

    public function acquireLock(string $code): bool
    {
        return $this->executePost("/v0/locks/acquire/{$code}", null, 'bool');
    }

    public function refreshLock(string $code): bool
    {
        return $this->executePost("/v0/locks/refresh/{$code}", null, 'bool');
    }

    public function releaseLock(string $code): bool
    {
        return $this->executePost("/v0/locks/release/{$code}", null, 'bool');
    }

    public function adminReleaseLock(string $code): bool
    {
        return $this->executePost("/v0/locks/admin/release/{$code}", null, 'bool');
    }

    public function getLockOwner(string $code): LockInfo
    {
        return $this->executeGet("/v0/locks/owner/{$code}", LockInfo::class);
    }

    public function getAllLocks(): array
    {
        return $this->executeGetList('/v0/locks/all', LockInfo::class);
    }

    // ==================== Slug Controller ====================

    public function checkSlugExists(string $slug): array
    {
        return $this->executeGetList("/v0/slugs/exists/{$slug}", 'string');
    }
}