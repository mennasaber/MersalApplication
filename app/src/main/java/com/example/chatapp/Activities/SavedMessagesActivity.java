package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatapp.Adapters.SavedMessagesAdapter;
import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class SavedMessagesActivity extends AppCompatActivity {
    ListView savedMessagesLV;
    SavedMessagesAdapter savedMessagesAdapter;
    ArrayList<Message> messageArrayList;
    String userPhoneNumber;
    DatabaseReference databaseReference;
    MediaPlayer mediaPlayer;
    Message selectedMessage;
    private ActionMode currentActionMode;

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_messages);
        savedMessagesLV = findViewById(R.id.savedMessagesLV);
        mediaPlayer = new MediaPlayer();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Saved Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        selectedMessage = null;
        final ActionMode.Callback callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                currentActionMode = mode;
                mode.setTitle("");
                mode.getMenuInflater().inflate(R.menu.action_bar_saved, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteMessageSaved:
                        mode.finish();
                        databaseReference.child(selectedMessage.getMessageId()).removeValue();
                        messageArrayList.remove(selectedMessage);
                        selectedMessage = null;
                        savedMessagesAdapter.notifyDataSetChanged();
                        Toast.makeText(SavedMessagesActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
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
        messageArrayList = new ArrayList<>();
        userPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        final String[] splitNumber = userPhoneNumber.split("\\+2");
        userPhoneNumber = splitNumber[1];
        databaseReference = FirebaseDatabase.getInstance().getReference("savedMessages").child(userPhoneNumber);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Message message = d.getValue(Message.class);
                    if (message != null) {
                        if (message.getSenderPhone().equals(userPhoneNumber))
                            message.setUsername("Me");
                        else
                            message.setUsername(getContactName(SavedMessagesActivity.this, message.getSenderPhone()));
                        messageArrayList.add(message);
                    }
                }
                savedMessagesAdapter = new SavedMessagesAdapter(SavedMessagesActivity.this, R.layout.their_message, messageArrayList,
                        getIntent().getStringExtra("mUserPic"), mediaPlayer);
                savedMessagesLV.setAdapter(savedMessagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        savedMessagesLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!view.isSelected() && selectedMessage == null) {
                    selectedMessage = savedMessagesAdapter.getItem(i);
                    startActionMode(callback);
                    view.setSelected(true);
                    view.setBackgroundResource(R.color.colorLightYellow);
                }
                return true;
            }
        });
        savedMessagesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (selectedMessage != null && selectedMessage == savedMessagesAdapter.getItem(i)) {
                    selectedMessage = null;
                    view.setSelected(false);
                    view.setBackgroundResource(R.color.colorAccent);
                    currentActionMode.finish();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    protected void onStop() {
        super.onStop();
        finish();
    }
}