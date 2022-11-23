package com.firebase.uidemo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ImageActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    Button buttonChoosePhoto, buttonDownloadDirect;
    ImageView firstImage;
    ProgressBar progressBar;

    private static final String TAG = "ImageDemo";
    private static final int RC_CHOOSE_PHOTO = 101;
    private static final int RC_IMAGE_PERMS = 102;
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    final static String IMAGE_STORAGE_FOLDER = "photos";
    private static String selectedImageFileReference, mFileName, mFileUriString;
    private StorageReference mImageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        progressBar = findViewById(R.id.pbImages);
        buttonChoosePhoto = findViewById(R.id.button_choose_photo);
        buttonDownloadDirect = findViewById(R.id.button_download_direct);
        firstImage = findViewById(R.id.first_image);

        // todo get this part to work - the ListImagesActivity serves basis data to download the file
        Intent intent = getIntent();
        selectedImageFileReference = intent.getStringExtra("FILEREFERENCE");
        mFileName = intent.getStringExtra("FILENAME");
        mFileUriString = intent.getStringExtra("FILEURI");
        if (selectedImageFileReference != null) {
            Log.i(TAG, "selectedImageFileReference: " + selectedImageFileReference);
            StorageReference photosRef = FirebaseStorage.getInstance().getReference(IMAGE_STORAGE_FOLDER + "/" + mFileName);
            mImageRef = photosRef;
            buttonDownloadDirect.setEnabled(true);
        } else {
            buttonDownloadDirect.setEnabled(false);
        }

        buttonDownloadDirect.setOnClickListener(view -> {
            // Download directly from StorageReference using Glide
            // (See MyAppGlideModule for Loader registration)
            showProgressBar();
            GlideApp.with(ImageActivity.this)
                    .load(mImageRef)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(firstImage);
            hideProgressBar();
        });

        buttonChoosePhoto.setOnClickListener(view -> choosePhoto());

        /*
        // By default, Cloud Storage files require authentication to read or write.
        // For this sample to function correctly, enable Anonymous Auth in the Firebase console:
        // https://console.firebase.google.com/project/_/authentication/providers
        FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnCompleteListener(new SignInResultNotifier(this));

         */
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                showProgressBar();
                Uri selectedImage = data.getData();
                uploadPhoto(selectedImage);
            } else {
                Toast.makeText(this, "No image chosen", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE
                && EasyPermissions.hasPermissions(this, PERMS)) {
            choosePhoto();
        }
    }

    @AfterPermissionGranted(RC_IMAGE_PERMS)
    protected void choosePhoto() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, "rational_image_perm",
                    RC_IMAGE_PERMS, PERMS);
            return;
        }

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    private void uploadPhoto(Uri uri) {
        // Reset UI
        showProgressBar();
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        // Upload to Cloud Storage
        String uuid = UUID.randomUUID().toString();
        StorageReference photosRef = FirebaseStorage.getInstance().getReference();
        mImageRef = photosRef.child(IMAGE_STORAGE_FOLDER + "/" + uuid);
        //mImageRef = FirebaseStorage.getInstance().getReference(uuid);
        mImageRef.putFile(uri)
                .addOnSuccessListener(this, taskSnapshot -> {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "uploadPhoto:onSuccess:" +
                                taskSnapshot.getMetadata().getReference().getPath());
                    }
                    Toast.makeText(ImageActivity.this, "Image uploaded",
                            Toast.LENGTH_SHORT).show();

                    showDownloadUI();
                    hideProgressBar();
                })
                .addOnFailureListener(this, e -> {
                    hideProgressBar();
                    Log.w(TAG, "uploadPhoto:onError", e);
                    Toast.makeText(ImageActivity.this, "Upload failed",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void hideDownloadUI() {
        buttonDownloadDirect.setEnabled(false);
        firstImage.setImageResource(0);
        firstImage.setVisibility(View.INVISIBLE);
    }

    private void showDownloadUI() {
        buttonDownloadDirect.setEnabled(true);
        firstImage.setVisibility(View.VISIBLE);
    }


    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // See #choosePhoto with @AfterPermissionGranted
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,
                Collections.singletonList(PERMS))) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}