# GoreeCloud Gallery Build and Release Model

## Purpose

This document defines the reproducible build, validation, acceptance, and release model for GoreeCloud Gallery.

GoreeCloud Gallery is a GoreeCloud-maintained fork based on Fossify Gallery. The installable application ID is `com.goreecloud.gallery`.

## Pinned upstream source

The gc.7 line is built from exact source revisions:

- Fossify Gallery 1.13.1: `b28299dc33821eee8d108a9880ce87876cf31443`
- Fossify Commons 6.1.5: `acfd352df1a1852d17a5f77def8b7ad6e522a5b6`

The workflow checks out those revisions in detached state and refuses to treat another revision as the accepted baseline.

## Migrated patch provenance

The original gc.1 through gc.7 patch scripts were developed on the isolated `build/goreecloud-gallery-apk` branch of `GoreeCloud/goreecloud-website`.

During the dedicated-repository migration, each historical script was preserved as ordered source fragments under `patches/gc1/` through `patches/gc7/`. `scripts/materialize-patches.sh` reconstructs each script and validates its Git blob SHA against the exact historical source blob before the script can execute.

This fail-closed provenance check prevents an incomplete migration, missing fragment, changed byte sequence, or accidental rewrite from silently becoming a build input.

Historical script blobs:

- gc.1 — `100b2080cf5e82275dce7a1a1f35d8869ab8af38`
- gc.2 — `5a9d84b0eaa49107cba52f2b1f02131fa5d03f3e`
- gc.3 — `67a4e4acfebb2c6e3d271d5387ededa60bc0ee87`
- gc.4 — `8f3a48d424ead1c253e0e9eb91f27706e6162757`
- gc.5 — `e8b1362e87d0e47997fa7f2ee36f851b4123fab5`
- gc.6 — `4c9094e7b4139e0472f1c17ab2ff4a2186244c78`
- gc.7 — `516339487492806932ec14b669c183e4919b1187`

## Patch history

The deterministic patch chain is cumulative:

1. **gc.1** — GoreeCloud package identity, branding, initial Glaze palette, launcher identity, offline boundary, and license notice.
2. **gc.2** — local Fossify Commons composite build, system-theme correction, launcher aliases, and maintained-fork identity fixes.
3. **gc.3** — real-device identity corrections, remaining Compose counterfeit-warning boundary, canonical launcher behavior, and Glaze app-bar surfaces.
4. **gc.4** — rounded Glaze Settings cards and popup/dialog geometry refinement.
5. **gc.5** — legacy non-Compose counterfeit-warning removal, popup contrast correction, rounded thumbnail defaults, API-qualified navigation-bar resources, and expanded readiness validation.
6. **gc.6** — complete removal of square-thumbnail controls from the GoreeCloud product surface, forced rounded folder/media thumbnails, and first toolbar overflow correction attempt.
7. **gc.7** — direct correction of the real owner path for the remaining overflow defect: Fossify Commons `MySearchMenu` and its embedded `MaterialToolbar`.

## Glaze UI contract

The Android implementation emphasizes rounded containers and media surfaces, layered light/dark surfaces, restrained depth, readable popup/dialog contrast, touch-friendly spacing, GoreeCloud-controlled product identity, and accessibility over decorative effects.

Square thumbnail presentation is intentionally not exposed. File and folder thumbnails resolve to the rounded GoreeCloud presentation even when imported or legacy preferences previously selected square behavior.

Toolbar overflow menus are also a Glaze-controlled surface. A light GoreeCloud screen must not display a dark inherited popup and popup foreground/background colors must remain readable.

## Offline and privacy boundary

GoreeCloud Gallery is intended to operate entirely against local Android media and storage APIs. The GoreeCloud patchset does not add analytics, advertising, tracking, cloud accounts, remote APIs, or `android.permission.INTERNET`.

The APK workflow verifies the absence of the Internet permission when `apkanalyzer` is available.

Android storage-management permissions are separate from network access and remain subject to real-device acceptance because file-management permissions are security-sensitive.

## CI gates

The dedicated GitHub Actions workflow performs:

- patch-fragment provenance reconstruction and exact Git-blob verification;
- exact upstream revision verification;
- deterministic gc.1 through gc.7 patch application;
- `git diff --check` for Gallery and Commons changes;
- counterfeit-warning source scans;
- forced rounded-thumbnail assertions;
- assertions that removed square-thumbnail controls stay absent;
- direct `MySearchMenu` popup-theme assertions;
- light/dark popup resource assertions;
- Android unit-test task execution;
- Android lint;
- FOSS debug APK compilation;
- package-ID verification when `apkanalyzer` is available;
- packaged Internet-permission verification;
- packaged DEX scanning for removed counterfeit-warning text;
- GoreeCloud notice verification;
- GNU GPL license preservation in the produced artifact bundle;
- SHA-256 generation;
- GitHub Actions artifact upload.

## Real-device acceptance

The gc.6 screenshots established that the no-square-thumbnail work and standard dialogs behaved as intended, but the top-bar three-dot menus still inherited a dark popup with unreadable dark foreground text.

gc.7 moved the correction into `MySearchMenu`, assigning a GoreeCloud light or dark popup theme directly to the embedded `MaterialToolbar` during construction and whenever search-bar colors refresh.

Real-device screenshots supplied after gc.7 confirm that the previously defective overflow menus are now readable on both the main folders view and an opened media folder. This closes that specific visual acceptance defect.

That acceptance does not by itself prove all file-management, storage-permission, accessibility, or release-signing requirements.

## Stable-release debt

Before stable promotion, complete at minimum:

- controlled long-lived Android release signing with secrets kept outside source control;
- targeted GoreeCloud-owned automated tests where practical;
- broader real-device validation of copy, move, delete, recycle bin, hidden/excluded items, favorites, video playback, editing, and destructive operations;
- storage-permission review;
- upgrade and rollback validation;
- accessibility review;
- final license/source attribution review;
- stable release evidence and checksum publication.

The current debug-signed gc.7 APK is an acceptance build, not a stable production release.
