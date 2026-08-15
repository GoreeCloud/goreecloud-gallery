# GoreeCloud Gallery Repository Readiness

## Purpose

This document records the source-controlled readiness model for GoreeCloud Gallery. It distinguishes repository readiness from stable-release readiness and prevents a green build from being interpreted as production approval.

## Current state

- Product: GoreeCloud Gallery
- Current acceptance line: `1.0.0-gc.7`
- Application ID: `com.goreecloud.gallery`
- Repository: `GoreeCloud/goreecloud-gallery`
- Development model: GoreeCloud-maintained open-source Android application based on Fossify Gallery
- License boundary: GNU GPL v3
- User-facing design language: Glaze UI
- Runtime model: offline-first local Android media application
- Release state: Acceptance Candidate
- Stable release: Not approved

## Repository readiness gates

The repository is expected to retain all of these controls:

### Structure and documentation

- [x] README project entry point.
- [x] Architecture record.
- [x] Build and release model.
- [x] Release-signing boundary.
- [x] Stable-release checklist.
- [x] Release-evidence template.
- [x] Security policy.
- [x] Contribution guide.
- [x] Modification/upstream provenance notice.
- [x] CODEOWNERS review ownership.
- [x] Pull-request template.
- [x] Dependabot configuration for GitHub Actions.
- [x] Repository structure validator.

### Source provenance

- [x] Exact Fossify Gallery source revision pinned.
- [x] Exact Fossify Commons source revision pinned.
- [x] Historical gc.1 through gc.7 transformation programs preserved.
- [x] Historical patch blob identities validated before execution.
- [x] Source reconstruction refuses ambiguous pre-existing working trees.
- [x] `git diff --check` required on reconstructed source.

### CI and supply-chain controls

- [x] Explicit Ubuntu 24.04 runner.
- [x] Top-level read-only GitHub Actions permissions.
- [x] Third-party Actions pinned to full reviewed commit SHAs.
- [x] Checkout credentials explicitly non-persistent.
- [x] Pull-request validation checks out the exact pull-request head SHA rather than relying on the synthetic merge checkout.
- [x] Push/manual validation checks out the exact GitHub SHA.
- [x] Repository structure validation runs before Android build work.
- [x] Repository security validation runs before Android build work.
- [x] Acceptance workflow has a finite timeout.
- [x] Signed-candidate workflow is manual and bound to `stable-release`.
- [x] Signed-candidate workflow does not publish Stable automatically.

### Packaged-artifact controls

- [x] Application ID validation.
- [x] Version validation.
- [x] Debuggable-state validation when expected.
- [x] `android.permission.INTERNET` absence validation.
- [x] APK signature validation.
- [x] Approved certificate-fingerprint validation for signed candidates.
- [x] GoreeCloud notice validation.
- [x] Removed counterfeit-warning DEX validation.
- [x] Final APK SHA-256 generation.
- [x] Build metadata/evidence generation.
- [x] Explicit unit-test `NO-SOURCE` interpretation retained as evidence.

## GoreeCloud production-readiness gates

Repository readiness does not waive the GoreeCloud production gates.

### Multi-user and private-data boundary

Gallery is a local Android client, so its application-appropriate multi-user model is Android OS user/profile isolation, the Android application sandbox, and permission-authorized media/storage access. It must not deliberately cross or merge private Android user/profile data boundaries.

Production evidence should include a secondary-user or managed/work-profile check when a representative device supports it.

### Security and safe operation

Security is a lifecycle requirement. Permission-sensitive and destructive media operations require controlled real-device testing with disposable copied media. A passing compiler/build does not prove safe file behavior.

### Glaze UI

Glaze UI is mandatory for Gallery's controlled user-facing surfaces. Current gc.7 acceptance has validated the previously defective toolbar overflow presentation, but stable acceptance still includes broader light/dark, dialog, destructive-action, accessibility, and responsive/device review.

## Stable-release blockers

The following work remains intentionally separate from repository-source readiness:

- **Issue #2 — Android stable signing:** create the long-lived release keystore, maintain independent protected recovery copies, configure the `stable-release` environment secrets, and successfully produce a fingerprint-verified signed candidate.
- **Issue #3 — main branch protection:** enforce repository-level pull-request and acceptance-CI protection so normal direct pushes cannot bypass the validated merge path. The connected automation used for source maintenance does not currently expose branch-protection/ruleset mutation, so this remains an explicit repository-administration task rather than being silently assumed complete.
- **Issue #4 — real-device stable acceptance:** complete storage/permission, copy/move/delete/trash/edit, accessibility, Glaze UI, upgrade, and recovery acceptance using disposable media.
- **Issue #5 — GoreeCloud-owned behavioral tests:** add meaningful automated regression coverage where maintainable, and clearly distinguish executed tests from upstream `NO-SOURCE` tasks.

Stable must remain blocked while any applicable signing, data-loss, permission, privacy, accessibility, upgrade/recovery, licensing, or evidence requirement is incomplete.

## Readiness interpretation

Use these classifications consistently:

- **Repository ready:** source, governance, build, and validation infrastructure are sufficiently structured for controlled ongoing development.
- **Acceptance candidate:** an installable candidate has passed the applicable automated acceptance path but still has blocking release gates.
- **Signed release candidate:** the candidate has passed protected long-lived signing validation but still requires final manual stable acceptance.
- **Stable:** all blocking release evidence is complete and publication has been deliberately approved.

The current gc.7 line must continue to be described as an acceptance candidate until the remaining stable-release gates are actually satisfied.