package com.example.chatapp.Fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.Models.Chat;
import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.Adapters.ChatsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class ChatsFragment extends Fragment {

    public static final int REQUEST_READ_CONTACTS = 79;

    FirebaseUser mUser;
    ArrayList<Chat> chats;
    ChatsAdapter chatsAdapter;
    ListView chatsListView;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
    Message lastMessage;
    boolean permission = false;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_chats, container, false);

        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            permission = true;
        } else {
            requestPermission();
        }
        chats = new ArrayList<>();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        chatsListView = view.findViewById(R.id.chatsListView);
        final String[] splitNumber = mUser.getPhoneNumber().split("\\+2");
        chatsAdapter = new ChatsAdapter(view.getContext(), R.layout.chat_item, chats);
        chatsListView.setAdapter(chatsAdapter);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (permission) {
                        final String chatId = d.getKey();
                        if (chatId.contains(splitNumber[1])) {
                            final String[] numbers = chatId.split(splitNumber[1]);
                            if (numbers[0].equals(""))
                                numbers[0] = numbers[1];

                            final String username = getContactName(view.getContext(), numbers[0]);

                            // getting last message for this chat
                            DatabaseReference mDatabaseReference2 = FirebaseDatabase.getInstance().getReference().child("Chats");
                            mDatabaseReference2.child(chatId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot d : dataSnapshot.getChildren())
                                        lastMessage = d.getValue(Message.class);

                                    if (lastMessage == null) {
                                        lastMessage = new Message("No Messages", "00:00", "", "", "");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            //getting the chat user image
                            DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                            mDatabaseReference.child(numbers[0]).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean removed = false;
                                    int chatIndex = -1;
                                    for (Iterator<Chat> iterator = chats.iterator(); iterator.hasNext(); ) {
                                        chatIndex++;
                                        if (iterator.next().getUser().getPhoneNumber().equals(dataSnapshot.getValue(User.class).getPhoneNumber())) {
                                            iterator.remove();
                                            removed = true;
                                            break;
                                        }
                                    }
                                    if (removed)
                                        chats.add(0, new Chat(new User(username, dataSnapshot.getValue(User.class).getImage(), numbers[0], dataSnapshot.getValue(User.class).getUserId()), lastMessage));
                                    else
                                        chats.add(new Chat(new User(username, dataSnapshot.getValue(User.class).getImage(), numbers[0], dataSnapshot.getValue(User.class).getUserId()), lastMessage));
                                    lastMessage = new Message("No Messages", "", "", "", "");
                                    chatsAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("receiverNumber", chatsAdapter.getItem(i).getUser().getPhoneNumber());
                intent.putExtra("receiverUsername", chatsAdapter.getItem(i).getUser().getUsername());
                intent.putExtra("receiverImage", chatsAdapter.getItem(i).getUser().getImage());
                intent.putExtra("hisUid", chatsAdapter.getItem(i).getUser().getUserId());
                startActivity(intent);
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
            permission = true;
//            try {
//                users = getAllContacts();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        } else {
            permission = false;
            // permission denied,Disable the
            // functionality that depends on this permission.
        }
    }
}