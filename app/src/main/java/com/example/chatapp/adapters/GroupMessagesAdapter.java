package com.example.chatapp.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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


    public MediaPlayer mediaPlayer = new MediaPlayer();
    Context context;
    String users;
    String dataResource = "";

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
            final ImageView playButton = view.findViewById(R.id.playButton);
            LinearLayout recordMess = view.findViewById(R.id.recordMess);
            if (currentMessage.getMessage().contains("recordsFolder")) {
                imageView.setVisibility(View.GONE);

                playButton.setImageResource(play);
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer = new MediaPlayer();
                            try {
                                playButton.setImageResource(R.drawable.pause);
                                mediaPlayer.setDataSource(currentMessage.getMessage());
                                dataResource = currentMessage.getMessage();
                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp.start();
                                    }
                                });
                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        playButton.setImageResource(play);
                                    }
                                });
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (currentMessage.getMessage().equals(dataResource)) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                            playButton.setImageResource(play);
                        }
//                        else if (!currentMessage.getMessage().equals(dataResource)) {
//                            mediaPlayer.stop();
//                            mediaPlayer.release();
//                            mediaPlayer = new MediaPlayer();
//                            try {
//                                playButton.setImageResource(R.drawable.pause);
//                                mediaPlayer.setDataSource(currentMessage.getMessage());
//                                dataResource = currentMessage.getMessage();
//                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                    @Override
//                                    public void onPrepared(MediaPlayer mp) {
//                                        mp.start();
//                                    }
//                                });
//                                mediaPlayer.prepare();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }

                    }
                });
            } else if (currentMessage.getMessage().contains("imagesFolder")) {
                Picasso.with(context).load(currentMessage.getMessage()).into(imageView);
                recordMess.setVisibility(View.GONE);
            } else {
                message.setText(currentMessage.getMessage());
                imageView.setVisibility(View.GONE);
                recordMess.setVisibility(View.GONE);
            }
            timeTV.setText(time);

            if (currentMessage.getSeeners().equals("All")) {
                ImageView seenImage = view.findViewById(R.id.seenImage);
                seenImage.setImageResource(R.drawable.ic_baseline_done_all_24);
            }
        } else {
            view = View.inflate(context, R.layout.their_message, null);
            final TextView usernameTV = view.findViewById(R.id.usernameMessageTV);
            final ImageView imageView = view.findViewById(R.id.imageView);
            TextView message = view.findViewById(R.id.theirMessageTV);
            ImageView imageView2 = view.findViewById(R.id.theirMessageIV);
            final ImageView theirPlayButton = view.findViewById(R.id.theirPlayButton);
            LinearLayout theirRecordMess = view.findViewById(R.id.theirRecordMess);
            if (currentMessage.getMessage().contains("recordsFolder")) {
                imageView2.setVisibility(View.GONE);
                theirPlayButton.setImageResource(play);
                theirPlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer = new MediaPlayer();
                            theirPlayButton.setImageResource(R.drawable.pause);
                            try {
                                mediaPlayer.setDataSource(currentMessage.getMessage());
                                dataResource = currentMessage.getMessage();
                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp.start();
                                    }
                                });
                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        theirPlayButton.setImageResource(play);
                                    }
                                });
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (currentMessage.getMessage().equals(dataResource)) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                            theirPlayButton.setImageResource(play);

                        }
//                        else if (!currentMessage.getMessage().equals(dataResource)) {
//                            mediaPlayer.stop();
//                            mediaPlayer.release();
//                            mediaPlayer = new MediaPlayer();
//                            try {
//                                theirPlayButton.setImageResource(R.drawable.pause);
//                                mediaPlayer.setDataSource(currentMessage.getMessage());
//                                dataResource = currentMessage.getMessage();
//                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                    @Override
//                                    public void onPrepared(MediaPlayer mp) {
//                                        mp.start();
//                                    }
//                                });
//                                mediaPlayer.prepare();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }

                    }
                });
            } else if (currentMessage.getMessage().contains("imagesFolder")) {
                Picasso.with(context).load(currentMessage.getMessage()).into(imageView);
                theirRecordMess.setVisibility(View.GONE);
            } else {
                message.setText(currentMessage.getMessage());
                imageView2.setVisibility(View.GONE);
                theirRecordMess.setVisibility(View.GONE);
            }
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
            databaseReference.child(currentMessage.getSenderPhone()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    String name = getContactName(context,user.getPhoneNumber());
                    if(name!=null)
                        usernameTV.setText(name);
                    else
                        usernameTV.setText(user.getPhoneNumber());
                    Picasso.with(context).load(user.getImage()).into(imageView);
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

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
}