"""
Exceptions for Nodify client
"""

class NodifyClientException(Exception):
    """Exception raised for Nodify client errors"""

    def __init__(self, message: str, status_code: int = -1):
        self.status_code = status_code
        super().__init__(f"{message} (Status: {status_code})" if status_code != -1 else message)