<?php

namespace Nodify;

use DateTime;

class PaginationParams
{
    public ?int $currentPage = null;
    public ?int $limit = null;

    public function __construct(?int $currentPage = null, ?int $limit = null)
    {
        $this->currentPage = $currentPage;
        $this->limit = $limit;
    }

    public function toQueryString(): string
    {
        $params = [];
        if ($this->currentPage !== null) {
            $params['currentPage'] = $this->currentPage;
        }
        if ($this->limit !== null) {
            $params['limit'] = $this->limit;
        }
        return http_build_query($params);
    }
}

// Auth models
class UserLogin
{
    public string $email;
    public string $password;

    public function __construct(string $email, string $password)
    {
        $this->email = $email;
        $this->password = $password;
    }
}

class AuthResponse
{
    public ?string $token = null;

    public function __construct(?string $token = null)
    {
        $this->token = $token;
    }
}

class Message
{
    public ?string $content = null;
}

// User models
class UserPost
{
    public ?string $id = null;
    public ?string $firstname = null;
    public ?string $lastname = null;
    public ?string $email = null;
    public ?string $password = null;
    public ?array $roles = null;
    public ?array $projects = null;
}

class UserRole
{
    public ?string $id = null;
    public ?string $name = null;
    public ?string $description = null;
    public ?string $code = null;
}

class UserParameters
{
    public ?string $id = null;
    public ?string $userId = null;
    public bool $acceptNotifications = false;
    public ?string $defaultLanguage = null;
    public ?string $theme = null;
    public bool $ai = false;
}

class UserPassword
{
    public ?string $userId = null;
    public ?string $password = null;
    public ?string $newPassword = null;
}

class User
{
    public ?string $id = null;
    public ?string $firstname = null;
    public ?string $lastname = null;
}

class UserLicense
{
    public ?string $email = null;
    public ?string $firstName = null;
    public ?string $lastName = null;
}

// Common models
class Value
{
    public ?string $id = null;
    public ?string $key = null;
    public ?string $value = null;
}

class Rule
{
    public ?TypeEnum $type = null;
    public ?string $name = null;
    public ?string $value = null;
    public bool $editable = false;
    public bool $erasable = false;
    public ?OperatorEnum $operator = null;
    public ?bool $behavior = null;
    public ?bool $enable = null;
    public ?string $description = null;
}

class Translation
{
    public ?string $language = null;
    public ?string $key = null;
    public ?string $value = null;

    public function __construct(?string $key = null, ?string $language = null, ?string $value = null)
    {
        $this->key = $key;
        $this->language = $language;
        $this->value = $value;
    }
}

class AccessRole
{
    public ?string $id = null;
    public ?string $name = null;
    public ?string $description = null;
    public ?string $code = null;
}

class Data
{
    public ?string $id = null;
    public ?string $contentNodeCode = null;
    public ?int $creationDate = null;
    public ?int $modificationDate = null;
    public ?string $dataType = null;
    public ?string $name = null;
    public ?string $user = null;
    public ?string $key = null;
    public ?string $value = null;
}

// Content models
class ContentFile
{
    public ?string $name = null;
    public ?string $type = null;
    public ?string $data = null;
    public int $size = 0;
}

class ContentUrl
{
    public ?string $id = null;
    public ?string $url = null;
    public ?string $description = null;
    public ?UrlTypeEnum $type = null;
}

class ContentNode
{
    public ?string $id = null;
    public ?string $parentCode = null;
    public ?string $parentCodeOrigin = null;
    public ?string $code = null;
    public ?string $slug = null;
    public ?string $environmentCode = null;
    public ?string $language = null;
    public ?ContentTypeEnum $type = null;
    public ?string $title = null;
    public ?string $description = null;
    public ?string $redirectUrl = null;
    public ?string $iconUrl = null;
    public ?string $pictureUrl = null;
    public ?string $content = null;
    public ?array $urls = null;
    public ?ContentFile $file = null;
    public ?array $tags = null;
    public ?array $values = null;
    public ?array $roles = null;
    public ?array $rules = null;
    public ?int $creationDate = null;
    public ?int $modificationDate = null;
    public ?string $modifiedBy = null;
    public ?string $version = null;
    public ?int $publicationDate = null;
    public ?StatusEnum $status = null;
    public ?int $maxVersionsToKeep = null;
    public bool $favorite = false;
    public ?string $publicationStatus = null;
    public ?array $translations = null;
}

