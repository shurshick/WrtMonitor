from __future__ import annotations

import hashlib
from pathlib import Path


DOWNLOADS_DIR = Path("openwrt-agent")
DOWNLOAD_FILES = (
    "wrtmonitor-agent",
    "wrtmonitor.init",
    "install-openwrt.sh",
)


def _sha256_for(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(8192), b""):
            digest.update(chunk)
    return digest.hexdigest()


def read_agent_version() -> str:
    source = (DOWNLOADS_DIR / "wrtmonitor-agent").read_text(encoding="utf-8")
    for line in source.splitlines():
        if line.startswith('AGENT_VERSION="') and line.endswith('"'):
            return line.split('"', 2)[1]
    raise RuntimeError("AGENT_VERSION not found in openwrt-agent/wrtmonitor-agent")


def ensure_openwrt_download_metadata() -> None:
    DOWNLOADS_DIR.mkdir(parents=True, exist_ok=True)
    version = read_agent_version()
    with (DOWNLOADS_DIR / "agent-version.txt").open(
        "w", encoding="utf-8", newline="\n"
    ) as handle:
        handle.write(f"{version}\n")
    checksums = []
    for filename in (*DOWNLOAD_FILES, "agent-version.txt"):
        path = DOWNLOADS_DIR / filename
        checksums.append(f"{_sha256_for(path)}  {filename}")
    with (DOWNLOADS_DIR / "SHA256SUMS.txt").open(
        "w", encoding="utf-8", newline="\n"
    ) as handle:
        handle.write("\n".join(checksums) + "\n")


def openwrt_download_metadata() -> dict[str, str | bool]:
    version = read_agent_version()
    return {
        "openwrt_downloads_enabled": True,
        "openwrt_agent_version": version,
        "openwrt_downloads_path": "/downloads/openwrt/",
    }
