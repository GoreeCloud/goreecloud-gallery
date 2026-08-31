# Authorized media multi-select foundation — Development

GoreeCloud Gallery now has a first-party core selection model for the next native selection and contextual-action restoration milestone.

Authority boundary:

- `AuthorizedMediaSelection` accepts only the current Android-authorized `MediaItem` snapshot supplied by the caller.
- Unknown item IDs fail closed and cannot enter selection state.
- Replacing the authorized snapshot immediately removes selections that are no longer authorized, including permission-scope reductions and refreshed MediaStore snapshots.
- Selected items are resolved again from the current authorized snapshot rather than retaining stale media objects.
- `selectedPresentedItems` additionally requires the selected item to remain in the explicitly presented collection and to match the current authorized content URI before a later contextual action may consume it.
- Duplicate IDs in an authorization snapshot are rejected rather than creating ambiguous selection identity.
- Selection state is in-memory only in this slice. No selected-media history, telemetry, account state, cloud synchronization, or new persistence is created.
- The model owns no MediaStore query, content mutation, share, delete/trash, restore, purge, move/copy, edit, upload, network, or Android permission authority.

Tests cover unauthorized IDs, deterministic toggle/clear, authorization-reduction pruning, authorized snapshot ordering, presented-subset revalidation, URI identity revalidation, and duplicate-ID rejection.

Status: **Development foundation only**. This does not yet render native selection UI or contextual bulk actions. Long-press/selection presentation, complete Glaze UI 2.1 interaction/accessibility acceptance, Android-authorized bulk action contracts, destructive-action safeguards, representative-device acceptance, signing, release, and Stable qualification remain separate gates.
