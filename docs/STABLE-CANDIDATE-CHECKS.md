# GoreeCloud Gallery Stable Candidate Checks

For the `1.0.0` candidate, CI must prove all source-controlled gates before any manual Stable decision:

- exact repository revision checkout;
- repository structure and security validation;
- deterministic upstream reconstruction through gc.9;
- source invariants including final version identity and Glaze UI behavior;
- GoreeCloud-owned behavioral tests with JUnit XML evidence;
- Android lint;
- APK assembly and packaged-version validation;
- offline permission boundary;
- package identity and signature validation;
- checksum and machine-readable build evidence;
- retained licensing and GoreeCloud notice files.

The signed candidate path adds long-lived signer verification against the approved public SHA-256 certificate fingerprint.

Passing these checks does not by itself approve Stable. Branch protection, signer/recovery administration, representative-device acceptance, accessibility, Glaze UI acceptance, upgrade/recovery validation, and final release evidence remain separate gates.
