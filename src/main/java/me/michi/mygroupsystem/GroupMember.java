package me.michi.mygroupsystem;

import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class GroupMember {
    private UUID playerUUID;
    private String name;
    private Date expirationTime;

    public GroupMember(Player player){
        this.name = player.getDisplayName();
        this.playerUUID = player.getUniqueId();
    }

    public String getName(){
        return name;
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
