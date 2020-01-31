package com.example.chatapp.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.adapters.ContactsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.app.Activity.RESULT_OK;


public class SettingsFragment extends Fragment {
    ImageView profileImage ;
    TextView username , phone ;
    Uri imageURI ;
    FirebaseUser firebaseUser;
    AlertDialog.Builder alertBuilder ;
    AlertDialog alertDialog ;
    EditText editUsername ;
    Button save ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        profileImage = view.findViewById(R.id.profilepicture) ;
        username = view.findViewById(R.id.usernameTextView) ;
        phone = view.findViewById(R.id.phoneNumberTextView) ;
        MainActivity mainActivity = (MainActivity)getActivity() ;
        username.setText(mainActivity.userName);
        phone.setText(firebaseUser.getPhoneNumber().substring(2));

        username.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createPopupDialogue();
                return false;
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (Intent.ACTION_GET_CONTENT) ;
                intent.setType("image/*") ;
                startActivityForResult(intent , 1);
            }
        });
        return view ;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK){
            imageURI= data.getData() ;
            profileImage.setImageURI(imageURI);

            // firebase upload image code
        }
    }

    private void createPopupDialogue(){
        alertBuilder = new AlertDialog.Builder(getContext()) ;
         View view = getLayoutInflater().inflate(R.layout.edit_username_popup,null);
        save = view.findViewById(R.id.save) ;
        editUsername = view.findViewById(R.id.editUsername) ;
        alertBuilder.setView(view);
        alertDialog = alertBuilder.create();
        alertDialog.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editUsername.getText().toString().isEmpty()) {
                    username.setText(editUsername.getText().toString());
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = database.getReference("users");
                    databaseReference.child(firebaseUser.getPhoneNumber().substring(2)).setValue
                            (new User(username.getText().toString(), "", firebaseUser.getPhoneNumber().substring(2)));
                    alertDialog.hide();
                }
            }
        });
    }
}