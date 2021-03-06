package com.wemessage.fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import com.wemessage.adapter.SendFriendRequestAdapter;

import java.util.ArrayList;

public class SendFrendRequestFragment extends Fragment {
    RecyclerView rcv;
    LinearLayout layout;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    DatabaseReference usersRef, requestRef;
    SendFriendRequestAdapter adapter;
    ArrayList<String> list;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_send_friend_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rcv = getView().findViewById(R.id.rcv);
        layout = getView().findViewById(R.id.layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rcv.setLayoutManager(layoutManager);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        list= new ArrayList<>();

        displayRcv();

    }
    public void displayRcv(){
        requestRef.child(currentUserId).orderByChild("type").equalTo("send").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    list.add(dataSnapshot.getKey());

                }
                Log.d("Loi", "onDataChange: size " + list.size());
                adapter=new SendFriendRequestAdapter(list, getActivity(),SendFrendRequestFragment.this,currentUserId);
                rcv.setAdapter(adapter);
                hideLayout();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void hideLayout()
    {
        if (list.size() == 0)
        {
            layout.setVisibility(View.GONE);
        }
        else
        {
            layout.setVisibility(View.VISIBLE);
        }
    }
}
