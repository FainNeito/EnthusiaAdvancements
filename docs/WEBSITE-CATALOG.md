# Website achievement catalogue — local SPEAR

Base: canonical main 2736261. No PR or publication is authorized.
The deployed renderer is the projection-only pilot. Export only successfully
registered provider definitions through the additive websiteAchievements() API.
The legacy Kotlin distribution is not this runtime and remains unchanged.
Never export bundled defaults, perform grants, inspect private player progress
or expose commands. Provider rewards and completion totals are unknown to the
renderer: reward text remains empty and totals null. Capture current registered
state on the main thread; removed/replaced trees automatically update the next
snapshot. Source time is the real capture time. Bound to 1000 entries.

No local EARS/state helpers were found. The canonical pilot Maven verify passes
42 tests, including catalogue projection/privacy/empty-state regressions.
The bridge consumes this public API on the server thread and filters the DTO.
API/runtime and provider acceptance remain distinct from local proof. No server
change is performed and artifacts are unmerged local test artifacts only.
