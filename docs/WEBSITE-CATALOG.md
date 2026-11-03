# Website achievement catalogue — local SPEAR

Original base: canonical main 2736261. The changes have been carried onto
current canonical main 296ed5e for review; no publication or deployment follows.
The deployed renderer is the projection-only pilot. Export only successfully
registered provider definitions through the additive websiteAchievements() API.
The legacy Kotlin distribution is not this runtime and remains unchanged.
The catalogue never exports bundled defaults, performs grants, inspects private progress
or exposes commands. Provider rewards and completion totals are unknown to the
renderer: reward text remains empty and totals null. Capture current registered
state on the main thread; removed/replaced trees automatically update the next
snapshot. Source time is the real capture time. Bound to 1000 entries.

No local EARS/state helpers were found. The canonical pilot Maven verify passes
42 tests, including catalogue projection/privacy/empty-state regressions.
The bridge consumes this public API on the server thread and filters the DTO.
API/runtime and provider acceptance remain distinct from local proof. No server
change is performed and artifacts are unmerged local test artifacts only.

Local continuation: opt-in public completed labels for requested loaded individual
players. Offline/unloaded/shared teams stay unknown; no loading, progression,
reward, redemption or Discord operation is allowed. Catalogue scope and the
actual pilot maximum (100) support offline exclusion of roots, removed nodes
and shared teams. website.public-player-completions defaults false. This is
captured renderer completion, not provider reward entitlement. Existing provider
APIs/constructors are unchanged. No producer is activated. Manual SPEAR retained.

The console-only local restore helper is enthusia-export-catalogue. Its
website.local-catalogue-export flag also defaults false. It saves only current
registered definitions to website-export/registered-achievement-catalogue.json
by atomic replacement, cleans its own temporary file, and rejects an empty
registry. No original backup database is opened by that command. Local operator
instructions and backup requirements are in the website's LOCAL-PROFILE-EXPORTS.md.

Canonical pilot Maven verify now passes 45 tests, including public projection
and atomic local catalogue output. Verified against the compiled UAA 2.8.1 API;
the website offline reader independently checks its SQLite storage shape.
This is unmerged local source/build proof only. No server installation,
configuration activation or console command invocation occurred. On the updated
base, `mvn -o -f pilot/pom.xml verify` passed 51 tests with no failures or
skips. The live bridge, companion runtime, network pin and real player data have
not been verified. The website bridge currently consumes the catalogue API;
player completion export is not yet connected to it.
