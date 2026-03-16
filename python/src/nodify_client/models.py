"""
All models for Nodify client
"""

from dataclasses import dataclass, field
from typing import Optional, List, Dict, Any
from urllib.parse import urlencode

from nodify_client.enums import (
    ContentTypeEnum, StatusEnum, UrlTypeEnum,
    TypeEnum, OperatorEnum, LicenseTypeEnum
)


@dataclass
class PaginationParams:
    """Pagination parameters for list endpoints"""
    current_page: Optional[int] = None
    limit: Optional[int] = None

    def to_query_string(self) -> str:
        params = {}
        if self.current_page is not None:
            params["currentPage"] = self.current_page
        if self.limit is not None:
            params["limit"] = self.limit
        return urlencode(params)


# Auth models
@dataclass
class AuthRequest:
    username: Optional[str] = None
    password: Optional[str] = None


@dataclass
class AuthResponse:
    token: Optional[str] = None


@dataclass
class UserLogin:
    email: str
    password: str


@dataclass
class Message:
    content: Optional[str] = None


# User models
@dataclass
class UserPost:
    id: Optional[str] = None
    firstname: Optional[str] = None
    lastname: Optional[str] = None
    email: Optional[str] = None
    password: Optional[str] = None
    roles: Optional[List[str]] = None
    projects: Optional[List[str]] = None


@dataclass
class UserRole:
    id: Optional[str] = None
    name: Optional[str] = None
    description: Optional[str] = None
    code: Optional[str] = None


@dataclass
class UserParameters:
    id: Optional[str] = None
    user_id: Optional[str] = None
    accept_notifications: bool = False
    default_language: Optional[str] = None
    theme: Optional[str] = None
    ai: bool = False


@dataclass
class UserPassword:
    user_id: Optional[str] = None
    password: Optional[str] = None
    new_password: Optional[str] = None


@dataclass
class User:
    id: Optional[str] = None
    firstname: Optional[str] = None
    lastname: Optional[str] = None


@dataclass
class UserLicense:
    email: Optional[str] = None
    first_name: Optional[str] = None
    last_name: Optional[str] = None


# Common models
@dataclass
class Value:
    id: Optional[str] = None
    key: Optional[str] = None
    value: Optional[str] = None


@dataclass
class Rule:
    type: Optional[TypeEnum] = None
    name: Optional[str] = None
    value: Optional[str] = None
    editable: bool = False
    erasable: bool = False
    operator: Optional[OperatorEnum] = None
    behavior: Optional[bool] = None
    enable: Optional[bool] = None
    description: Optional[str] = None


@dataclass
class Translation:
    language: Optional[str] = None
    key: Optional[str] = None
    value: Optional[str] = None


@dataclass
class AccessRole:
    id: Optional[str] = None
    name: Optional[str] = None
    description: Optional[str] = None
    code: Optional[str] = None


@dataclass
class Data:
    id: Optional[str] = None
    content_node_code: Optional[str] = None
    creation_date: Optional[int] = None
    modification_date: Optional[int] = None
    data_type: Optional[str] = None
    name: Optional[str] = None
    user: Optional[str] = None
    key: Optional[str] = None
    value: Optional[str] = None


# Content models
@dataclass
class ContentFile:
    name: Optional[str] = None
    type: Optional[str] = None
    data: Optional[str] = None
    size: int = 0


@dataclass
class ContentUrl:
    id: Optional[str] = None
    url: Optional[str] = None
    description: Optional[str] = None
    type: Optional[UrlTypeEnum] = None


@dataclass
class ContentNode:
    id: Optional[str] = None
    parent_code: Optional[str] = None
    parent_code_origin: Optional[str] = None
    code: Optional[str] = None
    slug: Optional[str] = None
    environment_code: Optional[str] = None
    language: Optional[str] = None
    type: Optional[ContentTypeEnum] = None
    title: Optional[str] = None
    description: Optional[str] = None
    redirect_url: Optional[str] = None
    icon_url: Optional[str] = None
    picture_url: Optional[str] = None
    content: Optional[str] = None
    urls: Optional[List[ContentUrl]] = None
    file: Optional[ContentFile] = None
    tags: Optional[List[str]] = None
    values: Optional[List[Value]] = None
    roles: Optional[List[str]] = None
    rules: Optional[List[Rule]] = None
    creation_date: Optional[int] = None
    modification_date: Optional[int] = None
    modified_by: Optional[str] = None
    version: Optional[str] = None
    publication_date: Optional[int] = None
    status: Optional[StatusEnum] = None
    max_versions_to_keep: Optional[int] = None
    favorite: bool = False
    publication_status: Optional[str] = None
    translations: Optional[List[Translation]] = None


