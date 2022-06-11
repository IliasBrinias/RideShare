package com.unipi.diplomaThesis.rideshare.messenger;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Interface.OnClickDriverRoute;
import com.unipi.diplomaThesis.rideshare.Interface.OnImageLoad;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Message;
import com.unipi.diplomaThesis.rideshare.Model.MessageSession;
import com.unipi.diplomaThesis.rideshare.Model.Review;
import com.unipi.diplomaThesis.rideshare.Model.Rider;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.Route.RouteActivity;
import com.unipi.diplomaThesis.rideshare.driver.adapter.DriverRouteListAdapter;
import com.unipi.diplomaThesis.rideshare.messenger.adapter.ReviewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatInfoActivity extends AppCompatActivity {
    String participantId;
    ImageView userImage, imageCar, imageLeave, imageExit;
    TextView userName,
            carName,
            carYear,
            carPlate,
            averageRating,
            countReviews;
    Switch muteMessage;
    RatingBar ratingBar;
    RecyclerView recyclerView;
    TableRow tableRowCar, tableRowRating;
    DriverRouteListAdapter riderRouteAdapter;
    Rider rider=new Rider();
    Driver driver = new Driver();
    ReviewAdapter reviewAdapter;
    List<Review> reviewList = new ArrayList<>();
    RatingBar reviewRatingBar;
    EditText reviewDescription;
    String messageSessionId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);
        if (!getIntent().hasExtra(User.class.getSimpleName())) finish();
        messageSessionId = getIntent().getStringExtra(MessageSession.class.getSimpleName());
        participantId = getIntent().getStringExtra(User.class.getSimpleName());

        userImage = findViewById(R.id.imageViewUser);
        imageCar = findViewById(R.id.imageViewCarImage);
        imageExit = findViewById(R.id.exitChatInfo);
        imageLeave = findViewById(R.id.leaveChat);
        userName = findViewById(R.id.textViewUserName);
        carName = findViewById(R.id.textViewCarName);
        carPlate = findViewById(R.id.textViewCarPlate);
        carYear = findViewById(R.id.textViewCarYear);
        ratingBar = findViewById(R.id.ratingBar);
        averageRating = findViewById(R.id.textViewRatingAverage);
        countReviews = findViewById(R.id.textViewRatingsCount);
        muteMessage = findViewById(R.id.switchMute);
        recyclerView = findViewById(R.id.recyclerView);
        tableRowCar = findViewById(R.id.tableRowCar);
        tableRowRating = findViewById(R.id.tableRowRating);
        muteMessage.setChecked(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(messageSessionId,false));
        muteMessage.setOnCheckedChangeListener((compoundButton, b) ->
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(ChatInfoActivity.this.messageSessionId,muteMessage.isChecked()).apply());
        imageExit.setOnClickListener(view -> finish());
        imageLeave.setOnClickListener(view -> leaveChat());
        tableRowCar.setVisibility(View.GONE);
        loadParticipantData();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        List<User> drivers = new ArrayList<>();
        drivers.add(driver);
        riderRouteAdapter = new DriverRouteListAdapter(this, routeList, driver, new OnClickDriverRoute() {
            @Override
            public void itemClick(View routeInfo, View layoutOptions, ImageView show, ImageView edit, ImageView delete, int position) {
                Intent i =new Intent(ChatInfoActivity.this, RouteActivity.class);
                i.putExtra(Route.class.getSimpleName(),routeList.get(position).getRouteId());
                i.putExtra(Driver.class.getSimpleName(),routeList.get(position).getDriverId());
                startActivity(i);

            }
        });
        recyclerView.setAdapter(riderRouteAdapter);
        routeList.clear();
    }
    User u;
    private void loadParticipantData() {
        u = User.loadUserInstance(this);
        if (u.getType().equals(Rider.class.getSimpleName())){
            Driver.loadDriver(participantId,d->searchMutualRoutes((Driver) d, (Rider) u));
        }else {
            tableRowRating.setVisibility(View.GONE);
            Rider.loadUser(participantId, r->searchMutualRoutes((Driver) u, (Rider) r));
        }
    }
    private void searchMutualRoutes(Driver driver, Rider rider){
        if (FirebaseAuth.getInstance().getUid().equals(rider.getUserId())){
            userName.setText(driver.getFullName());
            driver.loadCarImage(car -> {
                imageCar.setImageBitmap(null);
                imageCar.setBackgroundResource(0);
                if (car != null){
                    imageCar.setImageBitmap(car);
                }else {
                    imageCar.setBackgroundResource(R.drawable.ic_car);
                }
                carName.setText(driver.getOwnedCar().getCarName());
                carPlate.setText(driver.getOwnedCar().getCarPlates());
                carYear.setText(driver.getOwnedCar().getYear());
                imageCar.setVisibility(View.VISIBLE);
                tableRowCar.setVisibility(View.VISIBLE);
            });
            driver.loadReviewTotalScore(driver.getUserId(),((totalScore, reviewCount) -> {
                ratingBar.setRating(totalScore);
                averageRating.setText(String.valueOf(totalScore));
                countReviews.setText(" ("+reviewCount+")");
            }));
        }else {
            userName.setText(rider.getFullName());
        }
        this.rider = rider;
        this.driver = driver;
        User.mutualRoutes(driver,rider,this::refreshData);
        if (rider.getUserId().equals(participantId)){
            loadParticipantImage(rider);
        }else {
            loadParticipantImage(driver);
        }
    }
    private void loadParticipantImage(User u){
        u.loadUserImage(new OnImageLoad() {
            @Override
            public void loadImageSuccess(Bitmap image) {
                userImage.setImageBitmap(null);
                userImage.setBackgroundResource(0);
                if (image != null){
                    userImage.setImageBitmap(image);
                }else {
                    userImage.setBackgroundResource(R.drawable.ic_default_profile);

                }
            }
        });
    }
    List<Route> routeList = new ArrayList<>();
    @SuppressLint("NotifyDataSetChanged")
    private void refreshData(Route r){
        routeList.add(r);
//        check if the driver exists
        riderRouteAdapter.notifyDataSetChanged();
    }
    public void openReviews(View v){
        final View dialogView = View.inflate(this, R.layout.rating_alert_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.alert_dialog_background));
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewReviews);
        reviewRatingBar = dialogView.findViewById(R.id.ratingBar);
        Button buttonSubmit = dialogView.findViewById(R.id.buttonSubmitReview);
        reviewDescription = dialogView.findViewById(R.id.editTextReviewDescription);
        buttonSubmit.setOnClickListener(view->saveReview(reviewRatingBar.getRating(),reviewDescription.getText().toString(),alertDialog));
        buttonSubmit.setVisibility(View.GONE);
        reviewDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().length()!=0){
                    try {
                        if (!userReview.getDescription().equals(charSequence.toString())){
                            buttonSubmit.setVisibility(View.VISIBLE);
                        }else {
                            throw new NullPointerException();
                        }
                    }catch (NullPointerException nullPointerException){
                        buttonSubmit.setVisibility(View.VISIBLE);
                    }
                }else {
                    buttonSubmit.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        reviewRatingBar.setOnRatingBarChangeListener((ratingBar, v1, b) -> {
            if (v1 !=0){
                try {
                    if (userReview.getReview()!=v1){
                        buttonSubmit.setVisibility(View.VISIBLE);
                    }else {
                        throw new NullPointerException();
                    }
                }catch (NullPointerException nullPointerException){
                    buttonSubmit.setVisibility(View.VISIBLE);
                }
            }else {
                buttonSubmit.setVisibility(View.GONE);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(reviewAdapter);
        reviewSearch();

        alertDialog.setCancelable(true);
        alertDialog.show();
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void saveReview(float rating, String description,AlertDialog alertDialog) {
        try {
            rider = (Rider) u;
            if (rider != null) {
                rider.saveReview(participantId, rating, description, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                        startActivity(ChatInfoActivity.this.getIntent());
                    }
                });
                Toast.makeText(this, getString(R.string.complete_review),Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        }catch (ClassCastException classCastException){
            classCastException.printStackTrace();
            finish();
        }
    }
    private void reviewSearch() {
        reviewList.clear();
        User.loadReviews(participantId,50,this::refreshData);
    }
    Review userReview;
    @SuppressLint("NotifyDataSetChanged")
    private void refreshData(Review review){
        if (review==null) return;
        if (review.getUserId().equals(FirebaseAuth.getInstance().getUid())){
            userReview = review;
            reviewRatingBar.setRating((float) review.getReview());
            reviewDescription.setText(review.getDescription());
            return;
        }
        reviewList.add(review);
        reviewAdapter.notifyDataSetChanged();
    }

    private void leaveChat(){
        final View dialogView = View.inflate(this, R.layout.leave_chat_alert_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(this.getDrawable(R.drawable.tablerow_background));

        ImageView userImage = dialogView.findViewById(R.id.imageViewProfile);
        TextView userName = dialogView.findViewById(R.id.textViewUserName);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        User.loadUser(participantId,u->{
            userName.setText(u.getFullName());
            u.loadUserImage(image -> {
                userImage.setImageBitmap(null);
                userImage.setBackgroundResource(0);
                if (image!=null){
                    userImage.setImageBitmap(image);
                }else {
                    userImage.setBackgroundResource(R.drawable.ic_default_profile);
                }
            });
        });
        dialogView.findViewById(R.id.textViewCancel).setOnClickListener(view -> alertDialog.dismiss());
        dialogView.findViewById(R.id.leaveChat).setOnClickListener(view -> {
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            u.leaveChat(participantId,task -> {
                progressBar.setVisibility(View.GONE);
                progressBar.setIndeterminate(false);
                alertDialog.dismiss();
                if (task.isSuccessful()){
                    setResult(Message.LEAVE_CHAT,new Intent());
                }else {
                    Toast.makeText(this, getString(R.string.something_happened), Toast.LENGTH_SHORT).show();
                }
                finish();
            });
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

}