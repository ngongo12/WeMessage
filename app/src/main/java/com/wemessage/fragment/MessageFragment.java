package com.wemessage.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wemessage.ChatWithFriendActivity;
import com.wemessage.R;
import com.wemessage.adapter.FriendAdapter;
import com.wemessage.model.FriendInfo;

public class MessageFragment extends Fragment {

    RecyclerView rcv;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    DatabaseReference friendRef;

    FriendAdapter friendAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Ánh xạ các view
        rcv = getView().findViewById(R.id.rcv);

        //Set layout cho rcv
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        friendRef = FirebaseDatabase.getInstance().getReference().child("Users");


        //Thực hiện query
        FirebaseRecyclerOptions<FriendInfo> options = new FirebaseRecyclerOptions.Builder<FriendInfo>()
                .setQuery(friendRef.orderByKey(), FriendInfo.class)
                .build();

        friendAdapter = new FriendAdapter(options, MessageFragment.this, currentUserId);

        rcv.setAdapter(friendAdapter);
    }

    public void gotoChatActivity(String id)
    {
        Intent intent = new Intent(getActivity(), ChatWithFriendActivity.class);
        intent.putExtra("myFriendId", id);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        friendAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        friendAdapter.startListening();
    }
}
