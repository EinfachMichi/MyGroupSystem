package me.michi.mygroupsystem.listeners;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupMember;
import me.michi.mygroupsystem.GroupSystemManager;
import me.michi.mygroupsystem.logs.GroupSystemLogType;
import me.michi.mygroupsystem.logs.GroupSystemLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GroupSystemListener implements Listener {

    private final JavaPlugin javaPlugin;

    public GroupSystemListener(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        Group group = GroupSystemManager.getInstance().getGroup(playerUUID);
        if (group == null) {
            return;
        }

        String playerName = player.getDisplayName();
        String message = event.getMessage();

        event.setFormat("%2$s");
        event.setMessage("§f[§6" + group.getGroupPrefix() + "§f] " + playerName + ": " + message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        GroupMember groupMember;
        if (!GroupSystemManager.getInstance().playerHasGroup(playerUUID)) {
            groupMember = GroupSystemManager.getInstance().addPlayerToGroup(playerUUID, player.getDisplayName(), "Player", 0);
        } else {
            groupMember = GroupSystemManager.getInstance().getGroupMember(playerUUID);
        }

        event.setJoinMessage(null);

        String serverJoinedMessage = GroupSystemLogger.getInstance().getLogMessage(GroupSystemLogType.player_joined_the_server);
        serverJoinedMessage = serverJoinedMessage.replace("{group}", groupMember.getGroupName());
        serverJoinedMessage = serverJoinedMessage.replace("{player}", groupMember.getDisplayName());
        Bukkit.broadcastMessage(serverJoinedMessage);

        Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
            GroupSystemLogger.getInstance().trySendLogs(player);
        }, 10);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        GroupMember groupMember = GroupSystemManager.getInstance().getGroupMember(event.getPlayer().getUniqueId());

        event.setQuitMessage(null);

        String serverJoinedMessage = GroupSystemLogger.getInstance().getLogMessage(GroupSystemLogType.player_left_the_server);
        serverJoinedMessage = serverJoinedMessage.replace("{group}", groupMember.getGroupName());
        serverJoinedMessage = serverJoinedMessage.replace("{player}", groupMember.getDisplayName());
        Bukkit.broadcastMessage(serverJoinedMessage);
    }
}
