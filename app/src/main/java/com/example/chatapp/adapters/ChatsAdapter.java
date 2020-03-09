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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        String userPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(2);
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
        } catch (Exception pe) {
            pe.printStackTrace();
        }

        if(!currentChat.getUser().getImage().equals(""))
            Picasso.with(context).load(currentChat.getUser().getImage()).into(imageView);
        usernameTV.setText(currentChat.getUser().getUsername());
        lastMessageTV.setText(currentChat.getLastMessage().getMessage());
        timeTV.setText(time);
        try {
            if (currentChat.getLastMessage().getSeeners().equals("All")&&currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber))
                seenImageView.setImageResource(R.drawable.ic_baseline_done_all_24);
            if (!currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber))
                seenImageView.setVisibility(View.GONE);
              if(currentChat.getLastMessage().getSeeners().equals("All")||currentChat.getLastMessage().getSeeners().contains(userPhoneNumber)||
                currentChat.getLastMessage().getSenderPhone().equals(userPhoneNumber))
                UnReadImageView.setVisibility(View.INVISIBLE);
            else {
                UnReadImageView.setVisibility(View.VISIBLE);
                lastMessageTV.setTextColor(view.getResources().getColor(R.color.colorUnRead));
                timeTV.setTextColor(view.getResources().getColor(R.color.colorUnRead));
            }
        }
        catch (Exception n){ }
        if(currentChat.getLastMessage().getTime().equals("")){
            seenImageView.setVisibility(View.GONE);
            UnReadImageView.setVisibility(View.GONE);
            lastMessageTV.setTextColor(view.getResources().getColor(R.color.colorGray));
            timeTV.setVisibility(View.GONE);
        }
        if(currentChat.getLastMessage().getMessage().contains("imagesFolder")){
            ImageView mediaImage = view.findViewById(R.id.mediaChatImage);
            lastMessageTV.setVisibility(View.INVISIBLE);
            mediaImage.setVisibility(View.VISIBLE);
        }
        else if(currentChat.getLastMessage().getMessage().contains("recordsFolder")){
            ImageView mediaImage = view.findViewById(R.id.mediaChatImage);
            lastMessageTV.setVisibility(View.INVISIBLE);
            mediaImage.setImageResource(R.drawable.record_media);
            mediaImage.setVisibility(View.VISIBLE);
        }
        return view;
    }
}
