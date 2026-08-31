# GoreeCloud Gallery Features

## Product identity and restoration target

GoreeCloud Gallery is an original GoreeCloud-owned native Android application whose established product experience is heavily inspired by Samsung Gallery. The native replacement must preserve the useful mature GoreeCloud Gallery feature model instead of narrowing the product into a minimal photo-grid application.

Historical GoreeCloud Gallery screenshots, prior Gallery behavior, repository history, and applicable Samsung Gallery interaction references are migration and visual-comparison inputs. They are not authorization to copy Samsung proprietary source code, assets, trademarks, or implementation details.

The target is to recover the established GoreeCloud Gallery information architecture, browsing model, album behavior, viewer interactions, contextual actions, organization patterns, and first-party feature breadth, then revamp GoreeCloud-controlled presentation with the current Stable Glaze UI 2.1 contract.

## Implemented in the first-party Development line

- Android-authorized local image/video access with fail-closed permission gating and explicit selected-media/partial-access handling.
- Bounded MediaStore image/video reads through the compiled Android adapter.
- Validated media-item and MediaStore-row domain models.
- Local thumbnails with bounded in-memory caching and no cloud dependency.
- Direct Glaze UI 2.1 Photos / Albums / Videos navigation in the current `0.3.0-dev` candidate.
- Dense adaptive Photos and Videos grids grouped into Today / Yesterday / calendar-date sections.
- Newest / Oldest ordering over the current authorized snapshot.
- Local search over authorized display names and album names without an additional provider query.
- Dedicated Albums browsing with authoritative album covers, names, counts, adaptive cover layout, and bounded album-detail browsing.
- Device-local Favorites backed only by Gallery app-local state; favorite/unfavorite is available from the viewer and authorized Favorites appear as a dedicated collection.
- A full-screen bounded media viewer shell with Previous / Next navigation, restrained top chrome, and a bottom action surface.
- Android Share handoff for the currently authorized media content URI using read-only URI grant semantics.
- Viewer details for type, album, date, dimensions, duration, and size when available.
- Edit and Delete are intentionally unavailable in the `0.3.0-dev` viewer until their approved editing/mutation paths are implemented and validated.
- Video items currently use authorized poster thumbnails; native playback is not yet implemented.
- Permission and load-generation re-checks before viewer rendering.
- Framework-independent album/trash/recovery/mutation foundations used by later native milestones.
- Glaze UI 2.1 source mapping, adaptive gutters, light/dark presentation, floating navigation, and accessible control sizing foundations.

## Historical screenshot restoration requirements

The historical screenshots supplied for the native migration establish the following product requirements. These are restoration targets unless a later authoritative requirement intentionally supersedes them; they must not be represented as already implemented merely because they are documented here.

### Photos / primary library

- A media-dominant Photos surface with a dense multi-column thumbnail grid.
- Fast access to search, sorting/grouping, layout/view controls, and overflow actions.
- Clear navigation among the principal Gallery areas rather than exposing implementation/debug controls as the primary UI.
- Rounded media presentation and compact chrome so photos and videos remain the dominant content.

### Albums and folder browsing

- A dedicated Albums experience using meaningful cover thumbnails, album names, and item counts.
- Album/folder browsing that supports visually rich two-column or adaptive cover layouts where appropriate.
- Search folders/albums plus contextual creation, organization, sorting, and overflow actions.
- Album actions such as create, rename, reorder where supported, move/copy organization, hide/exclude policy, and details through Android-authorized boundaries.

### Photo and media viewer

- A full media viewer rather than a dialog-only preview.
- Edge-to-edge media presentation with restrained top chrome and a bottom action surface.
- Primary actions modeled around Send/Share, Favorite, Edit, Delete/Trash, and More/contextual actions.
- Swipe/previous/next navigation within the currently authorized and presented collection.
- Full-resolution image viewing and native video playback as separate implementation milestones.

### Grouping, sorting, and timeline views

- Chronological grouping by Today/date and other useful timeline units.
- User-selectable grouping and sorting rather than only one fixed newest-first grid.
- Timeline-oriented views capable of browsing media by capture date while retaining album/context identity.
- View-density/layout controls where they improve browsing without overwhelming the primary interface.

### Favorites

- A first-party Favorites collection/surface.
- Fast favorite/unfavorite action from the viewer and selection states.
- Favorites remain device-local by default and must not require GoreeCloud Photos or network access.

### Private Photos and hidden media

- Private/hidden media is an established Gallery concept that must be restored with a current security design rather than copied literally from the historical pattern-lock UI.
- Current implementation must use appropriate Android/GoreeCloud authentication and authorization boundaries, with GoreeCloud Identity where applicable and Privacy Shield governing consent, visibility, and user control.
- Authentication methods such as device credentials/biometrics may be used only through supported secure platform mechanisms; Gallery must not invent insecure credential storage merely to mimic the historical UI.
- Wardveil Security must govern applicable protection, validation, trust, and security-response responsibilities.

### Settings and recycle/trash behavior

- Gallery settings must include meaningful privacy/security, hidden-media, visible-action, organization, and recycle/trash controls where supported.
- Recycle Bin / Trash behavior is a first-party product expectation, with restore and permanent-delete flows clearly distinguished.
- Destructive operations must remain explicit and Android-authorized; historical UI is visual/behavioral migration evidence, not authority to bypass current Android safeguards.

### Navigation model

- The historical product used clear top-level destinations for media, albums, and video-oriented browsing. The current native implementation may modernize the exact tab labels and placement under Glaze UI 2.1, but it must preserve similarly direct access to the major Gallery domains.
- Search and contextual actions must be reachable from the relevant browsing surface without forcing users through debug-style filter controls.

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

- Continue the mature Samsung Gallery-inspired restoration beyond the current Photos / Albums / Videos shell and bounded viewer.
- Add selection and multi-select with contextual bulk actions.
- Add richer grouping modes, view-density/layout controls, album creation/rename/reorder, and approved move/copy organization.
- Complete useful/full-resolution image viewing and native video playback.
- Complete approved first-party editing and destructive Delete/Trash/Restore/Purge workflows; do not enable the current disabled viewer actions until those paths are validated.
- Expand Share/export acceptance beyond the current Android read-only share handoff where needed.
- Complete hidden/excluded media, Private Photos, and sensitive-media policy.
- Add meaningful Gallery Settings and Trash/Recycle Bin surfaces.
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
