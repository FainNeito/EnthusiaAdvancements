package io.github.badgersmc.advancements

import net.badgersmc.nexus.core.NexusContext
import net.badgersmc.nexus.paper.registerPaperCommands
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files

open class EnthusiaAdvancementsPlugin : JavaPlugin() {

    private lateinit var nexus: NexusContext

    override fun onEnable() {
        // Copy default tree configs if trees/ directory doesn't exist
        val treesDir = dataFolder.toPath().resolve("trees")
        if (!Files.exists(treesDir)) {
            Files.createDirectories(treesDir)
            copyDefaultTree("trees/combat.conf")
            copyDefaultTree("trees/exploration.conf")
            copyDefaultTree("trees/guilds.conf")
        }

        // Add the provider-owned tree to existing installs without overwriting edits.
        if (!Files.exists(treesDir.resolve("ekoth.conf"))) copyDefaultTree("trees/ekoth.conf")

        // Create Nexus DI context
        nexus = NexusContext.create(
            basePackage = "io.github.badgersmc.advancements",
            classLoader = this::class.java.classLoader,
            configDirectory = dataFolder.toPath(),
            contextName = "EnthusiaAdvancements",
            externalBeans = mapOf("plugin" to this)
        )

        // Register Paper commands
        nexus.registerPaperCommands(
            basePackage = "io.github.badgersmc.advancements",
            classLoader = this::class.java.classLoader,
            plugin = this
        )

        logger.info("EnthusiaAdvancements enabled")
    }

    override fun onDisable() {
        if (::nexus.isInitialized) {
            nexus.close()
        }
        logger.info("EnthusiaAdvancements disabled")
    }

    private fun copyDefaultTree(resourcePath: String) {
        getResource(resourcePath)?.let { stream ->
            val target = dataFolder.toPath().resolve(resourcePath)
            Files.copy(stream, target)
        }
    }
}
