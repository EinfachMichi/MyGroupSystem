package me.michi.mygroupsystem.logs;

public enum GroupSystemLogType {
    failed_invalid,
    failed_parse,
    failed_player_not_found,
    failed_group_not_found,
    failed_add_already_member,
    failed_add_player_to_group,
    failed_player_no_group,
    failed_only_player,
    failed_group_already_exists,

    success_create,
    success_add,
    success_remove,
    success_remove_after,

    time_expired,
}
