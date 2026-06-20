# v0.1.1-rc1 — Real Router Validation and Control Polish

## Command history and diagnostics

- Добавлен API `GET /api/v1/devices/{device_id}/commands` с lifecycle metadata.
- OpenWrt-agent получил `version` и `support-bundle [--public]` для безопасного сбора диагностики.

## Real router validation

- Добавлен [чек-лист проверки реального роутера](docs/real-router-testing.md) с recovery-инструкциями.
- Валидация на физическом OpenWrt с Wi-Fi radio, смена SSID, Wi-Fi on/off и reboot ещё не подтверждены: это обязательный ручной этап перед публичным `v0.1.1`.

## Upgrade

- Обновление с `v0.1.0-test.16` и новее сохраняет PostgreSQL volume.
- Android: `versionName 0.1.1-rc1`, `versionCode 17`.

## Known limitations

- Full Android architecture refactor и Jinja2 migration ещё не выполнены.
- APK остаётся debug-сборкой; auto-update агента, firewall, DHCP и VPN не входят в rc1.
