package com.example.chatapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.example.chatapp.R.drawable.pause;
import static com.example.chatapp.R.drawable.play;

public class MessagesAdapter extends ArrayAdapter<Message> {
    Context context;
    String username;
    String image ;

    public MessagesAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects, String username ,String image) {
        super(context, resource, objects);
        this.context = context;
        this.username = username;
        this.image = image ;
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
            TextView message = view.findViewById(R.id.myMessageTextView);
            final ImageView imageView = view.findViewById(R.id.myMessageIV);
            LinearLayout recordMess = view.findViewById(R.id.recordMess);
            final ImageView playButton = view.findViewById(R.id.playButton) ;
            playButton.setImageResource(play);
            if (currentMessage.getMessage().contains("recordsFolder")){
                imageView.setVisibility(View.GONE);
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
            else if (currentMessage.getMessage().contains("imagesFolder")){
                Picasso.with(context).load(currentMessage.getMessage()).into(imageView);
                recordMess.setVisibility(View.GONE);
            }
            else {
                message.setText(currentMessage.getMessage());
                imageView.setVisibility(View.GONE);
                recordMess.setVisibility(View.GONE);
            }
            TextView timeTV = view.findViewById(R.id.timeMyMessageTV);
            timeTV.setText(time);
            if (currentMessage.getSeeners().equals("All")) {
                ImageView seenImage = view.findViewById(R.id.seenImage);
                seenImage.setImageResource(R.drawable.ic_baseline_done_all_24);
            }
        } else {

            view = View.inflate(context, R.layout.their_message, null);
            TextView usernameTV = view.findViewById(R.id.usernameMessageTV);
            ImageView imageView = view.findViewById(R.id.imageView);
            TextView message = view.findViewById(R.id.theirMessageTV);
            ImageView imageView2 = view.findViewById(R.id.theirMessageIV);
            LinearLayout theirRecordMess = view.findViewById(R.id.theirRecordMess);
            final ImageView theirPlayButton = view.findViewById(R.id.theirPlayButton) ;
            theirPlayButton.setImageResource(play);
            if(!image.equals(""))
            Picasso.with(context).load(image).into(imageView);
            if (currentMessage.getMessage().contains("recordsFolder")){
                imageView2.setVisibility(View.GONE);
                theirPlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayer mediaPlayer = new MediaPlayer() ;
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
                Picasso.with(context).load(currentMessage.getMessage()).into(imageView2);
                theirRecordMess.setVisibility(View.GONE);
            }
            else {
                message.setText(currentMessage.getMessage());
                imageView2.setVisibility(View.GONE);
                theirRecordMess.setVisibility(View.GONE);
            }
            usernameTV.setText(username);
            TextView timeTV = view.findViewById(R.id.timeTheirMessageTV);
            timeTV.setText(time);
        }
        return view;
    }
}
