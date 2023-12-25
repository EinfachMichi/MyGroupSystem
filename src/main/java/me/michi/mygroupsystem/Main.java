package me.michi.mygroupsystem;

import me.michi.mygroupsystem.commands.GroupCommand;
import me.michi.mygroupsystem.database.*;
import me.michi.mygroupsystem.listeners.GroupSystemListener;
import me.michi.mygroupsystem.logs.GroupSystemLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        initialDatabaseLoad();

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        GroupSystemDataBase.uploadServerInfo();
    }

    private void registerCommands(){
        getCommand("group").setExecutor(new GroupCommand());
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new GroupSystemListener(this), this);
    }

    private void initialDatabaseLoad() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            CompletableFuture<List<GroupData>> futureGroupData = GroupSystemDataBase.retrieveGroupData();
            CompletableFuture<List<GroupMemberData>> futureGroupMemberData = GroupSystemDataBase.retrieveGroupMembers();
            CompletableFuture<List<LogData>> futureLogData = GroupSystemDataBase.retrieveLogData();
            CompletableFuture<ServerInfo> futureServerInfo = GroupSystemDataBase.retrieveServerInfo();

            try {
                List<GroupData> groupDataList = futureGroupData.get();
                List<GroupMemberData> groupMemberDataList = futureGroupMemberData.get();
                List<LogData> logDataList = futureLogData.get();
                ServerInfo serverInfo = futureServerInfo.get();

                GroupSystemManager.getInstance().createGroups(groupDataList);
                GroupSystemManager.getInstance().addGroupMembers(groupMemberDataList);
                GroupSystemLogger.getInstance().initLogEventMap(logDataList);
                GroupSystemManager.getInstance().manageServerInfo(serverInfo);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}