package me.michi.mygroupsystem;

import me.michi.mygroupsystem.database.GroupData;
import me.michi.mygroupsystem.database.GroupMemberData;
import me.michi.mygroupsystem.database.GroupSystemDataBase;
import me.michi.mygroupsystem.database.ServerInfo;
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

    public void createGroups(List<GroupData> groupDataList) {
        for (GroupData groupData : groupDataList) {
            createGroup(groupData.groupName(), groupData.prefix());
        }
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

    public void addGroupMembers(List<GroupMemberData> groupMemberDataList) {
        for (GroupMemberData groupMemberData : groupMemberDataList) {
            addPlayerToGroup(
                    groupMemberData.playerUUID(),
                    groupMemberData.displayName(),
                    groupMemberData.groupName(),
                    groupMemberData.seconds()
            );
        }
    }

    public void removePlayerFromGroup(UUID playerUUID, long seconds){
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
    }

    public void removeGroup(Group group){
        if(group.getGroupName().equals("Player")){
            return;
        }

        GroupMember[] groupMembers = getGroupMembers(group.getGroupName());
        for (GroupMember groupMember : groupMembers){
            addPlayerToGroup(groupMember.getPlayerUUID(), groupMember.getDisplayName(), "Player", 0);
        }
        groups.remove(group);
        GroupSystemDataBase.deleteGroupEntry(group);
    }

    public void manageServerInfo(ServerInfo serverInfo){
        GroupMember[] groupMembers = getGroupAllGroupMembers();
        for (GroupMember groupMember : groupMembers){
            if(groupMember.getRemainingSeconds() > 0){
                long timePassed = System.currentTimeMillis() - serverInfo.lastTimeOnline();
                timePassed /= 1000;
                if(groupMember.getRemainingSeconds() - timePassed < 0){
                    GroupSystemLogger.getInstance().addLogToQueue(
                            groupMember.getPlayerUUID(),
                            GroupSystemLogType.you_got_removed_from_group,
                            new GroupLogFlag("{group}", groupMember.getGroupName())
                    );
                    removePlayerFromGroup(groupMember.getPlayerUUID(), 0);
                    expirationTimes.remove(groupMember);
                }
                else{
                    groupMember.setTime(groupMember.getRemainingSeconds() - timePassed);
                    assignGroupMemberWithExpirationTime(groupMember);
                }
            }
        }
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

    public GroupMember getGroupMember(String displayName){
        return groups.stream()
                .filter(group -> group.containsGroupMember(displayName))
                .findFirst()
                .map(group -> group.getGroupMember(displayName))
                .orElse(null);
    }

    public GroupMember[] getGroupAllGroupMembers() {
        return groups.stream()
                .flatMap(group -> group.getGroupMembers().stream())
                .toArray(GroupMember[]::new);
    }

    public GroupMember[] getGroupMembers(String groupName) {
        return groups.stream()
                .filter(group -> group.getGroupName().equals(groupName))
                .findFirst()
                .map(Group::getGroupMembers)
                .orElse(Collections.emptyList())
                .toArray(GroupMember[]::new);
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