package com.wemessage.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.R;

import java.util.ArrayList;

public class MyFriendAdapter extends RecyclerView.Adapter<MyFriendAdapter.Holder> {
    Activity context;
    ArrayList<String> list;
    DatabaseReference userRef;
    public MyFriendAdapter(Activity context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
        Log.d("Loi", "MyFriendAdapter: " + list.size());
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_online_friend_layout, parent,false);
        return (new Holder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        //list.get(position); là id của friend
        userRef.child(list.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("state"))
                {
                    if(snapshot.child("state").getValue().toString().equals("online"))
                    {
                        //Hiển thị thông tin
                        holder.tvName.setText(snapshot.child("name").getValue().toString());
                        if (snapshot.hasChild("avatar"))
                        {
                            Glide.with(context).load(snapshot.child("avatar").getValue().toString()).into(holder.ivAvatar);
                        }
                    }
                    else
                    {
                        //Ẩn layout
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.layout.getLayoutParams();
                        params.width = 0;
                        holder.layout.setLayoutParams(params);
                        holder.layout.setVisibility(View.GONE);
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public class Holder extends RecyclerView.ViewHolder {

        ImageView ivAvatar;
        TextView tvName, tvInfo;
        LinearLayout layout;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
