package io.github.badgersmc.advancements.pilot;

import java.util.List;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import io.github.badgersmc.advancements.pilot.ProjectionService.Node;

/** Validated provider input snapshot used to rebuild a disposed UAA tab. */
record Registration(ItemStack icon, List<Node> definitions, Set<String> privateKeys) {
    static Registration validated(
        String namespace,
        ItemStack icon,
        List<Node> definitions
    ) {
        return validated(namespace, icon, definitions, Set.of());
    }

    static Registration validated(String namespace, ItemStack icon, List<Node> definitions, Set<String> privateKeys) {
        validateInputs(namespace, icon, definitions);
        if (privateKeys == null) throw new IllegalArgumentException("Missing private keys");
        Registration registration = new Registration(icon.clone(), List.copyOf(definitions), Set.copyOf(privateKeys));
        ProjectionChecks.validateDefinitions(registration.definitions());
        ProjectionChecks.validatePrivateNodes(registration.definitions(), registration.privateKeys());
        validateDisplays(registration);
        return registration;
    }

    private static void validateInputs(String namespace, ItemStack icon, List<Node> definitions) {
        if (namespace == null || !namespace.matches("[a-z0-9._-]+")) {
            throw new IllegalArgumentException(
                "Invalid namespace: " + namespace
            );
        }
        if (icon == null) throw new IllegalArgumentException(
            "Missing root icon"
        );
        if (definitions == null) throw new IllegalArgumentException(
            "Missing node definitions"
        );
        for (Node definition : definitions) {
            if (definition == null) throw new IllegalArgumentException(
                "Null node definition"
            );
        }
    }

    private static void validateDisplays(Registration registration) {
        // Build displays before removing the old tab: UAA validates coordinates and icons here.
        PilotDisplays.rootDisplay(registration.icon());
        for (Node definition : registration.definitions())
            PilotDisplays.nodeDisplay(definition);
    }

}
