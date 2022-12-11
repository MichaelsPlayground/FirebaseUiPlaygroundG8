package com.firebase.uidemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.uidemo.models.MessageModel;
import com.firebase.uidemo.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Date;
import java.util.Objects;

public class ChatDatabaseActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    TextView header;
    com.google.android.material.textfield.TextInputEditText edtMessage;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    RecyclerView messagesList;

    private static String authUserId = "", authUserEmail = "", authDisplayName = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    static final String TAG = "ChatDatabase";

    private DatabaseReference mDatabaseReference;
    private DatabaseReference messagesDatabase;
    private FirebaseAuth mFirebaseAuth;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @NonNull
    protected Query sMessageQuery; //=  FirebaseDatabase.getInstance().getReference().child("chats").limitToLast(50);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_database);

        header = findViewById(R.id.tvChatDatabaseHeader);
        edtMessageLayout = findViewById(R.id.etChatDatabaseMessageLayout);
        edtMessage = findViewById(R.id.etChatDatabaseMessage);
        messagesList = findViewById(R.id.rvChatDatabase);

        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(new LinearLayoutManager(this));

        // start with a disabled ui
        enableUiOnSignIn(false);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        loadSignedInUserData(mFirebaseAuth.getCurrentUser().getUid());

        // read data received from ListUserOnDatabase
        Intent intent = getIntent();
        receiveUserId = intent.getStringExtra("UID");
        if (receiveUserId != null) {
            Log.i(TAG, "selectedUid: " + receiveUserId);
        } else {
            receiveUserId = "";
        }
        receiveUserEmail = intent.getStringExtra("EMAIL");
        if (receiveUserEmail != null) {
            Log.i(TAG, "selectedEmail: " + receiveUserEmail);
        }
        receiveUserDisplayName = intent.getStringExtra("DISPLAYNAME");
        if (receiveUserDisplayName != null) {
            Log.i(TAG, "selectedDisplayName: " + receiveUserDisplayName);
        }
        String receiveUserString = "Email: " + receiveUserEmail;
        receiveUserString += "\nUID: " + receiveUserId;
        receiveUserString += "\nDisplay Name: " + receiveUserDisplayName;
        //receiveUser.setText(receiveUserString);
        Log.i(TAG, "receiveUser: " + receiveUserString);
        // get own data
        authUserEmail = intent.getStringExtra("AUTH_EMAIL");
        authDisplayName = intent.getStringExtra("AUTH_DISPLAYNAME");

        // Initialize Firebase Auth
        // mFirebaseAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        //mDatabase = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabase = FirebaseDatabase.getInstance().getReference();
        // see loadSignedInUserData as we use a new instance there

        // Create a instance of the database and get its reference
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        messagesDatabase = mDatabaseReference.child("messages");
        messagesDatabase.keepSynced(true);

        edtMessageLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showProgressBar();
                Log.i(TAG, "clickOnIconEnd");
                String messageString = edtMessage.getText().toString();
                Log.i(TAG, "message: " + messageString);
                // now we are going to send data to the database
                long actualTime = new Date().getTime();
                // retrieve the time string in GMT
                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //String millisInString  = dateFormat.format(new Date());
                MessageModel messageModel = new MessageModel(messageString, actualTime, authUserId, receiveUserId);
                messagesDatabase.child(roomId).push().setValue(messageModel);
                // without push there is no new chatId key
                // mDatabaseReference.child("messages").child(roomId).setValue(messageModel);
                Toast.makeText(getApplicationContext(),
                        "message written to database: " + messageString,
                        Toast.LENGTH_SHORT).show();
                edtMessage.setText("");
            }
        });

    }

    /**
     * service methods
     */

    // compare two strings and build a new string: if a < b: ab if a > b: ba, if a = b: ab
    private String getRoomId(String a, String b) {
        int compare = a.compareTo(b);
        if (compare > 0) return b + a;
        else return a + b;
    }

    /**
     * basic
     */

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if(currentUser != null){
            if (!receiveUserId.equals("")) {
                Log.i(TAG, "onStart prepare database for chat");
                reload();
                enableUiOnSignIn(true);
                setDatabaseForRoom(currentUser.getUid(), receiveUserId);
                firebaseRecyclerAdapter.startListening();
                attachRecyclerViewAdapter();
            } else {
                header.setText("you need to select a receiveUser first");
                Log.i(TAG, "you need to select a receiveUser first");
            }
        } else {
            //signedInUser.setText("no user is signed in");
            authUserId = "";
            enableUiOnSignIn(false);
            firebaseRecyclerAdapter.stopListening();
        }
        // startListening begins when a user is logged in
        //firebaseRecyclerAdapter.startListening();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        if (isSignedIn()) {
            //getAuthUserCredentials();
        } else {
            Toast.makeText(this, "you need to sign in before chatting", Toast.LENGTH_SHORT).show();
            //auth.signInAnonymously().addOnCompleteListener(new SignInResultNotifier(this));
        }
    }

    private void attachRecyclerViewAdapter() {
        final RecyclerView.Adapter adapter = firebaseRecyclerAdapter;
        if (adapter != null) {
            Log.i(TAG, "attachRecyclerViewAdapter");
            // Scroll to bottom on new messages
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    //mBinding.messagesList.smoothScrollToPosition(adapter.getItemCount());
                    messagesList.smoothScrollToPosition(adapter.getItemCount());
                }
            });

            //mBinding.messagesList.setAdapter(adapter);
            messagesList.setAdapter(adapter);
        } else {
            Log.i(TAG, "attachRecyclerViewAdapter NOT set, firebaseRecyclerAdapter is null");
        }
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void loadSignedInUserData(String mAuthUserId) {
        if (!mAuthUserId.equals("")) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(mAuthUserId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    //hideProgressBar();
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting data", task.getException());
                    } else {
                        // check for a null value means no user data were saved before
                        UserModel userModel = task.getResult().getValue(UserModel.class);
                        Log.i(TAG, String.valueOf(userModel));
                        if (userModel == null) {
                            Log.i(TAG, "userModel is null, show message");
                        } else {
                            Log.i(TAG, "userModel email: " + userModel.getUserMail());
                            authUserId = mAuthUserId;
                            authUserEmail = userModel.getUserMail();
                            authDisplayName = userModel.getUserName();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),
                    "sign in a user before loading",
                    Toast.LENGTH_SHORT).show();
            //hideProgressBar();
        }
    }

    // generates the room id and prepares for the querry
    private void setDatabaseForRoom(String ownUid, String receiverUid) {
        // get the roomId by comparing 2 UID strings
        roomId = getRoomId(ownUid, receiverUid);
        /*
        String conversationString = "chat between " + ownUid + " (" + authDisplayName + ")"
                + " and " + receiveUserId + " (" + receiveUserDisplayName + ")"
                + " in room " + roomId;
         */
        String  conversationString = "chat with " + receiveUserDisplayName;
        header.setText(conversationString);
        Log.i(TAG, conversationString);

        // get the last 50 messages from database
        // On the main screen of your app, you may want to show the 50 most recent chat messages.
        // With Firebase you would use the following query:
        Query query = messagesDatabase
                .child(roomId);
        //.limitToLast(10); // show the last 10 messages
        //.limitToLast(50); // show the last 50 messages
        // The FirebaseRecyclerAdapter binds a Query to a RecyclerView. When data is added, removed,
        // or changed these updates are automatically applied to your UI in real time.
        // First, configure the adapter by building FirebaseRecyclerOptions. In this case we will
        // continue with our chat example:
        FirebaseRecyclerOptions<MessageModel> options =
                new FirebaseRecyclerOptions.Builder<MessageModel>()
                        .setQuery(query, MessageModel.class)
                        .build();
        // Next create the FirebaseRecyclerAdapter object. You should already have a ViewHolder subclass
        // for displaying each item. In this case we will use a custom ChatHolder class:
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MessageModel, MessageHolder>(options) {
            @Override
            public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message, parent, false);
                return new MessageHolder(view);
            }

            @Override
            protected void onBindViewHolder(MessageHolder holder, int position, MessageModel model) {
                // Bind the Chat object to the ChatHolder
                holder.bind(model);
            }
        };
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
        //hideProgressBar();
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
            //signedInUser.setText(userData);
            Log.i(TAG, "authUser: " + userData);
        } else {
            //signedInUser.setText(null);
            authUserId = "";
        }
    }

    private void enableUiOnSignIn(boolean userIsSignedIn) {
        if (!userIsSignedIn) {
            header.setText("you need to be signed in before starting a chat");
            edtMessageLayout.setEnabled(userIsSignedIn);
        } else {
            edtMessageLayout.setEnabled(userIsSignedIn);
        }
    }

}