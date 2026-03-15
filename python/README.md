# Nodify Python Client

Official Nodify API client for Python.

## Installation

```bash
pip install aiohttp
```

## Quick Start

```python
import asyncio
from nodify_client import ReactiveNodifyClient

async def main():
    client = ReactiveNodifyClient.create(
        ReactiveNodifyClient.builder()
            .with_base_url("https://nodify-core.azirar.ovh")
            .build()
    )
    
    await client.login("admin", "Admin13579++")
    nodes = await client.find_all_nodes()
    print(f"Found {len(nodes)} nodes")
    await client.close()

asyncio.run(main())
```

## License

MIT
```