package me.michi.mygroupsystem.logs;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupMember;
import me.michi.mygroupsystem.database.GroupSystemDataBase;
import me.michi.mygroupsystem.database.LogData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GroupSystemLogger {

    private static GroupSystemLogger instance;
    private static Map<String, List<Map<String, String>>> logFileData;
    private static final Map<UUID, List<String>> playerEventLogsMap = new HashMap<>();

    private GroupSystemLogger() {
        loadDataFromLog();
    }

    public static GroupSystemLogger getInstance() {
        if (instance == null) {
            instance = new GroupSystemLogger();
        }
        return instance;
    }

    private void loadDataFromLog(){
        String path = "/log_config.yml";
        try (InputStream inputStream = getClass().getResourceAsStream(path)){
            if(inputStream != null){
                Yaml yaml = new Yaml();
                logFileData = yaml.load(inputStream);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void log(
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

    public void showInfo(
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

    public void listAllGroups(
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

    public void addLogToQueue(
            UUID playerUUID,
            GroupSystemLogType groupSystemLogType,
            GroupLogFlag... flags
    ) {
        StringBuilder message = new StringBuilder(getLogMessage(groupSystemLogType));

        for (GroupLogFlag flag : flags) {
            message = new StringBuilder(message.toString().replace(flag.flag(), flag.value()));
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage(message.toString());
            return;
        }

        handleOfflinePlayer(playerUUID, message);
    }

    private void handleOfflinePlayer(UUID playerUUID, StringBuilder message) {
        List<String> playerLogs = playerEventLogsMap.getOrDefault(playerUUID, new ArrayList<>());
        playerLogs.add(message.toString());
        playerEventLogsMap.put(playerUUID, playerLogs);
        GroupSystemDataBase.uploadLog(playerUUID, message.toString());
    }

    public void initLogEventMap(List<LogData> logDataList){
        for (LogData logData : logDataList) {
            List<String> playerLogList = playerEventLogsMap.getOrDefault(logData.playerUUID(), new ArrayList<>());
            playerLogList.add(logData.message());
            playerEventLogsMap.put(logData.playerUUID(), playerLogList);
        }
    }

    public void trySendLogs(Player player){
        if(playerEventLogsMap.containsKey(player.getUniqueId())){
            List<String> playerLogs = playerEventLogsMap.get(player.getUniqueId());
            for (String message : playerLogs) {
                player.sendMessage(message);
                GroupSystemDataBase.deleteLogEntry(player.getUniqueId(), message);
            }

            playerLogs.clear();
        }
    }

    public void showHelp(CommandSender commandSender){
        commandSender.sendMessage(getInfoMessage(GroupSystemInfoType.show_help));
    }

    public String getLogMessage(GroupSystemLogType groupSystemLogType){
        return getMessageOfType(logFileData.get("logs"), groupSystemLogType);
    }

    public String getInfoMessage(GroupSystemInfoType groupSystemInfoType){
        return getMessageOfType(logFileData.get("infos"), groupSystemInfoType);
    }

    private String getMessageOfType(List<Map<String, String>> messages, Enum<?> type) {
        return messages.stream()
                .filter(log -> log.containsKey("type") && log.get("type").equals(type.name()))
                .findFirst()
                .map(log -> log.get("message"))
                .orElse("");
    }

    public static String getTimeString(long seconds) {
        if (seconds <= 0) {
            return " ";
        }

        long days = seconds / (24 * 60 * 60);
        long hours = (seconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = ((seconds % (24 * 60 * 60)) % (60 * 60)) / 60;
        long remainingSeconds = ((seconds % (24 * 60 * 60)) % (60 * 60)) % 60;

        StringBuilder result = new StringBuilder("(");

        appendTimeComponent(result, days, "day");
        appendTimeComponent(result, hours, "hour");
        appendTimeComponent(result, minutes, "minute");
        appendTimeComponent(result, remainingSeconds, "second");

        result.append(")");

        return result.toString();
    }

    private static void appendTimeComponent(StringBuilder result, long value, String unit) {
        if (value > 0) {
            result.append(value).append(" ").append(unit);
            if (value > 1) {
                result.append("s");
            }
            result.append(" ");
        }
    }
}
