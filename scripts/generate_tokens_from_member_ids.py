import base64
import hashlib
import hmac
import json
import os
import sys
import time
from pathlib import Path


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")


def encode_token(secret: str, member_id: int, role: str, expires_at_ms: int) -> str:
    header = {"alg": "HS256", "typ": "JWT"}
    payload = {
        "sub": str(member_id),
        "role": role,
        "iat": int(time.time()),
        "exp": expires_at_ms // 1000,
    }

    header_b64 = b64url(json.dumps(header, separators=(",", ":")).encode("utf-8"))
    payload_b64 = b64url(json.dumps(payload, separators=(",", ":")).encode("utf-8"))
    signing_input = f"{header_b64}.{payload_b64}".encode("ascii")
    signature = hmac.new(secret.encode("utf-8"), signing_input, hashlib.sha256).digest()
    return f"{header_b64}.{payload_b64}.{b64url(signature)}"


def main():
    secret = os.environ.get("JWT_SECRET_KEY")
    if not secret:
        raise SystemExit("JWT_SECRET_KEY env is required")

    member_ids_file = Path(os.environ.get("MEMBER_IDS_FILE", ""))
    if not member_ids_file.exists():
        raise SystemExit("MEMBER_IDS_FILE is required and must exist")

    role = os.environ.get("TOKEN_ROLE", "ADMIN")
    expires_at_ms = int(time.time() * 1000) + (1000 * 60 * 30 * 30)
    member_ids = [
        int(value.strip())
        for value in member_ids_file.read_text().split(",")
        if value.strip()
    ]

    tokens = [
        encode_token(secret, member_id, role, expires_at_ms)
        for member_id in member_ids
    ]
    sys.stdout.write(",".join(tokens))


if __name__ == "__main__":
    main()
