#!/usr/bin/env bash
set -euo pipefail

OUT_DIR="${1:-.build/patches}"
mkdir -p "$OUT_DIR"

materialize() {
  local gc="$1"
  local output="$2"
  local expected_blob="$3"

  mapfile -t fragments < <(find "patches/$gc" -maxdepth 1 -type f -name '*.pyfrag' | sort)
  if [ "${#fragments[@]}" -eq 0 ]; then
    echo "No patch fragments found for $gc" >&2
    exit 1
  fi

  # GitHub's line-range source reads omit the source file's terminal newline.
  # The migrated final fragments therefore intentionally omit it too. Restore
  # exactly one terminal newline after concatenation before validating the
  # reconstructed file against the historical Git blob.
  cat "${fragments[@]}" > "$OUT_DIR/$output"
  printf '\n' >> "$OUT_DIR/$output"

  local actual_blob
  actual_blob="$(git hash-object "$OUT_DIR/$output")"
  if [ "$actual_blob" != "$expected_blob" ]; then
    echo "Patch provenance verification failed for $gc/$output" >&2
    echo "expected Git blob: $expected_blob" >&2
    echo "actual Git blob:   $actual_blob" >&2
    exit 1
  fi

  chmod +x "$OUT_DIR/$output"
  printf 'Verified %-4s %s (%s)\n' "$gc" "$output" "$actual_blob"
}

materialize gc1 build_goreecloud_gallery.py     100b2080cf5e82275dce7a1a1f35d8869ab8af38
materialize gc2 build_goreecloud_gallery_gc2.py 5a9d84b0eaa49107cba52f2b1f02131fa5d03f3e
materialize gc3 build_goreecloud_gallery_gc3.py 67a4e4acfebb2c6e3d271d5387ededa60bc0ee87
materialize gc4 build_goreecloud_gallery_gc4.py 8f3a48d424ead1c253e0e9eb91f27706e6162757
materialize gc5 build_goreecloud_gallery_gc5.py e8b1362e87d0e47997fa7f2ee36f851b4123fab5
materialize gc6 build_goreecloud_gallery_gc6.py 4c9094e7b4139e0472f1c17ab2ff4a2186244c78
materialize gc7 build_goreecloud_gallery_gc7.py 516339487492806932ec14b669c183e4919b1187
