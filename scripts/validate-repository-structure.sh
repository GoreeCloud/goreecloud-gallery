#!/usr/bin/env bash
set -euo pipefail

fail() {
  printf 'GoreeCloud Gallery repository structure validation failed: %s\n' "$*" >&2
  exit 1
}

required_files=(
  README.md
  CHANGELOG.md
  LICENSE
  SECURITY.md
  CONTRIBUTING.md
  NOTICE.md
  .gitignore
  .github/CODEOWNERS
  .github/dependabot.yml
  .github/pull_request_template.md
  .github/workflows/build-and-validate.yml
  .github/workflows/build-signed-release-candidate.yml
  docs/ARCHITECTURE.md
  docs/BUILD-AND-RELEASE.md
  docs/GLAZE-UI.md
  docs/RELEASE-SIGNING.md
  docs/STABLE-SIGNING-RUNBOOK.md
  docs/REAL-DEVICE-ACCEPTANCE-RUNBOOK.md
  docs/STABLE-CANDIDATE-1.0.0.md
  docs/STABLE-RELEASE-CHECKLIST.md
  docs/RELEASE-EVIDENCE-TEMPLATE.md
  docs/REPOSITORY-READINESS.md
  patches/gc9/build_goreecloud_gallery_gc9.py
  scripts/materialize-patches.sh
  scripts/reconstruct-source.sh
  scripts/validate-apk.sh
  scripts/validate-repository-security.sh
  scripts/validate-repository-structure.sh
  scripts/validate-source-invariants.sh
  scripts/write-build-evidence.sh
)

for path in "${required_files[@]}"; do
  [ -s "$path" ] || fail "required file is missing or empty: $path"
done

for patch_line in gc1 gc2 gc3 gc4 gc5 gc6 gc7 gc8 gc9; do
  [ -d "patches/$patch_line" ] || fail "required patch directory is missing: patches/$patch_line"
done

grep -Fq 'GoreeCloud Gallery' README.md || fail 'README does not identify GoreeCloud Gallery'
grep -Fq 'com.goreecloud.gallery' README.md || fail 'README does not record the application ID'
grep -Fq 'Glaze UI' README.md || fail 'README does not record the Glaze UI requirement'
grep -Fq 'GNU GENERAL PUBLIC LICENSE' LICENSE || fail 'root LICENSE is not the GNU GPL license text'
grep -Fq 'Version 3, 29 June 2007' LICENSE || fail 'root LICENSE does not identify GNU GPL version 3'
grep -Fq '@GoreeCloud' .github/CODEOWNERS || fail 'CODEOWNERS does not identify GoreeCloud review ownership'
grep -Fq 'GNU General Public License' NOTICE.md || fail 'NOTICE does not record the GPL license boundary'
grep -Fq 'b28299dc33821eee8d108a9880ce87876cf31443' NOTICE.md || fail 'NOTICE does not record the pinned Fossify Gallery revision'
grep -Fq 'acfd352df1a1852d17a5f77def8b7ad6e522a5b6' NOTICE.md || fail 'NOTICE does not record the pinned Fossify Commons revision'

grep -Fq 'Android user / profile boundary' docs/ARCHITECTURE.md || fail 'architecture does not document Android user/profile isolation'
grep -Fq 'Glaze UI architecture' docs/ARCHITECTURE.md || fail 'architecture does not document the Glaze UI layer'
grep -Fq 'GoreeCloud Gallery Glaze UI Contract' docs/GLAZE-UI.md || fail 'Glaze UI contract does not identify its Gallery scope'
grep -Fq 'No permanent Glaze UI exception is approved' docs/GLAZE-UI.md || fail 'Glaze UI exception boundary is not documented'
grep -Fq 'meaningful GoreeCloud-owned JVM tests actually execute' docs/GLAZE-UI.md || fail 'Glaze UI contract does not preserve behavioral-test evidence requirements'
grep -Fq 'GoreeCloud Gallery Stable Signing Runbook' docs/STABLE-SIGNING-RUNBOOK.md || fail 'stable signing runbook does not identify its Gallery scope'
grep -Fq 'document the existence, purpose, owner, lifecycle, and validation of signing secrets without reproducing the active secret values' docs/STABLE-SIGNING-RUNBOOK.md || fail 'stable signing runbook does not preserve secret-separation requirements'
grep -Fq 'GoreeCloud Gallery Real-Device Acceptance Runbook' docs/REAL-DEVICE-ACCEPTANCE-RUNBOOK.md || fail 'real-device acceptance runbook does not identify its Gallery scope'
grep -Fq 'Glaze UI is mandatory for controlled Gallery surfaces' docs/REAL-DEVICE-ACCEPTANCE-RUNBOOK.md || fail 'real-device runbook does not preserve the Glaze UI acceptance boundary'
grep -Fq 'GoreeCloud Gallery 1.0.0 Stable Candidate' docs/STABLE-CANDIDATE-1.0.0.md || fail 'Stable candidate contract does not identify the final candidate'
grep -Fq 'Version identity and release classification are deliberately separate' docs/STABLE-CANDIDATE-1.0.0.md || fail 'Stable candidate contract does not preserve classification separation'
grep -Fq 'Promotion to Stable must reuse the exact signed candidate binary' docs/STABLE-CANDIDATE-1.0.0.md || fail 'Stable candidate contract does not preserve binary identity'
grep -Fq 'Disposable copied media used' docs/RELEASE-EVIDENCE-TEMPLATE.md || fail 'release evidence template does not protect destructive-operation testing'
grep -Fq 'Stable release: Not approved' docs/REPOSITORY-READINESS.md || fail 'repository readiness record does not preserve the stable-release boundary'

grep -Fq 'VERSION_NAME = "1.0.0"' patches/gc9/build_goreecloud_gallery_gc9.py || fail 'gc.9 does not set the final semantic version'
grep -Fq 'VERSION_CODE = "10009"' patches/gc9/build_goreecloud_gallery_gc9.py || fail 'gc.9 does not set the Stable-candidate version code'
grep -Fq 'Stable classification is not automatic' patches/gc9/build_goreecloud_gallery_gc9.py || fail 'gc.9 does not preserve the Stable classification boundary'

if git ls-files -z | grep -zE '\.(apk|aab|jks|keystore|p12|pfx|pem|key|der)$' >/dev/null; then
  fail 'generated package or key/certificate-container material is tracked in Git'
fi

for generated in .build upstream-gallery upstream-commons dist dex-validation; do
  if git ls-files "$generated/**" | grep -q .; then
    fail "generated build path contains tracked files: $generated"
  fi
done

printf 'GoreeCloud Gallery repository structure validation passed.\n'
