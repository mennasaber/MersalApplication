package com.example.chatapp.Activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.adapters.ContactsAdapter;
import com.example.chatapp.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class ContactsFragment extends Fragment {
    ArrayList<User> contactsHaveAccount = new ArrayList<>();
    public static final int REQUEST_READ_CONTACTS = 79;
    ContactsAdapter contactsAdapter;
    ListView contactsListView;
    ArrayList<User> allUsers = new ArrayList<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestPermission();
        }
        // take aLot of time
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    allUsers.add(d.getValue(User.class));
                }
                contactsHaveAccount = getContactsHaveAccount(allUsers);
                contactsListView = view.findViewById(R.id.contactsListView);
                contactsAdapter = new ContactsAdapter(Objects.requireNonNull(getActivity()).getApplicationContext(), R.layout.contact_item, contactsHaveAccount);
                contactsListView.setAdapter(contactsAdapter);

                contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(),ChatActivity.class);
                        intent.putExtra("recieverNumber" , contactsAdapter.getItem(position).getPhoneNumber());
                        intent.putExtra("recieverUsername" , contactsAdapter.getItem(position).getUsername());
                        intent.putExtra("recieverImage" , contactsAdapter.getItem(position).getImage());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return view;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{android.Manifest.permission.READ_CONTACTS},
                REQUEST_READ_CONTACTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            try {
//                users = getAllContacts();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
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
        ContentResolver cr = Objects.requireNonNull(getContext()).getContentResolver();
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
                        if (u.getPhoneNumber().equals(number)) {
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


    private void checkUserHasAccount(String number) {

        databaseReference.child(number).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    contactsHaveAccount.add(dataSnapshot.getValue(User.class));
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

}