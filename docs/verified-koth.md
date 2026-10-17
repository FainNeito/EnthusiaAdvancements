# Verified eKOTH integration (SPEAR)

Canonical main inspected: 13acd15. This branch changes the full Kotlin renderer;
the current Java pilot remains display-only and its API needs no mutation.
eKOTH uses the pilot's existing owner-bound ProjectionService directly and retains
the lifetime ownership/claims ledger. Percent progress from KothProgressionV1 is
0..100; pilot projection uses 0..1000. API failures preserve display state.

- EA-KOTH-001: The full renderer SHALL load an eKOTH-only tree on existing/new
  installs without overwriting local tree files. No AxKOTH/global guild-member
  fallback SHALL authorize its nodes.
- EA-KOTH-002: Ordinary GrantProgress calls, join-time root grants and admin grant
  commands SHALL NOT award provider-owned nodes. The eKOTH namespace SHALL remain
  protected even if its requirements are locally edited.
- EA-KOTH-003: BuildTree SHALL suppress its own reward payloads for the eKOTH
  namespace and provider-owned requirement type. Only KOTH's earned claim ledger
  SHALL authorize tags/items. Projection SHALL suppress reward replay.

Prove: existing AxKOTH listener grants every online guild member; join listener
grants roots; admin command grants arbitrary nodes. These paths are forbidden for
the new provider-owned nodes. AxKOTH support was subsequently retired: existing capture tokens remain parseable to preserve saved trees, but cannot progress, receive administrative grants or pay rewards. No old capture is translated into verified eKOTH credit. Existing tree layouts and player records are retained; the dedicated eKOTH tree supplies current challenges.
No historical red run is invented. No EARS/state helpers exist here.

Engine/arch: checked-in read-only API mirror excluded from shadowJar; optional
Paper join-classpath to EnthusiaKOTH; bounded periodic provider snapshots on the
server thread. Full renderer's HOCON tree and KOTH rules must remain synchronized
when staff changes thresholds. The pilot builds its display descriptions from
KOTH's actual rules. Provider map is validated before mutation, with no rewards.

Refine: added regressions for ordinary trigger/admin grant rejection and reward
suppression. Repaired a pre-existing stale backgroundTexture assertion (TreeDef
has no such property) and aligned JUnit API/engine/platform for Gradle 9. Existing
mixed versions failed discovery before any gameplay tests could execute.
Local dependencies come from the actual candidate Guilds/Market/Playtime artifacts
and existing network Diary/Currency/Commend outputs, not invented API bodies.
The pilot clean Maven verification remains a separate supported contract check.
Production, TEST activation, exact runtime provider availability and client
acceptance remain separate gates. Source changes require reviewed PRs and network
pin verification before a production release.
Merge review: Codacy identified a labeled return in the periodic provider hook.
Projection now uses a named method with an ordinary return; unavailable providers
still preserve display state and cannot fall back to unverified progression.
