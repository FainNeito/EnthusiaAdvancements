# Tasks — CustomAdvancements Plugin

- [x] EA-KOTH-RETIRE (REQ-KOTH-RETIRE): Removed AxKoth build repository/API and capture listener; stored requirement tokens/layouts remain parseable, with retired progress, administrative grants and reward payloads blocked. Verified eKOTH projection remains unchanged. SPEAR helpers are absent; this manual record is authoritative. Prove: 19 targeted cases yielded exactly the two new progression/reward failures before implementation. Refine: clean renderer test/shadowJar and dependency report passed with 53 cases, zero failures and no Ax dependencies; existing saved tree parsing is covered. Exact-head hosted CI, canonical network pin and deferred server acceptance remain separate gates. No server changes or player-data migration.

> Version: 1.0 | Last Updated: 2026-04-02
> Related: `tech-stack.md`, `requirements.md`, `implementation.md`

---

## How to Use This Document

### BUILD-PT: Network Playtime classpath (2026-10-04)

Spec: REQ-BUILD-PT; canonical main afe40d9 hardcodes playtime-plugin-3.5.16.jar while the pending canonical Playtime PR produces 3.7.2. PlaytimeListener directly references PlayerPlaytimeTickEvent, so the missing filename cannot satisfy compilation.

Prove: this is build infrastructure; no gameplay behavior changes or synthetic red/green unit test applies. The candidate network checkout produces playtime-plugin-3.7.2.jar and has no 3.5.16 artifact. Local full-network validation is separate from this static classpath evidence.

Engine/arch: accept ENTHUSIAPLAYTIME_JAR or enthusiaplaytime.jar, following the existing Market/Guilds build pattern. Preserve standalone discovery using a fileTree of actual Playtime artifacts. No plugin API, defaults, permissions or persistence changes.

Refine: Gradle dependency configuration passed. A real Gradle compile-classpath probe resolved the exact supplied 3.7.2 artifact and confirmed its PlayerPlaytimeTickEvent API (EXACT_PLAYTIME_CLASSPATH_OK). The candidate network's buildAll passed all 22 tasks, including this root renderer's Kotlin compilation and shadowJar with actual Guilds/Market/Playtime artifacts. Hosted pilot verification and Codacy passed at implementation head e8a9a7f; the documentation refinement does not alter build behavior. No EARS validator/state helpers are present in this checkout; no helper pass is claimed. Production remains untouched. Status: local SPEAR phases complete; canonical merge and final network hosted approval remain external gates.

Each task is a **self-contained work unit**. To start a task:

1. Read the task description and its **References** section
2. Open the referenced files/sections in the linked docs
3. Execute the **Steps** in order
4. Verify using the **Done When** criteria
5. Mark the task complete and move to the next

**For AI-assisted development:** Tell your LLM:
> "Start task `P1-T3`. Here are the project docs: [attach tech-stack.md, requirements.md, implementation.md, tasks.md]"

Each task has enough context to execute independently. No need to read the full docs — just the referenced sections.

---

## Status Legend

- `[ ]` Not started
- `[~]` In progress
- `[x]` Complete
- `[!]` Blocked (see note)

---

## Phase 1: Project Scaffolding & Domain Models

### P1-T1: Create Gradle Build File
`[x]` | Priority: Critical | Est: Trivial

**What:** Create `build.gradle.kts` and `settings.gradle.kts` for the project.

**References:**
- `tech-stack.md` > "Core Dependencies" table — all dependency coordinates and versions
- `tech-stack.md` > "Build Tools" table — Gradle and Shadow plugin versions
- `implementation.md` > Section 1 "Package Layout" — project structure

**Steps:**
1. Create `D:\BadgersMC-Dev\CustomAdvancements\settings.gradle.kts` with `rootProject.name = "CustomAdvancements"`
2. Create `D:\BadgersMC-Dev\CustomAdvancements\build.gradle.kts` with:
   - Kotlin JVM plugin 2.0.21
   - Shadow plugin 8.3.5
   - Java 21 toolchain
   - All repositories (mavenLocal, mavenCentral, papermc, frengor nexus)
   - All dependencies from tech-stack.md tables (runtime + test)
   - Shadow config: shade only nexus-core and nexus-paper, relocate to `com.badgersmc.advancements.lib.nexus`
