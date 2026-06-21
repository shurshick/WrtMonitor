"""Add archived_at to devices for soft delete."""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op


revision: str = "0003_device_archive"
down_revision: str | None = "0002_command_lifecycle"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "devices",
        sa.Column("archived_at", sa.DateTime(timezone=True), nullable=True),
    )


def downgrade() -> None:
    op.drop_column("devices", "archived_at")
