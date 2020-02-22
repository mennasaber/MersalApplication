package com.example.chatapp.Adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.example.chatapp.R.drawable.play;

public class GroupMessagesAdapter extends ArrayAdapter<Message> {


    Context context;
    String users ;
    public GroupMessagesAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        String userPhoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        final Message currentMessage = getItem(position);
        String time = currentMessage.getTime();
        DateFormat df = new SimpleDateFormat("HH:mm");
        DateFormat outputFormat = new SimpleDateFormat("hh:mm aa");
        Date date = null;
        try {
            date = df.parse(time);
            time = outputFormat.format(date);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        final String[] splitNumber = userPhoneNumber.split("\\+2");
        userPhoneNumber = splitNumber[1];
        if (Objects.requireNonNull(currentMessage).getSenderPhone().equals(userPhoneNumber)) {
            view = View.inflate(context, R.layout.my_message, null);
            ImageView imageView = view.findViewById(R.id.myMessageIV);
            TextView message = view.findViewById(R.id.myMessageTextView);
            TextView timeTV = view.findViewById(R.id.timeMyMessageTV);
            LinearLayout recordMess = view.findViewById(R.id.recordMess);
            if (currentMessage.getMessage().contains("recordsFolder")) {
                imageView.setVisibility(View.GONE);
                final ImageView playButton = view.findViewById(R.id.playButton) ;
                playButton.setImageResource(play);
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final MediaPlayer mediaPlayer = new MediaPlayer() ;

                        try {
                            mediaPlayer.setDataSource(currentMessage.getMessage());
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                }
                            });
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            else if (currentMessage.getMessage().contains("imagesFolder")) {
                Picasso.with(context).load(currentMessage.getMessage()).into(imageView);
                recordMess.setVisibility(View.GONE);
            }
            else {
                message.setText(currentMessage.getMessage());
                imageView.setVisibility(View.GONE);
                recordMess.setVisibility(View.GONE);
            }
            timeTV.setText(time);
           /* final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("groupUsers");
            databaseReference.child(currentMessage.getReceiverPhone()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    users+=user.getPhoneNumber() ;
                    Toast.makeText(context , users , Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //if (users!=null&&users.length()==currentMessage.getSeeners().length())*/
            if (true) {
                ImageView seenImage = view.findViewById(R.id.seenImage);
                seenImage.setImageResource(R.drawable.ic_baseline_done_all_24);
            }
        } else {
            view = View.inflate(context, R.layout.their_message, null);
            final TextView usernameTV = view.findViewById(R.id.usernameMessageTV);
            final ImageView imageView = view.findViewById(R.id.imageView);
            TextView message = view.findViewById(R.id.theirMessageTV);
            ImageView imageView2 = view.findViewById(R.id.theirMessageIV);
            ImageView theirPlayButton = view.findViewById(R.id.theirPlayButton);
            LinearLayout theirRecordMess = view.findViewById(R.id.theirRecordMess) ;
            if (currentMessage.getMessage().contains("recordsFolder")) {
                imageView2.setVisibility(View.GONE);
                theirPlayButton.setImageResource(play);
                theirPlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final MediaPlayer mediaPlayer = new MediaPlayer() ;

                        try {
                            mediaPlayer.setDataSource(currentMessage.getMessage());
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                }
                            });
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            else if (currentMessage.getMessage().contains("imagesFolder")) {
                Picasso.with(context).load(currentMessage.getMessage()).into(imageView);
                theirRecordMess.setVisibility(View.GONE);
            }
            else {
                message.setText(currentMessage.getMessage());
                imageView2.setVisibility(View.GONE);
                theirRecordMess.setVisibility(View.GONE);
            }
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
            databaseReference.child(currentMessage.getSenderPhone()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    usernameTV.setText(user.getUsername());
                    //set image view
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            TextView timeTV = view.findViewById(R.id.timeTheirMessageTV);
            timeTV.setText(time);
        }
        return view;
    }
}