3. Create `D:\BadgersMC-Dev\CustomAdvancements\src\main\resources\paper-plugin.yml` per `implementation.md` > Section 5e
4. Run `./gradlew build` to verify compilation (expect no sources yet, just confirm dependency resolution)

**Done When:** `./gradlew dependencies` resolves all dependencies without errors.

---

### P1-T2: Create `Reward.kt` Domain Model + Tests
`[x]` | Priority: High | Est: Trivial | TDD

**What:** Create the sealed `Reward` class and its test. **Write test first.**

**References:**
- `requirements.md` > REQ-REWARD-01 — reward type definitions
- `implementation.md` > Section 5a "Domain Layer" > Reward description

**Steps:**
1. **TEST FIRST:** Create `src/test/kotlin/com/badgersmc/advancements/domain/RewardTest.kt`
   - Test: Each variant (`Command`, `Experience`, `Item`, `Message`) constructs correctly
   - Test: Pattern matching via `when` exhaustively covers all variants
   - Test: `Command` stores command string, `Item` defaults amount to 1
2. Create `src/main/kotlin/com/badgersmc/advancements/domain/Reward.kt`
   - Sealed class with four data class variants
   - See `implementation.md` > Section 5a for exact structure
3. Run `./gradlew test` — all tests pass

**Done When:** `RewardTest` passes. `Reward` sealed class has 4 variants matching REQ-REWARD-01.

---

### P1-T3: Create `Requirement.kt` Domain Model + Tests
`[x]` | Priority: High | Est: Trivial | TDD

**What:** Create the `Requirement` data class and `RequirementType` enum. **Write test first.**

**References:**
- `requirements.md` > REQ-PROG-01 — requirement type table
- `requirements.md` > REQ-GUILD-02 — guild requirement types
- `implementation.md` > Section 5a "Domain Layer" > Requirement description

**Steps:**
1. **TEST FIRST:** Create `src/test/kotlin/com/badgersmc/advancements/domain/RequirementTest.kt`
   - Test: All enum values in `RequirementType` exist (BLOCK_BREAK, BLOCK_PLACE, ENTITY_KILL, CRAFT_ITEM, PLAYER_KILL, PLAYER_JOIN, GUILD_CREATE, GUILD_LEVEL, GUILD_MEMBER_JOIN)
   - Test: `Requirement` with null target is valid (for PLAYER_JOIN, PLAYER_KILL)
   - Test: `Requirement` default amount is 1
2. Create `src/main/kotlin/com/badgersmc/advancements/domain/Requirement.kt`
3. Run `./gradlew test`

**Done When:** `RequirementTest` passes. All types from REQ-PROG-01 and REQ-GUILD-02 are present.

---

### P1-T4: Create `AdvancementNodeDef.kt` Domain Model
`[x]` | Priority: High | Est: Trivial

**What:** Create the `AdvancementNodeDef` data class and `FrameType` enum.

