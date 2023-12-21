package me.michi.mygroupsystem;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class GroupManager {
    public static GroupManager Instance;

    private final long INTERVAL = 1000; // 1 second
    private Timer expirationTimer = new Timer(true);
    private Map<GroupMember, Date> expirationTimes = new HashMap<>();

    private ArrayList<Group> groups;

    public GroupManager(){
        if(Instance == null){
            Instance = this;
            groups = new ArrayList<>();
            startExpirationTimer();
        }
    }

    /**
     *  Creates a new group with the given name and prefix
     * @param name group name
     * @param prefix group prefix
     */
    public void createGroup(String name, String prefix){
        groups.add(new Group(name, prefix));
    }

    /**
     * Check if the group exists
     * @param groupName group name
     * @return true if there is a group with the given group name
     */
    public boolean contains(String groupName){
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the player exists in the given group
     * @param groupName group name
     * @return true if there is a group with the given group name
     */
    public boolean contains(String groupName, UUID playerUUID){
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                return group.containsGroupMember(playerUUID);
            }
        }
        return false;
    }

    /**
     * Show info from the given group -> list all players including their time left
     * @param commandSender sender
     * @param groupName group name
     */
    public void showInfo(CommandSender commandSender, String groupName){
        Group infoGroup = null;
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                infoGroup = group;
                break;
            }
        }

        if(infoGroup == null){
            //TODO: replace from file
            commandSender.sendMessage("§cThat group doesn't exist.");
            return;
        }

        String[] players = infoGroup.getGroupMemberInfos().toArray(new String[0]);
        //TODO: replace from file
        commandSender.sendMessage("§2Group: " + groupName + " | " + "§2players (" + players.length + ")\n");

        for (String player : players) {
            //TODO: replace from file
            commandSender.sendMessage(player);
        }
    }

    /**
     * Returns the group name from the group from the player
     * @param playerUUID playerUUID
     * @return group name
     */
    public String getGroupName(UUID playerUUID){
        for (Group group : groups){
            if(group.containsGroupMember(playerUUID)){
                return group.getGroupName();
            }
        }
        return "";
    }

    /**
     * Add given player to given group for time seconds
     * @param groupName group name
     * @param player player
     * @param time time in seconds
     * @return true if the player successfully was added to the group
     */
    public boolean addPlayerToGroup(String groupName, Player player, long time){
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                GroupMember newMember = group.addGroupMember(player, time);
                assignGroupMemberWithExpirationTime(newMember);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the player from its current group
     * @param playerUUID playerUUID
     */
    public void removePlayerFromGroup(UUID playerUUID) {
        for (Group group : groups) {
            if (group.containsGroupMember(playerUUID)) {
                group.removeGroupMember(playerUUID);
                break;
            }
        }
    }

    /**
     * Starts the expiration timer
     */
    public void startExpirationTimer(){
        expirationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkExpirationTimes();
            }
        }, 0, INTERVAL);
    }

    /**
     * Assign the given group member to the expiration time
     * @param member member
     */
    private void assignGroupMemberWithExpirationTime(GroupMember member){
        if(member.getRemainingSeconds() > 0){
            System.out.println(member.getRemainingSeconds());
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
                //TODO: send message after being kicked
                removePlayerFromGroup(groupMember.getPlayerUUID());
                expirationTimes.remove(groupMember);
            }
        }
    }
}