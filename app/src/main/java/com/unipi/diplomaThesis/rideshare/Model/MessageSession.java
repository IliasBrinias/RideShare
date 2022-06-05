package com.unipi.diplomaThesis.rideshare.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageSession implements Serializable {
    private String messageSessionId;
    private ArrayList<String> participants = new ArrayList<>();
    private long creationTimestamp;
    private boolean seen = false;
    private Map<String,Message> messages=new HashMap<>();

    public MessageSession(String messageSessionId, ArrayList<String> participants, long creationTimestamp, Map<String,Message> messages) {
        this.messageSessionId = messageSessionId;
        this.participants = participants;
        this.creationTimestamp = creationTimestamp;
        this.messages = messages;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public MessageSession() {
    }

    public String getMessageSessionId() {
        return messageSessionId;
    }

    public void setMessageSessionId(String messageSessionId) {
        this.messageSessionId = messageSessionId;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Map<String,Message> getMessages() {
        return messages;
    }

    public void setMessages(Map<String,Message> messages) {
        this.messages = messages;
    }
}
