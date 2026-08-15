#!/usr/bin/env bash
set -euo pipefail

GALLERY_DIR="${1:-upstream-gallery}"
COMMONS_DIR="${2:-upstream-commons}"

fail() {
  printf 'GoreeCloud Gallery source validation failed: %s\n' "$*" >&2
  exit 1
}

[ -d "$GALLERY_DIR" ] || fail "Gallery source directory not found: $GALLERY_DIR"
[ -d "$COMMONS_DIR" ] || fail "Commons source directory not found: $COMMONS_DIR"

COMMONS_MAIN="$COMMONS_DIR/commons/src/main"
CONFIG="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/helpers/Config.kt"
POLICY="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/helpers/GoreeCloudGalleryPolicy.kt"
POLICY_TEST="$GALLERY_DIR/app/src/test/kotlin/org/fossify/gallery/helpers/GoreeCloudGalleryPolicyTest.kt"
FILE_STYLE_DIALOG="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/dialogs/ChangeFileThumbnailStyleDialog.kt"
FOLDER_STYLE_DIALOG="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/dialogs/ChangeFolderThumbnailStyleDialog.kt"
SETTINGS_LAYOUT="$GALLERY_DIR/app/src/main/res/layout/activity_settings.xml"
FILE_STYLE_LAYOUT="$GALLERY_DIR/app/src/main/res/layout/dialog_change_file_thumbnail_style.xml"
FOLDER_STYLE_LAYOUT="$GALLERY_DIR/app/src/main/res/layout/dialog_change_folder_thumbnail_style.xml"
SEARCH_MENU="$COMMONS_DIR/commons/src/main/kotlin/org/fossify/commons/views/MySearchMenu.kt"
SEARCH_LAYOUT="$COMMONS_DIR/commons/src/main/res/layout/menu_search.xml"
POPUP_LIGHT="$COMMONS_DIR/commons/src/main/res/drawable/goreecloud_gallery_popup_bg_light.xml"
POPUP_DARK="$COMMONS_DIR/commons/src/main/res/drawable/goreecloud_gallery_popup_bg_dark.xml"

! grep -R -Fq "You are using a fake version of the app" "$COMMONS_MAIN" \
  || fail "legacy counterfeit-build warning remains in Commons source"
! grep -R -Fq "download the original one from www.fossify.org. Thanks" "$COMMONS_MAIN" \
  || fail "legacy Fossify download warning remains in Commons source"

grep -Fq 'get() = GoreeCloudGalleryPolicy.CROP_THUMBNAILS' "$CONFIG" \
  || fail "GoreeCloud crop policy is not wired into Config"
grep -Fq 'get() = GoreeCloudGalleryPolicy.FOLDER_STYLE' "$CONFIG" \
  || fail "GoreeCloud folder-style policy is not wired into Config"
grep -Fq 'get() = GoreeCloudGalleryPolicy.FILE_ROUNDED_CORNERS' "$CONFIG" \
  || fail "GoreeCloud file-corner policy is not wired into Config"
grep -Fq 'const val CROP_THUMBNAILS = false' "$POLICY" \
  || fail "uncropped thumbnail policy is missing"
grep -Fq 'const val FILE_ROUNDED_CORNERS = true' "$POLICY" \
  || fail "rounded file thumbnail policy is missing"
grep -Fq 'const val FOLDER_STYLE = FOLDER_STYLE_ROUNDED_CORNERS' "$POLICY" \
  || fail "rounded folder thumbnail policy is missing"
grep -Fq 'class GoreeCloudGalleryPolicyTest' "$POLICY_TEST" \
  || fail "GoreeCloud behavioral policy tests are missing"
grep -Fq 'config.fileRoundedCorners = true' "$FILE_STYLE_DIALOG" \
  || fail "rounded file thumbnail enforcement is missing"
grep -Fq 'val style = FOLDER_STYLE_ROUNDED_CORNERS' "$FOLDER_STYLE_DIALOG" \
  || fail "rounded folder thumbnail enforcement is missing"

! grep -Fq 'settings_crop_thumbnails' "$SETTINGS_LAYOUT" \
  || fail "removed crop-thumbnail setting returned"
! grep -Fq 'dialog_file_style_rounded_corners' "$FILE_STYLE_LAYOUT" \
  || fail "removed file-style control returned"
! grep -Fq 'dialog_radio_folder_square' "$FOLDER_STYLE_LAYOUT" \
  || fail "removed square-folder control returned"
! grep -Fq 'dialog_radio_folder_rounded_corners' "$FOLDER_STYLE_LAYOUT" \
  || fail "removed folder-style selector returned"

grep -Fq 'ColorUtils.calculateLuminance(backgroundColor) >= 0.5' "$SEARCH_MENU" \
  || fail "popup luminance selection is missing"
grep -Fq 'R.style.GoreeCloudGalleryPopupThemeLight' "$SEARCH_MENU" \
  || fail "light popup theme is missing"
grep -Fq 'R.style.GoreeCloudGalleryPopupThemeDark' "$SEARCH_MENU" \
  || fail "dark popup theme is missing"
[ "$(grep -Fc 'updatePopupTheme()' "$SEARCH_MENU")" -ge 2 ] \
  || fail "popup theme is not refreshed in all accepted paths"
grep -Fq 'app:popupTheme="@style/GoreeCloudGalleryPopupThemeLight"' "$SEARCH_LAYOUT" \
  || fail "default toolbar popup theme is missing"
grep -Fq '#F7F8FC' "$POPUP_LIGHT" \
  || fail "accepted light popup surface is missing"
grep -Fq '#171C29' "$POPUP_DARK" \
  || fail "accepted dark popup surface is missing"

printf 'GoreeCloud Gallery source acceptance invariants passed.\n'
