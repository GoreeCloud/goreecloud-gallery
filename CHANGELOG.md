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
- `docs/REPOSITORY-READINESS.md` readiness classification and open stable-release blockers;
- `docs/RELEASE-EVIDENCE-TEMPLATE.md` comprehensive candidate/stable evidence record;
- `scripts/validate-repository-structure.sh` fail-closed repository layout/governance validation;
- `scripts/write-build-evidence.sh` machine-readable non-secret build identity.

### Changed

- pull-request CI now checks out and verifies the exact pull-request head SHA rather than relying on GitHub's synthetic merge checkout;
- push/manual acceptance validates the exact `github.sha`;
- signed-candidate CI verifies the exact dispatched revision;
- repository security guardrails now reject unapproved write permissions, `pull_request_target`, automatic signed-candidate triggers, missing checkout credential hardening, mutable third-party Action refs, and common committed key/certificate container files;
- CI retains Gradle test/lint console evidence and explicitly records `NO-SOURCE` as lack of behavioral unit-test coverage;
- CI retains packaged APK validator output and machine-readable build evidence;
- build/release and stable-checklist documentation now includes Android OS user/profile isolation as Gallery's application-appropriate multi-user boundary;
- pull-request review requirements now cover repository structure/security, exact-revision validation, Android user/profile isolation, signing boundary, Glaze UI, release evidence, and stable-classification integrity.

### Release boundary

These changes do not promote `1.0.0-gc.7` to Stable. Long-lived signing, protected repository branch policy, broader real-device destructive-operation/storage/accessibility/upgrade-recovery acceptance, and meaningful GoreeCloud-owned behavioral test coverage remain separately tracked blockers.

## 1.0.0-gc.7 — Acceptance candidate

### Changed

- corrected the remaining toolbar overflow popup defect at its real owner path in Fossify Commons `MySearchMenu` and embedded `MaterialToolbar`;
- ensured the overflow popup receives the intended GoreeCloud light/dark presentation during construction and search-bar color refresh;
- preserved rounded Gallery media presentation and previously accepted Glaze UI refinements.

### Validated

- real-device screenshots confirmed the previously unreadable top-bar overflow menus are readable on both the main folders view and an opened media folder;
- repository CI reconstructed the exact pinned Fossify Gallery and Commons source, applied the gc.1-gc.7 patch chain, ran source invariants, Android test task/lint, built the FOSS acceptance APK, verified package identity/version/offline boundary/signature/notice, scanned packaged DEX for removed warning text, generated SHA-256, and retained the acceptance artifact.

### Known release limitations

- the upstream unit-test task reports `NO-SOURCE`, so a green task is not behavioral unit-test coverage;
- the acceptance APK is not the long-lived stable signing baseline;
- full stable-release real-device, permission, destructive-operation, accessibility, upgrade/recovery, and signing evidence is incomplete.

## Historical gc.1 through gc.6 transformation line

The gc.1-gc.6 development increments established the dedicated GoreeCloud package identity, branding, offline boundary, Glaze UI palette and surfaces, launcher behavior, removal of inappropriate upstream counterfeit-build messaging from the GoreeCloud build, rounded settings/dialog/media presentation, no-square-thumbnail product behavior, navigation-resource corrections, and the first toolbar overflow correction attempt.

The exact accepted transformation programs are preserved under `patches/gc1/` through `patches/gc7/` and are verified by Git blob identity before source reconstruction.