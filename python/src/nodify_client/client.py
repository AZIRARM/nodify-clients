"""
Main client for Nodify API - Python equivalent of the Java ReactiveNodifyClient
Provides asynchronous access to all Nodify API endpoints
"""

import asyncio
import json
from typing import Any, Dict, List, Optional, Type, TypeVar, Union
from urllib.parse import urljoin
from dataclasses import asdict, is_dataclass
import re

import aiohttp
from aiohttp import ClientTimeout, ClientResponse, ClientSession

from nodify_client.config import ReactiveNodifyClientConfig
from nodify_client.exceptions import NodifyClientException
from nodify_client.models import (
    PaginationParams, AuthResponse, UserLogin, UserPost, UserPassword,
    UserRole, UserParameters, Plugin, PluginFile, Notification, Language,
    Feedback, FeedbackCharts, Data, Node, ContentNode, ContentNodePayload,
    ContentDisplay, ContentDisplayCharts, ContentClick, ContentClickCharts,
    AccessRole, TreeNode, LockInfo, User, Translation, Value, Rule
)

# Type variable for generic methods
T = TypeVar('T')


class ReactiveNodifyClient:
    """
    Reactive Nodify Client - Python equivalent of the Java ReactiveNodifyClient
    Provides asynchronous access to all Nodify API endpoints
    """

    def __init__(self, config: ReactiveNodifyClientConfig):
        """Initialize the client with configuration"""
        self.config = config
        self._session: Optional[ClientSession] = None

    @classmethod
    def create(cls, config: ReactiveNodifyClientConfig) -> 'ReactiveNodifyClient':
        """Create a new client instance"""
        return cls(config)

    @classmethod
    def builder(cls) -> ReactiveNodifyClientConfig.Builder:
        """Get a configuration builder"""
        return ReactiveNodifyClientConfig.builder()

    async def _get_session(self) -> ClientSession:
        """Get or create the HTTP session"""
        if self._session is None or self._session.closed:
            timeout = ClientTimeout(total=self.config.timeout / 1000)
            headers = self._build_headers()
            self._session = aiohttp.ClientSession(
                timeout=timeout,
                headers=headers
            )
        return self._session

    def _build_headers(self) -> Dict[str, str]:
        """Build headers for the request"""
        headers = self.config.default_headers.copy()
        if self.config.auth_token:
            headers["Authorization"] = f"Bearer {self.config.auth_token}"
        return headers

    def _build_url(self, path: str, params: Optional[PaginationParams] = None) -> str:
        """Build the full URL with query parameters"""
        url = urljoin(self.config.base_url, path)
        if params and params.to_query_string():
            url = f"{url}?{params.to_query_string()}"
        return url

    def _snake_to_camel(self, snake_str: str) -> str:
        """
        Convert snake_case to camelCase.
        Example: parent_code -> parentCode
        """
        components = snake_str.split('_')
        # Capitalize the first letter of each component except the first one
        return components[0] + ''.join(x.title() for x in components[1:])

    def _dataclass_to_dict(self, obj: Any) -> Any:
        """
        Recursively convert a dataclass to a dictionary for JSON serialization.
        Converts snake_case field names to camelCase for the server.
        Handles lists of dataclasses, nested dataclasses, and enum values.
        """
        if obj is None:
            return None

        # Handle lists
        if isinstance(obj, list):
            return [self._dataclass_to_dict(item) for item in obj]

        # Handle dataclasses
        if is_dataclass(obj) and not isinstance(obj, type):
            result = {}
            for key, value in obj.__dict__.items():
                if value is not None:  # Skip None values
                    # Convert snake_case to camelCase for the server
                    camel_key = self._snake_to_camel(key)
                    result[camel_key] = self._dataclass_to_dict(value)
            return result

        # Handle enums (they have a value attribute)
        if hasattr(obj, 'value') and hasattr(obj, '__class__') and obj.__class__.__name__.endswith('Enum'):
            return obj.value

        # Handle basic types
        return obj

    def _camel_to_snake(self, camel_str: str) -> str:
        """
        Convert camelCase to snake_case.
        Example: parentCode -> parent_code
        """
        pattern = re.compile(r'(?<!^)(?=[A-Z])')
        return pattern.sub('_', camel_str).lower()

    def _dict_to_dataclass(self, data: Dict, cls: Type[T]) -> T:
        """
        Convert a dictionary to a dataclass instance.
        Converts camelCase field names from server to snake_case for Python.
        Handles nested dataclasses and lists of dataclasses.
        """
        if data is None:
            return None

        if not isinstance(data, dict):
            return data

        # Get all fields from the dataclass
        if not hasattr(cls, '__dataclass_fields__'):
            return data

        field_types = cls.__dataclass_fields__
        converted_data = {}

        # Create a mapping of possible camelCase versions of field names
        camel_to_snake_map = {}
        for field_name in field_types.keys():
            camel_name = self._snake_to_camel(field_name)
            camel_to_snake_map[camel_name] = field_name

        for key, value in data.items():
            # Convert camelCase key from server to snake_case for Python
            snake_key = self._camel_to_snake(key)

            # If the converted key doesn't match any field, try the direct mapping
            if snake_key not in field_types and key in camel_to_snake_map:
                snake_key = camel_to_snake_map[key]

            if snake_key not in field_types:
                continue

            field_type = field_types[snake_key]
            field_class = field_type.type

            # Handle Optional types
            if hasattr(field_class, '__origin__') and field_class.__origin__ is Union:
                # Extract the actual type from Optional[Type]
                inner_types = [t for t in field_class.__args__ if t is not type(None)]
                if inner_types:
                    field_class = inner_types[0]

            # Handle lists
            if hasattr(field_class, '__origin__') and field_class.__origin__ is list:
                if value and isinstance(value, list):
                    item_type = field_class.__args__[0]
                    converted_data[snake_key] = [
                        self._dict_to_dataclass(item, item_type) if hasattr(item_type, '__dataclass_fields__') else item
                        for item in value
                    ]
                else:
                    converted_data[snake_key] = value

            # Handle nested dataclasses
            elif hasattr(field_class, '__dataclass_fields__') and value and isinstance(value, dict):
                converted_data[snake_key] = self._dict_to_dataclass(value, field_class)

            # Handle enums
            elif hasattr(field_class, '__bases__') and 'Enum' in [b.__name__ for b in field_class.__bases__]:
                try:
                    converted_data[snake_key] = field_class(value) if value is not None else None
                except ValueError:
                    converted_data[snake_key] = value

            # Handle basic types
            else:
                converted_data[snake_key] = value

        return cls(**converted_data)

    async def _handle_response(self, response: ClientResponse, response_type: Type[T]) -> T:
        """Handle the HTTP response and convert to the expected type"""
        if response.status < 200 or response.status >= 300:
            body = await response.text()
            raise NodifyClientException(
                f"Request failed with status: {response.status} - {body}",
                response.status
            )

        if response.status == 204 or response_type == type(None):
            return None

        if response_type == str:
            return await response.text()

        if response_type == bool:
            text = await response.text()
            return text.lower() == "true"

        if response_type == bytes:
            return await response.read()

        # Parse JSON
        data = await response.json()

        # If response_type is a dataclass, convert the dict to an object
        if hasattr(response_type, '__dataclass_fields__'):
            return self._dict_to_dataclass(data, response_type)

        return data

    async def _handle_list_response(self, response: ClientResponse, element_type: Type[T]) -> List[T]:
        """Handle response that returns a list of elements"""
        if response.status < 200 or response.status >= 300:
            body = await response.text()
            raise NodifyClientException(
                f"Request failed with status: {response.status} - {body}",
                response.status
            )

        data = await response.json()

        if not isinstance(data, list):
            if hasattr(element_type, '__dataclass_fields__'):
                return [self._dict_to_dataclass(data, element_type)]
            return [data]

        result = []
        for item in data:
            if hasattr(element_type, '__dataclass_fields__'):
                result.append(self._dict_to_dataclass(item, element_type))
            else:
                result.append(item)
        return result

    async def _execute_get(self, path: str, response_type: Type[T], params: Optional[PaginationParams] = None) -> T:
        """Execute a GET request"""
        session = await self._get_session()
        url = self._build_url(path, params)
        async with session.get(url) as response:
            return await self._handle_response(response, response_type)

    async def _execute_get_list(self, path: str, element_type: Type[T], params: Optional[PaginationParams] = None) -> List[T]:
        """Execute a GET request that returns a list"""
        session = await self._get_session()
        url = self._build_url(path, params)
        async with session.get(url) as response:
            return await self._handle_list_response(response, element_type)

    async def _execute_post(self, path: str, body: Any, response_type: Type[T], params: Optional[PaginationParams] = None) -> T:
        """Execute a POST request"""
        session = await self._get_session()
        url = self._build_url(path, params)

        # Convert body to JSON-serializable format with camelCase keys
        json_data = self._dataclass_to_dict(body)

        # Debug: print what we're sending (optional - remove in production)
        # import json as json_lib
        # print(f"📤 Sending to {url}:")
        # print(json_lib.dumps(json_data, indent=2)[:500])

        async with session.post(url, json=json_data) as response:
            return await self._handle_response(response, response_type)

    async def _execute_post_list(self, path: str, body: Any, element_type: Type[T], params: Optional[PaginationParams] = None) -> List[T]:
        """Execute a POST request that returns a list"""
        session = await self._get_session()
        url = self._build_url(path, params)

        # Convert body to JSON-serializable format with camelCase keys
        json_data = self._dataclass_to_dict(body)

        async with session.post(url, json=json_data) as response:
            return await self._handle_list_response(response, element_type)

    async def _execute_put(self, path: str, response_type: Type[T], body: Any = None, params: Optional[PaginationParams] = None) -> T:
        """Execute a PUT request"""
        session = await self._get_session()
        url = self._build_url(path, params)

        # Convert body to JSON-serializable format with camelCase keys
        json_data = self._dataclass_to_dict(body)

        async with session.put(url, json=json_data) as response:
            return await self._handle_response(response, response_type)

    async def _execute_delete(self, path: str, response_type: Type[T], params: Optional[PaginationParams] = None) -> T:
        """Execute a DELETE request"""
        session = await self._get_session()
        url = self._build_url(path, params)
        async with session.delete(url) as response:
            return await self._handle_response(response, response_type)

    def set_auth_token(self, token: str) -> None:
        """Set the authentication token"""
        self.config.auth_token = token

    def get_auth_token(self) -> Optional[str]:
        """Get the authentication token"""
        return self.config.auth_token

    async def close(self) -> None:
        """Close the HTTP session"""
        if self._session and not self._session.closed:
            await self._session.close()

    async def _refresh_session(self):
        """Force recreation of the session with the current token"""
        if self._session and not self._session.closed:
            await self._session.close()
            self._session = None

        timeout = ClientTimeout(total=self.config.timeout / 1000)
        headers = self._build_headers()
        self._session = aiohttp.ClientSession(
            timeout=timeout,
            headers=headers
        )
        return self._session

    # ==================== Authentication Endpoints ====================

    async def login(self, email: str, password: str) -> AuthResponse:
        """Login and get authentication token"""
        login_data = {"email": email, "password": password}

        # Use a temporary session for login
        async with aiohttp.ClientSession() as temp_session:
            async with temp_session.post(f"{self.config.base_url}/authentication/login", json=login_data) as resp:
                if resp.status != 200:
                    text = await resp.text()
                    raise NodifyClientException(f"Login failed: {resp.status} - {text}", resp.status)
                result = await resp.json()
                self.set_auth_token(result.get("token"))

        # Recreate the session with the token
        await self._refresh_session()

        return AuthResponse(token=self.config.auth_token)

    async def logout(self) -> None:
        """Logout and clear authentication token"""
        self.set_auth_token(None)
        await self._refresh_session()

    # ==================== Health Endpoint ====================

    async def health(self) -> str:
        """Check health endpoint"""
        return await self._execute_get("/health", str)

    # ==================== Users Endpoints ====================

    async def find_all_users(self) -> List[UserPost]:
        """Find all users"""
        return await self._execute_get_list("/v0/users/", UserPost)

    async def save_user(self, user: UserPost) -> UserPost:
        """Save a user"""
        return await self._execute_post("/v0/users/", user, UserPost)

    async def change_password(self, password_data: UserPassword) -> bool:
        """Change user password"""
        return await self._execute_post("/v0/users/password", password_data, bool)

    async def find_user_by_id(self, user_id: str) -> UserPost:
        """Find user by ID"""
        return await self._execute_get(f"/v0/users/id/{user_id}", UserPost)

    async def find_user_by_email(self, email: str) -> UserPost:
        """Find user by email"""
        return await self._execute_get(f"/v0/users/email/{email}", UserPost)

    async def delete_user(self, user_id: str) -> bool:
        """Delete user by ID"""
        return await self._execute_delete(f"/v0/users/id/{user_id}", bool)

    # ==================== User Roles Endpoints ====================

    async def find_all_user_roles(self) -> List[UserRole]:
        """Find all user roles"""
        return await self._execute_get_list("/v0/users-roles/", UserRole)

    async def save_user_role(self, role: UserRole) -> UserRole:
        """Save a user role"""
        return await self._execute_post("/v0/users-roles/", role, UserRole)

    async def find_user_role_by_id(self, role_id: str) -> UserRole:
        """Find user role by ID"""
        return await self._execute_get(f"/v0/users-roles/id/{role_id}", UserRole)

    async def delete_user_role(self, role_id: str) -> bool:
        """Delete user role by ID"""
        return await self._execute_delete(f"/v0/users-roles/id/{role_id}", bool)

    # ==================== User Parameters Endpoints ====================

    async def find_all_user_parameters(self) -> List[UserParameters]:
        """Find all user parameters"""
        return await self._execute_get_list("/v0/user-parameters/", UserParameters)

    async def save_user_parameters(self, params: UserParameters) -> UserParameters:
        """Save user parameters"""
        return await self._execute_post("/v0/user-parameters/", params, UserParameters)

    async def find_user_parameters_by_user_id(self, user_id: str) -> UserParameters:
        """Find user parameters by user ID"""
        return await self._execute_get(f"/v0/user-parameters/user/{user_id}", UserParameters)

    async def find_user_parameters_by_id(self, params_id: str) -> UserParameters:
        """Find user parameters by ID"""
        return await self._execute_get(f"/v0/user-parameters/id/{params_id}", UserParameters)

    async def delete_user_parameters(self, params_id: str) -> bool:
        """Delete user parameters by ID"""
        return await self._execute_delete(f"/v0/user-parameters/id/{params_id}", bool)

    # ==================== Plugins Endpoints ====================

    async def find_not_deleted_plugins(self) -> List[Plugin]:
        """Find not deleted plugins"""
        return await self._execute_get_list("/v0/plugins/", Plugin)

    async def save_plugin(self, plugin: Plugin) -> Plugin:
        """Save a plugin"""
        return await self._execute_post("/v0/plugins/", plugin, Plugin)

    async def import_content_node_plugin(self, plugin: Plugin) -> Plugin:
        """Import a content node plugin"""
        return await self._execute_post("/v0/plugins/import", plugin, Plugin)

    async def enable_plugin(self, plugin_id: str) -> Plugin:
        """Enable a plugin"""
        return await self._execute_put(f"/v0/plugins/id/{plugin_id}/enable", Plugin)

    async def disable_plugin(self, plugin_id: str) -> Plugin:
        """Disable a plugin"""
        return await self._execute_put(f"/v0/plugins/id/{plugin_id}/disable", Plugin)

    async def activate_plugin(self, plugin_id: str) -> Plugin:
        """Activate a plugin"""
        return await self._execute_put(f"/v0/plugins/id/{plugin_id}/activate", Plugin)

    async def find_plugin_by_id(self, plugin_id: str) -> Plugin:
        """Find plugin by ID"""
        return await self._execute_get(f"/v0/plugins/id/{plugin_id}", Plugin)

    async def export_plugin(self, plugin_id: str) -> bytes:
        """Export a plugin"""
        return await self._execute_get(f"/v0/plugins/id/{plugin_id}/export", bytes)

    async def find_deleted_plugins(self) -> List[Plugin]:
        """Find deleted plugins"""
        return await self._execute_get_list("/v0/plugins/deleteds", Plugin)

    async def delete_plugin(self, plugin_id: str) -> bool:
        """Delete a plugin"""
        return await self._execute_delete(f"/v0/plugins/id/{plugin_id}", bool)

    async def delete_plugin_definitively(self, plugin_id: str) -> bool:
        """Delete a plugin definitively"""
        return await self._execute_delete(f"/v0/plugins/id/{plugin_id}/deleteDefinitively", bool)

    # ==================== Plugin Files Endpoints ====================

    async def find_all_plugin_files(self, enabled: Optional[bool] = None) -> List[PluginFile]:
        """Find all plugin files"""
        path = "/v0/plugin-files/"
        if enabled is not None:
            path += f"?enabled={enabled}"
        return await self._execute_get_list(path, PluginFile)

    async def save_plugin_file(self, plugin_file: PluginFile) -> PluginFile:
        """Save a plugin file"""
        return await self._execute_post("/v0/plugin-files/", plugin_file, PluginFile)

    async def find_plugin_files_by_plugin_id(self, plugin_id: str) -> List[PluginFile]:
        """Find plugin files by plugin ID"""
        return await self._execute_get_list(f"/v0/plugin-files/plugin/{plugin_id}", PluginFile)

    async def find_plugin_files_by_plugin_name(self, name: str) -> List[PluginFile]:
        """Find plugin files by plugin name"""
        return await self._execute_get_list(f"/v0/plugin-files/plugin/name/{name}", PluginFile)

    async def find_plugin_file_by_id(self, file_id: str) -> PluginFile:
        """Find plugin file by ID"""
        return await self._execute_get(f"/v0/plugin-files/id/{file_id}", PluginFile)

    async def delete_plugin_file(self, file_id: str) -> bool:
        """Delete plugin file by ID"""
        return await self._execute_delete(f"/v0/plugin-files/id/{file_id}", bool)

    # ==================== Notifications Endpoints ====================

    async def mark_all_notifications_as_read(self) -> List[Notification]:
        """Mark all notifications as read"""
        return await self._execute_post_list("/v0/notifications/markAllAsRead", None, Notification)

    async def mark_notification_as_read(self, notification_id: str) -> Notification:
        """Mark a notification as read"""
        return await self._execute_post(f"/v0/notifications/id/{notification_id}/markread", None, Notification)

    # ==================== Languages Endpoints ====================

    async def find_all_languages(self) -> List[Language]:
        """Find all languages"""
        return await self._execute_get_list("/v0/languages/", Language)

    async def save_language(self, language: Language) -> Language:
        """Save a language"""
        return await self._execute_post("/v0/languages/", language, Language)

    async def find_language_by_id(self, language_id: str) -> Language:
        """Find language by ID"""
        return await self._execute_get(f"/v0/languages/id/{language_id}", Language)

    async def delete_language(self, language_id: str) -> bool:
        """Delete language by ID"""
        return await self._execute_delete(f"/v0/languages/id/{language_id}", bool)

    # ==================== Feedback Endpoints ====================

    async def find_all_feedback(self) -> List[Feedback]:
        """Find all feedback"""
        return await self._execute_get_list("/v0/feedbacks/", Feedback)

    async def save_feedback(self, feedback: Feedback) -> Feedback:
        """Save feedback"""
        return await self._execute_post("/v0/feedbacks/", feedback, Feedback)

    async def find_feedback_by_verified(self, verified: bool) -> List[Feedback]:
        """Find feedback by verified status"""
        return await self._execute_get_list(f"/v0/feedbacks/verified/{verified}", Feedback)

    async def find_feedback_by_user_id(self, user_id: str) -> List[Feedback]:
        """Find feedback by user ID"""
        return await self._execute_get_list(f"/v0/feedbacks/userId/{user_id}", Feedback)

    async def find_feedback_by_id(self, feedback_id: str) -> Feedback:
        """Find feedback by ID"""
        return await self._execute_get(f"/v0/feedbacks/id/{feedback_id}", Feedback)

    async def find_feedback_by_evaluation(self, evaluation: int) -> List[Feedback]:
        """Find feedback by evaluation"""
        return await self._execute_get_list(f"/v0/feedbacks/evaluation/{evaluation}", Feedback)

    async def find_feedback_by_content_code(self, code: str) -> List[Feedback]:
        """Find feedback by content code"""
        return await self._execute_get_list(f"/v0/feedbacks/contentCode/{code}", Feedback)

    async def get_content_charts(self) -> List[FeedbackCharts]:
        """Get content charts"""
        return await self._execute_get_list("/v0/feedbacks/charts", FeedbackCharts)

    async def delete_feedback(self, feedback_id: str) -> bool:
        """Delete feedback by ID"""
        return await self._execute_delete(f"/v0/feedbacks/id/{feedback_id}", bool)

    # ==================== Data Endpoints ====================

    async def save_data(self, data: Data) -> Data:
        """Save data"""
        return await self._execute_post("/v0/datas/", data, Data)

    async def find_data_by_key(self, key: str) -> Data:
        """Find data by key"""
        return await self._execute_get(f"/v0/datas/key/{key}", Data)

    async def find_data_by_content_code(self, code: str, params: Optional[PaginationParams] = None) -> List[Data]:
        """Find data by content code"""
        return await self._execute_get_list(f"/v0/datas/contentCode/{code}", Data, params)

    async def count_data_by_content_code(self, code: str) -> int:
        """Count data by content code"""
        return await self._execute_get(f"/v0/datas/contentCode/{code}/count", int)

    async def delete_all_data_by_content_code(self, code: str) -> bool:
        """Delete all data by content code"""
        return await self._execute_delete(f"/v0/datas/contentCode/{code}", bool)

    async def delete_data_by_id(self, data_id: str) -> bool:
        """Delete data by ID"""
        return await self._execute_delete(f"/v0/datas/id/{data_id}", bool)

    # ==================== Nodes Endpoints ====================

    async def find_all_nodes(self) -> List[Node]:
        """Find all nodes"""
        return await self._execute_get_list("/v0/nodes/", Node)

    async def save_node(self, node: Node) -> Node:
        """Save a node"""
        return await self._execute_post("/v0/nodes/", node, Node)

    async def import_node(self, node: Node) -> Node:
        """Import a node"""
        return await self._execute_post("/v0/nodes/import", node, Node)

    async def revert_node_version(self, code: str, version: str) -> Node:
        """Revert node version"""
        return await self._execute_post(f"/v0/nodes/code/{code}/version/{version}/revert", None, Node)

    async def deploy_node_version(self, code: str, version: str, environment: Optional[str] = None) -> bool:
        """Deploy node version"""
        uri = f"/v0/nodes/code/{code}/version/{version}/deploy"
        if environment:
            uri += f"?environment={environment}"
        return await self._execute_post(uri, None, bool)

    async def publish_node(self, code: str) -> Node:
        """Publish a node"""
        return await self._execute_post(f"/v0/nodes/code/{code}/publish", None, Node)

    async def activate_node(self, code: str) -> bool:
        """Activate a node"""
        return await self._execute_post(f"/v0/nodes/code/{code}/activate", None, bool)

    async def find_all_nodes_by_status(self, status: str) -> List[Node]:
        """Find all nodes by status"""
        return await self._execute_get_list(f"/v0/nodes/status/{status}", Node)

    async def find_published_nodes(self) -> List[Node]:
        """Find published nodes"""
        return await self._execute_get_list("/v0/nodes/published", Node)

    async def find_parent_nodes_by_status(self, status: str) -> List[Node]:
        """Find parent nodes by status"""
        return await self._execute_get_list(f"/v0/nodes/parent/status/{status}", Node)

    async def find_nodes_by_parent_code(self, code: str) -> List[Node]:
        """Find nodes by parent code"""
        return await self._execute_get_list(f"/v0/nodes/parent/code/{code}", Node)

    async def find_children_by_code_and_status(self, code: str, status: str) -> List[Node]:
        """Find children by code and status"""
        return await self._execute_get_list(f"/v0/nodes/parent/code/{code}/status/{status}", Node)

    async def find_all_descendants(self, code: str) -> List[Node]:
        """Find all descendants"""
        return await self._execute_get_list(f"/v0/nodes/parent/code/{code}/descendants", Node)

    async def find_parent_origin(self) -> List[Node]:
        """Find parent origin"""
        return await self._execute_get_list("/v0/nodes/origin", Node)

    async def get_deleted_nodes(self, parent: Optional[str] = None) -> List[Node]:
        """Get deleted nodes"""
        uri = "/v0/nodes/deleted"
        if parent:
            uri += f"?parent={parent}"
        return await self._execute_get_list(uri, Node)

    async def find_nodes_by_code(self, code: str) -> List[Node]:
        """Find nodes by code"""
        return await self._execute_get_list(f"/v0/nodes/code/{code}", Node)

    async def find_node_by_code_and_status(self, code: str, status: str) -> Node:
        """Find node by code and status"""
        return await self._execute_get(f"/v0/nodes/code/{code}/status/{status}", Node)

    async def generate_tree_view(self, code: str) -> TreeNode:
        """Generate tree view"""
        return await self._execute_get(f"/v0/nodes/code/{code}/tree-view", TreeNode)

    async def check_slug_exists_for_node(self, code: str, slug: str) -> bool:
        """Check if slug exists for node"""
        return await self._execute_get(f"/v0/nodes/code/{code}/slug/{slug}/exists", bool)

    async def has_contents(self, code: str) -> bool:
        """Check if node has contents"""
        return await self._execute_get(f"/v0/nodes/code/{code}/haveContents", bool)

    async def has_children(self, code: str) -> bool:
        """Check if node has children"""
        return await self._execute_get(f"/v0/nodes/code/{code}/haveChilds", bool)

    async def export_all_nodes(self, code: str, environment: Optional[str] = None) -> bytes:
        """Export all nodes"""
        uri = f"/v0/nodes/code/{code}/export"
        if environment:
            uri += f"?environment={environment}"
        return await self._execute_get(uri, bytes)

    async def deploy_node(self, code: str, environment: Optional[str] = None) -> List[Node]:
        """Deploy node"""
        uri = f"/v0/nodes/code/{code}/deploy"
        if environment:
            uri += f"?environment={environment}"
        return await self._execute_get_list(uri, Node)

    async def delete_node(self, code: str) -> bool:
        """Delete node"""
        return await self._execute_delete(f"/v0/nodes/code/{code}", bool)

    async def delete_node_definitively(self, code: str) -> bool:
        """Delete node definitively"""
        return await self._execute_delete(f"/v0/nodes/code/{code}/deleteDefinitively", bool)

    async def delete_node_version_definitively(self, code: str, version: str) -> bool:
        """Delete node version definitively"""
        return await self._execute_delete(f"/v0/nodes/code/{code}/version/{version}/deleteDefinitively", bool)

    # ==================== Content Nodes Endpoints ====================

    async def find_all_content_nodes(self) -> List[ContentNode]:
        """Find all content nodes"""
        return await self._execute_get_list("/v0/content-node/", ContentNode)

    async def save_content_node(self, content_node: ContentNode) -> ContentNode:
        """Save a content node"""
        return await self._execute_post("/v0/content-node/", content_node, ContentNode)

    async def import_content_node(self, content_node: ContentNode, node_parent_code: Optional[str] = None, from_file: Optional[bool] = None) -> ContentNode:
        """Import a content node"""
        uri = "/v0/content-node/import"
        params = []
        if node_parent_code:
            params.append(f"nodeParentCode={node_parent_code}")
        if from_file is not None:
            params.append(f"fromFile={from_file}")
        if params:
            uri += "?" + "&".join(params)
        return await self._execute_post(uri, content_node, ContentNode)

    async def revert_content_node_version(self, code: str, version: str) -> ContentNode:
        """Revert content node version"""
        return await self._execute_post(f"/v0/content-node/code/{code}/version/{version}/revert", None, ContentNode)

    async def deploy_content_node_version(self, code: str, version: str, environment: Optional[str] = None) -> bool:
        """Deploy content node version"""
        uri = f"/v0/content-node/code/{code}/version/{version}/deploy"
        if environment:
            uri += f"?environment={environment}"
        return await self._execute_post(uri, None, bool)

    async def fill_content(self, code: str, status: str, payload: ContentNodePayload) -> ContentNode:
        """Fill content"""
        return await self._execute_post(f"/v0/content-node/code/{code}/status/{status}/fill", payload, ContentNode)

    async def publish_content_node(self, code: str, publish: bool) -> ContentNode:
        """Publish content node"""
        return await self._execute_post(f"/v0/content-node/code/{code}/publish/{publish}", None, ContentNode)

    async def activate_content_node(self, code: str) -> bool:
        """Activate content node"""
        return await self._execute_post(f"/v0/content-node/code/{code}/activate", None, bool)

    async def find_all_content_nodes_by_status(self, status: str) -> List[ContentNode]:
        """Find all content nodes by status"""
        return await self._execute_get_list(f"/v0/content-node/status/{status}", ContentNode)

    async def find_all_content_nodes_by_node_code(self, code: str) -> List[ContentNode]:
        """Find all content nodes by node code"""
        return await self._execute_get_list(f"/v0/content-node/node/code/{code}", ContentNode)

    async def find_content_nodes_by_node_code_and_status(self, code: str, status: str) -> List[ContentNode]:
        """Find content nodes by node code and status"""
        return await self._execute_get_list(f"/v0/content-node/node/code/{code}/status/{status}", ContentNode)

    async def get_deleted_content_nodes(self, parent: Optional[str] = None) -> List[ContentNode]:
        """Get deleted content nodes"""
        uri = "/v0/content-node/deleted"
        if parent:
            uri += f"?parent={parent}"
        return await self._execute_get_list(uri, ContentNode)

    async def find_all_content_nodes_by_code(self, code: str) -> List[ContentNode]:
        """Find all content nodes by code"""
        return await self._execute_get_list(f"/v0/content-node/code/{code}", ContentNode)

    async def find_content_node_by_code_and_status(self, code: str, status: str) -> ContentNode:
        """Find content node by code and status"""
        return await self._execute_get(f"/v0/content-node/code/{code}/status/{status}", ContentNode)

    async def check_content_node_slug_exists(self, code: str, slug: str) -> bool:
        """Check if content node slug exists"""
        return await self._execute_get(f"/v0/content-node/code/{code}/slug/{slug}/exists", bool)

    async def export_content_node(self, code: str, environment: Optional[str] = None) -> bytes:
        """Export content node"""
        uri = f"/v0/content-node/code/{code}/export"
        if environment:
            uri += f"?environment={environment}"
        return await self._execute_get(uri, bytes)

    async def deploy_content_node(self, code: str, environment: Optional[str] = None) -> bool:
        """Deploy content node"""
        uri = f"/v0/content-node/code/{code}/deploy"
        if environment:
            uri += f"?environment={environment}"
        return await self._execute_get(uri, bool)

    async def delete_content_node(self, code: str) -> bool:
        """Delete content node"""
        return await self._execute_delete(f"/v0/content-node/code/{code}", bool)

    async def delete_content_node_definitively(self, code: str) -> bool:
        """Delete content node definitively"""
        return await self._execute_delete(f"/v0/content-node/code/{code}/deleteDefinitively", bool)

    async def delete_content_node_version_definitively(self, code: str, version: str) -> bool:
        """Delete content node version definitively"""
        return await self._execute_delete(f"/v0/content-node/code/{code}/version/{version}/deleteDefinitively", bool)

    # ==================== Content Displays Endpoints ====================

    async def find_all_content_displays(self) -> List[ContentDisplay]:
        """Find all content displays"""
        return await self._execute_get_list("/v0/content-displays/", ContentDisplay)

    async def save_content_display(self, content_display: ContentDisplay) -> ContentDisplay:
        """Save a content display"""
        return await self._execute_post("/v0/content-displays/", content_display, ContentDisplay)

    async def find_content_display_by_id(self, display_id: str) -> ContentDisplay:
        """Find content display by ID"""
        return await self._execute_get(f"/v0/content-displays/id/{display_id}", ContentDisplay)

    async def find_content_display_by_content_code(self, code: str) -> ContentDisplay:
        """Find content display by content code"""
        return await self._execute_get(f"/v0/content-displays/contentCode/{code}", ContentDisplay)

    async def get_content_display_charts(self) -> List[ContentDisplayCharts]:
        """Get content display charts"""
        return await self._execute_get_list("/v0/content-displays/charts", ContentDisplayCharts)

    async def delete_content_display(self, display_id: str) -> bool:
        """Delete content display by ID"""
        return await self._execute_delete(f"/v0/content-displays/id/{display_id}", bool)

    # ==================== Content Clicks Endpoints ====================

    async def find_all_content_clicks(self) -> List[ContentClick]:
        """Find all content clicks"""
        return await self._execute_get_list("/v0/content-clicks/", ContentClick)

    async def save_content_click(self, content_click: ContentClick) -> ContentClick:
        """Save a content click"""
        return await self._execute_post("/v0/content-clicks/", content_click, ContentClick)

    async def find_content_click_by_id(self, click_id: str) -> ContentClick:
        """Find content click by ID"""
        return await self._execute_get(f"/v0/content-clicks/id/{click_id}", ContentClick)

    async def find_content_click_by_content_code(self, code: str) -> List[ContentClick]:
        """Find content click by content code"""
        return await self._execute_get_list(f"/v0/content-clicks/contentCode/{code}", ContentClick)

    async def get_content_click_charts(self) -> List[ContentClickCharts]:
        """Get content click charts"""
        return await self._execute_get_list("/v0/content-clicks/charts", ContentClickCharts)

    async def delete_content_click(self, click_id: str) -> bool:
        """Delete content click by ID"""
        return await self._execute_delete(f"/v0/content-clicks/id/{click_id}", bool)

    # ==================== Access Roles Endpoints ====================

    async def find_all_access_roles(self) -> List[AccessRole]:
        """Find all access roles"""
        return await self._execute_get_list("/v0/access-roles/", AccessRole)

    async def save_access_role(self, access_role: AccessRole) -> AccessRole:
        """Save an access role"""
        return await self._execute_post("/v0/access-roles/", access_role, AccessRole)

    async def find_access_role_by_id(self, role_id: str) -> AccessRole:
        """Find access role by ID"""
        return await self._execute_get(f"/v0/access-roles/id/{role_id}", AccessRole)

    async def delete_access_role(self, role_id: str) -> bool:
        """Delete access role by ID"""
        return await self._execute_delete(f"/v0/access-roles/id/{role_id}", bool)

    # ==================== Charts Endpoints ====================

    async def get_charts(self) -> TreeNode:
        """Get charts"""
        return await self._execute_get("/v0/charts/", TreeNode)

    # ==================== Locks Endpoints ====================

    async def acquire_lock(self, code: str) -> bool:
        """Acquire lock"""
        return await self._execute_post(f"/v0/locks/acquire/{code}", None, bool)

    async def refresh_lock(self, code: str) -> bool:
        """Refresh lock"""
        return await self._execute_post(f"/v0/locks/refresh/{code}", None, bool)

    async def release_lock(self, code: str) -> bool:
        """Release lock"""
        return await self._execute_post(f"/v0/locks/release/{code}", None, bool)

    async def admin_release_lock(self, code: str) -> bool:
        """Admin release lock"""
        return await self._execute_post(f"/v0/locks/admin/release/{code}", None, bool)

    async def get_lock_owner(self, code: str) -> LockInfo:
        """Get lock owner"""
        return await self._execute_get(f"/v0/locks/owner/{code}", LockInfo)

    async def get_all_locks(self) -> List[LockInfo]:
        """Get all locks"""
        return await self._execute_get_list("/v0/locks/all", LockInfo)

    # ==================== Slug Controller ====================

    async def check_slug_exists(self, slug: str) -> List[str]:
        """Check if slug exists"""
        return await self._execute_get_list(f"/v0/slugs/exists/{slug}", str)