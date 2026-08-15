# GoreeCloud Gallery Glaze UI Contract

## Purpose

This document defines the repository-local Glaze UI implementation and release-review contract for GoreeCloud Gallery. It supplements the authoritative GoreeCloud Glaze UI Design Language and Application Branding and User Interface Design standards; it does not replace them.

GoreeCloud Gallery is a GoreeCloud-maintained Android fork. Every Gallery-controlled user-facing surface must therefore use Glaze UI unless a material technical, platform, legal, licensing, accessibility, or interoperability constraint is explicitly documented as an approved exception.

## Product identity

The user-facing product identity is **GoreeCloud Gallery**. Upstream Fossify origin, copyright, licensing, and attribution remain preserved in source and legal records, but the ordinary application experience must not present Fossify as the primary product identity.

The application ID is `com.goreecloud.gallery`. GoreeCloud-owned branding, package metadata, launcher presentation, color identity, and visible product terminology must remain consistent with that identity.

## Glaze UI design principles for Gallery

Gallery applies Glaze UI to a local-media Android experience using the following product-specific interpretation:

- clear Material-derived structure and predictable Android interaction patterns;
- softened, rounded geometry for media thumbnails, controls, dialogs, menus, and structural surfaces where technically appropriate;
- layered surfaces and restrained depth that improve hierarchy without obscuring media content;
- selective translucency or visual depth only where readability, performance, and accessibility remain strong;
- purposeful gradients and polished surfaces only when they support hierarchy or GoreeCloud identity;
- high-quality light and dark presentation with coherent foreground/background contrast;
- ergonomic controls and touch targets appropriate for a mobile media application;
- smooth but non-essential motion that does not block task completion;
- privacy-conscious, offline-first presentation with no analytics, advertising, tracking, cloud account, or remote-content dependency.

Glaze UI does not require glass effects on every screen. Media remains the primary content. Decorative effects must remain subordinate to media visibility, readability, interaction clarity, accessibility, and device performance.

## Accepted Gallery-specific visual invariants

The following are release-significant GoreeCloud presentation decisions and must not silently regress:

- file thumbnails use rounded corners;
- folder thumbnails use the rounded GoreeCloud presentation;
- square-thumbnail selection controls removed by GoreeCloud do not return;
- thumbnail cropping remains disabled by the GoreeCloud policy;
- toolbar overflow menus remain readable in both light and dark appearance modes;
- dialogs preserve readable foreground/background contrast;
- destructive actions remain visually distinguishable and retain the intended confirmation behavior;
- the main folder view and opened media-folder view remain visibly consistent with the accepted GoreeCloud Gallery presentation;
- primary user-facing Fossify branding does not reappear during upstream synchronization.

Where an invariant can be represented as pure behavior, GoreeCloud-owned automated tests should protect it. Where Android framework rendering, accessibility services, device profiles, permissions, or media operations are required, the corresponding real-device gate remains mandatory.

## Theme and appearance contract

Gallery must provide coherent light and dark presentation on supported Android configurations. Theme work must be reviewed across primary and secondary surfaces, including:

- main folder and media-folder views;
- top app bars and overflow menus;
- search presentation;
- settings;
- dialogs and confirmation surfaces;
- empty and error states;
- media viewers and editing surfaces;
- share/open-with flows where Gallery controls presentation;
- recycle-bin/trash and destructive-operation flows;
- permission-related application surfaces.

A surface is not considered Glaze UI compliant merely because its background color matches the GoreeCloud palette. Typography, spacing, geometry, hierarchy, interaction states, contrast, and feedback must remain coherent as a system.

## Accessibility contract

Visual quality does not override accessibility. Stable-release review must include, where applicable:

- TalkBack identification of primary navigation and actionable controls;
- meaningful labels for icon-only controls;
- practical touch-target sizing;
- readable text and state contrast in light and dark themes;
- large-font and increased-display-size behavior;
- usable focus order for major screens and dialogs;
- motion that does not block task completion;
- safe behavior when translucency or advanced visual effects are unavailable or inappropriate.

Any Glaze UI treatment that materially weakens accessibility must be revised even if it matches the preferred aesthetic.

## Privacy and dependency boundary

Glaze UI assets and behavior must remain local to the application or its approved open-source build dependencies. Gallery must not introduce remote fonts, remote icon libraries, analytics resources, tracking pixels, advertising resources, or other network-hosted presentation dependencies.

The packaged application is expected to remain without `android.permission.INTERNET`. A visual enhancement must not weaken that offline/privacy boundary.

## Maintained-fork and upstream-sync review

Every upstream synchronization must include a user-interface regression review for newly introduced or restored:

- upstream names and logos;
- launcher or application icons;
- colors, themes, styles, and visual assets;
- settings and onboarding terminology;
- menus, dialogs, empty states, and errors;
- links or promotional references;
- accessibility regressions;
- square-thumbnail or other presentation controls intentionally removed by GoreeCloud.

Required legal attribution must remain preserved. Rebranding does not remove license obligations.

## Automated conformance boundary

Repository validation must require this contract to remain present. Source validation must fail closed when accepted GoreeCloud presentation invariants or GoreeCloud-owned behavioral-test sources disappear.

The ordinary acceptance workflow must verify that meaningful GoreeCloud-owned JVM tests actually execute. A successful Gradle task with `NO-SOURCE`, missing XML results, zero executed tests, or failing/error tests is not acceptable behavioral-test evidence.

Automated conformance complements rather than replaces real-device Glaze UI acceptance. Android rendering, TalkBack behavior, user/profile isolation, permission handling, destructive media operations, upgrade/recovery, and final signed-build presentation remain manual or device-dependent release gates.

## Exception model

No permanent Glaze UI exception is approved for GoreeCloud Gallery at this time.

If a future material constraint prevents compliance, the exception record must identify the affected surface or behavior, the requirement that cannot be met, the reason, user-visible impact, compensating or approved alternative, owner, review condition, and condition for removal. Convenience, schedule pressure, upstream defaults, or unfinished redesign work are not production exceptions.

## Stable-release boundary

Glaze UI compliance is a blocking Stable-release requirement. Stable promotion requires both automated conformance evidence and the applicable real-device visual/accessibility acceptance in `docs/STABLE-RELEASE-CHECKLIST.md`.

A green build, successful APK assembly, or successful signing workflow alone does not establish Glaze UI production readiness.
