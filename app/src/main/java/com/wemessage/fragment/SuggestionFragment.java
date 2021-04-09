package com.wemessage.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.R;
import com.wemessage.adapter.SuggestionAdapter;
import com.wemessage.model.FriendInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SuggestionFragment extends Fragment {

    RecyclerView rcv;

    ArrayList<FriendInfo> list;
    SuggestionAdapter adapter;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    DatabaseReference usersRef, requestRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestion, container, false);
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
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        list = new ArrayList<>();
        adapter = new SuggestionAdapter(getActivity(), list, SuggestionFragment.this, currentUserId);
        rcv.setAdapter(adapter);

        displayRCV();
    }

    public void displayRCV()
    {

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot data : snapshot.getChildren())
                {
                    String key = data.getKey();
                    if(!data.getKey().equals(currentUserId))
                    {
                        FriendInfo item = data.getValue(FriendInfo.class);
                        //Log.d("Loi", "Suggestion: " + item.getName());
                        list.add(item);
                    }
                    //Log.d("Loi", "Suggestion: " + list.size());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void addRequest(String friendId)
    {
        //Thêm vào friend request của myId với giá trị type send
        requestRef.child(currentUserId).child(friendId).child("type").setValue("send")
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getContext(), "Gửi yêu cầu kết bạn thành công", Toast.LENGTH_SHORT).show();
                }
            });
        //Thêm vào friend request của myId với giá trị type received
        requestRef.child(friendId).child(currentUserId).child("type").setValue("receive");
    }
}
