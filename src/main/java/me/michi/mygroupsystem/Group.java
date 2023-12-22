package me.michi.mygroupsystem;

import java.util.*;

public class Group {
    private String groupName;
    private String prefix;
    private Map<UUID, GroupMember> groupMembers;

    public Group(String groupName, String prefix){
        this.groupName = groupName;
        this.prefix = prefix;
        groupMembers = new HashMap<>();
    }

    public String getGroupPrefix(){
        return prefix;
    }

    public String getGroupName(){
        return groupName;
    }

    public GroupMember getGroupMember(UUID playerUUID){
        return groupMembers.get(playerUUID);
    }

    public List<GroupMember> getGroupMembers(){
        return new ArrayList<>(groupMembers.values());
    }

    public int getSize(){
        return groupMembers.size();
    }

    /**
     * Add the given player to the group as a group member with a specific time -> where 0 secs = permanently
     * @param playerUUID
     * @param time in seconds
     * @return new member
     */
    public GroupMember addGroupMember(UUID playerUUID, long time){
        GroupMember newMember = new GroupMember(playerUUID, time);
        groupMembers.put(playerUUID, newMember);
        return newMember;
    }

    public void removeGroupMember(UUID playerUUID){
        if(containsGroupMember(playerUUID)){
            groupMembers.remove(playerUUID);
        }
    }

    public boolean containsGroupMember(UUID playerUUID){
        return groupMembers.containsKey(playerUUID);
    }
}
