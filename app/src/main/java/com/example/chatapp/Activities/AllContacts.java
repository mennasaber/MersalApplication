package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatapp.Adapters.ContactsAdapter;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AllContacts extends AppCompatActivity {
    public static final int REQUEST_READ_CONTACTS = 79;
    ContactsAdapter contactsAdapter;
    ArrayList<User> contactsHaveAccount = new ArrayList<>();
    ListView contactsListView;
    ArrayList<User> allUsers = new ArrayList<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
    String mUserNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    final String[] splitNumber = mUserNumber.split("\\+2");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);
        contactsListView = findViewById(R.id.contactsListView);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
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
                contactsListView = findViewById(R.id.contactsListView);
                contactsAdapter = new ContactsAdapter(getApplicationContext(), R.layout.contact_item, contactsHaveAccount);
                contactsListView.setAdapter(contactsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedPreferences = getSharedPreferences("forward", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("recNumber", contactsAdapter.getItem(position).getPhoneNumber());
                editor.putString("recUsername", contactsHaveAccount.get(position).getUsername());
                editor.putString("recImage", contactsAdapter.getItem(position).getImage());
                editor.apply();
                finish();
            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(this), new String[]{android.Manifest.permission.READ_CONTACTS},
                REQUEST_READ_CONTACTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            contactsHaveAccount = new ArrayList<>();
            // permission denied,Disable the
            // functionality that depends on this permission.
        }
    }

    private ArrayList<User> getContactsHaveAccount(ArrayList<User> listOfUsers) {
        ArrayList<User> users = new ArrayList<>();
        final String[] PROJECTION = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        ContentResolver cr = Objects.requireNonNull(getApplicationContext()).getContentResolver();
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
}