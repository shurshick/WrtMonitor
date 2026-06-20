# Безопасность Web UI

Web UI использует cookie-сессию `wrtmonitor_session` с флагами `HttpOnly` и `SameSite=Lax`.
Все изменяющие состояние формы (`/setup`, `/logout`, создание команды и удаление устройства) содержат CSRF-токен. Токен привязан к сессии и подписан серверным JWT secret; пустой или неверный токен возвращает HTTP 403.

Ответы Web UI получают заголовки `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, `Permissions-Policy` и строгий `Content-Security-Policy`. Bearer API Android и OpenWrt-agent не используют cookie-сессию и CSRF для них не применяется.
