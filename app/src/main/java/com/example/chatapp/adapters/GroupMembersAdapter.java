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

public class GroupMembersAdapter extends ArrayAdapter<User> {
    Context context;

    public GroupMembersAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.contact_member_item, null);
        User currentUser = getItem(position);
        TextView usernameTV = view.findViewById(R.id.usernameMemberTV);
        final ImageView selectImageView = view.findViewById(R.id.selectedImageView);
        ImageView userImageView = view.findViewById(R.id.imageView2);

        usernameTV.setText(currentUser.getUsername());
        selectImageView.setVisibility(View.INVISIBLE);
        return view;
    }
}
