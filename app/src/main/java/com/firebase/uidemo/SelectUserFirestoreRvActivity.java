package com.firebase.uidemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.uidemo.models.UserFirestoreModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectUserFirestoreRvActivity extends AppCompatActivity {

    /**
     * This class is similar to ListUserDatabase but it checks for an incoming intent to
     * select where to return:
     * no intentExtra given or not matching the following => return to MainActivity
     * intentExtra "CALLER_ACTIVITY" == "SEND_MESSAGE_DATABASE" => return to SendMessageDatabaseActivity
     * intentExtra "CALLER_ACTIVITY" == "CHAT_MESSAGE_DATABASE" => return to ChatMessageDatabaseActivity
     * intentExtra "CALLER_ACTIVITY" == "LIST_MESSAGE_DATABASE" => return to ListMessageDatabaseActivity
     */
    Intent returnIntent;

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    SwitchMaterial listOnlineUserOnly;
    TextView warningNoData;

    static final String TAG = "SelectUserFirestoreRv";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    RecyclerView listFirestore;
    FirestoreRecyclerAdapter adapter;

    private FirebaseAuth mAuth;
    FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();
    private static final String CHILD_USERS = "users";
    ProgressBar progressBar;

    List<String> arrayList = new ArrayList<>();
    List<String> uidList = new ArrayList<>();
    List<String> emailList = new ArrayList<>();
    List<String> displayNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user_firestore_rv);

        signedInUser = findViewById(R.id.etSelectUserFirestoreRvSignedInUser);
        listOnlineUserOnly = findViewById(R.id.swSelectUserFirestoreRvListOnlineOnly);
        progressBar = findViewById(R.id.pbSelectUserFirestoreRv);

        listFirestore = findViewById(R.id.rvSelectUserFirestoreRv);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        /**
         * see comment at class definition
         */
        Intent intent = getIntent();
        String callerActivity = intent.getStringExtra("CALLER_ACTIVITY");
        // the activity was called directly so it will return to MainActivity
        if (TextUtils.isEmpty(callerActivity)) callerActivity = "MainActivity"; // default
        returnIntent = new Intent(SelectUserFirestoreRvActivity.this, MainActivity.class); // default
        // todo remove comments when activites are live
        if (TextUtils.isEmpty(callerActivity)) {
            Log.i(TAG, "The activity was called directly and will return to MainActivity");
        }
        if (callerActivity.equals("SEND_MESSAGE_FIRESTORE")) {
            Log.i(TAG, "The activity was called from SendMessageFirestore and will return to the caller activity");
            //returnIntent = new Intent(SelectUserFirestoreActivity.this, SendMessageFirestoreActivity.class);
        }
        if (callerActivity.equals("LIST_MESSAGE_FIRESTORE")) {
            Log.i(TAG, "The activity was called from ListMessagesFirestore and will return to the caller activity");
            //returnIntent = new Intent(SelectUserFirestoreActivity.this, ListMessagesFirestoreActivity.class);
        }
        if (callerActivity.equals("CHAT_MESSAGE_FIRESTORE")) {
            Log.i(TAG, "The activity was called from MainActivity and will return to  ChatMessagesFirestore activity");
            returnIntent = new Intent(SelectUserFirestoreRvActivity.this, ChatMessageFirestoreActivity.class);
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        queryList();

        listOnlineUserOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.stopListening();
                queryList();
                adapter.startListening();
            }
        });

        Button backToMain = findViewById(R.id.btnSelectUserFirestoreRvToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectUserFirestoreRvActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void queryList() {
        Log.i(TAG, "queryList");
        // Create the query and the FirestoreRecyclerOptions
        Query query;
        if (listOnlineUserOnly.isChecked()) {
            // list online user only
            CollectionReference onlineUserReference = firestoreDatabase.collection(CHILD_USERS);
            query = onlineUserReference.whereEqualTo("userOnline", true);
        } else {
            query = firestoreDatabase.collection(CHILD_USERS);
        }
        FirestoreRecyclerOptions<UserFirestoreModel> options = new FirestoreRecyclerOptions.Builder<UserFirestoreModel>()
                .setQuery(query, UserFirestoreModel.class)
                .build();

        // Create the RecyclerViewAdapter
        adapter = new FirestoreRecyclerAdapter<UserFirestoreModel, UserFirestoreHolder>(options) {
            @NonNull
            @Override
            public UserFirestoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.select_user_firestore_item, parent, false);

                return new UserFirestoreHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserFirestoreHolder holder, int position, @NonNull UserFirestoreModel model) {
                holder.nameView.setText(model.getUserName());
                holder.emailView.setText(model.getUserMail());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("*** SelectUser uid: " + model.getUserId());
                        returnIntent.putExtra("UID", model.getUserId());
                        returnIntent.putExtra("EMAIL", model.getUserMail());
                        returnIntent.putExtra("DISPLAYNAME", model.getUserName());
                        startActivity(returnIntent);
                        finish();
                    }
                });
            }
        };
        listFirestore.setAdapter(adapter);
        listFirestore.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
            adapter.startListening();
        } else {
            signedInUser.setText("no user is signed in");
            adapter.stopListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
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