package com.example.chatapp.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chatapp.R;
import com.example.chatapp.Models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    public User user;
    ImageView userImage;
    Uri imageURI;
    StorageReference firebaseStorage;
    String imageUri = "";
    private EditText usernameEditText;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseStorage = FirebaseStorage.getInstance().getReference();
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
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
        }
    }

    private void captureImage() {
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void pushDataInDB() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");


       /* if(imageURI!=null) {
            final StorageReference path = firebaseStorage.child("Profile_Pics").child(Objects.requireNonNull(imageURI.getLastPathSegment()));
            path.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUri = uri.toString();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                   Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
        }*/
        if (phoneNumber.contains("+2")) {
            String[] array = phoneNumber.split("\\+2");
            phoneNumber = array[1];
        }
        user = new User(usernameEditText.getText().toString().trim(), imageUri, phoneNumber);

        myRef.child(user.getPhoneNumber()).setValue(user);

    }

    private boolean checkDataValidation() {
        return !usernameEditText.getText().toString().trim().equals("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageURI = data.getData();
            userImage.setImageURI(imageURI);
        }
    }
}