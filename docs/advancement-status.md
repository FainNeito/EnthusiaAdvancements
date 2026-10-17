# Advancement Implementation Status

> Last Updated: 2026-04-14
> For use as context in continuation prompts

## Architecture

- **CustomAdvancements** (`D:\BadgersMC-Dev\CustomAdvancements`) — Kotlin plugin, Nexus DI, UAAPI 3.0.0
- **enthusia-network** (`D:\BadgersMC-Dev\enthusia-network`) — monorepo with git submodules for all plugins
- All listeners use `Class.forName()` + inner handler class pattern for optional deps
- External plugin JARs referenced as `compileOnly(files("..."))` in `build.gradle.kts`
- Tree configs are HOCON `.conf` files in `src/main/resources/trees/`

## Current Trees (7 total)

| Tree | File | Nodes |
|------|------|-------|
| combat | combat.conf | 4 (root, zombie_slayer, bone_collector, wither_storm) |
| exploration | exploration.conf | 10 (root, first_steps, miner, diamond_hunter, time_well_spent, dedicated, no_life, rough_landing, caught_in_4k, the_ghost) |
| guilds | guilds.conf | 30 nodes (fully complete) |
| diary | diary.conf | 10 nodes (+stubborn) |
| economy | economy.conf | 7 nodes (root, printing_money, safely_secured, liquid_assets, philanthropist, fat_fingers, rock_bottom) |
| reputation | reputation.conf | 14 nodes (+trusted, bad_reputation, villain, public_enemy, public_record, fame_or_shame, change_of_heart) |
| shops | shops.conf | 4 nodes (root, monopoly, sold_out, bad_investment) |

## Current RequirementTypes (58 total)

Bukkit native: BLOCK_BREAK, BLOCK_PLACE, ENTITY_KILL, CRAFT_ITEM, PLAYER_KILL, PLAYER_JOIN, PLAYER_DEATH
LumaGuilds individual: GUILD_CREATE, GUILD_MEMBER_JOIN, GUILD_BANNER_SET, GUILD_HOME_SET, GUILD_VAULT_PLACED, GUILD_LEVEL_UP, GUILD_WAR_DECLARED, GUILD_WAR_WON, GUILD_MEMBER_REMOVED, GUILD_OWNERSHIP_TRANSFER, GUILD_FRIENDLY_FIRE
LumaGuilds collective: GUILD_COLLECTIVE_SMELT, GUILD_COLLECTIVE_BLOCK_BREAK, GUILD_COLLECTIVE_CROP_HARVEST, GUILD_COLLECTIVE_MOB_KILL, GUILD_COLLECTIVE_BOSS_KILL
ItemShops: GUILD_SHOP_SALE, GUILD_SHOP_PURCHASE
eKOTH: EKOTH_VERIFIED_CHALLENGE (authoritative provider projection). KOTH_CAPTURE and KOTH_CONSECUTIVE_CAPTURE are retired parsing-only tokens; they cannot progress or pay rewards.
War: GUILD_WAR_KILL
Playtime guild: GUILD_COMBINED_PLAYTIME
Leaderboard: GUILD_LEADERBOARD_RANK
Diplomacy: GUILD_ALLIANCE_FORMED
Shop/Market: SHOP_CREATED, SHOP_DELETED, GUILD_REGION_PURCHASED, SHOP_STOCK_DEPLETED
DiaryKeeper: DIARY_RECEIVED, DIARY_FILLED, DIARY_SIGNED, DIARY_OBTAINED, DIARY_VOID_RETURN, DIARY_DESTRUCTION_ATTEMPT, DIARY_CONTAINER_ATTEMPT, DIARY_DUPLICATE_WARNING
Currency: BALTOP_ENTER, CURRENCY_DEPOSIT, CURRENCY_WITHDRAW, CURRENCY_PAY, CURRENCY_PAY_SELF, CURRENCY_BALANCE_ZERO
Playtime individual: PLAYER_PLAYTIME, PLAYER_AFK_DETECTED, PLAYER_AFK_DURATION
Commend: COMMEND_RECEIVED, COMMEND_GIVEN, REP_MILESTONE, COMMEND_PROFILE_VIEWED, COMMEND_EDITED, COMMEND_LEADERBOARD_VIEWED

## Current Listeners (15 total)

EntityKillListener, BlockBreakListener, CraftItemListener, PlayerJoinListener,
PlayerDeathListener, GuildCollectiveListener, ShopTransactionListener, KothListener,
GuildPlaytimeListener, LeaderboardRankListener, ShopEventListener, DiaryListener,
CurrencyListener, CommendListener, PlaytimeListener

Plus: LumaGuildsHook (in plugins/ package)

## Events Added to Forked Plugins

### DiaryKeeper (BadgersMC/DiaryKeeper) — 8 events in `com.lincoln.diary.events`
DiaryReceivedEvent, DiaryFilledEvent, DiarySignedEvent, DiaryObtainedEvent,
DiaryVoidReturnEvent, DiaryDestructionAttemptEvent, DiaryContainerAttemptEvent,
DiaryDuplicateWarningEvent
**Wired into**: JoinListener, EditListener, DropTrackListener, ItemProtectionListener, ContainerGuardListener, VoidWatcher, DuplicateWatcher. Compiles clean.

