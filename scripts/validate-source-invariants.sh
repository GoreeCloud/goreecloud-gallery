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
PROPERTIES="$GALLERY_DIR/gradle.properties"
NOTICE="$GALLERY_DIR/GOREECLOUD-NOTICE.md"
CONFIG="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/helpers/Config.kt"
POLICY="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/helpers/GoreeCloudGalleryPolicy.kt"
POLICY_TEST="$GALLERY_DIR/app/src/test/kotlin/org/fossify/gallery/helpers/GoreeCloudGalleryPolicyTest.kt"
SETTINGS_ACTIVITY="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/activities/SettingsActivity.kt"
FILE_STYLE_DIALOG="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/dialogs/ChangeFileThumbnailStyleDialog.kt"
FOLDER_STYLE_DIALOG="$GALLERY_DIR/app/src/main/kotlin/org/fossify/gallery/dialogs/ChangeFolderThumbnailStyleDialog.kt"
MAIN_LAYOUT="$GALLERY_DIR/app/src/main/res/layout/activity_main.xml"
MEDIA_LAYOUT="$GALLERY_DIR/app/src/main/res/layout/activity_media.xml"
SETTINGS_LAYOUT="$GALLERY_DIR/app/src/main/res/layout/activity_settings.xml"
SETTINGS_STRINGS="$GALLERY_DIR/app/src/main/res/values/strings.xml"
GLAZE_VALUES="$GALLERY_DIR/app/src/main/res/values/goreecloud_glaze.xml"
GLAZE_NIGHT="$GALLERY_DIR/app/src/main/res/values-night/goreecloud_glaze.xml"
GLAZE_SW600="$GALLERY_DIR/app/src/main/res/values-sw600dp/goreecloud_glaze.xml"
GLAZE_SW840="$GALLERY_DIR/app/src/main/res/values-sw840dp/goreecloud_glaze.xml"
GLAZE_CANVAS="$GALLERY_DIR/app/src/main/res/drawable/goreecloud_glaze_canvas.xml"
GLAZE_TOOLBAR="$GALLERY_DIR/app/src/main/res/drawable/goreecloud_glaze_toolbar.xml"
GLAZE_ITEM="$GALLERY_DIR/app/src/main/res/drawable/goreecloud_glaze_settings_item.xml"
GLAZE_EMPTY_ACTION="$GALLERY_DIR/app/src/main/res/drawable/goreecloud_glaze_empty_action.xml"
FILE_STYLE_LAYOUT="$GALLERY_DIR/app/src/main/res/layout/dialog_change_file_thumbnail_style.xml"
FOLDER_STYLE_LAYOUT="$GALLERY_DIR/app/src/main/res/layout/dialog_change_folder_thumbnail_style.xml"
SEARCH_MENU="$COMMONS_DIR/commons/src/main/kotlin/org/fossify/commons/views/MySearchMenu.kt"
SEARCH_LAYOUT="$COMMONS_DIR/commons/src/main/res/layout/menu_search.xml"
POPUP_LIGHT="$COMMONS_DIR/commons/src/main/res/drawable/goreecloud_gallery_popup_bg_light.xml"
POPUP_DARK="$COMMONS_DIR/commons/src/main/res/drawable/goreecloud_gallery_popup_bg_dark.xml"

grep -Fqx 'VERSION_NAME=1.0.0' "$PROPERTIES" || fail "Acceptance-candidate version name is not 1.0.0"
grep -Fqx 'VERSION_CODE=10012' "$PROPERTIES" || fail "Acceptance-candidate version code is not 10012"
grep -Fq 'Stable-candidate binary identity' "$NOTICE" || fail "Stable-candidate release boundary is missing from notice"
grep -Fq 'Stable classification is not automatic' "$NOTICE" || fail "Stable classification boundary is missing from notice"
grep -Fq 'gc.10 simplifies Settings' "$NOTICE" || fail "gc.10 settings-cleanup notice is missing"
grep -Fq 'gc.11 deepens the native Android mapping to Glaze UI 1.0.0' "$NOTICE" || fail "gc.11 Glaze UI notice is missing"
grep -Fq 'gc.12 extends the native Glaze UI 1.0.0 mapping' "$NOTICE" || fail "gc.12 browsing Glaze UI notice is missing"

! grep -R -Fq "You are using a fake version of the app" "$COMMONS_MAIN" || fail "legacy counterfeit-build warning remains in Commons source"
! grep -R -Fq "download the original one from www.fossify.org. Thanks" "$COMMONS_MAIN" || fail "legacy Fossify download warning remains in Commons source"

