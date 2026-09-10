# GoreeCloud Gallery — Feature Roadmap

**Lifecycle:** Development  
**Canonical Drive control:** `GoreeCloud/Feature Roadmap/GoreeCloud Gallery/FEATURE-ROADMAP.docx`  
**Authoritative project record:** `Project Specification — Gallery`  
**Canonical repository:** `GoreeCloud/goreecloud-gallery`

## Control rules

This repository roadmap and the canonical Drive roadmap must remain materially synchronized with the authoritative Gallery project specification, verified repository implementation state, applicable GoreeCloud platform-system requirements, and GoreeCloud Tasks Management.

A roadmap entry does not establish implementation, acceptance, Release Candidate, production, or Stable status. Status changes require the applicable source, exact-revision validation, representative runtime/device evidence, review, release, and production evidence.

| ID | Feature / obligation | Priority | Current state |
| --- | --- | --- | --- |
| FR-001 | Reconcile and maintain every current planned or recommended GoreeCloud Gallery feature from the authoritative project record and verified repository evidence in this roadmap. | High | Ongoing control |
| FR-002 | Move actionable feature obligations into GoreeCloud Tasks Management when required, preserving priority, dependency, and lifecycle disposition. | High | Ongoing control |
| FR-003 | Do not mark features implemented, complete, cancelled, or superseded without authoritative evidence and synchronized repository/Drive roadmap updates. | High | Ongoing control |
| FR-004 | Deliberately migrate the first-party native Gallery source to the current GLAZE UI V1.3 / 1.3.0 Adaptive Resonance authority using exact source anchors and governed semantic roles. | High | Development — Draft PR #74 exact head `85472206cc5aab479c3582c144574473ad4ae26a` passed Platform Contract `34529005727`, Native Android App `34529004725`, and Native Android Adapter `34529004728`. Stacked Draft PR #75 exact head `e0cdb4edca25824d34ceac71dbc4bd954fffd96a` passed Native Android App `34539219627` and Native Android Adapter `34539219661` after mapping photo-editor target, spacing, and shape inputs to V1.3 semantic roles. Draft PR #76 implementation head `f1845d0e56c5191507354c57186654c67133019e` passed Native Android App `34542534291` and Native Android Adapter `34542534299`; adaptive gutter outputs now consume the governed `SPACE_STANDARD_CLUSTER`, `SPACE_CONTENT`, `SPACE_SECTION`, and `SPACE_REGION` roles while width thresholds remain Gallery-local composition heuristics. Whole-application acceptance remains incomplete. |
| FR-005 | Complete fresh Gallery-specific V1.3 rendered, interaction, accessibility, adaptive/form-factor, representative-device/OEM/profile, performance, Human Visual Excellence, rollback, release, and production acceptance. | High | Required / not accepted |
| FR-006 | Complete first-party photo-editor acceptance for crop, rotate, flip, Reset, non-destructive Save copy, process recreation, orientation/output fidelity, metadata/color behavior, failure/cancellation cases, accessibility, and representative devices. | High | Development — Draft PR #75 exact head `e0cdb4edca25824d34ceac71dbc4bd954fffd96a` maps ordinary editor target, spacing, and control/container shape inputs to the current V1.3 repository-local semantic contract and passed Native Android App `34539219627` plus Native Android Adapter `34539219661`. Rendered/device acceptance, output fidelity, and broader editor acceptance remain incomplete. |
| FR-007 | Complete Recycle Bin and destructive-operation edge-case acceptance, including permission changes, mixed-media behavior, provider failure, process recreation, OEM/profile behavior, retention/expiry refresh, and recovery correctness. | High | Development — core tested paths exist; broader acceptance remains incomplete. |
| FR-008 | Integrate and accept applicable Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Identity, GoreeCloud Mesh, and GoreeCloud Manager authorities without converting unknown/unavailable evidence into positive status. | High | Blocked / integration acceptance pending |
| FR-009 | Complete protected signing/provenance, packaging/distribution, upgrade/recovery, rollback, Release Candidate qualification, production approval, and Stable qualification for the native Android application. | High | Planned / release gates open |

## Current sequencing recommendation

1. Finish the bounded V1.3 source migration and remove residual ungoverned Gallery-controlled presentation values.
2. Capture fresh rendered and accessibility evidence across Photos, Albums, Videos, Favorites, viewer, editor, Settings, selection, dialogs, and Recycle Bin.
3. Close representative-device/OEM/profile correctness gaps for editor, permissions, media orientation, Recycle Bin, and destructive operations.
4. Integrate the applicable GoreeCloud platform authorities and preserve fail-closed evidence semantics.
5. Complete recovery, signing/provenance, Release Candidate, production, and Stable gates.

This sequencing preserves the current offline-first Android media boundary and avoids treating UI modernization as authority to broaden MediaStore, network, cloud, profile, mutation, or protected-media access.
