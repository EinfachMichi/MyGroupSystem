package me.michi.mygroupsystem;

public enum GroupEventLogType {
    failed_invalid,
    failed_parse,
    failed_player_not_found,
    failed_group_not_found,
    failed_player_not_in_group,
    failed_add_already_member,
    failed_add_player,
    failed_player_no_group,

    success_create,
    success_add,
    success_remove,

    time_expired,
}
