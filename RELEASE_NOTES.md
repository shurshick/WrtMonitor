# v0.1.1-rc7-agent-update-safety

## Что вошло в релиз

- безопасное обновление OpenWrt agent через `SHA256SUMS.txt`;
- `agent-version.txt` и серверная раздача актуальных agent-файлов через `/downloads/openwrt/`;
- backup и rollback agent при ошибке обновления;
- защита от параллельных обновлений и от автоматического downgrade;
- `wrtmonitor-agent update-status`, `wrtmonitor-agent rollback`, `--force`, `--allow-downgrade`;
- telemetry-блок `agent` с версией, статусом auto-update и результатом последнего обновления;
- раздел `Agent` в Web UI и Android;
- soft-archive для `disabled` устройств в Web UI, Android и backend API.

## Артефакты релиза

- `wrtmonitor-truenas-v0.1.1-rc7.yaml`
- `wrtmonitor-openwrt-agent-v0.1.1-rc7.tar.gz`
- `wrtmonitor-android-v0.1.1-rc7-debug.apk`
- `SHA256SUMS.txt`
