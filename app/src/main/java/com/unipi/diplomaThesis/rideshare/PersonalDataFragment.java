package com.unipi.diplomaThesis.rideshare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.unipi.diplomaThesis.rideshare.Model.Rider;
import com.unipi.diplomaThesis.rideshare.Model.User;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PersonalDataFragment extends Fragment {
    private final int LOAD_IMAGE_CODE = 13;
    private final int LOAD_BACKGROUND_IMAGE_CODE = 76;
    private static final int PICK_FROM_GALLERY = 43;

    ImageView profile, background;
    EditText name, birthDate, email;
    TextInputLayout nameLayout, birthDateLayout, emailLayout;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());


    public PersonalDataFragment() {
        // Required empty public constructor
    }
    Rider r;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        r = (Rider) User.loadUserInstance(PreferenceManager.getDefaultSharedPreferences(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_personal_data, container, false);
//          images
        background = v.findViewById(R.id.imageViewPersonalDataBackground);
        profile = v.findViewById(R.id.circleImageViewPersonalDateUserImage);
//          layouts
        nameLayout = v.findViewById(R.id.textInputPersonalDataFirstNameLayout);
        birthDateLayout = v.findViewById(R.id.textInputPersonalDataBirthDayLayout);
        emailLayout = v.findViewById(R.id.textInputPersonalDataEmailLayout);
//          editTexts
        name = v.findViewById(R.id.textInputPersonalDataFirstName);
        birthDate = v.findViewById(R.id.textInputPersonalDataBirthDay);
        email = v.findViewById(R.id.textInputPersonalDataEmail);
//        initialize the listeners
        r.loadUserImage(image -> bitmapToImageView(profile,image));
        r.loadUserImageBackGround(image -> bitmapToImageView(background,image));
        profile.setOnClickListener(view -> loadImageFromPhone(view.getId()));
        background.setOnClickListener(view -> loadImageFromPhone(view.getId()));
//        load UsersData
        loadUserData();
        return v;
    }

    private void bitmapToImageView(ImageView imageView, Bitmap image) {
        if (image!=null){
            imageView.setImageBitmap(image);
        }else{
            switch (imageView.getId()){
                case R.id.circleImageViewPersonalDateUserImage:
                    profile.setBackgroundResource(R.drawable.ic_default_profile);
                    break;
                case R.id.imageViewPersonalDataBackground:
                    background.setBackgroundResource(R.drawable.default_profile_background);
                    break;
            }
        }
    }

    private void loadImageFromPhone(int id){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }else {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            switch (id){
                case R.id.imageViewPersonalDataBackground:
                    startActivityForResult(i, LOAD_BACKGROUND_IMAGE_CODE);
                    break;
                case R.id.circleImageViewPersonalDateUserImage:
                    startActivityForResult(i, LOAD_IMAGE_CODE);
                    break;


            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_FROM_GALLERY) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, LOAD_IMAGE_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_IMAGE_CODE || requestCode == LOAD_BACKGROUND_IMAGE_CODE) {
            if (data.getData() != null && resultCode == Activity.RESULT_OK) {
                //Get image
                Uri localImagePath = data.getData();
                switch (requestCode) {
                    case LOAD_IMAGE_CODE:
                        loadSelectedImageToImageView(localImagePath, profile);
                        break;
                    case LOAD_BACKGROUND_IMAGE_CODE:
                        loadSelectedImageToImageView(localImagePath, background);
                        break;
                }
            }
        }
    }
    private void loadSelectedImageToImageView(Uri localImagePath, ImageView image){
        Glide.with(this).load(localImagePath).into(image);
    }
//    TODO: make the edit form
    private void loadUserData(){
        name.setText(r.getFullName());
        email.setText(r.getEmail());
        if (r.getBirthDay()!=0){
            birthDate.setText(dateFormat.format(r.getBirthDay()));
        }
    }
}