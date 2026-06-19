# Telemetry

`wrtmonitor` принимает telemetry от OpenWrt agent через:

```http
POST /api/v1/agent/telemetry
```

Актуальный snapshot доступен владельцу сервера через:

```http
GET /api/v1/devices/{device_id}/telemetry/latest
```

Ответ содержит:

- `device_id`;
- `created_at`;
- `age_seconds`;
- `is_stale` — `true`, если snapshot старше 5 минут;
- `source` — сейчас всегда `agent`;
- `telemetry` — последний payload или `null`, если данных ещё нет.

OpenWrt agent собирает:

- `system`: uptime, load average, память, `ubus system info`;
- `board`: `ubus system board`;
- `network`: `ubus network.interface dump`;
- `wifi`: multi-radio snapshot из UCI wireless config.

Retention: сервер хранит последние 100 telemetry snapshots на устройство. Старые snapshots удаляются после успешного ingest.
