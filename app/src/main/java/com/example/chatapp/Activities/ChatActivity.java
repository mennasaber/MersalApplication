package com.example.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    ImageButton sendButton ;
    TextView messageTextview ;
    ListView messagesLV ;
    ListAdapter listAdapter ;

    FirebaseDatabase firebaseDatabase ;
    DatabaseReference mDatabaseReference ;
    FirebaseUser mUser ;
    FirebaseAuth mAuth ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = firebaseDatabase.getReference().child("Chats");
        mAuth = FirebaseAuth.getInstance() ;
        mUser = mAuth.getCurrentUser() ;

        sendButton=findViewById(R.id.sendButton);
        messageTextview = findViewById(R.id.messageTextview) ;
        messagesLV = findViewById(R.id.messagesLV);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageTextview.getText().equals(""))
                {}
                else {
                    String chatId =getChatId(mUser.getPhoneNumber().substring(2) ,getIntent().getStringExtra("recieverNumber") );
                    String messageId=System.currentTimeMillis()+"" ;
                    Message message = new Message(messageTextview.getText().toString() , messageId
                            ,mUser.getPhoneNumber(),getIntent().getStringExtra("recieverNumber")) ;
                    mDatabaseReference.child(chatId).child(messageId).setValue(message);
                    messageTextview.setText("");
                }
            }
        });
    }


    String getChatId (String num1 , String num2) {  // get the chat id in firebase in order to put the new messages between the
                                                    // 2 Contacts with the old ones .
        for (int i = 0 ; i< num1.length() ; i++) {
            if (num1.charAt(i) > num2.charAt(i))
                return num1 + num2;
            else if (num1.charAt(i) < num2.charAt(i))
                return num2 + num1;
        }
        return  num2 + num1;
    }
}