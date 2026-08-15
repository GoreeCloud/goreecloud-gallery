# GoreeCloud Gallery Changelog

This changelog records material source, build, validation, release-engineering, and product-readiness changes in the dedicated `GoreeCloud/goreecloud-gallery` repository. Historical development that occurred in the temporary website-repository build carrier remains preserved in Git and in the GoreeCloud patch provenance records.

## Unreleased — Repository readiness hardening

### Added

- root GNU GPL v3 license text;
- `SECURITY.md` vulnerability and security-boundary guidance;
- `CONTRIBUTING.md` contribution, patch-provenance, Glaze UI, and release guidance;
- `NOTICE.md` modified-work and exact upstream provenance record;
- `.github/CODEOWNERS` review ownership;
- `docs/ARCHITECTURE.md` runtime, source, privacy, user/profile, Glaze UI, build, and release architecture;
- `docs/GLAZE-UI.md` Gallery-specific Glaze UI implementation and release-review contract;
- `docs/REPOSITORY-READINESS.md` readiness classification and open stable-release blockers;
- `docs/RELEASE-EVIDENCE-TEMPLATE.md` comprehensive candidate/stable evidence record;
- `scripts/validate-repository-structure.sh` fail-closed repository layout/governance validation;
- `scripts/write-build-evidence.sh` machine-readable non-secret build identity and patch-line provenance;
- gc.8 GoreeCloud-owned behavioral-test foundation for enforced thumbnail presentation policy.

### Changed

- pull-request CI now checks out and verifies the exact pull-request head SHA rather than relying on GitHub's synthetic merge checkout;
- push/manual acceptance validates the exact `github.sha`;
- signed-candidate CI verifies the exact dispatched revision;
- repository security guardrails reject unapproved write permissions, `pull_request_target`, automatic signed-candidate triggers, missing checkout credential hardening, mutable third-party Action refs, and common committed key/certificate container files;
- ordinary and signed-candidate CI now fail closed unless JUnit XML proves the required GoreeCloud behavioral test class executed with at least three tests and zero failures/errors;
- CI retains Gradle test/lint console evidence, JUnit XML/results, packaged APK validator output, and machine-readable build evidence;
- build evidence schema v2 records the exact validated checkout commit and maintained GoreeCloud patch line separately from the packaged application version;
- build/release and stable-checklist documentation includes Android OS user/profile isolation as Gallery's application-appropriate multi-user boundary;
- pull-request review requirements cover repository structure/security, exact-revision validation, Android user/profile isolation, signing boundary, Glaze UI, release evidence, and stable-classification integrity;
- the acceptance workflow uses a version-independent name so its GitHub check identity does not become stale as the maintained patch line evolves.

### Validated

- exact-head PR validation reconstructed pinned Fossify Gallery and Commons source plus gc.1-gc.8, passed repository/security/source checks, executed `GoreeCloudGalleryPolicyTest` with 3 tests, 0 failures, and 0 errors, completed Android lint, built and validated the `1.0.0-gc.7` FOSS acceptance APK, and retained checksum, licensing, test, lint, APK, and build-identity evidence.

### Release boundary

These changes do not promote `1.0.0-gc.7` to Stable. Meaningful GoreeCloud-owned behavioral test coverage is now present and validated, but long-lived signing, protected repository branch policy, broader real-device destructive-operation/storage/user-profile/accessibility/upgrade-recovery acceptance, and final Stable evidence remain separately tracked blockers.

## 1.0.0-gc.7 — Acceptance candidate

### Changed

- corrected the remaining toolbar overflow popup defect at its real owner path in Fossify Commons `MySearchMenu` and embedded `MaterialToolbar`;
- ensured the overflow popup receives the intended GoreeCloud light/dark presentation during construction and search-bar color refresh;
- preserved rounded Gallery media presentation and previously accepted Glaze UI refinements.

### Validated

- real-device screenshots confirmed the previously unreadable top-bar overflow menus are readable on both the main folders view and an opened media folder;
- repository CI reconstructed the exact pinned Fossify Gallery and Commons source, applied the accepted GoreeCloud patch chain, ran source invariants and Android validation, built the FOSS acceptance APK, verified package identity/version/offline boundary/signature/notice, scanned packaged DEX for removed warning text, generated SHA-256, and retained acceptance evidence.

### Known release limitations

- the acceptance APK is not the long-lived stable signing baseline;
- full stable-release real-device permission, destructive-operation, Android user/profile, accessibility, upgrade/recovery, and signing evidence is incomplete;
- repository-level `main` protection remains a separate administrative gate.

## Historical gc.1 through gc.7 transformation line

The gc.1-gc.7 development increments established the dedicated GoreeCloud package identity, branding, offline boundary, Glaze UI palette and surfaces, launcher behavior, removal of inappropriate upstream counterfeit-build messaging from the GoreeCloud build, rounded settings/dialog/media presentation, no-square-thumbnail product behavior, navigation-resource corrections, and the accepted toolbar overflow correction.

The gc.8 repository/testability increment preserves packaged behavior while adding GoreeCloud-owned policy tests and stronger release evidence. The maintained transformation programs are preserved under `patches/gc1/` through `patches/gc8/` and validated during source reconstruction.