**References:**
- `requirements.md` > REQ-TREE-03 — node definition fields (required + optional)
- `implementation.md` > Section 5a > AdvancementNodeDef description

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/domain/AdvancementNodeDef.kt`
   - Data class with all fields from REQ-TREE-03
   - `FrameType` enum: `TASK`, `GOAL`, `CHALLENGE`
   - `parentKey: String?` — null means root
   - `requirement: Requirement?` — null means no auto-trigger
   - `rewards: List<Reward>` — default empty list
   - `showToast: Boolean` — default true
   - `announceChat: Boolean` — default true

**Done When:** Compiles. Data class matches REQ-TREE-03 field list exactly.

---

### P1-T5: Create `TreeDef.kt` Domain Model + Tests
`[x]` | Priority: Critical | Est: Medium | TDD

**What:** Create `TreeDef` with validation and topological sort. **Write tests first.** This is the most complex domain model.

**References:**
- `requirements.md` > REQ-TREE-04 (single root), REQ-TREE-05 (parent refs), REQ-TREE-06 (cycles), REQ-TREE-07 (topo sort)
- `implementation.md` > Section 5a > TreeDef description
- `implementation.md` > Section 3 "Data Flow: Config File to Advancement GUI" — shows how sortedNodes() is used

**Steps:**
1. **TEST FIRST:** Create `src/test/kotlin/com/badgersmc/advancements/domain/TreeDefTest.kt`
   - Test `validate()`:
     - Valid tree (1 root, valid parents) -> empty error list
     - Zero roots -> error message mentioning "0 roots"
     - Two roots -> error message mentioning "2 roots"
     - Orphan node (parent key doesn't exist) -> error message mentioning the orphan key
     - *Stretch:* Cycle detection -> error message
   - Test `root`:
     - Returns the single node with `parentKey == null`
     - Throws if called on invalid tree
   - Test `sortedNodes()`:
     - Root always comes first
     - Every node appears after its parent
     - All nodes present in output
     - Linear chain: root -> A -> B -> C produces [root, A, B, C]
     - Branching tree: root -> A, root -> B, A -> C produces root first, A before C, B anywhere after root
2. Create `src/main/kotlin/com/badgersmc/advancements/domain/TreeDef.kt`
   - `validate(): List<String>` — check single root, valid parent refs, cycles (DFS-based)
   - `sortedNodes(): List<AdvancementNodeDef>` — Kahn's algorithm
   - `val root: AdvancementNodeDef get()` — `nodes.single { it.parentKey == null }`
3. Run `./gradlew test`

**Done When:** All `TreeDefTest` cases pass. Validation catches all four error classes from REQ-TREE-04/05/06. Topological sort satisfies REQ-TREE-07.

---

## Phase 2: Application Layer

### P2-T1: Create Port Interfaces
`[x]` | Priority: High | Est: Trivial

**What:** Create the three port interfaces in `application/ports/`.

**References:**
- `implementation.md` > Section 5b — all three port interfaces with full signatures
- `requirements.md` > REQ-PROG-02 — requirement indexing in AdvancementRegistry

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/application/ports/AdvancementRegistry.kt`
   - Interface with: `getAdvancement()`, `getTreeNamespaces()`, `isTreeLoaded()`, `findByRequirement()`
   - Return type for `getAdvancement` uses UAAPI's `Advancement` class (`com.fren_gor.ultimateAdvancementAPI.advancement.Advancement`)
2. Create `src/main/kotlin/com/badgersmc/advancements/application/ports/RewardExecutor.kt`
   - Interface with: `execute(player: Player, rewards: List<Reward>)`
3. Create `src/main/kotlin/com/badgersmc/advancements/application/ports/PluginHook.kt`
   - Interface with: `pluginName: String`, `isAvailable(): Boolean`, `onEnable()`

**Done When:** All three interfaces compile. Signatures match `implementation.md` Section 5b exactly.

---

### P2-T2: Create `BuildTree` Action + Tests
`[x]` | Priority: Critical | Est: Medium | TDD

**What:** Implement the core tree-building action that converts domain `TreeDef` into registered UAAPI objects. **Write tests first.**

**References:**
- `implementation.md` > Section 3 "Data Flow: Config File to Advancement GUI" — full flow diagram
- `implementation.md` > Section 5b > BuildTree description — step-by-step logic
- `implementation.md` > Section 8 "UAAPI 3.0.0 Display Construction" — how to build displays
- `tech-stack.md` > "UltimateAdvancementAPI 3.0.0-beta" > Constructor signatures — **critical: parent-first order**
- `requirements.md` > REQ-TREE-08 — registration sequence

**Steps:**
1. **TEST FIRST:** Create `src/test/kotlin/com/badgersmc/advancements/application/actions/BuildTreeTest.kt`
   - Use MockK to mock `UltimateAdvancementAPI`, `AdvancementTab`, `RewardExecutor`
   - Test: Root node creates `RewardableRootAdvancement` with correct (tab, key, maxProg, display) args
   - Test: Child nodes create `RewardableAdvancement` with correct (parent, key, maxProg, display) args — **parent first**
   - Test: `tab.registerAdvancements(root, children)` is called with correct objects
   - Test: `tab.automaticallyShowToPlayers()` and `tab.automaticallyGrantRootAdvancement()` are called
   - Test: Returns map of key -> Advancement for all nodes
   - Test: Nodes are built in topological order (parent advancement exists before child is created)
