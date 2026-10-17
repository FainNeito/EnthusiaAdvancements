package io.github.badgersmc.advancements.infrastructure.plugins

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.application.ports.PluginHook
import io.github.badgersmc.advancements.api.events.EnthusiaAdvancementCompletedEvent
import io.github.badgersmc.advancements.domain.RequirementType
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import net.lumalyte.lg.application.persistence.MemberRepository
import net.lumalyte.lg.domain.entities.RelationType
import net.lumalyte.lg.api.events.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

@Component
class LumaGuildsHook(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry
) : PluginHook, Listener {

    override val pluginName: String = "LumaGuilds"
    private var supportsExplorationMilestoneApi = false

    override fun isAvailable(): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled("LumaGuilds")
    }

    @PostConstruct
    override fun onEnable() {
        if (!isAvailable()) {
            plugin.logger.info("LumaGuilds not found, skipping guild advancement hooks")
            return
        }

        try {
            supportsExplorationMilestoneApi = runCatching {
                Class.forName(
                    "net.lumalyte.lg.api.events.GuildExplorationMilestoneEvent",
                    false,
                    plugin.server.pluginManager.getPlugin("LumaGuilds")?.javaClass?.classLoader,
                )
            }.isSuccess
            Bukkit.getPluginManager().registerEvents(this, plugin)
            plugin.logger.info("LumaGuilds integration enabled")
            if (!supportsExplorationMilestoneApi) {
                plugin.logger.info("LumaGuilds custom advancement XP bridge unavailable; update LumaGuilds to a build with GuildExplorationMilestoneEvent")
            }
        } catch (e: NoClassDefFoundError) {
            plugin.logger.warning("LumaGuilds classes not found on classpath, skipping guild hooks")
        }
    }

    @EventHandler
    fun onAdvancementCompleted(event: EnthusiaAdvancementCompletedEvent) {
        if (!supportsExplorationMilestoneApi) return
        try {
            Bukkit.getPluginManager().callEvent(
                GuildExplorationMilestoneEvent(
                    event.player,
                    "EnthusiaAdvancements",
                    "${event.namespace}:${event.key}",
                )
            )
        } catch (error: NoClassDefFoundError) {
            supportsExplorationMilestoneApi = false
            plugin.logger.warning("LumaGuilds custom advancement XP bridge became unavailable: ${error.message}")
        }
    }

    // --- Individual guild actions (grant to the acting player) ---

    @EventHandler
    fun onGuildCreated(event: GuildCreatedEvent) {
        val player = Bukkit.getPlayer(event.ownerId) ?: return
        GrantProgress.execute(registry, RequirementType.GUILD_CREATE, null, player)
    }

    @EventHandler
    fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        GrantProgress.execute(registry, RequirementType.GUILD_MEMBER_JOIN, null, player)
    }

    @EventHandler
    fun onGuildBannerSet(event: GuildBannerSetEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        GrantProgress.execute(registry, RequirementType.GUILD_BANNER_SET, null, player)
    }

    @EventHandler
    fun onGuildHomeSet(event: GuildHomeSetEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        GrantProgress.execute(registry, RequirementType.GUILD_HOME_SET, null, player)
    }

    @EventHandler
    fun onGuildVaultPlaced(event: GuildVaultPlacedEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        GrantProgress.execute(registry, RequirementType.GUILD_VAULT_PLACED, null, player)
    }

    @EventHandler
    fun onGuildMemberRemoved(event: GuildMemberRemovedEvent) {
        if (!event.wasKicked) return // Only counts when someone else kicks a member
        val actor = Bukkit.getPlayer(event.actorId) ?: return
        GrantProgress.execute(registry, RequirementType.GUILD_MEMBER_REMOVED, null, actor)
    }

    @EventHandler
    fun onGuildOwnershipTransfer(event: GuildOwnershipTransferEvent) {
        val newOwner = Bukkit.getPlayer(event.newOwnerId) ?: return
        GrantProgress.execute(registry, RequirementType.GUILD_OWNERSHIP_TRANSFER, null, newOwner)
    }

    // --- Guild-wide events (grant to all online guild members) ---

    @EventHandler
    fun onGuildLevelUp(event: GuildLevelUpEvent) {
        val onlineMembers = getOnlineGuildMembers(event.guildId)
        for (player in onlineMembers) {
            // Grant for each level threshold that's been reached
            GrantProgress.execute(registry, RequirementType.GUILD_LEVEL_UP, event.newLevel.toString(), player)
        }
    }

    @EventHandler
    fun onGuildWarDeclared(event: GuildWarDeclaredEvent) {
        // Grant to all members of the declaring guild
        val onlineMembers = getOnlineGuildMembers(event.declaringGuildId)
        for (player in onlineMembers) {
            GrantProgress.execute(registry, RequirementType.GUILD_WAR_DECLARED, null, player)
        }
    }

    @EventHandler
    fun onGuildWarEnd(event: GuildWarEndEvent) {
        val winnerGuildId = event.winnerGuildId ?: return // Draws don't count
        val onlineMembers = getOnlineGuildMembers(winnerGuildId)
        for (player in onlineMembers) {
            GrantProgress.execute(registry, RequirementType.GUILD_WAR_WON, null, player)
        }
    }

    @EventHandler
    fun onGuildWarKill(event: GuildWarKillEvent) {
        val killer = Bukkit.getPlayer(event.killerId) ?: return
        GrantProgress.execute(registry, RequirementType.GUILD_WAR_KILL, null, killer)
    }

    @EventHandler
    fun onGuildRelationChange(event: GuildRelationChangeEvent) {
        if (event.newRelationType != RelationType.ALLY) return
        // Grant to both guilds' online members
        for (guildId in listOf(event.guild1, event.guild2)) {
            for (player in getOnlineGuildMembers(guildId)) {
                GrantProgress.execute(registry, RequirementType.GUILD_ALLIANCE_FORMED, null, player)
            }
        }
    }

    // --- Helpers ---

    /**
     * Gets all online players who are members of the given guild.
     * Uses Koin to access LumaGuilds' MemberRepository at runtime.
     */
    fun getOnlineGuildMembers(guildId: UUID): List<Player> {
        return try {
            val memberRepo = org.koin.core.context.GlobalContext.get().get<MemberRepository>()
            val memberIds = memberRepo.getByGuild(guildId).map { it.playerId }.toSet()
            Bukkit.getOnlinePlayers().filter { it.uniqueId in memberIds }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to look up guild members: ${e.message}")
            emptyList()
        }
    }

    /**
     * Gets the first guild ID for a player, or null if not in a guild.
     */
    fun getPlayerGuildId(playerId: UUID): UUID? {
        return try {
            val memberRepo = org.koin.core.context.GlobalContext.get().get<MemberRepository>()
            memberRepo.getGuildsByPlayer(playerId).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
