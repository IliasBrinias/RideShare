package com.unipi.diplomaThesis.rideshare.Interface;

import com.unipi.diplomaThesis.rideshare.Model.Message;

public interface OnMessageAddedListener {
    void returnData(Message newMessage);
}