2. Create `src/main/kotlin/com/badgersmc/advancements/application/actions/BuildTree.kt`
   - Object with `fun execute(treeDef: TreeDef, api: UltimateAdvancementAPI, rewardExecutor: RewardExecutor): BuildTreeResult`
   - `BuildTreeResult` data class: `tab: AdvancementTab`, `advancements: Map<String, Advancement>`
   - Uses `AdvancementDisplayBuilder` per `implementation.md` Section 8
   - **Verify `AdvancementDisplayBuilder` API** via Context7 or reference source before implementing
3. Run `./gradlew test`

**Done When:** `BuildTreeTest` passes. Correct UAAPI 3.0.0 constructor order verified.

---

### P2-T3: Create `GrantProgress` Action + Tests
`[x]` | Priority: High | Est: Trivial | TDD

**What:** Implement the progression increment action. **Write test first.**

**References:**
- `implementation.md` > Section 4 "Data Flow: Bukkit Event to Progression Increment" — full flow
- `requirements.md` > REQ-PROG-03 — event-driven progression steps
- `requirements.md` > REQ-PROG-04 — async CompletableFuture handling

**Steps:**
1. **TEST FIRST:** Create `src/test/kotlin/com/badgersmc/advancements/application/actions/GrantProgressTest.kt`
   - Mock `AdvancementRegistry` and UAAPI `Advancement`
   - Test: Calls `findByRequirement(type, target)` on registry
   - Test: For each match, calls `getAdvancement()` then `incrementProgression(player)`
   - Test: No matches -> no progression calls
   - Test: Multiple matches -> all get incremented
2. Create `src/main/kotlin/com/badgersmc/advancements/application/actions/GrantProgress.kt`
   - Object with `fun execute(registry: AdvancementRegistry, type: RequirementType, target: String?, player: Player)`
3. Run `./gradlew test`

**Done When:** `GrantProgressTest` passes. Handles zero, one, and multiple matches correctly.

---

### P2-T4: Create `ReloadTrees` Action
`[x]` | Priority: Medium | Est: Trivial

**What:** Implement the reload orchestrator action.

**References:**
- `implementation.md` > Section 7 "Reload Sequence" — full flow diagram
- `requirements.md` > REQ-TREE-09 — reload steps

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/application/actions/ReloadTrees.kt`
   - Delegates to `UltimateAdvancementAdapter.reloadAll()` (will be implemented in Phase 4)
   - This is a thin orchestrator — the real logic lives in the adapter

**Done When:** Compiles. Follows the sequence in `implementation.md` Section 7.

---

## Phase 3: Config System

### P3-T1: Create `AdvancementsConfig.kt`
`[x]` | Priority: High | Est: Trivial

**What:** Create the global config class using Nexus `@ConfigFile`.

**References:**
- `requirements.md` > REQ-CONFIG-01 — fields and defaults
- `tech-stack.md` > Nexus > `@ConfigFile`, `@Comment` annotations
- `implementation.md` > Section 5d > AdvancementsConfig

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/config/AdvancementsConfig.kt`
   - `@ConfigFile("advancements")`
   - `var debug: Boolean = false`
   - `var treesDirectory: String = "trees"`
   - Add `@Comment` annotations for each field

**Done When:** Compiles. Nexus will auto-discover and generate `advancements.conf` at runtime.

---

### P3-T2: Create Default Tree Config Files
`[x]` | Priority: High | Est: Small

**What:** Create bundled default HOCON tree config files.

**References:**
- `requirements.md` > REQ-TREE-02, REQ-TREE-03 — structure and fields
- `requirements.md` > REQ-CONFIG-02 — default trees on first startup
- `implementation.md` > Section 9 "Requirement Index Structure" — shows example data that trees should produce

**Steps:**
1. Create `src/main/resources/trees/combat.conf` — combat advancement tree with:
   - Root: "Combat" (IRON_SWORD icon)
   - Child: "Zombie Slayer" — kill 10 zombies (ENTITY_KILL, ZOMBIE)
   - Child: "Bone Collector" — kill 20 skeletons (ENTITY_KILL, SKELETON)
   - Child: "Wither Storm" — kill 1 wither, CHALLENGE frame, rewards
