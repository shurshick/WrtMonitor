#!/bin/sh
set -eu

SERVER_URL=""
DEVICE_TOKEN=""
DEVICE_ID=""
NAME=""
ADMIN_USERNAME=""
ADMIN_PASSWORD=""

missing_packages=""

add_missing_package() {
  command_name="$1"
  package_name="$2"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    missing_packages="$missing_packages $package_name"
  fi
}

has_ca_bundle() {
  [ -r /etc/ssl/certs/ca-certificates.crt ] || [ -r /etc/ssl/cert.pem ]
}

ensure_dependencies() {
  add_missing_package curl curl
  add_missing_package jsonfilter jsonfilter
  add_missing_package uci uci
  add_missing_package ubus ubus
  if ! has_ca_bundle; then
    missing_packages="$missing_packages ca-bundle"
  fi

  if [ -n "$missing_packages" ]; then
    if ! command -v opkg >/dev/null 2>&1; then
      echo "Cannot install dependencies: opkg is not available" >&2
      exit 1
    fi
    echo "Installing agent dependencies:$missing_packages"
    opkg update
    # shellcheck disable=SC2086
    opkg install $missing_packages
  fi

  for command_name in curl jsonfilter uci ubus; do
    if ! command -v "$command_name" >/dev/null 2>&1; then
      echo "Required dependency is unavailable after installation: $command_name" >&2
      exit 1
    fi
  done
  if ! has_ca_bundle; then
    echo "Required dependency is unavailable after installation: ca-bundle" >&2
    exit 1
  fi
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --server) SERVER_URL="$2"; shift 2 ;;
    --admin-user) ADMIN_USERNAME="$2"; shift 2 ;;
    --admin-password) ADMIN_PASSWORD="$2"; shift 2 ;;
    --token) DEVICE_TOKEN="$2"; shift 2 ;;
    --name) NAME="$2"; shift 2 ;;
    *) echo "Unknown argument: $1" >&2; exit 1 ;;
  esac
done

ensure_dependencies

prompt_value() {
  label="$1"
  current="$2"
  required="$3"
  while [ -z "$current" ]; do
    printf '%s: ' "$label" >&2
    read -r current
    if [ "$required" != "1" ]; then
      break
    fi
  done
  printf '%s' "$current"
}

prompt_secret() {
  label="$1"
  current="$2"
  if [ -n "$current" ]; then
    printf '%s' "$current"
    return
  fi
  printf '%s: ' "$label" >&2
  if command -v stty >/dev/null 2>&1; then
    stty -echo
    read -r current
    stty echo
    printf '\n' >&2
  else
    read -r current
  fi
  printf '%s' "$current"
}

json_escape() {
  printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

openwrt_firmware_description() {
  if [ -r /etc/openwrt_release ]; then
    value="$(sed -n "s/^DISTRIB_DESCRIPTION='\(.*\)'/\1/p" /etc/openwrt_release | head -n 1)"
    if [ -n "$value" ]; then
      printf '%s' "$value"
      return
    fi
  fi
  printf 'OpenWrt'
}

post_json() {
  path="$1"
  body="$2"
  auth="${3:-}"
  if [ -n "$auth" ]; then
    curl -fsS -X POST "$SERVER_URL$path" -H "Content-Type: application/json" -H "Authorization: Bearer $auth" -d "$body"
  else
    curl -fsS -X POST "$SERVER_URL$path" -H "Content-Type: application/json" -d "$body"
  fi
}

if [ -z "$SERVER_URL" ]; then
  SERVER_URL="$(prompt_value 'wrtmonitor server URL, example https://monitor.example.ru' "$SERVER_URL" 1)"
fi
SERVER_URL="$(printf '%s' "$SERVER_URL" | sed 's#/$##')"

if [ -z "$DEVICE_TOKEN" ]; then
  ADMIN_USERNAME="$(prompt_value 'Administrator username' "$ADMIN_USERNAME" 1)"
  ADMIN_PASSWORD="$(prompt_secret 'Administrator password' "$ADMIN_PASSWORD")"
fi

if [ -z "$NAME" ]; then
  NAME="$(prompt_value 'Router name, optional' "$NAME" 0)"
fi

if [ -z "$DEVICE_TOKEN" ]; then
  hostname_value="$(json_escape "$(uci -q get system.@system[0].hostname 2>/dev/null || hostname)")"
  model_value="$(json_escape "$(cat /tmp/sysinfo/model 2>/dev/null || echo OpenWrt)")"
  firmware_value="$(json_escape "$(openwrt_firmware_description)")"
  name_value="$(json_escape "$NAME")"
  login_body="{\"username\":\"$(json_escape "$ADMIN_USERNAME")\",\"password\":\"$(json_escape "$ADMIN_PASSWORD")\"}"
  login_response="$(post_json /api/v1/auth/login "$login_body")"
  admin_token="$(printf '%s' "$login_response" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')"
  [ -n "$admin_token" ] || { echo "Failed to login as administrator" >&2; exit 1; }

  provision_body="{\"hostname\":\"$hostname_value\",\"model\":\"$model_value\",\"firmware\":\"$firmware_value\",\"name\":\"$name_value\"}"
  provision_response="$(post_json /api/v1/devices/provision "$provision_body" "$admin_token")"
  DEVICE_ID="$(printf '%s' "$provision_response" | sed -n 's/.*"device_id":"\([^"]*\)".*/\1/p')"
  DEVICE_TOKEN="$(printf '%s' "$provision_response" | sed -n 's/.*"device_token":"\([^"]*\)".*/\1/p')"
  [ -n "$DEVICE_ID" ] || { echo "Failed to provision device" >&2; exit 1; }
  [ -n "$DEVICE_TOKEN" ] || { echo "Failed to receive device token" >&2; exit 1; }
fi

cp wrtmonitor-agent /usr/bin/wrtmonitor-agent
chmod 0755 /usr/bin/wrtmonitor-agent
cp wrtmonitor.init /etc/init.d/wrtmonitor
chmod 0755 /etc/init.d/wrtmonitor

cat > /etc/config/wrtmonitor <<EOF
config wrtmonitor 'main'
	option enabled '1'
	option server_url '$SERVER_URL'
	option device_token '$DEVICE_TOKEN'
	option device_id '$DEVICE_ID'
	option name '$NAME'
	option interval '60'
EOF

/etc/init.d/wrtmonitor enable
/etc/init.d/wrtmonitor restart
echo "wrtmonitor agent installed"
