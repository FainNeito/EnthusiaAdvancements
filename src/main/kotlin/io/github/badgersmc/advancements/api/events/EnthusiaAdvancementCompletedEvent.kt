package io.github.badgersmc.advancements.api.events

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Fired once when tracked gameplay progression causes a custom advancement node
 * to cross from incomplete to complete.
 *
 * Administrative grants and root-tab bootstrap are intentionally outside this
 * event because they do not pass through GrantProgress.
 */
class EnthusiaAdvancementCompletedEvent(
    val player: Player,
    val namespace: String,
    val key: String,
) : Event() {
    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }

    override fun getHandlers(): HandlerList = Companion.handlers
}
