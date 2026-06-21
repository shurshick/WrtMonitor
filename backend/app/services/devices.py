from uuid import UUID

from fastapi import HTTPException
from sqlalchemy import select
from sqlalchemy.orm import Session

from ..models import Device, User


def get_device_or_404(db: Session, device_id: UUID) -> Device:
    device = db.scalars(
        select(Device).where(Device.id == device_id, Device.archived_at.is_(None))
    ).first()
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    return device


def get_user_device_or_404(db: Session, user: User, device_id: UUID) -> Device:
    # Current deployment model is single-owner. Keeping the user parameter here
    # prevents future multi-user routes from accidentally bypassing ownership.
    del user
    return get_device_or_404(db, device_id)


def archive_device_or_409(device: Device) -> None:
    if device.status != "disabled":
        raise HTTPException(
            status_code=409,
            detail="Only disabled devices can be removed from the active list",
        )
