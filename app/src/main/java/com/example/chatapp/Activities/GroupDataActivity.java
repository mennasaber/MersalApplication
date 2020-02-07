package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.chatapp.Models.Group;
import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.User;
import com.example.chatapp.Models.UserGroups;
import com.example.chatapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupDataActivity extends AppCompatActivity {
    int index = 0;
    private ArrayList<User> membersList = SelectGroupMembersActivity.usersSelected;
    private Group group;
    private FloatingActionButton createGroupFAB;
    private EditText groupNameEditText;
    private DatabaseReference databaseReference1;
    private DatabaseReference databaseReference2;
    private DatabaseReference databaseReference3;
    private UserGroups userGroups;
    private String userPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_data);

        userPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        String[] arr = userPhoneNumber.split("\\+2");
        userPhoneNumber = arr[1];

        userGroups = new UserGroups(new ArrayList<String>());
        groupNameEditText = findViewById(R.id.groupNameEditText);
        databaseReference1 = FirebaseDatabase.getInstance().getReference("groups");
        databaseReference2 = FirebaseDatabase.getInstance().getReference("userGroups").child(userPhoneNumber);
        databaseReference3 = FirebaseDatabase.getInstance().getReference("groupUsers");

        createGroupFAB = findViewById(R.id.createGroupFAB);
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userGroups = dataSnapshot.getValue(UserGroups.class);
                if (userGroups == null)
                    userGroups = new UserGroups(new ArrayList<String>());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        createGroupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!groupNameEditText.getText().toString().trim().equals("")) {
                    group = new Group();
                    group.setGroupImage("");
                    group.setGroupName(groupNameEditText.getText().toString().trim());
                    group.setGroupId(databaseReference1.push().getKey());
                    databaseReference1.child(group.getGroupId()).setValue(group);
                    databaseReference3.child(group.getGroupId()).setValue(membersList);
                    UpdateMembersGroups();
                    Intent intent = new Intent(GroupDataActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    private void UpdateMembersGroups() {

        for (int i = 0; i < membersList.size(); i++) {
            update(i);
        }
    }

    private void update(final int i) {
        final DatabaseReference databaseReference;
        databaseReference = FirebaseDatabase.getInstance().getReference("userGroups");
        databaseReference.child(membersList.get(i).getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userGroups = dataSnapshot.getValue(UserGroups.class);
                if (userGroups == null)
                    userGroups = new UserGroups(new ArrayList<String>());
                userGroups.getGroupsIds().add(group.getGroupId());
                databaseReference.child(membersList.get(i).getPhoneNumber()).setValue(userGroups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}