package me.michi.mygroupsystem.commands;

import me.michi.mygroupsystem.GroupManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GroupCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length < 2){
            commandSender.sendMessage("§cUnknown command. Use \"/group help\" for help.");
            return false;
        }

        if(create(commandSender, args)){
            return true;
        }

        if(add(commandSender, args)){
            return true;
        }

        if(remove()){
            return true;
        }

        if(access(commandSender, args)){
            return true;
        }

        commandSender.sendMessage("§cUnknown command. Use \"/group help\" for help.");
        return false;
    }

    private boolean create(CommandSender commandSender, String[] args){
        if(!args[0].equals("create")) return false;

        String name = args[1];
        String prefix = name;

        if(args.length == 3){
            prefix = args[2];
        }
        GroupManager.Instance.createGroup(name, prefix);
        commandSender.sendMessage("§aThe Group [" + args[1] + "] was created successfully!");
        return true;
    }

    private boolean add(CommandSender commandSender, String[] args){
        if(!args[0].equals("add")) return false;

        if(GroupManager.Instance.contains(args[1])){
            if(args.length == 3){
                Player playerToAdd = commandSender.getServer().getPlayer(args[2]);
                if(playerToAdd != null){
                    //TODO: Check if player already is in that group
                    GroupManager.Instance.addPlayerToGroup(args[1], playerToAdd);
                    commandSender.sendMessage("§a" + args[2] + " successfully added!");
                    return true;
                }
            }
            commandSender.sendMessage("§cCouldn't find player.");
        }
        else{
            commandSender.sendMessage("§cThat group doesn't exist.");
        }
        return true;
    }

    private boolean remove(){
        return false;
    }

    private boolean access(CommandSender commandSender, String[] args){
        if(!GroupManager.Instance.contains(args[0])) return false;

        if(args[1].equals("list")){
            GroupManager.Instance.listPlayerInGroup(commandSender, args[0]);
        }
        return true;
    }
}
