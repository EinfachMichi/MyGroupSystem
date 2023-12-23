package me.michi.mygroupsystem;

import me.michi.mygroupsystem.database.GroupMemberData;
import me.michi.mygroupsystem.database.GroupSystemDataBase;

import java.util.*;

public class GroupSystemManager {
    public static GroupSystemManager Instance;

    private final long INTERVAL = 1000; // 1 second
    private Timer expirationTimer = new Timer(true);
    private Map<GroupMember, Date> expirationTimes = new HashMap<>();

    private ArrayList<Group> groups = new ArrayList<>();;

    public GroupSystemManager(){
        if(Instance == null){
            Instance = this;
            startExpirationTimer();
        }
    }

    /**
     *  Creates a new group with the given name and prefix
     * @param name
     * @param prefix
     * @return null if group already exists
     */
    public Group createGroup(String name, String prefix){
        if(groupExist(name)){
            return null;
        }

        Group newGroup = new Group(name, prefix);
        groups.add(newGroup);
        GroupSystemDataBase.uploadGroup(newGroup);
        return newGroup;
    }

    /**
     * Add given group member to the given group for a specific time -> where 0 seconds is permanently
     * @param playerUUID
     * @param displayName
     * @param groupName
     * @param seconds
     * @return group member that is getting created
     */
    public GroupMember addPlayerToGroup(UUID playerUUID, String displayName, String groupName, long seconds){

        Group group = getGroup(groupName);
        GroupMember newMember = group.addGroupMember(playerUUID, displayName, seconds);

        if(newMember == null){
            return null;
        }

        GroupSystemDataBase.uploadGroupMember(newMember);
        assignGroupMemberWithExpirationTime(newMember);
        return newMember;
    }

    /**
     * Removes player from group instantly or after a period of time
     * @param playerUUID
     */
    public boolean removePlayerFromGroup(UUID playerUUID, long seconds){
        if(!playerHasGroup(playerUUID)){
            return false;
        }

        Group group = getGroup(playerUUID);
        if(seconds == 0){
            group.removeGroupMember(playerUUID);
            return true;
        }

        GroupMember groupMember = getGroupMember(playerUUID);
        groupMember.setTime(seconds);
        assignGroupMemberWithExpirationTime(groupMember);
        return true;
    }

    /**
     * Check if the group exists
     * @param groupName
     * @return false if group doesn't exist
     */
    public boolean groupExist(String groupName){
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the player exists in the given group
     * @param groupName
     * @return false if given player is not in given group
     */
    public boolean playerInGroup(String groupName, UUID playerUUID){
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                return group.containsGroupMember(playerUUID);
            }
        }
        return false;
    }

    /**
     * Check if the player has a group
     * @param playerUUID
     * @return false if given player is not in a group
     */
    public boolean playerHasGroup(UUID playerUUID){
        for (Group group : groups){
            if(group.containsGroupMember(playerUUID)){
                return true;
            }
        }
        return false;
    }

    /**
     * Return the group member object of the player
     * @param playerUUID playerUUID
     * @return group name
     */
    public GroupMember getGroupMember(UUID playerUUID){
        for (Group group : groups){
            if(group.containsGroupMember(playerUUID)){
                return group.getGroupMember(playerUUID);
            }
        }
        return null;
    }

    /**
     * Returns the group that the player is in
     * @param playerUUID
     * @return null if player has no group
     */
    public Group getGroup(UUID playerUUID){
        for (Group group : groups){
            if(group.containsGroupMember(playerUUID)){
                return group;
            }
        }
        return null;
    }

    /**
     * Tries to return the group with the given name
     * @param groupName
     * @return null if no group found
     */
    public Group getGroup(String groupName){
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                return group;
            }
        }
        return null;
    }

    /**
     * @return all groups
     */
    public Group[] getGroups(){
        return groups.toArray(new Group[0]);
    }

    private void startExpirationTimer(){
        expirationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkExpirationTimes();
            }
        }, 0, INTERVAL);
    }

    private void assignGroupMemberWithExpirationTime(GroupMember member){
        if(member.getRemainingSeconds() > 0){
            Date expirationTime = calculateExpirationTime(member.getRemainingSeconds());
            expirationTimes.put(member, expirationTime);
        }
    }

    private Date calculateExpirationTime(long seconds){
        long currentTime = System.currentTimeMillis();
        return new Date(currentTime + seconds * 1000);
    }

    private void checkExpirationTimes(){
        Date currentTime = new Date();

        for (Map.Entry<GroupMember, Date> entry : expirationTimes.entrySet()){
            GroupMember groupMember = entry.getKey();
            Date expirationTime = entry.getValue();

            // if the time is expired, kick the player out of the group
            if(currentTime.after(expirationTime)){
                // TODO: implement logic for if the player is not on the server
                removePlayerFromGroup(groupMember.getPlayerUUID(), 0);
                expirationTimes.remove(groupMember);
            }
        }
    }
}