2. Create `src/main/resources/trees/exploration.conf` — exploration tree with:
   - Root: "Exploration" (COMPASS icon)
   - Child: "First Steps" — join server (PLAYER_JOIN)
   - Child: "Miner" — break 50 stone (BLOCK_BREAK, STONE)
   - Child: "Diamond Hunter" — break 10 diamond ore (BLOCK_BREAK, DIAMOND_ORE)

**Done When:** Both `.conf` files parse correctly as HOCON. Every node has all required fields from REQ-TREE-03.

---

### P3-T3: Create `TreeConfigParser.kt` + Tests
`[x]` | Priority: Critical | Est: Medium | TDD

**What:** Service that reads HOCON tree files and produces `List<TreeDef>`. **Write tests first.**

**References:**
- `implementation.md` > Section 5d > TreeConfigParser description
- `implementation.md` > Section 3 — TreeConfigParser is the first step in the data flow
- `requirements.md` > REQ-TREE-01 through REQ-TREE-06 — all config validation requirements
- P3-T2 config files — use as test fixtures

**Steps:**
1. **TEST FIRST:** Create `src/test/kotlin/com/badgersmc/advancements/config/TreeConfigParserTest.kt`
   - Test: Parse valid HOCON file -> produces correct `TreeDef` with all fields
   - Test: Missing required field (e.g. no `namespace`) -> throws/returns error
   - Test: Unknown frame type -> error
   - Test: Empty nodes list -> error
   - Test: Multiple `.conf` files in directory -> returns list of all parsed trees
   - Use temp directories with test config files
2. Create `src/main/kotlin/com/badgersmc/advancements/config/TreeConfigParser.kt`
   - `@Service` annotation
   - Constructor injection: `AdvancementsConfig`
   - `fun parseAll(pluginDataFolder: Path): List<TreeDef>`
   - `fun parseFile(path: Path): TreeDef` (internal, parses one file)
   - Uses `com.typesafe.config.ConfigFactory.parseFile()`
   - Calls `treeDef.validate()` and logs errors for invalid trees
3. Run `./gradlew test`

**Done When:** `TreeConfigParserTest` passes. Parses the default tree configs from P3-T2 correctly.

---

## Phase 4: Infrastructure — UAAPI Adapter & Rewards

### P4-T1: Create `RewardableAdvancement.kt` and `RewardableRootAdvancement.kt`
`[x]` | Priority: Critical | Est: Small

**What:** Create the two UAAPI subclasses that override `giveReward()`.

**References:**
- `implementation.md` > Section 5c > RewardableAdvancement — full class with constructor
- `tech-stack.md` > UAAPI 3.0.0 > Constructor signatures — **parent-first for BaseAdvancement**
- `requirements.md` > REQ-REWARD-03 — reward via subclass override

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/advancement/RewardableAdvancement.kt`
   - Extends `BaseAdvancement(parent, key, maxProgression, display)`
   - Additional constructor params: `rewards: List<Reward>`, `rewardExecutor: RewardExecutor`
   - Overrides `giveReward(player: Player)` -> `rewardExecutor.execute(player, rewards)`
2. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/advancement/RewardableRootAdvancement.kt`
   - Same pattern, extends `RootAdvancement(advancementTab, key, maxProgression, display)`
3. **Verify** the override compiles against UAAPI 3.0.0. If `giveReward` signature changed, check reference source: `D:\BadgersMC-Dev\_api-reference\UltimateAdvancementAPI-3.0.0\Common\src\main\java\com\fren_gor\ultimateAdvancementAPI\advancement\Advancement.java` line ~738

**Done When:** Both classes compile against UAAPI 3.0.0. `giveReward` override delegates to `RewardExecutor`.

---

### P4-T2: Create `BukkitRewardExecutor.kt`
`[x]` | Priority: High | Est: Small

**What:** Implement the `RewardExecutor` port using Bukkit API.

