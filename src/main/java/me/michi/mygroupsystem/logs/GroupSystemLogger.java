package me.michi.mygroupsystem.logs;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupMember;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GroupSystemLogger {

    private final String path = "/log_configuration.yml";

    public static GroupSystemLogger Instance;
    private static Map<String, List<Map<String, String>>> logFileData;
    private static Map<UUID, Queue<String>> playerEventLogQueueMap = new HashMap<>();

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

        // get the message defined from the log type
        String message = "";
        for (Map<String, String> log : logFileData.get("logs")){
            if(log.containsKey("type") && log.get("type").equals(groupSystemLogType.name())) {
                message = log.get("message");
                break;
            }
        }

        // replace flags
        for (GroupLogFlag flag : flags){
            message = message.replace(flag.flag(), flag.value());
        }

        // finally sends the message
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
        // get the message defined for the header information
        StringBuilder message = new StringBuilder();
        StringBuilder groupMemberInfoTemplate = new StringBuilder();

        // loop through the log file data to find header and group member info templates
        for (Map<String, String> info : logFileData.get("infos")){

            // check for the header information type
            if(info.containsKey("type") && info.get("type").equals(GroupSystemInfoType.show_group_info_header.name())){
                message.append(info.get("message"));
            }

            // check for the group member information type
            if(info.containsKey("type") && info.get("type").equals(GroupSystemInfoType.show_player_info.name())){
                groupMemberInfoTemplate.append(info.get("message"));
            }

            // if both header and group member info templates are found, break out of the loop
            if(!message.isEmpty() && !groupMemberInfoTemplate.isEmpty()){
                break;
            }
        }

        // replace flags in the header message
        for (GroupLogFlag flag : flags){
            message = new StringBuilder(message.toString().replace(flag.flag(), flag.value()));
        }

        // list all group members
        for (GroupMember groupMember : group.getGroupMembers()){

            // create a copy of the group member info template for each member
            StringBuilder groupMemberInfo = new StringBuilder(groupMemberInfoTemplate);

            // define flags for group member information
            GroupLogFlag[] groupMemberFlags = new GroupLogFlag[]{
                    new GroupLogFlag("{player}", groupMember.getDisplayName()),
                    new GroupLogFlag("{time}", getTimeString(groupMember.getRemainingSeconds()))
            };

            // replace flags in the group member information template
            for (GroupLogFlag flag : groupMemberFlags){
                groupMemberInfo = new StringBuilder(groupMemberInfo.toString().replace(flag.flag(), flag.value()));
            }

            // append the group member information to the overall message
            message.append(groupMemberInfo);
        }

        // finally, send the message to the command sender
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
        // get the message defined for the header information
        StringBuilder message = new StringBuilder();
        StringBuilder groupInfoTemplate = new StringBuilder();

        // loop through the log file data to find header and group info templates
        for (Map<String, String> info : logFileData.get("infos")){

            // check for the header information type
            if(info.containsKey("type") && info.get("type").equals(GroupSystemInfoType.list_all_groups_info_header.name())) {
                message.append(info.get("message"));
            }

            // check for the group information type
            if(info.containsKey("type") && info.get("type").equals(GroupSystemInfoType.show_group_info.name())) {
                groupInfoTemplate.append(info.get("message"));
            }

            // if both header and group info templates are found, break out of the loop
            if(!message.isEmpty() && !groupInfoTemplate.isEmpty()){
                break;
            }
        }

        // replace flags in the header message
        for (GroupLogFlag flag : flags){
            message = new StringBuilder(message.toString().replace(flag.flag(), flag.value()));
        }

        // list all groups
        for (Group group : groups){

            // create a copy of the group info template for each group
            StringBuilder groupInfo = new StringBuilder(groupInfoTemplate);

            // define flags for group information
            GroupLogFlag[] groupFlag = new GroupLogFlag[]{
                    new GroupLogFlag("{group}", group.getGroupName()),
                    new GroupLogFlag("{count}", String.valueOf(group.getSize()))
            };

            // replace flags in the group information template
            for (GroupLogFlag flag : groupFlag){
                groupInfo = new StringBuilder(groupInfo.toString().replace(flag.flag(), flag.value()));
            }

            // append the group information to the overall message
            message.append(groupInfo);
        }

        // finally, send the message to the command sender
        commandSender.sendMessage(message.toString());
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
