package me.michi.mygroupsystem.commands;

import me.michi.mygroupsystem.GroupEventLogType;
import me.michi.mygroupsystem.GroupEventLogger;
import me.michi.mygroupsystem.GroupManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GroupCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // if there is no argument, the command is invalid
        if(args.length < 1){
            GroupEventLogger.Log(commandSender, GroupEventLogType.failed_invalid);
            return false;
        }

        // if first argument is equal to "create" -> goTo create()
        if(args[0].equals("create")){
            if(args.length >= 2){
                return create(commandSender, args);
            }
        }

        // if first argument is equal to "add" AND there are more or equal 3 arguments -> goTo add()
        // ["add" requires a minimum of 3 arguments] -> 1 = add | 2 = <player> | 3 = <groupName>
        if(args[0].equals("add")) {
            if(args.length >= 3){
                return add(commandSender, args);
            }
        }

        // if first argument is equal to "info" -> goTo info()
        if(args[0].equals("info")) {
            return info(commandSender, args);
        }

        // if first argument is equal to "remove" -> goTo remove()
        if(args[0].equals("remove")){
            return remove(commandSender, args);
        }

        if(args[0].equals("list")){
            return list(commandSender);
        }

        GroupEventLogger.Log(commandSender, GroupEventLogType.failed_invalid);
        return false;
    }

    /*
    CREATE-COMMAND:
        - /group create <groupName>
     */
    private boolean create(CommandSender commandSender, String[] args){
        String name = args[1];
        String prefix = name;

        // Set prefix if given
        if(args.length == 3){
            prefix = args[2];
        }

        GroupManager.Instance.createGroup(name, prefix);
        GroupEventLogger.Log(commandSender, GroupEventLogType.success_create);
        return true;
    }

    /*
    ADD-COMMAND:
        - /group add <player> <groupName> (PERM)
        - /group add <player> <groupName> <seconds> (TEMP)
     */
    private boolean add(CommandSender commandSender, String[] args){
        String playerName = args[1];
        String groupName = args[2];

        // Check if given group exists
        if(GroupManager.Instance.contains(groupName)){

            // Try to get player
            Player playerToAdd = commandSender.getServer().getPlayer(playerName);
            if(playerToAdd != null){

                // Check if player is already in that group
                if(GroupManager.Instance.contains(groupName, playerToAdd.getUniqueId())){
                    GroupEventLogger.Log(commandSender, GroupEventLogType.failed_add_already_member);
                    return false;
                }

                // Remove player from its current group
                GroupManager.Instance.removePlayerFromGroup(playerToAdd.getUniqueId());

                // Get the time in seconds
                long seconds = 0;
                if(args.length == 4){
                    String inputTime = args[3];
                    try {
                        seconds = Long.parseLong(inputTime);
                    } catch (Exception e){
                        GroupEventLogger.Log(commandSender, GroupEventLogType.failed_parse);
                        return false;
                    }
                }

                // Add player to the new group
                if(GroupManager.Instance.addPlayerToGroup(groupName, playerToAdd, seconds)){
                    GroupEventLogger.Log(commandSender, GroupEventLogType.success_add);
                    return true;
                }
                GroupEventLogger.Log(commandSender, GroupEventLogType.failed_add_player);
                return false;
            }
            GroupEventLogger.Log(commandSender, GroupEventLogType.failed_player_not_found);
        }
        else{
            GroupEventLogger.Log(commandSender, GroupEventLogType.failed_group_not_found);
        }
        return false;
    }

    /*
    INFO-COMMAND
        - /group info <groupName>
        - /group info
     */
    private boolean info(CommandSender commandSender, String[] args){
        if(args.length >= 2){
            String groupName = args[1];
            if(GroupManager.Instance.contains(groupName)){
                GroupManager.Instance.showInfo(commandSender, groupName);
                return true;
            }
            else{
                GroupEventLogger.Log(commandSender, GroupEventLogType.failed_group_not_found);
                return false;
            }
        }

        if(commandSender instanceof Player player){
            // Get the group name of the player -> if it's in no group, print error
            String groupName = GroupManager.Instance.getGroupName(player.getUniqueId());
            if(!groupName.isEmpty()){
                GroupManager.Instance.showInfo(commandSender, groupName);
                return true;
            }
            else{
                GroupEventLogger.Log(commandSender, GroupEventLogType.failed_player_no_group);
            }
        }

        return false;
    }

    /*
    REMOVE-COMMAND
        - /group remove <playerName> <seconds>
     */
    private boolean remove(CommandSender commandSender, String[] args){
        String playerName = args[1];;

        // Try to get player
        Player playerToRemove = commandSender.getServer().getPlayer(playerName);
        if(playerToRemove != null){

            // if time is given
            if(args.length == 3) {

                // Get the time in seconds
                long seconds = 0;
                String inputTime = args[2];
                try {
                    seconds = Long.parseLong(inputTime);
                } catch (Exception e){
                    GroupEventLogger.Log(commandSender, GroupEventLogType.failed_parse);
                    return false;
                }

                GroupManager.Instance.removePlayerFromGroupAfter(playerToRemove.getUniqueId(), seconds);
                return true;
            }
            else if(args.length > 3){
                GroupEventLogger.Log(commandSender, GroupEventLogType.failed_invalid);
                return false;
            }

            GroupManager.Instance.removePlayerFromGroup(playerToRemove.getUniqueId());
            GroupEventLogger.Log(commandSender, GroupEventLogType.success_remove);
            return true;
        }
        else{
            GroupEventLogger.Log(commandSender, GroupEventLogType.failed_player_not_found);
            return false;
        }
    }

    /*
    LIST-COMMAND
        - /group list
     */
    private boolean list(CommandSender commandSender){
        // This command will succeed everytime
        GroupManager.Instance.listGroups(commandSender);
        return true;
    }
}