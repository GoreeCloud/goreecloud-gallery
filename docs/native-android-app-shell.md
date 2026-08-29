# Native Android Gallery application shell

## Development capability

This milestone adds the first compiled first-party GoreeCloud Gallery Android application module under `native/app`.

The application uses the established `com.goreecloud.gallery` package identity and consumes the already-compiled `android-adapter` MediaStore bridge. It does not reuse the transitional Fossify application architecture or UI.

## Permission and local-media authority

The shell does not query MediaStore before Android grants readable media access.

- Android 13+ requests image/video media permissions.
- Android 14+ also declares selected-media access so the operating system may expose only the media the user authorizes.
- Android 12L and earlier use the platform read-storage permission within the supported API range.
- A denied or unavailable provider read is presented as unavailable; the application does not replace provider failure with a false empty-library result.

The current shell reads at most 100 recent authorized rows per refresh through `AndroidMediaStoreReader`. It renders local metadata only and introduces no network permission, cloud dependency, account requirement, analytics, remote font, remote icon, or remote UI resource.

## Glaze UI 2.0 source contract

The shell targets Glaze UI 2.0.0, the current Stable design-system baseline. The first source-level mapping intentionally uses native Android controls and platform theme semantics while preserving Gallery's media-first composition.

The repository-local contract currently enforces:

- Glaze UI version `2.0.0`;
- a 48dp general interactive-target floor;
- adaptive horizontal gutters for phone, tablet, and larger resizable widths;
- native light/dark theme variants;
- local Canvas/Surface-style composition using platform semantic colors;
- no animation dependency for task completion; and
- no network-delivered presentation resources.

This is source integration, not rendered acceptance. Accessibility, large-text reflow, representative phone/tablet/foldable behavior, visual hierarchy, contrast, focus behavior, and physical-device review remain required.

## Current UI scope

The application presents:

- a local-library heading and explicit Development/Glaze source target;
- media-permission state;
- a native permission action or local refresh action;
- authoritative provider-failure messaging; and
- a bounded newest-first list of authorized image/video metadata including display name, kind, optional album, timestamp, and size.

No thumbnail grid, album navigation, viewer, editor, share flow, delete/move flow, hidden-media policy, Photos integration, or cloud library is implemented by this shell.

## Security, privacy, and continuity boundary

Privacy Shield remains authoritative for media permission, consent, minimization, and user control. This shell relies on Android permission enforcement and does not attempt to bypass Android user/profile isolation.

Wardveil Security remains authoritative for future media/file trust, protection, validation, and security-state presentation. This shell does not claim that local images or videos have been malware-scanned or content-safety classified.

Everkeep remains authoritative for applicable recovery, preservation, portability, and continuity. This shell does not enable Android backup and does not claim backup/restore acceptance.

## Acceptance boundary

This milestone is a Development source and APK-build target only. It does not establish runtime permission acceptance on a device, MediaStore behavior across supported OEMs/profiles, rendered Glaze UI acceptance, accessibility acceptance, thumbnail performance, media mutations, release signing, upgrade/recovery evidence, production platform-system acceptance, release, or Stable qualification.
