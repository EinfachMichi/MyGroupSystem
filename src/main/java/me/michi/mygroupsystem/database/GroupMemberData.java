package me.michi.mygroupsystem.database;

import java.util.UUID;

public record GroupMemberData(UUID playerUUID, String displayName, String groupName, long seconds) { }
