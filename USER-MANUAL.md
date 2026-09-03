# GoreeCloud Gallery User Manual

## Status

This manual describes the **current first-party native Development experience**, including the `0.6.1-dev` corrective Android-authorized Delete/Trash candidate. It does not describe a Stable or production-approved release.

The initial `0.6.0-dev` destructive-operation test build is **superseded for Delete/Recycle Bin testing**. Physical-device testing showed that it could render the Delete action but rejected its generic `MediaStore.Files` item URI before Android's system confirmation opened. `0.6.1-dev` corrects the media-item URI projection and requires a fresh representative-device retest before Delete/Trash may be considered physically accepted.

Use **disposable copied photos and videos** when testing destructive operations. Do not use irreplaceable personal media as test input.

## Opening the local library

1. Launch GoreeCloud Gallery.
2. If media access has not been granted, choose the media-access action and use Android's permission surface to select the access scope you want to provide.
3. Gallery reads only the local MediaStore view allowed by the current Android permission scope.
4. If Android denies the read or the provider is unavailable, Gallery reports the failure instead of presenting that failure as an empty library.

On supported Android versions, the app may operate with selected-media access rather than broad image/video access.

## Main destinations

The current native Development experience provides direct **Photos**, **Albums**, **Videos**, and **Settings** destinations.

- Photos and Videos use dense local thumbnail grids grouped by Today, Yesterday, or calendar date.
- Albums uses Android-authorized album metadata and includes a device-local Favorites collection when Favorites exist.
- Search and Newest/Oldest ordering operate only over the currently authorized local snapshot.
- Long-press a media tile to enter multi-select mode.

## Viewer

Tap a visible photo or video to open the bounded full-screen viewer.

- Use Previous and Next within the current authorized/presented collection.
- Share hands the current content URI to Android with a read-only URI grant.
- Favorite/Unfavorite changes Gallery's device-local Favorites state.
- More displays available media details.
- Edit remains unavailable in this Development candidate.
- Delete is enabled on Android 11 and newer and is intended to route through Android's system-owned destructive confirmation flow. Physical-device confirmation of the corrected `0.6.1-dev` path remains pending.

Image viewing remains bounded rather than full-resolution. Video presentation is still poster/thumbnail based; native playback remains separate work.

## Selection and bulk actions

Long-press a visible media tile to enter selection mode, then tap additional items to add or remove them.

Current actions include:

- Share selected media.
- Favorite or Unfavorite selected media.
- Delete selected media on Android 11 and newer through the current corrective Development candidate.
- More/Details when exactly one item is selected.

Move remains disabled until an approved Android-authorized organization path is implemented.

## Testing Delete and Recycle Bin

The current `0.6.1-dev` candidate corrects the first physical-device failure found in the Android-authorized destructive path.

### What changed from 0.6.0-dev

Gallery still performs one bounded `MediaStore.Files` query for local image/video discovery. The initial 0.6.0-dev build incorrectly carried the resulting generic `content://media/<volume>/file/<id>` identity into Android's destructive-request API. Android rejected that request before showing its confirmation dialog.

The corrective candidate now projects each authorized image row to its media-specific `content://media/<volume>/images/media/<id>` item URI and each authorized video row to `content://media/<volume>/video/media/<id>`. The mutation boundary also rejects generic `/file/<id>` targets so the same regression cannot silently pass adapter validation again.

### Recycle Bin mode — default

1. Install the current `0.6.1-dev` corrective build rather than the superseded 0.6.0-dev build.
2. In Settings > Deletion & recovery, leave **Move deleted items to Recycle Bin** on.
3. Open a disposable copied photo/video in the viewer, or select one or more disposable items with long-press.
4. Choose **Delete**.
5. Verify that Android's system confirmation now opens. If it does not, stop and report the exact Gallery feedback; do not assume a mutation occurred.
6. Review Android's system confirmation carefully and approve only the test media you intended to mutate.
7. After Android reports success, Gallery should refresh the current authorized library. The affected items should no longer appear in the ordinary Gallery snapshot.