**References:**
- `implementation.md` > Section 5c > BukkitRewardExecutor — behavior for each reward type
- `requirements.md` > REQ-REWARD-01 — reward type table with expected behaviors

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/rewards/BukkitRewardExecutor.kt`
   - `@Service` annotation
   - Implements `RewardExecutor`
   - Constructor injection: `JavaPlugin` (for logging)
   - `when (reward)` exhaustive match on all 4 sealed variants:
     - `Command` -> `Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.name))`
     - `Experience` -> `player.giveExp(amount)`
     - `Item` -> `player.inventory.addItem(ItemStack(Material.valueOf(material), amount))`
     - `Message` -> `player.sendMessage(MiniMessage.miniMessage().deserialize(text))`
   - Wrap each reward execution in try-catch, log errors, continue with next reward

**Done When:** Compiles. All 4 reward types handled. Error in one reward doesn't prevent others from executing.

---

### P4-T3: Create `UltimateAdvancementAdapter.kt`
`[x]` | Priority: Critical | Est: Large

**What:** The central adapter service that bridges Nexus DI to UAAPI.

**References:**
- `implementation.md` > Section 5c > UltimateAdvancementAdapter — full class structure
- `implementation.md` > Section 6 "Bootstrap Sequence" — when initialize() runs
- `implementation.md` > Section 7 "Reload Sequence" — reloadAll() flow
- `implementation.md` > Section 9 "Requirement Index Structure" — index format
- `requirements.md` > REQ-PROG-02 — requirement indexing
- P2-T2 `BuildTree` action — called from `loadAllTrees()`

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/advancement/UltimateAdvancementAdapter.kt`
   - `@Service` annotation
   - Implements `AdvancementRegistry` port
   - Constructor injection: `JavaPlugin`, `AdvancementsConfig`, `TreeConfigParser`, `RewardExecutor`
   - Private fields: `api: UltimateAdvancementAPI` (lateinit), `tabs`, `advancementMap`, `requirementIndex`
   - `@PostConstruct fun initialize()` — gets API instance, calls `loadAllTrees()`
   - `fun loadAllTrees()` — parses configs, calls `BuildTree.execute()` for each, populates indexes
   - `fun reloadAll()` — unregisters all tabs, clears state, reloads
   - Implement all `AdvancementRegistry` methods
   - Build requirement index from `TreeDef` nodes that have non-null requirements
2. Add error handling per `implementation.md` Section 11 — log and skip invalid trees

**Done When:** Compiles. Implements full `AdvancementRegistry` interface. Index structure matches `implementation.md` Section 9.

---

## Phase 5: Event Listeners

### P5-T1: Create `EntityKillListener.kt`
`[x]` | Priority: High | Est: Small

**What:** Listener for entity kills to drive combat advancements.

**References:**
- `implementation.md` > Section 4 "Data Flow: Bukkit Event to Progression Increment" — full flow
- `implementation.md` > Section 5c > Listeners — pattern description
- `requirements.md` > REQ-PROG-01 — ENTITY_KILL row
- `requirements.md` > REQ-PROG-03 — event-driven progression steps

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/listeners/EntityKillListener.kt`
   - `@Component` annotation, implements `Listener`
   - Constructor injection: `JavaPlugin`, `AdvancementRegistry`
   - `@PostConstruct fun register()` -> `Bukkit.getPluginManager().registerEvents(this, plugin)`
   - `@EventHandler fun onEntityDeath(event: EntityDeathEvent)`:
     - `val killer = event.entity.killer ?: return`
     - `val entityType = event.entity.type.name`
     - Call `GrantProgress.execute(registry, ENTITY_KILL, entityType, killer)`

**Done When:** Compiles. Follows the exact data flow in `implementation.md` Section 4.

---

### P5-T2: Create `BlockBreakListener.kt`
`[x]` | Priority: High | Est: Small

**What:** Listener for block breaks to drive mining/exploration advancements.

**References:**
- Same pattern as P5-T1
- `requirements.md` > REQ-PROG-01 — BLOCK_BREAK row

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/listeners/BlockBreakListener.kt`
   - Same `@Component` + `Listener` pattern as P5-T1
   - `@EventHandler fun onBlockBreak(event: BlockBreakEvent)`:
     - `val material = event.block.type.name`
     - Call `GrantProgress.execute(registry, BLOCK_BREAK, material, event.player)`

**Done When:** Compiles. Matches pattern from P5-T1.

---

### P5-T3: Create `CraftItemListener.kt`
`[x]` | Priority: Medium | Est: Small

