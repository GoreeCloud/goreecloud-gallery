# Authorized selection summary

Status: Development

This slice adds a native-core summary projection for the current Gallery multi-selection foundation.

The summary reports selected item count, image count, video count, and total selected bytes. It is derived only from items that remain selected, remain in the current authorized media snapshot, and are still present in the caller-supplied rendered collection with the same authorized content URI.

## Authority boundary

The projection grants no MediaStore access, sharing, deletion, mutation, persistence, or selection authority. It cannot add items to selection and does not expand Gallery's authorized media snapshot.

Byte totals use checked addition and fail rather than wrapping on overflow.

## Next composition step

The Android Gallery surface can use this projection for a Glaze UI contextual selection header and action eligibility presentation while keeping actual share, move, copy, delete, favorite, and secure-content operations behind their own explicit authority paths.
