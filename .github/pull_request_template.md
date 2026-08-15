## Summary

Describe what changed and why.

## Scope

- [ ] Change is limited to the intended GoreeCloud Gallery behavior, build, validation, documentation, or release process.
- [ ] Exact upstream assumptions remain pinned or any upstream change is explicitly documented.
- [ ] Accepted historical gc patch provenance was not rewritten merely to introduce new behavior.
- [ ] No reusable secret, credential, private key, keystore, token, password, or personal media is included.

## Repository and source validation

- [ ] `scripts/validate-repository-structure.sh` succeeds.
- [ ] `scripts/validate-repository-security.sh` succeeds.
- [ ] Exact pull-request revision is the revision being validated.
- [ ] Patch/source reconstruction succeeds.
- [ ] `scripts/validate-source-invariants.sh` succeeds.

## Android validation

- [ ] Android unit-test task was executed and any `NO-SOURCE` result is acknowledged as lack of behavioral unit-test coverage.
- [ ] GoreeCloud-owned automated tests were added or updated when the changed behavior is maintainably testable.
- [ ] Android lint completed and the report was reviewed when relevant.
- [ ] APK build completed when application behavior changed.
- [ ] `scripts/validate-apk.sh` completed when an APK was produced.
- [ ] Build and APK validation evidence was retained when an APK was produced.
- [ ] Real-device acceptance was completed when the change affects UI, storage permissions, media handling, user/profile isolation, or destructive file operations.

## Privacy and security

- [ ] `android.permission.INTERNET` remains absent unless a separately approved product decision explicitly changes the offline boundary.
- [ ] No analytics, advertising, tracking, telemetry, cloud-account, or remote-API dependency was introduced.
- [ ] Android user/profile and application-sandbox isolation was not weakened.
- [ ] Destructive-operation changes were tested only with disposable media before approval.
- [ ] GitHub Actions permissions remain least privilege and third-party Actions are pinned to reviewed commit SHAs.
- [ ] Checkout credentials remain non-persistent.
- [ ] Signing material remains outside source control and ordinary documentation.

## Glaze UI

- [ ] UI changes follow the GoreeCloud Glaze UI design language.
- [ ] Rounded geometry, layered surfaces, and GoreeCloud identity remain consistent with the Gallery UI contract.
- [ ] Light and dark states remain readable.
- [ ] Accessibility impact was considered and manually checked where practical.
- [ ] Decorative effects do not reduce contrast, performance, or task completion.

## License and provenance

- [ ] Upstream GNU GPL v3 requirements remain preserved.
- [ ] `NOTICE.md` and GoreeCloud modification/provenance remain accurate.
- [ ] Exact upstream revisions remain documented.
- [ ] Source corresponding to any distributed artifact remains reconstructable.

## Stable release impact

- [ ] `docs/STABLE-RELEASE-CHECKLIST.md` was reviewed for any gate affected by this change.
- [ ] `docs/RELEASE-EVIDENCE-TEMPLATE.md` was reviewed when release evidence changed.
- [ ] Release-signing behavior was not weakened.
- [ ] The change does not describe an acceptance candidate as Stable without all blocking evidence and explicit approval.
