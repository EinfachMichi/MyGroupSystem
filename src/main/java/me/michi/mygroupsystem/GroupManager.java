package me.michi.mygroupsystem;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class GroupManager {
    public static GroupManager Instance;

    private ArrayList<Group> groups;

    public GroupManager(){
        if(Instance == null){
            Instance = this;
            groups = new ArrayList<>();
        }
    }

    public void createGroup(String name, String prefix){
        groups.add(new Group(name, prefix));
    }

    public boolean contains(String name){
        for (Group group : groups){
            if(group.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    public void listPlayerInGroup(CommandSender commandSender, String name){
        Group listGroup = null;
        for (Group group : groups){
            if(group.getName().equals(name)){
                listGroup = group;
                break;
            }
        }

        if(listGroup == null) {
            commandSender.sendMessage("§cThere is no group with that name.");
            return;
        }

        String[] players = listGroup.getPlayerList();
        commandSender.sendMessage("§2Group: " + name + " | " + "§2players (" + players.length + ")");

        for (String player : players) {
            String output = "§a-" + player;

            if (!player.equals(players[players.length - 1])) {
                output += ",";
            }

            commandSender.sendMessage(output);
        }
    }

    public void addPlayerToGroup(String groupName, Player player){
        for (Group group : groups){
            if(group.getName().equals(groupName)){
                group.addPlayer(player);
                break;
            }
        }
    }
}
