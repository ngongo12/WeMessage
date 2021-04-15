package com.wemessage.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.R;
import com.wemessage.fragment.SendFrendRequestFragment;

import java.util.ArrayList;

public class SendFriendRequestAdapter extends RecyclerView.Adapter<SendFriendRequestAdapter.Holder> {
    ArrayList<String> list;
    Activity context;
    DatabaseReference myRequestRef,userRef, requestRef;
    SendFrendRequestFragment sendFragment;

    String currentUserId;


    public SendFriendRequestAdapter(ArrayList<String> list , Activity context , SendFrendRequestFragment sendFragment, String currentUserId) {
        this.list = list;
        this.context = context;
        this.sendFragment = sendFragment;
        this.currentUserId = currentUserId;
        myRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(currentUserId);
        requestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public SendFriendRequestAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_friend_request_list_send_layout, parent,false);

        return (new SendFriendRequestAdapter.Holder(view));
    }
    @Override
    public void onBindViewHolder(@NonNull SendFriendRequestAdapter.Holder holder, int position) {
        String friendID = list.get(position);
        userRef.child(friendID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.tvName.setText(snapshot.child("name").getValue().toString());
                if (snapshot.hasChild("avatar"))
                {
                    try {
                        Glide.with(context).load(snapshot.child("avatar").getValue().toString()).into(holder.ivAvatar);
                    }
                    catch (Exception e)
                    {
                        Log.d("Loi", "Glide: " + e.toString());
                    }
                }
                if (snapshot.hasChild("status")){
                    holder.tvInfo.setText(snapshot.child("status").getValue().toString());
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Hủy yêu cầu kết bạn với: " + holder.tvName.getText().toString(), BaseTransientBottomBar.LENGTH_SHORT)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelRequest(friendID);
                            }
                        })
                        .setTextColor(context.getResources().getColor(R.color.snackbar_text))
                        .setActionTextColor(context.getResources().getColor(R.color.snackbar_text))
                        .show();
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
        Button btnCancel;
        RelativeLayout layout;
        public Holder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            layout = itemView.findViewById(R.id.layout);
        }
    }

    public void cancelRequest(String friendId)
    {
        //Xóa trạng thái request từ id của mình
        myRequestRef.child(friendId).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                requestRef.child(friendId).child(currentUserId)
                        .removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                Toast.makeText(context, "Xóa yêu cầu kết bạn thành công", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
