# GoreeCloud Gallery Features

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
- Glaze UI source-token mapping, adaptive gutters, light/dark presentation, and accessible control sizing foundations.

## Development work still required

- Mature album browsing and richer thumbnail/grid layouts.
- Full-resolution image viewing and native video playback.
- Editing, share/export, favorites, metadata editing, and approved destructive workflows.
- Hidden/excluded media and sensitive-media policy.
- Complete Privacy Shield, Wardveil, and Everkeep acceptance where applicable.
- TalkBack, switch access, large-text, contrast, adaptive-layout, tablet/foldable, and representative-device acceptance.
- Signed release packaging, upgrade/recovery acceptance, and Stable qualification.

## Product direction, not current implementation claims

- Optional user-controlled GoreeCloud Photos integration behind explicit adapters.
- Rich local organization/search experiences.
- Continuity and recovery features governed by Everkeep.
- Security-sensitive media workflows governed by Wardveil.

The preserved Fossify reconstruction line remains transitional provenance and regression reference; it is not the long-term feature authority for the native GoreeCloud Gallery product.
