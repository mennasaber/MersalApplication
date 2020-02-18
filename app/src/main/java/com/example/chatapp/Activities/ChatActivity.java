package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.example.chatapp.Adapters.MessagesAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    final static int PERMISSION_CODE = 1001;
    final static int PICK_CODE = 1000;
    ImageButton sendButton;
    EditText messageTextView;
    ListView messagesLV;
    MessagesAdapter messagesAdapter;
    List<String> blocks = new ArrayList<>();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseUser mUser;
    FirebaseAuth mAuth;
    String receiverNumber;
    String receiverUsername;
    String chatId;
    ArrayList<Message> messageArrayList;
    String userPhoneNumber;
    ImageButton loadImageButton;
    StorageReference Folder;
    private ActionMode currentActionMode;
    private ArrayList<Message> selectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        receiverNumber = getIntent().getStringExtra("receiverNumber");
        receiverUsername = getIntent().getStringExtra("receiverUsername");

        loadImageButton = findViewById(R.id.loadImageButton);

        Folder = FirebaseStorage.getInstance().getReference("imagesFolder");
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = firebaseDatabase.getReference().child("Chats");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        chatId = getChatId(Objects.requireNonNull(Objects.requireNonNull(mUser).getPhoneNumber()).substring(2), receiverNumber);

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
                        Toast.makeText(ChatActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.saveMessage:
                        mode.finish();
                        SaveMessages();
                        Toast.makeText(ChatActivity.this, "Saved", Toast.LENGTH_SHORT).show();
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
                    String messageId = System.currentTimeMillis() + "";
                    Message message = new Message(messageTextView.getText().toString(), dateFormat.format(new Date())
                            , splitNumber[1], receiverNumber, 0);
                    mDatabaseReference.child(chatId).child(messageId).setValue(message);
                    messageTextView.setText("");
                }
            }
        });

        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    } else {
                        pickImageFromGallery();
                    }
                } else {
                    pickImageFromGallery();
                }
            }
        });
        //TODO if chatid match a block id
        //messageTextView.setText("You can't reply to this conversation");
        //messageTextView.setEnabled(false);
        //sendButton.setEnabled(false);

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

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CODE && resultCode == RESULT_OK) {
            Uri imageData = Objects.requireNonNull(data).getData();
            final StorageReference imageName = Folder.child("image" + Objects.requireNonNull(imageData).getLastPathSegment());
            imageName.putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ChatActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            String messageId = System.currentTimeMillis() + "";
                            Message message = new Message(String.valueOf(uri), dateFormat.format(new Date())
                                    , userPhoneNumber, receiverNumber, 0);
                            mDatabaseReference.child(chatId).child(messageId).setValue(message);
                        }
                    });
                }
            });
        }
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


}