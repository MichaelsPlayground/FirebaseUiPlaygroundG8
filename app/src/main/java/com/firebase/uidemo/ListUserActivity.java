package com.firebase.uidemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.uidemo.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListUserActivity extends AppCompatActivity {
    // https://www.geeksforgeeks.org/how-to-populate-recyclerview-with-firebase-data-using-firebaseui-in-android-studio/
    com.google.android.material.textfield.TextInputEditText signedInUser;

    static final String TAG = "ListUser";

    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    boolean listAllUsers = true;

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    ListView userListView;
    FirebaseListAdapter<UserModel> listAdapter;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        signedInUser = findViewById(R.id.etListUserSignedInUser);
        progressBar = findViewById(R.id.pbListUser);
        userListView = findViewById(R.id.lvListUser);

        // read the intent and check if all users are displayed or only "other" user
        // means leave the own userId data out
        Intent intent = getIntent();
        listAllUsers = intent.getBooleanExtra("ALL_USERS", true);

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
        DatabaseReference usersDatabase = mDatabase.child("users");
        usersDatabase.keepSynced(true);

        List<String> emailList = new ArrayList<>();
        List<String> displayNameList = new ArrayList<>();

        // this is the new way
        FirebaseListOptions<UserModel> listAdapterOptions;
        listAdapterOptions = new FirebaseListOptions.Builder<UserModel>()
                .setLayout(android.R.layout.simple_list_item_1)
                .setQuery(usersDatabase, UserModel.class)
                .build();

        listAdapter = new FirebaseListAdapter<UserModel>(listAdapterOptions) {
            @Override
            protected void populateView(@NonNull View v, @NonNull UserModel model, int position) {
                if (listAllUsers) {
                    String email = model.getUserMail();
                    String displayName = model.getUserName();
                    emailList.add(email);
                    displayNameList.add(displayName);
                    ((TextView) v.findViewById(android.R.id.text1)).setText(email);
                    listAdapter.notifyDataSetChanged();
                } else {
                    String listUid = model.getUserId();
                    if (authUserId.equals(listUid)) {
                        // do nothing, do not list the own user id
                    } else {
                        String email = model.getUserMail();
                        String displayName = model.getUserName();
                        emailList.add(email);
                        displayNameList.add(displayName);
                        ((TextView) v.findViewById(android.R.id.text1)).setText(email);
                        listAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
        userListView.setAdapter(listAdapter);

        /*
        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // this is the new way
                FirebaseListOptions<UserModel> listAdapterOptions;
                listAdapterOptions = new FirebaseListOptions.Builder<UserModel>()
                        .setLayout(android.R.layout.simple_list_item_1)
                        .setQuery(usersDatabase, UserModel.class)
                        .build();

                listAdapter = new FirebaseListAdapter<UserModel>(listAdapterOptions) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull UserModel model, int position) {
                        String email = model.getUserMail();
                        String displayName = model.getUserName();
                        emailList.add(email);
                        displayNameList.add(displayName);
                        ((TextView) v.findViewById(android.R.id.text1)).setText(email);
                        listAdapter.notifyDataSetChanged();
                    }
                };
                userListView.setAdapter(listAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

         */

        Button backToMain = findViewById(R.id.btnListUserToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListUserActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button listUser = findViewById(R.id.btnListUserRun);
        listUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.startListening();
            }
        });

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.i(TAG, "userListView clicked on pos: " + position);
                System.out.println("listAdapter.getRef(position): " + listAdapter.getRef(position).getKey());
                Intent intent = new Intent(ListUserActivity.this, ChatActivity.class);
                intent.putExtra("UID", listAdapter.getRef(position).getKey());
                intent.putExtra("EMAIL", emailList.get(position));
                intent.putExtra("DISPLAYNAME", displayNameList.get(position));
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
            listAdapter.startListening();
        } else {
            signedInUser.setText("no user is signed in");
        }
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override protected void onStop()
    {
        super.onStop();
        listAdapter.stopListening();
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