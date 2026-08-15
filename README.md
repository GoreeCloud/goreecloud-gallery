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

## User and privacy model

GoreeCloud Gallery is a local Android client, not a shared GoreeCloud server application. It does not create an in-app account system merely to simulate multi-user behavior.

Its user boundary is the Android operating-system user/profile model, application sandbox, permission framework, and platform-authorized media/storage access. The application must not intentionally bypass Android user/profile isolation or combine private media across users or managed profiles without an explicit platform-authorized user action.

This is the application-appropriate implementation of GoreeCloud's multi-user and private-data-boundary requirements. Stable-release acceptance should include a secondary-user or managed/work-profile check when a representative device supports it.

See `docs/ARCHITECTURE.md` for the full runtime and security model.

## Exact upstream baseline

The current build is reproducible from pinned upstream revisions:

- Fossify Gallery 1.13.1 — `b28299dc33821eee8d108a9880ce87876cf31443`
- Fossify Commons 6.1.5 — `acfd352df1a1852d17a5f77def8b7ad6e522a5b6`

The build workflow checks out these exact commits before applying the GoreeCloud patch chain.

## GoreeCloud patch chain

The repository preserves the deterministic gc.1 through gc.7 source transformations under `patches/gc1/` through `patches/gc7/`. `scripts/materialize-patches.sh` reconstructs the historical patch programs under the generated `.build/patches/` directory and verifies their exact Git blob identities before execution.

The chain covers GoreeCloud package identity, branding, Glaze UI palette and surfaces, launcher behavior, upstream counterfeit-warning removal, rounded-thumbnail enforcement, settings/dialog refinements, popup contrast, and the gc.7 `MySearchMenu` toolbar overflow correction.

Each transformation is designed to fail closed when the expected upstream source shape changes. Accepted historical patches are provenance; future behavior changes should normally extend the ordered patch line instead of rewriting accepted history.

## Repository structure

The repository keeps generated upstream working trees and build artifacts separate from maintained source and governance records:

```text
.github/       GitHub Actions, Dependabot, PR template, CODEOWNERS
docs/          architecture, build/release, signing, stable-release evidence
patches/       preserved gc.1-gc.7 source transformations
scripts/       reconstruction, structure/security, source, APK, and evidence validation
README.md      project entry point
SECURITY.md    vulnerability and security boundary
CONTRIBUTING.md contribution and review requirements
NOTICE.md      modification, licensing, and upstream provenance notice
```

Generated `.build/`, `upstream-gallery/`, `upstream-commons/`, `dist/`, APK/AAB files, and signing-key containers do not belong in Git.

## Build and validation

GitHub Actions checks out the exact revision under review, validates repository structure and security guardrails, reconstructs the pinned upstream source, applies the GoreeCloud patch chain, runs centralized source acceptance checks, executes the Android unit-test task and lint, builds the FOSS acceptance APK, verifies the packaged application identity, version, signature validity, debug state, offline permission boundary, GoreeCloud notice, and removed-warning boundary, and publishes the APK plus SHA-256, licensing notices, unit-test interpretation, build metadata, and available validation reports as an Actions artifact.

Third-party GitHub Actions used by the build are pinned to reviewed commit SHAs, checkout credentials are not persisted, and the workflow has read-only repository permissions.

The upstream Gradle unit-test task may report `NO-SOURCE`. A green task in that state is retained as build evidence but is explicitly not represented as meaningful behavioral unit-test coverage. GoreeCloud-owned automated tests remain tracked as release-readiness work.

See `docs/BUILD-AND-RELEASE.md` for the full build and release model and `docs/RELEASE-EVIDENCE-TEMPLATE.md` for the candidate evidence record.

## Release-signing foundation

A separate manual workflow, `.github/workflows/build-signed-release-candidate.yml`, provides the controlled path for creating a long-lived-key-signed release candidate. It is intentionally isolated from normal pull-request validation and expects signing material only from the protected `stable-release` GitHub environment.

The workflow does **not** publish a stable release automatically. It checks out the exact dispatched revision, builds, aligns, signs, verifies, checks the approved signer fingerprint, and uploads a signed candidate and evidence for manual stable-release acceptance.

See:

- `docs/RELEASE-SIGNING.md` for the signing-key and secret boundary;
- `docs/STABLE-RELEASE-CHECKLIST.md` for the automated and real-device gates required before stable promotion.

## Repository migration

Earlier GoreeCloud Gallery APK development was intentionally isolated on the `build/goreecloud-gallery-apk` branch of `GoreeCloud/goreecloud-website`. This repository is now the authoritative home for GoreeCloud Gallery development. The website repository is historical build-carrier evidence only and is not the long-term application source location.

## Stable-release work still required

The stable-release engineering foundation now includes guarded signing, certificate-fingerprint verification, centralized APK validation, repository structure/security validation, exact-revision CI, machine-readable build evidence, a release checklist, pinned Actions dependencies, Dependabot tracking, and public security/contribution/provenance records.

Before stable promotion, GoreeCloud Gallery still requires creation and protected backup of the long-lived release key, configuration of the `stable-release` environment secrets, successful signed-candidate CI, broader real-device destructive-file-operation and storage-permission acceptance, Android user/profile isolation acceptance where practical, upgrade/recovery validation, accessibility review, meaningful GoreeCloud-owned behavioral tests or an explicit justified manual-only boundary, and final release evidence.

The `main` branch also remains subject to the repository-level protection work tracked separately in GitHub so acceptance CI cannot be bypassed by routine direct pushes.

## Security and contribution guidance

- Read `SECURITY.md` before reporting a vulnerability or changing a security-sensitive flow.
- Read `CONTRIBUTING.md` before modifying the patch chain, build/release logic, file operations, or Glaze UI surfaces.
- Read `NOTICE.md` for the GoreeCloud modification and upstream provenance boundary.

## License and attribution

GoreeCloud Gallery remains subject to the upstream GNU GPL v3 licensing requirements. Upstream authorship, copyright, licensing, and source provenance must remain preserved. GoreeCloud modifications do not remove or replace upstream license obligations.

The produced acceptance/release evidence includes the complete upstream GPLv3 license text. `NOTICE.md` records the modified-work and exact-source provenance boundary; it is not a replacement for the GPL license terms.
