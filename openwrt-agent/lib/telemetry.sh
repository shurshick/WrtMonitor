telemetry() {
    agent_enabled || return 0
    [ -n "$(device_id)" ] || register_device
    body="$(telemetry_payload)"
    api POST /api/v1/agent/telemetry "$body" >/dev/null
}

telemetry_payload() {
    [ -n "$(device_id)" ] || register_device
    uptime_value="$(cut -d. -f1 /proc/uptime 2>/dev/null || echo 0)"
    load_value="$(cut -d' ' -f1 /proc/loadavg 2>/dev/null || echo 0)"
    case "$uptime_value" in
        ""|*[!0-9]*) uptime_value="0" ;;
    esac
    load_value="$(json_escape "$load_value")"
    printf '{"device_id":"%s","telemetry":{"system":{"uptime":%s,"load":"%s","memory":%s,"processes":%s,"ubus":%s},"cpu":%s,"storage":%s,"thermal":%s,"traffic":%s,"board":%s,"network":%s,"network_devices":%s,"wifi":%s,"wireless_status":%s,"agent":%s}}' \
        "$(device_id)" \
        "$uptime_value" \
        "$load_value" \
        "$(memory_json)" \
        "$(processes_json)" \
        "$(ubus_json system info)" \
        "$(cpu_json)" \
        "$(storage_json)" \
        "$(thermal_json)" \
        "$(traffic_json)" \
        "$(ubus_json system board)" \
        "$(network_summary_json)" \
        "$(ubus_json network.device status)" \
        "$(wifi_status_json)" \
        "$(ubus_json network.wireless status)" \
        "$(agent_status_json)"
}

memory_json() {
    total="$(awk '/^MemTotal:/ {print $2}' /proc/meminfo 2>/dev/null || echo 0)"
    free="$(awk '/^MemFree:/ {print $2}' /proc/meminfo 2>/dev/null || echo 0)"
    available="$(awk '/^MemAvailable:/ {print $2}' /proc/meminfo 2>/dev/null || echo 0)"
    case "$total" in ""|*[!0-9]*) total="0" ;; esac
    case "$free" in ""|*[!0-9]*) free="0" ;; esac
    case "$available" in ""|*[!0-9]*) available="0" ;; esac
    printf '{"total_kb":%s,"free_kb":%s,"available_kb":%s}' "$total" "$free" "$available"
}

cpu_json() {
    cores="$(grep -c '^processor' /proc/cpuinfo 2>/dev/null || echo 0)"
    model="$(sed -n 's/^model name[[:space:]]*:[[:space:]]*//p; s/^system type[[:space:]]*:[[:space:]]*//p' /proc/cpuinfo 2>/dev/null | head -n 1)"
    case "$cores" in ""|*[!0-9]*) cores="0" ;; esac
    printf '{"cores":%s,"model":"%s"}' "$cores" "$(json_escape "$model")"
}

storage_json() {
    line="$(df -k /overlay 2>/dev/null | awk 'NR==2 {print $2, $3, $4}')"
    [ -n "$line" ] || line="$(df -k / 2>/dev/null | awk 'NR==2 {print $2, $3, $4}')"
    total="0"
    used="0"
    available="0"
    IFS=' ' read -r total used available <<EOF
$line
EOF
    case "$total" in ""|*[!0-9]*) total="0" ;; esac
    case "$used" in ""|*[!0-9]*) used="0" ;; esac
    case "$available" in ""|*[!0-9]*) available="0" ;; esac
    printf '{"mount":"/overlay","total_kb":%s,"used_kb":%s,"available_kb":%s}' "$total" "$used" "$available"
}

thermal_json() {
    sensor="$(find /sys/class/thermal -name temp -type f 2>/dev/null | head -n 1)"
    if [ -z "$sensor" ] || [ ! -r "$sensor" ]; then
        printf '{"available":false}'
        return
    fi
    milli_celsius="$(cat "$sensor" 2>/dev/null || echo 0)"
    case "$milli_celsius" in ""|*[!0-9]*) milli_celsius="0" ;; esac
    printf '{"available":true,"milli_celsius":%s}' "$milli_celsius"
}

traffic_json() {
    values="$(awk 'NR > 2 && $1 !~ /^lo:/ { rx += $2; tx += $10 } END { printf "%d %d", rx, tx }' /proc/net/dev 2>/dev/null)"
    rx="0"
    tx="0"
    IFS=' ' read -r rx tx <<EOF
$values
EOF
    case "$rx" in ""|*[!0-9]*) rx="0" ;; esac
    case "$tx" in ""|*[!0-9]*) tx="0" ;; esac
    printf '{"rx_bytes":%s,"tx_bytes":%s}' "$rx" "$tx"
}

processes_json() {
    count="$(ps 2>/dev/null | wc -l | tr -d ' ' || echo 0)"
    case "$count" in ""|*[!0-9]*) count="0" ;; esac
    printf '{"count":%s}' "$count"
}

