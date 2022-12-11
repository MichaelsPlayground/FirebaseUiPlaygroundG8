package com.firebase.uidemo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserFirestoreHolder extends RecyclerView.ViewHolder {

    TextView nameView;
    TextView emailView;

    public UserFirestoreHolder(@NonNull View itemView) {
        super(itemView);
        nameView = itemView.findViewById(R.id.tvSelectUserFirebaseItemName);
        emailView = itemView.findViewById(R.id.tvSelectUserFirebaseItemEmail);
    }
}
