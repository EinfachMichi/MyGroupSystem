package me.michi.mygroupsystem;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Group {
    private String name;
    private String prefix;
    private Map<UUID, String> players;

    public Group(String name, String prefix){
        this.name = name;
        this.prefix = prefix;
        players = new HashMap<>();
    }

    public String getPrefix(){
        return "[" + prefix + "]";
    }

    public String getName(){
        return name;
    }

    public void addPlayer(Player player){
        players.put(player.getUniqueId(), player.getDisplayName());
    }

    public String[] getPlayerList(){
        return players.values().toArray(new String[0]);
    }
}
