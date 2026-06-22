# WrtMonitor

`WrtMonitor` - self-hosted сервер, Web UI, Android-приложение и OpenWrt-агент для мониторинга и базового управления роутерами OpenWrt.

## Текущая версия

Текущий internal prerelease: `v0.1.1-rc9-agent-modularization-and-ui-fixes`.

Что вошло в `rc9`:

- OpenWrt-агент разбит на модульную структуру `wrtmonitor-agent + lib/*.sh`;
- installer и update pipeline переведены на manifest `openwrt-agent-files.txt`;
- сервер раздаёт полный набор agent-файлов для установки и обновления;
- Web UI и Android показывают compact summary для capabilities вместо длинного списка;
- в Web UI и Android закреплено удаление из активного списка только для `disabled` роутеров;
- переходная логика для старых prerelease-агентов упрощена.

Важно: `rc9` меняет layout файлов OpenWrt-агента. Для внутреннего тестирования рекомендуется clean reinstall агента. Обратная совместимость автообновления с `rc7/rc8` намеренно не гарантируется.

## Что уже есть

- сервер `FastAPI + PostgreSQL + Alembic`;
- Web UI в тёмной dashboard-теме;
- Android-клиент;
- OpenWrt-агент для регистрации, telemetry, очереди команд, diagnostics и автообновления;
- установка через Docker Compose, VPS, домашний Linux-сервер, NAS с Docker и TrueNAS Custom App;
- управление Wi-Fi, базовой сетью, диагностикой и жизненным циклом агента;
- release artifacts для сервера, агента и Android.

## Быстрый старт

1. Разверните сервер и PostgreSQL через Docker Compose или TrueNAS.
2. Откройте `/setup`.
3. Создайте первого администратора.
4. Проверьте `/health`.
5. Подключите Android-приложение.
6. Установите OpenWrt-агент.

Для reverse proxy указывайте внешний HTTPS-адрес:

```env
WRTMONITOR_PUBLIC_SERVER_URL=https://monitor.example.ru
WRTMONITOR_ALLOW_INSECURE_LOCAL=false
```

Для локального временного теста можно включить HTTP:

```env
WRTMONITOR_PUBLIC_SERVER_URL=http://192.168.1.10:8088
WRTMONITOR_ALLOW_INSECURE_LOCAL=true
```

## TrueNAS

Базовый YAML лежит в `deploy/truenas/wrtmonitor-truenas.yaml`.

В релизе он публикуется как:

```text
wrtmonitor-truenas-v0.1.1-rc9.yaml
```

Контейнер использует:

```text
ghcr.io/shurshick/wrtmonitor:latest
```

`latest` скачивается при redeploy через **Edit -> Save**, но не обновляет уже запущенный контейнер сам по себе.

## OpenWrt-агент

OpenWrt-агент можно установить:

- с GitHub Release;
- прямо с уже развернутого сервера `https://monitor.example.ru/downloads/openwrt/`.

Сервер раздаёт:

- `wrtmonitor-agent`
- `wrtmonitor.init`
- `install-openwrt.sh`
- `agent-version.txt`
- `openwrt-agent-files.txt`
- `SHA256SUMS.txt`
- `lib/*.sh`

Подробности:

- [OpenWrt agent](docs/openwrt-agent.md)
- [Развёртывание сервера](docs/server-deployment.md)
- [Router management core](docs/router-management-core.md)

## Документация

- [OpenWrt agent](docs/openwrt-agent.md)
- [Развёртывание сервера](docs/server-deployment.md)
- [API](docs/api.md)
- [Архитектура](docs/architecture.md)
- [Жизненный цикл команд](docs/command-lifecycle.md)
- [Проверка на реальном роутере](docs/real-router-testing.md)
- [Android](docs/android.md)
- [Roadmap](docs/roadmap.md)
- [Changelog](CHANGELOG.md)
