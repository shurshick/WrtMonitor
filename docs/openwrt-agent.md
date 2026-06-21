# OpenWrt agent

`wrtmonitor-agent` регистрирует роутер, отправляет telemetry, получает команды с сервера и умеет безопасно обновлять сам себя с собственного сервера WrtMonitor.

## Требования

- OpenWrt с `opkg`;
- исходящий доступ роутера к серверу WrtMonitor;
- созданный администратор сервера;
- для HTTPS нужен `ca-bundle`.

Установщик сам подтягивает зависимости через `opkg`, если их не хватает:

- `curl`
- `jsonfilter`
- `uci`
- `ubus`
- `ca-bundle`
- `coreutils-sha256sum`

## Установка с собственного сервера

Рекомендуемый способ:

```sh
cd /tmp
BASE_URL='https://monitor.example.ru/downloads/openwrt'

wget -O wrtmonitor-agent "$BASE_URL/wrtmonitor-agent"
wget -O wrtmonitor.init "$BASE_URL/wrtmonitor.init"
wget -O install-openwrt.sh "$BASE_URL/install-openwrt.sh"
chmod 0755 wrtmonitor-agent wrtmonitor.init install-openwrt.sh

sh install-openwrt.sh \
  --server 'https://monitor.example.ru' \
  --admin-user 'admin@example.com' \
  --admin-password 'your-admin-password' \
  --name 'HomeRouter'
```

## Установка из GitHub Release

Если сервер ещё не обновлён до нужной версии:

```sh
cd /tmp
wget -O wrtmonitor-agent.tar.gz \
  https://github.com/shurshick/wrtmonitor/releases/download/v0.1.1-rc7-agent-update-safety/wrtmonitor-openwrt-agent-v0.1.1-rc7.tar.gz
tar -xzf wrtmonitor-agent.tar.gz
sh install-openwrt.sh \
  --server 'https://monitor.example.ru' \
  --admin-user 'admin@example.com' \
  --admin-password 'your-admin-password' \
  --name 'HomeRouter'
```

## Проверка после установки

```sh
uci show wrtmonitor
/etc/init.d/wrtmonitor enabled
ps | grep wrtmonitor
wrtmonitor-agent version
wrtmonitor-agent send-now
logread | grep wrtmonitor | tail -50
```

## Auto-update

Agent проверяет свой сервер по адресу:

```text
https://monitor.example.ru/downloads/openwrt/
```

Проверка выполняется:

- при старте;
- затем раз в `update_interval_hours`, по умолчанию раз в 6 часов.

Во время обновления agent:

1. скачивает `wrtmonitor-agent`, `wrtmonitor.init`, `install-openwrt.sh`, `agent-version.txt`, `SHA256SUMS.txt`;
2. проверяет `SHA-256` для каждого файла;
3. делает `sh -n` для shell-скриптов;
4. сохраняет backup предыдущей версии;
5. заменяет файлы;
6. при ошибке выполняет rollback.

## Manual update

```sh
wrtmonitor-agent version
wrtmonitor-agent update
wrtmonitor-agent update --force
wrtmonitor-agent update --allow-downgrade
wrtmonitor-agent update-status
wrtmonitor-agent update-status --json
```

`--force` переустанавливает даже ту же самую версию.

`--allow-downgrade` разрешает ручной downgrade, если на сервере лежит более старая версия.

## Rollback

Ручной rollback:

```sh
wrtmonitor-agent rollback
```

Backup хранится в:

```text
/etc/wrtmonitor/backup/
```

Там лежат:

- `wrtmonitor-agent.previous`
- `wrtmonitor.init.previous`
- `VERSION.previous`
- `backup-info.txt`

## Disable auto-update

```sh
uci get wrtmonitor.main.auto_update
uci set wrtmonitor.main.auto_update='0'
uci commit wrtmonitor
```

Дополнительные параметры:

```sh
uci set wrtmonitor.main.update_interval_hours='6'
uci set wrtmonitor.main.update_channel='stable'
uci set wrtmonitor.main.allow_downgrade='0'
uci commit wrtmonitor
```

## Update status

```sh
wrtmonitor-agent update-status
```

Показывает:

- `current_version`
- `available_version`
- `auto_update`
- `last_update_check`
- `last_update_status`
- `last_update_error`
- `last_successful_update`
- `backup_available`
- `update_source`

## SHA256 verification

Server должен отдавать:

```text
/downloads/openwrt/SHA256SUMS.txt
/downloads/openwrt/agent-version.txt
```

Проверить вручную можно так:

```sh
cd /tmp
BASE_URL='https://monitor.example.ru/downloads/openwrt'
wget -O wrtmonitor-agent "$BASE_URL/wrtmonitor-agent"
wget -O SHA256SUMS.txt "$BASE_URL/SHA256SUMS.txt"
sha256sum wrtmonitor-agent
grep 'wrtmonitor-agent' SHA256SUMS.txt
```

## Удаление агента

```sh
/etc/init.d/wrtmonitor stop 2>/dev/null || true
/etc/init.d/wrtmonitor disable 2>/dev/null || true
rm -f /usr/bin/wrtmonitor-agent
rm -f /etc/init.d/wrtmonitor
rm -f /etc/config/wrtmonitor
rm -rf /etc/wrtmonitor
```

## Troubleshooting

Проверка логов:

```sh
logread | grep wrtmonitor | tail -50
```

Типовые ситуации:

- `checksum mismatch`
  Обычно сервер раздаёт не те файлы или `SHA256SUMS.txt` устарел.
- `download failed`
  Нет доступа к серверу, DNS или HTTPS.
- `sh -n failed`
  Повреждён скачанный shell-файл.
- `rollback completed`
  Обновление сорвалось, agent вернул предыдущую рабочую версию.
- `rollback unavailable`
  Backup ещё не был создан.
- `server unreachable`
  Проверьте `server_url`, DNS, шлюз и сертификаты.
- `ca-bundle missing`
  Установите пакет `ca-bundle`.
- `jsonfilter missing`
  Установите пакет `jsonfilter`.
