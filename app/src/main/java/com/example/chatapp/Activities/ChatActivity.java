package com.example.chatapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.Adapters.MessagesAdapter;
import com.example.chatapp.Models.Block;
import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {
    final static int PERMISSION_CODE = 1001;
    final static int PICK_CODE = 1000;
    private final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    ImageButton sendButton;
    EditText messageEditText;
    ListView messagesLV;
    MessagesAdapter messagesAdapter;
    DatabaseReference mDatabaseReference;
    FirebaseUser mUser;
    String receiverNumber, receiverUsername, recieverImage, chatId, hisUid;//??????????????????????????????
    ArrayList<Message> messageArrayList;
    String userPhoneNumber;
    ImageButton loadImageButton;
    StorageReference imageFolder;
    StorageReference recordsFolder;
    ImageButton recordButton;
    boolean notify = false, blocked;
    private ActionMode currentActionMode;
    private ArrayList<Message> selectedItems;
    private boolean record = false, closed;
    private MediaRecorder mediaRecorder;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        receiverNumber = getIntent().getStringExtra("receiverNumber");
        receiverUsername = getIntent().getStringExtra("receiverUsername");
        recieverImage = getIntent().getStringExtra("receiverImage");
        hisUid = getIntent().getStringExtra("hisUid");
        recordButton = findViewById(R.id.recordButton);
        loadImageButton = findViewById(R.id.loadImageButton);
        fileName = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath();
        fileName += "/audioRecordTest.3gp";
        imageFolder = FirebaseStorage.getInstance().getReference("imagesFolder");
        recordsFolder = FirebaseStorage.getInstance().getReference("recordsFolder");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        chatId = getChatId(Objects.requireNonNull(Objects.requireNonNull(mUser).getPhoneNumber()).substring(2), receiverNumber);

        selectedItems = new ArrayList<>();
        blocked = false;
        closed = false;
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
                        Intent intent = new Intent(getApplicationContext(), AllContacts.class);
                        startActivityForResult(intent, 1);
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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Disable the default and enable the custom
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            View customView = getLayoutInflater().inflate(R.layout.actionbar_title, null);
            // Get the textView of the title
            TextView customTitle = (TextView) customView.findViewById(R.id.actionbarTitle);
            customTitle.setText(receiverUsername);
            // Set the on click listener for the title
            customTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ContactProfileActivity.class);
                    intent.putExtra("recieverUserName", receiverUsername);
                    intent.putExtra("recieverNum", receiverNumber);
                    intent.putExtra("recieverPic", recieverImage);
                    intent.putExtra("recieverUid", hisUid);
                    startActivity(intent);
                }
            });
            // Apply the custom view
            actionBar.setCustomView(customView);
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle(receiverUsername);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sendButton = findViewById(R.id.sendButton);
        messageEditText = findViewById(R.id.messageEditText);
        messagesLV = findViewById(R.id.messagesLV);
        messageArrayList = new ArrayList<>();
        userPhoneNumber = mUser.getPhoneNumber().substring(2);
        messagesAdapter = new MessagesAdapter(getApplicationContext(), R.layout.my_message, messageArrayList, receiverUsername, recieverImage);
        messagesLV.setAdapter(messagesAdapter);
        mDatabaseReference.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Message message = d.getValue(Message.class);
                    if (message != null)
                        if (!message.getSenderPhone().equals(userPhoneNumber) && !closed)
                            mDatabaseReference.child(chatId).child(Objects.requireNonNull(d.getKey())).child("seeners").setValue("All");

                    message.setMessageId(d.getKey());
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
                if (!messageEditText.getText().toString().trim().equals("")) {
                    notify = true;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    sendMessage(new Message(messageEditText.getText().toString(), dateFormat.format(new Date())
                            , userPhoneNumber, receiverNumber, "0"));
                }
            }
        });

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!messageEditText.getText().toString().trim().equals("")) {
                    sendButton.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.INVISIBLE);
                    loadImageButton.setVisibility(View.INVISIBLE);
                } else {
                    recordButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.INVISIBLE);
                    loadImageButton.setVisibility(View.VISIBLE);
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
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!record) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                                    String[] permissions = {Manifest.permission.RECORD_AUDIO};
                                    requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                                } else {
                                    Vibrate();
                                    startRecord();
                                }
                            } else {
                                startRecord();
                            }

                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (record) {
                            stopRecord();
                            Vibrate();

                        }
                        break;
                }
                return false;
            }
        });
    }

    private void stopRecord() {
        if (record) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                record = false;
                saveRecordToDB();
            } catch (Exception e) {
                mediaRecorder = null;
                record = false;
            }
        }
    }

    private void startRecord() {
        if (!record) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();
            record = true;
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_CODE);
    }

    private void saveRecordToDB() {
        Uri recordData = Uri.fromFile(new File(fileName));
        final StorageReference recordName = recordsFolder.child("record" + System.currentTimeMillis());
        recordName.putFile(recordData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                recordName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String messageId = System.currentTimeMillis() + "";
                        Message message = new Message(String.valueOf(uri), dateFormat.format(new Date())
                                , userPhoneNumber, receiverNumber, userPhoneNumber);
                        mDatabaseReference.child(chatId).child(messageId).setValue(message);

                    }
                });
            }
        });
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
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecord();
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
            final StorageReference imageName = imageFolder.child("image" + Objects.requireNonNull(imageData).getLastPathSegment());
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
                                    , userPhoneNumber, receiverNumber, userPhoneNumber);
                            mDatabaseReference.child(chatId).child(messageId).setValue(message);
                        }
                    });
                }
            });
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("forward", MODE_PRIVATE);
            String thisReciever = receiverNumber;
            receiverNumber = sharedPreferences.getString("recNumber", "");
            String thisChat = chatId;
            chatId = getChatId(mUser.getPhoneNumber().substring(2), receiverNumber);
            for (int i = 0; i < selectedItems.size(); i++) {
                selectedItems.get(i).setReceiverPhone(receiverNumber);
                selectedItems.get(i).setSeeners(userPhoneNumber);
                selectedItems.get(i).setSenderPhone(userPhoneNumber);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                selectedItems.get(i).setTime(dateFormat.format(new Date()));
                sendMessage(selectedItems.get(i));
            }
            chatId = thisChat;
            receiverNumber = thisReciever;
            messagesAdapter.notifyDataSetChanged();
            selectedItems.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // checking if one user blocked another
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Blocks").child(chatId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Block.class) == null) {
                } else {
                    Block block = dataSnapshot.getValue(Block.class);
                    if (block.getBlockId().equals(chatId)) {
                        blocked = true;
                        sendButton.setEnabled(false);
                        messageEditText.setText(R.string.blocked_tv);
                        messageEditText.setEnabled(false);
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
            databaseReference.child(selectedItems.get(i).getMessageId()).setValue(selectedItems.get(i));
        }
        messagesAdapter.notifyDataSetChanged();
        selectedItems.clear();
    }

    private void DeleteMessages() {
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.get(i).getSenderPhone().equals(userPhoneNumber))
                mDatabaseReference.child(chatId).child(selectedItems.get(i).getMessageId()).removeValue();
        }
        selectedItems.clear();
    }

    private void sendMessage(final Message message) {
        String messageId = System.currentTimeMillis() + "";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        databaseReference.child(chatId).child(messageId).setValue(message);
        messageEditText.setText("");
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
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void Vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(150);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closed = true;
        finish();
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        intent.putExtra("fragmentName", "chats");
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(messagesAdapter.mediaPlayer.isPlaying()){
            messagesAdapter.mediaPlayer.stop();
            messagesAdapter.mediaPlayer.release();
            messagesAdapter.mediaPlayer = new MediaPlayer();
        }
    }
}