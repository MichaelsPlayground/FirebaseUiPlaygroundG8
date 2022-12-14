package com.firebase.uidemo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageStorageHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    TextView imageTextView;

    public ImageStorageHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.ivListImage);
        imageTextView = itemView.findViewById(R.id.tvImageName);
    }
}
