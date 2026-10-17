# Requirements — CustomAdvancements Plugin

> Version: 1.0 | Last Updated: 2026-04-02
> Format: EARS (Easy Approach to Requirements Syntax)
> Related: `tech-stack.md`, `implementation.md`, `tasks.md`

---

## EARS Syntax Reference

| Pattern | Template | When to use |
|---------|----------|-------------|
| **Ubiquitous** | The `<system>` shall `<action>` | Always-on behavior |
| **Event-driven** | When `<event>`, the `<system>` shall `<action>` | Triggered behavior |
| **State-driven** | While `<state>`, the `<system>` shall `<action>` | Behavior during a state |
| **Unwanted** | If `<condition>`, then the `<system>` shall `<action>` | Error/edge-case handling |
| **Optional** | Where `<feature is enabled>`, the `<system>` shall `<action>` | Feature-flagged behavior |

---

## REQ-ARCH: Architecture

### REQ-KOTH-RETIRE: Remove AxKoth
WHEN EnthusiaAdvancements builds or starts THE SYSTEM SHALL operate without the AxKoth repository, API dependency or capture listener, preserve verified eKOTH progression, and reject progression/rewards from retired capture requirement types while keeping existing tree files parseable.

### REQ-BUILD-PT: Pinned playtime API
When the network supplies an explicit Playtime artifact, the build shall compile against that artifact without requiring a legacy version in its filename.

### REQ-ARCH-01: Hexagonal Architecture
The plugin shall follow hexagonal (ports and adapters) architecture with three layers:
- **Domain** — Pure Kotlin models with zero framework imports
- **Application** — Use cases (actions) and port interfaces
- **Infrastructure** — Adapters implementing ports using framework-specific code

### REQ-ARCH-02: Dependency Injection
The plugin shall use Nexus (`@Service`, `@Repository`, `@Component`) for all dependency injection. No manual wiring, no alternative DI frameworks.

### REQ-ARCH-03: Config via Nexus
The plugin shall use Nexus `@ConfigFile` for global settings and Typesafe Config (HOCON) for tree definitions.

### REQ-ARCH-04: Coroutine-First
The plugin shall use Nexus-provided `CoroutineScope` and `BukkitDispatcher` for all async operations. No custom coroutine scopes.

### REQ-ARCH-05: No Custom Persistence
The plugin shall NOT implement custom progress storage. UltimateAdvancementAPI handles all advancement persistence internally.

---

## REQ-BOOT: Bootstrap & Lifecycle

### REQ-BOOT-01: Plugin Initialization
When the server enables the plugin, the plugin shall:
1. Copy default tree config files to `trees/` if the directory does not exist
2. Create a `NexusContext` scanning `com.badgersmc.advancements`
3. Register Paper commands via `nexus.registerPaperCommands()`

### REQ-BOOT-02: Plugin Shutdown
When the server disables the plugin, the plugin shall call `nexus.close()`, which cancels coroutines, invokes `@PreDestroy` hooks, and clears the registry.

### REQ-BOOT-03: Dependency Declaration
The plugin shall declare `UltimateAdvancementAPI` as a required dependency and `LumaGuilds` as an optional dependency in `paper-plugin.yml` with `load: BEFORE`.

---

## REQ-TREE: Advancement Tree System

### REQ-TREE-01: Config-Driven Trees
The plugin shall load advancement trees from HOCON `.conf` files located in the `trees/` subdirectory of the plugin data folder. Each file defines exactly one tree.

### REQ-TREE-02: Tree Structure
The plugin shall require each tree definition to contain:
- `namespace` — unique identifier string
- `background-texture` — Minecraft texture path for the tab background
- `nodes` — list of advancement node definitions

### REQ-TREE-03: Node Definition
The plugin shall require each node definition to contain:
- `key` — unique within the tree
- `title` — display title (string)
- `description` — display description (list of strings)
- `icon` — Minecraft Material name
- `frame` — one of `TASK`, `GOAL`, `CHALLENGE`
- `x`, `y` — coordinates in the advancement GUI
- `max-progression` — integer, minimum 1

