# WrtMonitor

`WrtMonitor` это self-hosted сервер и Android-приложение для мониторинга и безопасного управления OpenWrt-роутерами.

## Что уже есть

- сервер `FastAPI + PostgreSQL`;
- Android-клиент;
- OpenWrt agent для регистрации, telemetry и выполнения команд;
- Web UI с тёмной dashboard-темой;
- установка через Docker, Compose и TrueNAS Custom App;
- очередь команд, аудит и история telemetry;
- автообновление OpenWrt agent с собственного сервера WrtMonitor.

## Текущий релиз

Текущая release candidate версия: `v0.1.1-rc7`.

Что добавлено в `rc7`:

- безопасное автообновление OpenWrt agent через `SHA256SUMS.txt`;
- backup текущего agent и init-скрипта перед заменой;
- rollback при неудачном обновлении;
- защита от автоматического downgrade;
- lock от параллельных обновлений;
- `wrtmonitor-agent update-status` и `wrtmonitor-agent rollback`;
- публикация `/downloads/openwrt/agent-version.txt` и `/downloads/openwrt/SHA256SUMS.txt`;
- статус agent update в Web UI и Android;
- удаление из списка только `disabled` устройств через soft-archive.

## Быстрый старт сервера

1. Поднимите сервер и PostgreSQL через Docker Compose или TrueNAS.
2. Откройте `/setup`.
3. Создайте первого администратора.
4. Проверьте `/health` и `/health/config`.
5. Подключите Android-приложение.
6. Установите OpenWrt agent.

Если сервер публикуется через Nginx Proxy Manager или другой reverse proxy, указывайте внешний HTTPS-адрес:

```env
WRTMONITOR_PUBLIC_SERVER_URL=https://monitor.example.ru
WRTMONITOR_ALLOW_INSECURE_LOCAL=false
```

Для локальной временной проверки можно включить HTTP:

```env
WRTMONITOR_PUBLIC_SERVER_URL=http://192.168.1.10:8088
WRTMONITOR_ALLOW_INSECURE_LOCAL=true
```

## TrueNAS

Базовый YAML находится в [`deploy/truenas/wrtmonitor-truenas.yaml`](deploy/truenas/wrtmonitor-truenas.yaml).

В релизных артефактах файл публикуется как:

```text
wrtmonitor-truenas-v0.1.1-rc7.yaml
```

Контейнер продолжает использовать:

```text
ghcr.io/shurshick/wrtmonitor:latest
```

`latest` скачивается при redeploy через **Edit -> Save**, но не обновляет уже работающий контейнер сам по себе.

## OpenWrt agent

OpenWrt agent устанавливается либо с GitHub Release, либо прямо с уже развернутого сервера:

```text
https://monitor.example.ru/downloads/openwrt/
```

Начиная с `rc7`, сервер раздаёт:

- `wrtmonitor-agent`
- `wrtmonitor.init`
- `install-openwrt.sh`
- `agent-version.txt`
- `SHA256SUMS.txt`

Подробно:

- [OpenWrt agent](docs/openwrt-agent.md)
- [TrueNAS Custom App](docs/truenas-custom-app.md)
- [Архитектура](docs/architecture.md)
- [Развёртывание сервера](docs/server-deployment.md)

## Документация

- [OpenWrt agent](docs/openwrt-agent.md)
- [TrueNAS Custom App](docs/truenas-custom-app.md)
- [Архитектура](docs/architecture.md)
- [Telemetry](docs/telemetry.md)
- [Android](docs/android.md)
- [Безопасность](docs/security.md)
- [Roadmap](docs/roadmap.md)
- [Changelog](CHANGELOG.md)
