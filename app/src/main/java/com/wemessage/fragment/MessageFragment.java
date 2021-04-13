package com.wemessage.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.ChatWithFriendActivity;
import com.wemessage.CreateGroupActivity;
import com.wemessage.R;
import com.wemessage.adapter.FriendMessageAdapter;
import com.wemessage.model.FriendInfo;

import java.util.ArrayList;

public class MessageFragment extends Fragment {

    RecyclerView rcv;
    LinearLayout layout_create_group;
    ShimmerFrameLayout shimmer;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    DatabaseReference messRef;

    ArrayList<String> list;

    FriendMessageAdapter adapter;

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
        layout_create_group = getView().findViewById(R.id.layout_create_group);
        shimmer = getView().findViewById(R.id.shimmer);

        //Set layout cho rcv
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList();

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        messRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        adapter = new FriendMessageAdapter(list,MessageFragment.this, currentUserId, getContext());
        rcv.setAdapter(adapter);


        //Thực hiện query
        messRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot data : snapshot.getChildren())
                {
                    list.add(data.getKey());
                }

                adapter.notifyDataSetChanged();

                //Dừng và ẩn shimmer
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        layout_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
                startActivity(intent);
            }
        });
    }

    public void gotoChatActivity(String id)
    {
        Intent intent = new Intent(getActivity(), ChatWithFriendActivity.class);
        intent.putExtra("myFriendId", id);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmer.startShimmer();
    }

    @Override
    public void onPause() {
        shimmer.stopShimmer();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