And optionally:
- `parent` — key of parent node (absent = root node)
- `requirement` — trigger definition (type + target)
- `rewards` — list of reward definitions
- `show-toast` — boolean, default true
- `announce-chat` — boolean, default true

### REQ-TREE-04: Single Root Enforcement
If a tree definition contains zero or more than one node without a `parent` field, then the plugin shall reject the tree with a descriptive error message.

### REQ-TREE-05: Parent Reference Validation
If a node references a `parent` key that does not exist in the same tree, then the plugin shall reject the tree with a descriptive error message.

### REQ-TREE-06: Cycle Detection
If a tree's parent references form a cycle, then the plugin shall reject the tree with a descriptive error message.

### REQ-TREE-07: Topological Build Order
When building a tree, the plugin shall sort nodes topologically (parents before children) to ensure UAAPI objects are created in valid order.

### REQ-TREE-08: UAAPI Registration
When a tree is built, the plugin shall:
1. Create an `AdvancementTab` via `api.createAdvancementTab(namespace, backgroundTexture)`
2. Create `RewardableRootAdvancement` for the root node
3. Create `RewardableAdvancement` for each child node
4. Call `tab.registerAdvancements(root, children)`
5. Call `tab.automaticallyShowToPlayers()` and `tab.automaticallyGrantRootAdvancement()`

### REQ-TREE-09: Tree Reload
When an admin executes the reload command, the plugin shall:
1. Unregister all existing tabs via `api.unregisterPluginAdvancementTabs()`
2. Clear all internal indexes
3. Re-parse all tree config files
4. Rebuild all trees via REQ-TREE-08

---

## REQ-PROG: Progression System

### REQ-PROG-01: Requirement Types
The plugin shall support the following requirement types for triggering progression:

| Type | Bukkit Event | Target Field |
|------|-------------|-------------|
| `BLOCK_BREAK` | `BlockBreakEvent` | Material name (e.g. `"DIAMOND_ORE"`) |
| `BLOCK_PLACE` | `BlockPlaceEvent` | Material name |
| `ENTITY_KILL` | `EntityDeathEvent` | EntityType name (e.g. `"ZOMBIE"`) |
| `CRAFT_ITEM` | `CraftItemEvent` | Material name |
| `PLAYER_KILL` | `PlayerDeathEvent` | `null` (any player) |
| `PLAYER_JOIN` | `PlayerJoinEvent` | `null` |

### REQ-PROG-02: Requirement Indexing
The plugin shall maintain a runtime index mapping `(RequirementType, target)` to `List<(namespace, key)>` for O(1) listener lookups.

### REQ-PROG-03: Event-Driven Progression
When a Bukkit event matching a requirement type fires, the plugin shall:
1. Extract the relevant player and target from the event
2. Query the requirement index for matching advancements
3. Call `advancement.incrementProgression(player)` for each match

### REQ-PROG-04: Async Progression
While processing progression updates, the plugin shall handle `CompletableFuture<ProgressionUpdateResult>` returned by UAAPI 3.0.0 without blocking the main thread.

---

## REQ-REWARD: Reward System

### REQ-REWARD-01: Reward Types
The plugin shall support the following reward types:

| Type | Fields | Behavior |
|------|--------|----------|
| `command` | `command: String` | Dispatches console command. `%player%` replaced with player name. |
| `experience` | `amount: Int` | Gives XP via `player.giveExp()` |
| `item` | `material: String`, `amount: Int` | Adds item to inventory |
| `message` | `text: String` | Sends MiniMessage-formatted message to player |

### REQ-REWARD-02: Reward Execution on Grant
When UAAPI calls `giveReward(Player)` on an advancement, the plugin shall execute all rewards defined for that advancement's node.

