#!/usr/bin/env bash
set -euo pipefail

fail() {
  printf 'GoreeCloud Gallery repository security validation failed: %s\n' "$*" >&2
  exit 1
}

mapfile -t shell_scripts < <(find scripts -maxdepth 1 -type f -name '*.sh' | sort)
[ "${#shell_scripts[@]}" -ge 1 ] || fail "no GoreeCloud shell scripts found"
for script in "${shell_scripts[@]}"; do
  bash -n "$script" || fail "shell syntax check failed: $script"
done

mapfile -t workflow_files < <(find .github/workflows -maxdepth 1 -type f \( -name '*.yml' -o -name '*.yaml' \) | sort)
[ "${#workflow_files[@]}" -ge 1 ] || fail "no GitHub Actions workflows found"

for workflow in "${workflow_files[@]}"; do
  grep -Eq '^permissions:' "$workflow" \
    || fail "$workflow does not declare an explicit top-level permissions boundary"
done

if grep -R -n -F 'persist-credentials: true' .github/workflows; then
  fail "a workflow persists GitHub checkout credentials"
fi

while IFS= read -r use_target; do
  case "$use_target" in
    ./*)
      continue
      ;;
  esac

  ref="${use_target##*@}"
  if [[ ! "$ref" =~ ^[0-9a-f]{40}$ ]]; then
    fail "third-party Action is not pinned to a 40-character commit SHA: $use_target"
  fi
done < <(grep -R -h -E '^[[:space:]]*uses:[[:space:]]*[^[:space:]#]+' .github/workflows \
  | sed -E 's/^[[:space:]]*uses:[[:space:]]*([^[:space:]#]+).*$/\1/')

mapfile -t secret_files < <(find . -path './.git' -prune -o -type f \( \
  -name '*.jks' -o \
  -name '*.keystore' -o \
  -name '*.p12' -o \
  -name '*.pfx' -o \
  -name '*.pem' \
\) -print)

if [ "${#secret_files[@]}" -ne 0 ]; then
  printf 'Unexpected key/certificate container files found:\n' >&2
  printf '  %s\n' "${secret_files[@]}" >&2
  fail "release key material must remain outside source control"
fi

if grep -R -n -E -- '-----BEGIN ([A-Z0-9 ]+ )?PRIVATE KEY-----' \
  --exclude-dir=.git \
  --exclude='*.md' \
  .; then
  fail "private-key PEM material appears to be committed"
fi

printf 'GoreeCloud Gallery repository security guardrails passed.\n'