network_summary_json() {
    tmp="/tmp/wrtmonitor-network-$$.json"
    if ! ubus call network.interface dump >"$tmp" 2>/dev/null; then
        rm -f "$tmp"
        printf '{"interfaces":[]}'
        return
    fi
    if ! require_json_tool; then
        rm -f "$tmp"
        printf '{"interfaces":[]}'
        return
    fi
    index=0
    items=""
    while true; do
        name="$(json_get_string "$tmp" "@.interface[$index].interface")"
        [ -n "$name" ] || break
        up="$(json_get_bool "$tmp" "@.interface[$index].up")"
        proto="$(json_get_string "$tmp" "@.interface[$index].proto")"
        device_name="$(json_get_string "$tmp" "@.interface[$index].l3_device")"
        gateway="$(jsonfilter -i "$tmp" -e "@.interface[$index].route[@.target='0.0.0.0'].nexthop" 2>/dev/null | head -n 1)"
        ip4="$(jsonfilter -i "$tmp" -e "@.interface[$index]['ipv4-address'][*].address" 2>/dev/null | tr '\n' ',' | sed 's/,$//')"
        dns="$(jsonfilter -i "$tmp" -e "@.interface[$index]['dns-server'][*]" 2>/dev/null | tr '\n' ',' | sed 's/,$//')"
        ipv4_json=""
        dns_json=""
        old_ifs="$IFS"
        IFS=','
        for value in $ip4; do
            [ -n "$value" ] || continue
            [ -n "$ipv4_json" ] && ipv4_json="$ipv4_json,"
            ipv4_json="$ipv4_json\"$(json_escape "$value")\""
        done
        for value in $dns; do
            [ -n "$value" ] || continue
            [ -n "$dns_json" ] && dns_json="$dns_json,"
            dns_json="$dns_json\"$(json_escape "$value")\""
        done
        IFS="$old_ifs"
        [ -n "$items" ] && items="$items,"
        items="$items{\"interface\":\"$(json_escape "$name")\",\"up\":$( [ "$up" = "true" ] && printf true || printf false ),\"proto\":\"$(json_escape "$proto")\",\"device\":\"$(json_escape "$device_name")\",\"ipv4\":[${ipv4_json}],\"gateway\":\"$(json_escape "$gateway")\",\"dns\":[${dns_json}],\"errors\":[]}"
        index=$((index + 1))
    done
    rm -f "$tmp"
    printf '{"interfaces":[%s]}' "$items"
}

wifi_status_json() {
    radios=""
    index=0
    while uci -q get "wireless.@wifi-device[$index]" >/dev/null 2>&1; do
        name="radio$index"
        disabled="$(uci -q get "wireless.@wifi-device[$index].disabled" 2>/dev/null || echo 0)"
        channel="$(uci -q get "wireless.@wifi-device[$index].channel" 2>/dev/null || true)"
        band="$(uci -q get "wireless.@wifi-device[$index].band" 2>/dev/null || true)"
        ssids=""
        interfaces=""
        encryption=""
        iface_index=0
        while uci -q get "wireless.@wifi-iface[$iface_index]" >/dev/null 2>&1; do
            iface_device="$(uci -q get "wireless.@wifi-iface[$iface_index].device" 2>/dev/null || true)"
            if [ "$iface_device" = "$name" ]; then
                ssid="$(uci -q get "wireless.@wifi-iface[$iface_index].ssid" 2>/dev/null || true)"
                encryption="$(uci -q get "wireless.@wifi-iface[$iface_index].encryption" 2>/dev/null || true)"
                iface_disabled="$(uci -q get "wireless.@wifi-iface[$iface_index].disabled" 2>/dev/null || echo 0)"
                if [ -n "$ssid" ]; then
                    [ -n "$ssids" ] && ssids="$ssids,"
                    ssids="$ssids\"$(json_escape "$ssid")\""
                fi
                [ -n "$interfaces" ] && interfaces="$interfaces,"
                interfaces="$interfaces{\"id\":\"@wifi-iface[$iface_index]\",\"index\":$iface_index,\"ssid\":\"$(json_escape "$ssid")\",\"enabled\":$( [ "$iface_disabled" = "1" ] && printf false || printf true ),\"encryption\":\"$(json_escape "$encryption")\"}"
            fi
            iface_index=$((iface_index + 1))
        done
        up=true
        [ "$disabled" = "1" ] && up=false
        radio="{\"id\":\"$name\",\"name\":\"$name\",\"up\":$up,\"disabled\":$( [ "$disabled" = "1" ] && printf true || printf false ),\"ssid\":[$ssids],\"interfaces\":[${interfaces}]"
        [ -n "$channel" ] && radio="$radio,\"channel\":\"$(json_escape "$channel")\""
        [ -n "$band" ] && radio="$radio,\"band\":\"$(json_escape "$band")\""
        [ -n "${encryption:-}" ] && radio="$radio,\"encryption\":\"$(json_escape "$encryption")\""
        radio="$radio}"
        [ -n "$radios" ] && radios="$radios,"
        radios="$radios$radio"
        index=$((index + 1))
    done
    if [ "$index" -gt 0 ]; then
        printf '{"available":true,"radios":[%s]}' "$radios"
    else
        printf '{"available":false,"radios":[]}'
    fi
}
