package com.unipi.diplomaThesis.rideshare.messenger.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Model.Messages;
import com.unipi.diplomaThesis.rideshare.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat date = new SimpleDateFormat("HH:mm MMM d");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateYear = new SimpleDateFormat("HH:mm  EEE, MMM d, ''yy");
    public static final int SENDER = 234;
    public static final int RECEIVER = 123;
    Context c;
    List<Messages> messagesList = new ArrayList<>();
    String userId;
    Bitmap participantProfileBitmap;
    public ChatAdapter(Context c, List<Messages> messagesList, String userId, Bitmap participantProfileBitmap) {
        this.c = c;
        this.messagesList = messagesList;
        this.userId = userId;
        this.participantProfileBitmap=participantProfileBitmap;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case RECEIVER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_receive_layout, parent, false);
                return new ViewHolderReceiver(view);
            case SENDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_send_layout, parent, false);
                return new ViewHolderSender(view);

            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).getUserSenderId().equals(userId)){
            return SENDER;
        }else {
            return RECEIVER;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case SENDER:
                sender((ViewHolderSender) holder,position);
                break;
            case RECEIVER:
                receiver((ViewHolderReceiver) holder, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    private void sender(ViewHolderSender holder, int position){
        String timeLabel = getTimeLabel(position);
        if (timeLabel!=null){
            holder.time.setText(timeLabel);
            holder.time.setVisibility(View.VISIBLE);
        }else {holder.time.setVisibility(View.GONE);}
        holder.message.setText(messagesList.get(position).getMessage());
        holder.linearLayout.setPadding(10, setPadding(position),10,10);
        if (messagesList.size()-1!=position) {
            holder.viewProgress.setVisibility(View.GONE);
            return;
        }
        if (messagesList.get(position).isSeen()){
            holder.viewProgress.setText(c.getString(R.string.seen));
        }else {
            holder.viewProgress.setText(c.getString(R.string.send));
        }
        holder.viewProgress.setVisibility(View.VISIBLE);
    }
    private void receiver(ViewHolderReceiver holder, int position){
        String timeLabel = getTimeLabel(position);
        if (timeLabel!=null){
            holder.time.setText(timeLabel);
            holder.time.setVisibility(View.VISIBLE);
        }else {holder.time.setVisibility(View.GONE);}
        holder.message.setText(messagesList.get(position).getMessage());
        holder.linearLayout.setPadding(10, setPadding(position),10,10);
    }
    private int setPadding(int position){
        if (position==0) return 10;
        try {
            if (messagesList.get(position).getUserSenderId().equals(
                    messagesList.get(position-1).getUserSenderId()
            )){
                return 0;
            }else {
                return 10;
            }
        }catch (IndexOutOfBoundsException ignore){
            return 0;
        }
    }
    private String getTimeLabel(int position){
        if (position==0){
            return dateYear.format(messagesList.get(position).getTimestamp());
        }
        try {
            switch (Messages.checkIfTheLastMessageIsOld(
                    messagesList.get(position).getTimestamp(),
                    messagesList.get(position-1).getTimestamp())) {
                case Messages.NO_TIME_LABEL_NEEDED:
                    return null;
                case Messages.TIME_FORMAT:
                    return time.format(messagesList.get(position).getTimestamp());
                case Messages.DATE_FORMAT:
                    return date.format(messagesList.get(position).getTimestamp());
            }
        }catch (IndexOutOfBoundsException indexOutOfBoundsException){
            return null;
        }
        return null;
    }
    public static class ViewHolderReceiver extends RecyclerView.ViewHolder{
        TextView time, message;
        LinearLayout linearLayout;

        public ViewHolderReceiver(@NonNull View v) {
            super(v);
            time = v.findViewById(R.id.textViewTimeReceiver);
            message = v.findViewById(R.id.textViewMessageReceive);
            linearLayout = v.findViewById(R.id.linearLayoutReceive);
        }
    }
    public static class ViewHolderSender extends RecyclerView.ViewHolder{
        TextView time, message,viewProgress;
        LinearLayout linearLayout;
        public ViewHolderSender(@NonNull View v) {
            super(v);
            time = v.findViewById(R.id.textViewTimeSender);
            message = v.findViewById(R.id.textViewMessageSender);
            viewProgress = v.findViewById(R.id.textViewViewProgress);
            linearLayout = v.findViewById(R.id.linearLayoutSender);
            viewProgress.setVisibility(View.GONE);
        }
    }

}
