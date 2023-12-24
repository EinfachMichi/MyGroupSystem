package me.michi.mygroupsystem;

import me.michi.mygroupsystem.commands.GroupCommand;
import me.michi.mygroupsystem.database.GroupData;
import me.michi.mygroupsystem.database.GroupMemberData;
import me.michi.mygroupsystem.database.GroupSystemDataBase;
import me.michi.mygroupsystem.database.LogData;
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

        initSingletons();
        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {

    }

    private void initSingletons(){
        new GroupSystemLogger(this);
        new GroupSystemManager();
    }

    private void registerCommands(){
        getCommand("group").setExecutor(new GroupCommand());
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new GroupSystemListener(this), this);
    }

    private void initialDatabaseLoad(){
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            CompletableFuture<List<GroupData>> futureGroupData = GroupSystemDataBase.retrieveGroupData();
            CompletableFuture<List<GroupMemberData>> futureGroupMemberData = GroupSystemDataBase.retrieveGroupMembers();
            CompletableFuture<List<LogData>> futureLogData = GroupSystemDataBase.retrieveLogData();

            try {
                List<GroupData> groupDataList = futureGroupData.get();
                List<GroupMemberData> groupMemberDataList = futureGroupMemberData.get();
                List<LogData> logDataList = futureLogData.get();

                // create groups
                for (GroupData groupData : groupDataList){
                    GroupSystemManager.Instance.createGroup(groupData.groupName(), groupData.prefix());
                }

                // add group members to their group
                for (GroupMemberData groupMemberData : groupMemberDataList){
                    GroupSystemManager.Instance.addPlayerToGroup(
                            groupMemberData.playerUUID(),
                            groupMemberData.displayName(),
                            groupMemberData.groupName(),
                            groupMemberData.seconds()
                    );
                }

                // add all logs to the system logger
                GroupSystemLogger.initLogEventMap(logDataList);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}
