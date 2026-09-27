# Cloud sessions

What runs here:
- Python: `uv run ruff check src tests foobar/wambridge_pcm_entry.py tools`, `uv run python -m unittest discover -s tests -v`, CLI `--help` smoke checks.
- Android (`mobile/`): `./mobile/gradlew -p mobile :app:lintDebug :app:testDebugUnitTest :app:assembleDebug`. Inspect APKs with `androguard`.
- foobar2000 SDK headers are at `$FOOBAR_SDK_ROOT` for reading only.

What runs only in GitHub Actions: the fb2k component (MSVC, `build.yml` on windows-2022), PyInstaller `.exe` helpers, signed APKs. Push a branch, open a PR and read the `build` / `android` job logs; treat that as the compiler.

No physical M5 here. Hardware acceptance (full track, seekbar, second track, pause/resume, stop/change, clean shutdown) stays with Bartek; say which of these a change needs.
