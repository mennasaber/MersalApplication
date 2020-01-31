package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.example.chatapp.adapters.MessagesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    ImageButton sendButton;
    TextView messageTextView;
    ListView messagesLV;
    MessagesAdapter messagesAdapter;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseUser mUser;
    FirebaseAuth mAuth;

    String recieverNumber;
    String recieverUsername;
    String chatId;

    ArrayList<Message> messageArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recieverNumber = getIntent().getStringExtra("recieverNumber");
        recieverUsername = getIntent().getStringExtra("recieverUsername");


        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = firebaseDatabase.getReference().child("Chats");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        chatId = getChatId(Objects.requireNonNull(Objects.requireNonNull(mUser).getPhoneNumber()).substring(2), recieverNumber);

        Objects.requireNonNull(getSupportActionBar()).setTitle(recieverUsername);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        sendButton = findViewById(R.id.sendButton);
        messageTextView = findViewById(R.id.messageEditText);
        messagesLV = findViewById(R.id.messagesLV);
        messageArrayList = new ArrayList<>();
        final String[] splitNumber = mUser.getPhoneNumber().split("\\+2");


        mDatabaseReference.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Message message = d.getValue(Message.class);
                    if (!message.getSenderPhone().equals(splitNumber[1]))
                        mDatabaseReference.child(chatId).child(message.getTime()).child("seen").setValue(1);
                    messageArrayList.add(d.getValue(Message.class));
                }
                messagesAdapter = new MessagesAdapter(getApplicationContext(), R.layout.my_message, messageArrayList, recieverUsername);
                messagesLV.setAdapter(messagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messageTextView.getText().toString().trim().equals("")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String messageId = dateFormat.format(new Date()) + "";
                    Message message = new Message(messageTextView.getText().toString(), messageId
                            , splitNumber[1], recieverNumber, 0);
                    mDatabaseReference.child(chatId).child(messageId).setValue(message);
                    messageTextView.setText("");
                }
            }
        });
    }


    String getChatId(String num1, String num2) {  // get the chat id in firebase in order to put the new messages between the
        // 2 Contacts with the old ones .
        for (int i = 0; i < num1.length(); i++) {
            if (num1.charAt(i) > num2.charAt(i))
                return num1 + num2;
            else if (num1.charAt(i) < num2.charAt(i))
                return num2 + num1;
        }
        return num2 + num1;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}