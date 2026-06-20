# v0.1.0-test.16 — Security, Refactor and Command Reliability

## Безопасность и сервер

- Web UI POST-формы защищены CSRF-токеном, связанным с cookie session.
- Добавлены security headers и безопасный endpoint `/health/config` без раскрытия секретов.
- Устройства в user-facing routes проходят через единый access helper.
- Latest telemetry содержит summary, а retention управляется `WRTMONITOR_TELEMETRY_RETENTION_PER_DEVICE`.

## Команды и агент

- Команды получили lifecycle metadata: source, expiry, picked/completed timestamps, retry count и last error.
- Просроченные queued/sent/running команды становятся `expired`.
- Agent защищён lock от параллельного запуска, использует curl timeouts и новые команды `debug`, `debug-telemetry`, `debug-api` без раскрытия полного token.

## Android и артефакты

- Android: `versionName 0.1.0-test.16`, `versionCode 16`; APK ставится поверх `test.15`.
- Docker image: `ghcr.io/shurshick/wrtmonitor:0.1.0-test.16` и `latest`.
- Релиз содержит TrueNAS YAML, Android APK, OpenWrt-agent archive и `SHA256SUMS.txt`.
