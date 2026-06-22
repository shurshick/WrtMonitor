# v0.1.1-rc9-agent-modularization-and-ui-fixes

## Что вошло в релиз

- OpenWrt-агент переведён на модульную структуру: `wrtmonitor-agent` стал тонким entrypoint, основная логика вынесена в `lib/common.sh`, `status.sh`, `update.sh`, `telemetry.sh`, `capabilities.sh`, `diagnostics.sh`, `commands.sh`, `api.sh`.
- Installer и update pipeline переведены на manifest `openwrt-agent-files.txt` и полный набор checksums для новой структуры.
- Clean reinstall агента теперь считается основным сценарием для internal prerelease testing после перехода на новый layout файлов.
- В Web UI добавлено компактное отображение capabilities: summary по умолчанию и раскрытие полного списка через `<details>`.
- В Android экран устройства переведён на тот же compact capabilities UX.
- В Web UI и Android закреплена кнопка удаления из активного списка только для `disabled` роутеров.
- Для старых prerelease-агентов интерфейс теперь явно показывает, что capabilities ещё не переданы и для управления нужен reinstall `rc9`.

## Важно для обновления

`rc9` меняет layout файлов OpenWrt-агента.

Для внутреннего тестирования рекомендуется:

- остановить старый агент;
- выполнить clean reinstall;
- затем заново запустить сервис.

Backward-compatible auto-update с `rc7/rc8` намеренно не гарантируется.

## Артефакты релиза

- `wrtmonitor-truenas-v0.1.1-rc9.yaml`
- `wrtmonitor-openwrt-agent-v0.1.1-rc9.tar.gz`
- `wrtmonitor-android-v0.1.1-rc9-debug.apk`
- `SHA256SUMS.txt`
