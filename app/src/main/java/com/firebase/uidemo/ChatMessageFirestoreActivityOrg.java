package com.firebase.uidemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.uidemo.models.MessageModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatMessageFirestoreActivityOrg extends AppCompatActivity {

    static final String TAG = "ChatMessageFirestore";

    TextView chatHeader;
    com.google.android.material.textfield.TextInputEditText edtMessage;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    RecyclerView chatList;
    private ArrayList<MessageModel> chatArrayList;
    private ChatFirestoreRvAdapter chatRvAdapter;
    LinearLayoutManager linearLayoutManager;

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
        setContentView(R.layout.activity_chat_message_firestore_org);

        chatHeader = findViewById(R.id.tvChatFirestoreHeader);
        edtMessageLayout = findViewById(R.id.etChatFirestoreMessageLayout);
        edtMessage = findViewById(R.id.etChatFirestoreMessage);
        chatList = findViewById(R.id.rvChatFirestore);

        chatList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        chatList.setLayoutManager(linearLayoutManager);

        // creating our new array list
        chatArrayList = new ArrayList<>();

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
        } else {
            // no userId was given
            Log.e(TAG, "no userId was given, abort");
            roomId = "";
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

        // adding our array list to our recycler view adapter class.
        chatRvAdapter = new ChatFirestoreRvAdapter(chatArrayList, this);

        // setting adapter to our recycler view.
        chatList.setAdapter(chatRvAdapter);

        CollectionReference collectionReference = firestoreDatabase
                .collection(CHILD_MESSAGES)
                .document(roomId)
                .collection(CHILD_MESSAGES_SUB);
        // sort list by message time
        Query query = collectionReference.orderBy("messageTime", Query.Direction.ASCENDING);

/*
        // this is a onetime snapshot - no updates
        query
        //collectionReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // after getting the data we are calling on success method
                        // and inside this method we are checking if the received
                        // query snapshot is empty or not.
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // if the snapshot is not empty we are
                            // hiding our progress bar and adding
                            // our data in a list.
                            //loadingPB.setVisibility(View.GONE);
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                // after getting this list we are passing
                                // that list to our object class.
                                MessageModel chat = documentSnapshot.toObject(MessageModel.class);

                                // and we will pass this object class
                                // inside our arraylist which we have
                                // created for recycler view.
                                chatArrayList.add(chat);
                            }
                            // after adding the data to recycler view.
                            // we are calling recycler view notifuDataSetChanged
                            // method to notify that data has been changed in recycler view.
                            chatRvAdapter.notifyDataSetChanged();
                        } else {
                            // if the snapshot is empty we are displaying a toast message.
                            Toast.makeText(ChatMessageFirestoreActivity.this, "No data found in Database", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // if we do not get any data or any error we are displaying
                        // a toast message that we do not get any data
                        Toast.makeText(ChatMessageFirestoreActivity.this, "Fail to get the data.", Toast.LENGTH_SHORT).show();
                    }
                });
*/

        // this is a realtime listener
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (snapshot != null) {
                    //Log.d(TAG, "Current data: " + snapshot.getQuery()..getData());
                    if (!snapshot.isEmpty()) {
                        // if the snapshot is not empty we are
                        // hiding our progress bar and adding
                        // our data in a list.
                        //loadingPB.setVisibility(View.GONE);
                        List<DocumentSnapshot> list = snapshot.getDocuments();
                        for (DocumentSnapshot documentSnapshot : list) {
                            // after getting this list we are passing
                            // that list to our object class.
                            MessageModel chat = documentSnapshot.toObject(MessageModel.class);
                            // and we will pass this object class
                            // inside our arraylist which we have
                            // created for recycler view.
                            chatArrayList.add(chat);
                        }
                        // after adding the data to recycler view.
                        // we are calling recycler view notifuDataSetChanged
                        // method to notify that data has been changed in recycler view.
                        chatRvAdapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(chatRvAdapter.getItemCount() - 1);
                    } else {
                        // if the snapshot is empty we are displaying a toast message.
                        Toast.makeText(ChatMessageFirestoreActivityOrg.this, "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

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