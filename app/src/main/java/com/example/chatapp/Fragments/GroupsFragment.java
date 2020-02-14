package com.example.chatapp.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.Activities.GroupActivity;
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

public class GroupsFragment extends Fragment {

    FirebaseUser mUser;
    ArrayList<Chat> chats;
    ArrayList<String> groupsIds;
    ArrayList<Group> groups;
    ChatsAdapter chatsAdapter;
    ListView groupsListView;
    Message lastMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_groups, container, false);
        chats = new ArrayList<>();
        groups = new ArrayList<>();
        groupsIds = new ArrayList<>();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        groupsListView = view.findViewById(R.id.groupsListView);

        chatsAdapter = new ChatsAdapter(view.getContext(), R.layout.chat_item, chats);
        groupsListView.setAdapter(chatsAdapter);

        //getting all group ids for this user
        DatabaseReference mdataReference = FirebaseDatabase.getInstance().getReference("userGroups").child(mUser.getPhoneNumber().substring(2)).child("groupsIds");
        mdataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    groupsIds.add(d.getValue(String.class));

                }

                //get all group chats for this user
                DatabaseReference chatsDR = FirebaseDatabase.getInstance().getReference("groups");
                chatsDR.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot g : dataSnapshot.getChildren()) {
                            final Group group = g.getValue(Group.class);

                            if (groupsIds.contains(group.getGroupId())) { // get the last message
                                final DatabaseReference lastMessageDR = FirebaseDatabase.getInstance().getReference().child("GroupsMessages").child(group.getGroupId());
                                lastMessageDR.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot c : dataSnapshot.getChildren()) {
                                            lastMessage = c.getValue(Message.class);
                                        }
                                        if (lastMessage == null) {
                                            lastMessage = new Message("No Messages Yet", "", "", "", 0);
                                        }
                                        chats.add(new Chat(new User(group.getGroupName(), "", group.getGroupId()), lastMessage));
                                        chatsAdapter.notifyDataSetChanged();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), GroupActivity.class);
                intent.putExtra("receiverNumber", chatsAdapter.getItem(i).getUser().getPhoneNumber());
                intent.putExtra("receiverUsername", chatsAdapter.getItem(i).getUser().getUsername());
                intent.putExtra("receiverImage", chatsAdapter.getItem(i).getUser().getImage());
                startActivity(intent);
            }
        });
        return view;
    }
}