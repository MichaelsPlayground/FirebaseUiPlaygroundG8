package com.firebase.uidemo;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageFirestoreHolder extends RecyclerView.ViewHolder {

    public TextView messageView;
    public TextView messageTimeView;
    public LinearLayout mMessageLayout;
    public RelativeLayout mMessageContainer;

    public MessageFirestoreHolder(@NonNull View itemView) {
        super(itemView);
        messageView = itemView.findViewById(R.id.firestore_chat_message_text);
        messageTimeView = itemView.findViewById(R.id.firestore_chat_time);

        mMessageLayout = itemView.findViewById(R.id.firestore_chat_message_layout);
        mMessageContainer = itemView.findViewById(R.id.firestore_chat_message_container);
    }
}
