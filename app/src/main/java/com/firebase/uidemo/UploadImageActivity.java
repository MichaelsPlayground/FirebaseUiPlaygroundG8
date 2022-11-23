package com.firebase.uidemo;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

/**
 * FirebaseUI does not provide a method to UPLOAD images, just for downloading
 * This is the same code as used in FirebasePlaygound
 */

public class UploadImageActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText edtLinkToImage;

    Button uploadImage;

    static final String TAG = "UploadImage";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    final static String IMAGE_STORAGE_FOLDER = "photos";
    private UploadTask uploadTask;

    private Uri selectedImageFileUri = null;
    private String selectedImageFileName = null;
    private ActivityResultLauncher<String[]> intentLauncher;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        signedInUser = findViewById(R.id.etUploadImageSignedInUser);
        progressBar = findViewById(R.id.pbUploadImage);
        edtLinkToImage = findViewById(R.id.etUploadImageLinkToImage);



        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // initialize Firebase Storage
        mStorage = FirebaseStorage.getInstance();

        intentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), fileUri -> {
                    hideProgressBar();
                    if (fileUri != null) {
                        selectedImageFileUri = fileUri;
                        Log.i(TAG, "intentLauncher with URI: " + selectedImageFileUri.toString());

                        // try to get a filename from that uri
                        selectedImageFileName = queryNameFromUri(this.getContentResolver(), selectedImageFileUri);
                        edtLinkToImage.setText("URI: " + selectedImageFileUri.toString()
                                + "\nFilename: " + selectedImageFileName);
                        //uploadFromUri(fileUri);
                        uploadImage.setEnabled(true);
                    } else {
                        Log.w(TAG, "File URI is null");
                    }
                });

        Button selectImage = findViewById(R.id.btnUploadImageSelectImage);
        Button backToMain = findViewById(R.id.btnUploadImageToMain);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "select an image from local to store");
                showProgressBar();
                // Pick an image from storage
                intentLauncher.launch(new String[]{ "image/*" });
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UploadImageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        uploadImage = findViewById(R.id.btnUploadImageSend);
        uploadImage.setEnabled(false); // default
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                uploadImage(selectedImageFileUri, selectedImageFileName);
            }
        });
    }

    private void uploadImage(Uri selectedImageFileUri, String selectedImageFileName) {
        uploadImage.setEnabled(false);
        String filenameFull = IMAGE_STORAGE_FOLDER + "/" + selectedImageFileName;
        Log.i(TAG, "uploadImage to " + filenameFull);
        // Create a reference to 'images/mountains.jpg'
        StorageReference uploadImagesRef = mStorage.getReference().child(filenameFull);
        uploadTask = uploadImagesRef.putFile(selectedImageFileUri);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "uploadImage onFailure: " + exception.toString());
                // Handle unsuccessful uploads
                System.out.println("*** ERROR: " + exception.toString());
                hideProgressBar();
                uploadImage.setEnabled(false);
                edtLinkToImage.setText("upload failure: " + exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                hideProgressBar();
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                long bytesTransferred = taskSnapshot.getBytesTransferred();
                StorageMetadata storageMetadata = taskSnapshot.getMetadata();
                String contentType = storageMetadata.getContentType();
                String name = storageMetadata.getName();
                long sizeBytes = storageMetadata.getSizeBytes();

                System.out.println("*** upload success ***");
                String uploadInfo = "bytesTransferred: " + bytesTransferred + "\n"
                + "contentType: " + contentType + "\n"
                                + "name: " + name + "\n"
                                + "sizeBytes: " + sizeBytes + "\n";
                System.out.println(uploadInfo);
                Log.i(TAG, "upload success\n" + uploadInfo);
                uploadImage.setEnabled(false);
                edtLinkToImage.setText("upload success");
            }
        });
    }

    /**
     * gets the filename of the image
     * https://developer.android.com/training/secure-file-sharing/retrieve-info.html
     * https://stackoverflow.com/a/38304115/8166854
     */
    private String queryNameFromUri(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
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

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                })
                .create();
        ad.show();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * basic
     */

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
        if (user != null) {
            authUserId = user.getUid();
            authUserEmail = user.getEmail();
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "no display name available";
            }
            String userData = String.format("Email: %s", authUserEmail);
            userData += "\nUID: " + authUserId;
            userData += "\nDisplay Name: " + authDisplayName;
            signedInUser.setText(userData);
            Log.i(TAG, "authUser: " + userData);
        } else {
            signedInUser.setText(null);
        }
    }
}