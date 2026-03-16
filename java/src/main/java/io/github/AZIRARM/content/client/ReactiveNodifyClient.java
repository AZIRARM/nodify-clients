package io.github.AZIRARM.content.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.AZIRARM.content.client.configs.ReactiveNodifyClientConfig;
import io.github.AZIRARM.content.client.exceptions.ReactiveNodifyClientException;
import io.github.AZIRARM.content.client.models.PaginationParams;
import io.github.AZIRARM.content.lib.models.*;
import io.github.AZIRARM.content.lib.models.auth.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class ReactiveNodifyClient {
    private final ReactiveNodifyClientConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ReactiveNodifyClient(ReactiveNodifyClientConfig config) {
        this.config = config;

        // Configuration du client HTTP Netty réactif
        ConnectionProvider provider = ConnectionProvider.builder("nodify-client")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .evictInBackground(Duration.ofSeconds(30))
                .build();

        this.httpClient = HttpClient.create(provider)
                .baseUrl(config.getBaseUrl())
                .responseTimeout(Duration.ofMillis(config.getTimeout()))
                .followRedirect(true)
                .compress(true);

        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ReactiveNodifyClientConfig.Builder builder() {
        return new ReactiveNodifyClientConfig.Builder();
    }

    public static ReactiveNodifyClient create(ReactiveNodifyClientConfig config) {
        return new ReactiveNodifyClient(config);
    }

    // ==================== Gestion du token ====================

    public void setAuthToken(String token) {
        config.setAuthToken(token);
    }

    public String getAuthToken() {
        return config.getAuthToken();
    }

    // ==================== Méthodes privées ====================

    private HttpClient buildClientWithHeaders() {
        return httpClient.headers(headers -> {
            config.getDefaultHeaders().forEach(headers::set);
            if (config.getAuthToken() != null && !config.getAuthToken().isEmpty()) {
                headers.set("Authorization", "Bearer " + config.getAuthToken());
            }
        });
    }

    private <T> Mono<T> handleResponse(HttpClient.ResponseReceiver<?> responseReceiver, Class<T> responseType) {
        return responseReceiver.responseSingle((response, byteBufMono) -> {
            if (response.status().code() < 200 || response.status().code() >= 300) {
                return byteBufMono.asString(StandardCharsets.UTF_8)
                        .defaultIfEmpty("")
                        .flatMap(body -> Mono.error(new ReactiveNodifyClientException(
                                "Request failed with status: " + response.status().code() + " - " + body,
                                response.status().code())));
            }

            if (responseType == Void.class || response.status().code() == 204) {
                return byteBufMono.then(Mono.empty());
            }

            if (responseType == String.class) {
                return byteBufMono.asString(StandardCharsets.UTF_8)
                        .map(body -> (T) body);
            }

            if (responseType == Boolean.class) {
                return byteBufMono.asString(StandardCharsets.UTF_8)
                        .map(body -> (T) Boolean.valueOf(body));
            }

            if (responseType == byte[].class) {
                return byteBufMono.asByteArray()
                        .map(bytes -> (T) bytes);
            }

            return byteBufMono.asString(StandardCharsets.UTF_8)
                    .flatMap(body -> {
                        try {
                            return Mono.just(objectMapper.readValue(body, responseType));
                        } catch (Exception e) {
                            return Mono.error(new ReactiveNodifyClientException("Error parsing response: " + e.getMessage(), e));
                        }
                    });
        });
    }

    private <T> Mono<T> handleResponse(HttpClient.ResponseReceiver<?> responseReceiver, TypeReference<T> typeReference) {
        return responseReceiver.responseSingle((response, byteBufMono) -> {
            if (response.status().code() < 200 || response.status().code() >= 300) {
                return byteBufMono.asString(StandardCharsets.UTF_8)
                        .defaultIfEmpty("")
                        .flatMap(body -> Mono.error(new ReactiveNodifyClientException(
                                "Request failed with status: " + response.status().code() + " - " + body,
                                response.status().code())));
            }

            return byteBufMono.asString(StandardCharsets.UTF_8)
                    .flatMap(body -> {
                        try {
                            return Mono.just(objectMapper.readValue(body, typeReference));
                        } catch (Exception e) {
                            return Mono.error(new ReactiveNodifyClientException("Error parsing response: " + e.getMessage(), e));
                        }
                    });
        });
    }

    private <T> Mono<T> executeGet(String uri, Class<T> responseType) {
        return handleResponse(
                buildClientWithHeaders().get().uri(uri),
                responseType
        );
    }

    private <T> Mono<T> executeGet(String uri, TypeReference<T> typeReference) {
        return handleResponse(
                buildClientWithHeaders().get().uri(uri),
                typeReference
        );
    }

    private <T> Flux<T> executeGetFlux(String uri, Class<T> elementType) {
        return executeGet(uri, new TypeReference<List<T>>() {})
                .flatMapMany(Flux::fromIterable);
    }

    private <T> Mono<T> executePost(String uri, Object body, Class<T> responseType) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(body))
                .flatMap(jsonBody -> handleResponse(
                        buildClientWithHeaders()
                                .post()
                                .uri(uri)
                                .send(ByteBufFlux.fromString(Mono.just(jsonBody))),
                        responseType
                ));
    }

    private <T> Mono<T> executePost(String uri, Object body, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(body))
                .flatMap(jsonBody -> handleResponse(
                        buildClientWithHeaders()
                                .post()
                                .uri(uri)
                                .send(ByteBufFlux.fromString(Mono.just(jsonBody))),
                        typeReference
                ));
    }

    private <T> Flux<T> executePostFlux(String uri, Object body, Class<T> elementType) {
        return executePost(uri, body, new TypeReference<List<T>>() {})
                .flatMapMany(Flux::fromIterable);
    }

    private <T> Mono<T> executePut(String uri, Class<T> responseType) {
        return handleResponse(
                buildClientWithHeaders().put().uri(uri),
                responseType
        );
    }

    private <T> Mono<T> executePut(String uri, Object body, Class<T> responseType) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(body))
                .flatMap(jsonBody -> handleResponse(
                        buildClientWithHeaders()
                                .put()
                                .uri(uri)
                                .send(ByteBufFlux.fromString(Mono.just(jsonBody))),
                        responseType
                ));
    }

    private <T> Mono<T> executeDelete(String uri, Class<T> responseType) {
        return handleResponse(
                buildClientWithHeaders().delete().uri(uri),
                responseType
        );
    }

    private String buildUri(String path, PaginationParams params) {
        if (params == null || params.toQueryString().isEmpty()) {
            return path;
        }
        return path + "?" + params.toQueryString();
    }

    // ==================== Authentication Endpoint ====================

    public Mono<AuthResponse> login(String email, String password) {
        UserLogin login = new UserLogin(email, password);
        return executePost("/authentication/login", login, AuthResponse.class)
                .doOnNext(response -> {
                    if (response != null && response.getToken() != null) {
                        config.setAuthToken(response.getToken());
                    }
                });
    }

    public Mono<Void> logout() {
        return Mono.fromRunnable(() -> config.setAuthToken(null));
    }

    // ==================== Health Endpoint ====================

    public Mono<String> health() {
        return executeGet("/health", String.class);
    }

    // ==================== Users Endpoints ====================

    public Flux<UserPost> findAllUsers() {
        return executeGetFlux("/v0/users/", UserPost.class);
    }

    public Mono<UserPost> saveUser(UserPost user) {
        return executePost("/v0/users/", user, UserPost.class);
    }

    public Mono<Boolean> changePassword(UserPassword passwordData) {
        return executePost("/v0/users/password", passwordData, Boolean.class);
    }

    public Mono<UserPost> findUserById(UUID id) {
        return executeGet("/v0/users/id/" + id.toString(), UserPost.class);
    }

    public Mono<UserPost> findUserByEmail(String email) {
        return executeGet("/v0/users/email/" + email, UserPost.class);
    }

    public Mono<Boolean> deleteUser(UUID id) {
        return executeDelete("/v0/users/id/" + id.toString(), Boolean.class);
    }

    // ==================== User Roles Endpoints ====================

    public Flux<UserRole> findAllUserRoles() {
        return executeGetFlux("/v0/users-roles/", UserRole.class);
    }

    public Mono<UserRole> saveUserRole(UserRole role) {
        return executePost("/v0/users-roles/", role, UserRole.class);
    }

    public Mono<UserRole> findUserRoleById(String id) {
        return executeGet("/v0/users-roles/id/" + id, UserRole.class);
    }

    public Mono<Boolean> deleteUserRole(String id) {
        return executeDelete("/v0/users-roles/id/" + id, Boolean.class);
    }

    // ==================== User Parameters Endpoints ====================

    public Flux<UserParameters> findAllUserParameters() {
        return executeGetFlux("/v0/user-parameters/", UserParameters.class);
    }

    public Mono<UserParameters> saveUserParameters(UserParameters params) {
        return executePost("/v0/user-parameters/", params, UserParameters.class);
    }

    public Mono<UserParameters> findUserParametersByUserId(UUID userId) {
        return executeGet("/v0/user-parameters/user/" + userId.toString(), UserParameters.class);
    }

    public Mono<UserParameters> findUserParametersById(String id) {
        return executeGet("/v0/user-parameters/id/" + id, UserParameters.class);
    }

    public Mono<Boolean> deleteUserParameters(String id) {
        return executeDelete("/v0/user-parameters/id/" + id, Boolean.class);
    }

    // ==================== Plugins Endpoints ====================

    public Flux<Plugin> findNotDeletedPlugins() {
        return executeGetFlux("/v0/plugins/", Plugin.class);
    }

    public Mono<Plugin> savePlugin(Plugin plugin) {
        return executePost("/v0/plugins/", plugin, Plugin.class);
    }

    public Mono<Plugin> importContentNodePlugin(Plugin plugin) {
        return executePost("/v0/plugins/import", plugin, Plugin.class);
    }

    public Mono<Plugin> enablePlugin(UUID id) {
        return executePut("/v0/plugins/id/" + id.toString() + "/enable", Plugin.class);
    }

    public Mono<Plugin> disablePlugin(UUID id) {
        return executePut("/v0/plugins/id/" + id.toString() + "/disable", Plugin.class);
    }

    public Mono<Plugin> activatePlugin(UUID id) {
        return executePut("/v0/plugins/id/" + id.toString() + "/activate", Plugin.class);
    }

    public Mono<Plugin> findPluginById(UUID id) {
        return executeGet("/v0/plugins/id/" + id.toString(), Plugin.class);
    }

    public Mono<byte[]> exportPlugin(UUID id) {
        return executeGet("/v0/plugins/id/" + id.toString() + "/export", byte[].class);
    }

    public Flux<Plugin> findDeletedPlugins() {
        return executeGetFlux("/v0/plugins/deleteds", Plugin.class);
    }

    public Mono<Boolean> deletePlugin(UUID id) {
        return executeDelete("/v0/plugins/id/" + id.toString(), Boolean.class);
    }

    public Mono<Boolean> deletePluginDefinitively(UUID id) {
        return executeDelete("/v0/plugins/id/" + id.toString() + "/deleteDefinitively", Boolean.class);
    }

    // ==================== Plugin Files Endpoints ====================

    public Flux<PluginFile> findAllPluginFiles(Boolean enabled) {
        String uri = "/v0/plugin-files/";
        if (enabled != null) {
            uri += "?enabled=" + enabled;
        }
        return executeGetFlux(uri, PluginFile.class);
    }

    public Mono<PluginFile> savePluginFile(PluginFile pluginFile) {
        return executePost("/v0/plugin-files/", pluginFile, PluginFile.class);
    }

    public Flux<PluginFile> findPluginFilesByPluginId(String pluginId) {
        return executeGetFlux("/v0/plugin-files/plugin/" + pluginId, PluginFile.class);
    }

    public Flux<PluginFile> findPluginFilesByPluginName(String name) {
        return executeGetFlux("/v0/plugin-files/plugin/name/" + name, PluginFile.class);
    }

    public Mono<PluginFile> findPluginFileById(String id) {
        return executeGet("/v0/plugin-files/id/" + id, PluginFile.class);
    }

    public Mono<Boolean> deletePluginFile(String id) {
        return executeDelete("/v0/plugin-files/id/" + id, Boolean.class);
    }

    // ==================== Notifications Endpoints ====================

    public Flux<Notification> markAllNotificationsAsRead() {
        return executePostFlux("/v0/notifications/markAllAsRead", null, Notification.class);
    }

    public Mono<Notification> markNotificationAsRead(UUID notificationId) {
        return executePost("/v0/notifications/id/" + notificationId.toString() + "/markread", null, Notification.class);
    }

    // ==================== Languages Endpoints ====================

    public Flux<Language> findAllLanguages() {
        return executeGetFlux("/v0/languages/", Language.class);
    }

    public Mono<Language> saveLanguage(Language language) {
        return executePost("/v0/languages/", language, Language.class);
    }

    public Mono<Language> findLanguageById(String id) {
        return executeGet("/v0/languages/id/" + id, Language.class);
    }

    public Mono<Boolean> deleteLanguage(String id) {
        return executeDelete("/v0/languages/id/" + id, Boolean.class);
    }

    // ==================== Feedback Endpoints ====================

    public Flux<Feedback> findAllFeedback() {
        return executeGetFlux("/v0/feedbacks/", Feedback.class);
    }

    public Mono<Feedback> saveFeedback(Feedback feedback) {
        return executePost("/v0/feedbacks/", feedback, Feedback.class);
    }

    public Flux<Feedback> findFeedbackByVerified(boolean verified) {
        return executeGetFlux("/v0/feedbacks/verified/" + verified, Feedback.class);
    }

    public Flux<Feedback> findFeedbackByUserId(String userId) {
        return executeGetFlux("/v0/feedbacks/userId/" + userId, Feedback.class);
    }

    public Mono<Feedback> findFeedbackById(UUID id) {
        return executeGet("/v0/feedbacks/id/" + id.toString(), Feedback.class);
    }

    public Flux<Feedback> findFeedbackByEvaluation(int evaluation) {
        return executeGetFlux("/v0/feedbacks/evaluation/" + evaluation, Feedback.class);
    }

    public Flux<Feedback> findFeedbackByContentCode(String code) {
        return executeGetFlux("/v0/feedbacks/contentCode/" + code, Feedback.class);
    }

    public Flux<FeedbackCharts> getContentCharts() {
        return executeGetFlux("/v0/feedbacks/charts", FeedbackCharts.class);
    }

    public Mono<Boolean> deleteFeedback(UUID id) {
        return executeDelete("/v0/feedbacks/id/" + id.toString(), Boolean.class);
    }

    // ==================== Data Endpoints ====================

    public Mono<Data> saveData(Data data) {
        return executePost("/v0/datas/", data, Data.class);
    }

    public Mono<Data> findDataByKey(String key) {
        return executeGet("/v0/datas/key/" + key, Data.class);
    }

    public Flux<Data> findDataByContentCode(String code, PaginationParams params) {
        String uri = buildUri("/v0/datas/contentCode/" + code, params);
        return executeGetFlux(uri, Data.class);
    }

    public Mono<Long> countDataByContentCode(String code) {
        return executeGet("/v0/datas/contentCode/" + code + "/count", Long.class);
    }

    public Mono<Boolean> deleteAllDataByContentCode(String code) {
        return executeDelete("/v0/datas/contentCode/" + code, Boolean.class);
    }

    public Mono<Boolean> deleteDataById(UUID uuid) {
        return executeDelete("/v0/datas/id/" + uuid.toString(), Boolean.class);
    }

    // ==================== Nodes Endpoints ====================

    public Flux<Node> findAllNodes() {
        return executeGetFlux("/v0/nodes/", Node.class);
    }

    public Mono<Node> saveNode(Node node) {
        return executePost("/v0/nodes/", node, Node.class);
    }

    public Mono<Node> importNode(Node node) {
        return executePost("/v0/nodes/import", node, Node.class);
    }

    public Flux<Node> importNodes(List<Node> nodes, String nodeParentCode, Boolean fromFile) {
        String uri = "/v0/nodes/importAll";
        StringBuilder queryParams = new StringBuilder();
        if (nodeParentCode != null) {
            queryParams.append("nodeParentCode=").append(nodeParentCode);
        }
        if (fromFile != null) {
            if (queryParams.length() > 0) queryParams.append("&");
            queryParams.append("fromFile=").append(fromFile);
        }
        if (queryParams.length() > 0) {
            uri += "?" + queryParams.toString();
        }
        String finalUri = uri;
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(nodes))
                .flatMapMany(jsonBody -> handleResponse(
                        buildClientWithHeaders()
                                .post()
                                .uri(finalUri)
                                .send(ByteBufFlux.fromString(Mono.just(jsonBody))),
                        new TypeReference<List<Node>>() {})
                        .flatMapMany(Flux::fromIterable)
                );
    }

    public Mono<Node> revertNodeVersion(String code, String version) {
        return executePost("/v0/nodes/code/" + code + "/version/" + version + "/revert", null, Node.class);
    }

    public Mono<Boolean> deployNodeVersion(String code, String version, String environment) {
        String uri = "/v0/nodes/code/" + code + "/version/" + version + "/deploy";
        if (environment != null && !environment.isEmpty()) {
            uri += "?environment=" + environment;
        }
        return executePost(uri, null, Boolean.class);
    }

    public Mono<Node> publishNode(String code) {
        return executePost("/v0/nodes/code/" + code + "/publish", null, Node.class);
    }

    public Mono<Boolean> activateNode(String code) {
        return executePost("/v0/nodes/code/" + code + "/activate", null, Boolean.class);
    }

    public Flux<Node> findAllNodesByStatus(String status) {
        return executeGetFlux("/v0/nodes/status/" + status, Node.class);
    }

    public Flux<Node> findPublishedNodes() {
        return executeGetFlux("/v0/nodes/published", Node.class);
    }

    public Flux<Node> findParentNodesByStatus(String status) {
        return executeGetFlux("/v0/nodes/parent/status/" + status, Node.class);
    }

    public Flux<Node> findNodesByParentCode(String code) {
        return executeGetFlux("/v0/nodes/parent/code/" + code, Node.class);
    }

    public Flux<Node> findChildrenByCodeAndStatus(String code, String status) {
        return executeGetFlux("/v0/nodes/parent/code/" + code + "/status/" + status, Node.class);
    }

    public Flux<Node> findAllDescendants(String code) {
        return executeGetFlux("/v0/nodes/parent/code/" + code + "/descendants", Node.class);
    }

    public Flux<Node> findParentOrigin() {
        return executeGetFlux("/v0/nodes/origin", Node.class);
    }

    public Flux<Node> getDeletedNodes(String parent) {
        String uri = "/v0/nodes/deleted";
        if (parent != null && !parent.isEmpty()) {
            uri += "?parent=" + parent;
        }
        return executeGetFlux(uri, Node.class);
    }

    public Flux<Node> findNodesByCode(String code) {
        return executeGetFlux("/v0/nodes/code/" + code, Node.class);
    }

    public Mono<Node> findNodeByCodeAndStatus(String code, String status) {
        return executeGet("/v0/nodes/code/" + code + "/status/" + status, Node.class);
    }

    public Mono<TreeNode> generateTreeView(String code) {
        return executeGet("/v0/nodes/code/" + code + "/tree-view", TreeNode.class);
    }

    public Mono<Boolean> checkSlugExistsForNode(String code, String slug) {
        return executeGet("/v0/nodes/code/" + code + "/slug/" + slug + "/exists", Boolean.class);
    }

    public Mono<Boolean> hasContents(String code) {
        return executeGet("/v0/nodes/code/" + code + "/haveContents", Boolean.class);
    }

    public Mono<Boolean> hasChildren(String code) {
        return executeGet("/v0/nodes/code/" + code + "/haveChilds", Boolean.class);
    }

    public Mono<byte[]> exportAllNodes(String code, String environment) {
        String uri = "/v0/nodes/code/" + code + "/export";
        if (environment != null && !environment.isEmpty()) {
            uri += "?environment=" + environment;
        }
        return executeGet(uri, byte[].class);
    }

    public Flux<Node> deployNode(String code, String environment) {
        String uri = "/v0/nodes/code/" + code + "/deploy";
        if (environment != null && !environment.isEmpty()) {
            uri += "?environment=" + environment;
        }
        return executeGetFlux(uri, Node.class);
    }

    public Mono<Boolean> deleteNode(String code) {
        return executeDelete("/v0/nodes/code/" + code, Boolean.class);
    }

    public Mono<Boolean> deleteNodeDefinitively(String code) {
        return executeDelete("/v0/nodes/code/" + code + "/deleteDefinitively", Boolean.class);
    }

    public Mono<Boolean> deleteNodeVersionDefinitively(String code, String version) {
        return executeDelete("/v0/nodes/code/" + code + "/version/" + version + "/deleteDefinitively", Boolean.class);
    }

    // ==================== Content Nodes Endpoints ====================

    public Flux<ContentNode> findAllContentNodes() {
        return executeGetFlux("/v0/content-node/", ContentNode.class);
    }

    public Mono<ContentNode> saveContentNode(ContentNode contentNode) {
        return executePost("/v0/content-node/", contentNode, ContentNode.class);
    }

    public Mono<ContentNode> importContentNode(ContentNode contentNode, String nodeParentCode, Boolean fromFile) {
        String uri = "/v0/content-node/import";
        StringBuilder queryParams = new StringBuilder();
        if (nodeParentCode != null) {
            queryParams.append("nodeParentCode=").append(nodeParentCode);
        }
        if (fromFile != null) {
            if (queryParams.length() > 0) queryParams.append("&");
            queryParams.append("fromFile=").append(fromFile);
        }
        if (queryParams.length() > 0) {
            uri += "?" + queryParams.toString();
        }
        return executePost(uri, contentNode, ContentNode.class);
    }

    public Mono<ContentNode> revertContentNodeVersion(String code, String version) {
        return executePost("/v0/content-node/code/" + code + "/version/" + version + "/revert", null, ContentNode.class);
    }

    public Mono<Boolean> deployContentNodeVersion(String code, String version, String environment) {
        String uri = "/v0/content-node/code/" + code + "/version/" + version + "/deploy";
        if (environment != null && !environment.isEmpty()) {
            uri += "?environment=" + environment;
        }
        return executePost(uri, null, Boolean.class);
    }

    public Mono<ContentNode> fillContent(String code, String status, ContentNodePayload payload) {
        return executePost("/v0/content-node/code/" + code + "/status/" + status + "/fill", payload, ContentNode.class);
    }

    public Mono<ContentNode> publishContentNode(String code, boolean publish) {
        return executePost("/v0/content-node/code/" + code + "/publish/" + publish, null, ContentNode.class);
    }

    public Mono<Boolean> activateContentNode(String code) {
        return executePost("/v0/content-node/code/" + code + "/activate", null, Boolean.class);
    }

    public Flux<ContentNode> findAllContentNodesByStatus(String status) {
        return executeGetFlux("/v0/content-node/status/" + status, ContentNode.class);
    }

    public Flux<ContentNode> findAllContentNodesByNodeCode(String code) {
        return executeGetFlux("/v0/content-node/node/code/" + code, ContentNode.class);
    }

    public Flux<ContentNode> findContentNodesByNodeCodeAndStatus(String code, String status) {
        return executeGetFlux("/v0/content-node/node/code/" + code + "/status/" + status, ContentNode.class);
    }

    public Flux<ContentNode> getDeletedContentNodes(String parent) {
        String uri = "/v0/content-node/deleted";
        if (parent != null && !parent.isEmpty()) {
            uri += "?parent=" + parent;
        }
        return executeGetFlux(uri, ContentNode.class);
    }

    public Flux<ContentNode> findAllContentNodesByCode(String code) {
        return executeGetFlux("/v0/content-node/code/" + code, ContentNode.class);
    }

    public Mono<ContentNode> findContentNodeByCodeAndStatus(String code, String status) {
        return executeGet("/v0/content-node/code/" + code + "/status/" + status, ContentNode.class);
    }

    public Mono<Boolean> checkContentNodeSlugExists(String code, String slug) {
        return executeGet("/v0/content-node/code/" + code + "/slug/" + slug + "/exists", Boolean.class);
    }

    public Mono<byte[]> exportContentNode(String code, String environment) {
        String uri = "/v0/content-node/code/" + code + "/export";
        if (environment != null && !environment.isEmpty()) {
            uri += "?environment=" + environment;
        }
        return executeGet(uri, byte[].class);
    }

    public Mono<Boolean> deployContentNode(String code, String environment) {
        String uri = "/v0/content-node/code/" + code + "/deploy";
        if (environment != null && !environment.isEmpty()) {
            uri += "?environment=" + environment;
        }
        return executeGet(uri, Boolean.class);
    }

    public Mono<Boolean> deleteContentNode(String code) {
        return executeDelete("/v0/content-node/code/" + code, Boolean.class);
    }

    public Mono<Boolean> deleteContentNodeDefinitively(String code) {
        return executeDelete("/v0/content-node/code/" + code + "/deleteDefinitively", Boolean.class);
    }

    public Mono<Boolean> deleteContentNodeVersionDefinitively(String code, String version) {
        return executeDelete("/v0/content-node/code/" + code + "/version/" + version + "/deleteDefinitively", Boolean.class);
    }

    // ==================== Content Displays Endpoints ====================

    public Flux<ContentDisplay> findAllContentDisplays() {
        return executeGetFlux("/v0/content-displays/", ContentDisplay.class);
    }

    public Mono<ContentDisplay> saveContentDisplay(ContentDisplay contentDisplay) {
        return executePost("/v0/content-displays/", contentDisplay, ContentDisplay.class);
    }

    public Mono<ContentDisplay> findContentDisplayById(UUID id) {
        return executeGet("/v0/content-displays/id/" + id.toString(), ContentDisplay.class);
    }

    public Mono<ContentDisplay> findContentDisplayByContentCode(String code) {
        return executeGet("/v0/content-displays/contentCode/" + code, ContentDisplay.class);
    }

    public Flux<ContentDisplayCharts> getContentDisplayCharts() {
        return executeGetFlux("/v0/content-displays/charts", ContentDisplayCharts.class);
    }

    public Mono<Boolean> deleteContentDisplay(UUID id) {
        return executeDelete("/v0/content-displays/id/" + id.toString(), Boolean.class);
    }

    // ==================== Content Clicks Endpoints ====================

    public Flux<ContentClick> findAllContentClicks() {
        return executeGetFlux("/v0/content-clicks/", ContentClick.class);
    }

    public Mono<ContentClick> saveContentClick(ContentClick contentClick) {
        return executePost("/v0/content-clicks/", contentClick, ContentClick.class);
    }

    public Mono<ContentClick> findContentClickById(UUID id) {
        return executeGet("/v0/content-clicks/id/" + id.toString(), ContentClick.class);
    }

    public Flux<ContentClick> findContentClickByContentCode(String code) {
        return executeGetFlux("/v0/content-clicks/contentCode/" + code, ContentClick.class);
    }

    public Flux<ContentClickCharts> getContentClickCharts() {
        return executeGetFlux("/v0/content-clicks/charts", ContentClickCharts.class);
    }

    public Mono<Boolean> deleteContentClick(UUID id) {
        return executeDelete("/v0/content-clicks/id/" + id.toString(), Boolean.class);
    }

    // ==================== Access Roles Endpoints ====================

    public Flux<AccessRole> findAllAccessRoles() {
        return executeGetFlux("/v0/access-roles/", AccessRole.class);
    }

    public Mono<AccessRole> saveAccessRole(AccessRole accessRole) {
        return executePost("/v0/access-roles/", accessRole, AccessRole.class);
    }

    public Mono<AccessRole> findAccessRoleById(String id) {
        return executeGet("/v0/access-roles/id/" + id, AccessRole.class);
    }

    public Mono<Boolean> deleteAccessRole(String id) {
        return executeDelete("/v0/access-roles/id/" + id, Boolean.class);
    }

    // ==================== Charts Endpoints ====================

    public Mono<TreeNode> getCharts() {
        return executeGet("/v0/charts/", TreeNode.class);
    }

    // ==================== Locks Endpoints ====================

    public Mono<Boolean> acquireLock(String code) {
        return executePost("/v0/locks/acquire/" + code, null, Boolean.class);
    }

    public Mono<Boolean> refreshLock(String code) {
        return executePost("/v0/locks/refresh/" + code, null, Boolean.class);
    }

    public Mono<Boolean> releaseLock(String code) {
        return executePost("/v0/locks/release/" + code, null, Boolean.class);
    }

    public Mono<Boolean> adminReleaseLock(String code) {
        return executePost("/v0/locks/admin/release/" + code, null, Boolean.class);
    }

    public Mono<LockInfo> getLockOwner(String code) {
        return executeGet("/v0/locks/owner/" + code, LockInfo.class);
    }

    public Flux<LockInfo> getAllLocks() {
        return executeGetFlux("/v0/locks/all", LockInfo.class);
    }

    // ==================== Slug Controller ====================

    public Flux<String> checkSlugExists(String slug) {
        return executeGetFlux("/v0/slugs/exists/" + slug, String.class);
    }
}