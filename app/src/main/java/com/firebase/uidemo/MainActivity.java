package com.firebase.uidemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser;
    Button signInWithEmailAndPassword;
    Button databaseUserProfile, databaseSendMessage, databaseListUser, databaseListUserRv;
    Button firestoreUserProfile;
    Button images, uploadImage, listImages;

    static final String TAG = "FirebaseUiMain";

    private FirebaseAuth mFirebaseAuth;
    public static final int RC_SIGN_IN = 1;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signedInUser = findViewById(R.id.etMainSignedInUser);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(MainActivity.this, "User Signed In", Toast.LENGTH_SHORT).show();
                    signedInUser.setText(user.getEmail() + "\nDisplayName: " + user.getDisplayName());
                    activeButtonsWhileUserIsSignedIn(true);
                } else {

                }
            }
        };

        /**
         * authentication sign-in/out section
         */

        signInWithEmailAndPassword = findViewById(R.id.btnMainSignInEmailPassword);
        signInWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(true)
                                .setAvailableProviders(providers)
                                .setTheme(R.style.Theme_FirebaseUiPlayground)
                                .build(),
                        RC_SIGN_IN
                );
            }
        });

        Button signOut = findViewById(R.id.btnMainSignOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sign out the current user");
                mFirebaseAuth.signOut();
                signedInUser.setText(null);
            }
        });

        /**
         * database section
         */

        databaseUserProfile = findViewById(R.id.btnMainDatabaseUser);
        databaseUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "show the user profile");
                Intent intent = new Intent(MainActivity.this, DatabaseUserActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        databaseListUser = findViewById(R.id.btnMainDatabaseListUser);
        databaseListUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "list user on database");
                Intent intent = new Intent(MainActivity.this, ListUserDatabaseActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        databaseListUserRv = findViewById(R.id.btnMainDatabaseListUserRv);
        databaseListUserRv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "list user on database in RecyclerView");
                Intent intent = new Intent(MainActivity.this, ListUserDatabaseRecyclerviewActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        databaseSendMessage = findViewById(R.id.btnMainDatabaseSendMessage);
        databaseSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "send a message to another user");
                // todo first we need to select a receipient, from there we move to ChatActivity
                //Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                Intent intent = new Intent(MainActivity.this, ListUserDatabaseActivity.class);
                intent.putExtra("ALL_USERS", false);
                startActivity(intent);
                //finish();
            }
        });

        /**
         * firestore database section
         */

        Button firestoreDatabaseUserProfile = findViewById(R.id.btnMainFirestoreDatabaseUser);
        firestoreDatabaseUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "show the user profile on Firestore Database");
                Intent intent = new Intent(MainActivity.this, FirestoreDatabaseUserActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        /**
         * storage section
         */

        images = findViewById(R.id.btnMainImages);
        images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "upload an image to Storage");
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        uploadImage = findViewById(R.id.btnMainUploadImage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "upload an image to Storage");
                Intent intent = new Intent(MainActivity.this, UploadImageActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        listImages = findViewById(R.id.btnMainListImages);
        listImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list images on Storage");
                Intent intent = new Intent(MainActivity.this, ListImagesActivity.class);
                startActivity(intent);
                //finish();
            }
        });

    }

    private void activeButtonsWhileUserIsSignedIn(boolean isSignedIn) {
        databaseUserProfile.setEnabled(isSignedIn);
        databaseListUser.setEnabled(isSignedIn);
        databaseListUserRv.setEnabled(isSignedIn);
        databaseSendMessage.setEnabled(isSignedIn);
        images.setEnabled(isSignedIn);
        uploadImage.setEnabled(isSignedIn);
        listImages.setEnabled(isSignedIn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);

    }
}