package com.unipi.diplomaThesis.rideshare;

import static com.unipi.diplomaThesis.rideshare.StartActivity.REQ_DRIVER_ACTIVITY;
import static com.unipi.diplomaThesis.rideshare.StartActivity.REQ_RIDER_ACTIVITY;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.gson.Gson;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.driver.DriverActivity;
import com.unipi.diplomaThesis.rideshare.rider.RiderActivity;

import java.util.ArrayList;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private static final int REQ_GOOGLE_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private EditText email, password;
    private SharedPreferences sharedPreferences;
    private String WEB_CLIENT_TOKEN = "719991187082-6o6stl5ugq4u52jkja0vulajud89sjun.apps.googleusercontent.com";
    private int REQ_USER_ACTIVITY = 719;
    private TextView registerTitle;
    private Button login, register;
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        registerTitle = v.findViewById(R.id.textViewOpenRegister);
        registerTitle.setOnClickListener(this::register);
        email = v.findViewById(R.id.autoCompleteEmail);
        password = v.findViewById(R.id.textInputRegisterPassword);
        login = v.findViewById(R.id.buttonLogin);
        login.setOnClickListener(this::login);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return v;
    }

    public void login(View view){
        List<EditText> editTexts = new ArrayList<>();
        editTexts.add(email);
        editTexts.add(password);
        if (User.checkIfEditTextIsNull(this.getContext(), editTexts)) return;
        ((StartActivity) getActivity()).startProgressBarAnimation();
        mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    User.loadSignInUser(new OnUserLoadComplete() {
                        @Override
                        public void returnedUser(User u) {
                            // if the user didn't found sign out
                            if (u == null){
                                FirebaseAuth.getInstance().signOut();
                                email.setText("");
                                password.setText("");
                                errorMessage();
                                return;
                            }
                            saveUserAndOpenActivity(u);
                            ((StartActivity) getActivity()).stopProgressBarAnimation();
                        }
                    });
                }else{
                    ((StartActivity) getActivity()).stopProgressBarAnimation();
                    switch (((FirebaseAuthException) task.getException()).getErrorCode()){
                        case "ERROR_WRONG_PASSWORD":
                            password.setError(getString(R.string.wrongPassword));
                            break;
                        case "ERROR_USER_NOT_FOUND":
                            errorMessage();
                            break;
                        default:
                            if (t!=null) t.cancel();
                            Toast.makeText(getActivity(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }
    private void saveUserAndOpenActivity(User u){
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString(User.REQ_TYPE_TAG,u.getType()).apply();
        Gson gson = new Gson();
        String json = gson.toJson(u);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString(User.class.getSimpleName(),json).apply();
        if (u.getType().equals(Driver.class.getSimpleName())){
            getActivity().startActivityForResult(new Intent(getActivity(), DriverActivity.class), REQ_DRIVER_ACTIVITY);
        }else {
            getActivity().startActivityForResult(new Intent(getActivity(), RiderActivity.class), REQ_RIDER_ACTIVITY);
        }
        ((StartActivity) getActivity()).stopProgressBarAnimation();
    }
    public void register(View view){
        ((StartActivity) getActivity()).register(view);
    }
    Toast t;
    private void errorMessage(){
        String error_msg = "User Not Found";
        email.setError(error_msg);
        password.setError(error_msg);
        mAuth.signOut();
        if (t!=null) t.cancel();
        t.makeText(getActivity(), error_msg, Toast.LENGTH_SHORT).show();
    }
}