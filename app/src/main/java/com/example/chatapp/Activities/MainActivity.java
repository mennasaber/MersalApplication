package com.example.chatapp.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatapp.Fragments.ChatsFragment;
import com.example.chatapp.Fragments.ContactsFragment;
import com.example.chatapp.Fragments.GroupsFragment;
import com.example.chatapp.Fragments.SettingsFragment;
import com.example.chatapp.Models.Block;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener  {
    public static User currentUser;
    FirebaseUser firebaseUser;
    private DrawerLayout drawerLayout;
    AlertDialog.Builder alertBuilder;
    AlertDialog alertDialog;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    View header ;
    Button confirm ;
    ImageView profileImage ;
    TextView phoneNum , userName ,confirmationMessage;
    String mUID ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUID = FirebaseAuth.getInstance().getUid() ;
        NavigationView navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
        profileImage = header.findViewById(R.id.profileImage) ;
        phoneNum = header.findViewById(R.id.phoneText) ;
        userName = header.findViewById(R.id.usernameText) ;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if ("groups".equals(getIntent().getStringExtra("fragmentName"))) {
            fragmentTransaction.replace(R.id.frameLayout, new GroupsFragment());
        } else {
            fragmentTransaction.replace(R.id.frameLayout, new ChatsFragment());
        }
        fragmentTransaction.commit();


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //getting current user data form firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("users").child(firebaseUser.getPhoneNumber().substring(2));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    currentUser = dataSnapshot.getValue(User.class);
                    userName.setText(currentUser.getUsername()) ;
                    phoneNum.setText(currentUser.getPhoneNumber()) ;
                    Picasso.with(getApplicationContext()).load(currentUser.getImage()).into(profileImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}});
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        switch (menuItem.getItemId()) {
            case R.id.contactsOption:
                fragment = new ContactsFragment();
                break;
            case R.id.chats:
                fragment = new ChatsFragment();
                break;
            case R.id.settingOption:
                fragment = new SettingsFragment();
                break;
            case R.id.newGroupOption:
                Intent intent1 = new Intent(this, SelectGroupMembersActivity.class);
                startActivity(intent1);
                break;
            case R.id.Groups:
                fragment = new GroupsFragment();
                break;
            case R.id.savedMessagesOption:
                Intent intent2 = new Intent(this, SavedMessagesActivity.class);
                intent2.putExtra("mUserPic" , currentUser.getImage()) ;
                startActivity(intent2);
                break;
            case R.id.logout:
            createPopupDialogue();
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, fragment);
            fragmentTransaction.commit();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }
    private void createPopupDialogue() {
        alertBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.block_confirmation_message, null);
        confirm = view.findViewById(R.id.confirm);
        confirmationMessage = view.findViewById(R.id.confirmationMessage);
        confirmationMessage.setText("Are you sure you want to logout ?");
        alertBuilder.setView(view);
        alertDialog = alertBuilder.create();
        alertDialog.show();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                startActivity(new Intent(getApplicationContext(), VerificationActivity.class));
                finish();
                alertDialog.hide();

            }
        });
    }
}