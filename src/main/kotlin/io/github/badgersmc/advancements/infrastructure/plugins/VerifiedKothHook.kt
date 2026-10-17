package io.github.badgersmc.advancements.infrastructure.plugins

import io.github.badgersmc.advancements.infrastructure.advancement.UltimateAdvancementAdapter
import net.badgersmc.ek.api.KothProgressionV1
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

@Component
class VerifiedKothHook(private val plugin:JavaPlugin,private val adapter:UltimateAdvancementAdapter) {
    @PostConstruct fun register() {
        // No lifecycle-event or AxKOTH fallback: unavailable/disabled means no credit.
        Bukkit.getScheduler().runTaskTimer(plugin,Runnable { projectOnlinePlayers() },20L,200L)
    }

    private fun projectOnlinePlayers() {
        val provider=runCatching { Bukkit.getServicesManager().load(KothProgressionV1::class.java) }.getOrNull() ?: return
        Bukkit.getOnlinePlayers().forEach { player ->
            runCatching { provider.progress(player.uniqueId)?.let { adapter.projectKoth(player,it) } }
                .onFailure { plugin.logger.warning("Verified KOTH progress unavailable: ${it.message}") }
        }
    }
}
