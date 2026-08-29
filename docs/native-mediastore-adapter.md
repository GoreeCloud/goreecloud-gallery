# Native MediaStore Adapter Boundary

## Purpose

This milestone defines the first-party normalization boundary between Android MediaStore rows and the authoritative GoreeCloud Gallery native media model.

It does not yet create a device `ContentResolver`, request Android media permissions, render a Gallery screen, mutate MediaStore, or establish production acceptance. The adapter contract is intentionally kept in the pure Kotlin native core so provider-row normalization can be tested without Android runtime dependencies.

## Projection

`MediaStoreProjection.columns` defines the current minimum row surface expected from a future Android MediaStore query:

- `_id`
- `_display_name`
- `mime_type`
- `date_taken`
- `date_modified`
- `width`
- `height`
- `duration`
- `_size`
- `bucket_id`
- `bucket_display_name`

The Android adapter remains responsible for reading those columns from an authoritative `ContentResolver` cursor. Browser, network, sync, and third-party metadata are not substitutes for the local Android media authority.

## Normalization

`MediaStoreRow.toMediaItem()`:

- requires a `content://` collection URI;
- requires a non-negative provider ID;
- accepts only image or video MIME types;
- converts Android `DATE_TAKEN` milliseconds to `Instant`;
- converts Android `DATE_MODIFIED` seconds to `Instant`;
- derives the item content URI from the collection URI and provider ID;
- keeps image duration unset even when a provider row contains a duration value;
- maps album metadata only when both bucket ID and bucket display name are complete and nonblank;
- leaves incomplete bucket metadata ungrouped instead of fabricating an album;
- fails closed for invalid dimensions, negative sizes/timestamps, unsupported media, or invalid URI authority.

## Current Acceptance Boundary

This is a source-level adapter contract and test milestone only. It does not establish:

- an Android application shell;
- a real MediaStore query or cursor implementation;
- runtime media permission handling;
- rendered timeline, album, or viewer UI;
- MediaStore write/delete/move authorization;
- device or emulator acceptance;
- Privacy Shield, Wardveil Security, Everkeep, or Glaze UI production integration acceptance;
- release signing, deployment, or Stable qualification.

Those require separate implementation and evidence.
