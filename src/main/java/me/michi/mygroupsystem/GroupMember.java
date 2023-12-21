package me.michi.mygroupsystem;

public class GroupMember {
    private String name;
    private long seconds;

    public GroupMember(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public long getSeconds(){
        return seconds;
    }

    public void setSeconds(long seconds){
        this.seconds = seconds;
    }
}
