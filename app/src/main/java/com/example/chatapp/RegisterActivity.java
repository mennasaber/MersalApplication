package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chatapp.modules.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText usernameEditText;
    private String phoneNumber;
    public User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        usernameEditText = findViewById(R.id.usernameEditText);
        Button nextRegisterButton = findViewById(R.id.nextRegisterButton);
        ImageView userImage = findViewById(R.id.userImage);
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
                captureImage();
        }
    }

    private void captureImage() {
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void pushDataInDB() {
        user = new User(usernameEditText.getText().toString().trim(), "", phoneNumber);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.child(user.getPhoneNumber()).setValue(user);
    }

    private boolean checkDataValidation() {
        return !usernameEditText.getText().toString().trim().equals("");
    }
}