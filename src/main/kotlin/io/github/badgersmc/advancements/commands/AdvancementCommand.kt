package io.github.badgersmc.advancements.commands

import io.github.badgersmc.advancements.infrastructure.advancement.UltimateAdvancementAdapter
import net.badgersmc.nexus.commands.annotations.Arg
import net.badgersmc.nexus.commands.annotations.Command
import net.badgersmc.nexus.commands.annotations.Context
import net.badgersmc.nexus.paper.commands.annotations.Async
import net.badgersmc.nexus.paper.commands.annotations.Permission
import net.badgersmc.nexus.paper.commands.annotations.Subcommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@Command(name = "advancements")
class AdvancementCommand(
    private val adapter: UltimateAdvancementAdapter
) {

    @Subcommand("reload")
    @Async
    @Permission("advancements.admin")
    fun reload(@Context sender: CommandSender) {
        adapter.reloadAll()
        sender.sendMessage(Component.text("Advancement trees reloaded.", NamedTextColor.GREEN))
    }

    @Subcommand("grant")
    @Permission("advancements.admin")
    fun grant(
        @Context sender: CommandSender,
        @Arg("player") playerName: String,
        @Arg("tree") tree: String,
        @Arg("key") key: String
    ) {
        if(adapter.isProviderOwned(tree,key)) {
            sender.sendMessage(Component.text("This advancement requires verified KOTH records and cannot be granted administratively.",NamedTextColor.RED))
            return
        }
        val player = Bukkit.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage(Component.text("Player '$playerName' not found.", NamedTextColor.RED))
            return
        }

        val advancement = adapter.getAdvancement(tree, key)
        if (advancement == null) {
            sender.sendMessage(Component.text("Advancement '$tree:$key' not found.", NamedTextColor.RED))
            return
        }

        advancement.grant(player, true)
        sender.sendMessage(
            Component.text("Granted '$tree:$key' to ${player.name}.", NamedTextColor.GREEN)
        )
    }

    @Subcommand("list")
    fun list(@Context sender: CommandSender) {
        val namespaces = adapter.getTreeNamespaces()
        if (namespaces.isEmpty()) {
            sender.sendMessage(Component.text("No advancement trees loaded.", NamedTextColor.YELLOW))
            return
        }

        sender.sendMessage(Component.text("Loaded advancement trees:", NamedTextColor.GREEN))
        for (namespace in namespaces) {
            sender.sendMessage(Component.text("  - $namespace", NamedTextColor.GRAY))
        }
    }
}
