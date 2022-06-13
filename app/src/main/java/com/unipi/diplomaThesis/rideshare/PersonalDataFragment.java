package com.unipi.diplomaThesis.rideshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.UploadTask;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Passenger;
import com.unipi.diplomaThesis.rideshare.Model.Reviews;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.driver.DriverActivity;
import com.unipi.diplomaThesis.rideshare.messenger.adapter.ReviewAdapter;
import com.unipi.diplomaThesis.rideshare.passenger.PassengerActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PersonalDataFragment extends Fragment implements TextWatcher, CompoundButton.OnCheckedChangeListener {
    private static final int LOAD_IMAGE_CODE = 13;
    ImageView profile;
    EditText name, birthDate, email;
    TextInputLayout nameLayout, birthDateLayout, emailLayout;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    Button saveUserInfo, buttonChangePassword, buttonDeleteDriverAccount, buttonDeleteAccount;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch muteNotification;
    long tempUserBirthday=0;
    Bitmap userImage=null;
    TextInputLayout textInputLayoutOnlyForColor;
    ReviewAdapter reviewAdapter;
    RecyclerView recyclerView;
    RatingBar ratingBar;
    List<Reviews> reviewsList = new ArrayList<>();
    TextView textViewReviewTitle;

    public PersonalDataFragment() {
        // Required empty public constructor
    }
    User u;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_personal_data, container, false);
        try {
            u = User.loadUserInstance(getActivity());
            if (FirebaseAuth.getInstance().getUid()==null){
                getActivity().finish();
            }
        }catch (ClassCastException classCastException){
            classCastException.fillInStackTrace();
            getActivity().finish();
        }
        if (u.getType().equals(Driver.class.getSimpleName())) {
            textViewReviewTitle = v.findViewById(R.id.textViewReviewTitle);
            ratingBar = v.findViewById(R.id.ratingBar);
            recyclerView = v.findViewById(R.id.recyclerViewReviews);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
            recyclerView.setLayoutManager(linearLayoutManager);
            reviewAdapter = new ReviewAdapter(reviewsList);
            recyclerView.setAdapter(reviewAdapter);
            recyclerView.setNestedScrollingEnabled(false);
            reviewSearch();
            ((Driver) u).loadReviewTotalScore(u.getUserId(), (totalScore, reviewCount) -> {
                ratingBar.setRating(totalScore);
                ratingBar.setIndeterminate(true);
            });
        }
//          images
        profile = v.findViewById(R.id.circleImageViewPersonalDateUserImage);
//          layouts
        birthDateLayout = v.findViewById(R.id.textInputPersonalDataBirthDayLayout);
        emailLayout = v.findViewById(R.id.textInputPersonalDataEmailLayout);
        nameLayout = v.findViewById(R.id.textInputUserNameLayout);
        textInputLayoutOnlyForColor = nameLayout;
//          editTexts
        name = v.findViewById(R.id.textInputUserName);
        birthDate = v.findViewById(R.id.textInputPersonalDataBirthDay);
        email = v.findViewById(R.id.textInputPersonalDataEmail);
        saveUserInfo = v.findViewById(R.id.buttonSaveUserInfo);
        muteNotification = v.findViewById(R.id.switchMute);
        buttonDeleteAccount = v.findViewById(R.id.buttonDeleteAccount);
        buttonDeleteAccount.setOnClickListener(view -> deleteAccount());
        buttonChangePassword = v.findViewById(R.id.buttonChangePassword);

        muteNotification.setChecked(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("mute", false));
        buttonChangePassword.setOnClickListener(this::changePassword);
        muteNotification.setOnCheckedChangeListener(this);
        emailLayout.setEndIconVisible(false);
        birthDate.setOnClickListener(this::setDateTime);
        name.addTextChangedListener(this);
        email.addTextChangedListener(this);
        birthDate.addTextChangedListener(this);
        saveUserInfo.setVisibility(View.GONE);
        saveUserInfo.setOnClickListener(this::saveData);
        profile.setOnClickListener(view -> loadImageFromPhone());
