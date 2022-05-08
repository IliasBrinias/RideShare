package com.unipi.diplomaThesis.rideshare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Rider;
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
    private OnUserLoadComplete onUserLoadComplete;
    private TextView registerTitle;
    private Button login, register;
    private TableRow tableRowGoogleLogin, tableRowFacebookLogin;
    GoogleSignInClient mGoogleApiClient;
    private ActivityResultLauncher<IntentSenderRequest> loginResultHandler;
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
        email = v.findViewById(R.id.autoCompleteOriginPoint);
        password = v.findViewById(R.id.textInputRegisterPassword);
        login = v.findViewById(R.id.buttonLogin);
        tableRowGoogleLogin = v.findViewById(R.id.tableRowGoogleLogin);
        tableRowGoogleLogin.setOnClickListener(this::loginWithGoogle);
        login.setOnClickListener(this::login);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        loginResultHandler =
        registerForActivityResult(new ActivityResultContracts
                        .StartIntentSenderForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    System.out.println(task.getResult().getEmail());
                });
        return v;
    }

    public void loginWithGoogle(View view){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_TOKEN)
                .requestEmail()
                .build();
        mGoogleApiClient = GoogleSignIn.getClient(getActivity(),gso);
        Intent signInIntent = mGoogleApiClient.getSignInIntent();
        startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
    }

    public void login(View view){
        List<EditText> editTexts = new ArrayList<>();
        editTexts.add(email);
        editTexts.add(password);
        if (User.checkIfEditTextIsNull(this.getContext(), editTexts)) return;
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
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                    .putString(User.REQ_TYPE_TAG,u.getType()).apply();
                            Gson gson = new Gson();
                            String json = gson.toJson(u);
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                    .putString(User.class.getSimpleName(),json).apply();
                            if (u.getType().equals(Driver.class.getSimpleName())){
                                getActivity().startActivityForResult(new Intent(getActivity(), DriverActivity.class),StartActivity.REQ_DRIVER_ACTIVITY);
                            }else {
                                getActivity().startActivityForResult(new Intent(getActivity(), RiderActivity.class),StartActivity.REQ_RIDER_ACTIVITY);
                            }
                        }
                    });
                }else{
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_GOOGLE_SIGN_IN) {
            if (data == null) return;
            Task<GoogleSignInAccount> result = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = result.getResult(ApiException.class);
                handleSignInResult(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }else if(requestCode == REQ_USER_ACTIVITY){
            if (resultCode == Activity.RESULT_OK && data.getStringExtra("LogOut").equals("true")){
                try {
                    mGoogleApiClient.signOut();
                }catch (Exception ignored){
                }
            }
        }
    }

    private void handleSignInResult(GoogleSignInAccount acc) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Rider r = new Rider(mAuth.getUid(),acc.getEmail(),acc.getDisplayName(),null,null);
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(User.class.getSimpleName())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (!snapshot.hasChild(mAuth.getUid())){
                                                User.saveUser(r, task1 -> saveUserAndOpenActivity(r));
                                            }else {
                                                User u;
                                                if (snapshot.child(mAuth.getUid()).child("type").getValue(String.class).equals(Driver.class.getSimpleName())){
                                                    u = (Driver) snapshot.child(mAuth.getUid()).getValue(Driver.class);
                                                }else{
                                                    u = (Rider) snapshot.child(mAuth.getUid()).getValue(Rider.class);
                                                }
                                                saveUserAndOpenActivity(u);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }
                });
    }

    private void saveUserAndOpenActivity(User u){
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString("type",u.getType()).apply();
        Gson gson = new Gson();
        String json = gson.toJson(u);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString(User.class.getSimpleName(),json).apply();
        if (u.getType().equals(Driver.class.getSimpleName())){
            getActivity().startActivityForResult(new Intent(getActivity(), DriverActivity.class),StartActivity.REQ_DRIVER_ACTIVITY);
        }else {
            getActivity().startActivityForResult(new Intent(getActivity(), RiderActivity.class),StartActivity.REQ_RIDER_ACTIVITY);
        }
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