package io.github.badgersmc.advancements.pilot;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.PlayerUtil;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/** Best-effort DiscordSRV delivery; never controls toasts, progress, or rewards. */
final class DiscordAdvancementAnnouncer {
    private final JavaPlugin owner;
    private long nextWarning;
    private final Semaphore iconJobs = new Semaphore(8);

    DiscordAdvancementAnnouncer(JavaPlugin owner) {
        this.owner = owner;
    }

    void announce(Player player, String namespace, ProjectionService.Node node) {
        if (!bridgeEnabled()) return;
        try {
            announceVisible(player, namespace, node);
        } catch (RuntimeException | LinkageError failure) {
            warn("Discord advancement bridge unavailable", failure);
        }
    }

    private void announceVisible(Player player, String namespace, ProjectionService.Node node) {
        if (PlayerUtil.isVanished(player)) return;
        var channel = destination();
        if (channel != null) publish(player, namespace, node, channel);
    }

    private void publish(Player player, String namespace, ProjectionService.Node node,
        TextChannel channel) {
        var embed = createEmbed(player, node);
        String url = iconUrl(namespace, node.key());
        if (url != null) embed.setThumbnail(url);
        if (url == null && renderIcon(player, node, embed, channel)) return;
        send(embed, channel, null);
    }

    private boolean bridgeEnabled() {
        return owner.getConfig().getBoolean("discord.enabled", false)
            && Bukkit.getPluginManager().isPluginEnabled("DiscordSRV");
    }

    private TextChannel destination() {
        var discord = DiscordSRV.getPlugin();
        String configured = owner.getConfig().getString("discord.channel", "awards");
        var channel = discord.getDestinationTextChannelForGameChannelName(
            discord.getOptionalChannel(configured));
        if (channel == null)
            warn("DiscordSRV advancement channel is unavailable: " + configured, null);
        return channel;
    }

    private EmbedBuilder createEmbed(Player player, ProjectionService.Node node) {
        var notice = DiscordAdvancementNotice.from(player.getName(), node);
        var embed = new EmbedBuilder().setColor(notice.color()).setTitle(notice.heading())
            .setDescription(notice.description());
        String avatarUrl = DiscordSRV.getAvatarUrl(player);
        if (avatarUrl != null && avatarUrl.startsWith("https://"))
            embed.setAuthor(player.getName(), null, avatarUrl);
        return embed;
    }

    private boolean renderIcon(Player player, ProjectionService.Node node,
        EmbedBuilder embed, TextChannel channel) {
        if (!owner.getConfig().getBoolean("discord.auto-icons", true)) return false;
        var addon = Bukkit.getPluginManager().getPlugin("InteractiveChatDiscordSrvAddon");
        if (!reserveIconJob(addon)) return false;
        try {
            var job = DiscordAdvancementIcon.prepare(addon.getClass().getClassLoader(), player,
                PilotDisplays.createIcon(node), node.frame());
            var pending = new PendingIcon(player, addon, embed, channel, job);
            Bukkit.getScheduler().runTaskAsynchronously(owner, () -> render(pending));
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            iconJobs.release();
            warn("Discord advancement renderer unavailable; sending text only", failure);
            return false;
        }
    }

    private boolean reserveIconJob(Plugin addon) {
        return addon != null && addon.isEnabled() && iconJobs.tryAcquire();
    }

    private void render(PendingIcon pending) {
        byte[] attachment = null;
        try {
            if (owner.isEnabled() && pending.addon().isEnabled())
                attachment = pending.job().render();
        } catch (ReflectiveOperationException | java.io.IOException | RuntimeException | LinkageError failure) {
            warn("Discord advancement icon unavailable; sending text only", failure);
        } finally {
            iconJobs.release();
        }
        scheduleDelivery(pending, attachment);
    }

    private void scheduleDelivery(PendingIcon pending, byte[] attachment) {
        if (!owner.isEnabled()) return;
        try {
            Bukkit.getScheduler().runTask(owner, () -> deliver(pending, attachment));
        } catch (RuntimeException failure) {
            warn("Discord advancement callback unavailable", failure);
        }
    }

    private void deliver(PendingIcon pending, byte[] attachment) {
        try {
            if (!owner.isEnabled() || PlayerUtil.isVanished(pending.player())) return;
            send(pending.embed(), pending.channel(), attachment);
        } catch (RuntimeException | LinkageError failure) {
            warn("Discord advancement delivery unavailable", failure);
        }
    }

    private void send(EmbedBuilder embed, TextChannel channel, byte[] attachment) {
        if (attachment != null) embed.setThumbnail("attachment://advancement.png");
        var message = channel.sendMessageEmbeds(embed.build());
        if (attachment != null) message.addFile(attachment, "advancement.png");
        message.queue(sent -> { }, failure -> warn("Discord advancement delivery failed", failure));
    }

    private String iconUrl(String namespace, String key) {
        var configured = owner.getConfig().getConfigurationSection("discord.icon-urls");
        Map<String, Object> urls = configured == null ? Map.of() : configured.getValues(false);
        Object value = urls.get(namespace + "/" + key);
        return value instanceof String url && url.startsWith("https://") ? url : null;
    }

    private synchronized void warn(String message, Throwable failure) {
        if (System.currentTimeMillis() < nextWarning) return;
        nextWarning = System.currentTimeMillis() + 60_000;
        owner.getLogger().log(Level.WARNING, message, failure);
    }

    private record PendingIcon(Player player, Plugin addon, EmbedBuilder embed,
        TextChannel channel, DiscordAdvancementIcon.RenderJob job) {}
}
