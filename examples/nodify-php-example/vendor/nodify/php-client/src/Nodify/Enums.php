<?php

namespace Nodify;

enum ApplicationEnum: string
{
    case LICENCE_KEY = "LICENCE_KEY";
    case LICENCE_PRIVATE_KEY = "LICENCE_PRIVATE_KEY";
}

enum BehaviorEnum: string
{
    case ENABLED = "ENABLED";
    case DISABLED = "DISABLED";
}

enum ContentTypeEnum: string
{
    case FILE = "FILE";
    case PICTURE = "PICTURE";
    case SCRIPT = "SCRIPT";
    case STYLE = "STYLE";
    case HTML = "HTML";
    case JSON = "JSON";
    case URLS = "URLS";
    case DATA = "DATA";
    case XML = "XML";
}

enum JoinEnum: string
{
    case AND = "AND";
    case OR = "OR";
    case NAND = "NAND";
    case NOR = "NOR";
}

enum LanguagesEnum: string
{
    case FR = "fr";
    case EN = "en";
    case ES = "sp";
    case AR = "ar";

    public function getLabel(): string
    {
        return match($this) {
            self::FR => "Français",
            self::EN => "English",
            self::ES => "Español",
            self::AR => "العربية",
        };
    }
}

enum LicenseTypeEnum: string
{
    case FREE = "FREE";
    case BRONZE = "BRONZE";
    case SILVER = "SILVER";
    case GOLD = "GOLD";
    case PLATINUM = "PLATINUM";

    public function getMaxUsers(): int
    {
        return match($this) {
            self::FREE => 1,
            self::BRONZE => 2,
            self::SILVER => 10,
            self::GOLD => 50,
            self::PLATINUM => 1000000,
        };
    }

    public function getMaxNodes(): int
    {
        return match($this) {
            self::FREE => 2,
            self::BRONZE => 5,
            self::SILVER => 10,
            self::GOLD => 50,
            self::PLATINUM => 1000000,
        };
    }
}

enum NotificationEnum: string
{
    case CREATION = "CREATION";
    case DELETION = "DELETION";
    case DELETION_DEFINITIVELY = "DELETION_DEFINITIVELY";
    case ARCHIVING = "ARCHIVING";
    case DEACTIVATION = "DEACTIVATION";
    case ACTIVATION = "ACTIVATION";
    case REACTIVATION = "REACTIVATION";
    case DEPLOYMENT = "DEPLOYMENT";
    case UPDATE = "UPDATE";
    case REVERT = "REVERT";
    case EXPORT = "EXPORT";
    case CREATION_OR_UPDATE = "CREATION_OR_UPDATE";
    case PASSWORD_CHANGE = "PASSWORD_CHANGE";
    case DEPLOYMENT_VERSION = "DEPLOYMENT_VERSION";
    case IMPORT = "IMPORT";
}

enum OperatorEnum: string
{
    case EQ = "EQ";
    case SUP = "SUP";
    case LOW = "LOW";
    case SUP_EQ = "SUP_EQ";
    case LOW_EQ = "LOW_EQ";
    case DIF = "DIF";
}

enum StatusEnum: string
{
    case SNAPSHOT = "SNAPSHOT";
    case PUBLISHED = "PUBLISHED";
    case ARCHIVE = "ARCHIVE";
    case DELETED = "DELETED";
    case NEW = "NEW";
}

enum TypeEnum: string
{
    case BOOL = "BOOL";
    case FLOAT = "FLOAT";
    case NUM = "NUM";
    case DATE = "DATE";
    case STRING = "STRING";
}

enum UrlTypeEnum: string
{
    case API = "API";
    case PAGE = "PAGE";
    case SCRIPT = "SCRIPT";
    case STYLE = "STYLE";
    case MEDIA = "MEDIA";
}

enum RoleEnum: string
{
    case ADMIN = "ADMIN";
    case EDITOR = "EDITOR";
    case READER = "READER";
}