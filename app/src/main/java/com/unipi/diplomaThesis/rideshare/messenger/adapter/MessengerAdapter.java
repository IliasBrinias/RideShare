package com.unipi.diplomaThesis.rideshare.messenger.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Interface.OnClickMessageSession;
import com.unipi.diplomaThesis.rideshare.Model.Messages;
import com.unipi.diplomaThesis.rideshare.Model.MessageSessions;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class MessengerAdapter extends RecyclerView.Adapter<MessengerAdapter.ViewHolder> {
    List<MessageSessions> messageSessionsList;
    OnClickMessageSession onClickMessageSession;
    private final SimpleDateFormat time = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    Context c;

    public MessengerAdapter(List<MessageSessions> messageSessionsList, Context c, OnClickMessageSession onClickMessageSession) {
        this.c = c;
        this.messageSessionsList = messageSessionsList;
        this.onClickMessageSession = onClickMessageSession;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messenger_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onClickMessageSession = onClickMessageSession;
        for (String p : messageSessionsList.get(position).getParticipants()){
            if (!p.equals(FirebaseAuth.getInstance().getUid())){
                User.loadUser(p, u -> loadUserData(u,holder));
            }
        }
        if (messageSessionsList.get(position).getMessages().isEmpty()) {
            holder.lastMessage.setText(c.getString(R.string.first_message_chat));
            holder.lastMessage.setVisibility(View.VISIBLE);
            makeMessageUnseen(holder);
            return;
        }
        Map.Entry<String, Messages> m = messageSessionsList.get(position).getMessages().entrySet().iterator().next();
        Messages lastMessages = m.getValue();
        holder.lastMessage.setText(cutMessageIfIsLong(lastMessages.getMessage(),12));
        if (!lastMessages.getUserSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            if (!lastMessages.isSeen()) {
                makeMessageUnseen(holder);
            }else {
                makeMessageSeen(holder);
            }
        }else {
            makeMessageSeen(holder);
        }
        holder.timeLastMessage.setText(time.format(lastMessages.getTimestamp()));
        holder.timeLastMessage.setVisibility(View.VISIBLE);
        holder.lastMessage.setVisibility(View.VISIBLE);
    }
    private String cutMessageIfIsLong(String mess,int bound){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<mess.length();i++){
            if (i>=bound) break;
            stringBuilder.append(mess.charAt(i));
        }
        if (stringBuilder.length()<mess.length()) stringBuilder.append("...");
        return stringBuilder.toString();
    }
    private void loadUserData(User u, ViewHolder holder){
        holder.userName.setText(User.reformatLengthString(u.getFullName(),20));
        holder.userName.setVisibility(View.VISIBLE);
        u.loadUserImage(image ->
        {
            holder.imageUser.setBackgroundResource(0);
            holder.imageUser.setImageBitmap(null);
            if (image != null) {
                holder.imageUser.setImageBitmap(image);
            }else {
                holder.imageUser.setBackgroundResource(R.drawable.ic_default_profile);
            }
            holder.imageUser.setVisibility(View.VISIBLE);
        });
    }
    private void makeMessageUnseen(@NonNull ViewHolder holder){
        holder.imageMessageUnseen.setVisibility(View.VISIBLE);
        holder.lastMessage.setTextColor(holder.unSeenColor);
        holder.timeLastMessage.setTextColor(holder.unSeenColor);
    }
    private void makeMessageSeen(@NonNull ViewHolder holder){
        holder.imageMessageUnseen.setVisibility(View.INVISIBLE);
        holder.lastMessage.setTextColor(holder.seenColor);
        holder.timeLastMessage.setTextColor(holder.seenColor);
    }

    @Override
    public int getItemCount() {
        return messageSessionsList.size();
    }
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageUser, imageMessageUnseen;
        TextView userName, lastMessage, timeLastMessage;
        OnClickMessageSession onClickMessageSession;
        ColorStateList seenColor,unSeenColor;
        public ViewHolder(@NonNull View v) {
            super(v);
            imageUser = v.findViewById(R.id.circleImageViewUser);
            imageUser.setVisibility(View.INVISIBLE);
            userName = v.findViewById(R.id.textViewUserName);
            userName.setVisibility(View.GONE);
            lastMessage = v.findViewById(R.id.textViewLastMessage);
            lastMessage.setVisibility(View.GONE);
            seenColor = lastMessage.getTextColors();
            timeLastMessage = v.findViewById(R.id.textViewLastMessageTime);
            timeLastMessage.setVisibility(View.GONE);
            imageMessageUnseen = v.findViewById(R.id.circleImageMessageUnseen);
            imageMessageUnseen.setVisibility(View.GONE);
            unSeenColor = imageMessageUnseen.getBackgroundTintList();
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onClickMessageSession.onClick(view,getAdapterPosition());
        }
    }
}
