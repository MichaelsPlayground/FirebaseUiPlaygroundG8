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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Date;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    TextView header;
    com.google.android.material.textfield.TextInputEditText edtMessage;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    RecyclerView messagesList;

    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    static final String TAG = "Chat";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    /**
     * Get the last 50 chat messages.
     */
    @NonNull
    //protected final Query sChatQuery =  FirebaseDatabase.getInstance().getReference().child("chats").limitToLast(50);
    protected Query sMessageQuery; //=  FirebaseDatabase.getInstance().getReference().child("chats").limitToLast(50);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_org);

        header = findViewById(R.id.tvChatHeader);
        edtMessageLayout = findViewById(R.id.etChatMessageLayout);
        edtMessage = findViewById(R.id.etChatMessage);
        messagesList = findViewById(R.id.rvChat);

        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // todo read intent data
        Intent intent = getIntent();
        receiveUserId = intent.getStringExtra("UID");
        if (receiveUserId != null) {
            Log.i(TAG, "selectedUid: " + receiveUserId);
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

        // todo: read the users details before setting the room (UID + userDisplayName)

        /*
        // todo get the real uids, remove these line
        if (authUserId.equals("QLawxZmT98g276Om5xeeMQd6fco2")) {
            receiveUserId = "wxEMT5hSLfU18HrXXYBWiPAsYgC3";
            System.out.println("*** authUserId.equals(QLawxZmT98g276Om5xeeMQd6fco2");
        } else {
            System.out.println("*** authUserId NOT equals(QLawxZmT98g276Om5xeeMQd6fco2");
            authUserId = "wxEMT5hSLfU18HrXXYBWiPAsYgC3";
            receiveUserId = "QLawxZmT98g276Om5xeeMQd6fco2";
        }

         */

        // get the roomId by comparing 2 UID strings
        roomId = getRoomId(authUserId, receiveUserId);
        String conversationString = "chat between " + authUserId + " (" + authDisplayName + ")"
                + " and " + receiveUserId + " (" + receiveUserDisplayName + ")"
                + " in room " + roomId;
        header.setText(conversationString);
        Log.i(TAG, conversationString);



        // todo change to messages/roomId
        sMessageQuery = FirebaseDatabase.getInstance().getReference()
                .child("messages")
                .child(roomId)
                .limitToLast(50);

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
        //mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference messagesDatabase = mDatabaseReference.child("messages");
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


        // id klaus zwang: hguE3YZEUfhrVTGi28wDHrOhDu83
        // id mf:          wxEMT5hSLfU18HrXXYBWiPAsYgC3
        // id q:           QLawxZmT98g276Om5xeeMQd6fco2
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
            attachRecyclerViewAdapter();
            reload();
        } else {
            //signedInUser.setText("no user is signed in");
            authUserId = "";
        }
        firebaseRecyclerAdapter.startListening();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        //sendButton.setEnabled(isSignedIn());
        //messageEdit.setEnabled(isSignedIn());
        //mBinding.sendButton.setEnabled(isSignedIn());
        //mBinding.messageEdit.setEnabled(isSignedIn());

        if (isSignedIn()) {
            attachRecyclerViewAdapter();
        } else {
            Toast.makeText(this, "signing_in", Toast.LENGTH_SHORT).show();
            //auth.signInAnonymously().addOnCompleteListener(new SignInResultNotifier(this));
        }
    }

    private void attachRecyclerViewAdapter() {
        //final RecyclerView.Adapter adapter = newAdapter();
        final RecyclerView.Adapter adapter = firebaseRecyclerAdapter;

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
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
/*
    @NonNull
    protected RecyclerView.Adapter newAdapter() {
        FirebaseRecyclerOptions<MessageModel> options =
                new FirebaseRecyclerOptions.Builder<MessageModel>()
                        .setQuery(sMessageQuery, MessageModel.class)
                        .setLifecycleOwner(this)
                        .build();

        return new FirebaseRecyclerAdapter<MessageModel, MessageHolder>(options) {
            @Override
            public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new MessageHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull MessageModel model) {
                holder.bind(model);
            }

            @Override
            public void onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
                //mBinding.emptyTextView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
                //emptyTextView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
                System.out.println("*** room is empty, send the first message ***");
            }
        };
    }
*/
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

}