**What:** Listener for item crafting.

**References:**
- Same pattern as P5-T1
- `requirements.md` > REQ-PROG-01 — CRAFT_ITEM row

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/listeners/CraftItemListener.kt`
   - `@EventHandler fun onCraft(event: CraftItemEvent)`:
     - `val player = event.whoClicked as? Player ?: return`
     - `val material = event.recipe.result.type.name`
     - Call `GrantProgress.execute(registry, CRAFT_ITEM, material, player)`

**Done When:** Compiles.

---

### P5-T4: Create `PlayerJoinListener.kt`
`[x]` | Priority: Medium | Est: Small

**What:** Listener for player joins. Also handles showing tabs.

**References:**
- Same pattern as P5-T1
- `requirements.md` > REQ-PROG-01 — PLAYER_JOIN row (target = null)

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/listeners/PlayerJoinListener.kt`
   - `@EventHandler fun onPlayerJoin(event: PlayerJoinEvent)`:
     - Call `GrantProgress.execute(registry, PLAYER_JOIN, null, event.player)`
   - Note: Tab showing is handled by UAAPI's `automaticallyShowToPlayers()` set in BuildTree

**Done When:** Compiles.

---

## Phase 6: Commands

### P6-T1: Create `AdvancementCommand.kt`
`[x]` | Priority: High | Est: Small

**What:** Register the `/advancements` command with reload, grant, and list subcommands.

**References:**
- `implementation.md` > Section 5e — full command class with all subcommands
- `tech-stack.md` > Nexus > Key annotations table — `@Command`, `@Subcommand`, `@Arg`, `@Context`, `@Permission`, `@Async`
- `requirements.md` > REQ-CMD-01 through REQ-CMD-04 — all command requirements

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/commands/AdvancementCommand.kt`
   - `@Command(name = "advancements")`
   - Constructor injection: `UltimateAdvancementAdapter`
   - Subcommand `reload`: `@Async`, `@Permission("advancements.admin")` — calls `adapter.reloadAll()`
   - Subcommand `grant`: `@Permission("advancements.admin")` — `@Arg("player")`, `@Arg("tree")`, `@Arg("key")`
     - If not found: error message per REQ-CMD-03
     - If found: `adv.grant(player, true)`
   - Subcommand `list`: no permission — iterates `adapter.getTreeNamespaces()`
   - Use Adventure `Component` for messages (not legacy `String`)

**Done When:** Compiles. All three subcommands implemented per REQ-CMD-01 through REQ-CMD-04.

---

## Phase 7: Plugin Hooks

### P7-T1: Create `LumaGuildsHook.kt`
`[x]` | Priority: Low | Est: Small | Partially Blocked

**What:** Optional LumaGuilds integration via Bukkit custom events.

**References:**
- `implementation.md` > Section 5c > LumaGuildsHook description
- `implementation.md` > Section 10 "External Dependencies" — communication via Bukkit events
- `requirements.md` > REQ-GUILD-01 (optional dep), REQ-GUILD-02 (existing events), REQ-GUILD-03 (missing events)

**Blocked by:** `GuildLevelUpEvent` not yet added to LumaGuilds (REQ-GUILD-03). Can still implement for existing events.

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/infrastructure/plugins/LumaGuildsHook.kt`
   - `@Component` annotation, implements `PluginHook` and `Listener`
   - `isAvailable()` -> `Bukkit.getPluginManager().isPluginEnabled("LumaGuilds")`
   - `@PostConstruct` -> only registers events if `isAvailable()`
   - Event handlers for existing events (GuildCreatedEvent, GuildMemberJoinEvent)
   - Map to `RequirementType.GUILD_CREATE`, `GUILD_MEMBER_JOIN`
   - Note: LumaGuilds event classes are `compileOnly` — use try-catch for ClassNotFoundException at registration time in case plugin isn't present

**Done When:** Compiles with and without LumaGuilds on classpath. Gracefully skips if not available. Existing guild events trigger progression.

---

## Phase 8: Bootstrap & Integration

### P8-T1: Create `CustomAdvancementsPlugin.kt`
`[x]` | Priority: Critical | Est: Small

**What:** Main plugin entry point that wires everything together.

