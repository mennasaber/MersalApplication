package com.example.chatapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.modules.User;

import java.util.ArrayList;


public class ContactsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        // TODO: scan all contacts
        ArrayList<User> users= scanAllContacts();
        return view;
    }

    private ArrayList<User> scanAllContacts() {
        ArrayList<User> users = new ArrayList<>();

        return users;
    }

}