//        load UsersData
        loadUserData();
        return v;
    }
    private void reviewSearch() {
        reviewsList.clear();
        User.loadReviews(u.getUserId(),5,this::refreshData);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void refreshData(Reviews reviews){
        if (reviews ==null) return;
        reviewsList.add(reviews);
        reviewAdapter.notifyDataSetChanged();
        ratingBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        textViewReviewTitle.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NonConstantResourceId")
    private void loadImageFromPhone(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},LOAD_IMAGE_CODE);
        }else {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, LOAD_IMAGE_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOAD_IMAGE_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length==0) return;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, LOAD_IMAGE_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (requestCode == LOAD_IMAGE_CODE) {
            if (data.getData() != null && resultCode == Activity.RESULT_OK) {
                //Get image
                Uri localImagePath = data.getData();
                loadSelectedImageToImageView(localImagePath, profile);
                saveUserInfo.setVisibility(View.VISIBLE);
            }
        }
    }
    private void loadSelectedImageToImageView(Uri localImagePath, ImageView image){
        Glide.with(this).load(localImagePath).into(image);
    }
    private void loadUserData(){
        name.setText(u.getFullName());
        email.setText(u.getEmail());
        tempUserBirthday = u.getBirthDay();
        if (u.getBirthDay()!=0){
            birthDate.setText(simpleDateFormat.format(u.getBirthDay()));
        }
        //        initialize the listeners
        u.loadUserImage(image -> {
            profile.setImageBitmap(null);
            profile.setBackgroundResource(0);
            if (image!=null){
                profile.setImageBitmap(image);
            }else{
                profile.setBackgroundResource(R.drawable.ic_default_profile);
            }
            userImage =((BitmapDrawable) profile.getDrawable()).getBitmap();
        });

    }
    public void setDateTime(View view){
        //load the custom date Picker to the alertDialog
        final View dialogView = View.inflate(getActivity(), R.layout.date_time_picker, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.tablerow_background));
        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
        DatePicker datePickerSpinner = dialogView.findViewById(R.id.date_pickerSpinner);
        datePicker.setVisibility(View.GONE);
        timePicker.setVisibility(View.GONE);
        datePickerSpinner.setVisibility(View.VISIBLE);
        Calendar maxCalendar = new GregorianCalendar();
        maxCalendar.setTimeInMillis(new Date().getTime());
        maxCalendar.add(Calendar.YEAR,-17);
        datePickerSpinner.setMaxDate(maxCalendar.getTimeInMillis());
        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = new GregorianCalendar(
                        datePickerSpinner.getYear(),
                        datePickerSpinner.getMonth(),
                        datePickerSpinner.getDayOfMonth());
                tempUserBirthday = calendar.getTimeInMillis();
                birthDate.setText(simpleDateFormat.format(tempUserBirthday));
                alertDialog.dismiss();
                checkIfIsDifferent();

            }
        });
        dialogView.findViewById(R.id.date_time_cancel).setOnClickListener(view1 -> alertDialog.dismiss());
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
    private void deleteAccount() {
        final View dialogView = View.inflate(getActivity(), R.layout.delete_account_alert_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.tablerow_background));
        EditText password = dialogView.findViewById(R.id.textInputPassword);
        TextInputLayout passwordLayout = dialogView.findViewById(R.id.textInputPasswordLayout);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        dialogView.findViewById(R.id.textViewCancel).setOnClickListener(view -> {
            alertDialog.dismiss();
        });
        dialogView.findViewById(R.id.deleteAccount).setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            OnCompleteListener<Void> onCompleteListener = new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    alertDialog.dismiss();
                    if (!task.isSuccessful()) {
                        if ("ERROR_WRONG_PASSWORD".equals(((FirebaseAuthException) task.getException()).getErrorCode())) {
                            makeFieldError(passwordLayout,password,getString(R.string.wrongPassword));
                        }
                        progressBar.setVisibility(View.GONE);
                        progressBar.setIndeterminate(false);
                    } else {
                        FirebaseAuth.getInstance().signOut();
                        getActivity().finish();
                    }
                }
            };
            if (u instanceof Passenger){
                ((Passenger) u).deleteAccount(password.getText().toString(), onCompleteListener);

            }else if (u instanceof Driver){
                ((Driver) u).deleteAccount(password.getText().toString(),onCompleteListener);
            }
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
    private void saveData(View v) {
        //load the custom date Picker to the alertDialog
        final View dialogView = View.inflate(getActivity(), R.layout.personal_data_save_alert_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.tablerow_background));
        EditText password = dialogView.findViewById(R.id.textInputPassword);
        TextInputLayout passwordLayout = dialogView.findViewById(R.id.textInputPasswordLayout);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        dialogView.findViewById(R.id.textViewCancel).setOnClickListener(view -> {
            alertDialog.dismiss();
        });
        dialogView.findViewById(R.id.saveData).setOnClickListener(view -> {
            saveUserDataWithPass(progressBar,passwordLayout,password,alertDialog);
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
    private void clearFocus(){
        name.clearFocus();
        email.clearFocus();
        birthDate.clearFocus();
    }
    private void saveUserDataWithPass(ProgressBar progressBar, TextInputLayout passwordLayout, EditText password, AlertDialog alertDialog){
        if (password.getText().toString().equals("")){
            makeFieldError(passwordLayout,password,getString(R.string.null_error_editText));
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        AuthCredential credential = EmailAuthProvider
                .getCredential(u.getEmail(), password.getText().toString());
        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            switch (((FirebaseAuthException) task.getException()).getErrorCode()){
                                case "ERROR_WRONG_PASSWORD":
                                    makeFieldError(passwordLayout,password,getString(R.string.wrongPassword));
                                    break;
                            }
                            progressBar.setVisibility(View.GONE);
                            progressBar.setIndeterminate(false);
                            alertDialog.dismiss();
                            clearFocus();
                            return;
                        }
                        if (!u.getEmail().equals(email.getText().toString())) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isComplete()) {
                                        updateData(getActivity(),alertDialog, progressBar);
                                    } else {
                                        switch (((FirebaseAuthException) task.getException()).getErrorCode()) {
                                            case "ERROR_INVALID_EMAIL":
                                                makeFieldError(emailLayout,email,getString(R.string.error_invalid_email));
                                            case "ERROR_EMAIL_ALREADY_IN_USE":
                                                makeFieldError(emailLayout,email,getString(R.string.error_email_already_in_use));
                                        }
                                            alertDialog.dismiss();
                                    }
                                }
                            });
                        }else {
                            updateData(getActivity(),alertDialog, progressBar);
                        }
                    }
                });
    }
    private void updateData(Context c, AlertDialog alertDialog, ProgressBar progressBar){
        u.saveUserPersonalData(c,profile, name.getText().toString(), email.getText().toString(), tempUserBirthday,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dataUpdated = true;
                        onDataUpdate(alertDialog, progressBar);
                    }
                }, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            photoUploaded = true;
                            onDataUpdate(alertDialog, progressBar);
                        }
                    }
                });
    }
    private void refreshUserInstance(){
        User.loadSignInUser(user -> {
            user.saveUserInstance(getActivity());
            u.setBirthDay(user.getBirthDay());
            u.setFullName(user.getFullName());
            u.setEmail(user.getEmail());
            if (getActivity() instanceof PassengerActivity){
                ((PassengerActivity) getActivity()).loadUserData(user);
            }else if (getActivity() instanceof DriverActivity){
                ((DriverActivity) getActivity()).loadUserData(user);
            }
            makeFieldsNormal();
        });
    }
    private void makeFieldsNormal(){
        makeFieldNormal(emailLayout,email);
        makeFieldNormal(nameLayout,name);
        makeFieldNormal(birthDateLayout,birthDate);
    }
    private void changePassword(View v){
        final View dialogView = View.inflate(getActivity(), R.layout.personal_data_save_alert_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.tablerow_background));
        EditText password = dialogView.findViewById(R.id.textInputPassword);
        TextInputLayout passwordLayout = dialogView.findViewById(R.id.textInputPasswordLayout);
        EditText newPassword = dialogView.findViewById(R.id.textInputNewPassword);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.textInputNewPasswordLayout);
        newPasswordLayout.setVisibility(View.VISIBLE);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        dialogView.findViewById(R.id.textViewCancel).setOnClickListener(view -> {
            alertDialog.dismiss();
        });
        dialogView.findViewById(R.id.saveData).setOnClickListener(view -> {
            if (password.getText().toString().equals("")){
                makeFieldError(passwordLayout,password,getString(R.string.null_error_editText));
                return;
            }
            if (newPassword.getText().toString().equals("")){
                makeFieldError(newPasswordLayout,newPassword,getString(R.string.null_error_editText));
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider
                    .getCredential(u.getEmail(), password.getText().toString());
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        user.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    progressBar.setIndeterminate(false);
                                    Toast.makeText(getActivity(), getString(R.string.password_changed_success), Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                    password.clearFocus();
                                    clearFocus();

                                }
                            }
                        });
                    }else {
                        switch (((FirebaseAuthException) task.getException()).getErrorCode()){
                            case "ERROR_WRONG_PASSWORD":
                                makeFieldError(passwordLayout,password,getString(R.string.wrongPassword));
                                break;
                        }
                        progressBar.setVisibility(View.GONE);
                        progressBar.setIndeterminate(false);
                        clearFocus();

                    }
                }
            });
        });
        alertDialog.setCancelable(true);
        alertDialog.show();

    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        if (name.isFocused()){
            checkIfIsDifferent();
            if (name.getText().toString().equals("")){
                makeFieldError(nameLayout,name,getString(R.string.null_error_editText));
                saveUserInfo.setVisibility(View.GONE);
            }else {
                makeFieldNormal(nameLayout,name);
            }
        }else if (email.isFocused()){
            if (charSequence.toString().matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                    + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")) {
                makeFieldAcceptable(emailLayout,email);
            }else{
                makeFieldError(emailLayout,email,getString(R.string.error_email_format));
            }
            checkIfIsDifferent();
        }
    }
    @Override
    public void afterTextChanged(Editable editable) {}
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == muteNotification.getId()){
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("mute",b).apply();
        }
    }

    static boolean photoUploaded = false;
    static boolean dataUpdated = false;
    private void onDataUpdate(AlertDialog alertDialog, ProgressBar progressBar){
        if (!photoUploaded || !dataUpdated) return;
        refreshUserInstance();
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(false);
        saveUserInfo.setVisibility(View.GONE);
        alertDialog.dismiss();
    }
    private void makeFieldError(TextInputLayout layout, EditText editText, String errorMessage){
        editText.setError(errorMessage);
        layout.setEndIconVisible(false);
        layout.setStartIconTintList(ColorStateList.valueOf(Color.RED));
        layout.setBoxStrokeColor(Color.RED);
        layout.setHintTextColor(ColorStateList.valueOf(Color.RED));
        layout.setStartIconTintList(ColorStateList.valueOf(Color.RED));
    }
    private void makeFieldAcceptable(TextInputLayout layout, EditText editText){
        editText.setError(null);
        layout.setEndIconVisible(true);
        layout.setStartIconTintList(ColorStateList.valueOf(Color.GREEN));
        layout.setBoxStrokeColor(Color.GREEN);
        layout.setHintTextColor(ColorStateList.valueOf(Color.GREEN));
        layout.setEndIconTintList(ColorStateList.valueOf(Color.GREEN));
    }
    private void makeFieldNormal(TextInputLayout layout, EditText editText){
        editText.setError(null);
        layout.setEndIconVisible(false);
        layout.setBoxStrokeColor(textInputLayoutOnlyForColor.getBoxStrokeColor());
        layout.setStartIconTintList(ColorStateList.valueOf(textInputLayoutOnlyForColor.getBoxStrokeColor()));
        layout.setHintTextColor(textInputLayoutOnlyForColor.getHintTextColor());
    }
    private void checkIfIsDifferent(){
        boolean isDifferent = false;
        if (!u.getFullName().equals(name.getText().toString())){
            isDifferent = true;
        }
        if (!u.getEmail().equals(email.getText().toString()) &&
                email.getText().toString().matches("^(.+)@(.+)$")){
            isDifferent = true;
        }
        if (u.getBirthDay() != tempUserBirthday){
            isDifferent = true;
        }
        if (isDifferent){
            saveUserInfo.setVisibility(View.VISIBLE);
        }else {
            saveUserInfo.setVisibility(View.GONE);
        }
    }

}