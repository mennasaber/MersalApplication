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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.example.chatapp.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    final static int PERMISSION_CODE = 1001;
    final static int PICK_CODE = 1000;
    public User user;
    ImageButton userImage;
    Uri imageURI;
    StorageReference firebaseStorage;
    ImageView imageView;
    StorageReference imageFolder;
    private EditText usernameEditText;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        imageFolder = FirebaseStorage.getInstance().getReference("imagesFolder");
        imageView = findViewById(R.id.userImage);
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        usernameEditText = findViewById(R.id.usernameEditText);
        Button nextRegisterButton = findViewById(R.id.nextRegisterButton);
        userImage = findViewById(R.id.choiceImageRegister);
        userImage.setOnClickListener(this);
        nextRegisterButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nextRegisterButton:
                boolean validation = checkDataValidation();
                if ((validation)) {
                    pushDataInDB();
                    goToMainActivity();
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

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void pushDataInDB() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        if (phoneNumber.contains("+2")) {
            String[] array = phoneNumber.split("\\+2");
            phoneNumber = array[1];
        }
        user = new User(usernameEditText.getText().toString().trim(), String.valueOf(imageURI), phoneNumber);
        myRef.child(user.getPhoneNumber()).setValue(user);
    }

    private boolean checkDataValidation() {
        return !usernameEditText.getText().toString().trim().equals("");
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
        if (requestCode == PICK_CODE && resultCode == RESULT_OK) {
            final Uri imageData = Objects.requireNonNull(data).getData();
            final StorageReference imageName = imageFolder.child("image" + Objects.requireNonNull(imageData).getLastPathSegment());
            imageName.putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(RegisterActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageView.setImageURI(imageData);
                            imageURI = imageData;
                        }
                    });
                }
            });
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}