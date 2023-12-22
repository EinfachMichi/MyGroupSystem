package me.michi.mygroupsystem.logs;

import me.michi.mygroupsystem.Group;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class GroupSystemLogger {

    private final String path = "/configuration_log.yml";

    public static GroupSystemLogger Instance;
    private static Map<String, List<Map<String, String>>> data;

    private JavaPlugin javaPlugin;

    public GroupSystemLogger(JavaPlugin javaPlugin){

        if(Instance == null){
            Instance = this;
            this.javaPlugin = javaPlugin;

            readConfigLogFile();
        }
    }

    public static void Log(CommandSender commandSender, GroupSystemLogType groupSystemLogType){
        Log(commandSender, groupSystemLogType, null, "", 0);
    }

    public static void Log(CommandSender commandSender, GroupSystemLogType groupSystemLogType, Player anyPlayer){
        Log(commandSender, groupSystemLogType, anyPlayer, "", 0);
    }

    public static void Log(CommandSender commandSender, GroupSystemLogType groupSystemLogType, String groupName){
        Log(commandSender, groupSystemLogType, null, groupName, 0);
    }

    public static void Log(CommandSender commandSender, GroupSystemLogType groupSystemLogType, Player anyPlayer, String groupName){
        Log(commandSender, groupSystemLogType, anyPlayer, groupName, 0);
    }

    public static void Log(CommandSender commandSender, GroupSystemLogType groupSystemLogType, Player anyPlayer, long time){
        Log(commandSender, groupSystemLogType, anyPlayer, "", time);
    }

    public static void Log(
            CommandSender commandSender, GroupSystemLogType groupSystemLogType, Player anyPlayer, String groupName, long time){
        if(data != null && data.containsKey("logs")){
            List<Map<String, String>> events = data.get("logs");

            for (Map<String, String> event : events) {

                if(event.get("type").equals(groupSystemLogType.name())) {

                    String message = event.get("message");

                    // replace flags
                    if(anyPlayer != null){
                        message = message.replace("{anyPlayer}", anyPlayer.getDisplayName());
                    }

                    if(!groupName.isEmpty()){
                        message = message.replace("{group}", groupName);
                    }

                    if(time > 0){
                        message = message.replace("{time}", getTimeString(time));
                    }

                    commandSender.sendMessage(message);
                    break;
                }
            }
        }
    }

    private void readConfigLogFile(){
        try (InputStream inputStream = getClass().getResourceAsStream(path)){
            if(inputStream != null){
                Yaml yaml = new Yaml();
                data = yaml.load(inputStream);
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
