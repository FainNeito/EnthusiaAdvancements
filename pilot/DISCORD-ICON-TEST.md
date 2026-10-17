# Discord compact icon test.3

Local-only build: 1.0.0-pilot.6-discord-test.3.

Discord descriptions now show only the objective (at most two lines joined into
one paragraph, capped at 240 characters). Duplicate criteria, provider headings,
progress explanations, rewards and claim instructions are omitted. Full in-game
tooltips are unchanged. TASK and GOAL embed accent colors are gold (#FFAA00);
CHALLENGE embed accents are dark red (#AA0000). This does not recolor vanilla
resource-pack advancement frame textures.

The shared live celebration hook now attaches a PNG thumbnail rendered by
InteractiveChatDiscordSrvAddon using its vanilla advancement renderer. The
advancement's existing material, custom model data and item model are preserved;
the TASK/GOAL/CHALLENGE frame is rendered in completed state. No resource-pack
assets, progression rules, rewards or historical announcements are changed.

Requires DiscordSRV with the destination channel configured, plus enabled
InteractiveChat and InteractiveChatDiscordSrvAddon for automatic thumbnails.
Existing HTTPS discord.icon-urls overrides take priority. discord.auto-icons
defaults to true even for existing configs; false disables automatic rendering.
If the optional renderer is absent, incompatible, fails or is saturated (eight
concurrent jobs), delivery falls back to the existing text embed. Rendering runs
off the tick thread; vanished-player checks and delivery preparation run on the
main thread. Delivery failures are logged, not retried with ambiguous duplicates.

Custom Nexo icons require the matching resource pack to be loaded by the addon,
not just by the Minecraft client.

Verification: local Maven tests/package; installed 2026.1.1.0 addon and
InteractiveChat method signatures inspected. This is not a live Discord test.

Acceptance on SMP Test, after separately authorized installation:

1. Trigger a NEW live completion with a vanilla item; confirm exactly one Discord
   embed with the correct item and completed frame.
2. Check TASK, GOAL and CHALLENGE, including a Nexo custom-item icon with the
   matching addon resource pack.
3. Confirm historical backfill remains silent and rewards/progress unchanged.
4. Disable auto-icons or remove the optional addon; confirm text delivery works.
5. Confirm a configured HTTPS override takes priority and vanished players do not
   produce announcements.

No server deployment or PR is included in this change.
