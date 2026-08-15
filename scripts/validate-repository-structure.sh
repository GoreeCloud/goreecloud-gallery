#!/usr/bin/env bash
set -euo pipefail

fail() {
  printf 'GoreeCloud Gallery repository structure validation failed: %s\n' "$*" >&2
  exit 1
}

required_files=(
  README.md
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
  docs/RELEASE-SIGNING.md
  docs/STABLE-RELEASE-CHECKLIST.md
  docs/RELEASE-EVIDENCE-TEMPLATE.md
  docs/REPOSITORY-READINESS.md
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

for patch_line in gc1 gc2 gc3 gc4 gc5 gc6 gc7; do
  [ -d "patches/$patch_line" ] || fail "required historical patch directory is missing: patches/$patch_line"
done

grep -Fq 'GoreeCloud Gallery' README.md \
  || fail 'README does not identify GoreeCloud Gallery'
grep -Fq '1.0.0-gc.7' README.md \
  || fail 'README does not identify the current acceptance line'
grep -Fq 'com.goreecloud.gallery' README.md \
  || fail 'README does not record the application ID'
grep -Fq 'Glaze UI' README.md \
  || fail 'README does not record the Glaze UI requirement'

grep -Fq '@GoreeCloud' .github/CODEOWNERS \
  || fail 'CODEOWNERS does not identify GoreeCloud review ownership'
grep -Fq 'GNU General Public License' NOTICE.md \
  || fail 'NOTICE does not record the GPL license boundary'
grep -Fq 'b28299dc33821eee8d108a9880ce87876cf31443' NOTICE.md \
  || fail 'NOTICE does not record the pinned Fossify Gallery revision'
grep -Fq 'acfd352df1a1852d17a5f77def8b7ad6e522a5b6' NOTICE.md \
  || fail 'NOTICE does not record the pinned Fossify Commons revision'

grep -Fq 'Android user / profile boundary' docs/ARCHITECTURE.md \
  || fail 'architecture does not document Android user/profile isolation'
grep -Fq 'Glaze UI architecture' docs/ARCHITECTURE.md \
  || fail 'architecture does not document the Glaze UI layer'
grep -Fq 'Disposable copied media used' docs/RELEASE-EVIDENCE-TEMPLATE.md \
  || fail 'release evidence template does not protect destructive-operation testing'
grep -Fq 'Stable release: Not approved' docs/REPOSITORY-READINESS.md \
  || fail 'repository readiness record does not preserve the stable-release boundary'

if git ls-files -z | grep -zE '\.(apk|aab|jks|keystore|p12|pfx|pem|key|der)$' >/dev/null; then
  fail 'generated package or key/certificate-container material is tracked in Git'
fi

for generated in .build upstream-gallery upstream-commons dist dex-validation; do
  if git ls-files "$generated/**" | grep -q .; then
    fail "generated build path contains tracked files: $generated"
  fi
done

printf 'GoreeCloud Gallery repository structure validation passed.\n'
