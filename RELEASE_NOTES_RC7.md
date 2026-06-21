# v0.1.1-rc7-agent-update-safety

## Изменения релиза

- OpenWrt agent теперь проверяет `SHA256SUMS.txt` перед установкой обновления.
- Перед заменой agent создаётся backup текущих файлов и metadata backup.
- Добавлен rollback при ошибке установки или в ручном режиме через `wrtmonitor-agent rollback`.
- Добавлены `agent-version.txt`, `update-status`, lock обновления и защита от downgrade.
- Web UI и Android показывают версию agent, статус auto-update, последнюю проверку и последнюю ошибку.
- `disabled` устройство теперь можно убрать из списка через soft-archive без потери telemetry/history.
