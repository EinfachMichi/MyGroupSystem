package me.michi.mygroupsystem;

import me.michi.mygroupsystem.commands.GroupCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        initSingletons();
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initSingletons(){
        new GroupManager();
    }

    private void registerCommands(){
        getCommand("group").setExecutor(new GroupCommand());
    }
}
