package com.wemessage.fragment;

import android.content.Intent;
import android.os.Bundle;
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
import com.wemessage.ChatWithGroupActivity;
import com.wemessage.R;
import com.wemessage.adapter.GroupSuggestionAdapter;
import com.wemessage.adapter.MyGroupAdapter;

import java.util.ArrayList;

public class GroupSuggestionFragment extends Fragment {

    RecyclerView rcv;
    LinearLayout layout;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    DatabaseReference usersRef, groupRef;

    ArrayList<String> list;
    GroupSuggestionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_sugesstion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Ánh xạ các view
        rcv = getView().findViewById(R.id.rcv);
        layout = getView().findViewById(R.id.layout);

        //Set layout cho rcv
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rcv.setLayoutManager(layoutManager);

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        list = new ArrayList<>();
        adapter = new GroupSuggestionAdapter(list, this, currentUserId, getContext());
        rcv.setAdapter(adapter);


        displayRCV();
    }

    private void displayRCV() {
        groupRef.orderByChild("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    if (dataSnapshot.hasChild("members"))
                    {
                        if (!dataSnapshot.child("members").hasChild(currentUserId))
                        {
                            list.add(dataSnapshot.getKey());
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                hideLayout();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void gotoChatActivity(String groupId)
    {
        Intent intent = new Intent(getActivity(), ChatWithGroupActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
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
