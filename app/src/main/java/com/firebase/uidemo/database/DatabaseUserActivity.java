package com.firebase.uidemo.database;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.uidemo.MainActivity;
import com.firebase.uidemo.R;
import com.firebase.uidemo.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class DatabaseUserActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    static final String TAG = "DatabaseUser";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_user);

        signedInUser = findViewById(R.id.etDatabaseUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseUser);

        warningNoData = findViewById(R.id.tvDatabaseUserNoData);
        userId = findViewById(R.id.etDatabaseUserUserId);
        userEmail = findViewById(R.id.etDatabaseUserUserEmail);
        userPhotoUrl = findViewById(R.id.etDatabaseUserPhotoUrl);
        userPublicKey = findViewById(R.id.etDatabaseUserPublicKey);
        userName = findViewById(R.id.etDatabaseUserUserName);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        //mDatabase = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button loadData = findViewById(R.id.btnDatabaseUserLoad);
        Button savaData = findViewById(R.id.btnDatabaseUserSave);
        Button backToMain = findViewById(R.id.btnDatabaseUserToMain);

        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                warningNoData.setVisibility(View.GONE);
                showProgressBar();
                Log.i(TAG, "load user data from database for user id: " + authUserId);
                if (!authUserId.equals("")) {
                    mDatabase.child("users").child(authUserId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            hideProgressBar();
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "Error getting data", task.getException());
                            } else {
                                // check for a null value means no user data were saved before
                                UserModel userModel = task.getResult().getValue(UserModel.class);
                                Log.i(TAG, String.valueOf(userModel));
                                if (userModel == null) {
                                    Log.i(TAG, "userModel is null, show message");
                                    warningNoData.setVisibility(View.VISIBLE);
                                    // get data from user
                                    userId.setText(authUserId);
                                    userEmail.setText(authUserEmail);
                                    userName.setText(usernameFromEmail(authUserEmail));
                                    userPublicKey.setText("not in use");
                                    userPhotoUrl.setText(authPhotoUrl);
                                } else {
                                    Log.i(TAG, "userModel email: " + userModel.getUserMail());
                                    warningNoData.setVisibility(View.GONE);
                                    // get data from user
                                    userId.setText(authUserId);
                                    userEmail.setText(userModel.getUserMail());
                                    userName.setText(userModel.getUserName());
                                    userPublicKey.setText(userModel.getUserPublicKey());
                                    userPhotoUrl.setText(userModel.getUserPhotoUrl());
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(),
                            "sign in a user before loading",
                            Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                }
            }
        });

        savaData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                Log.i(TAG, "save user data from database for user id: " + authUserId);
                if (!authUserId.equals("")) {
                    if (!Objects.requireNonNull(userId.getText()).toString().equals("")) {
                        warningNoData.setVisibility(View.GONE);
                        writeNewUser(authUserId, Objects.requireNonNull(userName.getText()).toString(),
                                Objects.requireNonNull(userEmail.getText()).toString(),
                                Objects.requireNonNull(userPhotoUrl.getText()).toString(),
                                Objects.requireNonNull(userPublicKey.getText()).toString());
                        Snackbar snackbar = Snackbar
                                .make(view, "data written to database", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "load user data before saving",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "sign in a user before saving",
                            Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DatabaseUserActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        } else {
            signedInUser.setText("no user is signed in");
        }
    }

    private void reload() {
        Objects.requireNonNull(mAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    /*
                    Toast.makeText(getApplicationContext(),
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
                     */
                } else {
                    Log.e(TAG, "reload", task.getException());
                    Toast.makeText(getApplicationContext(),
                            "Failed to reload user.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            authUserId = user.getUid();
            authUserEmail = user.getEmail();
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "";
            }
            if (user.getPhotoUrl() != null) {
                authPhotoUrl = Objects.requireNonNull(user.getPhotoUrl()).toString();
            } else {
                authPhotoUrl = "";
            }
            String userData = String.format("Email: %s", authUserEmail)
                    + String.format("\nemail address is verified: %s", user.isEmailVerified());
            if (user.getDisplayName() != null) {
                userData += String.format("\ndisplay name: %s", authDisplayName);
            } else {
                userData += "\nno display name available";
            }

            userData += "\nuser id: " + authUserId;
            if (user.getPhotoUrl() != null) {
                userData += String.format("\nphoto url: %s", Objects.requireNonNull(user.getPhotoUrl()).toString());
            } else {
                userData += "\nno photo url available";
            }
            signedInUser.setText(userData);

            if (user.isEmailVerified()) {
//                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
//                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }
        } else {
            signedInUser.setText(null);

        }
    }

    // try to read the entry for the signed-in user in RealtimeDatabase
    private void readUserDatabase(FirebaseUser user) {


    }

    public void writeNewUser(String userId, String name, String email, String photoUrl, String publicKey) {
        UserModel user = new UserModel(name, email, photoUrl, publicKey);
        mDatabase.child("users").child(userId).setValue(user);
    }

    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}