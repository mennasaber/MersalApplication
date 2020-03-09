package com.example.chatapp.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.chatapp.Activities.MainActivity;
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

import static android.app.Activity.RESULT_OK;


public class  SettingsFragment extends Fragment {
    final static int PERMISSION_CODE = 1001;
    final static int PICK_CODE = 1000;
    TextView username, phone;
    FirebaseUser firebaseUser;
    AlertDialog.Builder alertBuilder;
    AlertDialog alertDialog;
    EditText editUsername;
    Button save;
    ImageView profileImage ;
    ImageButton imageButton;
    StorageReference imageFolder;
    ProgressBar progressBar ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.fragment_settings, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        imageButton = mainView.findViewById(R.id.changeProfileImage);
        username = mainView.findViewById(R.id.usernameTextView);
        phone = mainView.findViewById(R.id.phoneNumberTextView);
        profileImage = mainView.findViewById(R.id.profilepicture) ;
        progressBar=mainView.findViewById(R.id.indeterminateBar);
        MainActivity mainActivity = (MainActivity) getActivity();
        username.setText(mainActivity.currentUser.getUsername());
        phone.setText(firebaseUser.getPhoneNumber().substring(2));
        if (!mainActivity.currentUser.getImage().equals(""))
            Picasso.with(getContext()).load(mainActivity.currentUser.getImage()).into(profileImage);

        imageFolder = FirebaseStorage.getInstance().getReference("imagesFolder");

        username.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createPopupDialogue();
                return false;
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
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
        return mainView;
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                            profileImage.setImageURI(imageData);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                            databaseReference.child(firebaseUser.getPhoneNumber().substring(2)).child("image").setValue(String.valueOf(uri));
                        }
                    });
                }
            });
        }
    }

    private void createPopupDialogue() {
        alertBuilder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.edit_username_popup, null);
        save = view.findViewById(R.id.save);
        editUsername = view.findViewById(R.id.editUsername);
        alertBuilder.setView(view);
        alertDialog = alertBuilder.create();
        alertDialog.show();

        Dialog dlg = alertDialog;

        Window window = dlg.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editUsername.getText().toString().isEmpty()) {
                    username.setText(editUsername.getText().toString());
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = database.getReference("users");
                    databaseReference.child(firebaseUser.getPhoneNumber().substring(2)).setValue
                            (new User(username.getText().toString(), "", firebaseUser.getPhoneNumber().substring(2),firebaseUser.getUid()));
                    alertDialog.hide();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(getContext(), "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}