package com.example.chatapp.Adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.example.chatapp.R.drawable.play;

public class SavedMessagesAdapter extends ArrayAdapter<Message> {
    Context context;
    String image;
    MediaPlayer mediaPlayer;
    public SavedMessagesAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects , String image , MediaPlayer mediaPlayer) {
        super(context, resource, objects);
        this.context = context;
        this.image=image ;
        this.mediaPlayer=mediaPlayer ;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
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
        if (view == null)
            view = View.inflate(context, R.layout.their_message, null);
        TextView usernameTV = view.findViewById(R.id.usernameMessageTV);
        ImageView profPic = view.findViewById(R.id.imageView);
        ImageView messagePic = view.findViewById(R.id.theirMessageIV);
        TextView message = view.findViewById(R.id.theirMessageTV);
        TextView timeTV = view.findViewById(R.id.timeTheirMessageTV);
        LinearLayout theirRecordMess = view.findViewById(R.id.theirRecordMess);
        final ImageView theirPlayButton = view.findViewById(R.id.theirPlayButton) ;
        if(!image.equals(""))
            Picasso.with(context).load(image).into(profPic);
        if (currentMessage.getMessage().contains("imagesFolder")) {
            Picasso.with(context).load(currentMessage.getMessage()).into(messagePic);
            theirRecordMess.setVisibility(View.GONE);
        }
        else if  (currentMessage.getMessage().contains("recordsFolder")){
            messagePic.setVisibility(View.GONE);
            theirPlayButton.setImageResource(play);
            theirPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     mediaPlayer = new MediaPlayer() ;
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
        else {
            message.setText(currentMessage.getMessage());
            messagePic.setVisibility(View.GONE);
            theirRecordMess.setVisibility(View.GONE);
        }
        usernameTV.setText(currentMessage.getUsername());
        timeTV.setText(time);
        return view;
    }
}
