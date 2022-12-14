package com.firebase.uidemo.storage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.uidemo.ImageActivity;
import com.firebase.uidemo.MainActivity;
import com.firebase.uidemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListImagesActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    static final String TAG = "ListImages";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    ListView imagesListView;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_images);

        signedInUser = findViewById(R.id.etListImagesSignedInUser);
        progressBar = findViewById(R.id.pbListImages);

        imagesListView = findViewById(R.id.lvListImages);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // init the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Button listImages = findViewById(R.id.btnListImagesRun);
        listImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "listImages start");
                showProgressBar();
                StorageReference listRef = mStorageRef.child("photos");

                List<String> arrayList = new ArrayList<>();
                List<String> fileNameList = new ArrayList<>();
                List<String> fileReferenceList = new ArrayList<>();
                List<StorageReference> storageReferenceList = new ArrayList<>();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListImagesActivity.this, android.R.layout.simple_list_item_1, arrayList);

                listRef.listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                Log.i(TAG, "listRef.listAll onSuccess");
                                for (StorageReference prefix : listResult.getPrefixes()) {
                                    // All the prefixes under listRef.
                                    // You may call listAll() recursively on them.
                                    //List<StorageReference> storagePrefixes;
                                    //storagePrefixes = listResult.getPrefixes();
                                    //arrayList.add(storagePrefixes.)
                                }
                                Log.i(TAG, "listResult.getItems size: " + listResult.getItems().size());
                                for (StorageReference item : listResult.getItems()) {
                                    // All the items under listRef.
                                    String listEntry = "item: " + item.toString()
                                            + "\nname: " + item.getName();
                                    Log.i(TAG, "item: " + listEntry);
                                    arrayList.add(listEntry);
                                    fileReferenceList.add(item.toString());
                                    fileNameList.add(item.getName());
                                    arrayAdapter.notifyDataSetChanged();
                                    storageReferenceList.add(item);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Uh-oh, an error occurred!
                                Log.e(TAG, "listAllImages failure: " + e);
                            }
                        });
                        imagesListView.setAdapter(arrayAdapter);
                hideProgressBar();

                imagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                        StorageReference storageReference = storageReferenceList.get(position);
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri dlUri = uri;
                                Log.i(TAG, "downloadUri: " + dlUri.toString());
                                Intent intent = new Intent(ListImagesActivity.this, ImageActivity.class);
                                intent.putExtra("FILEREFERENCE", fileReferenceList.get(position));
                                intent.putExtra("FILENAME", fileNameList.get(position));
                                intent.putExtra("FILEURI", dlUri.toString());
                                startActivity(intent);
                                finish();
                            }
                        });


                    }
                });
            }
        });

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

        Button listImagesPaginated = findViewById(R.id.btnListImagesPageRun);
        listImagesPaginated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "listImagesPaginated start");
                showProgressBar();
                StorageReference listRef = mStorageRef.child("photos");

                List<String> arrayList = new ArrayList<>();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListImagesActivity.this, android.R.layout.simple_list_item_1, arrayList);

                listRef.listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                Log.i(TAG, "listRef.listAll onSuccess");
                                for (StorageReference prefix : listResult.getPrefixes()) {
                                    // All the prefixes under listRef.
                                    // You may call listAll() recursively on them.
                                    //List<StorageReference> storagePrefixes;
                                    //storagePrefixes = listResult.getPrefixes();
                                    //arrayList.add(storagePrefixes.)
                                }
                                Log.i(TAG, "listResult.getItems size: " + listResult.getItems().size());
                                for (StorageReference item : listResult.getItems()) {
                                    // All the items under listRef.
                                    String listEntry = "item: " + item.toString()
                                            + " name: " + item.getName()
                                            + " bucket: " + item.getBucket();
                                    Log.i(TAG, "item: " + listEntry);
                                    arrayList.add(listEntry);
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Uh-oh, an error occurred!
                                Log.e(TAG, "listAllImages failure: " + e);
                            }
                        });
                imagesListView.setAdapter(arrayAdapter);

                //List<String> arrayList = new ArrayList<>();
                List<String> uidList = new ArrayList<>();
                List<String> emailList = new ArrayList<>();
                List<String> displayNameList = new ArrayList<>();


                hideProgressBar();

                imagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String uidSelected = uidList.get(position);
                        String emailSelected = emailList.get(position);
                        String displayNameSelected = displayNameList.get(position);
                        Intent intent = new Intent(ListImagesActivity.this, MainActivity.class);
                        intent.putExtra("UID", uidSelected);
                        intent.putExtra("EMAIL", emailSelected);
                        intent.putExtra("DISPLAYNAME", displayNameSelected);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

        Button backToMain = findViewById(R.id.btnListImagesToMain);

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListImagesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void listAllPaginated(@Nullable String pageToken) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference listRef = storage.getReference().child("files/uid");

        // Fetch the next page of results, using the pageToken if we have one.
        Task<ListResult> listPageTask = pageToken != null
                ? listRef.list(100, pageToken)
                : listRef.list(100);

        listPageTask
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> prefixes = listResult.getPrefixes();
                        List<StorageReference> items = listResult.getItems();

                        // Process page of results
                        // ...

                        // Recurse onto next page
                        if (listResult.getPageToken() != null) {
                            listAllPaginated(listResult.getPageToken());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred.
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