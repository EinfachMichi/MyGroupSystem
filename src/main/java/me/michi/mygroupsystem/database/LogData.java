package me.michi.mygroupsystem.database;

import java.util.UUID;

public record LogData(UUID playerUUID, String message) { }
