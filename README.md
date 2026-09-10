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
- independently installable as `com.goreecloud.gallery`;
- governed by Android user/profile isolation and platform-authorized media access;
- free of advertising and unnecessary tracking;
- governed by the current Stable Glaze UI contract;
- integrated substantively with Privacy Shield, Wardveil Security, Everkeep, Manager, Mesh, and Identity where those platform responsibilities apply; and
- honest about which capabilities are source foundations, packaged, device-accepted, released, or Stable.

Optional GoreeCloud Photos integration may be added behind explicit adapters and user control. Local browsing must not depend on a GoreeCloud account, network connection, or cloud service.

## First-party native implementation

The repository contains GoreeCloud-owned native foundations under `native/`.

`native/core` provides framework-independent domain behavior for validated image/video media items, deterministic filtering and sorting, local mutation contracts, trash/recovery behavior, authoritative album metadata, deterministic album summaries, and MediaStore row normalization.

`native/android-adapter` is a compiled Android library bridge over local `ContentResolver` / Android MediaStore. It reads bounded image/video provider rows, fails rather than fabricating an empty library when no cursor is returned, rejects malformed rows, and maps accepted state into the native core model.

`native/app` is the first-party Android application target using package ID `com.goreecloud.gallery`. The active Development line requires Android media authorization before provider reads, consumes the MediaStore adapter directly, provides bounded local photo/video browsing, Albums and Recycle Bin flows, Favorites and settings behavior, bounded full-screen navigation, Android-authorized Trash/Restore/Purge actions, and a first-party photo-editor candidate with crop, 90-degree rotation, horizontal flip, Reset, and non-destructive Save copy semantics.

The current Development source maps the repository-local native `GalleryGlazeContract` to **GLAZE UI V1.3 / 1.3.0 — Adaptive Resonance** at exact authoritative Glaze revision `8354308445da9ac35ced2b37a7f503a08a0aaf72`. That mapping now uses governed semantic spacing, shape, motion, ordinary target-size, and adaptive-gutter values and changes real navigation/gutter/inset source inputs rather than only relabeling a version string.

This is still **source-migration evidence, not application conformance**. Gallery remains `applicable-migration-required` and globally nonconformant while residual Gallery-controlled presentation values and Android resources await reconciliation and fresh whole-application rendered, interaction, accessibility, adaptive/form-factor, representative-device, performance, Human Visual Excellence, rollback, platform-system, release, and production acceptance. See [docs/GLAZE-UI.md](docs/GLAZE-UI.md).

The current photo-editor candidate is Development evidence only. Representative physical-device/OEM/profile crop/rotate/flip/save-copy flows, source/output orientation fidelity, image-quality and metadata/color behavior, invalid/oversized/provider-failure/cancellation handling, process recreation, accessibility, current-Stable Glaze application acceptance, Platform-System acceptance, signing, Release Candidate qualification, release approval, and Stable qualification remain open.

## Platform Contract

`goreecloud.platform.yaml` is the machine-readable declaration of Gallery's current GoreeCloud Platform Contract state. It deliberately separates:

- the repository-local Glaze V1.3 source mapping now present in Development;
- the still-incomplete whole-application Glaze acceptance gates;
- Development lifecycle state;
- blocked/unaccepted Platform-System integrations; and
- outstanding representative-device, accessibility, recovery, signing, release, and Stable gates.

The Platform Contract workflow validates that declaration against the pinned central contract authority. A green manifest check is evidence of declaration validity, not evidence that blocked integrations or release gates are accepted.

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
- **Everkeep / Continuity Center** — recovery, preservation, portability, continuity, and applicable Gallery-owned state resilience.
- **Manager** — accepted platform visibility and administrative integration where required.
- **GoreeCloud Mesh** — authenticated cross-service registration/capability publication where required.
- **GoreeCloud Identity** — any future account, device, session, Photos-account, or delegated-authority behavior.

These are functional requirements, not decorative labels. Missing or unvalidated required integration blocks Stable qualification.

## Stable-release work

The native application still requires substantial work before Stable qualification, including mature media/viewer/editor behavior, approved organization and sharing flows, destructive-operation edge-case acceptance, hidden/protected/excluded media policy, Android user/profile acceptance, rendered accessibility and current-Stable Glaze UI acceptance, applicable Privacy Shield/Wardveil/Everkeep/Manager/Mesh/Identity integration evidence, packaging/signing, upgrade/recovery validation, and representative physical-device testing.

The old Fossify-based acceptance candidate is not a shortcut around those native acceptance gates.

## Repository guidance

- [USER-MANUAL.md](USER-MANUAL.md) — current first-party native Development user guidance.
- [SPECIFICATIONS.md](SPECIFICATIONS.md) — current native architecture, authority, and acceptance boundaries.
- [FEATURES.md](FEATURES.md) — implemented Development capabilities and incomplete work.
- [BENEFITS.md](BENEFITS.md) — current and intended product benefits without Stable overclaiming.
- [COMPETITIVE-OBJECTIVES.md](COMPETITIVE-OBJECTIVES.md) — current first-party product objectives.
- [goreecloud.platform.yaml](goreecloud.platform.yaml) — machine-readable Development, compatibility, Platform-System, and conformance state.
- [docs/NATIVE-MIGRATION.md](docs/NATIVE-MIGRATION.md) — native replacement and transitional-source boundary.
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — architecture/security context; inherited-application sections remain transitional/historical unless superseded by native milestones.
- [docs/PLATFORM_CONFORMANCE.md](docs/PLATFORM_CONFORMANCE.md) — platform conformance requirements.
- [docs/GLAZE-UI.md](docs/GLAZE-UI.md) — current Gallery-specific V1.3 source mapping and application-acceptance boundary.
- [SECURITY.md](SECURITY.md) — vulnerability and security boundary guidance.
- [NOTICE.md](NOTICE.md) — inherited-work licensing and provenance notices.

Canonical GoreeCloud application project specifications are maintained under `GoreeCloud/Projects`, and canonical GoreeCloud changelogs are maintained under `GoreeCloud/Changelogs`.
