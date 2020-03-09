package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatapp.Models.Group;
import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.User;
import com.example.chatapp.Models.UserGroups;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class GroupDataActivity extends AppCompatActivity {
    final static int PICK_CODE = 1000;
    final static int PERMISSION_CODE = 1001;
    int index = 0;
    ImageButton imageButton;
    Uri imageURI;
    ImageView imageView;
    String imageEncoded = "";
    StorageReference imageFolder;
    private ArrayList<User> membersList = SelectGroupMembersActivity.usersSelected;
    private Group group;
    private FloatingActionButton createGroupFAB;
    private EditText groupNameEditText;
    private DatabaseReference databaseReference1;
    private DatabaseReference databaseReference2;
    private DatabaseReference databaseReference3;
    private UserGroups userGroups;
    private String userPhoneNumber;
    ProgressBar progressBar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_data);
        Objects.requireNonNull(getSupportActionBar()).setTitle("New group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = findViewById(R.id.indeterminateBar3);
        imageFolder = FirebaseStorage.getInstance().getReference("imagesFolder");
        userPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        String[] arr = userPhoneNumber.split("\\+2");
        userPhoneNumber = arr[1];
        imageView = findViewById(R.id.groupImage);
        imageButton = findViewById(R.id.choiceImageGroupData);
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
                    group.setGroupImage(imageEncoded);
                    group.setGroupName(groupNameEditText.getText().toString().trim());
                    group.setGroupId(databaseReference1.push().getKey());
                    databaseReference1.child(group.getGroupId()).setValue(group);
                    databaseReference3.child(group.getGroupId()).setValue(membersList);
                    UpdateMembersGroups();
                    Intent intent = new Intent(GroupDataActivity.this, GroupActivity.class);
                    intent.putExtra("receiverNumber", group.getGroupId());
                    intent.putExtra("receiverUsername", group.getGroupName());
                    intent.putExtra("gImage", group.getGroupImage());
                    startActivity(intent);
                    finish();
                }
                else
                {
                    groupNameEditText.setError("Valid name is required");
                    groupNameEditText.requestFocus();
                }
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    } else {
                        pickImageFromGallery();
                    }
                } else {
                    pickImageFromGallery();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CODE && resultCode == RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);
            final Uri imageData = Objects.requireNonNull(data).getData();
            final StorageReference imageName = imageFolder.child("image" + Objects.requireNonNull(imageData).getLastPathSegment());
            imageName.putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageView.setImageURI(imageData);
                            imageEncoded = String.valueOf(uri);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}