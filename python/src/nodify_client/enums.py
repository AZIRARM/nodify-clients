"""
All enums for Nodify client
"""

from enum import Enum


class ApplicationEnum(str, Enum):
    LICENCE_KEY = "LICENCE_KEY"
    LICENCE_PRIVATE_KEY = "LICENCE_PRIVATE_KEY"


class BehaviorEnum(str, Enum):
    ENABLED = "ENABLED"
    DISABLED = "DISABLED"


class ContentTypeEnum(str, Enum):
    FILE = "FILE"
    PICTURE = "PICTURE"
    SCRIPT = "SCRIPT"
    STYLE = "STYLE"
    HTML = "HTML"
    JSON = "JSON"
    URLS = "URLS"
    DATA = "DATA"
    XML = "XML"


class JoinEnum(str, Enum):
    AND = "AND"
    OR = "OR"
    NAND = "NAND"
    NOR = "NOR"


class LanguagesEnum(str, Enum):
    FR = "fr"
    EN = "en"
    ES = "sp"
    AR = "ar"

    def __new__(cls, label: str):
        obj = str.__new__(cls, label)
        obj._value_ = label
        obj.label = label
        obj.url_flag = None
        return obj


class LicenseTypeEnum(str, Enum):
    FREE = "FREE"
    BRONZE = "BRONZE"
    SILVER = "SILVER"
    GOLD = "GOLD"
    PLATINUM = "PLATINUM"

    # Dictionnaires pour les valeurs associées
    _max_users = {
        "FREE": 1,
        "BRONZE": 2,
        "SILVER": 10,
        "GOLD": 50,
        "PLATINUM": 1000000
    }

    _max_nodes = {
        "FREE": 2,
        "BRONZE": 5,
        "SILVER": 10,
        "GOLD": 50,
        "PLATINUM": 1000000
    }

    def get_max_users(self):
        return self._max_users[self.value]

    def get_max_nodes(self):
        return self._max_nodes[self.value]


class NotificationEnum(str, Enum):
    CREATION = "CREATION"
    DELETION = "DELETION"
    DELETION_DEFINITIVELY = "DELETION_DEFINITIVELY"
    ARCHIVING = "ARCHIVING"
    DEACTIVATION = "DEACTIVATION"
    ACTIVATION = "ACTIVATION"
    REACTIVATION = "REACTIVATION"
    DEPLOYMENT = "DEPLOYMENT"
    UPDATE = "UPDATE"
    REVERT = "REVERT"
    EXPORT = "EXPORT"
    CREATION_OR_UPDATE = "CREATION_OR_UPDATE"
    PASSWORD_CHANGE = "PASSWORD_CHANGE"
    DEPLOYMENT_VERSION = "DEPLOYMENT_VERSION"
    IMPORT = "IMPORT"


class OperatorEnum(str, Enum):
    EQ = "EQ"
    SUP = "SUP"
    LOW = "LOW"
    SUP_EQ = "SUP_EQ"
    LOW_EQ = "LOW_EQ"
    DIF = "DIF"


class StatusEnum(str, Enum):
    SNAPSHOT = "SNAPSHOT"
    PUBLISHED = "PUBLISHED"
    ARCHIVE = "ARCHIVE"
    DELETED = "DELETED"
    NEW = "NEW"


class TypeEnum(str, Enum):
    BOOL = "BOOL"
    FLOAT = "FLOAT"
    NUM = "NUM"
    DATE = "DATE"
    STRING = "STRING"


class UrlTypeEnum(str, Enum):
    API = "API"
    PAGE = "PAGE"
    SCRIPT = "SCRIPT"
    STYLE = "STYLE"
    MEDIA = "MEDIA"


class RoleEnum(str, Enum):
    ADMIN = "ADMIN"
    EDITOR = "EDITOR"
    READER = "READER"