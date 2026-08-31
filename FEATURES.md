# GoreeCloud Gallery Features

## Product identity and restoration target

GoreeCloud Gallery is an original GoreeCloud-owned native Android application whose established product experience is heavily inspired by Samsung Gallery. The native replacement must preserve the useful mature GoreeCloud Gallery feature model instead of narrowing the product into a minimal photo-grid application.

Historical GoreeCloud Gallery screenshots, prior Gallery behavior, repository history, and applicable Samsung Gallery interaction references are migration and visual-comparison inputs. They are not authorization to copy Samsung proprietary source code, assets, trademarks, or implementation details.

The target is to recover the established GoreeCloud Gallery information architecture, browsing model, album behavior, viewer interactions, contextual actions, organization patterns, and first-party feature breadth, then revamp GoreeCloud-controlled presentation with the current Stable Glaze UI 2.1 contract.

## Implemented in the first-party Development line

- Android-authorized local image/video access with fail-closed permission gating.
- Bounded MediaStore image/video reads through the compiled Android adapter.
- Validated media-item and MediaStore-row domain models.
- Local thumbnails with bounded in-memory caching and no cloud dependency.
- All / Images / Videos filtering over the current authorized snapshot.
- Newest / Oldest sorting over that same snapshot with deterministic tie order.
- Bounded local image previews and video poster previews.
- Previous / Next preview navigation constrained to the currently presented authorized snapshot.
- Permission and load-generation re-checks before preview rendering.
- Framework-independent album/trash/recovery/mutation foundations used by later native milestones.
- Adaptive photo-first grid presentation in the current Glaze UI 2.1 Development candidate.
- Glaze UI source mapping, adaptive gutters, light/dark presentation, and accessible control sizing foundations.

## Established Gallery capabilities to restore in the native replacement

The exact migration set is governed by historical GoreeCloud Gallery behavior and screenshots, but the restoration program includes the mature Gallery areas below wherever they were part of the established product or are required to complete the intended Samsung Gallery-inspired experience:

- Pictures/media browsing with dense chronological thumbnail presentation and date grouping.
- Albums browsing with album covers, counts, ordering, creation, rename, move/copy organization, and appropriate album actions.
- Search across local media and albums using locally available metadata where authorized.
- Favorites and favorite filtering/collections.
- Selection and multi-select with contextual bulk actions.
- Full media viewer with swipe/previous/next navigation and appropriate viewer chrome.
- Native image viewing at useful/full resolution and native video playback.
- Share/export and approved Android handoff workflows.
- Edit entry points and approved first-party editing workflows.
- Delete/trash/recovery flows with explicit destructive-action authorization.
- Move/copy/organize actions through Android-supported media boundaries.
- Details/metadata presentation and approved metadata-editing workflows.
- Slideshow and other established local presentation actions where supported by the historical Gallery product.
- Hidden/excluded album or media controls and sensitive-media policy governed by Privacy Shield.
- Contextual overflow menus and action surfaces appropriate to the current browsing/viewer state.
- Settings/preferences needed to support Gallery behavior without surfacing meaningless controls.
- Any additional established first-party Gallery capability evidenced by the historical GoreeCloud Gallery screenshots or prior accepted product behavior.

## Development work still required

- Restore the mature Samsung Gallery-inspired Pictures/Albums information architecture instead of relying on a single simplified browse surface.
- Restore the established feature set above in bounded, testable native milestones.
- Complete full-resolution image viewing and native video playback.
- Complete editing, share/export, favorites, metadata, selection/multi-select, and approved destructive workflows.
- Complete hidden/excluded media and sensitive-media policy.
- Complete Privacy Shield, Wardveil, Everkeep, GoreeCloud Identity, and GoreeCloud Mesh integration where applicable and evidence-backed.
- Complete TalkBack, switch access, large-text, contrast, reduced-motion/transparency, adaptive-layout, tablet/foldable, and representative-device acceptance.
- Complete signed release packaging, upgrade/recovery acceptance, and Stable qualification.

## Glaze UI modernization requirement

Glaze UI modernization must improve hierarchy, navigation, material, responsive behavior, motion, accessibility, transient surfaces, and visual polish without deleting established Gallery capabilities merely to simplify the interface. Media remains dominant content; interaction chrome may use Glaze material selectively and must preserve Android-native behavior, performance, readability, and accessibility.

A visually polished replacement that omits mature Gallery capabilities is not a successful migration.

## Product direction, not current implementation claims

- Optional user-controlled GoreeCloud Photos integration behind explicit adapters.
- Richer local organization/search experiences that remain device-local by default.
- Continuity and recovery features governed by Everkeep.
- Security-sensitive media workflows governed by Wardveil.

The preserved Fossify reconstruction line remains transitional provenance and regression reference; it is not the long-term implementation authority for the native GoreeCloud Gallery product. Historical GoreeCloud Gallery behavior remains important migration evidence for intended product capabilities and interaction expectations.
