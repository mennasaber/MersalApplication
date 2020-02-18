package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Adapters.MessagesAdapter;
import com.example.chatapp.Models.Block;
import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
    String blockId ;
    AlertDialog.Builder alertBuilder ;
    AlertDialog alertDialog ;
    FirebaseAuth firebaseAuth ;
    FirebaseUser mUser ;
    FirebaseDatabase mDatabase ;
    DatabaseReference mDatabaseReference ;
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
         blockId= getChatId(userPhone.getText().toString() , mUser.getPhoneNumber().substring(2));
        mDatabase = FirebaseDatabase.getInstance() ;
        mDatabaseReference = mDatabase.getReference().child("Blocks");
        mDatabaseReference.keepSynced(true);

        // checking if one user blocked another
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Blocks").child(blockId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Block.class) == null) {}
                else {
                    Block block = dataSnapshot.getValue(Block.class);
                    if (block.getBlockId().equals(blockId)){
                        blockButton.setText(R.string.unblock);
                    }
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
        if (blockButton.getText().toString().equals("Unblock")){
            confirmationMessage.setText("By confirming this you will be able to text "+userName.getText().toString());
        }
        else {
            confirmationMessage.setText( "By confirming this you won't be able to text" + userName.getText().toString());
        }
        alertBuilder.setView(view);
        alertDialog = alertBuilder.create();
        alertDialog.show();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blockButton.getText().toString().equals("Block")) {
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference().child("Blocks").child(blockId);
                    databaseReference.child("blockId").setValue(blockId);
                blockButton.setText(R.string.unblock);
                }
                else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Blocks").child(blockId);
                    databaseReference.removeValue() ;
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