package com.unipi.diplomaThesis.rideshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.unipi.diplomaThesis.rideshare.Interface.OnCompleteUserSave;
import com.unipi.diplomaThesis.rideshare.Model.Rider;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.rider.RiderActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment implements TextWatcher {
    EditText email, password, passwordVerify, name;
    TextInputLayout passwordLayout, passwordVerifyLayout;
    TextView title;
    TextView openLogin;
    SharedPreferences sharedPreferences;
    FirebaseAuth mAuth;
    Button register;
    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void login(View view){
        ((StartActivity) getActivity()).login(view);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        title = v.findViewById(R.id.textViewRegisterTitle);
        email = v.findViewById(R.id.autoCompleteEmail);
        password = v.findViewById(R.id.textInputRegisterPassword);
        passwordLayout = v.findViewById(R.id.textInputPasswordLayout);
        passwordVerifyLayout = v.findViewById(R.id.textInputPasswordAgainLayout);
        passwordVerify = v.findViewById(R.id.textInputPasswordAgain);
        openLogin = v.findViewById(R.id.textViewOpenLogin);
        openLogin.setOnClickListener(this::login);
        passwordLayout.setEndIconVisible(false);
        passwordVerifyLayout.setEndIconVisible(false);
        name = v.findViewById(R.id.textInputPersonalDataFullName);
        register = v.findViewById(R.id.buttonRegister);
        register.setOnClickListener(this::register);
        mAuth = FirebaseAuth.getInstance();
        password.addTextChangedListener(this);
        passwordVerify.addTextChangedListener(this);
        return v;
    }
    private String checkPasswordFormat(String password){
        String errorMessage=getString(R.string.password_error_message_title);
        boolean isCorrect = true;
        if (password.length()<=6){
            errorMessage+="\n"+getString(R.string.password_format_error_length);
            isCorrect = false;
        }
        if (!password.matches(".*[0-9].*")){
            errorMessage+="\n" + getString(R.string.password_format_error_digit)+" ([0-9])";
            isCorrect = false;
        }
        if (isCorrect){
            return null;
        }else{
            return errorMessage;
        }
    }
    public void register(View view){
        //check if the fields have the right format
        List<EditText> editTexts = new ArrayList<>();
        editTexts.add(email);editTexts.add(password);editTexts.add(passwordVerify);
        editTexts.add(name);
        if (User.checkIfEditTextIsNull(getActivity(), editTexts)) return;
        // create authentication User
        mAuth.createUserWithEmailAndPassword(email.getText().toString(),
                password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(getActivity(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
//                Create User object
                        User u = new Rider(null,email.getText().toString(),
                                    name.getText().toString());
//                Save the user Object
                        User.saveUser(u, new OnCompleteUserSave() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                            .putString(User.REQ_TYPE_TAG,u.getType()).apply();
                                    Gson gson = new Gson();
                                    String json = gson.toJson(u);
                                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                                            .edit().putString(User.class.getSimpleName(),json).apply();
                                    getActivity().startActivityForResult(new Intent(getActivity(), RiderActivity.class),StartActivity.REQ_RIDER_ACTIVITY);
                                }
                            }
                        });

                    }
                });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //based on hashed editTexts texts i can specify which editText was called
        if (charSequence.hashCode() == password.getText().hashCode()){
            password.setError(checkPasswordFormat(charSequence.toString()));
            if (password.getError() == null && password.getText() != null){
                makeFieldAcceptable(passwordLayout,password);
            }else{
                makeFieldError(passwordLayout,password);
            }
        }else if (charSequence.hashCode() == passwordVerify.getText().hashCode()){
            if (passwordVerify.getText() == null) return;
            if (!charSequence.toString().equals(password.getText().toString())){
                makeFieldError(passwordVerifyLayout,passwordVerify);
            }else {
                makeFieldAcceptable(passwordVerifyLayout,passwordVerify);
            }
        }
    }
    @Override
    public void afterTextChanged(Editable editable) {
    }
    private void makeFieldError(TextInputLayout layout, EditText editText){
        editText.setError(getString(R.string.password_match_error));
        layout.setEndIconVisible(false);
        layout.setBoxStrokeColor(Color.RED);
        layout.setHintTextColor(ColorStateList.valueOf(Color.RED));
        layout.setStartIconTintList(ColorStateList.valueOf(Color.RED));
    }
    private void makeFieldAcceptable(TextInputLayout layout, EditText editText){
        editText.setError(null);
        layout.setEndIconVisible(true);
        layout.setBoxStrokeColor(Color.GREEN);
        layout.setHintTextColor(ColorStateList.valueOf(Color.GREEN));
        layout.setEndIconTintList(ColorStateList.valueOf(Color.GREEN));
        layout.setStartIconTintList(ColorStateList.valueOf(Color.GREEN));
    }
}