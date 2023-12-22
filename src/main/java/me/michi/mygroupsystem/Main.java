package me.michi.mygroupsystem;

import me.michi.mygroupsystem.commands.GroupCommand;
import me.michi.mygroupsystem.listeners.GroupSystemListener;
import me.michi.mygroupsystem.logs.GroupSystemLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        initSingletons();
        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {

    }

    private void initSingletons(){
        new GroupSystemLogger(this);
        new GroupManager();
    }

    private void registerCommands(){
        getCommand("group").setExecutor(new GroupCommand());
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new GroupSystemListener(), this);
    }
}
