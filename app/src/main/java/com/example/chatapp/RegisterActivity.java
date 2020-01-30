package com.example.chatapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chatapp.modules.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText usernameEditText;
    private String phoneNumber;
    ImageView userImage ;
    Uri imageURI ;
    public User user;
    StorageReference firebaseStorage ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        usernameEditText = findViewById(R.id.usernameEditText);
        Button nextRegisterButton = findViewById(R.id.nextRegisterButton);
        userImage = findViewById(R.id.userImage);
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
            case R.id.userImage:
                Intent intent = new Intent (Intent.ACTION_GET_CONTENT) ;
                intent.setType("image/*") ;
                startActivityForResult(intent , 1);
        }
    }

    private void captureImage() {
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void pushDataInDB() {
        final StorageReference path = firebaseStorage.child("Profile_Pics").child(imageURI.getLastPathSegment()) ;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        final String[] imageUri = new String[1];
        if(imageURI.getLastPathSegment()!=null)
        path.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUri[0] = uri.toString();
                    }
                });
            }
        });

        if (phoneNumber.contains("+2")) {
            String[] array = phoneNumber.split("\\+2");
            phoneNumber = array[1];
        }
        user = new User(usernameEditText.getText().toString().trim(), imageUri[0], phoneNumber);
        myRef.child(user.getPhoneNumber()).setValue(user);
    }

    private boolean checkDataValidation() {
        return !usernameEditText.getText().toString().trim().equals("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK){
            imageURI= data.getData() ;
            userImage.setImageURI(imageURI);
        }
    }
}