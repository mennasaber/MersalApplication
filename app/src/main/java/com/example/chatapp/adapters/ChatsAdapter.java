package com.example.chatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.Models.Chat;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatsAdapter extends ArrayAdapter<Chat> {
    Context context;

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

        usernameTV.setText(currentChat.getUser().getUsername());
        lastMessageTV.setText(currentChat.getLastMessage().getMessage());
        timeTV.setText(time);
        if (currentChat.getLastMessage().getSeen() == 1 && currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber))
            seenImageView.setImageResource(R.drawable.ic_baseline_done_all_24);
        else if (!currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber))
            seenImageView.setVisibility(View.GONE);
        if (!currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber) && currentChat.getLastMessage().getSeen() == 0) {
            UnReadImageView.setVisibility(View.VISIBLE);
            lastMessageTV.setTextColor(view.getResources().getColor(R.color.colorUnRead));
            timeTV.setTextColor(view.getResources().getColor(R.color.colorUnRead));
        } else
            UnReadImageView.setVisibility(View.INVISIBLE);
        return view;
    }
}
