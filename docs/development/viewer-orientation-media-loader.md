# Orientation-aware viewer media loader

Status: Development

The native Gallery app now has one full-screen media-loading boundary that composes the validated orientation-aware image decoder with the existing provider-thumbnail fallback.

Authorized image items are routed through `GalleryViewerImageDecoder.decodeBounded(...)` first, preserving Android's encoded-orientation handling and the 2048 px long-edge / viewport bounds. Video items retain the provider thumbnail path. If an authorized image decode fails, the same bounded provider thumbnail path may be used as a presentation fallback without requesting broader media authority.

The loader catches provider security, I/O, and runtime failures and returns no bitmap rather than inventing a successful viewer state.

## Remaining integration

`GalleryActivity` still needs to replace its full-screen viewer `loadLocalThumbnail(...)` call with `GalleryViewerMediaLoader.load(...)` using the measured viewer viewport. Grid, album, and video-poster thumbnail loading should remain unchanged.

This slice does not add MediaStore scope, file/network/cloud access, mutation, sharing, deletion, or Stable acceptance. Representative-device orientation verification remains required.
