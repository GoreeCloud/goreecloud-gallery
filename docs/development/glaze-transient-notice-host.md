# Gallery Glaze transient notice host

Status: Development

This slice introduces `GalleryTransientNoticeHost`, a Gallery-owned non-modal feedback surface intended to replace app-generated Android Toast UI.

The host renders short-lived feedback inside the existing root `FrameLayout`, uses the Gallery Glaze spacing/radius vocabulary, keeps a 48 dp minimum height, announces messages through a polite accessibility live region, and guarantees only one active notice at a time.

## Authority boundary

The host owns presentation only. It does not change MediaStore access, Favorites persistence, settings persistence, JSON import/export, sharing intents, or any other Gallery authority.

## Remaining integration step

The current `GalleryActivity` still contains existing Android Toast call sites. Those call sites should be migrated to this host in a dedicated source rewrite with build and device evidence rather than mixing a large activity rewrite into the reusable-host introduction.

This is Development evidence only and is not Stable/device acceptance.
