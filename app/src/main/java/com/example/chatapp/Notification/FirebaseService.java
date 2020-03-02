package com.example.chatapp.Notification;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseService extends FirebaseMessagingService {
    FirebaseUser user ;
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
         user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null)
            updateToken(s);
    }

    private void updateToken(String tokenRefresh) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        databaseReference.child(user.getUid()).setValue(tokenRefresh) ;
    }
}