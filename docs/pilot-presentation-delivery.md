# Pilot celebration presentation delivery

## Spec

REQ-001: WHEN a provider requests an authorized live celebration THE SYSTEM SHALL color TASK and GOAL Minecraft announcement frame text gold and CHALLENGE frame text dark red on a component copy.

REQ-002: WHEN Discord delivery is enabled for a live celebration THE SYSTEM SHALL publish a bounded objective summary with gold TASK/GOAL or dark-red CHALLENGE accent and an optional framed icon.

REQ-003: IF the optional icon renderer is absent, incompatible, fails or reaches its eight-job limit THEN THE SYSTEM SHALL retain best-effort text delivery without changing progression or rewards.

REQ-004: WHEN progress is projected without an explicit live celebration THE SYSTEM SHALL remain silent and preserve all four public Node constructor descriptors and ten record components.

## Task and evidence

- [x] Inspect canonical main afe40d9, isolate the branch, and preserve the original dirty checkout.
- [x] Recover the previously local b56bd8d Discord bridge and presentation candidate from source, not from a patched JAR.
- [x] Revalidate the carried brownfield implementation through the existing Java 25 Maven suite: 40 tests passed with zero failures, errors or skips.
- [x] Inspect architecture: pilot-only platform adapters; no root Kotlin domain/application change, persistence, rewards, permissions or provider API change.
- [ ] Obtain exact-head hosted verification and substantive review; resolve actionable findings before merge.
- [ ] Check canonical merged source and monorepo pin/build delivery separately before any production artifact.
- [ ] Test actual Minecraft components and Discord rendering/delivery on an authorized test server; unit tests are not client acceptance.

Evidence: pilot/README.md defines the display-only owner boundary. PilotPlugin.celebrate and announce enforce explicit live celebration and viewer visibility. MinecraftAdvancementColorsTest covers frame colors, deep copies, nested text/click events and neutral/custom colors. DiscordAdvancementNoticeTest covers objective summaries and bounded plain text. DiscordAdvancementIconTest covers optional renderer absence and bounded PNG encoding. NodeApiCompatibilityTest preserves provider binary contracts. The actual compile profile is Paper 26.2.build.124-stable, UAA 2.8.1 and DiscordSRV 1.30.5; reflection targets the locally inspected InteractiveChat and addon 2026.1.1.0 signatures.

## SPEAR phases and limits

spec: requirements above. prove: carried regression/contract tests, re-executed now; no historical red run is invented. engine: existing local implementation imported onto current canonical main. arch: platform adapters stay in the pilot module and do not affect Kotlin domain/application. refine: clean Maven verification and whitespace inspection; hosted review remains open.

The repository has no local EARS validator or SPEAR state helper. The existing external SPEAR EARS validator is used for this scoped record. No state-helper pass is claimed. This is brownfield source delivery, not a claim that the older local implementation was originally developed test-first.

## Exact-head Codacy refinement

The first hosted verify passed at 38b8c0e. Codacy reported five method size/complexity findings plus two generic post-regex string-modification warnings. Delivery is now separated into channel resolution, embed construction, rendering, main-thread callback and sending; PNG dimension validation is separated from encoding. These are behavior-preserving adapter refactors, not changed retry or delivery semantics.

Security disposition for DiscordAdvancementNotice.clean: HEX_COLOR and LEGACY_COLOR use Matcher.replaceAll to remove presentation markup; neither performs a security validation or an allow/deny decision. Later fullwidth-at conversion and CR normalization are output sanitization. No filesystem, SQL, permission, URL authorization or other validated identifier consumes this output. The post-validation-modification warning is therefore a false positive in this context. Do not suppress the rules or claim the hosted gate passes before refreshed results and review accept this disposition.

## Delivery boundary

The Maven pilot and Kotlin root distribution are alternative profiles with the same plugin name. Never install both. This PR changes the pilot only. In-game titles/tooltips and resource-pack frame textures are not globally rewritten; only announcement component frame colors change. Full tooltips, custom item models, provider-owned completion state and rewards remain unchanged. No production upload, config edit, activation or restart is part of this delivery.
