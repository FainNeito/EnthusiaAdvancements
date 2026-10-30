package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Fail-closed, per-session visibility; the provider owns authoritative completion. */
final class PrivateAdvancement extends BaseAdvancement {
    private final Set<UUID> eligible = ConcurrentHashMap.newKeySet();

    PrivateAdvancement(String key, AdvancementDisplay display, Advancement parent) {
        super(key, display, parent, 100);
    }

    void authorize(UUID player, boolean complete) {
        if (complete) eligible.add(player); else eligible.remove(player);
    }

    @Override public boolean isVisible(TeamProgression team) {
        try {
            return team != null && team.getSize() == 1 && eligible.contains(team.getAMember());
        } catch (Throwable failure) {
            return false;
        }
    }

    @Override public void onUpdate(TeamProgression team, Map<AdvancementWrapper, Integer> outgoing) {
        try {
            // UAA can reuse the map after a packet-construction exception; remove stale private data first.
            AdvancementWrapper wrapper = getNMSWrapper();
            outgoing.remove(wrapper);
            if (isVisible(team)) outgoing.put(wrapper, 100);
        } catch (Throwable failure) {
            // If we cannot identify/remove our wrapper, never allow a potentially stale map to be sent.
            outgoing.clear();
        }
    }
}
