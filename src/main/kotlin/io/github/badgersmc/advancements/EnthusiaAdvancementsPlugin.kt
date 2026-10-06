package io.github.badgersmc.advancements

import io.github.badgersmc.advancements.config.BundledTrees
import net.badgersmc.nexus.core.NexusContext
import net.badgersmc.nexus.paper.registerPaperCommands
import org.bukkit.plugin.java.JavaPlugin

open class EnthusiaAdvancementsPlugin : JavaPlugin() {

    private lateinit var nexus: NexusContext

    override fun onEnable() {
        // Starter trees on a fresh install; integration trees (holidays) whenever missing.
        BundledTrees.install(dataFolder.toPath(), ::getResource).forEach { logger.info("Installed default $it") }

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
}
