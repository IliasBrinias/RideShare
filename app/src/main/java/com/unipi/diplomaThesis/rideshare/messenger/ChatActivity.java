package com.unipi.diplomaThesis.rideshare.messenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.Model.Messages;
import com.unipi.diplomaThesis.rideshare.Model.MessageSessions;
import com.unipi.diplomaThesis.rideshare.Model.MyApplication;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.messenger.adapter.ChatAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQ_CHAT_ACTIVITY = 109;
    protected MyApplication mMyApp;

    TextView userName, userMessage;
    RecyclerView recyclerView;
    ImageButton buttonSend,buttonInfo,buttonBack;
    ImageView imageViewUser;
    ChatAdapter chatAdapter;
    List<Messages> messagesList = new ArrayList<>();
    MessageSessions messageSessions;
    User senderUser;
    Bitmap userImageBitmap=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        senderUser = User.loadUserInstance(this);
        if (!getIntent().hasExtra(MessageSessions.class.getSimpleName())) finish();
        messageSessions = (MessageSessions) getIntent().getSerializableExtra(MessageSessions.class.getSimpleName());
        if (senderUser == null) finish();
        userName = findViewById(R.id.textViewUserName);
        buttonInfo = findViewById(R.id.buttonInfo);
        userMessage = findViewById(R.id.autoCompleteMessage);
        recyclerView = findViewById(R.id.recyclerView);
        imageViewUser = findViewById(R.id.imageUser);
        buttonSend = findViewById(R.id.buttonSend);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);
        buttonSend.setOnClickListener(this);
        buttonInfo.setOnClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        chatAdapter = new ChatAdapter(this, messagesList, senderUser.getUserId(),userImageBitmap);
        recyclerView.setAdapter(chatAdapter);
        messageSearch();
        loadParticipantData();
        mMyApp = (MyApplication) this.getApplicationContext();
        userMessage.requestFocus();

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == buttonBack.getId()){
            finish();
        }else if (view.getId() == buttonInfo.getId()){
            Intent i = new Intent(this, ChatInfoActivity.class);
            ArrayList<String> participants = messageSessions.getParticipants();
            String participantId="";
            for (String p:participants){
                if (!p.equals(FirebaseAuth.getInstance().getUid())){
                    participantId = p;
                    break;
                }
            }
            i.putExtra(User.class.getSimpleName(),participantId);
            i.putExtra(MessageSessions.class.getSimpleName(), messageSessions.getMessageSessionId());
            startActivityForResult(i,REQ_CHAT_ACTIVITY);
        }else if (view.getId() == buttonSend.getId()){
            Messages m = new Messages(null,senderUser.getUserId(),userMessage.getText().toString(),new Date().getTime(),false);
            senderUser.sendMessageTo(this,participant, messageSessions,m,null);
            userMessage.setText("");
            closeKeyboard(this,userMessage.getWindowToken());
            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void refreshData(Messages m){
        boolean messageExists = false;
        try {
            for (int i = messagesList.size()-1; i>=0; i--){
                if (messagesList.get(i).getMessageId().equals(m.getMessageId())){
                    messagesList.set(i,m);
                    messageExists = true;
                    break;
                }
            }
        }catch (IndexOutOfBoundsException ignore){}
        if (!messageExists) messagesList.add(m);
        chatAdapter.notifyDataSetChanged();
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        }, 500);
    }
    private void messageSearch() {
        messagesList.clear();
        senderUser.loadMessages(messageSessions,this::refreshData);
    }
    public void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }
    User participant;
    public void loadParticipantData(){
        ArrayList<String> participants = messageSessions.getParticipants();
        String participantId="";
        boolean exists = false;
        for (String p:participants){
            if (!p.equals(FirebaseAuth.getInstance().getUid())){
                exists = true;
                participantId = p;
                break;
            }
        }
        if (!exists) finish();
        User.loadUser(participantId, new OnUserLoadComplete() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void returnedUser(User u) {
                participant = u;
                u.loadUserImage(image ->
                {
                    imageViewUser.setImageBitmap(null);
                    imageViewUser.setBackgroundResource(0);
                    if (image!=null){
                        imageViewUser.setImageBitmap(image);
                    }else{
                        imageViewUser.setBackgroundResource(R.drawable.ic_default_profile);
                    }
                    chatAdapter.notifyDataSetChanged();
                });
                userName.setText(User.reformatLengthString(u.getFullName(),20));

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        senderUser.stopLoadingMessages(messageSessions);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CHAT_ACTIVITY && resultCode == Messages.LEAVE_CHAT){
            finish();
        }
    }
}