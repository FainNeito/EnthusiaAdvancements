# EnthusiaAdvancements projection pilot

This Maven module is a small test-server distribution of EnthusiaAdvancements. It deliberately does not launch the full Kotlin/Nexus distribution, copy its bundled trees, register unrelated plugin listeners, or execute advancement rewards. Only provider-supplied definitions are displayed. It uses the same plugin name, so never install both builds together. It is not a replacement for a production installation already using the full distribution's other trees.

Build with Java 25 and Maven: `mvn clean install`. Use the same workspace Maven repository when building EnthusiaTags afterward. Paper API: 26.2.build.124-stable. Required runtime plugin: UltimateAdvancementAPI 2.8.1 (separate JAR, not shaded).

`ProjectionService` is registered through Bukkit's services manager. Providers register an owned namespace, ordered node definitions, and a root icon. They submit verified 0..1000 progress; the client sees 0..100 percent. Missing entries leave existing display unchanged. Providers separately request live celebration only after their own durable completion handling. All automatic toast/chat display flags are false, and no reward executor exists here.

Calls run on the server thread. UAA player data must be loaded and the player must have an individual UAA team. Shared teams are not modified or split automatically; this prevents personal progress leaking across accounts. Keep UAA's default individual-player teams for this pilot.

Server/client rendering remains a staging check. 26.3 is not verified. No vanilla advancements are disabled or replaced.
