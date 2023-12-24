package me.michi.mygroupsystem.listeners;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupMember;
import me.michi.mygroupsystem.GroupSystemManager;
import me.michi.mygroupsystem.logs.GroupLogFlag;
import me.michi.mygroupsystem.logs.GroupSystemLogType;
import me.michi.mygroupsystem.logs.GroupSystemLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GroupSystemListener implements Listener {

    private final JavaPlugin javaPlugin;

    public GroupSystemListener(JavaPlugin javaPlugin){
        this.javaPlugin = javaPlugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getDisplayName();
        UUID playerUUID = event.getPlayer().getUniqueId();
        String message = event.getMessage();

        Group group = GroupSystemManager.Instance.getGroup(playerUUID);
        if(group == null){
            return;
        }

        event.setFormat("%2$s");
        event.setMessage("§f[§6" + group.getGroupPrefix() + "§f] " + playerName + ": " + message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        GroupMember groupMember;

        if(!GroupSystemManager.Instance.playerHasGroup(player.getUniqueId())){

            groupMember = GroupSystemManager.Instance.addPlayerToGroup(
                    player.getUniqueId(), player.getDisplayName(), "Player", 0
            );
        }
        else{
            groupMember = GroupSystemManager.Instance.getGroupMember(player.getUniqueId());
        }

        event.setJoinMessage(null);

        String serverJoinedMessage = GroupSystemLogger.getLogMessage(GroupSystemLogType.player_joined_the_server);
        serverJoinedMessage = serverJoinedMessage.replace("{group}", groupMember.getGroupName());
        serverJoinedMessage = serverJoinedMessage.replace("{player}", groupMember.getDisplayName());
        Bukkit.broadcastMessage(serverJoinedMessage);

        // send logs a bit delayed
        Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {

            GroupSystemLogger.trySendLogs(player);
        }, 10);
    }
}