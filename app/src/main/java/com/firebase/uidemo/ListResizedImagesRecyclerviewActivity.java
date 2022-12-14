package com.firebase.uidemo;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class ListResizedImagesRecyclerviewActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    static final String TAG = "ListResizedImages";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    RecyclerView imagesRecyclerView;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    ImageAdapter adapter;
    ArrayList<String> imageList;
    ArrayList<String> imageNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_resized_images_recyclerview);

        signedInUser = findViewById(R.id.etListResizedImagesSignedInUser);
        progressBar = findViewById(R.id.pbListResizedImages);

        imagesRecyclerView = findViewById(R.id.rvListResizedImages);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // init the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        imageList = new ArrayList<>();
        imageNameList = new ArrayList<>();
        adapter = new ImageAdapter(imageList, imageNameList, this);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(null));

        Button listImages = findViewById(R.id.btnListResizedImagesRun);
        listImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "listResizedImages start");
                //showProgressBar();
                StorageReference listRef = mStorageRef.child("photos_resized");

                listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference file : listResult.getItems()) {
                            file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageList.add(uri.toString());
                                    String[] paths = uri.getLastPathSegment().split("/");
                                    imageNameList.add(paths[1]);
                                    Log.e("Itemvalue", uri.toString());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imagesRecyclerView.setAdapter(adapter);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });

            }
        });
        //hideProgressBar();


/*
list images

https://stackoverflow.com/questions/43826927/firebase-storage-and-android-images

// Reference to an image file in Cloud Storage
StorageReference storageReference = = FirebaseStorage.getInstance().getReference().child("myimage");

ImageView image = (ImageView)findViewById(R.id.imageView);

// Load the image using Glide
Glide.with(thisContext)
        .using(new FirebaseImageLoader())
                .load(storageReference)
                .into(image );

or

see: Put this class FirebaseImageLoader.java into your source, or write yourself.
https://github.com/firebase/FirebaseUI-Android/blob/master/storage/src/main/java/com/firebase/ui/storage/images/FirebaseImageLoader.java

Make a class anywhere in your app source like below.

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

@Override
public void registerComponents(Context context, Glide glide, Registry registry) {
    // Register FirebaseImageLoader to handle StorageReference
    registry.append(StorageReference.class, InputStream.class,
            new FirebaseImageLoader.Factory());
    }
}

list images:
StorageReference storageReference = FirebaseStorage
                                    .getInstance().getReference().child("myimage");

Glide.with(getApplicationContext())
      .load(completeStorageRefranceToImage)
      .into(imageView);
 */
        Button backToMain = findViewById(R.id.btnListResizedImagesToMain);

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListResizedImagesRecyclerviewActivity.this, MainActivity.class);
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
        if (currentUser != null) {
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
            String userData = String.format("Email: %s", authUserEmail);
            signedInUser.setText(userData);
        } else {
            signedInUser.setText(null);
        }
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