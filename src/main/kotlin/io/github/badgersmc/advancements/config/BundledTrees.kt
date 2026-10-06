package io.github.badgersmc.advancements.config

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * Copies bundled tree configs into the plugin's data folder.
 *
 * Starter trees are copied only on a fresh install (no `trees/` directory), as before.
 * Integration trees, such as the command-granted holidays tree that EnthusiaHolidays relies on
 * (REQ-HOL-02), are also copied onto existing servers whenever their file is missing.
 * An existing file is never overwritten.
 */
object BundledTrees {
    val STARTER = listOf("trees/combat.conf", "trees/exploration.conf", "trees/guilds.conf")
    val INTEGRATION = listOf("trees/holidays.conf")

    /** Copies what is missing from [dataFolder] and returns the resource paths it wrote. */
    fun install(dataFolder: Path, resource: (String) -> InputStream?): List<String> {
        val treesDir = dataFolder.resolve("trees")
        val freshInstall = !Files.exists(treesDir)
        Files.createDirectories(treesDir)
        val wanted = if (freshInstall) STARTER + INTEGRATION else INTEGRATION
        return wanted.filter { path ->
            val target = dataFolder.resolve(path)
            if (Files.exists(target)) return@filter false
            val stream = resource(path) ?: return@filter false
            stream.use { Files.copy(it, target) }
            true
        }
    }
}
