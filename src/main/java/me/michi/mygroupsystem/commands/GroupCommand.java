package me.michi.mygroupsystem.commands;

import me.michi.mygroupsystem.GroupManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GroupCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length < 1){
            commandSender.sendMessage("§cUnknown command. Use \"/group help\" for help.");
            return false;
        }

        if(args[0].equals("create")){
            if(args.length >= 2){
                return create(commandSender, args);
            }
        }

        if(args[0].equals("add")) {
            if(args.length >= 3){
                return add(commandSender, args);
            }
        }

        if(args[0].equals("info")) {
            return info(commandSender, args);
        }

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
        commandSender.sendMessage("§aThe Group §6[" + args[1] + "]§a was successfully created!");
        return true;
    }

    /*
    ADD-COMMAND:
        - /group add <player> <groupName> (PERMANENT)
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
                // Remove player from its current group
                GroupManager.Instance.removePlayerFromGroup(playerToAdd);

                long seconds = 0;
                if(args.length == 4){
                    String inputTime = args[3];
                    try {
                        seconds = Long.parseLong(inputTime);
                    } catch (Exception e){
                        commandSender.sendMessage("§cSomething went wrong.");
                        return false;
                    }
                }

                // Add player to the new group
                if(GroupManager.Instance.addPlayerToGroup(groupName, playerToAdd, seconds)){
                    commandSender.sendMessage("§6[" + playerName + "]§a successfully added to §6[" + groupName + "]§a!");
                    return true;
                }
                commandSender.sendMessage("§cSomething went wrong.");
                return false;
            }
            commandSender.sendMessage("§cCouldn't find player.");
        }
        else{
            commandSender.sendMessage("§cThat group doesn't exist.");
        }
        return false;
    }

    /*
    INFO-COMMAND
        - /group info
        - /group info <groupName>
     */
    private boolean info(CommandSender commandSender, String[] args){
        if(args.length >= 2){
            String groupName = args[1];
            if(GroupManager.Instance.contains(groupName)){
                GroupManager.Instance.listPlayerInGroup(commandSender, groupName);
                return true;
            }
            else{
                commandSender.sendMessage("§cThat isn't in a group yet.");
                return false;
            }
        }

        if(commandSender instanceof Player player){
            String groupName = GroupManager.Instance.getGroupName(player);
            if(!groupName.isEmpty()){
                GroupManager.Instance.showInfo(commandSender, groupName);
                return true;
            }
            else{
                commandSender.sendMessage("§cYou aren't in a group yet.");
            }
        }

        return false;
    }
}
