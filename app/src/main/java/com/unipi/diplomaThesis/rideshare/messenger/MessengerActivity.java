package com.unipi.diplomaThesis.rideshare.messenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnClickMessageSession;
import com.unipi.diplomaThesis.rideshare.Model.MessageSession;
import com.unipi.diplomaThesis.rideshare.Model.MyApplication;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.messenger.adapter.MessengerAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessengerActivity extends AppCompatActivity {
    protected MyApplication mMyApp;
    public static final int REQ_CHAT_ACTIVITY=123;
    RecyclerView recyclerViewMessageSession;
    User user;
    ProgressBar progressBar;
    MessengerAdapter messengerAdapter;
    ImageView imageViewBack;
    static List<MessageSession> messageSessionList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        user = User.loadUserInstance(this);
        if (user==null) finish();
        imageViewBack = findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(v->finish());
        recyclerViewMessageSession = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        //        initialize recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerViewMessageSession.setLayoutManager(linearLayoutManager);
        messengerAdapter = new MessengerAdapter(messageSessionList,this, new OnClickMessageSession() {
            @Override
            public void onClick(View v, int position) {
                Intent i =new Intent(MessengerActivity.this,ChatActivity.class);
                i.putExtra(MessageSession.class.getSimpleName(),messageSessionList.get(position));
               startActivityForResult(i,REQ_CHAT_ACTIVITY);
            }
        });
        recyclerViewMessageSession.setAdapter(messengerAdapter);
        routeSearch();
        mMyApp = (MyApplication) this.getApplicationContext();

    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void refreshData(MessageSession messageSession){
        stopProgressBarAnimation();
        if (messageSession == null) return;
//        check if the messageSession exists
        for (int i=0; i<messageSessionList.size(); i++){
            if (messageSessionList.get(i).getMessageSessionId().equals(messageSession.getMessageSessionId())){
                messageSessionList.remove(i);
                break;
            }
        }
        messageSessionList.add(messageSession);
        messageSessionList.sort(new Comparator<MessageSession>() {
            @Override
            public int compare(MessageSession messageSession0, MessageSession messageSession1) {
                try {
                    Long messageSessionTimestamp0 = messageSession0.getMessages().entrySet().iterator().next().getValue().getTimestamp();
                    Long messageSessionTimestamp1 = messageSession1.getMessages().entrySet().iterator().next().getValue().getTimestamp();
                    return Long.compare(messageSessionTimestamp1,messageSessionTimestamp0);

                }catch (Exception e){
                    return 0;
                }
            }
        });
        messengerAdapter.notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void routeSearch() {
        startProgressBarAnimation();
        messageSessionList.clear();
        messengerAdapter.notifyDataSetChanged();
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQ_CHAT_ACTIVITY){
            routeSearch();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}