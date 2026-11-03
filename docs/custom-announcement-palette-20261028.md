# Custom advancement announcement palette

## Requirements

- REQ-PALETTE-001: WHEN a non-holiday provider celebrates a task or goal THE SYSTEM SHALL display the announcement's advancement title and brackets in gold, regardless of explicit legacy or RGB title colors.
- REQ-PALETTE-002: WHEN a non-holiday provider celebrates a challenge THE SYSTEM SHALL display the announcement's advancement title and brackets in dark red, regardless of explicit legacy or RGB title colors.
- REQ-PALETTE-003: WHEN a holiday provider celebrates an advancement THE SYSTEM SHALL retain its existing announcement colors, including Watcher-specific title-colored brackets.
- REQ-PALETTE-004: WHEN a vanilla advancement is completed THE SYSTEM SHALL leave its announcement unchanged.
- REQ-PALETTE-005: WHEN formatting a custom announcement THE SYSTEM SHALL preserve player text, title content, hover and click events, text decorations, original input components and advancement progress and reward behavior.

## TDD task

TASK-PALETTE-001: Exercise the real UltimateAdvancementAPI announcement builder and parsed pilot title, then fix the non-holiday title subtree without recoloring player text, hover descriptions or holiday content.

Evidence: `PilotPlugin.celebrate` is the provider-owned call site; `ParsedAdvancementDisplay.getChatTitle` parses configured colors; `MinecraftAdvancementColors.format` presently recolors only the frame's original color. UAA 2.8.1 `Advancement.getAnnounceMessage(Player)` builds a white player/message component, opening bracket, parsed title and closing bracket, carrying hover events. Existing `MinecraftAdvancementColorsTest`, `ParsedAdvancementDisplayTest` and `UaaDisplayFixture` provide the JUnit/Mockito/NMS-adapter test seam. Vanilla announcements do not use the pilot's explicit provider celebrate path. Production Tags configuration contains explicit legacy-colored reward names; mirror folding may remove some entries, so no claim that each configured name appears separately.

Architecture: pilot presentation adapter only, no API descriptor, persistent data, rewards, configuration or Discord change. Legacy custom chat elements outside the advancement title remain unchanged. Holiday behavior is retained rather than repainted.

Prove: the actual UAA message construction with legacy and RGB title colors produced two assertion failures and zero errors before the fix; holiday preservation passed. This is a real behavioral red, not a fixture failure. Engine: recolor only the identified title subtree on a copied non-holiday UAA message; retain existing bracket palette, hover events, player text and holiday logic. Inputs with an unexpected message structure remain untouched apart from the existing frame-color mapping.

Verification: the pilot clean verify passed 47 tests with no failures, errors or skips. The existing external EARS validator passed on this requirements record; this repository has no project-local EARS validator or SPEAR state helper. A bytecode inspection of the retained production UAA 2.8.2 artifact, whose SHA-256 matches the production deployment receipt, confirms the same four-component announcement structure used by the tests. Hosted CI and in-game/client acceptance remain pending. No production or TEST operation is authorized by this source change.
