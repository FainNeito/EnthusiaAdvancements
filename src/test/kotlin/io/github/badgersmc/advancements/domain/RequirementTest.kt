package io.github.badgersmc.advancements.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RequirementTest {

    @Test
    fun `all requirement types exist`() {
        val expected = setOf(
            "BLOCK_BREAK", "BLOCK_PLACE", "ENTITY_KILL", "CRAFT_ITEM",
            "PLAYER_KILL", "PLAYER_JOIN",
            "GUILD_CREATE", "GUILD_MEMBER_JOIN",
            "GUILD_BANNER_SET", "GUILD_HOME_SET", "GUILD_VAULT_PLACED",
            "GUILD_LEVEL_UP", "GUILD_WAR_DECLARED", "GUILD_WAR_WON",
            "GUILD_MEMBER_REMOVED", "GUILD_OWNERSHIP_TRANSFER", "GUILD_FRIENDLY_FIRE",
            "GUILD_COLLECTIVE_SMELT", "GUILD_COLLECTIVE_BLOCK_BREAK",
            "GUILD_COLLECTIVE_CROP_HARVEST", "GUILD_COLLECTIVE_MOB_KILL",
            "GUILD_COLLECTIVE_BOSS_KILL",
            "GUILD_SHOP_SALE", "GUILD_SHOP_PURCHASE",
            "KOTH_CAPTURE", "KOTH_CONSECUTIVE_CAPTURE", "EKOTH_VERIFIED_CHALLENGE",
            "GUILD_WAR_KILL", "GUILD_COMBINED_PLAYTIME",
            "GUILD_LEADERBOARD_RANK",
            "GUILD_ALLIANCE_FORMED",
            "SHOP_CREATED", "SHOP_DELETED", "GUILD_REGION_PURCHASED", "SHOP_STOCK_DEPLETED",
            "DIARY_RECEIVED", "DIARY_FILLED", "DIARY_SIGNED", "DIARY_OBTAINED",
            "DIARY_VOID_RETURN", "DIARY_DESTRUCTION_ATTEMPT", "DIARY_CONTAINER_ATTEMPT",
            "DIARY_DUPLICATE_WARNING",
            "PLAYER_DEATH", "PLAYER_AFK_DETECTED", "PLAYER_AFK_DURATION",
            "BALTOP_ENTER", "CURRENCY_DEPOSIT", "CURRENCY_WITHDRAW",
            "CURRENCY_PAY", "CURRENCY_PAY_SELF", "CURRENCY_BALANCE_ZERO",
            "PLAYER_PLAYTIME",
            "COMMEND_RECEIVED", "COMMEND_GIVEN", "REP_MILESTONE",
            "COMMEND_PROFILE_VIEWED", "COMMEND_EDITED", "COMMEND_LEADERBOARD_VIEWED"
        )
        val actual = RequirementType.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `requirement type count is 22`() {
        assertEquals(59, RequirementType.entries.size)
    }

    @Test
    fun `requirement with null target is valid`() {
        val req = Requirement(RequirementType.PLAYER_JOIN)
        assertNull(req.target)
        assertEquals(1, req.amount)
    }

    @Test
    fun `requirement default amount is 1`() {
        val req = Requirement(RequirementType.ENTITY_KILL, "ZOMBIE")
        assertEquals(1, req.amount)
    }

    @Test
    fun `requirement stores all fields`() {
        val req = Requirement(RequirementType.BLOCK_BREAK, "STONE", 50)
        assertEquals(RequirementType.BLOCK_BREAK, req.type)
        assertEquals("STONE", req.target)
        assertEquals(50, req.amount)
    }

    @Test
    fun `valueOf resolves enum values`() {
        assertEquals(RequirementType.ENTITY_KILL, RequirementType.valueOf("ENTITY_KILL"))
        assertEquals(RequirementType.GUILD_CREATE, RequirementType.valueOf("GUILD_CREATE"))
    }
}
