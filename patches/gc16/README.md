# gc.16 — Compact Glaze dialog geometry

This patch line is intentionally narrow. It fixes the representative-device dialog geometry regression observed after gc.15 by making sorting, grouping, and media-filter ScrollViews content-sized (`wrap_content`) while retaining overflow scrolling when the available viewport is constrained.

The patch does not change sorting, grouping, filtering, file operations, permissions, confirmation semantics, privacy boundaries, or network behavior. Semantic version remains `1.0.0`; Android `versionCode` advances to `10016` for installable acceptance testing.
