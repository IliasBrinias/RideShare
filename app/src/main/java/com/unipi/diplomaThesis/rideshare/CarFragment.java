package com.unipi.diplomaThesis.rideshare;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.unipi.diplomaThesis.rideshare.Interface.OnImageLoad;
import com.unipi.diplomaThesis.rideshare.Model.Car;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Rider;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.rider.RiderActivity;

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
    User user;
    ProgressBar progressBar;
    TextView title;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_car, container, false);
        title = v.findViewById(R.id.textView6);
        carManufacturer = v.findViewById(R.id.editTextCarManufacturer);
        carModel = v.findViewById(R.id.editTextCarModel);
        carYear = v.findViewById(R.id.editTextCarYear);
        carPlate = v.findViewById(R.id.editTextCarPlate);
        carImage = v.findViewById(R.id.imageViewCar);
        buttonSaveCar = v.findViewById(R.id.buttonSaveCar);
        carManufacturerLayout = v.findViewById(R.id.textInputCarManufacturerLayout);
        carModelLayout = v.findViewById(R.id.textInputCarModelLayout);
        carYearLayout = v.findViewById(R.id.textInputCarYearLayout);
        carPlateLayout = v.findViewById(R.id.textInputCarPlateLayout);
        progressBar = v.findViewById(R.id.progressBar);
        stopProgressBarAnimation();
        carImage.setVisibility(View.GONE);
        carImage.setOnClickListener(view -> loadImageFromPhone());
        buttonSaveCar.setOnClickListener(this::saveCar);
        carPlateLayout.setEndIconVisible(false);
        carPlate.addTextChangedListener(this);
        carModel.addTextChangedListener(this);
        try {
            user = User.loadUserInstance(getActivity());
            if (user instanceof Driver) {
                loadDriverCarData();
            }else {
                carImage.setVisibility(View.VISIBLE);
            }
            return v;
        }catch (ClassCastException ignore){}
        return v;
    }
    private void saveCar(View view){
        List<EditText> editTexts = new ArrayList<>();
        editTexts.add(carModel);editTexts.add(carManufacturer);
        editTexts.add(carYear);editTexts.add(carPlate);
//        check for empty fields
        if (!User.checkIfEditTextIsNull(getActivity(),editTexts)){
//            create Driver and return the new User instance
            if (user instanceof Driver) {
                saveEditedCar();
                return;
            }
            createDriverDialog();
        }
    }
    RiderActivity riderActivity;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            Activity activity = (Activity) context;
            if (activity instanceof RiderActivity){
                riderActivity =(RiderActivity) activity;
            }
        }
    }

    private void createDriverDialog() {
        final View dialogView = View.inflate(getActivity(), R.layout.become_driver_alert_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.alert_dialog_background));
        TextView cancel = dialogView.findViewById(R.id.textViewCancel);
        Button submit = dialogView.findViewById(R.id.buttonSubmit);
        cancel.setOnClickListener(v-> alertDialog.dismiss());
        submit.setOnClickListener(v-> {
            alertDialog.dismiss();
            startProgressBarAnimation();
            Car c = new Car(
                    carModel.getText().toString(),
                    carManufacturer.getText().toString(),
                    carYear.getText().toString(),
                    carPlate.getText().toString()
            );
            ((Rider) user).becomeDriver(c, User.getByteArray(carImage),driver -> {
                riderActivity.setResult(RESULT_OK, new Intent());
                riderActivity.finish();

            });
        });
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private void saveEditedCar(){
        startProgressBarAnimation();
        ((Driver) user).saveCar(
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
                            Toast.makeText(getActivity(), getString(R.string.successfull_edit_car_driver), Toast.LENGTH_SHORT).show();
                            stopProgressBarAnimation();
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.error_message),Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        stopProgressBarAnimation();
                    }
                });

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
            if (data.getData() != null && resultCode == RESULT_OK) {
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
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
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
    public void afterTextChanged(Editable editable) {}
    private void loadDriverCarData(){
        title.setText(getString(R.string.edit_car));
        ((Driver) user).loadCarImage(new OnImageLoad() {
            @Override
            public void loadImageSuccess(Bitmap image) {
                carImage.setImageBitmap(null);
                carImage.setBackgroundResource(0);
                if (image != null){
                    carImage.setImageBitmap(image);
                }else {
                    carImage.setBackgroundResource(R.drawable.ic_car);
                }
                carImage.setVisibility(View.VISIBLE);
            }
        });
        carManufacturer.setText(((Driver) user).getOwnedCar().getMake());
        carModel.setText(((Driver) user).getOwnedCar().getModel());
        carYear.setText(((Driver) user).getOwnedCar().getYear());
        carPlate.setText(((Driver) user).getOwnedCar().getCarPlates());
    }
    public void startProgressBarAnimation(){
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }
    public void stopProgressBarAnimation(){
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }
}