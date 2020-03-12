package com.example.chatapp.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    final static int PERMISSION_CODE = 1001;
    final static int PICK_CODE = 1000;
    public User user;
    ImageButton chooseImage;
    String imageURI;
    StorageReference firebaseStorage;
    ImageView imageView;
    StorageReference imageFolder;
    EditText usernameEditText;
    Button nextRegisterButton ;
    ProgressBar progressBar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progressBar=findViewById(R.id.indeterminateBar2);
        imageFolder = FirebaseStorage.getInstance().getReference("imagesFolder");
        imageView = findViewById(R.id.userImage);
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        usernameEditText = findViewById(R.id.usernameEditText);
        nextRegisterButton = findViewById(R.id.nextRegisterButton);
        chooseImage = findViewById(R.id.choiceImageRegister);
        chooseImage.setOnClickListener(this);
        nextRegisterButton.setOnClickListener(this);
        imageURI="" ;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nextRegisterButton:
                try {
                    if (!usernameEditText.getText().equals("")&&!imageURI.equals("")) {
                        pushDataInDB();
                        startActivity(new Intent(this, MainActivity.class));
                    }
                }
                catch (Exception e ){
                    Toast.makeText(this, "Failed to insert data", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.choiceImageRegister:
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
                break;
        }
    }

    private void pushDataInDB() {

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        user = new User(usernameEditText.getText().toString().trim(), String.valueOf(imageURI), mUser.getPhoneNumber().substring(2), mUser.getUid());
        myRef.child(user.getPhoneNumber()).setValue(user);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        nextRegisterButton.setEnabled(false);
        try {
            if (requestCode == PICK_CODE && resultCode == RESULT_OK) {
                progressBar.setVisibility(View.VISIBLE);
                final Uri imageData = Objects.requireNonNull(data).getData();
                final StorageReference imageName = imageFolder.child("image" + Objects.requireNonNull(imageData).getLastPathSegment());
                imageName.putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(RegisterActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageURI = uri.toString();
                                Picasso.with(getApplicationContext()).load(imageURI).into(imageView);
                            }
                        });
                    }
                });
            }
        }catch (Exception e){
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
        nextRegisterButton.setEnabled(true);
    }
}