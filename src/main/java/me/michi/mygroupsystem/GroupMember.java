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

    /**
     * Returns the name of the group member
     * @return name
     */
    public String getName(){
        return name;
    }

    /**
     * Returns the UUID of the player
     * @return playerUUID
     */
    public UUID getPlayerUUID(){
        return playerUUID;
    }

    /**
     * Returns remaining seconds until the player gets removed from the group
     * @return remaining seconds
     */
    public long getRemainingSeconds(){
        long currentTime = System.currentTimeMillis();
        long remainingTime = expirationTime.getTime() - currentTime;

        return Math.max(remainingTime / 1000, 0);
    }

    /**
     * Sets the time for before the player gets removed from the group
     * @param seconds seconds
     */
    public void setTime(long seconds){
        this.expirationTime = new Date(System.currentTimeMillis() + seconds * 1000);
    }
}
