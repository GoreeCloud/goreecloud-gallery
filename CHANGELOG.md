# GoreeCloud Gallery Changelog

This changelog records material source, build, validation, release-engineering, and product-readiness changes in the dedicated `GoreeCloud/goreecloud-gallery` repository. Historical development that occurred in the temporary website-repository build carrier remains preserved in Git and in the GoreeCloud patch provenance records.

## 1.0.0 — Stable candidate

### Added

- gc.9 final package-identity patch with `VERSION_NAME=1.0.0` and Android `VERSION_CODE=10009`;
- non-secret Stable signing execution runbook;
- representative-device acceptance runbook;
- authoritative `docs/STABLE-CANDIDATE-1.0.0.md` promotion contract;
- gc.10 Settings cleanup and Android-native Privacy & permissions access;
- gc.11 native Glaze UI 1.0 semantic resources, adaptive Settings composition, practical target sizing, and fail-closed conformance checks;
- gc.12 Glaze UI browsing-surface integration for the folders screen and opened-folder media grid.

### Changed

- ordinary acceptance CI builds and validates `GoreeCloud-Gallery-1.0.0.apk` while retaining `acceptance-candidate` classification;
- the protected manual signing workflow defaults to `1.0.0` while retaining `signed-release-candidate` classification;
- gc.10 removes misleading fixed-thumbnail controls and upstream purchase UI while preserving meaningful Gallery settings;
- gc.11 increments Android `VERSION_CODE` to `10011` while preserving semantic version `1.0.0`;
- gc.12 increments Android `VERSION_CODE` to `10012` while preserving semantic version `1.0.0`;
- Settings uses Glaze Canvas, gradient navigation emphasis, rounded Raised rows, Android ripple feedback, 48dp comfortable interactive targets, dedicated light/dark semantic palettes, and wider native insets at `sw600dp` and `sw840dp`;
- primary browsing surfaces now use the Glaze Canvas, branded menu chrome, adaptive 8/16/24dp media-aware gutters, Raised empty-state actions, comfortable action targets, and semantic loading accent without cardifying every thumbnail;
- Gallery records Glaze UI `1.0.0` and canonical reference revision `d6e446fd8ef251259d16368d50aad90d9287a774` as its native conformance target;
- repository/source validation requires the current Glaze patch line and fails closed if the semantic resource, Settings, or browsing-surface integration contract disappears;
- Stable promotion must reuse the exact accepted signed binary rather than rebuilding merely to alter release labeling.

### Validated

- gc.9 exact-head validation reconstructed pinned Fossify Gallery and Commons source, passed repository/security/source checks, executed `GoreeCloudGalleryPolicyTest`, passed Android lint, and assembled/validated the `1.0.0` FOSS acceptance APK;
- gc.10 exact-head and post-merge validation passed after Settings simplification and produced the updated acceptance APK from `main`;
- gc.11 exact-head run 31973570278 completed successfully on `14763702612f4c455102f3032e20eb204ea17cec`, including repository/security/source validation, GoreeCloud behavioral tests, Android lint, APK assembly, APK/evidence validation, and artifact upload;
- PR #15 was squash merged as `245a34753f4d83e3abe36b95bbb7be37d1eb9002` after the exact gc.11 head passed acceptance; the post-merge main run was started as run 31977767072;
- gc.12 requires its own exact-head acceptance run before merge because it changes primary Android browsing layouts and versionCode.

### Release boundary

`1.0.0` is **not yet Stable**. Long-lived signing and recovery, protected `stable-release` administration, repository-level `main` protection, representative-device permissions/file-operation/user-profile/accessibility/Glaze UI acceptance, same-signer upgrade/recovery testing, and final release evidence remain blocking.

## Repository readiness hardening — gc.8

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

- pull-request CI checks out and verifies the exact pull-request head SHA rather than relying on GitHub's synthetic merge checkout;
- push/manual acceptance validates the exact `github.sha`;
- signed-candidate CI verifies the exact dispatched revision;
- repository security guardrails reject unapproved write permissions, `pull_request_target`, automatic signed-candidate triggers, missing checkout credential hardening, mutable third-party Action refs, and common committed key/certificate container files;
- ordinary and signed-candidate CI fail closed unless JUnit XML proves the required GoreeCloud behavioral test class executed with at least three tests and zero failures/errors;
- CI retains Gradle test/lint console evidence, JUnit XML/results, packaged APK validator output, and machine-readable build evidence;
- build evidence schema v2 records the exact validated checkout commit and maintained GoreeCloud patch line separately from the packaged application version;
- build/release and stable-checklist documentation includes Android OS user/profile isolation as Gallery's application-appropriate multi-user boundary;
- the acceptance workflow uses a version-independent name so its GitHub check identity does not become stale as the maintained patch line evolves.

### Validated

- exact-head validation reconstructed pinned Fossify Gallery and Commons source plus gc.1-gc.8, passed repository/security/source checks, executed `GoreeCloudGalleryPolicyTest` with 3 tests, 0 failures, and 0 errors, completed Android lint, built and validated the `1.0.0-gc.7` FOSS acceptance APK, and retained checksum, licensing, test, lint, APK, and build-identity evidence.

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
