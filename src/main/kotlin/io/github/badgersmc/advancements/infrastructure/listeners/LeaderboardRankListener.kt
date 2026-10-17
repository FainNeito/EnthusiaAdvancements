package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import io.github.badgersmc.advancements.infrastructure.plugins.LumaGuildsHook
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import net.lumalyte.lg.api.events.GuildLeaderboardRankChangeEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Listens to GuildLeaderboardRankChangeEvent from LumaGuilds and grants
 * GUILD_LEADERBOARD_RANK progress to online guild members when rank thresholds are met.
 */
@Component
class LeaderboardRankListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val guildHook: LumaGuildsHook
) : Listener {

    @PostConstruct
    fun initialize() {
        if (!guildHook.isAvailable()) {
            plugin.logger.info("LumaGuilds not available, leaderboard rank listener disabled")
            return
        }

        try {
            Bukkit.getPluginManager().registerEvents(this, plugin)
            plugin.logger.info("Leaderboard rank advancement listener enabled")
        } catch (e: NoClassDefFoundError) {
            plugin.logger.warning("LumaGuilds leaderboard classes not found, listener disabled")
        }
    }

    @EventHandler
    fun onRankChange(event: GuildLeaderboardRankChangeEvent) {
        val members = guildHook.getOnlineGuildMembers(event.guildId)
        if (members.isEmpty()) return

        // Grant progress with the new rank as target so config can match thresholds
        // e.g. target="10" matches top-10, target="3" matches top-3
        for (player in members) {
            GrantProgress.execute(registry, RequirementType.GUILD_LEADERBOARD_RANK, event.newRank.toString(), player)
        }
    }
}
