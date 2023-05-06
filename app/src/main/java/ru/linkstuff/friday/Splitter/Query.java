package ru.linkstuff.friday.Splitter;

/**
 * Created by sasha on 02.05.2017.
 */
public class Query {

    String arg;
    String command;
    String extraArg;
    String argOfChange;

    public Query(String command, String arg){
        this.command = command;
        this.arg = arg;
    }

    public String getCommand() {
        return command;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public String getExtraArg() {
        return extraArg;
    }

    public void setExtraArg(String extraArg) {
        this.extraArg = extraArg;
    }

    public String getArgOfChange() {
        return argOfChange;
    }

    public void setArgOfChange(String argOfChange) {
        this.argOfChange = argOfChange;
    }
}
