package io.github.badgersmc.advancements.domain

enum class RequirementType {
    // Bukkit native
    BLOCK_BREAK,
    BLOCK_PLACE,
    ENTITY_KILL,
    CRAFT_ITEM,
    PLAYER_KILL,
    PLAYER_JOIN,

    // LumaGuilds - individual actions
    GUILD_CREATE,
    GUILD_MEMBER_JOIN,
    GUILD_BANNER_SET,
    GUILD_HOME_SET,
    GUILD_VAULT_PLACED,
    GUILD_LEVEL_UP,
    GUILD_WAR_DECLARED,
    GUILD_WAR_WON,
    GUILD_MEMBER_REMOVED,
    GUILD_OWNERSHIP_TRANSFER,
    GUILD_FRIENDLY_FIRE,

    // LumaGuilds - guild collective stats
    GUILD_COLLECTIVE_SMELT,
    GUILD_COLLECTIVE_BLOCK_BREAK,
    GUILD_COLLECTIVE_CROP_HARVEST,
    GUILD_COLLECTIVE_MOB_KILL,
    GUILD_COLLECTIVE_BOSS_KILL,

    // ItemShops / ARM-Guilds-Bridge - guild shop transactions
    GUILD_SHOP_SALE,
    GUILD_SHOP_PURCHASE,

    // KOTH: legacy tokens retained only for stored configuration compatibility
    KOTH_CAPTURE,
    EKOTH_VERIFIED_CHALLENGE,
    KOTH_CONSECUTIVE_CAPTURE,

    // War kills
    GUILD_WAR_KILL,

    // Playtime (guild combined)
    GUILD_COMBINED_PLAYTIME,

    // Leaderboard rank
    GUILD_LEADERBOARD_RANK,

    // Diplomacy
    GUILD_ALLIANCE_FORMED,

    // Shop / Market
    SHOP_CREATED,
    SHOP_DELETED,
    GUILD_REGION_PURCHASED,
    SHOP_STOCK_DEPLETED,

    // DiaryKeeper
    DIARY_RECEIVED,
    DIARY_FILLED,
    DIARY_SIGNED,
    DIARY_OBTAINED,
    DIARY_VOID_RETURN,
    DIARY_DESTRUCTION_ATTEMPT,
    DIARY_CONTAINER_ATTEMPT,
    DIARY_DUPLICATE_WARNING,

    // Bukkit native — death
    PLAYER_DEATH,

    // EnthusiaCurrency
    BALTOP_ENTER,
    CURRENCY_DEPOSIT,
    CURRENCY_WITHDRAW,
    CURRENCY_PAY,
    CURRENCY_PAY_SELF,
    CURRENCY_BALANCE_ZERO,

    // Playtime (individual)
    PLAYER_PLAYTIME,
    PLAYER_AFK_DETECTED,
    PLAYER_AFK_DURATION,

    // EnthusiaCommend
    COMMEND_RECEIVED,
    COMMEND_GIVEN,
    REP_MILESTONE,
    COMMEND_PROFILE_VIEWED,
    COMMEND_EDITED,
    COMMEND_LEADERBOARD_VIEWED;

    val isRetiredKoth: Boolean
        get() = this == KOTH_CAPTURE || this == KOTH_CONSECUTIVE_CAPTURE
}

data class Requirement(
    val type: RequirementType,
    val target: String? = null,
    val amount: Int = 1
)
