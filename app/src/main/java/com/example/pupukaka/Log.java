package com.example.pupukaka;

public class Log {
    private String action;
    private String timestamp;

    public Log() {}

    public Log(String action, String timestamp) {
        this.action = action;
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