class ContentNodePayload
{
    public ?string $code = null;
    public ?string $content = null;
    public ?string $status = null;
}

class ContentStatsDTO
{
    public ?string $contentCode = null;
    public int $displays = 0;
    public int $clicks = 0;
    public ?array $feedbacksPerEvaluation = null;
}

class ContentClick
{
    public ?string $id = null;
    public ?string $contentCode = null;
    public ?int $clicks = null;
}

class ContentClickCharts
{
    public ?string $name = null;
    public ?string $value = null;
}

class ContentDisplay
{
    public ?string $id = null;
    public ?string $contentCode = null;
    public ?int $displays = null;
}

class ContentDisplayCharts
{
    public ?string $name = null;
    public ?string $value = null;
}

// Node models
class Node
{
    public ?string $id = null;
    public ?string $parentCode = null;
    public ?string $parentCodeOrigin = null;
    public ?string $name = null;
    public ?string $code = null;
    public ?string $slug = null;
    public ?string $environmentCode = null;
    public ?string $description = null;
    public ?string $defaultLanguage = null;
    public ?string $type = null;
    public ?array $subNodes = null;
    public ?array $contents = null;
    public ?array $tags = null;
    public ?array $values = null;
    public ?array $roles = null;
    public ?array $rules = null;
    public ?array $languages = null;
    public ?int $creationDate = null;
    public ?int $modificationDate = null;
    public ?string $modifiedBy = null;
    public ?string $version = null;
    public ?int $publicationDate = null;
    public ?StatusEnum $status = null;
    public ?int $maxVersionsToKeep = null;
    public bool $favorite = false;
    public ?string $publicationStatus = null;
    public ?array $translations = null;
}

// Plugin models
class PluginFile
{
    public ?string $id = null;
    public ?string $pluginId = null;
    public ?string $fileName = null;
    public ?string $description = null;
    public ?string $data = null;
}

class Plugin
{
    public ?string $id = null;
    public bool $enabled = false;
    public bool $editable = false;
    public ?string $description = null;
    public ?string $name = null;
    public ?string $code = null;
    public ?string $entrypoint = null;
    public ?int $creationDate = null;
    public ?int $modificationDate = null;
    public ?string $modifiedBy = null;
    public bool $deleted = false;
    public ?array $resources = null;
}

// Feedback models
class Feedback
{
    public ?string $id = null;
    public ?string $contentCode = null;
    public ?int $createdDate = null;
    public ?int $modifiedDate = null;
    public int $evaluation = 0;
    public ?string $message = null;
    public ?string $userId = null;
    public bool $verified = false;
}

class Chart
{
    public ?string $name = null;
    public ?string $value = null;
    public bool $verified = false;
}

class FeedbackCharts
{
    public ?string $contentCode = null;
    public ?array $charts = null;
    public ?array $verified = null;
    public ?array $notVerified = null;
}

// Other models
class Notification
{
    public ?string $id = null;
    public ?string $type = null;
    public ?string $typeCode = null;
    public ?string $typeVersion = null;
    public ?string $code = null;
    public ?int $date = null;
    public ?string $description = null;
    public ?string $user = null;
    public ?string $modifiedBy = null;
    public bool $read = false;
}

class LockInfo
{
    public ?string $owner = null;
    public ?bool $mine = null;
    public ?bool $locked = null;
    public ?string $resourceCode = null;
}

class Language
{
    public ?string $id = null;
    public ?string $name = null;
    public ?string $code = null;
    public ?string $urlIcon = null;
    public ?string $description = null;
}

class License
{
    public ?string $id = null;
    public ?string $userName = null;
    public ?string $licence = null;
    public ?LicenseTypeEnum $type = null;
    public ?string $product = null;
    public ?string $version = null;
    public ?string $customer = null;
    public ?int $creationDate = null;
    public ?int $modificationDate = null;
    public ?int $startDate = null;
    public ?int $endDate = null;
    public ?int $countLicencesRequested = null;
}

class Environment
{
    public ?string $id = null;
    public ?string $description = null;
    public ?string $code = null;
    public ?string $name = null;
    public ?string $nodeCode = null;
}

class TreeNode
{
    public ?string $name = null;
    public ?string $code = null;
    public ?string $type = null;
    public bool $isLeaf = false;
    public ?string $value = null;
    public ?array $children = null;
}