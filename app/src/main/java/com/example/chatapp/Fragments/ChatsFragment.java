package com.example.chatapp.Fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.Models.Chat;
import com.example.chatapp.Models.Group;
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

public class ChatsFragment extends Fragment {

    FirebaseUser mUser;
    ArrayList<Chat> chats;
    ChatsAdapter chatsAdapter;
    ListView chatsListView;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
    Message lastMessage;

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
                    final String chatId = d.getKey();
                    if (chatId.contains(splitNumber[1])) {
                        final String[] numbers = chatId.split(splitNumber[1]);
                        if (numbers[0].equals(""))
                            numbers[0] = numbers[1];
                        final String username = getContactName(view.getContext(), numbers[0]);
                        databaseReference.child(chatId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot d : dataSnapshot.getChildren())
                                    lastMessage = d.getValue(Message.class);
                                //getting the chat user image
                                DatabaseReference mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                                mdatabaseReference.child(numbers[0]).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (Iterator<Chat> iterator = chats.iterator(); iterator.hasNext(); ) {
                                            if (iterator.next().getUser().getPhoneNumber().equals(dataSnapshot.getValue(User.class).getPhoneNumber())) {
                                                iterator.remove();
                                                break;
                                            }
                                        }
                                        chats.add(new Chat(new User(username, dataSnapshot.getValue(User.class).getImage(), numbers[0]), lastMessage));
                                        chatsAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                /*for(int i = 0 ; i< chats.size()-1 ; i++ )
                                    for(int j = i+1 ; j<chats.size();j++)
                                        if (chats.get(i).getUser().getPhoneNumber().equals(chats.get(j).getUser().getPhoneNumber())) {
                                            chats.remove(i);
                                            i--;
                                        }*/
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
                startActivity(intent);
            }
        });
        return view;
    }
}