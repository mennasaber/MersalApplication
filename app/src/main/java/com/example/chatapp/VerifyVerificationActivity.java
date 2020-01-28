package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyVerificationActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText codeEditText;
    private FirebaseAuth firebaseAuth;
    private String verificationId;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_verification);
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        codeEditText = findViewById(R.id.codeEditText);
        Button verifyButton = findViewById(R.id.verifyButton);
        verifyButton.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        sendVerificationCode(phoneNumber);
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    codeEditText.setText(code);
                    signInWithPhoneAuthCredential(code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId = s;
            }
        };
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, callbacks);
    }

    private void signInWithPhoneAuthCredential(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    goToRegisterActivity();
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        codeEditText.setError("Invalid code entered");
                        codeEditText.requestFocus();
                    }
                }
            }
        });
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("phoneNumber", phoneNumber);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.verifyButton) {
            String code = codeEditText.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                codeEditText.setError("Enter valid code");
                codeEditText.requestFocus();
                return;
            }
            signInWithPhoneAuthCredential(code);
        }
    }
}