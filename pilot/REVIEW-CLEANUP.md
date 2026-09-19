# Projection API review cleanup

The renderer requires Java 25 for its pinned Paper 26.2 API. Version pilot.4 introduces owner-bound project/celebrate methods; update companion providers together. Deprecated ownerless methods fail closed rather than silently bypassing ownership. No server deployment is performed by this PR.

The entire progress map is snapshotted and null-checked before root grants, tab display, or node mutation. Existing unavailable/out-of-range sentinel handling is retained. Tests exercise wrong-owner rejection for projection and celebrations and no display mutation after a malformed map. Root and child automatic notification flags are independently checked.

Validation: renderer-boundary-red.log reproduced all four newly asserted API cases on the old implementation. Java 25 clean install now passes 7 tests, zero failures/errors/skips. Hosted verification is defined in .github/workflows/pilot-verify.yml and checks the exact head. Hosted success is not inferred from local success.

No reward execution, changes to player data, auto-merge, release, or custom-icon troubleshooting.
