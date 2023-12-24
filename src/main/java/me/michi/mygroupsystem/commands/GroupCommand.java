package me.michi.mygroupsystem.commands;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupMember;
import me.michi.mygroupsystem.logs.GroupLogFlag;
import me.michi.mygroupsystem.logs.GroupSystemLogType;
import me.michi.mygroupsystem.logs.GroupSystemLogger;
import me.michi.mygroupsystem.GroupSystemManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        // the command is invalid if there is no argument given
        if(args.length < 1){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.invalid_input);
            return true;
        }

        String[] specificArgs = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "create" -> createGroup(commandSender, specificArgs);
            case "add" -> addPlayerToGroup(commandSender, specificArgs);
            case "info" -> showInfo(commandSender, specificArgs);
            case "remove" -> removePlayerFromGroup(commandSender, specificArgs);
            case "list" -> listAllGroups(commandSender);
            case "help" -> help(commandSender);
            default -> GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.invalid_command);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        List<String> list = new ArrayList<>();
        switch (args.length){
            case 1 -> list = tabCompleteFirst();
            case 2 -> list = tabCompleteSecond(args[0]);
            case 3 -> list = tabCompleteThird(args[0]);
            case 4 -> list = tabCompleteFourth(args[0]);
        }

        return list;
    }

    //COMMAND-TAB COMPLETER SECTION
    //------------------------------------------------------------------------------------------------------------------

    private List<String> tabCompleteFirst(){
        return Arrays.asList("create", "add", "remove", "info", "list", "help");
    }

    private List<String> tabCompleteSecond(String firstArgument){
        List<String> tabCompleteList = new ArrayList<>();
        if ("add".equals(firstArgument) || "remove".equals(firstArgument)) {
            tabCompleteList.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getDisplayName).toList());
        } else if ("info".equals(firstArgument)) {
            tabCompleteList.addAll(Arrays.asList(GroupSystemManager.getInstance().getGroupNames()));
        }

        return tabCompleteList;
    }

    private List<String> tabCompleteThird(String firstArgument){
        List<String> tabCompleteList = new ArrayList<>();
        if ("add".equals(firstArgument)) {
            tabCompleteList.addAll(Arrays.asList(GroupSystemManager.getInstance().getGroupNames()));
        } else if ("remove".equals(firstArgument)) {
            tabCompleteList = Arrays.asList("60", "300", "900", "1800", "3600");
        }

        return tabCompleteList;
    }

    private List<String> tabCompleteFourth(String firstArgument){
        List<String> tabCompleteList = new ArrayList<>();
        if ("add".equals(firstArgument)) {
            tabCompleteList = Arrays.asList("60", "300", "900", "1800", "3600");
        }

        return tabCompleteList;
    }

    //COMMAND-EXECUTOR SECTION
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Tries to create a new group with the given arguments.
     * @param commandSender
     * @param args String[] <br>
     * args[0] = group name <br>
     * args[1] = group prefix
     * @example possible in-game usages: <br>
     * /group create YourGroupName
     */
    public static void createGroup(CommandSender commandSender, String[] args){
        if(args.length == 0 || args.length > 2) {
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.invalid_input);
            return;
        }

        String groupName = args[0];
        String prefix = args.length == 2 ? args[1] : groupName;

        // if group with the given name already exists -> log and return
        if(GroupSystemManager.getInstance().groupExists(groupName)){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.group_already_exists,
                    new GroupLogFlag("{group}", groupName));
            return;
        }

        if(GroupSystemManager.getInstance().createGroup(groupName, prefix) != null){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.group_created,
                    new GroupLogFlag("{group}", groupName));
            return;
        }

        GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.group_not_created,
                new GroupLogFlag("{group}", groupName));
    }

    /**
     * Tries to add the given player to the given group.
     * @param commandSender
     * @param args String[] <br>
     * args[0] = player name <br>
     * args[1] = group name <br>
     * args[2] = time in seconds
     * @example possible in-game usages: <br>
     * /group add Player Group <br>
     *      - adds player permanently to this group <br> <br>
     * /group add Player Group 60 <br>
     *      - adds player for a period of time to this group
     */
    public static void addPlayerToGroup(CommandSender commandSender, String[] args){
        if(args.length == 0 || args.length > 3 || args.length == 1){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.invalid_input);
            return;
        }

        String playerName = args[0];
        String groupName = args[1];

        // check if given player exists -> if false, log and return
        Player player = Bukkit.getPlayer(playerName);
        if(player == null){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_not_found,
                    new GroupLogFlag("{player}", playerName));
            return;
        }

        // check if given group exists -> if false, log and return
        if(!GroupSystemManager.getInstance().groupExists(groupName)){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.group_not_found,
                    new GroupLogFlag("{group}", groupName));
            return;
        }

        // check if player is already in that group -> if true, log and return
        if(GroupSystemManager.getInstance().playerInGroup(groupName, player.getUniqueId())){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_already_in_that_group,
                    new GroupLogFlag("{player}", playerName),
                    new GroupLogFlag("{group}", groupName));
            return;
        }

        // remove player from its current group
        Group group = GroupSystemManager.getInstance().getGroup(player.getUniqueId());
        group.removeGroupMember(player.getUniqueId());

        // get the time in seconds
        long seconds = 0;
        if(args.length == 3){
            String timeInSeconds = args[2];
            try {
                seconds = Long.parseLong(timeInSeconds);
            } catch (Exception e){
                GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.invalid_input);
                return;
            }
        }

        // add player to the new group
        GroupMember groupMember = GroupSystemManager.getInstance().addPlayerToGroup(
                player.getUniqueId(), playerName, groupName, seconds
        );
        if(groupMember != null){

            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_added_to_group,
                    new GroupLogFlag("{player}", playerName),
                    new GroupLogFlag("{group}", groupName));
            return;
        }

        GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_not_added_to_group,
                new GroupLogFlag("{player}", playerName),
                new GroupLogFlag("{group}", groupName));
    }

    /**
     * Tries to show info about a group from either yourself or a specific player
     * @param commandSender
     * @param args String[] <br>
     * args[0] = player name OR group name
     * @example possible in-game usages: <br>
     * /group info <br>
     * /group info Player <br>
     * /group info Group
     */
    public static void showInfo(CommandSender commandSender, String[] args){
        if(args.length > 1){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.invalid_input);
            return;
        }

        // check if there is no argument -> if true, show info about the group from yourself
        if(args.length == 0){

            // check if command sender is a player -> if false, log and return
            if(!(commandSender instanceof Player player)) {
                GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.sender_is_not_player);
                return;
            }

            // check if player has a group -> if true, show info and return
            if(GroupSystemManager.getInstance().playerHasGroup(player.getUniqueId())){
                Group group = GroupSystemManager.getInstance().getGroup(player.getUniqueId());
                GroupSystemLogger.getInstance().showInfo(
                        commandSender,
                        group,
                        new GroupLogFlag("{group}", group.getGroupName()),
                        new GroupLogFlag("{count}", String.valueOf(group.getSize()))
                );
                return;
            }


            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_not_in_group,
                    new GroupLogFlag("{player}", player.getDisplayName()));
            return;
        }

        String playerOrGroupName = args[0];
        Player player = Bukkit.getPlayer(playerOrGroupName);

        // check if argument is a player
        if(player != null){

            // check if player has a group
            if(GroupSystemManager.getInstance().playerHasGroup(player.getUniqueId())){
                Group group = GroupSystemManager.getInstance().getGroup(player.getUniqueId());
                GroupSystemLogger.getInstance().showInfo(
                        commandSender,
                        group,
                        new GroupLogFlag("{group}", group.getGroupName()),
                        new GroupLogFlag("{count}", String.valueOf(group.getSize()))
                );
            }
            else{

                GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_not_in_group,
                        new GroupLogFlag("{player}", player.getDisplayName()));
            }
            return;
        }
        // check if argument is a group
        else if(GroupSystemManager.getInstance().groupExists(playerOrGroupName)){

            Group group = GroupSystemManager.getInstance().getGroup(playerOrGroupName);
            GroupSystemLogger.getInstance().showInfo(
                    commandSender,
                    group,
                    new GroupLogFlag("{group}", group.getGroupName()),
                    new GroupLogFlag("{count}", String.valueOf(group.getSize()))
            );
            return;
        }

        GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.group_or_player_not_found,
                new GroupLogFlag("{player}", playerOrGroupName),
                new GroupLogFlag("{group}", playerOrGroupName));
    }

    /**
     * Tries to remove given player from its group OR starts countdown to get kicked out
     * @param commandSender
     * @param args String[] <br>
     * args[0] = player name <br>
     * args[1] = time in seconds
     * @example possible in-game usages: <br>
     * /group remove Player <br>
     *      - removes player instantly from its group <br> <br>
     * /group remove Player 60 <br>
     *      - removes player after a period of time
     */
    public static void removePlayerFromGroup(CommandSender commandSender, String[] args){
        if(args.length == 0 || args.length > 2){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.invalid_input);
            return;
        }

        String playerName = args[0];
        Player player = commandSender.getServer().getPlayer(playerName);

        // check if player exists -> if true, log and return
        if(player == null){
            GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_not_found,
                    new GroupLogFlag("{player}", playerName));
            return;
        }

        // get time in seconds if it's given
        long seconds = 0;
        if(args.length == 2) {
            String timeInSeconds = args[1];
            try {
                seconds = Long.parseLong(timeInSeconds);
            } catch (Exception e){
                GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.invalid_input);
                return;
            }
        }

        // remove player from its current group
        String groupName = GroupSystemManager.getInstance().getGroup(player.getUniqueId()).getGroupName();
        if(GroupSystemManager.getInstance().removePlayerFromGroup(player.getUniqueId(), seconds)){
            if(seconds == 0){
                GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_removed_from_group,
                        new GroupLogFlag("{player}", playerName),
                        new GroupLogFlag("{group}", groupName));
            }
            else{
                GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_removed_from_group_for_time,
                        new GroupLogFlag("{player}", playerName),
                        new GroupLogFlag("{group}", groupName),
                        new GroupLogFlag("{time}", GroupSystemLogger.getTimeString(seconds)));
            }
            return;
        }

        GroupSystemLogger.getInstance().log(commandSender, GroupSystemLogType.player_not_removed_from_group,
                new GroupLogFlag("{player}", playerName),
                new GroupLogFlag("{group}", groupName)
        );
    }

    /**
     * Lists all groups that exists
     * @param commandSender
     * @example possible in-game usages: <br>
     * /group list
     */
    public static void listAllGroups(CommandSender commandSender){
        Group[] groups = GroupSystemManager.getInstance().getGroups();
        GroupSystemLogger.getInstance().listAllGroups(
                commandSender,
                groups,
                new GroupLogFlag("{count}", String.valueOf(groups == null ? 0 : groups.length))
        );
    }

    /**
     * Lists all possible commands you can execute
     * @param commandSender
     * @example possible in-game usages: <br>
     * /group help
     */
    public static void help(CommandSender commandSender){
        GroupSystemLogger.getInstance().showHelp(commandSender);
    }
}