package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

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
    DatabaseReference databaseReference;
    FirebaseUser mUser;
    FirebaseAuth mAuth;

    String receiverNumber;
    String receiverUsername;
    ArrayList<Message> messageArrayList;
    String userPhoneNumber;
    private ActionMode currentActionMode;
    private ArrayList<Message> selectedItems;

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

        selectedItems = new ArrayList<>();


        final ActionMode.Callback callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                currentActionMode = mode;
                mode.setTitle("");
                mode.getMenuInflater().inflate(R.menu.action_bar, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteMessage:
                        mode.finish();
                        DeleteMessages();
                        Toast.makeText(GroupActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.saveMessage:
                        mode.finish();
                        SaveMessages();
                        Toast.makeText(GroupActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                currentActionMode = null;
            }
        };


        sendButton = findViewById(R.id.groupSendButton);
        messageTextView = findViewById(R.id.groupMessageEditText);
        messagesLV = findViewById(R.id.groupMessagesLV);
        messageArrayList = new ArrayList<>();
        groupMessagesAdapter = new GroupMessagesAdapter(getApplicationContext(), R.layout.my_message, messageArrayList);
        messagesLV.setAdapter(groupMessagesAdapter);
        final String[] splitNumber = mUser.getPhoneNumber().split("\\+2");
        userPhoneNumber = splitNumber[1];
        //Loading group messages
        databaseReference.child(receiverNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Message message = d.getValue(Message.class);
                    if (message != null) {
                        message.messageId = d.getKey();
                        databaseReference.child(receiverNumber).child(Objects.requireNonNull(d.getKey())).child("seen").setValue(1);
                        messageArrayList.add(message);
                    }
                }
                groupMessagesAdapter.notifyDataSetChanged();
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
        messagesLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!view.isSelected() && selectedItems.size() == 0) {
                    selectedItems.add(messageArrayList.get(i));
                    startActionMode(callback);
                    view.setSelected(true);
                    view.setBackgroundResource(R.color.colorLightYellow);
                }
                return true;
            }
        });
        messagesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (selectedItems.contains(messageArrayList.get(i))) {
                    selectedItems.remove(messageArrayList.get(i));
                    view.setSelected(false);
                    view.setBackgroundResource(R.color.colorAccent);
                    if (selectedItems.size() == 0)
                        currentActionMode.finish();
                } else if (currentActionMode != null) {
                    selectedItems.add(messageArrayList.get(i));
                    view.setSelected(true);
                    view.setBackgroundResource(R.color.colorLightYellow);
                }
            }
        });
    }


    private void SaveMessages() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("savedMessages").child(userPhoneNumber);
        for (int i = 0; i < selectedItems.size(); i++) {
            databaseReference.child(selectedItems.get(i).messageId).setValue(selectedItems.get(i));
        }
        groupMessagesAdapter.notifyDataSetChanged();
        selectedItems.clear();
    }

    private void DeleteMessages() {
        for (int i = 0; i < selectedItems.size(); i++) {
            databaseReference.child(receiverNumber).child(selectedItems.get(i).messageId).removeValue();
        }
        selectedItems.clear();
    }
}


