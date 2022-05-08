package com.unipi.diplomaThesis.rideshare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.Model.Car;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.User;

import java.util.ArrayList;
import java.util.List;

public class CarFragment extends Fragment implements TextWatcher {
    private final int LOAD_IMAGE_CODE = 13;
    private static final int PICK_FROM_GALLERY = 43;

    public CarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    TextInputLayout carManufacturerLayout,
             carModelLayout,
             carYearLayout,
             carPlateLayout;
    EditText carManufacturer,
             carModel,
             carYear,
             carPlate;
    ImageView carImage;
    Button buttonSaveCar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_car, container, false);
        carManufacturer = v.findViewById(R.id.editTextCarManufacturer);
        carModel = v.findViewById(R.id.editTextCarModel);
        carYear = v.findViewById(R.id.editTextCarYear);
        carPlate = v.findViewById(R.id.editTextCarPlate);
        carImage = v.findViewById(R.id.circleImageViewCar);
        buttonSaveCar = v.findViewById(R.id.buttonSaveCar);
        carManufacturerLayout = v.findViewById(R.id.textInputCarManufacturerLayout);
        carModelLayout = v.findViewById(R.id.textInputCarModelLayout);
        carYearLayout = v.findViewById(R.id.textInputCarYearLayout);
        carPlateLayout = v.findViewById(R.id.textInputCarPlateLayout);
        carImage.setOnClickListener(view -> loadImageFromPhone());
        buttonSaveCar.setOnClickListener(this::saveCar);
        carPlateLayout.setEndIconVisible(false);
        carPlate.addTextChangedListener(this);
        carModel.addTextChangedListener(this);
        return v;
    }
    private void saveCar(View view){
        List<EditText> editTexts = new ArrayList<>();
        editTexts.add(carModel);editTexts.add(carManufacturer);
        editTexts.add(carYear);editTexts.add(carPlate);
//        check for empty fields
        if (!User.checkIfEditTextIsNull(getActivity(),editTexts)){
//            create Driver and return the new User instance
            Driver.createDriver(FirebaseAuth.getInstance().getUid(), new OnUserLoadComplete() {
                @Override
                public void returnedUser(User u) {
                    if (u == null) return;
                    Driver driver = (Driver) u;
//                    save the Drivers car
                    driver.saveCar(
                            new Car(
                                    carModel.getText().toString(),
                                    carManufacturer.getText().toString(),
                                    carYear.getText().toString(),
                                    carPlate.getText().toString()
                            ),
                            User.getByteArray(carImage),
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "You are a Driver", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent();
                                        getActivity().setResult(Driver.REQ_CREATE_DRIVER_ACCOUNT, i);
                                        getActivity().finish();
                                    }
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), getString(R.string.error_message),Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            });
                }
            });
        }
    }
    private void bitmapToImageView(ImageView imageView, Bitmap image) {
        if (image!=null){
            imageView.setImageBitmap(image);
        }else{
            carImage.setBackgroundResource(R.drawable.ic_image_upload);
        }
    }

    private void loadImageFromPhone(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }else {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, LOAD_IMAGE_CODE);
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
        if (data == null) return;
        if (requestCode == LOAD_IMAGE_CODE) {
            if (data.getData() != null && resultCode == Activity.RESULT_OK) {
                //Get image
                Uri localImagePath = data.getData();
                loadSelectedImageToImageView(localImagePath, carImage);
            }
        }
    }
    private void loadSelectedImageToImageView(Uri localImagePath, ImageView image){
        Glide.with(this).load(localImagePath).into(image);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.hashCode() == carPlate.getText().hashCode()){
            if (charSequence.toString().matches("[Α-Ω]{3}[-][0-9]{4}$")){
                carPlate.setError(null);
            }else {
                carPlate.setError(getString(R.string.error_format_car_plate)+"("+getString(R.string.e_x)+" ΑΒΓ-1234"+")");
            }
            if (carPlate.getError() == null && carPlate.getText() != null){
                carPlateLayout.setEndIconVisible(true);
                carPlateLayout.setBoxStrokeColor(Color.GREEN);
                carPlateLayout.setHintTextColor(ColorStateList.valueOf(Color.GREEN));
                carPlateLayout.setEndIconTintList(ColorStateList.valueOf(Color.GREEN));
                carPlateLayout.setStartIconTintList(ColorStateList.valueOf(Color.GREEN));
            }else{
                carPlateLayout.setEndIconVisible(false);
                carPlateLayout.setBoxStrokeColor(Color.RED);
                carPlateLayout.setHintTextColor(ColorStateList.valueOf(Color.RED));
                carPlateLayout.setStartIconTintList(ColorStateList.valueOf(Color.RED));
            }
        }

    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}