### REQ-REWARD-03: Reward via Subclass Override
The plugin shall implement rewards by extending `BaseAdvancement` and `RootAdvancement` (`non-sealed` in 3.0.0) and overriding `giveReward(Player)` to delegate to the `RewardExecutor` port.

---

## REQ-CMD: Commands

### REQ-CMD-01: Base Command
The plugin shall register the `/advancements` command via Nexus Paper command system.

### REQ-CMD-02: Reload Subcommand
When a sender with `advancements.admin` permission executes `/advancements reload`, the plugin shall reload all trees per REQ-TREE-09 and confirm completion to the sender.

### REQ-CMD-03: Grant Subcommand
When a sender with `advancements.admin` permission executes `/advancements grant <player> <tree> <key>`, the plugin shall grant the specified advancement to the target player.

If the advancement is not found, then the plugin shall reply with an error message including the attempted `tree:key`.

### REQ-CMD-04: List Subcommand
When a sender executes `/advancements list`, the plugin shall display all loaded tree namespaces.

---

## REQ-GUILD: LumaGuilds Integration

### REQ-GUILD-01: Optional Dependency
Where LumaGuilds is installed, the plugin shall register listeners for guild events to drive guild-related advancements.

Where LumaGuilds is not installed, the plugin shall skip guild hook registration with no errors.

### REQ-GUILD-02: Existing Guild Events
Where LumaGuilds is installed, the plugin shall listen for:
- `GuildCreatedEvent` -> `RequirementType.GUILD_CREATE`
- `GuildMemberJoinEvent` -> `RequirementType.GUILD_MEMBER_JOIN`

### REQ-GUILD-03: Future Guild Events (Prerequisite)
The `GuildLevelUpEvent` must be added to LumaGuilds before `RequirementType.GUILD_LEVEL` advancements can function. This is tracked as a **blocking external dependency**.

---

## REQ-CONFIG: Configuration

### REQ-CONFIG-01: Global Config
The plugin shall auto-generate an `advancements.conf` file via Nexus `@ConfigFile("advancements")` containing:
- `debug: Boolean` (default `false`)
- `treesDirectory: String` (default `"trees"`)

### REQ-CONFIG-02: Default Trees
When the `trees/` directory does not exist on first startup, the plugin shall copy bundled default tree configs (e.g. `combat.conf`, `exploration.conf`) into the directory.

---

## REQ-TEST: Testing

### REQ-TEST-01: TDD Workflow
The development team shall write tests BEFORE implementation for all domain models and application actions.

### REQ-TEST-02: Domain Model Tests
The plugin shall have unit tests covering:
- `TreeDef` validation (single root, orphan detection, cycle detection, topological sort)
- `Reward` sealed class construction
- `Requirement` type/target handling

### REQ-TEST-03: Action Tests
The plugin shall have unit tests covering:
- `BuildTree` — topological ordering, correct UAAPI constructor signatures
- `GrantProgress` — requirement index lookup, progression calls
- `TreeConfigParser` — valid HOCON parsing, error cases

### REQ-TEST-04: Integration Tests
The plugin shall have integration tests (with mocked UAAPI) verifying:
- End-to-end: config file -> parsed tree -> UAAPI adapter -> registered tab

---

## Acceptance Criteria Summary

| ID | Criteria | Verified By |
|----|----------|-------------|
| AC-01 | Custom tabs appear in advancement GUI (press L) | Manual server test |
| AC-02 | Killing entities increments combat tree progression | Manual server test |
| AC-03 | Breaking blocks increments exploration tree progression | Manual server test |
| AC-04 | Completing advancement shows toast + executes rewards | Manual server test |
| AC-05 | `/advancements reload` rebuilds trees without error | Manual server test |
| AC-06 | `/advancements grant` manually grants advancements | Manual server test |
| AC-07 | Invalid tree configs produce clear error messages | Unit test |
| AC-08 | All domain/action tests pass | CI / `./gradlew test` |
| AC-09 | Plugin starts cleanly without LumaGuilds installed | Manual server test |
