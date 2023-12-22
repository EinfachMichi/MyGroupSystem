package me.michi.mygroupsystem.listeners;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class GroupSystemListener implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getDisplayName();
        UUID playerUUID = event.getPlayer().getUniqueId();
        String message = event.getMessage();

        Group group = GroupManager.Instance.getGroup(playerUUID);
        if(group == null){
            return;
        }

        event.setFormat("%2$s");
        event.setMessage("§f[§6" + group.getGroupPrefix() + "§f] " + playerName + ": " + message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){

    }
}