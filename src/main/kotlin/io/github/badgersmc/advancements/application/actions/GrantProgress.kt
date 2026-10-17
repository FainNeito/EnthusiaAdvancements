package io.github.badgersmc.advancements.application.actions

import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.api.events.EnthusiaAdvancementCompletedEvent
import io.github.badgersmc.advancements.domain.RequirementType
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object GrantProgress {

    fun execute(
        registry: AdvancementRegistry,
        type: RequirementType,
        target: String?,
        player: Player,
        onCompleted: (Player, String, String) -> Unit = ::publishCompleted,
    ) {
        if (type == RequirementType.EKOTH_VERIFIED_CHALLENGE || type.isRetiredKoth) return
        val matches = registry.findByRequirement(type, target)
        for ((namespace, key) in matches) {
            if(namespace == "ekoth") continue
            val advancement = registry.getAdvancement(namespace, key) ?: continue
            val wasGranted = advancement.isGranted(player)
            advancement.incrementProgression(player)
            if (!wasGranted && advancement.isGranted(player)) {
                onCompleted(player, namespace, key)
            }
        }
    }

    private fun publishCompleted(player: Player, namespace: String, key: String) {
        Bukkit.getPluginManager().callEvent(EnthusiaAdvancementCompletedEvent(player, namespace, key))
    }
}
