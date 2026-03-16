"""
Configuration module for Nodify client
"""

from dataclasses import dataclass, field
from typing import Dict, Optional


@dataclass
class ReactiveNodifyClientConfig:
    """Configuration for the ReactiveNodifyClient"""
    base_url: str
    timeout: int = 30000  # milliseconds
    default_headers: Dict[str, str] = field(default_factory=lambda: {
        "Content-Type": "application/json",
        "Accept": "application/json"
    })
    auth_token: Optional[str] = None

    @classmethod
    def builder(cls):
        """Create a new builder instance"""
        return ReactiveNodifyClientConfig.Builder()

    @dataclass
    class Builder:
        """Builder for ReactiveNodifyClientConfig"""
        base_url: Optional[str] = None
        timeout: int = 30000
        default_headers: Dict[str, str] = field(default_factory=lambda: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        })
        auth_token: Optional[str] = None

        def with_base_url(self, base_url: str) -> 'ReactiveNodifyClientConfig.Builder':
            """Set the base URL"""
            if base_url.endswith("/"):
                self.base_url = base_url[:-1]
            else:
                self.base_url = base_url
            return self

        def with_timeout(self, timeout: int) -> 'ReactiveNodifyClientConfig.Builder':
            """Set the timeout in milliseconds"""
            self.timeout = timeout
            return self

        def with_auth_token(self, auth_token: str) -> 'ReactiveNodifyClientConfig.Builder':
            """Set the authentication token"""
            self.auth_token = auth_token
            return self

        def with_header(self, key: str, value: str) -> 'ReactiveNodifyClientConfig.Builder':
            """Add a default header"""
            self.default_headers[key] = value
            return self

        def build(self) -> 'ReactiveNodifyClientConfig':
            """Build the configuration"""
            if not self.base_url:
                raise ValueError("Base URL is required")
            return ReactiveNodifyClientConfig(
                base_url=self.base_url,
                timeout=self.timeout,
                default_headers=self.default_headers.copy(),
                auth_token=self.auth_token
            )