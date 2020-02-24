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
import android.view.MenuItem;
import android.widget.ListView;

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
    MediaPlayer mediaPlayer ;
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
        mediaPlayer = new MediaPlayer() ;
        Objects.requireNonNull(getSupportActionBar()).setTitle("Saved Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                        getIntent().getStringExtra("mUserPic"),mediaPlayer);
                savedMessagesLV.setAdapter(savedMessagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
}