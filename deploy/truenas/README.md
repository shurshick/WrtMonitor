# Установка на TrueNAS Custom App

Тестовая установка использует готовый Docker image:

```text
ghcr.io/shurshick/wrtmonitor:0.1.0-test.3
```

Если пакет GHCR приватный, сначала добавьте в TrueNAS Docker registry credentials для `ghcr.io`.

## Быстрый порядок

1. Скачайте `wrtmonitor-truenas-0.1.0-test.3.yaml` из релиза.
2. Создайте Custom App из YAML.
3. Задайте переменные.
4. Запустите приложение.
5. Откройте `/setup`.
6. Создайте первого администратора.
7. Проверьте `/health`.

## Переменные для локального теста

```env
WRTMONITOR_PUBLIC_SERVER_URL=http://truenas-ip:8088
WRTMONITOR_HTTP_PORT=8088
WRTMONITOR_JWT_SECRET=replace-with-long-random-secret
POSTGRES_PASSWORD=replace-with-db-password
POSTGRES_DB=wrtmonitor
POSTGRES_USER=wrtmonitor
WRTMONITOR_ALLOW_INSECURE_LOCAL=true
```

## Переменные для HTTPS

```env
WRTMONITOR_PUBLIC_SERVER_URL=https://monitor.example.ru
WRTMONITOR_HTTP_PORT=8088
WRTMONITOR_JWT_SECRET=replace-with-long-random-secret
POSTGRES_PASSWORD=replace-with-db-password
POSTGRES_DB=wrtmonitor
POSTGRES_USER=wrtmonitor
WRTMONITOR_ALLOW_INSECURE_LOCAL=false
```

После запуска откройте:

```text
http://truenas-ip:8088/setup
```

Проверка:

```text
http://truenas-ip:8088/health
```

Подробная инструкция: [`docs/server-deployment.md`](../../docs/server-deployment.md).
