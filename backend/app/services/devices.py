from uuid import UUID

from fastapi import HTTPException
from sqlalchemy.orm import Session

from ..models import Device, User


def get_device_or_404(db: Session, device_id: UUID) -> Device:
    device = db.get(Device, device_id)
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    return device


def get_user_device_or_404(db: Session, user: User, device_id: UUID) -> Device:
    # Current deployment model is single-owner. Keeping the user parameter here
    # prevents future multi-user routes from accidentally bypassing ownership.
    del user
    return get_device_or_404(db, device_id)
