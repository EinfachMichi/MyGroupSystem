package me.michi.mygroupsystem.database;

import me.michi.mygroupsystem.Group;
import me.michi.mygroupsystem.GroupMember;
import me.michi.mygroupsystem.Main;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupSystemDataBase {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static CompletableFuture<List<GroupData>> retrieveGroupData() {
        return CompletableFuture.supplyAsync(() -> {
            List<GroupData> groupDataList = new ArrayList<>();

            try {
                Map<String, String> config = readYamlConfig();

                assert config != null;
                String host = String.valueOf(config.get("host"));
                String port = String.valueOf(config.get("port"));
                String database = config.get("name");
                String user = config.get("username");
                String password = config.get("password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                try (Connection connection = DriverManager.getConnection(url, user, password);
                     PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `Group`;");
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        String groupName = resultSet.getString("name");
                        String prefix = resultSet.getString("prefix");

                        GroupData groupData = new GroupData(groupName, prefix);
                        groupDataList.add(groupData);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return groupDataList;
        }, executor);
    }

    public static CompletableFuture<List<GroupMemberData>> retrieveGroupMembers() {
        return CompletableFuture.supplyAsync(() -> {
            List<GroupMemberData> groupDataList = new ArrayList<>();

            try {
                Map<String, String> config = readYamlConfig();

                assert config != null;
                String host = String.valueOf(config.get("host"));
                String port = String.valueOf(config.get("port"));
                String database = config.get("name");
                String user = config.get("username");
                String password = config.get("password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                try (Connection connection = DriverManager.getConnection(url, user, password);
                     PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `GroupMember`;");
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        UUID playerUUID = UUID.fromString(resultSet.getString("playerUUID"));
                        String displayName = resultSet.getString("displayName");
                        String groupName = resultSet.getString("groupName");
                        long seconds = Long.parseLong(resultSet.getString("seconds"));

                        GroupMemberData groupMemberData = new GroupMemberData(playerUUID, displayName, groupName, seconds);
                        groupDataList.add(groupMemberData);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return groupDataList;
        }, executor);
    }

    public static CompletableFuture<List<LogData>> retrieveLogData() {
        return CompletableFuture.supplyAsync(() -> {
            List<LogData> logDataList = new ArrayList<>();

            try {
                Map<String, String> config = readYamlConfig();

                assert config != null;
                String host = String.valueOf(config.get("host"));
                String port = String.valueOf(config.get("port"));
                String database = config.get("name");
                String user = config.get("username");
                String password = config.get("password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                try (Connection connection = DriverManager.getConnection(url, user, password);
                     PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `Log`;");
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        UUID playerUUID = UUID.fromString(resultSet.getString("playerUUID"));
                        String message = resultSet.getString("message");

                        LogData logData = new LogData(playerUUID, message);
                        logDataList.add(logData);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return logDataList;
        }, executor);
    }

    public static CompletableFuture<Void> uploadGroupMember(GroupMember groupMember) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> config = readYamlConfig();

                assert config != null;
                String host = String.valueOf(config.get("host"));
                String port = String.valueOf(config.get("port"));
                String database = config.get("name");
                String user = config.get("username");
                String password = config.get("password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                try (Connection connection = DriverManager.getConnection(url, user, password);
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public static CompletableFuture<Void> uploadGroup(Group group) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> config = readYamlConfig();

                assert config != null;
                String host = String.valueOf(config.get("host"));
                String port = String.valueOf(config.get("port"));
                String database = config.get("name");
                String user = config.get("username");
                String password = config.get("password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                try (Connection connection = DriverManager.getConnection(url, user, password);
                     PreparedStatement preparedStatement = connection.prepareStatement(
                             "INSERT IGNORE INTO `Group` (`name`, `prefix`) VALUES (?, ?);"
                     )
                ) {
                    preparedStatement.setString(1, group.getGroupName());
                    preparedStatement.setString(2, group.getGroupPrefix());

                    preparedStatement.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public static CompletableFuture<Void> uploadLog(UUID playerUUID, String message) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> config = readYamlConfig();

                assert config != null;
                String host = String.valueOf(config.get("host"));
                String port = String.valueOf(config.get("port"));
                String database = config.get("name");
                String user = config.get("username");
                String password = config.get("password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                try (Connection connection = DriverManager.getConnection(url, user, password);
                     PreparedStatement preparedStatement = connection.prepareStatement(
                             "INSERT IGNORE INTO `Log` (`playerUUID`, `message`) VALUES (?, ?);"
                     )
                ) {
                    preparedStatement.setString(1, playerUUID.toString());
                    preparedStatement.setString(2, message);

                    preparedStatement.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public static CompletableFuture<Void> deleteLogEntry(UUID playerUUID, String message) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> config = readYamlConfig();

                assert config != null;
                String host = String.valueOf(config.get("host"));
                String port = String.valueOf(config.get("port"));
                String database = config.get("name");
                String user = config.get("username");
                String password = config.get("password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                try (Connection connection = DriverManager.getConnection(url, user, password)) {
                    String deleteQuery = "DELETE FROM `Log` WHERE `playerUUID` = ? AND `message` = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                        preparedStatement.setString(1, playerUUID.toString());
                        preparedStatement.setString(2, message);
                        preparedStatement.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    private static Map<String, String> readYamlConfig() {

        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.yml")) {
            Yaml yaml = new Yaml();
            return yaml.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
