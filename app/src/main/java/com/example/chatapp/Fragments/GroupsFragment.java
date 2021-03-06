package com.example.chatapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.chatapp.Activities.GroupActivity;
import com.example.chatapp.Adapters.ChatsAdapter;
import com.example.chatapp.Models.Chat;
import com.example.chatapp.Models.Group;
import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class GroupsFragment extends Fragment {

    FirebaseUser mUser;
    ArrayList<Chat> chats;
    ArrayList<String> groupsIds;
    ArrayList<Group> groups;
    ChatsAdapter chatsAdapter;
    ListView groupsListView;
    Message lastMessage;
    String users;

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
                chats.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    groupsIds.add(d.getValue(String.class));

                }

                //get all group chats for this user
                DatabaseReference chatsDR = FirebaseDatabase.getInstance().getReference("groups");
                chatsDR.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chats.clear();
                        for (DataSnapshot g : dataSnapshot.getChildren()) {
                            final Group group = g.getValue(Group.class);

                            if (groupsIds.contains(group.getGroupId())) { // get the last message
                                final DatabaseReference lastMessageDR = FirebaseDatabase.getInstance().getReference().child("GroupsChats").child(group.getGroupId());
                                lastMessageDR.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot c : dataSnapshot.getChildren())
                                            lastMessage = c.getValue(Message.class);
                                        if (lastMessage == null) {
                                            lastMessage = new Message("No Messages", "", "", group.getGroupId(), "");
                                        }
                                        boolean removed = false;
                                        int chatIndex = -1;
                                        for (Iterator<Chat> iterator = chats.iterator(); iterator.hasNext(); ) {
                                            chatIndex++;
                                            if (iterator.next().getUser().getPhoneNumber().equals(lastMessage.getReceiverPhone())) {
                                                iterator.remove();
                                                removed = true;
                                                break;
                                            }
                                        }
                                        if (removed)
                                            chats.add(0, new Chat(new User(group.getGroupName(),
                                                    group.getGroupImage(), group.getGroupId(), group.getGroupId()), lastMessage));
                                        else
                                            chats.add(new Chat(new User(group.getGroupName(),
                                                    group.getGroupImage(), group.getGroupId(), group.getGroupId()), lastMessage));
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
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                //getting group users
                DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference("groupUsers")
                        .child(chatsAdapter.getItem(i).getUser().getPhoneNumber());
                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        users = "";
                        for (DataSnapshot d : dataSnapshot.getChildren())
                            users += d.getValue(User.class).getPhoneNumber();

                        Intent intent = new Intent(getActivity(), GroupActivity.class);
                        intent.putExtra("receiverNumber", chatsAdapter.getItem(i).getUser().getPhoneNumber());
                        intent.putExtra("receiverUsername", chatsAdapter.getItem(i).getUser().getUsername());
                        intent.putExtra("gImage", chatsAdapter.getItem(i).getUser().getImage());
                        intent.putExtra("gUsers", users);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }
        });
        return view;
    }
}