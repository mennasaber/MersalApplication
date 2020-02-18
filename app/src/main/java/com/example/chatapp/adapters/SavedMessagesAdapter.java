package com.example.chatapp.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SavedMessagesAdapter extends ArrayAdapter<Message> {
    Context context;

    public SavedMessagesAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        Message currentMessage = getItem(position);
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
        if (currentMessage.getMessage().contains("https")) {
            Picasso.with(context).load(currentMessage.getMessage()).into(messagePic);
        }
        else {
            message.setText(currentMessage.getMessage());
            messagePic.setVisibility(View.GONE);
        }
        usernameTV.setText(currentMessage.username);
        timeTV.setText(time);
        return view;
    }
}