@dataclass
class ContentNodePayload:
    code: Optional[str] = None
    content: Optional[str] = None
    status: Optional[str] = None


@dataclass
class ContentStatsDTO:
    content_code: Optional[str] = None
    displays: int = 0
    clicks: int = 0
    feedbacks_per_evaluation: Optional[Dict[int, int]] = None


@dataclass
class ContentClick:
    id: Optional[str] = None
    content_code: Optional[str] = None
    clicks: Optional[int] = None


@dataclass
class ContentClickCharts:
    name: Optional[str] = None
    value: Optional[str] = None


@dataclass
class ContentDisplay:
    id: Optional[str] = None
    content_code: Optional[str] = None
    displays: Optional[int] = None


@dataclass
class ContentDisplayCharts:
    name: Optional[str] = None
    value: Optional[str] = None


# Node models
@dataclass
class Node:
    id: Optional[str] = None
    parent_code: Optional[str] = None
    parent_code_origin: Optional[str] = None
    name: Optional[str] = None
    code: Optional[str] = None
    slug: Optional[str] = None
    environment_code: Optional[str] = None
    description: Optional[str] = None
    default_language: Optional[str] = None
    type: Optional[str] = None
    sub_nodes: Optional[List[str]] = None
    contents: Optional[List[ContentNode]] = None
    tags: Optional[List[str]] = None
    values: Optional[List[Value]] = None
    roles: Optional[List[str]] = None
    rules: Optional[List[Rule]] = None
    languages: Optional[List[str]] = None
    creation_date: Optional[int] = None
    modification_date: Optional[int] = None
    modified_by: Optional[str] = None
    version: Optional[str] = None
    publication_date: Optional[int] = None
    status: Optional[StatusEnum] = None
    max_versions_to_keep: Optional[int] = None
    favorite: bool = False
    publication_status: Optional[str] = None
    translations: Optional[List[Translation]] = None


# Plugin models
@dataclass
class PluginFile:
    id: Optional[str] = None
    plugin_id: Optional[str] = None
    file_name: Optional[str] = None
    description: Optional[str] = None
    data: Optional[str] = None


@dataclass
class Plugin:
    id: Optional[str] = None
    enabled: bool = False
    editable: bool = False
    description: Optional[str] = None
    name: Optional[str] = None
    code: Optional[str] = None
    entrypoint: Optional[str] = None
    creation_date: Optional[int] = None
    modification_date: Optional[int] = None
    modified_by: Optional[str] = None
    deleted: bool = False
    resources: Optional[List[PluginFile]] = None


# Feedback models
@dataclass
class Feedback:
    id: Optional[str] = None
    content_code: Optional[str] = None
    created_date: Optional[int] = None
    modified_date: Optional[int] = None
    evaluation: int = 0
    message: Optional[str] = None
    user_id: Optional[str] = None
    verified: bool = False


@dataclass
class Chart:
    name: Optional[str] = None
    value: Optional[str] = None
    verified: bool = False


@dataclass
class FeedbackCharts:
    content_code: Optional[str] = None
    charts: Optional[List[Chart]] = None
    verified: Optional[List[Chart]] = None
    not_verified: Optional[List[Chart]] = None


# Other models
@dataclass
class Notification:
    id: Optional[str] = None
    type: Optional[str] = None
    type_code: Optional[str] = None
    type_version: Optional[str] = None
    code: Optional[str] = None
    date: Optional[int] = None
    description: Optional[str] = None
    user: Optional[str] = None
    modified_by: Optional[str] = None
    read: bool = False


@dataclass
class LockInfo:
    owner: Optional[str] = None
    mine: Optional[bool] = None
    locked: Optional[bool] = None
    resource_code: Optional[str] = None


@dataclass
class Language:
    id: Optional[str] = None
    name: Optional[str] = None
    code: Optional[str] = None
    url_icon: Optional[str] = None
    description: Optional[str] = None


@dataclass
class License:
    id: Optional[str] = None
    user_name: Optional[str] = None
    licence: Optional[str] = None
    type: Optional[LicenseTypeEnum] = None
    product: Optional[str] = None
    version: Optional[str] = None
    customer: Optional[str] = None
    creation_date: Optional[int] = None
    modification_date: Optional[int] = None
    start_date: Optional[int] = None
    end_date: Optional[int] = None
    count_licences_requested: Optional[int] = None


@dataclass
class Environment:
    id: Optional[str] = None
    description: Optional[str] = None
    code: Optional[str] = None
    name: Optional[str] = None
    node_code: Optional[str] = None


@dataclass
class TreeNode:
    name: Optional[str] = None
    code: Optional[str] = None
    type: Optional[str] = None
    is_leaf: bool = False
    value: Optional[str] = None
    children: Optional[List['TreeNode']] = None