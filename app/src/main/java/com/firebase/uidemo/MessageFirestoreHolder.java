package com.firebase.uidemo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageFirestoreHolder extends RecyclerView.ViewHolder {

    TextView messageView;
    TextView messageTimeView;

    public MessageFirestoreHolder(@NonNull View itemView) {
        super(itemView);
        messageView = itemView.findViewById(R.id.tvChatFirebaseItemMessage);
        messageTimeView = itemView.findViewById(R.id.tvChatFirebaseItemMessageTime);
    }
}
