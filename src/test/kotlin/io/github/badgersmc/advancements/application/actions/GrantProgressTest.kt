package io.github.badgersmc.advancements.application.actions

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import io.mockk.*
import org.bukkit.entity.Player
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class GrantProgressTest {

    private lateinit var server: ServerMock
    private lateinit var player: Player
    private val registry = mockk<AdvancementRegistry>()

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        player = server.addPlayer()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `verified KOTH cannot increment through ordinary progress triggers`() {
        GrantProgress.execute(registry,RequirementType.EKOTH_VERIFIED_CHALLENGE,"sovereign_blade",player)
        verify(exactly=0) { registry.findByRequirement(any(),any()) }
    }

    @Test
    fun `retired capture types cannot increment saved trees`() {
        GrantProgress.execute(registry, RequirementType.KOTH_CAPTURE, null, player)
        GrantProgress.execute(registry, RequirementType.KOTH_CONSECUTIVE_CAPTURE, null, player)
        verify(exactly = 0) { registry.findByRequirement(any(), any()) }
    }

    @Test
    fun `calls findByRequirement on registry`() {
        every { registry.findByRequirement(any(), any()) } returns emptyList()

        GrantProgress.execute(registry, RequirementType.ENTITY_KILL, "ZOMBIE", player)

        verify { registry.findByRequirement(RequirementType.ENTITY_KILL, "ZOMBIE") }
    }

    @Test
    fun `no matches results in no progression calls`() {
        every { registry.findByRequirement(any(), any()) } returns emptyList()

        GrantProgress.execute(registry, RequirementType.ENTITY_KILL, "ZOMBIE", player)

        verify(exactly = 0) { registry.getAdvancement(any(), any()) }
    }

    @Test
    fun `single match increments progression`() {
        val advancement = mockk<Advancement>(relaxed = true)
        every { registry.findByRequirement(any(), any()) } returns listOf("combat" to "kill_zombies")
        every { registry.getAdvancement("combat", "kill_zombies") } returns advancement

        GrantProgress.execute(registry, RequirementType.ENTITY_KILL, "ZOMBIE", player)

        verify { advancement.incrementProgression(player) }
    }

    @Test
    fun `newly completed advancement publishes completion exactly once`() {
        val advancement = mockk<Advancement>(relaxed = true)
        every { registry.findByRequirement(any(), any()) } returns listOf("exploration" to "ancient_city")
        every { registry.getAdvancement("exploration", "ancient_city") } returns advancement
        every { advancement.isGranted(player) } returnsMany listOf(false, true)
        val completions = mutableListOf<Pair<String, String>>()

        GrantProgress.execute(
            registry,
            RequirementType.ENTITY_KILL,
            "WARDEN",
            player,
        ) { _, namespace, key -> completions += namespace to key }

        assertEquals(listOf("exploration" to "ancient_city"), completions)
    }

    @Test
    fun `incomplete progress does not publish completion`() {
        val advancement = mockk<Advancement>(relaxed = true)
        every { registry.findByRequirement(any(), any()) } returns listOf("combat" to "kill_zombies")
        every { registry.getAdvancement("combat", "kill_zombies") } returns advancement
        every { advancement.isGranted(player) } returns false
        val completions = mutableListOf<Pair<String, String>>()

        GrantProgress.execute(
            registry,
            RequirementType.ENTITY_KILL,
            "ZOMBIE",
            player,
        ) { _, namespace, key -> completions += namespace to key }

        assertEquals(emptyList(), completions)
    }

    @Test
    fun `already completed advancement does not republish completion`() {
        val advancement = mockk<Advancement>(relaxed = true)
        every { registry.findByRequirement(any(), any()) } returns listOf("combat" to "kill_zombies")
        every { registry.getAdvancement("combat", "kill_zombies") } returns advancement
        every { advancement.isGranted(player) } returns true
        val completions = mutableListOf<Pair<String, String>>()

        GrantProgress.execute(
            registry,
            RequirementType.ENTITY_KILL,
            "ZOMBIE",
            player,
        ) { _, namespace, key -> completions += namespace to key }

        assertEquals(emptyList(), completions)
    }

    @Test
    fun `multiple matches all get incremented`() {
        val adv1 = mockk<Advancement>(relaxed = true)
        val adv2 = mockk<Advancement>(relaxed = true)
        every { registry.findByRequirement(any(), any()) } returns listOf(
            "combat" to "kill_zombies",
            "quests" to "undead_slayer"
        )
        every { registry.getAdvancement("combat", "kill_zombies") } returns adv1
        every { registry.getAdvancement("quests", "undead_slayer") } returns adv2

        GrantProgress.execute(registry, RequirementType.ENTITY_KILL, "ZOMBIE", player)

        verify { adv1.incrementProgression(player) }
        verify { adv2.incrementProgression(player) }
    }

    @Test
    fun `null advancement from registry is skipped`() {
        every { registry.findByRequirement(any(), any()) } returns listOf("combat" to "missing")
        every { registry.getAdvancement("combat", "missing") } returns null

        // Should not throw
        GrantProgress.execute(registry, RequirementType.ENTITY_KILL, "ZOMBIE", player)
    }
}
