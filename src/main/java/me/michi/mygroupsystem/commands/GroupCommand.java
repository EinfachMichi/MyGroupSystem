package me.michi.mygroupsystem.commands;

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
            //TODO: replace from file
            commandSender.sendMessage("§cUnknown command. Use \"/group help\" for help.");
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

        //TODO: replace from file
        commandSender.sendMessage("§cUnknown command. Use \"/group help\" for help.");
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
        //TODO: replace from file
        commandSender.sendMessage("§aThe Group §6[" + args[1] + "]§a was successfully created!");
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
                    //TODO: replace from file
                    commandSender.sendMessage("§6[" + playerName + "]§4 is already a member of §6[" + groupName + "].");
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
                        //TODO: replace from file
                        commandSender.sendMessage("§cSomething went wrong.");
                        return false;
                    }
                }

                // Add player to the new group
                if(GroupManager.Instance.addPlayerToGroup(groupName, playerToAdd, seconds)){
                    //TODO: replace from file
                    commandSender.sendMessage("§6[" + playerName + "]§a successfully added to §6[" + groupName + "]§a!");
                    return true;
                }
                //TODO: replace from file
                commandSender.sendMessage("§cSomething went wrong.");
                return false;
            }
            //TODO: replace from file
            commandSender.sendMessage("§cCouldn't find player.");
        }
        else{
            //TODO: replace from file
            commandSender.sendMessage("§cThat group doesn't exist.");
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
                //TODO: replace from file
                commandSender.sendMessage("§cThat isn't in a group yet.");
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
                //TODO: replace from file
                commandSender.sendMessage("§cYou aren't in a group yet.");
            }
        }

        return false;
    }

    /*
    REMOVE-COMMAND
        - /group remove <playerName> <groupName>
     */
    private boolean remove(CommandSender commandSender, String[] args){
        if(args.length == 3){
            String playerName = args[1];
            String groupName = args[2];

            // Check if given group exists
            if(GroupManager.Instance.contains(groupName)){

                // Try to get player
                Player playerToRemove = commandSender.getServer().getPlayer(playerName);
                if(playerToRemove != null){

                    // Check if player is in that group
                    if(GroupManager.Instance.contains(groupName, playerToRemove.getUniqueId())){
                        GroupManager.Instance.removePlayerFromGroup(playerToRemove.getUniqueId());
                        //TODO: replace from file
                        commandSender.sendMessage("§6[" + playerName + "]§4 successfully removed from group §6[" + groupName + "]§4.");
                        return true;
                    }
                    else {
                        //TODO: replace from file
                        commandSender.sendMessage("§6[" + playerName + "]§4 doesn't exist in group §6[" + groupName + "]§4.");
                        return false;
                    }
                }
                else{
                    //TODO: replace from file
                    commandSender.sendMessage("§cPlayer not found.");
                    return false;
                }
            }
            else{
                //TODO: replace from file
                commandSender.sendMessage("§cThat group doesn't exist.");
                return false;
            }
        }

        //TODO: replace from file
        commandSender.sendMessage("§cNot a valid command.");
        return false;
    }
}
