# Release Notes

## v0.1.0-test.11-agent-stability

Релиз закрепляет telemetry-контур `v0.1.0-test.10` и делает агент/сервер устойчивее для дальнейших тестов.

Главное:

- OpenWrt agent проверяется через `sh -n` и agent smoke tests.
- Критичные API JSON-поля в agent читаются через `jsonfilter`, а не regex.
- Wi-Fi telemetry готова к multi-radio конфигурациям.
- Сервер не стартует с дефолтным JWT secret или дефолтным паролем PostgreSQL.
- Latest telemetry API возвращает возраст данных и stale-флаг.
- Сервер хранит последние 100 telemetry snapshots на устройство.
- Android экран устройства показывает основные telemetry-поля без raw JSON как основного UI.

Upgrade from `v0.1.0-test.10`:

- PostgreSQL volume удалять не нужно.
- Замените `WRTMONITOR_JWT_SECRET`, если он дефолтный или короткий.
- Замените `POSTGRES_PASSWORD` и пароль в `WRTMONITOR_DATABASE_URL`, если они начинаются с `change-me`.
- Обновите OpenWrt agent.
- Обновите Android APK.
