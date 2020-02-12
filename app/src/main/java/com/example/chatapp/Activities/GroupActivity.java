package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.chatapp.Adapters.GroupMessagesAdapter;
import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.example.chatapp.Adapters.MessagesAdapter;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class GroupActivity extends AppCompatActivity {

    ImageButton sendButton;
    EditText messageTextView;
    ListView messagesLV;
    GroupMessagesAdapter groupMessagesAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference  databaseReference;
    FirebaseUser mUser;
    FirebaseAuth mAuth;

    String receiverNumber;
    String receiverUsername;
    ArrayList<Message> messageArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        receiverNumber = getIntent().getStringExtra("receiverNumber");
        receiverUsername = getIntent().getStringExtra("receiverUsername");


        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("GroupsMessages");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        Objects.requireNonNull(getSupportActionBar()).setTitle(receiverUsername);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sendButton = findViewById(R.id.groupSendButton);
        messageTextView = findViewById(R.id.groupMessageEditText);
        messagesLV = findViewById(R.id.groupMessagesLV);
        messageArrayList = new ArrayList<>();
        groupMessagesAdapter = new GroupMessagesAdapter(getApplicationContext(), R.layout.my_message, messageArrayList);
        messagesLV.setAdapter(groupMessagesAdapter);
        final String[] splitNumber = mUser.getPhoneNumber().split("\\+2");
        //Loading group messages
        databaseReference.child(receiverNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    databaseReference.child(receiverNumber).child(Objects.requireNonNull(d.getKey())).child("seen").setValue(1);
                    messageArrayList.add(d.getValue(Message.class));
                    groupMessagesAdapter.notifyDataSetChanged();
                }
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
                    String messageId = System.currentTimeMillis() + "";
                    Message message = new Message(messageTextView.getText().toString(), dateFormat.format(new Date())
                            , splitNumber[1], receiverNumber, 0);
                        databaseReference.child(receiverNumber).child(messageId).setValue(message);
                    messageTextView.setText("");
                }
            }
        });

    }
}


