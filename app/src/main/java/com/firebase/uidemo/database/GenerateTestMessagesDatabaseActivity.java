package com.firebase.uidemo.database;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.firebase.uidemo.R;

public class GenerateTestMessagesDatabaseActivity extends AppCompatActivity {

    static final String TAG = "GenTextMessDatabase";

    TextView header;
    com.google.android.material.textfield.TextInputEditText edtMessage;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    RecyclerView messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_test_messages_database);

        header = findViewById(R.id.tvGenerateTestMessagesDatabaseHeader);
        edtMessageLayout = findViewById(R.id.etGenerateTestMessagesDatabaseMessageLayout);
        edtMessage = findViewById(R.id.etGenerateTestMessagesDatabaseMessage);
        messagesList = findViewById(R.id.rvGenerateTestMessagesDatabase);
    }
}