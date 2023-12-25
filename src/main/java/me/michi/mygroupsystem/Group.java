package me.michi.mygroupsystem;

import java.util.*;

public class Group {
    private final String groupName;
    private final String prefix;
    private final Map<UUID, GroupMember> groupMembers;

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

    public GroupMember getGroupMember(String displayName) {
        for (GroupMember member : groupMembers.values()) {
            if (member.getDisplayName().equals(displayName)) {
                return member;
            }
        }
        return null;
    }

    public List<GroupMember> getGroupMembers(){
        return new ArrayList<>(groupMembers.values());
    }

    public int getSize(){
        return groupMembers.size();
    }

    public GroupMember addGroupMember(UUID playerUUID, String displayName, long seconds){
        if(groupMembers.containsKey(playerUUID)){
            return null;
        }

        GroupMember newMember = new GroupMember(playerUUID, displayName, groupName, seconds);
        groupMembers.put(playerUUID, newMember);
        return newMember;
    }

    public void removeGroupMember(UUID playerUUID){
        groupMembers.remove(playerUUID);
    }

    public boolean containsGroupMember(UUID playerUUID){
        return groupMembers.containsKey(playerUUID);
    }

    public boolean containsGroupMember(String displayName){
        for (GroupMember member : groupMembers.values()) {
            if (member.getDisplayName().equals(displayName)) {
                return true;
            }
        }
        return false;
    }
}
