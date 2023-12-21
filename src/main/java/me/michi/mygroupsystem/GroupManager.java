package me.michi.mygroupsystem;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    public boolean contains(String groupName){
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                return true;
            }
        }
        return false;
    }

    public void listPlayerInGroup(CommandSender commandSender, String groupName){
        Group listGroup = null;
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                listGroup = group;
                break;
            }
        }

        if(listGroup == null) {
            commandSender.sendMessage("§cThat group doesn't exist.");
            return;
        }

        String[] players = listGroup.getGroupMemberList();
        commandSender.sendMessage("§2Group: " + groupName + " | " + "§2players (" + players.length + ")\n");

        for (String player : players) {
            String output = "§a-" + player;

            if (!player.equals(players[players.length - 1])) {
                output += ",";
            }

            commandSender.sendMessage(output);
        }
    }

    public void showInfo(CommandSender commandSender, String groupName){
        Group infoGroup = null;
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                infoGroup = group;
                break;
            }
        }

        if(infoGroup == null){
            commandSender.sendMessage("§cThat group doesn't exist.");
            return;
        }

        String[] players = infoGroup.getGroupMemberInfos();
        commandSender.sendMessage("§2Group: " + groupName + " | " + "§2players (" + players.length + ")\n");

        for (String player : players) {
            commandSender.sendMessage("§a-" + player);
        }
    }

    public String getGroupName(Player player){
        for (Group group : groups){
            if(group.containsGroupMember(player)){
                return group.getGroupName();
            }
        }
        return "";
    }

    public boolean addPlayerToGroup(String groupName, Player player, long time){
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                group.addGroupMember(player, time);
                return true;
            }
        }
        return false;
    }

    public void removePlayerFromGroup(Player player) {
        for (Group group : groups) {
            if (group.containsGroupMember(player)) {
                group.removeGroupMember(player);
                break;
            }
        }
    }
}
