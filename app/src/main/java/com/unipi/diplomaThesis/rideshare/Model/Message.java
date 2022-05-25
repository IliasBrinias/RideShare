package com.unipi.diplomaThesis.rideshare.Model;

import java.io.Serializable;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Message implements Serializable {
    public static final int TIME_FORMAT = 132;
    public static final int DATE_FORMAT = 234;
    public static final int DATE_YEAR_FORMAT = 753;
    public static final int NO_TIME_LABEL_NEEDED = 345;
    public static final int MINUTES_AFTER_TIME = 10;

    private String messageId;
    private String userSenderId;
    private String message;
    private long timestamp;
    private boolean seen;

    public Message() {
    }

    public Message(String messageId, String userSenderId, String message, long timestamp, boolean seen) {
        this.messageId = messageId;
        this.userSenderId = userSenderId;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = seen;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserSenderId() {
        return userSenderId;
    }

    public void setUserSenderId(String userSenderId) {
        this.userSenderId = userSenderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public static int checkIfTheLastMessageIsOld(long currentMessageTime,long lastMessageTime){
        Calendar now = new GregorianCalendar();
        now.setTimeInMillis(new Date().getTime());

        Calendar current = new GregorianCalendar();
        current.setTimeInMillis(currentMessageTime);

        Calendar old = new GregorianCalendar();
        old.setTimeInMillis(lastMessageTime);

        Duration duration = Duration.between(current.toInstant(),old.toInstant());
        if (Math.abs(duration.toMinutes())>MINUTES_AFTER_TIME){
            Duration diffBasedNow = Duration.between(current.toInstant(),old.toInstant());
            if (Math.abs(diffBasedNow.toHours())>12){
                if (current.get(Calendar.DAY_OF_MONTH)!=old.get(Calendar.DAY_OF_MONTH)){
                    return  DATE_FORMAT;
                }
            }
            return TIME_FORMAT;
        }
        return NO_TIME_LABEL_NEEDED;
    }

}