grep -Fq 'get() = GoreeCloudGalleryPolicy.CROP_THUMBNAILS' "$CONFIG" || fail "GoreeCloud crop policy is not wired into Config"
grep -Fq 'get() = GoreeCloudGalleryPolicy.FOLDER_STYLE' "$CONFIG" || fail "GoreeCloud folder-style policy is not wired into Config"
grep -Fq 'get() = GoreeCloudGalleryPolicy.FILE_ROUNDED_CORNERS' "$CONFIG" || fail "GoreeCloud file-corner policy is not wired into Config"
grep -Fq 'const val CROP_THUMBNAILS = false' "$POLICY" || fail "uncropped thumbnail policy is missing"
grep -Fq 'const val FILE_ROUNDED_CORNERS = true' "$POLICY" || fail "rounded file thumbnail policy is missing"
grep -Fq 'const val FOLDER_STYLE = FOLDER_STYLE_ROUNDED_CORNERS' "$POLICY" || fail "rounded folder thumbnail policy is missing"
grep -Fq 'class GoreeCloudGalleryPolicyTest' "$POLICY_TEST" || fail "GoreeCloud behavioral policy tests are missing"
grep -Fq 'config.fileRoundedCorners = true' "$FILE_STYLE_DIALOG" || fail "rounded file thumbnail enforcement is missing"
grep -Fq 'val style = FOLDER_STYLE_ROUNDED_CORNERS' "$FOLDER_STYLE_DIALOG" || fail "rounded folder thumbnail enforcement is missing"

! grep -Fq 'settings_crop_thumbnails' "$SETTINGS_LAYOUT" || fail "removed crop-thumbnail setting returned"
! grep -Fq 'settings_file_thumbnail_style_holder' "$SETTINGS_LAYOUT" || fail "removed file-thumbnail-style setting returned"
! grep -Fq 'settings_folder_thumbnail_style_holder' "$SETTINGS_LAYOUT" || fail "removed folder-thumbnail-style setting returned"
! grep -Fq 'settings_purchase_thank_you_holder' "$SETTINGS_LAYOUT" || fail "upstream purchase acknowledgement returned"
! grep -Fq 'setupFileThumbnailStyle()' "$SETTINGS_ACTIVITY" || fail "removed file-thumbnail handler returned"
! grep -Fq 'setupFolderThumbnailStyle()' "$SETTINGS_ACTIVITY" || fail "removed folder-thumbnail handler returned"
grep -Fq 'Settings.ACTION_APPLICATION_DETAILS_SETTINGS' "$SETTINGS_ACTIVITY" || fail "Android privacy/permissions shortcut is missing"
grep -Fq 'Uri.parse("package:$packageName")' "$SETTINGS_ACTIVITY" || fail "Android app-settings package target is missing"
grep -Fq '@string/goreecloud_privacy_permissions' "$SETTINGS_LAYOUT" || fail "privacy section label is missing"
grep -Fq '@string/goreecloud_android_app_permissions' "$SETTINGS_LAYOUT" || fail "Android app-permissions label is missing"
grep -Fq 'name="goreecloud_privacy_permissions"' "$SETTINGS_STRINGS" || fail "privacy section string is missing"
grep -Fq 'name="goreecloud_android_app_permissions"' "$SETTINGS_STRINGS" || fail "Android app-permissions string is missing"

for path in "$GLAZE_VALUES" "$GLAZE_NIGHT" "$GLAZE_SW600" "$GLAZE_SW840" "$GLAZE_CANVAS" "$GLAZE_TOOLBAR" "$GLAZE_ITEM" "$GLAZE_EMPTY_ACTION"; do
  [ -f "$path" ] || fail "missing Glaze UI native resource: $path"
done
grep -Fq 'goreecloud_glaze_target_minimum">44dp' "$GLAZE_VALUES" || fail "Glaze 44dp minimum target is missing"
grep -Fq 'goreecloud_glaze_target_comfortable">48dp' "$GLAZE_VALUES" || fail "Glaze 48dp comfortable target is missing"
grep -Fq 'goreecloud_glaze_motion_instant">90' "$GLAZE_VALUES" || fail "Glaze Instant motion token is missing"
grep -Fq 'goreecloud_glaze_motion_fast">160' "$GLAZE_VALUES" || fail "Glaze Fast motion token is missing"
grep -Fq 'goreecloud_glaze_motion_standard">220' "$GLAZE_VALUES" || fail "Glaze Standard motion token is missing"
grep -Fq 'goreecloud_glaze_motion_emphasized">320' "$GLAZE_VALUES" || fail "Glaze Emphasized motion token is missing"
grep -Fq 'goreecloud_glaze_browser_horizontal_inset">8dp' "$GLAZE_VALUES" || fail "Glaze compact browsing inset is missing"
grep -Fq 'goreecloud_glaze_browser_vertical_inset">8dp' "$GLAZE_VALUES" || fail "Glaze browsing vertical inset is missing"
grep -Fq '#0D1119' "$GLAZE_NIGHT" || fail "Glaze dark canvas mapping is missing"
grep -Fq '#7AA2FF' "$GLAZE_NIGHT" || fail "Glaze dark accent mapping is missing"
grep -Fq '>32dp<' "$GLAZE_SW600" || fail "Glaze medium settings inset is missing"
grep -Fq '>64dp<' "$GLAZE_SW840" || fail "Glaze expanded settings inset is missing"
grep -Fq 'goreecloud_glaze_browser_horizontal_inset">16dp' "$GLAZE_SW600" || fail "Glaze medium browsing inset is missing"
grep -Fq 'goreecloud_glaze_browser_horizontal_inset">24dp' "$GLAZE_SW840" || fail "Glaze expanded browsing inset is missing"
grep -Fq '@drawable/goreecloud_glaze_canvas' "$SETTINGS_LAYOUT" || fail "Glaze canvas is not applied to Settings"
grep -Fq '@drawable/goreecloud_glaze_toolbar' "$SETTINGS_LAYOUT" || fail "Glaze toolbar is not applied to Settings"
grep -Fq '@drawable/goreecloud_glaze_settings_item' "$SETTINGS_LAYOUT" || fail "Glaze raised setting rows are not applied"
grep -Fq '@dimen/goreecloud_glaze_target_comfortable' "$SETTINGS_LAYOUT" || fail "Glaze comfortable target size is not applied"
grep -Fq '@dimen/goreecloud_glaze_settings_horizontal_inset' "$SETTINGS_LAYOUT" || fail "Glaze adaptive inset is not applied"

