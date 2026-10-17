package io.github.badgersmc.advancements.config

import io.github.badgersmc.advancements.domain.FrameType
import io.github.badgersmc.advancements.domain.RequirementType
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class TreeConfigParserTest {

    @TempDir
    lateinit var tempDir: Path

    private fun createParser(treesDir: String = "trees"): TreeConfigParser {
        val config = AdvancementsConfig().apply { treesDirectory = treesDir }
        return TreeConfigParser(config)
    }

    private fun writeConfig(filename: String, content: String): Path {
        val treesDir = tempDir.resolve("trees").also { it.createDirectories() }
        val file = treesDir.resolve(filename)
        file.writeText(content)
        return file
    }

    @Test
    fun `parses valid HOCON file into TreeDef with all fields`() {
        writeConfig("test.conf", """
            namespace = "test"
            background-texture = "minecraft:textures/block/stone.png"
            nodes = [
                {
                    key = "root"
                    title = "Root"
                    description = ["The root node"]
                    icon = "STONE"
                    frame = "TASK"
                    x = 0.0
                    y = 0.0
                    max-progression = 1
                },
                {
                    key = "child"
                    parent = "root"
                    title = "Child"
                    description = ["A child node"]
                    icon = "DIAMOND"
                    frame = "GOAL"
                    x = 1.0
                    y = 0.0
                    max-progression = 5
                    requirement = {
                        type = "BLOCK_BREAK"
                        target = "DIAMOND_ORE"
                        amount = 5
                    }
                    rewards = [
                        {
                            type = "experience"
                            amount = 100
                        }
                    ]
                }
            ]
        """.trimIndent())

        val parser = createParser()
        val trees = parser.parseAll(tempDir)

        assertEquals(1, trees.size)
        val tree = trees[0]
        assertEquals("test", tree.namespace)
        // Current TreeDef no longer exposes backgroundTexture; validate node parsing.
        assertEquals(2, tree.nodes.size)

        val root = tree.nodes.find { it.key == "root" }!!
        assertEquals("Root", root.title)
        assertEquals(FrameType.TASK, root.frame)
        assertEquals(null, root.parentKey)

        val child = tree.nodes.find { it.key == "child" }!!
        assertEquals("root", child.parentKey)
        assertEquals(FrameType.GOAL, child.frame)
        assertEquals(5, child.maxProgression)
        assertEquals(RequirementType.BLOCK_BREAK, child.requirement!!.type)
        assertEquals("DIAMOND_ORE", child.requirement!!.target)
        assertEquals(1, child.rewards.size)
    }

    @Test
    fun `missing namespace returns empty list and logs error`() {
        writeConfig("bad.conf", """
            background-texture = "minecraft:textures/block/stone.png"
            nodes = [
                {
                    key = "root"
                    title = "Root"
                    description = ["Test"]
                    icon = "STONE"
                    frame = "TASK"
                    x = 0.0
                    y = 0.0
                    max-progression = 1
                }
            ]
        """.trimIndent())

        val parser = createParser()
        val trees = parser.parseAll(tempDir)

        assertEquals(0, trees.size)
    }

    @Test
    fun `unknown frame type returns empty list`() {
        writeConfig("bad_frame.conf", """
            namespace = "test"
            background-texture = "minecraft:textures/block/stone.png"
            nodes = [
                {
                    key = "root"
                    title = "Root"
                    description = ["Test"]
                    icon = "STONE"
                    frame = "INVALID_FRAME"
                    x = 0.0
                    y = 0.0
                    max-progression = 1
                }
            ]
        """.trimIndent())

        val parser = createParser()
        val trees = parser.parseAll(tempDir)

        assertEquals(0, trees.size)
    }

    @Test
    fun `empty nodes list returns empty list`() {
        writeConfig("empty_nodes.conf", """
            namespace = "test"
            background-texture = "minecraft:textures/block/stone.png"
            nodes = []
        """.trimIndent())

        val parser = createParser()
        val trees = parser.parseAll(tempDir)

        assertEquals(0, trees.size)
    }

    @Test
    fun `multiple conf files in directory returns list of all parsed trees`() {
        writeConfig("tree1.conf", """
            namespace = "tree1"
            background-texture = "minecraft:textures/block/stone.png"
            nodes = [
                { key = "root", title = "Root", description = ["Test"], icon = "STONE", frame = "TASK", x = 0.0, y = 0.0, max-progression = 1 }
            ]
        """.trimIndent())

        writeConfig("tree2.conf", """
            namespace = "tree2"
            background-texture = "minecraft:textures/block/dirt.png"
            nodes = [
                { key = "root", title = "Root", description = ["Test"], icon = "DIRT", frame = "TASK", x = 0.0, y = 0.0, max-progression = 1 }
            ]
        """.trimIndent())

        val parser = createParser()
        val trees = parser.parseAll(tempDir)

        assertEquals(2, trees.size)
        val namespaces = trees.map { it.namespace }.toSet()
        assertEquals(setOf("tree1", "tree2"), namespaces)
    }

    @Test
    fun `parses rewards correctly for all types`() {
        writeConfig("rewards.conf", """
            namespace = "rewards_test"
            background-texture = "minecraft:textures/block/stone.png"
            nodes = [
                {
                    key = "root"
                    title = "Root"
                    description = ["Test"]
                    icon = "STONE"
                    frame = "TASK"
                    x = 0.0
                    y = 0.0
                    max-progression = 1
                    rewards = [
                        { type = "command", command = "say hello %player%" },
                        { type = "experience", amount = 50 },
                        { type = "item", material = "DIAMOND", amount = 3 },
                        { type = "message", text = "<green>Well done!" }
                    ]
                }
            ]
        """.trimIndent())

        val parser = createParser()
        val trees = parser.parseAll(tempDir)

        assertEquals(1, trees.size)
        val rewards = trees[0].nodes[0].rewards
        assertEquals(4, rewards.size)
    }

    @Test
    fun `non-existent trees directory returns empty list`() {
        val parser = createParser("nonexistent")
        val trees = parser.parseAll(tempDir)

        assertEquals(0, trees.size)
    }
}
