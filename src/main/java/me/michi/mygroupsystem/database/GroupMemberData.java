package me.michi.mygroupsystem.database;

import java.util.UUID;

public class GroupMemberData {
    private UUID playerUUID;
    private String groupName;
    private long seconds;

    public GroupMemberData(UUID playerUUID, String groupName, long seconds){
        this.playerUUID = playerUUID;
        this.groupName = groupName;
        this.seconds = seconds;
    }

    public UUID getPlayerUUID(){
        return playerUUID;
    }

    public String getGroupName(){
        return groupName;
    }

    public long getSeconds(){
        return seconds;
    }
}
