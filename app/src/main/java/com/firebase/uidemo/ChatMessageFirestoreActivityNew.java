package com.firebase.uidemo;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.uidemo.models.MessageModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMessageFirestoreActivityNew extends AppCompatActivity {

    static final String TAG = "ChatMessageFirestore";

    TextView chatHeader;
    com.google.android.material.textfield.TextInputEditText edtMessage;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;

    RecyclerView listFirestore;
    FirestoreRecyclerAdapter adapter;



    //RecyclerView chatList;
    //private ArrayList<MessageModel> chatArrayList;
    //private ChatFirestoreRvAdapter chatRvAdapter;
    //LinearLayoutManager linearLayoutManager;

    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();
    private static final String CHILD_MESSAGES = "messages";
    private static final String CHILD_MESSAGES_SUB = "mess";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message_firestore);

        chatHeader = findViewById(R.id.tvChatFirestoreHeader);
        edtMessageLayout = findViewById(R.id.etChatFirestoreMessageLayout);
        edtMessage = findViewById(R.id.etChatFirestoreMessage);

        listFirestore = findViewById(R.id.rvChatFirestoreRv);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        authUserId = firebaseAuth.getCurrentUser().getUid();

        // todo disable message send functionality when no user is selected

        // get the receiveUser from SelectUserFirestoreActivity
        Intent intent = getIntent();
        receiveUserId = intent.getStringExtra("UID");
        if (receiveUserId != null) {
            roomId = getRoomId(authUserId, receiveUserId);
            Log.i(TAG, "selectedUid: " + receiveUserId);
            Log.i(TAG, "we chat in roomId: " + roomId);
            //adapter.startListening();
        } else {
            // no userId was given
            Log.e(TAG, "no userId was given, abort");
            roomId = "";
            adapter.getStateRestorationPolicy();
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
        chatHeader.setText("Chat with " + receiveUserDisplayName);
        Log.i(TAG, "receiveUser: " + receiveUserString);

        queryList();

        edtMessageLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send message");
                String messageString = edtMessage.getText().toString();
                // check for selected receipient
                if (TextUtils.isEmpty(receiveUserId)) {
                    Log.i(TAG, "no receipient selected, abort");
                    Toast.makeText(getApplicationContext(),
                            "please select a receipient first",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // check that message is not empty
                if (TextUtils.isEmpty(messageString)) {
                    Log.i(TAG, "message is emptyd, abort");
                    Toast.makeText(getApplicationContext(),
                            "please enter a message",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                //showProgressBar();

                //adapter.startListening();

                // get the roomId by comparing 2 UID strings
                //String roomId = getRoomId(authUserId, receiveUserId);
                Log.i(TAG, "message: " + messageString + " send to roomId: " + roomId);
                // now we are going to send data to the database
                long actualTime = new Date().getTime();
                //public MessageModel(String message, long messageTime, String senderId, String receiverId) {
                MessageModel messageModel = new MessageModel(messageString, actualTime, authUserId, receiveUserId);
                //MessageModel messageModel = new MessageModel(authUserId, messageString, actualTime, false);

                // the message is nested in a structure like
                // messages - roomId - "messages" - random id - single message
                firestoreDatabase.collection(CHILD_MESSAGES)
                        .document(roomId)
                        .collection(CHILD_MESSAGES_SUB).add(messageModel)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.i(TAG, "DocumentSnapshot successfully written for roomId: " + roomId);
                                Toast.makeText(getApplicationContext(),
                                        "message written to database",
                                        Toast.LENGTH_SHORT).show();
                                //hideProgressBar();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "Error writing document for roomId: " + roomId, e);
                                Toast.makeText(getApplicationContext(),
                                        "ERROR on writing message to database",
                                        Toast.LENGTH_SHORT).show();
                                //hideProgressBar();
                            }
                        });

                edtMessage.setText("");
                //hideProgressBar();
            }
        });

    }

    private void queryList() {
        Log.i(TAG, "queryList");
        //adapter.startListening();
        // Create the query and the FirestoreRecyclerOptions
        CollectionReference collectionReference = firestoreDatabase
                .collection(CHILD_MESSAGES)
                .document(roomId)
                .collection(CHILD_MESSAGES_SUB);
        // sort list by message time
        Query query = collectionReference.orderBy("messageTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<MessageModel> options = new FirestoreRecyclerOptions.Builder<MessageModel>()
                .setQuery(query, MessageModel.class)
                .build();

        RelativeLayout mMessageContainer = findViewById(R.id.message_container);
        LinearLayout mMessageLayout = findViewById(R.id.messageLayout);
        int mGreen300 = ContextCompat.getColor(getApplicationContext(), R.color.material_green_300);
        int mGray300 = ContextCompat.getColor(getApplicationContext(), R.color.material_gray_300);

        // Create the RecyclerViewAdapter
        adapter = new FirestoreRecyclerAdapter<MessageModel, MessageHolder>(options) {
            @NonNull
            @Override
            public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        //.inflate(R.layout.chat_firestore_item, parent, false);
                        .inflate(R.layout.message, parent, false);

                return new MessageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull MessageModel model) {
                System.out.println("### onBindViewHolder position: " + position);
                System.out.println("### model.getMessage: " + model.getMessage());
                System.out.println("### model.getSenderId: " + model.getSenderId());
                //holder.messageView.setText(model.getMessage());
                holder.mTextField.setText(model.getMessage());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                String messageTime = dateFormat.format(model.getMessageTime());
                //holder.messageTimeView.setText(messageTime);
                holder.mNameField.setText(messageTime);

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                //setIsSender(currentUser != null && message.getUid().equals(currentUser.getUid()));
                //setIsSender(currentUser != null && model.getSenderId().equals(currentUser.getUid()));
                boolean setIsSender = currentUser != null && model.getSenderId().equals(currentUser.getUid());
                int color;
                if (setIsSender) {
                    color = mGreen300;
                    mMessageContainer.setGravity(Gravity.END);
                } else {
                    color = mGray300;
                    mMessageContainer.setGravity(Gravity.START);
                }

                ((GradientDrawable) mMessageLayout.getBackground()).setColor(color);


                /*
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
                });*/

            }

        };
        listFirestore.setAdapter(adapter);
        listFirestore.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }



    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    /**
     * service methods
     */

    // compare two strings and build a new string: if a < b: a_b if a > b: b_a, if a = b: a_b
    private String getRoomId(String a, String b) {
        int compare = a.compareTo(b);
        /*
        System.out.println("*** getRoomId compare: " + compare);
        System.out.println("*** a: " + a);
        System.out.println("*** b: " + b);
         */
        if (compare > 0) return b + "_" + a;
        else return a + "_" + b;
    }
}