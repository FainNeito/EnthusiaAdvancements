package io.github.badgersmc.advancements.application.actions

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
import com.fren_gor.ultimateAdvancementAPI.advancement.display.FancyAdvancementDisplay
import io.github.badgersmc.advancements.application.ports.RewardExecutor
import io.github.badgersmc.advancements.domain.AdvancementNodeDef
import io.github.badgersmc.advancements.domain.TreeDef
import io.github.badgersmc.advancements.infrastructure.advancement.RewardableAdvancement
import io.github.badgersmc.advancements.infrastructure.advancement.RewardableRootAdvancement
import org.bukkit.Material

data class BuildTreeResult(
    val tab: AdvancementTab,
    val advancements: Map<String, Advancement>,
    val rootKey: String
)

object BuildTree {
    internal fun rewardsFor(namespace:String,node:AdvancementNodeDef):List<io.github.badgersmc.advancements.domain.Reward> =
        if(namespace == "ekoth" || node.requirement?.type == io.github.badgersmc.advancements.domain.RequirementType.EKOTH_VERIFIED_CHALLENGE || node.requirement?.type?.isRetiredKoth == true) emptyList() else node.rewards

    fun execute(treeDef: TreeDef, api: UltimateAdvancementAPI, rewardExecutor: RewardExecutor): BuildTreeResult {
        val tab = api.createAdvancementTab(treeDef.namespace)
        val sorted = treeDef.sortedNodes()
        val yOffset = 1f - sorted.minOf { it.y }
        val advancementMap = mutableMapOf<String, Advancement>()
        val children = mutableSetOf<BaseAdvancement>()

        for (node in sorted) {
            val display = buildDisplay(node, yOffset)
            val rewards = rewardsFor(treeDef.namespace,node)

            if (node.parentKey == null) {
                val root = RewardableRootAdvancement(
                    tab, node.key, node.maxProgression, display,
                    rewards, rewardExecutor
                )
                advancementMap[node.key] = root
            } else {
                val parent = advancementMap[node.parentKey]!!
                val child = RewardableAdvancement(
                    parent, node.key, node.maxProgression, display,
                    rewards, rewardExecutor
                )
                advancementMap[node.key] = child
                children.add(child)
            }
        }

        val root = advancementMap[treeDef.root.key] as RootAdvancement
        tab.registerAdvancements(root, children)

        return BuildTreeResult(tab, advancementMap, treeDef.root.key)
    }

    private fun buildDisplay(node: AdvancementNodeDef, yOffset: Float): AdvancementDisplay {
        return FancyAdvancementDisplay.Builder(Material.valueOf(node.icon), node.title)
            .description(node.description)
            .frame(AdvancementFrameType.valueOf(node.frame.name))
            .showToast(node.showToast)
            .announceChat(node.announceChat)
            .coords(node.x, node.y + yOffset)
            .build()
    }
}
