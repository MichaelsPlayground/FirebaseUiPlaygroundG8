package com.firebase.uidemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.uidemo.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
//import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

public class UpdateUserImageDatabaseActivity extends AppCompatActivity {

    /*
    This class uses Glide to download and show the image
    https://egemenhamutcu.medium.com/displaying-images-from-firebase-storage-using-glide-for-kotlin-projects-3e4950f6c103
    https://itecnote.com/tecnote/java-using-firebase-storage-image-with-glide/
    https://firebaseopensource.com/projects/firebase/firebaseui-android/storage/readme
     */

    private CircularImageView userProfileImage;
    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    private static final String TAG = "UpdateUserProfileImage";
    private static int START_GALLERY_REQUEST = 1;
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference userProfileStorageReference;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_image_database);

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
        userProfileStorageReference= FirebaseStorage.getInstance().getReference().child("profile_images");

        Button loadData = findViewById(R.id.btnDatabaseUserLoad);
        // todo change to iv
        userProfileImage = findViewById(R.id.ivUserProfileImage);
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
                                    String photoUrl = userModel.getUserPhotoUrl();
                                    userPhotoUrl.setText(photoUrl);
                                    // todo change the code here:
                                    //Picasso.get().load(photoUrl).into(userProfileImage);
                                    // Download directly from StorageReference using Glide
                                    // (See MyAppGlideModule for Loader registration)
                                    GlideApp.with(view.getContext())
                                            .load(photoUrl)
                                            .into(userProfileImage);

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

        // click on profile image to load a new one
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,START_GALLERY_REQUEST);
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
                Intent intent = new Intent(UpdateUserImageDatabaseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == START_GALLERY_REQUEST && resultCode == RESULT_OK && data != null)
        {
            Uri uriimage = data.getData();
            // Start picker to get image for cropping and then use the image in cropping activity.
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();
                StorageReference filepath = userProfileStorageReference.child(mAuth + ".jpg");
                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                mDatabase.child("users").child(authUserId).child("userPhotoUrl")
                                        .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Log.i(TAG, "downloadUrl: " + downloadUrl);
                                                    Toast.makeText(UpdateUserImageDatabaseActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                    userPhotoUrl.setText(downloadUrl);
                                                    // todo reload changed image
                                                    // Download directly from StorageReference using Glide
                                                    // (See MyAppGlideModule for Loader registration)
                                                    GlideApp.with(getApplicationContext())
                                                            .load(downloadUrl)
                                                            .into(userProfileImage);
                                                }
                                                else
                                                {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(UpdateUserImageDatabaseActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });

            }
        }
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