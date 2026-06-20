from typing import Any


def build_telemetry_summary(payload: dict[str, Any]) -> dict[str, Any]:
    system = payload.get("system") or {}
    memory = system.get("memory") or {}
    wifi = payload.get("wifi") or {}
    network = payload.get("network") or {}
    interfaces = network.get("interfaces") or network.get("interface") or []
    radios = wifi.get("radios") or []
    return {
        "uptime_seconds": system.get("uptime"),
        "load_1m": system.get("load"),
        "memory_total_mb": int(memory.get("total_kb", 0) or 0) // 1024,
        "memory_available_mb": int(memory.get("available_kb", memory.get("free_kb", 0)) or 0) // 1024,
        "wifi_available": bool(wifi.get("available", False)),
        "wifi_radio_count": len(radios),
        "network_interface_count": len(interfaces),
    }
