package com.firebase.uidemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.uidemo.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class UpdateUserImageFirestoreActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    /*
    This class uses Glide to download and show the image
    https://egemenhamutcu.medium.com/displaying-images-from-firebase-storage-using-glide-for-kotlin-projects-3e4950f6c103
    https://itecnote.com/tecnote/java-using-firebase-storage-image-with-glide/
    https://firebaseopensource.com/projects/firebase/firebaseui-android/storage/readme
     */

    /**
     * This class is NOT using firestoreUi for the upload purposes
     */

    private CircleImageView profileImageView;
    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    private static final String TAG = "UpdateProfImgFirestore";

    private static int START_GALLERY_REQUEST = 1;
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference userProfileStorageReference;
    ProgressBar progressBar;

    private ProgressDialog progressDialog;

    // used for EasyPermissions
    private static final String[] READ_EXTERNAL_STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] READ_MEDIA_IMAGES_PERMISSION = {Manifest.permission.READ_MEDIA_IMAGES};
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERM = 123;
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES_PERM = 124;

    /*
    Starting from Target 13, you must request one or more of the following granular media permissions
    instead of the READ_EXTERNAL_STORAGE permission: READ_MEDIA_IMAGES for accessing images.
    READ_MEDIA_VIDEO for accessing videos. READ_MEDIA_AUDIO for accessing audio files.
    https://levelup.gitconnected.com/read-external-storage-permission-is-deprecated-heres-the-new-way-to-access-android-storage-8ce0644e9955

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_image_firestore);

        signedInUser = findViewById(R.id.etFirestoreDatabaseUserSignedInUser);
        progressBar = findViewById(R.id.pbFirestoreDatabaseUser);

        warningNoData = findViewById(R.id.tvFirestoreDatabaseUserNoData);
        userId = findViewById(R.id.etFirestoreDatabaseUserUserId);
        userEmail = findViewById(R.id.etFirestoreDatabaseUserUserEmail);
        userPhotoUrl = findViewById(R.id.etFirestoreDatabaseUserPhotoUrl);
        userPublicKey = findViewById(R.id.etFirestoreDatabaseUserPublicKey);
        userName = findViewById(R.id.etFirestoreDatabaseUserUserName);

        profileImageView = findViewById(R.id.ivFirestoreUserProfileImage);

        progressDialog = new ProgressDialog(this);

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

        Button loadData = findViewById(R.id.btnFirestoreDatabaseUserLoad);

        Button savaData = findViewById(R.id.btnFirestoreDatabaseUserSave);
        Button backToMain = findViewById(R.id.btnFirestoreDatabaseUserToMain);

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
                                            .into(profileImageView);

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
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "click on profileImage, now check permissions");
                //checkPermissions();
                // todo check for build version
                //readExternalStorageTask();
                readMediaImagesTask();

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
                Intent intent = new Intent(UpdateUserImageFirestoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * permission check with EasyPermissions
     */

    private boolean hasReadExternalStoragePermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private boolean hasReadMediaImagesPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_MEDIA_IMAGES);
    }

    @AfterPermissionGranted(REQUEST_CODE_READ_MEDIA_IMAGES_PERM)
    public void readMediaImagesTask() {
        if (hasReadMediaImagesPermission()) {
            // Have permission, do the thing!
            Toast.makeText(this, "TODO: read media images things", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "checkPermissions -> granted");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, START_GALLERY_REQUEST);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs read access to your storage so you can select a profile image",
                    REQUEST_CODE_READ_MEDIA_IMAGES_PERM,
                    Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_READ_EXTERNAL_STORAGE_PERM)
    public void readExternalStorageTask() {
        if (hasReadExternalStoragePermission()) {
            // Have permission, do the thing!
            Toast.makeText(this, "TODO: read external storage things", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "checkPermissions -> granted");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, START_GALLERY_REQUEST);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs read access to your storage so you can select a profile image",
                    REQUEST_CODE_READ_EXTERNAL_STORAGE_PERM,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // permission activity results
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            String yes = "Yes";
            String no = "No";

            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                            this,
                            getString(R.string.returned_from_app_settings_to_activity,
                                    hasReadExternalStoragePermission() ? yes : no),
                            Toast.LENGTH_LONG)
                    .show();
        }
        // now the requests for cropping
        if (requestCode == START_GALLERY_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                resizeImage(data.getData());
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            profileImageView.setImageURI(result.getUri());
            uploadImage(result.getUri());
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.i(TAG, "onRationaleAccepted:" + requestCode);
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.i(TAG, "onRationaleDenied:" + requestCode);
    }

    /**
     * Permission check with Dexter does not work anymore on Android 13+
     */

    private void checkPermissions() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Log.i(TAG, "checkPermissions -> granted");
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, START_GALLERY_REQUEST);
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Log.i(TAG, "checkPermissions -> denied");
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        Log.i(TAG, "checkPermissions -> RationaleShouldBeShown");
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_GALLERY_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                resizeImage(data.getData());
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            profileImageView.setImageURI(result.getUri());
            uploadImage(result.getUri());
        }
    }
*/


    private void uploadImage(Uri uri) {
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(mAuth.getUid() + ".jpg");
        storageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                                if (task.isSuccessful()) {
                                                    Log.i(TAG, "downloadUrl: " + downloadUrl);
                                                    Toast.makeText(UpdateUserImageFirestoreActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                    userPhotoUrl.setText(downloadUrl);
                                                    // todo reload changed image
                                                    // Download directly from StorageReference using Glide
                                                    // (See MyAppGlideModule for Loader registration)
                                                    GlideApp.with(getApplicationContext())
                                                            .load(downloadUrl)
                                                            .into(profileImageView);
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(UpdateUserImageFirestoreActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                }
                                                progressDialog.dismiss();
                                            }
                                        });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateUserImageFirestoreActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                    }
                });

                /* original
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Image upload success", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.cancel();
                        Toast.makeText(getApplicationContext(), "Image upload failure " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }); */
    }

    private void resizeImage(Uri data) {
        CropImage.activity(data)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1 , 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setMaxCropResultSize(512, 512)
                //.setOutputCompressQuality(50)
                .start(this);
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