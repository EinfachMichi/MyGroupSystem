package me.michi.mygroupsystem;

import me.michi.mygroupsystem.database.GroupSystemDataBase;
import me.michi.mygroupsystem.logs.GroupLogFlag;
import me.michi.mygroupsystem.logs.GroupSystemLogType;
import me.michi.mygroupsystem.logs.GroupSystemLogger;

import java.util.*;

public class GroupSystemManager {
    private static GroupSystemManager instance;
    private final long INTERVAL = 1000; // 1 second

    private Timer expirationTimer = new Timer(true);
    private Map<GroupMember, Date> expirationTimes = new HashMap<>();
    private ArrayList<Group> groups = new ArrayList<>();;

    private GroupSystemManager(){
        startExpirationTimer();
    }

    public static GroupSystemManager getInstance() {
        if (instance == null) {
            instance = new GroupSystemManager();
        }
        return instance;
    }

    public Group createGroup(String name, String prefix){
        if(groupExists(name)){
            return null;
        }

        Group newGroup = new Group(name, prefix);
        groups.add(newGroup);
        GroupSystemDataBase.uploadGroup(newGroup);
        return newGroup;
    }

    public GroupMember addPlayerToGroup(UUID playerUUID, String displayName, String groupName, long seconds){
        Group group = getGroup(groupName);
        if(group == null){
            return null;
        }

        GroupMember newMember = group.addGroupMember(playerUUID, displayName, seconds);
        if(newMember == null){
            return null;
        }

        GroupSystemDataBase.uploadGroupMember(newMember);
        assignGroupMemberWithExpirationTime(newMember);
        return newMember;
    }

    public boolean removePlayerFromGroup(UUID playerUUID, long seconds){
        if(!playerHasGroup(playerUUID)){
            return false;
        }

        Group group = getGroup(playerUUID);
        GroupMember groupMember = getGroupMember(playerUUID);

        if(seconds == 0){
            group.removeGroupMember(playerUUID);
            groupMember = addPlayerToGroup(playerUUID, groupMember.getDisplayName(), "Player", 0);
        }
        else{
            groupMember.setTime(seconds);
            assignGroupMemberWithExpirationTime(groupMember);
        }

        GroupSystemDataBase.uploadGroupMember(groupMember);
        return true;
    }

    public boolean groupExists(String groupName){
        return groups.stream().anyMatch(group -> group.getGroupName().equals(groupName));
    }

    public boolean playerInGroup(String groupName, UUID playerUUID){
        Group group = getGroup(groupName);
        return group != null && group.containsGroupMember(playerUUID);
    }

    public boolean playerHasGroup(UUID playerUUID){
        return groups.stream().anyMatch(group -> group.containsGroupMember(playerUUID));
    }

    public String[] getGroupNames() {
        return groups.stream()
                .map(Group::getGroupName)
                .toArray(String[]::new);
    }


    public GroupMember getGroupMember(UUID playerUUID){
        return groups.stream()
                .filter(group -> group.containsGroupMember(playerUUID))
                .findFirst()
                .map(group -> group.getGroupMember(playerUUID))
                .orElse(null);
    }

    public Group getGroup(UUID playerUUID){
        return groups.stream()
                .filter(group -> group.containsGroupMember(playerUUID))
                .findFirst()
                .orElse(null);
    }

    public Group getGroup(String groupName){
        return groups.stream()
                .filter(group -> group.getGroupName().equals(groupName))
                .findFirst()
                .orElse(null);
    }

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

            if(currentTime.after(expirationTime)){
                GroupSystemLogger.getInstance().addLogToQueue(
                        groupMember.getPlayerUUID(),
                        GroupSystemLogType.you_got_removed_from_group,
                        new GroupLogFlag("{group}", groupMember.getGroupName())
                );
                removePlayerFromGroup(groupMember.getPlayerUUID(), 0);
                expirationTimes.remove(groupMember);
            }
        }
    }
}