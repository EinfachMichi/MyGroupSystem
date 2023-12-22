package me.michi.mygroupsystem;

import org.bukkit.Bukkit;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class GroupMember {
    private final UUID playerUUID;
    private String displayName;
    private Date expirationTime;

    public GroupMember(UUID playerUUID, long time){
        this.playerUUID = playerUUID;
        displayName = Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getDisplayName();
        setTime(time);
    }

    public UUID getPlayerUUID(){
        return playerUUID;
    }

    public String getDisplayName(){
        return displayName;
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
