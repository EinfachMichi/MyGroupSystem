package me.michi.mygroupsystem.database;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupMember;
import me.michi.mygroupsystem.Main;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupSystemDataBase {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static Connection getConnection() throws SQLException {
        Map<String, String> config = readYamlConfig();
        assert config != null;
        String host = String.valueOf(config.get("host"));
        String port = String.valueOf(config.get("port"));
        String database = config.get("name");
        String user = config.get("username");
        String password = config.get("password");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(url, user, password);
    }

    private static Map<String, String> readYamlConfig() {
        Map<String, String> config = null;
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.yml")) {
            if (input != null) {
                Yaml yaml = new Yaml();
                return yaml.load(input);
            } else {
                System.err.println("Resource 'config.yml' not found!");
                return null;
            }
        } catch (IOException | YAMLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CompletableFuture<List<GroupData>> retrieveGroupData() {
        return CompletableFuture.supplyAsync(() -> {
            List<GroupData> groupDataList = new ArrayList<>();

            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `Group`;")) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String groupName = resultSet.getString("name");
                        String prefix = resultSet.getString("prefix");

                        GroupData groupData = new GroupData(groupName, prefix);
                        groupDataList.add(groupData);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return groupDataList;
        }, executor);
    }

    public static CompletableFuture<List<GroupMemberData>> retrieveGroupMembers() {
        return CompletableFuture.supplyAsync(() -> {
            List<GroupMemberData> groupDataList = new ArrayList<>();

            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `GroupMember`;")) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        UUID playerUUID = UUID.fromString(resultSet.getString("playerUUID"));
                        String displayName = resultSet.getString("displayName");
                        String groupName = resultSet.getString("groupName");
                        long seconds = Long.parseLong(resultSet.getString("seconds"));

                        GroupMemberData groupMemberData = new GroupMemberData(playerUUID, displayName, groupName, seconds);
                        groupDataList.add(groupMemberData);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return groupDataList;
        }, executor);
    }

    public static CompletableFuture<List<LogData>> retrieveLogData() {
        return CompletableFuture.supplyAsync(() -> {
            List<LogData> logDataList = new ArrayList<>();

            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `Log`;")) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        UUID playerUUID = UUID.fromString(resultSet.getString("playerUUID"));
                        String message = resultSet.getString("message");

                        LogData logData = new LogData(playerUUID, message);
                        logDataList.add(logData);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return logDataList;
        }, executor);
    }

    public static CompletableFuture<ServerInfo> retrieveServerInfo() {
        return CompletableFuture.supplyAsync(() -> {
            ServerInfo serverInfo = null;
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `ServerInfo`;")) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        long lastTimeOnline = Long.parseLong(resultSet.getString("lastTimeOnline"));

                        serverInfo = new ServerInfo(lastTimeOnline);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return serverInfo;
        }, executor);
    }

    public static CompletableFuture<Void> uploadGroupMember(GroupMember groupMember) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT INTO `GroupMember` (`playerUUID`, `displayName`, `groupName`, `seconds`) \n" +
                                 "VALUES (?, ?, ?, ?) \n" +
                                 "ON DUPLICATE KEY UPDATE \n" +
                                 "    `displayName` = VALUES(`displayName`), \n" +
                                 "    `groupName` = VALUES(`groupName`), \n" +
                                 "    `seconds` = VALUES(`seconds`);"
                 )
            ) {
                preparedStatement.setString(1, groupMember.getPlayerUUID().toString());
                preparedStatement.setString(2, groupMember.getDisplayName());
                preparedStatement.setString(3, groupMember.getGroupName());
                preparedStatement.setLong(4, groupMember.getRemainingSeconds());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public static CompletableFuture<Void> uploadGroup(Group group) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT IGNORE INTO `Group` (`name`, `prefix`) VALUES (?, ?);"
                 )
            ) {
                preparedStatement.setString(1, group.getGroupName());
                preparedStatement.setString(2, group.getGroupPrefix());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public static CompletableFuture<Void> uploadLog(UUID playerUUID, String message) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT IGNORE INTO `Log` (`playerUUID`, `message`) VALUES (?, ?);"
                 )
            ) {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setString(2, message);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public static void uploadServerInfo() {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE `ServerInfo` SET `lastTimeOnline` = ?;"
             )
        ) {
            preparedStatement.setLong(1, System.currentTimeMillis());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static CompletableFuture<Void> deleteLogEntry(UUID playerUUID, String message) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                String deleteQuery = "DELETE FROM `Log` WHERE `playerUUID` = ? AND `message` = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                    preparedStatement.setString(1, playerUUID.toString());
                    preparedStatement.setString(2, message);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public static CompletableFuture<Void> deleteGroupEntry(Group group) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                String deleteQuery = "DELETE FROM `Group` WHERE `name` = ? AND `prefix` = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                    preparedStatement.setString(1, group.getGroupName());
                    preparedStatement.setString(2, group.getGroupPrefix());
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }
}
