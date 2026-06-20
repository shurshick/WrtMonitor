import hashlib
import hmac


def generate_csrf_token(session_token: str, secret: str) -> str:
    if not session_token or not secret:
        raise ValueError("session token and secret are required")
    return hmac.new(secret.encode(), session_token.encode(), hashlib.sha256).hexdigest()


def verify_csrf_token(session_token: str, csrf_token: str, secret: str) -> bool:
    if not session_token or not csrf_token or not secret:
        return False
    expected = generate_csrf_token(session_token, secret)
    return hmac.compare_digest(expected, csrf_token)
