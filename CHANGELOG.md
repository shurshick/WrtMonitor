# Changelog

## v0.1.0-test.11

- Stabilized OpenWrt agent for BusyBox `ash`.
- Added `jsonfilter`-based API response parsing in the agent.
- Added multi-radio Wi-Fi telemetry shape.
- Added backend startup rejection for default database passwords.
- Improved latest telemetry API with `age_seconds`, `is_stale`, and `source`.
- Added telemetry retention: last 100 snapshots per device.
- Added backend E2E and agent smoke tests.
- Improved Android device telemetry screen.

## v0.1.0-test.10

- Added first end-to-end telemetry flow.
- Added latest telemetry API.
- Added Android device screen.
- Rejected default JWT secrets.
