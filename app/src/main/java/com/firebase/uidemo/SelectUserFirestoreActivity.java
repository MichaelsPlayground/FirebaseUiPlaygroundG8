package com.firebase.uidemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.uidemo.models.UserFirestoreModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectUserFirestoreActivity extends AppCompatActivity {

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

    static final String TAG = "SelectUserFirestore";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private ListView userListView;

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
        setContentView(R.layout.activity_select_user_firestore);

        signedInUser = findViewById(R.id.etFirestoreUserSignedInUser);
        listOnlineUserOnly = findViewById(R.id.swFirestoreSelectUserListOnlineOnly);
        progressBar = findViewById(R.id.pbFirestoreUser);

        userListView = findViewById(R.id.lvSelectUserFirestore);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        /**
         * see comment at class definition
         */
        Intent intent = getIntent();
        String callerActivity = intent.getStringExtra("CALLER_ACTIVITY");
        // the activity was called directly so it will return to MainActivity
        if (TextUtils.isEmpty(callerActivity)) callerActivity = "MainActivity"; // default
        returnIntent = new Intent(SelectUserFirestoreActivity.this, MainActivity.class); // default
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
            //returnIntent = new Intent(SelectUserFirestoreActivity.this, ChatMessageFirestoreActivity.class);
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String uidSelected = uidList.get(position);
                String emailSelected = emailList.get(position);
                String displayNameSelected = displayNameList.get(position);
                returnIntent.putExtra("UID", uidSelected);
                returnIntent.putExtra("EMAIL", emailSelected);
                returnIntent.putExtra("DISPLAYNAME", displayNameSelected);
                startActivity(returnIntent);
                finish();
            }
        });

        listOnlineUserOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listUserOnFirestore();
            }
        });

        listUserOnFirestore();

        Button backToMain = findViewById(R.id.btnFirestoreUserToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectUserFirestoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void listUserOnFirestore() {
        Log.i(TAG, "list user on firestore database run");
        showProgressBar();

        arrayList = new ArrayList<>();
        uidList = new ArrayList<>();
        emailList = new ArrayList<>();
        displayNameList = new ArrayList<>();

        if (listOnlineUserOnly.isChecked()) {
            // list online user only
            CollectionReference onlineUserReference = firestoreDatabase.collection(CHILD_USERS);
            Query query = onlineUserReference.whereEqualTo("userOnline", true);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.i(TAG, "listUser on firestore complete");
                    if (task.isSuccessful()) {
                        Log.i(TAG, "listUser on firestore complete and successful");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserFirestoreModel userFirestoreModel = document.toObject(UserFirestoreModel.class);
                            final String displayName;
                            if (TextUtils.isEmpty(userFirestoreModel.getUserName())) {
                                displayName = "";
                            } else {
                                displayName = userFirestoreModel.getUserName();
                            }
                            arrayList.add(userFirestoreModel.getUserMail() + " " + displayName);
                            uidList.add(document.getId());
                            emailList.add(userFirestoreModel.getUserMail());
                            displayNameList.add(displayName);
                        }
                        ListView usersListView = (ListView) findViewById(R.id.lvSelectUserFirestore);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SelectUserFirestoreActivity.this, android.R.layout.simple_list_item_1, arrayList);
                        usersListView.setAdapter(arrayAdapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        } else {
            // list all user regardles online status
            firestoreDatabase.collection(CHILD_USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.i(TAG, "select user on firestore complete");
                    if (task.isSuccessful()) {
                        Log.i(TAG, "listUser on firestore complete and successful");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserFirestoreModel userFirestoreModel = document.toObject(UserFirestoreModel.class);
                            final String displayName;
                            if (TextUtils.isEmpty(userFirestoreModel.getUserName())) {
                                displayName = "";
                            } else {
                                displayName = userFirestoreModel.getUserName();
                            }
                            arrayList.add(userFirestoreModel.getUserMail() + " " + displayName);
                            uidList.add(document.getId());
                            emailList.add(userFirestoreModel.getUserMail());
                            displayNameList.add(displayName);
                        }
                        ListView usersListView = (ListView) findViewById(R.id.lvSelectUserFirestore);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SelectUserFirestoreActivity.this, android.R.layout.simple_list_item_1, arrayList);
                        usersListView.setAdapter(arrayAdapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }
        hideProgressBar();
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