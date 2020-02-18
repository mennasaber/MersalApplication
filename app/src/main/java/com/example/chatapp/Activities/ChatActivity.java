package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.chatapp.Models.Block;
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

public class ChatActivity extends AppCompatActivity {
    ImageButton sendButton;
    EditText messageTextView;
    ListView messagesLV;
    MessagesAdapter messagesAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseUser mUser;
    FirebaseAuth mAuth;

    String receiverNumber;
    String receiverUsername;
    String chatId;
    boolean blocked ;
    ArrayList<Message> messageArrayList;
    String userPhoneNumber;
    private ActionMode currentActionMode;
    private ArrayList<Message> selectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        receiverNumber = getIntent().getStringExtra("receiverNumber");
        receiverUsername = getIntent().getStringExtra("receiverUsername");

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = firebaseDatabase.getReference().child("Chats");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        chatId = getChatId(Objects.requireNonNull(Objects.requireNonNull(mUser).getPhoneNumber()).substring(2), receiverNumber);

        selectedItems = new ArrayList<>();
        blocked = false ;

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
                        Toast.makeText(ChatActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.saveMessage:
                        mode.finish();
                        SaveMessages();
                        Toast.makeText(ChatActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.forward:
                        mode.finish();
                        Intent intent = new Intent(getApplicationContext(), AllContacts.class) ;
                        startActivityForResult(intent,1);
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

        Objects.requireNonNull(getSupportActionBar()).setTitle(receiverUsername);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sendButton = findViewById(R.id.sendButton);
        messageTextView = findViewById(R.id.messageEditText);
        messagesLV = findViewById(R.id.messagesLV);
        messageArrayList = new ArrayList<>();
        final String[] splitNumber = mUser.getPhoneNumber().split("\\+2");
        userPhoneNumber = splitNumber[1];
        messagesAdapter = new MessagesAdapter(getApplicationContext(), R.layout.my_message, messageArrayList, receiverUsername);
        messagesLV.setAdapter(messagesAdapter);
        mDatabaseReference.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Message message = d.getValue(Message.class);
                    if (!message.getSenderPhone().equals(splitNumber[1]))
                        mDatabaseReference.child(chatId).child(Objects.requireNonNull(d.getKey())).child("seen").setValue(1);
                    message.messageId = d.getKey();
                    messageArrayList.add(message);

                }
                messagesAdapter.notifyDataSetChanged();
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
                    sendMessage(new Message(messageTextView.getText().toString(), dateFormat.format(new Date())
                            , userPhoneNumber, receiverNumber, 0));
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

    @Override
    protected void onResume() {
        super.onResume();
        // checking if one user blocked another
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Blocks").child(chatId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Block.class) == null) {}
                else {
                    Block block = dataSnapshot.getValue(Block.class);
                    if (block.getBlockId().equals(chatId)){
                        blocked=true ;
                        sendButton.setEnabled(false);
                        messageTextView.setText(R.string.blocked_tv);
                        messageTextView.setEnabled(false);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessages() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("savedMessages").child(userPhoneNumber);
        for (int i = 0; i < selectedItems.size(); i++) {
            databaseReference.child(selectedItems.get(i).messageId).setValue(selectedItems.get(i));
        }
        messagesAdapter.notifyDataSetChanged();
        selectedItems.clear();
    }

    private void DeleteMessages() {
        for (int i = 0; i < selectedItems.size(); i++) {
            mDatabaseReference.child(chatId).child(selectedItems.get(i).messageId).removeValue();
        }
        selectedItems.clear();
    }
    private  void sendMessage(Message message){
            String messageId = System.currentTimeMillis() + "";
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");
            databaseReference.child(chatId).child(messageId).setValue(message);
            messageTextView.setText("");

    }
    // get the chat id in firebase in order to put the new messages between the
    // 2 Contacts with the old ones .
    String getChatId(String num1, String num2) {
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

            case R.id.viewProfile:
                Intent intent = new Intent(getApplicationContext(), ContactProfileActivity.class);
                intent.putExtra("recieverUserName", receiverUsername);
                intent.putExtra("recieverNum", receiverNumber);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SharedPreferences sharedPreferences = getSharedPreferences("forward" ,MODE_PRIVATE) ;
        receiverNumber = sharedPreferences.getString("recNumber","");
        String thisChat = chatId ;
        chatId = getChatId(mUser.getPhoneNumber().substring(2), receiverNumber);
        for(int i = 0 ; i<selectedItems.size();i++) {
            selectedItems.get(i).setReceiverPhone(receiverNumber);
            selectedItems.get(i).setSeen(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            selectedItems.get(i).setTime(dateFormat.format(new Date()));
            sendMessage(selectedItems.get(i));
        }
        chatId = thisChat ;
    }
}