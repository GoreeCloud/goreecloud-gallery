# GoreeCloud Gallery

GoreeCloud Gallery is a GoreeCloud-maintained Android gallery application based on Fossify Gallery and governed by the GoreeCloud **Glaze UI** design language.

## Status

**Current packaged candidate:** `1.0.0`

**Current maintained reconstruction patch line:** `gc.9`

The application remains an **Acceptance Candidate**, not a Stable production release. The `1.0.0` semantic version is intentionally final so the exact signed and device-tested binary can later be promoted without rebuilding merely to change a version suffix.

## Product boundary

GoreeCloud Gallery is intended to be:

- offline-first and local-media focused;
- free of analytics, advertising, tracking, cloud accounts, and remote APIs;
- built without `android.permission.INTERNET`;
- independently installable as `com.goreecloud.gallery`;
- visually and interactively governed by Glaze UI;
- open source and maintained under the applicable upstream GNU GPL v3 license requirements.

## User and privacy model

GoreeCloud Gallery is a local Android client, not a shared GoreeCloud server application. It does not create an in-app account system merely to simulate multi-user behavior.

Its user boundary is the Android operating-system user/profile model, application sandbox, permission framework, and platform-authorized media/storage access. The application must not intentionally bypass Android user/profile isolation or combine private media across users or managed profiles without an explicit platform-authorized user action.

Stable-release acceptance includes a secondary-user or managed/work-profile check when a representative device supports it. See `docs/ARCHITECTURE.md` for the runtime and security model.

## Glaze UI contract

Glaze UI is a release requirement for GoreeCloud-controlled Gallery surfaces, not optional polish. `docs/GLAZE-UI.md` defines Gallery-specific requirements for identity, rounded presentation, light/dark behavior, contrast, accessibility, secondary surfaces, destructive actions, upstream-brand regression, exceptions, and Stable-release review.

No permanent Glaze UI exception is currently approved.

## Exact upstream baseline

The current build is reproducible from pinned upstream revisions:

- Fossify Gallery 1.13.1 — `b28299dc33821eee8d108a9880ce87876cf31443`
- Fossify Commons 6.1.5 — `acfd352df1a1852d17a5f77def8b7ad6e522a5b6`

The build workflow checks out these exact commits before applying the ordered GoreeCloud patch chain.

## GoreeCloud patch chain

The repository preserves deterministic gc.1 through gc.9 transformations under `patches/gc1/` through `patches/gc9/`.

The chain covers GoreeCloud package identity, branding, Glaze UI palette and surfaces, launcher behavior, upstream counterfeit-warning removal, rounded-thumbnail enforcement, settings/dialog refinements, popup contrast, the accepted gc.7 `MySearchMenu` toolbar overflow correction, the gc.8 behavioral-test/policy foundation, and gc.9 final `1.0.0` package identity.

Each transformation is designed to fail closed when expected upstream source shape changes. Accepted historical patches are provenance; future behavior changes should extend the ordered patch line instead of rewriting accepted history.

## Repository structure

```text
.github/        GitHub Actions, Dependabot, PR template, CODEOWNERS
docs/           architecture, Glaze UI, build/release, signing, readiness and evidence
patches/        preserved gc.1-gc.9 source transformations
scripts/        reconstruction, structure/security, source, APK, and evidence validation
README.md       project entry point
SECURITY.md     vulnerability and security boundary
CONTRIBUTING.md contribution and review requirements
NOTICE.md       modification, licensing, and upstream provenance notice
```

Generated `.build/`, `upstream-gallery/`, `upstream-commons/`, `dist/`, APK/AAB files, and signing-key containers do not belong in Git.

## Build and validation

GitHub Actions checks out the exact revision under review, validates repository structure and security guardrails, reconstructs pinned upstream source, applies the GoreeCloud patch chain, runs centralized source acceptance checks, executes required GoreeCloud JVM behavioral tests and Android lint, builds the FOSS acceptance APK, verifies packaged application identity/version/signature/debug state/offline permission boundary/GoreeCloud notice/removed-warning boundary, and publishes the APK plus SHA-256, licensing notices, test evidence, lint evidence, and machine-readable build identity.

Behavioral-test validation is fail closed. CI requires JUnit XML proving `org.fossify.gallery.helpers.GoreeCloudGalleryPolicyTest` executed with at least three tests and zero failures/errors; a merely successful `NO-SOURCE` Gradle task is not accepted as behavioral coverage.

The validated suite protects GoreeCloud's enforced thumbnail behavior: thumbnails remain uncropped, file thumbnails remain rounded, and folder thumbnails remain rounded. Device-sensitive permission, file-operation, profile-isolation, accessibility, lifecycle, and Glaze UI behavior remains subject to controlled real-device acceptance.

Build-evidence schema v2 records both the exact validated repository checkout commit and the maintained GoreeCloud patch line separately from the packaged application version.

Third-party GitHub Actions used by the build are pinned to reviewed commit SHAs, checkout credentials are not persisted, and validation workflows retain read-only repository permissions.

See `docs/BUILD-AND-RELEASE.md`, `docs/REPOSITORY-READINESS.md`, `docs/GLAZE-UI.md`, `docs/STABLE-CANDIDATE-1.0.0.md`, and `docs/RELEASE-EVIDENCE-TEMPLATE.md` for the full readiness model.

## Release-signing foundation

A separate manual workflow, `.github/workflows/build-signed-release-candidate.yml`, provides the controlled path for creating a long-lived-key-signed release candidate. It is isolated from normal pull-request validation and expects signing material only from the protected `stable-release` GitHub environment.

The workflow does **not** publish Stable automatically. It validates repository/source controls and behavioral tests before building, aligning, signing, verifying, checking the approved signer fingerprint, and uploading a signed candidate with evidence for manual Stable acceptance.

The final Stable release must reuse the exact accepted signed `1.0.0` binary. Any build-input or APK-content change creates a new candidate and invalidates affected acceptance evidence.

See `docs/RELEASE-SIGNING.md`, `docs/STABLE-SIGNING-RUNBOOK.md`, and `docs/STABLE-RELEASE-CHECKLIST.md`.

## Stable-release work still required

Source-controlled `1.0.0` acceptance is green, but Stable promotion still requires:

- creation and protected recovery copies of the long-lived release key;
- configuration and verification of the protected `stable-release` environment and signed-candidate workflow;
- repository-level protection for `main` so routine direct pushes cannot bypass acceptance CI;
- broader real-device permission, destructive file-operation, hidden/excluded media, editing, sharing, lifecycle, Android user/profile, accessibility, and Glaze UI acceptance;
- in-place upgrade and documented bad-release recovery validation using the long-lived signing identity;
- final complete release evidence and deliberate Stable approval.

Until those gates are complete, `1.0.0` remains an Acceptance Candidate or Signed Release Candidate, not Stable.

## Security and contribution guidance

- Read `SECURITY.md` before reporting a vulnerability or changing a security-sensitive flow.
- Read `CONTRIBUTING.md` before modifying the patch chain, build/release logic, file operations, or Glaze UI surfaces.
- Read `NOTICE.md` for the GoreeCloud modification and upstream provenance boundary.

## License and attribution

GoreeCloud Gallery remains subject to upstream GNU GPL v3 licensing requirements. Upstream authorship, copyright, licensing, and source provenance must remain preserved. GoreeCloud modifications do not remove or replace upstream license obligations.

Produced acceptance/release evidence includes the complete upstream GPLv3 license text. `NOTICE.md` records the modified-work and exact-source provenance boundary; it is not a replacement for the GPL license terms.
