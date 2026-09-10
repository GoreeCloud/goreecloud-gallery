# GoreeCloud Gallery — GLAZE UI V1.3 Application Contract

## Status

**Lifecycle:** Development  
**Required design system:** GLAZE UI V1.3 / 1.3.0 — Adaptive Resonance  
**Repository-local source mapping:** 1.3.0 on the current Development candidate  
**Application conformance:** Not established  
**Production eligibility:** Not established

This document defines the Gallery-specific application mapping and acceptance boundary for the current GoreeCloud design system. It supplements the authoritative GLAZE UI repository and GoreeCloud application-design governance. It does not grant Gallery conformance, Release Candidate status, production acceptance, or Stable status.

## Authoritative source anchor

The current Gallery source mapping is pinned to the authoritative `GoreeCloud/goreecloud-glaze-ui` revision:

`8354308445da9ac35ced2b37a7f503a08a0aaf72`

That revision identifies **GLAZE UI V1.3 / 1.3.0 — Adaptive Resonance** as the required current consumer baseline. Gallery must not silently follow a moving branch when recording application acceptance; acceptance evidence must remain bound to an exact Gallery revision and an exact Glaze authority revision.

The V1.3 repository retains some implementation-provenance artifacts under `.candidate` filenames. Those filenames do not downgrade the promoted V1.3 consumer baseline. Gallery uses them only as the governed implementation source behind the current semantic roles.

## Source-token mapping

`native/app/src/main/kotlin/com/goreecloud/gallery/GalleryGlazeContract.kt` is Gallery's compact repository-local Android mapping of the shared semantic contract.

The current mapping consumes these authoritative roles and inherited numeric baselines:

- **Spacing:** 2, 4, 8, 12, 16, 24, 32, and 48 dp from the shared spatial scale.
- **Shape:** quiet 10 dp, control 12 dp, container/soft 20 dp, rounded 24 dp, overlay 28 dp, and capsule/full-pill geometry.
- **Motion:** micro 160 ms, standard 240 ms, connected 360 ms, spatial 480 ms, reduced-standard 180 ms, and minimal 0 ms.
- **Interaction target:** 48 dp minimum for ordinary Android application controls represented by this contract.
- **Adaptive gutters:** 16, 24, 32, and 48 dp values from the governed spatial baseline.

Gallery's media-grid and album-grid item counts remain product-specific presentation decisions. They are not the same thing as the Glaze foundational layout-grid column tokens.

The Android width thresholds used to select Gallery's current gutter and media-density composition are also application/platform adapter heuristics. They must not be represented as universal Glaze breakpoints or device identities.

## Current source migration tranche

The current V1.3 source-mapping tranche deliberately changes real native presentation inputs rather than merely relabeling the design-system version:

- the native contract is re-pinned from the obsolete `1.0.0` source declaration to `1.3.0` with an exact Glaze authority revision;
- the widest Gallery gutter moves to the governed 48 dp spatial value;
- the bottom navigation surface consumes the V1.3 capsule shape role rather than a hand-selected near-pill radius;
- the bottom navigation margin consumes the 12 dp compact-cluster spacing role;
- the reserved navigation zone is adjusted to preserve separation after the margin change;
- the content bottom inset consumes the 32 dp section-spacing role;
- shared semantic spacing, shape, motion, and target-size roles are represented by named source constants and protected by unit tests.

These changes improve the source contract and reduce arbitrary geometry, but they are **not a complete whole-application migration**. Gallery still contains Gallery-controlled presentation values and Android resource styling outside this compact contract that require deliberate review against the V1.3 semantic system.

## Media-first hierarchy

Gallery is a media application. Glaze treatment must improve structure and usability without competing with the user's photos or videos.

Gallery should therefore prefer:

- media-dominant browsing surfaces;
- solid or readability-first content planes around primary media;
- restrained container and overlay treatment for controls, dialogs, contextual actions, settings, and Recycle Bin workflows;
- semantic rounded geometry rather than unrelated hand-selected radii;
- clear state and focus treatment that does not rely on shape, color, translucency, or motion as the sole signal;
- connected motion only where it preserves a meaningful relationship, such as thumbnail-to-detail transitions;
- no continuous decorative wobble, pulse, bounce, or restless animation.

Full-screen viewer chrome and editor controls should remain visually subordinate to media. Destructive actions must remain explicit and distinguishable without using aesthetic treatment to imply that Android or a GoreeCloud platform authority has approved an operation.

## Accessibility and resilience

