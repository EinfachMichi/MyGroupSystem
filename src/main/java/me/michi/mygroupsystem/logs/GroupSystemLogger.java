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
     * @param groupSystemInfoType
     * @param group
     * @param flags
     */
    public static void showGroupInfo(
            CommandSender commandSender,
            GroupSystemInfoType groupSystemInfoType,
            Group group,
            GroupLogFlag... flags
    ){
        // get the message defined from the info type
        StringBuilder message = new StringBuilder();
        for (Map<String, String> info : logFileData.get("infos")){
            if(info.containsKey("type") && info.get("type").equals(groupSystemInfoType.name())) {
                message.append(info.get("message"));
                break;
            }
        }

        // replace flags
        for (GroupLogFlag flag : flags){

            message = new StringBuilder(message.toString().replace(flag.flag(), flag.value()));
        }

        // add all group members
        for (GroupMember groupMember : group.getGroupMembers()){

            message.append("§a- §f").
                    append(groupMember.getDisplayName()).
                    append(" §7").
                    append("(").
                    append(getTimeString(groupMember.getRemainingSeconds())).
                    append(")");
        }

        // finally sends the message
        commandSender.sendMessage(message.toString());
    }

    /**
     * List all groups that exists with their player count
     * @param commandSender
     * @param groupSystemInfoType
     * @param groups
     * @param flags
     */
    public static void listAllGroups(
            CommandSender commandSender,
            GroupSystemInfoType groupSystemInfoType,
            Group[] groups,
            GroupLogFlag... flags
    ){
        // get the message defined from the info type
        StringBuilder message = new StringBuilder();
        for (Map<String, String> info : logFileData.get("infos")){
            if(info.containsKey("type") && info.get("type").equals(groupSystemInfoType.name())) {
                message.append(info.get("message"));
                break;
            }
        }

        // replace flags
        for (GroupLogFlag flag : flags){

            message = new StringBuilder(message.toString().replace(flag.flag(), flag.value()));
        }

        // add all group members
        for (Group group : groups){

            message.append("§a- §6[").
                    append(group.getGroupName()).
                    append("]§a |§7 players (").
                    append(group.getSize()).
                    append(")\n");
        }

        // finally sends the message
        commandSender.sendMessage(message.toString());
    }

    /**
     * @param seconds seconds
     * @return format(day, hours, minutes, seconds)
     */
    public static String getTimeString(long seconds){
        if (seconds < 0) {
            return "";
        }

        long days = seconds / (24 * 60 * 60);
        long hours = (seconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = ((seconds % (24 * 60 * 60)) % (60 * 60)) / 60;
        long remainingSeconds = ((seconds % (24 * 60 * 60)) % (60 * 60)) % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        }

        if (hours > 0) {
            result.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        }

        if (minutes > 0) {
            result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(" ");
        }

        if (remainingSeconds > 0) {
            result.append(remainingSeconds).append(" second").append(remainingSeconds > 1 ? "s" : "");
        }

        return result.toString();
    }
}
