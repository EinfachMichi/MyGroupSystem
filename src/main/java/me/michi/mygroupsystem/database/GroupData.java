package me.michi.mygroupsystem.database;

public class GroupData {
    private String groupName;
    private String prefix;

    public GroupData(String groupName, String prefix){
        this.groupName = groupName;
        this.prefix = prefix;
    }

    public String getGroupName(){
        return groupName;
    }

    public String getPrefix(){
        return prefix;
    }
}
