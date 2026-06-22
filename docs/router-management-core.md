# Router Management Core

Документ фиксирует фундамент `v0.1.1-rc9-agent-modularization-and-ui-fixes`.

## Что закреплено в rc9

- `agent capabilities`
- command metadata и risk levels
- backend validation payload
- diagnostics commands
- backup `wireless` перед Wi-Fi-изменениями
- нормализованные `wifi` и `network` summary
- compact capabilities UX в Web UI и Android
- модульная структура OpenWrt-агента

## OpenWrt-агент

Новая структура агента:

```text
wrtmonitor-agent
lib/common.sh
lib/status.sh
lib/update.sh
lib/telemetry.sh
lib/capabilities.sh
lib/diagnostics.sh
lib/commands.sh
lib/api.sh
```

`wrtmonitor-agent` теперь отвечает только за:

- `AGENT_VERSION`
- `CONFIG`
- определение `LIB_DIR`
- загрузку модулей в фиксированном порядке
- вызов `main "$@"`

## Capabilities

Агент публикует JSON с:

- `agent.version`
- `agent.platform`
- `agent.capabilities_version`
- набором булевых capabilities

В `rc9` интерфейсы по умолчанию показывают краткий summary capabilities, а полный список раскрывается по запросу.

Если capabilities отсутствуют, сервер не делает сложный compatibility bootstrap для старых prerelease-агентов, а переводит управление в режим явного сообщения о необходимости reinstall `rc9`.

## Update/install pipeline

Для новой структуры используется manifest:

```text
openwrt-agent-files.txt
```

Installer и updater:

- скачивают manifest;
- скачивают все перечисленные файлы;
- проверяют `SHA256SUMS.txt`;
- выполняют `sh -n` для entrypoint, installer, init и `lib/*.sh`;
- устанавливают `/usr/bin/wrtmonitor-agent`, `/etc/init.d/wrtmonitor`, `/usr/lib/wrtmonitor/*.sh`.

Для internal prerelease testing clean reinstall считается допустимым и основным путём миграции на `rc9`.

## Risk levels

- `level_1_readonly`
- `level_2_safe_action`
- `level_3_reversible_config`

`level_3_reversible_config` требует подтверждения и должен сопровождаться понятным UI.

## Diagnostics

Поддерживаются:

- `check-server`
- `check-dns`
- `check-route`
- `check-wifi`
- `check-dependencies`
- `diagnostics --json`

Backend использует `diagnostics.run` как обычную queued-команду.

## Wi-Fi backup before change

Перед командами Wi-Fi агент создаёт:

- `.bak` файл конфигурации;
- `.meta` файл с metadata.

Rollback/backup сохраняются в новой структуре и не зависят от старого layout `rc7/rc8`.

## Secret masking

Секреты не должны попадать в:

- command history
- command result
- telemetry
- agent status
- support bundle
- Web UI
- Android UI
