package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Adapters.ContactsAdapter;
import com.example.chatapp.Models.User;
import com.example.chatapp.Models.UserGroups;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class GroupDetailsActivity extends AppCompatActivity {

    String groupId, groupName, groupImage;
    ImageView groupImageIV;
    TextView groupNameTV, membersNumber;
    ListView membersLV;
    ContactsAdapter membersAdapter;
    ArrayList<User> membersArrayList;
    DatabaseReference databaseReference;
    TextView leaveGroupTV;
    String userPhoneNumber;

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor == null) {
            return null;
        }
        String contactName = "";
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
        setContentView(R.layout.activity_group_details);

        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        groupImage = getIntent().getStringExtra("groupImage");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String[] split = firebaseUser.getPhoneNumber().split("\\+2");
        userPhoneNumber = split[1];

        membersLV = findViewById(R.id.membersDetails);
        groupImageIV = findViewById(R.id.groupImageDetails);
        groupNameTV = findViewById(R.id.groupNameDetails);
        membersNumber = findViewById(R.id.membersNumberTV);
        leaveGroupTV = findViewById(R.id.leaveGroupTV);

        if (!groupImage.equals(""))
            Picasso.with(getApplicationContext()).load(groupImage).into(groupImageIV);
        membersArrayList = new ArrayList<>();
        membersAdapter = new ContactsAdapter(GroupDetailsActivity.this, R.layout.contact_item, membersArrayList);
        membersLV.setAdapter(membersAdapter);
        groupNameTV.setText(groupName);

        databaseReference = FirebaseDatabase.getInstance().getReference("groupUsers").child(groupId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    User user = d.getValue(User.class);
                    String contactName = getContactName(GroupDetailsActivity.this, user.getPhoneNumber());
                    if (!contactName.equals(""))
                        user.setUsername(contactName);
                    membersArrayList.add(user);
                }
                membersAdapter.notifyDataSetChanged();
                String temp = membersArrayList.size() + " Members";
                membersNumber.setText(temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        leaveGroupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupDetailsActivity.this, MainActivity.class);
                startActivity(intent);
                final DatabaseReference databaseReference_ = FirebaseDatabase.getInstance().getReference("groupUsers").child(groupId);
                databaseReference_.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            if (d.getValue(User.class).getPhoneNumber().equals(userPhoneNumber)) {
                                databaseReference_.child(d.getKey()).removeValue();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                final DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("userGroups").child(userPhoneNumber).child("groupsIds");
                databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            if (d.getValue(String.class).equals(groupId)) {
                                databaseReference2.child(d.getKey()).removeValue();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Toast.makeText(GroupDetailsActivity.this, "Done", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
    protected void onStop() {
        super.onStop();
        finish();
    }
}