# GoreeCloud Gallery

GoreeCloud Gallery is GoreeCloud's offline-first Android gallery for device-local photos and videos.

## Status

**Active Development — native replacement in progress. Not Stable.**

The long-term product is an original GoreeCloud-owned Android application built natively from the ground up. The repository still preserves the earlier Fossify-based `1.0.0` acceptance-candidate reconstruction and gc patch history as **transitional provenance, continuity, regression, and migration reference**. That inherited application is not the long-term GoreeCloud Gallery architecture.

New Gallery product behavior should advance the first-party native implementation unless a narrowly documented migration or compatibility need requires work on the transitional line.

See [docs/NATIVE-MIGRATION.md](docs/NATIVE-MIGRATION.md) for the replacement boundary.

## Product boundary

GoreeCloud Gallery is intended to remain:

- offline-first and local-media focused;
- independently installable as `com.goreecloud.gallery` when the native application is packaged;
- governed by Android user/profile isolation and platform-authorized media access;
- free of advertising and unnecessary tracking;
- governed by the current Stable Glaze UI contract;
- integrated substantively with Privacy Shield, Wardveil Security, and Everkeep where those platform responsibilities apply; and
- honest about which capabilities are source foundations, packaged, device-accepted, released, or Stable.

Optional GoreeCloud Photos integration may be added behind explicit adapters and user control. Local browsing must not depend on a GoreeCloud account, network connection, or cloud service.

## First-party native implementation

The repository now contains GoreeCloud-owned native foundations under `native/`.

`native/core` currently provides framework-independent domain behavior for:

- validated image/video media items;
- deterministic media filtering and sorting;
- local media mutation contracts;
- trash/recovery behavior;
- authoritative album metadata and deterministic album summaries; and
- focused unit tests.

The album model deliberately does not fabricate synthetic albums when the platform has not supplied authoritative grouping metadata. Conflicting display names for one authoritative album identifier fail closed rather than silently merging inconsistent state.

An early Android-facing media repository boundary also exists under `native/src`. These foundations do **not** yet constitute a complete installable native Gallery application.

## Transitional reconstruction line

The repository preserves deterministic Fossify Gallery/Commons reconstruction material and the accepted gc patch chain for historical continuity, provenance, migration comparison, and regression reference.

The pinned historical baseline is:

- Fossify Gallery 1.13.1 — `b28299dc33821eee8d108a9880ce87876cf31443`
- Fossify Commons 6.1.5 — `acfd352df1a1852d17a5f77def8b7ad6e522a5b6`

The prior `1.0.0` candidate and its acceptance evidence remain evidence for that transitional binary only. They are not evidence that the first-party native replacement is complete or Stable. Applicable upstream GPLv3 licensing and provenance obligations remain preserved for that inherited work.

## Platform requirements

Gallery must remain current with the applicable GoreeCloud platform systems:

- **Glaze UI / Design Center** — interface, interaction, accessibility, responsiveness, and design-system conformance.
- **Privacy Shield / Privacy Center** — media permissions, data minimization, privacy controls, consent, and user control.
- **Wardveil Security / Security Center** — protection, validation, safe file/media handling boundaries, diagnostics, and evidence-backed security states.
- **Everkeep / Continuity Center** — recovery, preservation, portability, continuity, and applicable local-media resilience workflows.

These are functional requirements, not decorative labels. Missing or unvalidated required integration blocks Stable qualification.

## Stable-release work

The native application still requires substantial work before Stable qualification, including the final Android application shell, MediaStore integration, local albums and browsing UI, media viewer behavior, approved editing/sharing flows, destructive-operation authorization, hidden/excluded media policy, Android user/profile acceptance, accessibility, Glaze UI acceptance, Privacy Shield/Wardveil/Everkeep integration evidence, packaging/signing, upgrade/recovery validation, and representative physical-device testing.

The old Fossify-based acceptance candidate is not a shortcut around those native acceptance gates.

## Repository guidance

- [docs/NATIVE-MIGRATION.md](docs/NATIVE-MIGRATION.md) — native replacement and transitional-source boundary.
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — existing architecture/security context; portions describing the inherited application must be read as transitional until updated by native milestones.
- [docs/PLATFORM_CONFORMANCE.md](docs/PLATFORM_CONFORMANCE.md) — platform conformance requirements.
- [docs/GLAZE-UI.md](docs/GLAZE-UI.md) — Gallery-specific Glaze UI acceptance history and requirements.
- [SECURITY.md](SECURITY.md) — vulnerability and security boundary guidance.
- [NOTICE.md](NOTICE.md) — inherited-work licensing and provenance notices.

Canonical GoreeCloud application project specifications are maintained under `GoreeCloud/Projects`, and canonical GoreeCloud changelogs are maintained under `GoreeCloud/Changelogs`.
