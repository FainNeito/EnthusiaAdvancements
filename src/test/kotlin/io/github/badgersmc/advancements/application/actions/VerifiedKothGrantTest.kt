package io.github.badgersmc.advancements.application.actions

import io.github.badgersmc.advancements.commands.AdvancementCommand
import io.github.badgersmc.advancements.infrastructure.advancement.UltimateAdvancementAdapter
import io.mockk.*
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Test
import io.github.badgersmc.advancements.domain.RequirementType
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VerifiedKothGrantTest {
    @Test fun `retired capture requirements block administrative grants in saved trees`() {
        val namespace = "guilds"
        val adapter = UltimateAdvancementAdapter(mockk(), mockk(), mockk(), mockk())
        val field = UltimateAdvancementAdapter::class.java.getDeclaredField("requirementIndex")
        field.isAccessible = true
        field.set(adapter, mutableMapOf(
            RequirementType.KOTH_CAPTURE to mutableMapOf<String?, MutableList<Pair<String, String>>>(null to mutableListOf(namespace to "capture")),
            RequirementType.KOTH_CONSECUTIVE_CAPTURE to mutableMapOf<String?, MutableList<Pair<String, String>>>(null to mutableListOf(namespace to "streak"))
        ))
        val sender = mockk<CommandSender>(relaxed = true)
        for (key in listOf("capture", "streak")) {
            assertTrue(adapter.isProviderOwned(namespace, key))
            AdvancementCommand(adapter).grant(sender, "someone", namespace, key)
        }
        assertFalse(adapter.isProviderOwned(namespace, "ordinary"))
    }
    @Test fun `admin grant cannot authorize a provider-owned advancement`() {
        val adapter=mockk<UltimateAdvancementAdapter>()
        val sender=mockk<CommandSender>(relaxed=true)
        every { adapter.isProviderOwned("ekoth","sovereign_blade") } returns true
        AdvancementCommand(adapter).grant(sender,"someone","ekoth","sovereign_blade")
        verify(exactly=0) { adapter.getAdvancement(any(),any()) }
    }
}
