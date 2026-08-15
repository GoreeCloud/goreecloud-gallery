# GoreeCloud Gallery

GoreeCloud Gallery is a GoreeCloud-maintained Android gallery application based on Fossify Gallery and styled with the GoreeCloud **Glaze UI** design language.

## Status

**Current acceptance line:** `1.0.0-gc.7`

The current implementation has completed the first dedicated-repository migration from the temporary `GoreeCloud/goreecloud-website` build carrier. The application remains an acceptance candidate rather than a stable production release.

Real-device acceptance has confirmed the gc.7 overflow-menu correction on both the main folders screen and an opened media folder. The previously observed dark-popup/dark-text defect is no longer present in the accepted screenshots.

## Product boundary

GoreeCloud Gallery is intended to be:

- offline-first and local-media focused;
- free of analytics, advertising, tracking, cloud accounts, and remote APIs;
- built without `android.permission.INTERNET`;
- independently installable as `com.goreecloud.gallery`;
- visually governed by Glaze UI;
- open source and maintained under the applicable upstream GNU GPL v3 license requirements.

## Exact upstream baseline

The current build is reproducible from pinned upstream revisions:

- Fossify Gallery 1.13.1 — `b28299dc33821eee8d108a9880ce87876cf31443`
- Fossify Commons 6.1.5 — `acfd352df1a1852d17a5f77def8b7ad6e522a5b6`

The build workflow checks out these exact commits before applying the GoreeCloud patch chain.

## GoreeCloud patch chain

The repository preserves the deterministic gc.1 through gc.7 source transformations under `patches/gc1/` through `patches/gc7/`. `scripts/materialize-patches.sh` reconstructs the historical patch programs under the generated `.build/patches/` directory and verifies their exact Git blob identities before execution.

The chain covers GoreeCloud package identity, branding, Glaze UI palette and surfaces, launcher behavior, upstream counterfeit-warning removal, rounded-thumbnail enforcement, settings/dialog refinements, popup contrast, and the gc.7 `MySearchMenu` toolbar overflow correction.

Each transformation is designed to fail closed when the expected upstream source shape changes.

## Build and validation

GitHub Actions reconstructs the pinned upstream source, applies the GoreeCloud patch chain, runs centralized source acceptance checks, executes the Android unit-test task and lint, builds the FOSS acceptance APK, verifies the packaged application identity, version, signature validity, debug state, offline permission boundary, GoreeCloud notice, and removed-warning boundary, and publishes the APK plus SHA-256, licensing notices, and available validation reports as an Actions artifact.

Third-party GitHub Actions used by the build are pinned to reviewed commit SHAs, checkout credentials are not persisted, and the workflow has read-only repository permissions.

See `docs/BUILD-AND-RELEASE.md` for the full build and release model.

## Release-signing foundation

A separate manual workflow, `.github/workflows/build-signed-release-candidate.yml`, provides the controlled path for creating a long-lived-key-signed release candidate. It is intentionally isolated from normal pull-request validation and expects signing material only from the protected `stable-release` GitHub environment.

The workflow does **not** publish a stable release automatically. It builds, aligns, signs, verifies, checks the approved signer fingerprint, and uploads a signed candidate for manual stable-release acceptance.

See:

- `docs/RELEASE-SIGNING.md` for the signing-key and secret boundary;
- `docs/STABLE-RELEASE-CHECKLIST.md` for the automated and real-device gates required before stable promotion.

## Repository migration

Earlier GoreeCloud Gallery APK development was intentionally isolated on the `build/goreecloud-gallery-apk` branch of `GoreeCloud/goreecloud-website`. This repository is now the authoritative home for GoreeCloud Gallery development. The website repository is historical build-carrier evidence only and is not the long-term application source location.

## Stable-release work still required

The stable-release engineering foundation now includes a guarded signing workflow, certificate-fingerprint verification, centralized APK validation, a release checklist, pinned Actions dependencies, and Dependabot tracking for GitHub Actions.

Before stable promotion, GoreeCloud Gallery still requires creation and protected backup of the long-lived release key, configuration of the `stable-release` environment secrets, successful signed-candidate CI, broader real-device destructive-file-operation and storage-permission acceptance, upgrade/recovery validation, accessibility review, and final release evidence.

## License and attribution

GoreeCloud Gallery remains subject to the upstream GNU GPL v3 licensing requirements. Upstream authorship, copyright, licensing, and source provenance must remain preserved. GoreeCloud modifications do not remove or replace upstream license obligations.
