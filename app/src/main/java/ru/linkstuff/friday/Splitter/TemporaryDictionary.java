package ru.linkstuff.friday.Splitter;

/**
 * Created by sasha on 02.05.2017.
 */
public class TemporaryDictionary {
    private String command;
    private int id;

    TemporaryDictionary(String command, int id){
        this.command = command;
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public int getId() {
        return id;
    }
}
