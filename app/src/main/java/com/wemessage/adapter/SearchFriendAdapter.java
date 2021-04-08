package com.wemessage.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.wemessage.model.FriendInfo;

import java.util.ArrayList;

public class SearchFriendAdapter extends RecyclerView.Adapter<SearchFriendAdapter.Holder> {
    Activity context;
    ArrayList<FriendInfo> list;
    DatabaseReference userRef;
    public SearchFriendAdapter(Activity context, ArrayList<FriendInfo> list) {
        this.context = context;
        this.list = list;
        Log.d("Loi", "MyFriendAdapter: " + list.size());
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_friend_search, parent,false);
        return (new Holder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (list.get(position).getAvatar() != null)
        {
            Glide.with(context).load(list.get(position).getAvatar()).into(holder.ivAvatar);
        }
        if (list.get(position).getName() != null)
        {
            holder.tvName.setText(list.get(position).getName());
        }
        if (list.get(position).getStatus() != null)
        {
            holder.tvStatus.setText(list.get(position).getStatus());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public class Holder extends RecyclerView.ViewHolder {

        ImageView ivAvatar;
        TextView tvName, tvStatus;
        RelativeLayout layout;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
