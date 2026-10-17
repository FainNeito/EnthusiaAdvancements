package io.github.badgersmc.advancements.infrastructure.advancement

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import io.github.badgersmc.advancements.application.actions.BuildTree
import io.github.badgersmc.advancements.application.actions.ReloadableRegistry
import io.github.badgersmc.advancements.application.ports.RewardExecutor
import io.github.badgersmc.advancements.config.AdvancementsConfig
import io.github.badgersmc.advancements.config.TreeConfigParser
import io.github.badgersmc.advancements.domain.RequirementType
import net.badgersmc.nexus.annotations.PostConstruct
import net.badgersmc.nexus.annotations.Service
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

@Service
class UltimateAdvancementAdapter(
    private val plugin: JavaPlugin,
    private val config: AdvancementsConfig,
    private val treeConfigParser: TreeConfigParser,
    private val rewardExecutor: RewardExecutor
) : ReloadableRegistry {

    private lateinit var api: UltimateAdvancementAPI
    private val tabs = mutableMapOf<String, AdvancementTab>()
    private val rootKeys = mutableMapOf<String, String>()
    private val advancementMap = mutableMapOf<String, MutableMap<String, Advancement>>()
    private val requirementIndex = mutableMapOf<RequirementType, MutableMap<String?, MutableList<Pair<String, String>>>>()

    @PostConstruct
    fun initialize() {
        api = UltimateAdvancementAPI.getInstance(plugin)
        loadAllTrees()
    }

    fun loadAllTrees() {
        val treeDefs = treeConfigParser.parseAll(plugin.dataFolder.toPath())

        for (treeDef in treeDefs) {
            try {
                val result = BuildTree.execute(treeDef, api, rewardExecutor)
                tabs[treeDef.namespace] = result.tab
                rootKeys[treeDef.namespace] = result.rootKey
                advancementMap[treeDef.namespace] = result.advancements.toMutableMap()

                // Build requirement index
                for (node in treeDef.nodes) {
                    val req = node.requirement ?: continue
                    requirementIndex
                        .getOrPut(req.type) { mutableMapOf() }
                        .getOrPut(req.target) { mutableListOf() }
                        .add(treeDef.namespace to node.key)
                }

                if (config.debug) {
                    plugin.logger.info("Loaded tree '${treeDef.namespace}' with ${treeDef.nodes.size} nodes")
                }
            } catch (e: Exception) {
                plugin.logger.log(Level.SEVERE, "Failed to build tree '${treeDef.namespace}'", e)
            }
        }

        plugin.logger.info("Loaded ${tabs.size} advancement tree(s)")
    }

    override fun reloadAll() {
        api.unregisterPluginAdvancementTabs()
        tabs.clear()
        rootKeys.clear()
        advancementMap.clear()
        requirementIndex.clear()
        loadAllTrees()
    }

    fun showTabToPlayer(namespace: String, player: org.bukkit.entity.Player) {
        tabs[namespace]?.showTab(player)
    }

    fun grantRootAdvancement(namespace: String, player: org.bukkit.entity.Player) {
        val rootKey = rootKeys[namespace] ?: return
        if (isProviderOwned(namespace,rootKey)) return
        val root = advancementMap[namespace]?.get(rootKey) ?: return
        root.grant(player)
    }

    override fun getAdvancement(treeNamespace: String, key: String): Advancement? {
        return advancementMap[treeNamespace]?.get(key)
    }
    fun isProviderOwned(namespace:String,key:String):Boolean = namespace == "ekoth" ||
        requirementIndex.any { (type, targets) ->
            (type == RequirementType.EKOTH_VERIFIED_CHALLENGE || type.isRetiredKoth) &&
                targets.values.any { (namespace to key) in it }
        }

    fun projectKoth(player:org.bukkit.entity.Player,progress:Map<String,Int>) {
        require(progress.values.all { it in 0..100 }) { "Invalid verified KOTH progress" }
        progress.forEach { (challenge,percent) ->
            findByRequirement(RequirementType.EKOTH_VERIFIED_CHALLENGE,challenge).forEach { (namespace,key) ->
                getAdvancement(namespace,key)?.let { advancement ->
                    val count=(percent.coerceIn(0,100).toLong()*advancement.maxProgression/100).toInt()
                    advancement.setProgression(player,count,false)
                }
            }
        }
    }

    override fun getTreeNamespaces(): Set<String> {
        return tabs.keys.toSet()
    }

    override fun isTreeLoaded(namespace: String): Boolean {
        return namespace in tabs
    }

    override fun findByRequirement(type: RequirementType, target: String?): List<Pair<String, String>> {
        return requirementIndex[type]?.get(target).orEmpty()
    }
}
