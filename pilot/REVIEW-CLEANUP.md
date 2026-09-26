# Projection API review cleanup

The renderer requires Java 25 for its pinned Paper 26.2 API. Version pilot.4 introduces owner-bound project/celebrate methods; update companion providers together. Deprecated ownerless methods fail closed rather than silently bypassing ownership. No server deployment is performed by this PR.

The entire progress map is snapshotted and null-checked before root grants, tab display, or node mutation. Existing unavailable/out-of-range sentinel handling is retained. Tests exercise wrong-owner rejection for projection and celebrations and no display mutation after a malformed map. Root and child automatic notification flags are independently checked.

Validation: renderer-boundary-red.log reproduced all four newly asserted API cases on the old implementation. Java 25 clean install now passes 7 tests, zero failures/errors/skips. Hosted verification is defined in .github/workflows/pilot-verify.yml and checks the exact head. Hosted success is not inferred from local success.

No reward execution, changes to player data, auto-merge, release, or custom-icon troubleshooting.

## September 26 CodeRabbit follow-up (SPEAR)

Spec:
- REQ-PILOT-REC-01: If replacement tab creation or registration fails, the renderer shall rebuild the previous tree from snapshotted inputs under its original owner and namespace, then propagate the replacement failure.
- REQ-PILOT-REC-02: If recovery fails, the renderer shall preserve both failures and shall not index a disposed tree.
- REQ-PILOT-VAL-01: If node descriptions, description entries, or frames are null, the API shall reject them with IllegalArgumentException.
- REQ-PILOT-NOTIFY-01: Historical projection displays shall disable automatic toast and chat flags for the root and every supported child frame.

Prove: The old implementation failed five focused assertions: the three null-input exception contracts and both replacement recovery paths. The UAA 2.8.1 frame adapter is stubbed only in test scope so the tests can construct real AdvancementDisplay objects without a Minecraft runtime; display flags are not mocked.

Engine/architecture: Private registration snapshots retain the cloned root icon and immutable definitions. All display inputs are constructed/validated before old-tab removal. New-tab registration failure disposes the failed tab; recovery creates a fresh tab, never reuses a disposed UAA tab. Cleanup/recovery failures remain suppressed on the original exception. Providers still own progress/rewards; no database migrations, new tracks, or deployments are included.

Refine: Java 25/Paper 26.2 clean verify passes 25 tests. Coverage includes creation/registration failure recovery, mutated provider inputs, recovery failure, first registration failure, pre-removal display validation, null fields, and actual automatic notification flags for TASK/GOAL/CHALLENGE. Hosted fork review/checks remain separate gates. Live Minecraft tab replacement/reload rendering and 26.3 are not verified by these unit tests.
