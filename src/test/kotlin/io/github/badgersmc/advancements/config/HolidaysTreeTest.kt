package io.github.badgersmc.advancements.config

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

/** The bundled holidays tree that EnthusiaHolidays grants by command (REQ-HOL-01, REQ-HOL-02). */
class HolidaysTreeTest {

    @TempDir
    lateinit var tempDir: Path

    private fun bundled(name: String): String =
        requireNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing $name" }
            .bufferedReader().use { it.readText() }

    private fun parseBundledHolidays() =
        tempDir.resolve("trees").createDirectories().resolve("holidays.conf")
            .also { it.writeText(bundled("trees/holidays.conf")) }
            .let { TreeConfigParser(AdvancementsConfig()).parseAll(tempDir).single() }

    @Test
    fun `bundled holidays tree parses and validates`() {
        val tree = parseBundledHolidays()
        assertEquals("holidays", tree.namespace)
        assertEquals(emptyList(), tree.validate())
        assertEquals(
            setOf(
                "holidays_root", "pumpkin_hunter", "no_pumpkin_left_behind", "present_seeker",
                "home_for_the_holidays", "advent_keeper", "secret_santa", "seen_the_watcher",
                "trick_or_treat", "hexed", "rare_treat", "pumpkin_king", "spooky_together", "dont_blink", "forgotten_melody",
            ),
            tree.nodes.map { it.key }.toSet(),
        )
    }

    @Test
    fun `holiday advancements are granted only by command`() {
        // EnthusiaHolidays grants these with `advancements grant <player> holidays <key>`;
        // a requirement would let normal gameplay complete them too.
        parseBundledHolidays().nodes.forEach { assertNull(it.requirement, it.key) }
    }

    @Test
    fun `fresh install copies the starter trees and the holidays tree`() {
        val installed = BundledTrees.install(tempDir) { name -> bundled(name).byteInputStream() }
        assertEquals(
            setOf("trees/combat.conf", "trees/exploration.conf", "trees/guilds.conf", "trees/holidays.conf"),
            installed.toSet(),
        )
    }

    @Test
    fun `existing servers get the holidays tree without other trees changing`() {
        val trees = tempDir.resolve("trees").createDirectories()
        trees.resolve("combat.conf").writeText("namespace = \"custom\"")

        val installed = BundledTrees.install(tempDir) { name -> bundled(name).byteInputStream() }

        assertEquals(listOf("trees/holidays.conf"), installed)
        assertEquals("namespace = \"custom\"", trees.resolve("combat.conf").readText())
        assertFalse(Files.exists(trees.resolve("exploration.conf")), "starter trees stay fresh-install only")
        assertTrue(Files.exists(trees.resolve("holidays.conf")))
    }

    @Test
    fun `an admin-edited holidays tree is never overwritten`() {
        val trees = tempDir.resolve("trees").createDirectories()
        trees.resolve("holidays.conf").writeText("namespace = \"holidays\" # edited")

        assertEquals(emptyList(), BundledTrees.install(tempDir) { name -> bundled(name).byteInputStream() })
        assertEquals("namespace = \"holidays\" # edited", trees.resolve("holidays.conf").readText())
    }
}
