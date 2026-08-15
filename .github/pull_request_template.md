## Summary

Describe what changed and why.

## Scope

- [ ] Change is limited to the intended GoreeCloud Gallery behavior, build, validation, documentation, or release process.
- [ ] Exact upstream assumptions remain pinned or any upstream change is explicitly documented.
- [ ] No reusable secret, credential, private key, keystore, token, or password is included.

## Validation

- [ ] Patch/source reconstruction succeeds.
- [ ] `scripts/validate-source-invariants.sh` succeeds.
- [ ] Android unit-test task was executed and any `NO-SOURCE` result is acknowledged as lack of unit-test coverage.
- [ ] Android lint completed and the report was reviewed when relevant.
- [ ] APK build completed when application behavior changed.
- [ ] `scripts/validate-apk.sh` completed when an APK was produced.
- [ ] Real-device acceptance was completed when the change affects UI, storage permissions, media handling, or destructive file operations.

## Privacy and security

- [ ] `android.permission.INTERNET` remains absent unless a separately approved product decision explicitly changes the offline boundary.
- [ ] No analytics, advertising, tracking, telemetry, cloud-account, or remote-API dependency was introduced.
- [ ] Destructive-operation changes were tested only with disposable media before approval.
- [ ] GitHub Actions permissions remain least privilege and third-party actions are pinned to reviewed commit SHAs.

## Glaze UI

- [ ] UI changes follow the GoreeCloud Glaze UI design language.
- [ ] Light and dark states remain readable.
- [ ] Accessibility impact was considered and manually checked where practical.

## License and provenance

- [ ] Upstream GNU GPL v3 requirements remain preserved.
- [ ] GoreeCloud notice/provenance remains accurate.
- [ ] Source corresponding to any distributed artifact remains reconstructable.

## Stable release impact

- [ ] `docs/STABLE-RELEASE-CHECKLIST.md` was reviewed for any gate affected by this change.
- [ ] Release-signing behavior was not weakened.
