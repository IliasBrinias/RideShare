package com.unipi.diplomaThesis.rideshare.messenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnClickMessageSession;
import com.unipi.diplomaThesis.rideshare.Model.MessageSession;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.messenger.adapter.MessengerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessengerActivity extends AppCompatActivity {
    RecyclerView recyclerViewMessageSession;
    User user;
    ProgressBar progressBar;
    MessengerAdapter messengerAdapter;
    static List<MessageSession> messageSessionList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        user = User.loadUserInstance(this);
        if (user==null) finish();
        recyclerViewMessageSession = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        //        initialize recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerViewMessageSession.setLayoutManager(linearLayoutManager);
        messengerAdapter = new MessengerAdapter(messageSessionList, new OnClickMessageSession() {
            @Override
            public void onClick(View v, int position) {
                Intent i =new Intent(MessengerActivity.this,ChatActivity.class);
                i.putExtra(MessageSession.class.getSimpleName(),messageSessionList.get(position));
               startActivityForResult(i,REQ_CHAT_ACTIVITY);
            }
        });
        recyclerViewMessageSession.setAdapter(messengerAdapter);
        RouteSearch();
    }
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void refreshData(MessageSession messageSession){
        stopProgressBarAnimation();
        if (messageSession == null) return;
//        check if the messageSession exists
        boolean exists=false;
        for (int i=0; i<messageSessionList.size(); i++){
            if (messageSessionList.get(i).getMessageSessionId().equals(messageSession.getMessageSessionId())){
                messageSessionList.set(i,messageSession);
                exists = true;
                break;
            }
        }
        if (!exists) messageSessionList.add(messageSession);
        messengerAdapter.notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void RouteSearch() {
        startProgressBarAnimation();
        messageSessionList.clear();
        user.loadUserMessageSession(this::refreshData);
    }
    private void startProgressBarAnimation(){
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }
    private void stopProgressBarAnimation(){
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }
    public static final int REQ_CHAT_ACTIVITY=123;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQ_CHAT_ACTIVITY){
            RouteSearch();
        }

    }
}