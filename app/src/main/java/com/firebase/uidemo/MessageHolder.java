package com.firebase.uidemo;

import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.uidemo.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;

public class MessageHolder extends RecyclerView.ViewHolder{

    TextView mNameField;
    TextView mTextField;
    //private final FrameLayout mLeftArrow;
    //private final FrameLayout mRightArrow;
    private final RelativeLayout mMessageContainer;
    private final LinearLayout mMessage;
    private final int mGreen300;
    private final int mGray300;

    public MessageHolder(@NonNull View itemView) {
        super(itemView);
        mNameField = itemView.findViewById(R.id.name_text); // will be shown
        mTextField = itemView.findViewById(R.id.message_text); // will be shown
        //mLeftArrow = itemView.findViewById(R.id.left_arrow);
        //mRightArrow = itemView.findViewById(R.id.right_arrow);
        mMessageContainer = itemView.findViewById(R.id.message_container);
        mMessage = itemView.findViewById(R.id.message);
        mGreen300 = ContextCompat.getColor(itemView.getContext(), R.color.material_green_300);
        mGray300 = ContextCompat.getColor(itemView.getContext(), R.color.material_gray_300);
    }

    /*
    public void bind(@NonNull AbstractMessage message) {
        setName(message.getName());
        setMessage(message.getMessage());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //setIsSender(currentUser != null && message.getUid().equals(currentUser.getUid()));
        setIsSender(currentUser != null && message.getSenderId().equals(currentUser.getUid()));
    }
    */
    public void bind(@NonNull MessageModel message) {
        //setName(message.getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String messageTime = dateFormat.format(message.getMessageTime());
        setName(messageTime);
        setMessage(message.getMessage());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //setIsSender(currentUser != null && message.getUid().equals(currentUser.getUid()));
        setIsSender(currentUser != null && message.getSenderId().equals(currentUser.getUid()));
    }


    private void setName(@Nullable String name) {
        mNameField.setText(name);
    }

    private void setMessage(@Nullable String text) {
        mTextField.setText(text);
    }

    private void setIsSender(boolean isSender) {
        final int color;
        if (isSender) {
            color = mGreen300;
            //mLeftArrow.setVisibility(View.GONE);
            //mRightArrow.setVisibility(View.VISIBLE);
            mMessageContainer.setGravity(Gravity.END);
        } else {
            color = mGray300;
            //mLeftArrow.setVisibility(View.VISIBLE);
            //mRightArrow.setVisibility(View.GONE);
            mMessageContainer.setGravity(Gravity.START);
        }

        ((GradientDrawable) mMessage.getBackground()).setColor(color);
        //((RotateDrawable) mLeftArrow.getBackground()).getDrawable().setColorFilter(color, PorterDuff.Mode.SRC);
        //((RotateDrawable) mRightArrow.getBackground()).getDrawable().setColorFilter(color, PorterDuff.Mode.SRC);
    }
}
