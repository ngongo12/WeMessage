package com.wemessage.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
import com.wemessage.CreateGroupActivity;
import com.wemessage.R;
import com.wemessage.fragment.ReceiveFriendRequestFragment;
import com.wemessage.model.FriendInfo;

import java.util.ArrayList;
import java.util.Map;

public class FriendsForGroupAdapter extends RecyclerView.Adapter<FriendsForGroupAdapter.Holder> {
    ArrayList<FriendInfo> list;
    Activity context;

    String currentUserId;
    Map mapChoosen;



    public FriendsForGroupAdapter(ArrayList<FriendInfo> list, Activity context, Map map) {
        this.list=list;
        this.context = context;
        this.currentUserId = currentUserId;
        this.mapChoosen = map;

    }


    @NonNull
    @Override
    public FriendsForGroupAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_friend_for_group_list_layout, parent,false);

        return (new Holder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsForGroupAdapter.Holder holder, int position) {
        if (list.get(position).getAvatar() != null)
        {
            Glide.with(context).load(list.get(position).getAvatar()).into(holder.ivAvatar);
        }
        else
        {
            Glide.with(context).load(R.drawable.default_avatar).into(holder.ivAvatar);
        }
        if (list.get(position).getName() != null)
        {
            holder.tvName.setText(list.get(position).getName());
        }
        if (list.get(position).getStatus() != null)
        {
            holder.tvInfo.setText(list.get(position).getStatus());
        }

        if (mapChoosen.containsKey(list.get(position).getUid()))
        {
            holder.chk.setChecked(true);
        }
        else
        {
            holder.chk.setChecked(false);
        }

        holder.chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    //thêm vào map
                    mapChoosen.put(list.get(position).getUid(), "");
                }
                else
                {
                    mapChoosen.remove(list.get(position).getUid());
                }

                ((CreateGroupActivity) context).getChoosenMap(mapChoosen);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView ivAvatar,ivCancel;
        TextView tvName, tvInfo;
        CheckBox chk;
        RelativeLayout layout;
        public Holder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivCancel = itemView.findViewById(R.id.ivCancel);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            chk = itemView.findViewById(R.id.chk);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
