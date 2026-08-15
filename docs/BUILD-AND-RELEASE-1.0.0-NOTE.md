# GoreeCloud Gallery 1.0.0 Build Note

The maintained build path now extends through gc.9. gc.9 changes package identity only: `VERSION_NAME=1.0.0` and `VERSION_CODE=10009`. It does not change Gallery behavior or Glaze UI presentation established and tested through gc.8.

The ordinary workflow builds `GoreeCloud-Gallery-1.0.0.apk` as an `acceptance-candidate`. The protected manual signing workflow builds the same semantic version as a `signed-release-candidate`. Neither classification is Stable.

The GoreeCloud-owned JVM behavioral suite is mandatory in both debug acceptance and release-candidate paths. A `NO-SOURCE` test task fails CI. JUnit XML must show the required GoreeCloud test class executed with at least three tests and zero failures/errors.

Stable promotion must use the exact accepted signed binary rather than rebuilding after approval. See `STABLE-CANDIDATE-1.0.0.md` for the promotion boundary and remaining external gates.
