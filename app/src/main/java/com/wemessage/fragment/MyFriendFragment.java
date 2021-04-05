package com.wemessage.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.R;
import com.wemessage.adapter.MyFriendAdapter;

import java.util.ArrayList;

public class MyFriendFragment extends Fragment {
    RecyclerView rcv;
    ArrayList<String> list;

    MyFriendAdapter adapter;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    DatabaseReference friendRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myfriend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Ánh xạ các view
        rcv = getView().findViewById(R.id.rcv);

        //Set layout cho rcv
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rcv.setLayoutManager(layoutManager);

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        displayRCV();

    }
    public void displayRCV()
    {
        list = new ArrayList<>();
        friendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot data : snapshot.getChildren())
                {
                    if(!data.getKey().equals(currentUserId))
                    {
                        String friendId = data.getKey().toString();
                        list.add(friendId);
                    }
                    Log.d("Loi", "MyFriend: " + list.size());
                }
                adapter = new MyFriendAdapter(getActivity(), list);
                rcv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