### EnthusiaCommend (wsg138/EnthusiaCommend, NOT forked) — 3 events in `org.enthusia.rep.events`
CommendationReceivedEvent, CommendationGivenEvent, RepMilestoneReachedEvent
**Wired into**: RepService.addOrUpdateCommendation() and adjustScore(). Note: plugin has pre-existing compile error (missing skin package) unrelated to our changes. Events compiled standalone into commend-events.jar.

### EnthusiaCurrency (BadgersMC/EnthusiaCurrency) — 1 event exists
BaltopTopEnterEvent in `com.enthusia.enthusiacurrency.event` (pre-existing, not added by us)

---

## REMAINING WORK — Still Missing Advancements

### 1. Commendation — Complex State-Tracking (4 nodes)
**Need**: Custom state persistence or cross-system listeners

| Advancement | What's Needed | Complexity |
|---|---|---|
| The Critic | Track distinct categories used in CommendationGivenEvent | Medium — needs per-player category set storage |
| Cornered Beast | PlayerKillEvent + rep score lookup from EnthusiaCommend | Medium — cross-plugin listener |
| Polarizing Figure (Hidden) | Track both positive AND negative received counts per-player | Medium — counter logic in CommendListener |
| Forgiven Not Forgotten (Hidden) | Track "was ever <= -10" historical state | Medium — needs persistent flag |

### 2. EnthusiaCurrency — Remaining (2 nodes)
| Advancement | What's Needed |
|---|---|
| Cold Hard Cash | PlayerPickupItemEvent + check if currency item (needs CurrencyManager API) |
| The Gold Standard | Inventory check for Raw Gold Block on some trigger |

### 3. Container Shops — Remaining (4 nodes)
**Need**: Deeper ItemShops source changes

| Advancement | What's Needed |
|---|---|
| Compact Commerce | ShopSignAttachedEvent in SignShopListener |
| Business Partners | ShopTrustAddedEvent in TrustManageMenu/BulkTrustMenu |
| Supply Chain | HopperConnectedToShopEvent in HopperControlListener |
| Wholesale Buyer | isBuyMax flag on PostShopTransactionEvent |
| False Advertising (Hidden) | PreShopTransactionEvent check when stock=0 |

### 4. DiaryKeeper — Identity Theft (1 node)
**Blocked**: DiaryDuplicateWarningEvent has no player field. Need to add `Player` to event constructor in DiaryKeeper fork.

### 5. NotBounties — Fully Missing (8+ nodes)
**Status**: No source available. Skip until source is obtained or build Trigger API.

---

## What Was Completed (2026-04-14)

### Events added to external plugins:
- **EnthusiaCurrency** (5 events): CurrencyDepositEvent, CurrencyWithdrawEvent, CurrencyPayEvent, CurrencyPaySelfAttemptEvent, CurrencyBalanceZeroEvent — wired into DepositCommand, WithdrawCommand, PayCommand
- **EnthusiaCommend** (3 events): CommendationProfileViewedEvent, CommendationEditedEvent, CommendationLeaderboardViewedEvent — wired into RepGuiManager, RepService, CommendCommand. Events JAR rebuilt.
- **ItemShops** (1 event): ShopStockDepletedEvent — wired into PurchaseMenu after PostShopTransactionEvent

### New nodes added (19 total):
- **exploration.conf** +3: rough_landing, caught_in_4k, the_ghost
- **diary.conf** +1: stubborn
- **economy.conf** +5: safely_secured, liquid_assets, philanthropist, fat_fingers, rock_bottom
- **reputation.conf** +6: trusted, bad_reputation, villain, public_enemy, public_record, fame_or_shame, change_of_heart
- **shops.conf** (NEW tree) +4: shops_root, monopoly, sold_out, bad_investment

### New RequirementTypes (12 total):
PLAYER_DEATH, CURRENCY_DEPOSIT, CURRENCY_WITHDRAW, CURRENCY_PAY, CURRENCY_PAY_SELF, CURRENCY_BALANCE_ZERO, SHOP_STOCK_DEPLETED, COMMEND_PROFILE_VIEWED, COMMEND_EDITED, COMMEND_LEADERBOARD_VIEWED, PLAYER_AFK_DETECTED, PLAYER_AFK_DURATION

### New listeners:
- PlayerDeathListener (Bukkit native)
- PlaytimeListener updated with AFK detection
- CurrencyListener expanded with 5 new handlers
- CommendListener expanded with 3 new handlers
- ShopEventListener expanded with delete + stock depleted handlers

## Build Notes

- DiaryKeeper: Maven, compiles with `./mvnw package` (wrapper copied from currency)
- EnthusiaCurrency: Maven, compiles with `.\mvnw.cmd package` (Windows)
- EnthusiaCommend: Maven, has pre-existing compile error (missing skin package). Events compiled standalone into `target/commend-events.jar` via javac
- Playtime-plugin: Maven, compiles with `./mvnw package`
- ItemShops: Gradle, compiles with `./gradlew shadowJar`
- CustomAdvancements: Gradle + Kotlin, all tests pass, `./gradlew test` green
