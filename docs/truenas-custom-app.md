# TrueNAS Custom App

## Перед первым запуском обязательно замените секреты

Шаблон YAML намеренно содержит значения `change-me-*`. Перед сохранением App укажите собственные значения для `POSTGRES_PASSWORD`, `WRTMONITOR_DATABASE_URL`, `WRTMONITOR_JWT_SECRET` и внешний `WRTMONITOR_PUBLIC_SERVER_URL`.

Секрет можно сгенерировать на Linux/macOS/TrueNAS Shell:

```sh
openssl rand -base64 32
```

Один и тот же пароль PostgreSQL должен быть в `POSTGRES_PASSWORD` и в URL базы. Не публикуйте эти значения в скриншотах или issue.

Полная инструкция: [развёртывание серверной части](server-deployment.md).

## Образ

В TrueNAS YAML используется стабильная ссылка на текущую тестовую сборку:

```text
ghcr.io/shurshick/wrtmonitor:latest
```

В сервисе включён `pull_policy: always`. Это означает: при redeploy TrueNAS всегда проверяет и скачивает новый образ `latest`.

## Важное ограничение Docker

`latest` не является автообновлением работающего контейнера. Если новый релиз опубликован, старый контейнер продолжит работать до ручного redeploy.

## Обновление

1. Apps → `wrtmonitor` → **Edit**.
2. Проверьте image `ghcr.io/shurshick/wrtmonitor:latest`.
3. Нажмите **Save** и дождитесь redeploy.
4. Убедитесь, что App перешёл в `Running`.
5. Откройте `/health` через внешний HTTPS-адрес.

PostgreSQL volume не удаляйте. Его удаление стирает администратора, роутеры и историю telemetry.
