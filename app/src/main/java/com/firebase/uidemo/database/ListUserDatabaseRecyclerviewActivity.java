package com.firebase.uidemo.database;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.uidemo.ItemClickListener;
import com.firebase.uidemo.MainActivity;
import com.firebase.uidemo.R;
import com.firebase.uidemo.UserModelAdapter;
import com.firebase.uidemo.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ListUserDatabaseRecyclerviewActivity extends AppCompatActivity implements ItemClickListener {
    // https://www.geeksforgeeks.org/how-to-populate-recyclerview-with-firebase-data-using-firebaseui-in-android-studio/
    com.google.android.material.textfield.TextInputEditText signedInUser;

    static final String TAG = "ListUserDatabaseRv";

    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private RecyclerView recyclerView;
    UserModelAdapter adapter; // Create Object of the Adapter class
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_database_recyclerview);

        signedInUser = findViewById(R.id.etListUserDatabaseRvSignedInUser);
        progressBar = findViewById(R.id.pbListUserDatabaseRv);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        //mDatabase = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabase = FirebaseDatabase.getInstance().getReference();

        // Create a instance of the database and get
        // its reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //DatabaseReference personsDatabase = mDatabase.child("persons");
        DatabaseReference usersDatabase = mDatabase.child("users");
        recyclerView = findViewById(R.id.rvListUserDatabaseRv);
        // To display the Recycler view linearlayout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // It is a class provide by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        FirebaseRecyclerOptions<UserModel> options
                = new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(usersDatabase, UserModel.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new UserModelAdapter(options);
        adapter.setClickListener(this);
        /*
        // code from tutorial
        // It is a class provide by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        FirebaseRecyclerOptions<person> options
                = new FirebaseRecyclerOptions.Builder<person>()
                .setQuery(personsDatabase, person.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new personAdapter(options);
        */
        // Connecting Adapter class with the Recycler view*/
        recyclerView.setAdapter(adapter);

        Button backToMain = findViewById(R.id.btnListUserDatabaseRvToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListUserDatabaseRecyclerviewActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button listUser = findViewById(R.id.btnListUserDatabaseRvRun);
        listUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo start listening only if a user is signed in
                adapter.startListening();
            }
        });

        // note: the onClick listener is implemented in UserModelAdapter
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
        } else {
            signedInUser.setText("no user is signed in");
        }
        //adapter.startListening();
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override protected void onStop()
    {
        super.onStop();
        adapter.stopListening();
    }

    // called when clicking on recyclerview
    @Override
    public void onClick(View view, int position) {
        Log.i(TAG, "recyclerview clicked on position: " + position);
        /*
        String uidSelected = uidList.get(position);
        String emailSelected = emailList.get(position);
        String displayNameSelected = displayNameList.get(position);
        Intent intent = new Intent(ListUserRecyclerviewActivity.this, ChatActivity.class);
        intent.putExtra("UID", uidSelected);
        intent.putExtra("EMAIL", emailSelected);
        intent.putExtra("DISPLAYNAME", displayNameSelected);
        startActivity(intent);
        finish();
        */
    }


    private void reload() {
        Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mFirebaseAuth.getCurrentUser());
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



}