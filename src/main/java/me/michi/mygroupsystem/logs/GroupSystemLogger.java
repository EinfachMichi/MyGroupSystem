package me.michi.mygroupsystem.logs;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupMember;
import me.michi.mygroupsystem.database.GroupSystemDataBase;
import me.michi.mygroupsystem.database.LogData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GroupSystemLogger {

    private final String path = "/log_config.yml";

    public static GroupSystemLogger Instance;
    private static Map<String, List<Map<String, String>>> logFileData;
    private static Map<UUID, List<String>> playerEventLogsMap = new HashMap<>();

    private JavaPlugin javaPlugin;

    public GroupSystemLogger(JavaPlugin javaPlugin){

        if(Instance == null){
            Instance = this;
            this.javaPlugin = javaPlugin;

            loadDataFromLog();
        }
    }

    private void loadDataFromLog(){

        try (InputStream inputStream = getClass().getResourceAsStream(path)){

            if(inputStream != null){
                Yaml yaml = new Yaml();
                logFileData = yaml.load(inputStream);
            }
            else{

                javaPlugin.getLogger().info("Event configuration file couldn't be found.");
            }
        }
        catch (IOException e){

            e.printStackTrace();
        }
    }

    /**
     * Tries to log information to the command sender
     * @param commandSender
     * @param groupSystemLogType
     * @param flags
     */
    public static void log(
            CommandSender commandSender,
            GroupSystemLogType groupSystemLogType,
            GroupLogFlag... flags
    ){
        if(!logFileData.containsKey("logs")){
            return;
        }

        String message = getLogMessage(groupSystemLogType);
        for (GroupLogFlag flag : flags){
            message = message.replace(flag.flag(), flag.value());
        }

        commandSender.sendMessage(message);
    }

    /**
     * Send all infos about the given group in the chat
     * @param commandSender
     * @param group
     * @param flags
     */
    public static void showInfo(
            CommandSender commandSender,
            Group group,
            GroupLogFlag... flags
    ){

        StringBuilder message = new StringBuilder(getInfoMessage(GroupSystemInfoType.show_group_info_header));
        StringBuilder groupMemberInfoTemplate = new StringBuilder(getInfoMessage(GroupSystemInfoType.show_player_info));

        for (GroupLogFlag flag : flags){
            message = new StringBuilder(message.toString().replace(flag.flag(), flag.value()));
        }

        for (GroupMember groupMember : group.getGroupMembers()){

            StringBuilder groupMemberInfo = new StringBuilder(groupMemberInfoTemplate);

            GroupLogFlag[] groupMemberFlags = new GroupLogFlag[]{
                    new GroupLogFlag("{player}", groupMember.getDisplayName()),
                    new GroupLogFlag("{time}", getTimeString(groupMember.getRemainingSeconds()))
            };

            for (GroupLogFlag flag : groupMemberFlags){
                groupMemberInfo = new StringBuilder(groupMemberInfo.toString().replace(flag.flag(), flag.value()));
            }

            message.append(groupMemberInfo);
        }

        commandSender.sendMessage(message.toString());
    }

    /**
     * List all groups that exists with their player count
     * @param commandSender
     * @param groups
     * @param flags
     */
    public static void listAllGroups(
            CommandSender commandSender,
            Group[] groups,
            GroupLogFlag... flags
    ){

        StringBuilder message = new StringBuilder();
        StringBuilder groupInfoTemplate = new StringBuilder();

        message.append(getInfoMessage(GroupSystemInfoType.list_all_groups_info_header));
        groupInfoTemplate.append(getInfoMessage(GroupSystemInfoType.show_group_info));

        for (GroupLogFlag flag : flags){
            message = new StringBuilder(message.toString().replace(flag.flag(), flag.value()));
        }

        for (Group group : groups){

            StringBuilder groupInfo = new StringBuilder(groupInfoTemplate);
            GroupLogFlag[] groupFlag = new GroupLogFlag[]{
                    new GroupLogFlag("{group}", group.getGroupName()),
                    new GroupLogFlag("{count}", String.valueOf(group.getSize()))
            };

            for (GroupLogFlag flag : groupFlag){
                groupInfo = new StringBuilder(groupInfo.toString().replace(flag.flag(), flag.value()));
            }

            message.append(groupInfo);
        }

        commandSender.sendMessage(message.toString());
    }

    /**
     * Add a log to the player. When he is offline, it will be queued
     * @param playerUUID
     * @param groupSystemLogType
     * @param flags
     */
    public static void addLogToQueue(
            UUID playerUUID,
            GroupSystemLogType groupSystemLogType,
            GroupLogFlag... flags
    ) {

        StringBuilder message = new StringBuilder(getLogMessage(groupSystemLogType));

        for (GroupLogFlag flag : flags){
            message = new StringBuilder(message.toString().replace(flag.flag(), flag.value()));
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if(player != null){
            player.sendMessage(message.toString());
            return;
        }

        List<String> playerLogs = playerEventLogsMap.get(playerUUID);
        if(playerLogs == null){
            playerLogs = new ArrayList<>();
        }

        playerLogs.add(message.toString());
        playerEventLogsMap.put(playerUUID, playerLogs);
        GroupSystemDataBase.uploadLog(playerUUID, message.toString());
    }

    /**
     * Initialize the log data coming from the database
     * @param logDataList
     */
    public static void initLogEventMap(List<LogData> logDataList){

        for (LogData logData : logDataList){

            List<String> playerLogList = playerEventLogsMap.get(logData.playerUUID());
            if(playerLogList == null){
                playerLogList = new ArrayList<>();
            }

            playerLogList.add(logData.message());
            playerEventLogsMap.put(logData.playerUUID(), playerLogList);
        }
    }

    /**
     * If there are logs for that players in queue, it will send those to it
     * @param player
     */
    public static void trySendLogs(Player player){

        if(playerEventLogsMap.containsKey(player.getUniqueId())){

            for (String message : playerEventLogsMap.get(player.getUniqueId())){
                player.sendMessage(message);
                GroupSystemDataBase.deleteLogEntry(player.getUniqueId(), message);
            }

            playerEventLogsMap.get(player.getUniqueId()).clear();
        }
    }

    /**
     * Print help in the chat -> helps the player to get an overview of the command
     * @param commandSender
     */
    public static void showHelp(CommandSender commandSender){
        commandSender.sendMessage(getInfoMessage(GroupSystemInfoType.show_help));
    }

    public static String getLogMessage(GroupSystemLogType groupSystemLogType){

        String message = "";
        for (Map<String, String> log : logFileData.get("logs")){

            if(log.containsKey("type") && log.get("type").equals(groupSystemLogType.name())) {

                message = log.get("message");
                break;
            }
        }
        return message;
    }

    public static String getInfoMessage(GroupSystemInfoType groupSystemInfoType){

        String message = "";
        for (Map<String, String> log : logFileData.get("infos")){

            if(log.containsKey("type") && log.get("type").equals(groupSystemInfoType.name())) {

                message = log.get("message");
                break;
            }
        }
        return message;
    }

    /**
     * @param seconds seconds
     * @return format(day, hours, minutes, seconds)
     */
    public static String getTimeString(long seconds) {
        if (seconds <= 0) {
            return " ";
        }

        long days = seconds / (24 * 60 * 60);
        long hours = (seconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = ((seconds % (24 * 60 * 60)) % (60 * 60)) / 60;
        long remainingSeconds = ((seconds % (24 * 60 * 60)) % (60 * 60)) % 60;

        StringBuilder result = new StringBuilder("(");

        if (days > 0) {
            result.append(days).append(" day").append(days > 1 ? "s" : "");
            if (hours > 0 || minutes > 0 || remainingSeconds > 0) {
                result.append(" ");
            }
        }

        if (hours > 0) {
            result.append(hours).append(" hour").append(hours > 1 ? "s" : "");
            if (minutes > 0 || remainingSeconds > 0) {
                result.append(" ");
            }
        }

        if (minutes > 0) {
            result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
            if (remainingSeconds > 0) {
                result.append(" ");
            }
        }

        if (remainingSeconds > 0) {
            result.append(remainingSeconds).append(" second").append(remainingSeconds > 1 ? "s" : "");
        }

        result.append(")");

        return result.toString();
    }
}
