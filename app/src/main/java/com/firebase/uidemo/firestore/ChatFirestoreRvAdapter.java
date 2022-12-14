package com.firebase.uidemo.firestore;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.uidemo.R;
import com.firebase.uidemo.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ChatFirestoreRvAdapter extends RecyclerView.Adapter<ChatFirestoreRvAdapter.ViewHolder> {

    // creating variables for our ArrayList and context
    private ArrayList<MessageModel> chatArrayList;
    private Context context;

    private RelativeLayout mMessageContainer;
    private LinearLayout mMessageLayout;
    private int mGreen300;
    private int mGray300;

    // creating constructor for our adapter class
    public ChatFirestoreRvAdapter(ArrayList<MessageModel> chatArrayList, Context context) {
        this.chatArrayList = chatArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // passing our layout file for displaying our card item
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.firestore_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // setting data to our text views from our modal class.
        MessageModel messageModel = chatArrayList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String messageTime  = dateFormat.format(messageModel.getMessageTime());
        holder.messageTextTV.setText(messageModel.getMessage());
        holder.messageTimeTV.setText(messageTime);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        setIsSender(currentUser != null && messageModel.getSenderId().equals(currentUser.getUid()));
    }

    private void setIsSender(boolean isSender) {
        final int color;
        if (isSender) {
            color = mGreen300;
            mMessageContainer.setGravity(Gravity.END);
        } else {
            color = mGray300;
            mMessageContainer.setGravity(Gravity.START);
        }
        ((GradientDrawable) mMessageLayout.getBackground()).setColor(color);
    }

    @Override
    public int getItemCount() {
        // returning the size of our array list.
        return chatArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our text views.
        private final TextView messageTextTV;
        //private final TextView messageSenderIdTV;
        private final TextView messageTimeTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views.
            messageTextTV = itemView.findViewById(R.id.firestore_chat_message_text);
            messageTimeTV = itemView.findViewById(R.id.firestore_chat_time);
            mMessageContainer = itemView.findViewById(R.id.message_container);
            mMessageLayout = itemView.findViewById(R.id.firestore_chat_message_layout);
            mGreen300 = ContextCompat.getColor(itemView.getContext(), R.color.material_green_300);
            mGray300 = ContextCompat.getColor(itemView.getContext(), R.color.material_gray_300);
        }
    }
}