**References:**
- `implementation.md` > Section 6 "Bootstrap Sequence" — full startup diagram
- `tech-stack.md` > Nexus > Bootstrap pattern — exact code for NexusContext.create()
- `requirements.md` > REQ-BOOT-01, REQ-BOOT-02, REQ-BOOT-03

**Steps:**
1. Create `src/main/kotlin/com/badgersmc/advancements/CustomAdvancementsPlugin.kt`
   - Extends `JavaPlugin`
   - `onEnable()`:
     1. Copy default tree configs if `trees/` doesn't exist (per REQ-CONFIG-02)
     2. `NexusContext.create(...)` with basePackage, classLoader, configDirectory, contextName, externalBeans
     3. `nexus.registerPaperCommands(...)` per `tech-stack.md` bootstrap pattern
   - `onDisable()`:
     1. `nexus.close()`
2. Verify `paper-plugin.yml` from P1-T1 matches REQ-BOOT-03

**Done When:** Plugin compiles as shadow JAR. `./gradlew shadowJar` produces a valid JAR.

---

### P8-T2: Full Integration Test
`[ ]` | Priority: Critical | Est: Medium

**What:** Deploy to test server and verify all acceptance criteria.

**References:**
- `requirements.md` > "Acceptance Criteria Summary" table — all 9 criteria
- `implementation.md` > Section 3, 4 — expected data flows

**Steps:**
1. Build: `./gradlew shadowJar`
2. Install UAAPI 3.0.0-beta on Paper 1.21.1 test server
3. Drop plugin JAR in `plugins/`
4. Start server, verify:
   - `[ ]` AC-01: Custom tabs in advancement GUI
   - `[ ]` AC-02: Entity kills increment progression
   - `[ ]` AC-03: Block breaks increment progression
   - `[ ]` AC-04: Completion shows toast + runs rewards
   - `[ ]` AC-05: `/advancements reload` works
   - `[ ]` AC-06: `/advancements grant` works
   - `[ ]` AC-07: Invalid config produces clear error (test by breaking a tree file)
   - `[ ]` AC-08: `./gradlew test` passes
   - `[ ]` AC-09: Server starts without LumaGuilds (remove LumaGuilds, restart)

**Done When:** All 9 acceptance criteria checked off.

---

## Task Dependency Graph

```
P1-T1 (Gradle)
  |
  +-- P1-T2 (Reward)
  +-- P1-T3 (Requirement)
  +-- P1-T4 (NodeDef) -- depends on P1-T2, P1-T3
  +-- P1-T5 (TreeDef) -- depends on P1-T4
        |
        +-- P2-T1 (Ports)
        |     |
        |     +-- P2-T2 (BuildTree) -- depends on P4-T1
        |     +-- P2-T3 (GrantProgress)
        |     +-- P2-T4 (ReloadTrees)
        |
        +-- P3-T1 (Config class)
        +-- P3-T2 (Default configs)
        +-- P3-T3 (Config parser) -- depends on P3-T1, P3-T2
              |
              +-- P4-T1 (Rewardable advancements) -- depends on P1-T2
              +-- P4-T2 (Reward executor) -- depends on P1-T2
              +-- P4-T3 (UAAPI Adapter) -- depends on P2-T2, P3-T3, P4-T1, P4-T2
                    |
                    +-- P5-T1..T4 (Listeners) -- depend on P2-T3
                    +-- P6-T1 (Commands)
                    +-- P7-T1 (LumaGuilds hook) -- partially blocked
                    +-- P8-T1 (Bootstrap) -- depends on everything above
                          |
                          +-- P8-T2 (Integration test)
```

---

## Quick Reference: Task Count by Phase

| Phase | Tasks | Critical | TDD |
|-------|-------|----------|-----|
| 1: Domain Models | 5 | 2 | 3 |
| 2: Application | 4 | 1 | 2 |
| 3: Config | 3 | 1 | 1 |
| 4: Infrastructure | 3 | 2 | 0 |
| 5: Listeners | 4 | 0 | 0 |
| 6: Commands | 1 | 0 | 0 |
| 7: Plugin Hooks | 1 | 0 | 0 |
| 8: Integration | 2 | 2 | 0 |
| **Total** | **23** | **8** | **6** |
