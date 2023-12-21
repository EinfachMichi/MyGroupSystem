package me.michi.mygroupsystem;

import org.bukkit.entity.Player;

import java.util.*;

public class Group {
    private String name;
    private String prefix;
    private Map<UUID, GroupMember> groupMembers;

    public Group(String name, String prefix){
        this.name = name;
        this.prefix = prefix;
        groupMembers = new HashMap<>();
    }

    /**
     * @return the group prefix
     */
    public String getGroupPrefix(){
        return "[" + prefix + "]";
    }

    /**
     * @return the group name
     */
    public String getGroupName(){
        return name;
    }

    /**
     * @return the group member
     */
    public GroupMember getGroupMember(UUID playerUUID){
        return groupMembers.get(playerUUID);
    }

    /**
     * @return player count in that group
     */
    public int getSize(){
        return groupMembers.size();
    }

    /**
     * Add the given player to the group AS a group member for seconds
     * @param player player
     * @param seconds seconds
     * @return new member
     */
    public GroupMember addGroupMember(Player player, long seconds){
        GroupMember newMember = new GroupMember(player);
        newMember.setTime(seconds);
        groupMembers.put(player.getUniqueId(), newMember);
        return newMember;
    }

    /**
     * Removes the given group member from the group
     * @param playerUUID player
     */
    public void removeGroupMember(UUID playerUUID){
        groupMembers.remove(playerUUID);
    }

    /**
     * @param playerUUID playerUUID
     * @return true if player is part of the group
     */
    public boolean containsGroupMember(UUID playerUUID){
        return groupMembers.containsKey(playerUUID);
    }

    /**
     * @return a String[] with all infos about the group members in this group
     */
    public List<String> getGroupMemberInfos() {
        List<String> infos = new ArrayList<>();

        for (GroupMember groupMember : groupMembers.values()) {
            StringBuilder infoBuilder = new StringBuilder("§a-- ").append(groupMember.getName());

            if (groupMember.getRemainingSeconds() > 0) {
                infoBuilder.append(" (").append(getTimeString(groupMember.getRemainingSeconds())).append(")");
            }

            infos.add(infoBuilder.toString());
        }

        return infos;
    }

    /**
     * @param seconds seconds
     * @return format(day, hours, minutes, seconds)
     */
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
