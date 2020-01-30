package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText phoneEditText;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        Button nextButton = findViewById(R.id.nextButton);
        phoneEditText = findViewById(R.id.phoneEditText);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        nextButton.setOnClickListener(this);


        if (firebaseUser!=null){
            startActivity(new Intent (getApplicationContext() , MainActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.nextButton) {
            boolean validation = checkPhoneValidation(phoneEditText.getText().toString().trim());
            if (validation)
                goToVerifyActivity(phoneEditText.getText().toString().trim());
        }
    }

    private boolean checkPhoneValidation(String phone) {
        if (phone != "" && phone.length() == 11)
            return true;
        else {
            phoneEditText.setError("Valid number is required");
            phoneEditText.requestFocus();
            return false;
        }
    }

    private void goToVerifyActivity(String phone) {
        Intent intent = new Intent(this, VerifyVerificationActivity.class);
        intent.putExtra("phoneNumber", "+2" + phone);
        startActivity(intent);
        finish() ;
    }
}