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

import com.example.chatapp.Models.User;
import com.example.chatapp.R;

import java.util.List;

public class contactsAdapter extends ArrayAdapter<User> {
    private Context context;

    public contactsAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.contact_item, null);
        }
        User currentUser = getItem(position);
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView phoneNumberTextView = view.findViewById(R.id.phoneNumberTextView);
        ImageView userImage = view.findViewById(R.id.imageView);

        usernameTextView.setText(currentUser.getUsername());
        phoneNumberTextView.setText(currentUser.getPhoneNumber());

        return view;
    }
}
