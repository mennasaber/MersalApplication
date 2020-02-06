package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.adapters.GroupMembersAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SelectGroupMembersActivity extends AppCompatActivity {

    public static final int REQUEST_READ_CONTACTS = 79;
    ArrayList<User> contactsHaveAccount = new ArrayList<>();
    ArrayList<User> allUsers = new ArrayList<>();
    GroupMembersAdapter groupMembersAdapter;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
    String mUserNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    final String[] splitNumber = mUserNumber.split("\\+2");
    ListView contactsMemberLV;
    public static ArrayList<User> usersSelected;
    FloatingActionButton floatingActionButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_members);
        usersSelected = new ArrayList<>();
        floatingActionButton = findViewById(R.id.floatingActionButton);
        contactsMemberLV = findViewById(R.id.contactsMembersLV);
        if (ActivityCompat.checkSelfPermission(getApplicationContext().getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestPermission();
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    allUsers.add(d.getValue(User.class));
                }
                contactsHaveAccount = getContactsHaveAccount(allUsers);
                groupMembersAdapter = new GroupMembersAdapter(getApplicationContext(), R.layout.contact_member_item, contactsHaveAccount);
                contactsMemberLV.setAdapter(groupMembersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        contactsMemberLV.setItemsCanFocus(false);
        contactsMemberLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                View v = view.findViewById(R.id.selectedImageView);
                if (!v.isShown()) {
                    v.setVisibility(View.VISIBLE);
                    usersSelected.add(contactsHaveAccount.get(i));
                } else if(v.isShown()){
                    v.setVisibility(View.INVISIBLE);
                    usersSelected.remove(contactsHaveAccount.get(i));
                }
                if (usersSelected.size() >= 1) {
                    floatingActionButton.show();
                } else
                    floatingActionButton.hide();
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersSelected.add(MainActivity.currentUser);
                Intent intent = new Intent(SelectGroupMembersActivity.this, GroupDataActivity.class);
                startActivity(intent);
            }
        });
    }

    private ArrayList<User> getContactsHaveAccount(ArrayList<User> listOfUsers) {
        ArrayList<User> users = new ArrayList<>();
        final String[] PROJECTION = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        ContentResolver cr = getApplicationContext().getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
        if (cursor != null) {
            try {
                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name, number;
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex);
                    number = cursor.getString(numberIndex);
                    if (number.contains("+2")) {
                        String[] array = number.split("\\+2");
                        number = array[1];
                    }
                    //checkUserHasAccount(number);
                    for (User u : listOfUsers)
                        if (u.getPhoneNumber().equals(number) && !u.getPhoneNumber().equals(splitNumber[1])) {
                            u.setUsername(name);
                            users.add(u);
                        }
                }
            } finally {
                cursor.close();
            }
        }
        return users;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                REQUEST_READ_CONTACTS);
    }
}