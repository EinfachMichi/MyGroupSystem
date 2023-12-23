package me.michi.mygroupsystem;

import me.michi.mygroupsystem.database.GroupMemberData;
import org.bukkit.Bukkit;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class GroupMember {
    private final UUID playerUUID;
    private final String displayName;
    private final String groupName;
    private Date expirationTime;

    public GroupMember(UUID playerUUID, String displayName, String groupName, long time){
        this.playerUUID = playerUUID;
        this.displayName = displayName;
        this.groupName = groupName;
        setTime(time);
    }

    public UUID getPlayerUUID(){
        return playerUUID;
    }

    public String getDisplayName(){
        return displayName;
    }

    public String getGroupName() {
        return groupName;
    }

    public long getRemainingSeconds(){
        long currentTime = System.currentTimeMillis();
        long remainingTime = expirationTime.getTime() - currentTime;

        return Math.max(remainingTime / 1000, 0);
    }

    public void setTime(long seconds){
        this.expirationTime = new Date(System.currentTimeMillis() + seconds * 1000);
    }
}
