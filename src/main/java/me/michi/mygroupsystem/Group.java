package me.michi.mygroupsystem;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Group {
    private String name;
    private String prefix;
    private Map<UUID, GroupMember> groupMembers;

    public Group(String name, String prefix){
        this.name = name;
        this.prefix = prefix;
        groupMembers = new HashMap<>();
    }

    public String getGroupPrefix(){
        return "[" + prefix + "]";
    }

    public String getGroupName(){
        return name;
    }

    public void addGroupMember(Player player, long seconds){
        //TODO: add timer

        GroupMember newMember = new GroupMember(player.getDisplayName());
        newMember.setSeconds(seconds);
        groupMembers.put(player.getUniqueId(), newMember);
    }

    public void removeGroupMember(Player player){
        groupMembers.remove(player.getUniqueId());
    }

    public String[] getGroupMemberList(){
        return groupMembers.values().toArray(new String[0]);
    }

    public boolean containsGroupMember(Player player){
        return groupMembers.containsKey(player.getUniqueId());
    }

    public String[] getGroupMemberInfos(){
        GroupMember[] groupMemberInfos = groupMembers.values().toArray(new GroupMember[0]);
        String[] infos = new String[groupMemberInfos.length];
        for (int i = 0; i < groupMemberInfos.length; i++) {
            infos[i] = "§a- " + groupMemberInfos[i].getName();
            if(groupMemberInfos[i].getSeconds() > 0){
                infos[i] += " (" + getTimeString(groupMemberInfos[i].getSeconds()) + ")";
            }
        }
        return infos;
    }

    private String getTimeString(long seconds){
        if (seconds < 0) {
            return "";
        }

        long days = seconds / (24 * 60 * 60);
        long hours = (seconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = ((seconds % (24 * 60 * 60)) % (60 * 60)) / 60;
        long remainingSeconds = ((seconds % (24 * 60 * 60)) % (60 * 60)) % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        }

        if (hours > 0) {
            result.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        }

        if (minutes > 0) {
            result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(" ");
        }

        if (remainingSeconds > 0) {
            result.append(remainingSeconds).append(" second").append(remainingSeconds > 1 ? "s" : "");
        }

        return result.toString();
    }
}
