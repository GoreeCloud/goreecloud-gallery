# Gallery Glaze transient notice host

Status: Development

This slice introduces `GalleryTransientNoticeHost`, a Gallery-owned non-modal feedback surface intended to replace app-generated Android Toast UI.

The host renders short-lived feedback inside the existing root `FrameLayout`, uses the Gallery Glaze spacing/radius vocabulary, keeps a 48 dp minimum height, announces messages through a polite accessibility live region, and guarantees only one active notice at a time.

`GalleryTransientNoticePolicy` now provides a pure bounded presentation contract before text reaches the host. Notice text is trimmed, repeated whitespace is collapsed, blank input is ignored, and output is capped at 180 Unicode code points without splitting a code point. The display duration remains a bounded 2400 ms. Focused JVM coverage protects whitespace normalization and Unicode-safe truncation.

## Authority boundary

The host and policy own presentation only. They do not change MediaStore access, Favorites persistence, settings persistence, JSON import/export, sharing intents, or any other Gallery authority.

## Remaining integration step

The current `GalleryActivity` still contains existing Android Toast call sites. Those call sites should be migrated to this host in a dedicated source rewrite with build and device evidence rather than mixing a large activity rewrite into this older reusable-host branch while the active viewer-orientation branch also modifies `GalleryActivity`.

This is Development evidence only and is not Stable/device acceptance. The separate landscape-orientation issue still requires representative-device verification before closure.