V1.3 source mapping does not replace Gallery-specific accessibility acceptance.

Before Gallery can claim current design-system conformance, applicable testing must cover at least:

- TalkBack labels, role/state announcements, and logical traversal;
- keyboard and switch-access operation where applicable;
- visible focus and non-color state differentiation;
- 200% text scaling and increased Android display size;
- ordinary 48 dp target floors and any applicable larger assisted-target behavior;
- right-to-left layout and localization resilience;
- increased contrast and reduced-transparency behavior;
- reduced-motion and minimal-motion semantic equivalents;
- narrow phone, representative phone, tablet/large-window, rotation, and other applicable Android form factors;
- safe-area, system-bar, keyboard-occlusion, and platform-inset behavior;
- representative-device frame pacing and interaction stability.

Accessibility reflow may take precedence over density or multi-region presentation. Gallery must not shrink interaction targets merely to preserve a desired number of grid columns or controls.

## Appearance and material boundary

Gallery must not equate conformance with a matching color palette. Application acceptance must evaluate typography, spacing, geometry, hierarchy, state, contrast, motion, input behavior, and transient surfaces as a coherent system.

The application remains offline-first. A Glaze enhancement must not add remote fonts, remote icons, analytics, advertising, tracking pixels, network-hosted style resources, or another network dependency. Gallery's current no-unnecessary-network boundary remains authoritative over decorative effects.

Advanced material effects are optional when they would reduce readability, accessibility, performance, or platform consistency. Solid semantic fallback is preferable to an effect that cannot be rendered safely or consistently.

## Gallery-specific product invariants

The current migration must preserve established Gallery product behavior while modernizing the interface. In particular:

- Photos, Albums, Videos, Settings, Favorites, Recovery/Recycle Bin, viewer, selection, and editor capabilities must remain discoverable according to their implemented Development scope.
- Media grids must remain media-dominant rather than turning every thumbnail into a decorative card.
- Album surfaces must preserve meaningful covers, names, counts, and Android-authorized scope.
- Viewer navigation must stay within the current authorized/presented collection.
- Accessible Previous/Next alternatives must remain available alongside swipe navigation.
- Photo editing must retain the current non-destructive Save copy authority boundary unless a separately approved contract changes it.
- Trash, Restore, and permanent deletion must remain distinct and continue to rely on the applicable Android-owned authorization path.
- Unknown or unavailable Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, or Manager evidence must never be converted into a positive visual status.

## Historical design-system evidence

The repository contains historical Gallery Glaze work from earlier design-system generations, including prior 1.0 and 2.x-era mappings and transitional Fossify-era presentation evidence. Those records remain useful for regression analysis, provenance, restoration, and comparison, but they do not override the current required V1.3 / 1.3.0 consumer baseline.

Older screenshots and accepted Gallery presentation invariants remain valuable visual-comparison evidence. They do not make historical Glaze version labels current, and they do not authorize copying third-party proprietary assets or implementation details.

## Automated source evidence

The repository-local V1.3 source mapping must be protected by tests that verify at least:

- exact `1.3.0` source version;
- exact Glaze authority revision;
- governed semantic spacing values;
- governed semantic shape-role mapping;
- governed semantic motion values;
- 48 dp ordinary interaction floor;
- adaptive gutter outputs;
- product-specific media/album density remains explicit rather than being confused with the shared layout grid;
- navigation reserved space remains sufficient for the bottom control surface.

A passing source/build workflow proves only the checks executed on that exact Gallery revision. It does not establish visual quality, accessibility, device behavior, platform-system integration, recovery, signing, production readiness, or Stable qualification.

## Remaining V1.3 acceptance gates

Gallery remains `applicable-migration-required` and globally nonconformant until the applicable current-source work and acceptance evidence are complete. Remaining gates include:

- reconciliation of residual Gallery-controlled hard-coded presentation values and Android resources with V1.3 semantic roles;
- whole-application rendered review across browsing, albums, search, Favorites, viewer, editor, Settings, dialogs, selection, and Recycle Bin;
- interaction-state and connected-motion review;
- accessibility and large-text acceptance;
- adaptive/form-factor and rotation acceptance;
- representative physical-device/OEM/profile testing;
- Human Visual Excellence review;
- performance/frame-pacing review where affected;
- rollback evidence bound to exact source revisions;
- platform-system acceptance where applicable;
- protected signing/provenance and release evidence;
- explicit production approval and Stable qualification.

No source version string, manifest label, unit test, screenshot, or CI run may independently waive these gates.