In this mode, Gallery requests Android's MediaStore Trash operation. Gallery does **not yet** provide its own Recycle Bin browser or Restore/Purge interface; those are follow-on milestones.

### Permanent-delete mode

1. In Settings > Deletion & recovery, turn **Move deleted items to Recycle Bin** off.
2. Select only disposable copied media.
3. Choose **Delete**.
4. Verify that Android presents the destructive confirmation for permanent deletion.
5. Confirm only when you intend to permanently remove the disposable test media.

A confirmed permanent deletion removes stale Gallery Favorite references for those content URIs and refreshes the authorized library.

### Cancel behavior

Canceling Android's destructive confirmation must not be treated as success. The selected or viewed media should remain available unless another application or the platform changed it independently.

### Android-version boundary

This Development implementation supports Android 11 and newer. Android 10 remains fail-closed for Delete/Trash; no legacy direct-delete workaround is enabled.

### Mutation scope

A single destructive request is bounded to at most 100 unique Android MediaStore image/video item URIs. Gallery rejects blank, malformed, file, network, non-MediaStore, generic MediaStore Files, collection-only, and nonnumeric-item targets at the mutation adapter boundary.

## Settings

Current active settings include local thumbnail loading priority, included/excluded folder presentation, hidden-item visibility within Android's authorized snapshot, rounded-square thumbnails, Favorites/settings import/export, cache clearing, and the Recycle Bin versus permanent-delete choice on supported Android versions.

Playback/GIF preferences remain stored future-facing preferences until their corresponding runtime capabilities are implemented. Automatic empty-folder deletion also remains separately gated; its saved toggle does not mean that empty-folder cleanup is currently implemented.

Protected Photos/password protection is not simulated with insecure app-local credentials; it remains unavailable until supported authentication, protected storage, Privacy Shield, GoreeCloud Identity where applicable, and Wardveil requirements are implemented.

## Privacy and security

- Local browsing does not require cloud retrieval.
- Gallery does not receive authority to read media that Android has not authorized.
- Destructive requests are restricted to current media-specific MediaStore item URIs and Android owns the final confirmation surface.
- Selection itself never grants filesystem or media-write authority.
- Optional GoreeCloud Photos integration is a future user-controlled adapter milestone, not a dependency of the current local library.

## Current limitations

Still incomplete or separately gated are physical-device acceptance of the corrected Delete/Trash path, full-resolution viewing, native video playback, editing, Move/Copy, album creation/rename/reorder, Gallery-owned Recycle Bin browsing/restore/permanent purge, Protected Photos, richer grouping/view-density controls, complete GLAZE UI V1.0 and accessibility acceptance, signed release packaging, and Stable qualification.

## Troubleshooting

**No media is shown after permission was granted:** use the available refresh/change-access path. If the provider read fails, Gallery states that the provider read failed rather than assuming there are no files.

**Only some media appears:** Android may have granted selected-media or media-type-limited access. Change Android media access if you want Gallery to see a different authorized subset.

**Delete is disabled:** this Development path requires Android 11 or newer and a currently selected/presented authorized media item.

**Gallery says “Gallery refused an invalid media mutation request”:** if this occurs on `0.6.0-dev`, that is the known generic `MediaStore.Files` URI defect and the build is superseded for destructive-operation testing. If it occurs on the corrective `0.6.1-dev` build, stop testing destructive actions and report the item type plus the exact feedback so the new URI projection can be investigated without bypassing validation.

**Android confirmation does not open:** Gallery must not claim success. Do not assume the item was deleted or trashed.

**A trashed item is not visible in Gallery but still exists in the system Recycle Bin:** that is expected after a successful Recycle Bin operation. Gallery-owned Trash browsing and Restore/Purge are not implemented yet.
