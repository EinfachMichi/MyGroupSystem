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

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
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

            try {
                List<GroupData> groupDataList = futureGroupData.get();
                List<GroupMemberData> groupMemberDataList = futureGroupMemberData.get();
                List<LogData> logDataList = futureLogData.get();

                createGroups(groupDataList);
                addGroupMembers(groupMemberDataList);
                GroupSystemLogger.getInstance().initLogEventMap(logDataList);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private void createGroups(List<GroupData> groupDataList) {
        for (GroupData groupData : groupDataList) {
            GroupSystemManager.getInstance().createGroup(groupData.groupName(), groupData.prefix());
        }
    }

    private void addGroupMembers(List<GroupMemberData> groupMemberDataList) {
        for (GroupMemberData groupMemberData : groupMemberDataList) {
            GroupSystemManager.getInstance().addPlayerToGroup(
                    groupMemberData.playerUUID(),
                    groupMemberData.displayName(),
                    groupMemberData.groupName(),
                    groupMemberData.seconds()
            );
        }
    }
}