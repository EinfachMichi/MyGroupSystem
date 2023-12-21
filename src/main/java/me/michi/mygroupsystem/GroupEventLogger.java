package me.michi.mygroupsystem;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class GroupEventLogger {

    private final String path = "/event_configuration_log.yml";

    public static GroupEventLogger Instance;
    private static Map<String, List<Map<String, String>>> data;

    private JavaPlugin javaPlugin;

    public GroupEventLogger(JavaPlugin javaPlugin){

        if(Instance == null){
            Instance = this;
            this.javaPlugin = javaPlugin;

            readConfigFile();
        }
    }

    public static void Log(CommandSender commandSender, GroupEventLogType groupEventLogType){
        if(data != null && data.containsKey("logs")){
            List<Map<String, String>> events = data.get("logs");

            for (Map<String, String> event : events) {

                if(event.get("type").equals(groupEventLogType.name())) {
                    commandSender.sendMessage(event.get("message"));
                    break;
                }
            }
        }
    }

    private void readConfigFile(){
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
}