for layout in "$MAIN_LAYOUT" "$MEDIA_LAYOUT"; do
  grep -Fq '@drawable/goreecloud_glaze_canvas' "$layout" || fail "Glaze canvas is missing from a primary browsing surface"
  grep -Fq '@drawable/goreecloud_glaze_toolbar' "$layout" || fail "Glaze menu chrome is missing from a primary browsing surface"
  grep -Fq '@dimen/goreecloud_glaze_browser_horizontal_inset' "$layout" || fail "Glaze adaptive browsing inset is missing"
  grep -Fq '@dimen/goreecloud_glaze_browser_vertical_inset' "$layout" || fail "Glaze browsing breathing room is missing"
  grep -Fq '@drawable/goreecloud_glaze_empty_action' "$layout" || fail "Glaze Raised empty-state action is missing"
  grep -Fq '@dimen/goreecloud_glaze_target_comfortable' "$layout" || fail "Glaze comfortable browsing action target is missing"
done
grep -Fq '@color/goreecloud_glaze_accent' "$MEDIA_LAYOUT" || fail "Glaze media loading accent is missing"
grep -Fq '@color/goreecloud_glaze_surface' "$GLAZE_EMPTY_ACTION" || fail "Glaze Raised empty-action surface is missing"
grep -Fq '@color/goreecloud_glaze_line' "$GLAZE_EMPTY_ACTION" || fail "Glaze Raised empty-action outline is missing"

# Android XML namespace declarations use the standard http://schemas.android.com URI;
# reject actual network-delivered UI references without treating that namespace as a dependency.
! grep -R -Eq 'https://|src="http://|href="http://|@font/.*remote' "$GLAZE_VALUES" "$GLAZE_NIGHT" "$GLAZE_CANVAS" "$GLAZE_TOOLBAR" "$GLAZE_ITEM" "$GLAZE_EMPTY_ACTION" || fail "Glaze UI resources introduced a remote dependency"

! grep -Fq 'dialog_file_style_rounded_corners' "$FILE_STYLE_LAYOUT" || fail "removed file-style control returned"
! grep -Fq 'dialog_radio_folder_square' "$FOLDER_STYLE_LAYOUT" || fail "removed square-folder control returned"
! grep -Fq 'dialog_radio_folder_rounded_corners' "$FOLDER_STYLE_LAYOUT" || fail "removed folder-style selector returned"

grep -Fq 'ColorUtils.calculateLuminance(backgroundColor) >= 0.5' "$SEARCH_MENU" || fail "popup luminance selection is missing"
grep -Fq 'R.style.GoreeCloudGalleryPopupThemeLight' "$SEARCH_MENU" || fail "light popup theme is missing"
grep -Fq 'R.style.GoreeCloudGalleryPopupThemeDark' "$SEARCH_MENU" || fail "dark popup theme is missing"
[ "$(grep -Fc 'updatePopupTheme()' "$SEARCH_MENU")" -ge 2 ] || fail "popup theme is not refreshed in all accepted paths"
grep -Fq 'app:popupTheme="@style/GoreeCloudGalleryPopupThemeLight"' "$SEARCH_LAYOUT" || fail "default toolbar popup theme is missing"
grep -Fq '#F7F8FC' "$POPUP_LIGHT" || fail "accepted light popup surface is missing"
grep -Fq '#171C29' "$POPUP_DARK" || fail "accepted dark popup surface is missing"

printf 'GoreeCloud Gallery 1.0.0 gc.12 Glaze UI source acceptance invariants passed.\n'