# OpenWrt agent

Agent устанавливается на роутер и работает как клиент сервера.

Функции:

- регистрация устройства;
- отправка heartbeat;
- отправка telemetry;
- получение команд;
- выполнение allowlist-команд через `uci`, `wifi`, `reboot`, `ubus`.

Пример установки:

```sh
sh install-openwrt.sh \
  --server https://monitor.example.ru \
  --token DEVICE_TOKEN \
  --name HomeRouter
```

Если параметры не переданы, установщик спросит адрес сервера, логин/пароль администратора и имя роутера в консоли. После входа администратором установщик вызывает `/api/v1/devices/provision`, получает отдельный device token и сохраняет его в UCI. Пароль администратора на роутере не сохраняется.

Команды управления должны быть ограничены и логироваться.

Текущий allowlist:

- `router.reboot`;
- `wifi.status`;
- `wifi.set_enabled`;
- `wifi.set_ssid`;
- `network.interfaces`.

Произвольный `shell.exec` и произвольный `uci.apply` не поддерживаются.

## v0.1.0-test.11 stability notes

- Agent остаётся POSIX/BusyBox `ash` shell script.
- CI проверяет синтаксис командой `sh -n openwrt-agent/wrtmonitor-agent`.
- Критичные JSON-поля из API response читаются через `jsonfilter`.
- Если `jsonfilter` недоступен, agent пишет ошибку в logread и не выполняет команды.
- Wi-Fi telemetry собирается как multi-radio структура из UCI `wireless`.
- Команды `wifi.set_enabled` и `wifi.set_ssid` принимают параметры `radio` и `iface`; если они не переданы, используется первый radio/iface для обратной совместимости.
