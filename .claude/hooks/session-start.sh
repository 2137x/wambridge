#!/usr/bin/env bash
# SessionStart, cloud sessions only; mirrors .github/workflows/copilot-setup-steps.yml.
# Never fails the session. Heavy builds (foo_out_wam.dll via MSVC, signed APK)
# stay in GitHub Actions.
[ "${CLAUDE_CODE_REMOTE:-}" = "true" ] || exit 0
cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

persist() { [ -n "${CLAUDE_ENV_FILE:-}" ] && echo "$1" >> "$CLAUDE_ENV_FILE"; }

# Python package, ruff and pyinstaller (Python 3.14 via uv).
{ uv sync --locked -q && echo "uv sync: ok"; } || echo "uv sync failed" >&2 &

# Android adapter (mobile/): compileSdk 37, build-tools 36.
if [ -z "${ANDROID_HOME:-}" ] && [ -d /opt/android-sdk ]; then
  export ANDROID_HOME=/opt/android-sdk
  persist 'export ANDROID_HOME=/opt/android-sdk'
fi
sdkm="${ANDROID_HOME:-/nonexistent}/cmdline-tools/latest/bin/sdkmanager"
if [ -x "$sdkm" ] && [ ! -d "$ANDROID_HOME/build-tools/36.0.0" ]; then
  "$sdkm" "build-tools;36.0.0" >/dev/null || echo "sdkmanager: build-tools 36 failed" >&2
fi

# Android skills from the Android CLI, user-level so the repo stays clean.
# Installed in parallel (~11 s on a fresh VM, 0 s once present) and waited
# for, so the first turn already has them.
android_cli=$(command -v android || echo "${ANDROID_HOME:-/opt/android-sdk}/cmdline-tools/latest/bin/android")
if [ -x "$android_cli" ]; then
  for skill in android-cli testing-setup edge-to-edge r8-analyzer android-intent-security android-permissions-security; do
    [ -d "$HOME/.claude/skills/$skill" ] && continue
    "$android_cli" skills add --agent=claude-code "$skill" >/dev/null 2>&1 \
      || echo "android skill failed: $skill" >&2 &
  done
  wait
fi

# foobar2000 SDK headers, outside the repo so they never get committed.
sdk="$HOME/.cache/foobar-sdk"
if [ ! -d "$sdk" ] && command -v 7z >/dev/null; then
  if ! { mkdir -p "$sdk" \
      && curl -fsSL --retry 3 https://www.foobar2000.org/downloads/SDK-2025-03-07.7z -o /tmp/foobar-sdk.7z \
      && 7z x /tmp/foobar-sdk.7z -o"$sdk" -y >/dev/null; }; then
    echo "foobar SDK download failed (is www.foobar2000.org allowed?)" >&2
    rm -rf "$sdk"
  fi
  rm -f /tmp/foobar-sdk.7z
fi
proj=$(find "$sdk" -type f -name foobar2000_SDK.vcxproj -print -quit 2>/dev/null)
if [ -n "$proj" ]; then
  persist "export FOOBAR_SDK_ROOT=$(dirname "$(dirname "$(dirname "$proj")")")"
fi

wait
exit 0
