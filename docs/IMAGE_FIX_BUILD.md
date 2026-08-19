# Android card image fix

Checkpoint: 2026-08-18

- Replaced invalid/stale seed Scryfall image UUIDs with verified static image URLs.
- Corrected Commander Masters Sol Ring from collector number 396 to 410.
- Corrected seed metadata discovered during verification.
- Explicitly enabled automatic/network image loading in Android WebView.
- Added regression tests for seed image URL shape and Sol Ring printing coordinates.

This document records the fix only. `docs/MASTER_SPEC.md` remains authoritative.
