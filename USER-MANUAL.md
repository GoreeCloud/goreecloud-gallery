# GoreeCloud Gallery User Manual

## Status

This manual describes the **current first-party native Development experience**. It does not describe a Stable or production-approved release.

## What Gallery currently does

The native Android application reads a bounded set of photos and videos that Android has authorized GoreeCloud Gallery to access. The current experience is local-first: ordinary browsing, filtering, sorting, thumbnails, and bounded previews do not require a GoreeCloud account or cloud connection.

## Opening the local library

1. Launch GoreeCloud Gallery.
2. If media access has not been granted, choose **Choose media access** and use Android's permission surface to select the access scope you want to provide.
3. Gallery reads only the local MediaStore view allowed by the current Android permission scope.
4. If Android denies the read or the provider is unavailable, Gallery reports the failure instead of presenting that failure as an empty library.

On supported Android versions, the app may operate with selected-media access rather than broad image/video access.

## Filtering media

Above the media list, use:

- **All** — show authorized images and videos.
- **Images** — show only authorized image rows.
- **Videos** — show only authorized video rows.

Changing the filter does **not** request another MediaStore listing. It filters the already-authorized snapshot currently held by Gallery.

## Sorting media

Use:

- **Newest first** — order the currently presented authorized snapshot from newest to oldest.
- **Oldest first** — order it from oldest to newest.

Sorting is local and does not trigger another provider query or cloud request. When media items have the same effective timestamp, their existing snapshot order is preserved.

## Previewing media

Tap a media row to open the current bounded preview.

- Use **Previous** and **Next** to navigate inside the same filtered and sorted authorized snapshot.
- Gallery re-checks the current media permission and load generation before it renders the next preview item.
- Images use a bounded local preview; full-resolution viewing and editing are separate milestones.
- Videos currently use a local poster/thumbnail preview only; playback is not yet implemented in this Development surface.

## Privacy and security

- Local browsing does not require cloud retrieval.
- Gallery does not receive authority to read media that Android has not authorized.
- The current browse experience does not claim production Wardveil acceptance for future risky file/media operations.
- Optional GoreeCloud Photos integration is a future user-controlled adapter milestone, not a dependency of the current local library.

## Current limitations

The native Development line does not yet provide a mature album/grid browser, full-resolution viewer, video playback, editing, approved sharing/export, complete hidden/excluded-media policy, signed Stable packaging, or completed representative-device/accessibility acceptance.

The preserved Fossify-based application and patch history are transitional provenance/migration reference and should not be treated as the current native product manual.

## Troubleshooting

**No media is shown after permission was granted:** use **Refresh local library** when available. If the provider read fails, Gallery will state that the provider read failed rather than assuming there are no files.

**Only some media appears:** Android may have granted selected-media or media-type-limited access. Use **Change selected media** when the current permission mode supports it.

**A filter is empty:** the current authorized snapshot may contain no media of that type. Switching to **All** does not expand Android authorization; it only changes the local presentation of already-authorized rows.
