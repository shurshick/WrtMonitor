# Проверка на реальном OpenWrt-роутере

Перед изменением SSID, отключением Wi-Fi или reboot убедитесь, что есть SSH-доступ по кабелю и сохранён backup: `sysupgrade -b /tmp/openwrt-backup.tar.gz`.

| Тест | Ожидаемо | Факт | PASS/FAIL | Комментарий |
|---|---|---|---|---|
| Telemetry | radio и SSID видны | | | |
| Wi-Fi on/off | выбранный radio меняет состояние | | | |
| SSID | меняется только выбранный iface | | | |
| Network | interfaces обновляются | | | |
| Reboot | result приходит до reboot | | | |

## Порядок

1. Зафиксируйте модель, версию OpenWrt, target/platform, число radio и исходный SSID.
2. Установите agent, дождитесь telemetry и выполните `wrtmonitor-agent support-bundle` при ошибке.
3. Сначала проверьте Wi-Fi telemetry, затем одну безопасную смену SSID и только после этого on/off и reboot.

## Recovery

Через SSH можно проверить и вернуть wireless:

```sh
uci show wireless
uci set wireless.default_radio0.ssid='старый SSID'
uci commit wireless
wifi reload
wifi
/etc/init.d/network restart
```

Для удаления agent: `/etc/init.d/wrtmonitor stop; /etc/init.d/wrtmonitor disable; rm -f /usr/bin/wrtmonitor-agent /etc/init.d/wrtmonitor /etc/config/wrtmonitor`.
