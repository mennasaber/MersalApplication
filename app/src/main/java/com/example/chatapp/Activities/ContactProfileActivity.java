package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Adapters.MessagesAdapter;
import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactProfileActivity extends AppCompatActivity {

    TextView userPhone , userName,confirmationMessage ;
    Button blockButton ,confirm  ;
    AlertDialog.Builder alertBuilder ;
    AlertDialog alertDialog ;
    FirebaseAuth firebaseAuth ;
    FirebaseUser mUser ;
    List<String> blocks = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth= FirebaseAuth.getInstance();
        mUser = firebaseAuth.getCurrentUser();

        setContentView(R.layout.activity_contact_profile);
        userName = findViewById(R.id.usernameTextView2);
        userPhone = findViewById(R.id.phoneNumberTextView2);
        blockButton = findViewById(R.id.blockButton);

        userPhone.setText(getIntent().getStringExtra("recieverNum"));
        userName.setText(getIntent().getStringExtra("recieverUserName"));
        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopupDialogue();
            }
        });
        String blockId = getChatId(userPhone.getText().toString() , mUser.getPhoneNumber().substring(2));
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child("Blocks") ;

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    blocks.add(d.getValue(String.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createPopupDialogue(){
        alertBuilder = new AlertDialog.Builder(this) ;
        View view = getLayoutInflater().inflate(R.layout.block_confirmation_message,null);
        confirm = view.findViewById(R.id.confirm) ;
        confirmationMessage = view.findViewById(R.id.confirmationMessage) ;
        confirmationMessage.setText(getString(R.string.Confirmationmessage)+userName.getText().toString());
        alertBuilder.setView(view);
        alertDialog = alertBuilder.create();
        alertDialog.show();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blockButton.getText().toString().equals("Block")) {
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference().child("Blocks");
                    databaseReference.child(getChatId(userPhone.getText().toString() , mUser.getPhoneNumber().substring(2)))
                            .setValue(getChatId(userPhone.getText().toString(), mUser.getPhoneNumber().substring(2)));
                blockButton.setText(R.string.unblock);
                }
                else {
                    // TODO if user is blocked already
                    blockButton.setText(R.string.block);
                }
                    alertDialog.hide();

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

}