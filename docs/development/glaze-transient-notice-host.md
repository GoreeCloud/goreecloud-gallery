# Gallery Glaze transient notice host

Status: Development — Glaze UI 2.2 Adoption Candidate

This slice advances Gallery's repository-local native Glaze authority to the current **Glaze UI 2.2.0 Stable** baseline while keeping the existing Gallery product structure and media-authority boundaries intact. The exact Stable promotion head is `fb5ecde4a8258503789ffde08ac46a2e524ef71e`, the Stable release merge is `6731098b28dd0393faa878c70d989a221d714a20`, and the reviewed tag is `v2.2.0`.

`GalleryGlazeContract` now records 2.2.0 as the required Stable target, retains the 48 dp normal interaction floor, adds the 56 dp Touch Assistance floor, and records the ordinary 2.2 System Glaze budget of one dominant Glaze panel plus at most three small floating Glaze controls with no nested backdrop blur. These values are application mapping/evidence inputs; they do not by themselves establish rendered conformance.

This branch's `GalleryTransientNoticeHost` is a bounded native mapping of the Glaze UI 2.2 **GlzToast** standard variant for application-owned confirmation/status feedback. It uses a 52 dp minimum visual height, 20 dp radius, 16 dp horizontal padding, concise 14 sp text, a polite accessibility live region, one active notice at a time, and a bounded 2400 ms timeout. The host remains non-modal and does not steal focus. Its `WRAP_CONTENT` layout may grow vertically rather than clipping longer/localized text.

`GalleryTransientNoticePolicy` provides a pure bounded presentation contract before text reaches the host. Notice text is trimmed, repeated whitespace is collapsed, blank input is ignored, and output is capped at 180 Unicode code points without splitting a code point. Focused JVM coverage protects whitespace normalization and Unicode-safe truncation.

The host is an application-owned transient overlay component; it is **not** a system-level Control Center, Universal Search surface, modal critical warning, or source of media/security/privacy truth. Critical decisions or destructive warnings must remain on persistent/explicit surfaces rather than being demoted into transient notices.

## Authority boundary

The host, policy, and Glaze contract own presentation only. They do not change MediaStore access, Favorites persistence, settings persistence, JSON import/export, sharing intents, Android permission authority, Wardveil/Privacy Shield/Identity/Everkeep state, or any other Gallery capability.

## Remaining integration step

The current `GalleryActivity` still contains existing Android Toast call sites. Those call sites should be migrated to this host in a dedicated source reconciliation with build and device evidence rather than mixing a large Activity rewrite into this reusable-host branch while the active viewer-orientation branch also modifies `GalleryActivity`.

Gallery has not yet earned complete 2.2 consumer conformance. Reduced Transparency/solid fallback, Increased Contrast, platform forced-color/native-equivalent handling, Touch Assistance preference wiring, 200% text/device acceptance, RTL/localization review, complete component/state mapping, System Glaze budget review across the full Activity, Human Visual Excellence, representative-device acceptance, and production/release qualification remain separate gates.

The separate landscape-orientation issue #47 also still requires representative Moto G 2026 verification before closure. This branch remains Draft / Development.
