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

import java.util.List;
import java.util.Objects;

public class MessagesAdapter extends ArrayAdapter<Message> {
    Context context;
    String username;

    public MessagesAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects, String username) {
        super(context, resource, objects);
        this.context = context;
        this.username = username;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        String userPhoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        Message currentMessage = getItem(position);
        final String[] splitNumber = userPhoneNumber.split("\\+2");
        userPhoneNumber = splitNumber[1];
        if (Objects.requireNonNull(currentMessage).getSenderPhone().equals(userPhoneNumber)) {
            view = View.inflate(context, R.layout.my_message, null);
            TextView message = view.findViewById(R.id.myMessageTextView);
            message.setText(currentMessage.getMessage());

            TextView timeTV = view.findViewById(R.id.timeMyMessageTV);
            String time = currentMessage.getTime();
            if (time.charAt(1) == ':')
                time += "AM";
            else {
                String hours = time.substring(0, 2);
                String minutes = time.substring(2);
                int nHours = Integer.parseInt(hours) - 12;
                time = nHours + minutes + "PM";
            }
            timeTV.setText(time);
            if (currentMessage.getSeen() == 1) {
                ImageView seenImage = view.findViewById(R.id.seenImage);
                seenImage.setImageResource(R.drawable.ic_baseline_done_all_24);
            }

        } else {
            view = View.inflate(context, R.layout.their_message, null);
            TextView message = view.findViewById(R.id.theirMessageTV);
            message.setText(currentMessage.getMessage());
            TextView usernameTV = view.findViewById(R.id.usernameMessageTV);
            usernameTV.setText(username);
            TextView timeTV = view.findViewById(R.id.timeTheirMessageTV);
            String time = currentMessage.getTime();
            if (time.charAt(1) == ':')
                time += "AM";
            else {
                String hours = time.substring(0, 2);
                String minutes = time.substring(2);
                int nHours = Integer.parseInt(hours) - 12;
                time = nHours + minutes + "PM";
            }
            timeTV.setText(time);
        }
        return view;
    }
}
