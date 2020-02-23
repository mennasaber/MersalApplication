package com.example.chatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.Activities.GroupDetailsActivity;
import com.example.chatapp.Models.Chat;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatsAdapter extends ArrayAdapter<Chat> {
    Context context;
    String users="";
    public ChatsAdapter(@NonNull Context context, int resource, @NonNull List<Chat> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        String userPhoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        final String[] splitNumber = userPhoneNumber.split("\\+2");
        userPhoneNumber = splitNumber[1];
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item, null);
        }
        TextView usernameTV = view.findViewById(R.id.usernameChatTextView);
        TextView lastMessageTV = view.findViewById(R.id.lastMessageTextView);
        TextView timeTV = view.findViewById(R.id.timeChatTextView);
        ImageView imageView = view.findViewById(R.id.chatImageView);
        ImageView seenImageView = view.findViewById(R.id.seenChatImage);
        ImageView UnReadImageView = view.findViewById(R.id.UnReadImageView);
        Chat currentChat = getItem(position);
        String time = currentChat.getLastMessage().getTime();

        DateFormat df = new SimpleDateFormat("HH:mm");
        DateFormat outputFormat = new SimpleDateFormat("hh:mm aa");
        Date date = null;
        try {
            if (!time.equals("")) {
                date = df.parse(time);
                time = outputFormat.format(date);
            }
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        if(!currentChat.getUser().getImage().equals(""))
            Picasso.with(context).load(currentChat.getUser().getImage()).into(imageView);
        usernameTV.setText(currentChat.getUser().getUsername());
        lastMessageTV.setText(currentChat.getLastMessage().getMessage());
        timeTV.setText(time);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("groupUsers").child(currentChat.getLastMessage().getReceiverPhone());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users = "" ;
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    User user = d.getValue(User.class);
                    users+=user.getPhoneNumber();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }});

        try {
            if (currentChat.getLastMessage().getSeeners().equals("All"))
                seenImageView.setImageResource(R.drawable.ic_baseline_done_all_24);
            else if (!currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber))
                seenImageView.setVisibility(View.GONE);
            if (!currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber) && !currentChat.getLastMessage().getSeeners().equals("All")||
                    !currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber)&&!currentChat.getLastMessage().getSeeners().contains(userPhoneNumber)&&
                            !currentChat.getLastMessage().getSeeners().equals("All")) {
                UnReadImageView.setVisibility(View.VISIBLE);
                lastMessageTV.setTextColor(view.getResources().getColor(R.color.colorUnRead));
                timeTV.setTextColor(view.getResources().getColor(R.color.colorUnRead));
            } else
                UnReadImageView.setVisibility(View.INVISIBLE);
        }
        catch (NullPointerException n){
        }

        return view;
    }
}
