package me.michi.mygroupsystem;

import java.util.Date;
import java.util.UUID;

public class GroupMember {
    private final UUID playerUUID;
    private Date expirationTime;

    public GroupMember(UUID playerUUID, long time){
        this.playerUUID = playerUUID;
        setTime(time);
    }

    public UUID getPlayerUUID(){
        return playerUUID;
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
