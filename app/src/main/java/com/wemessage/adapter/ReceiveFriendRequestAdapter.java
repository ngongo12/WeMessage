package com.wemessage.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.R;
import com.wemessage.fragment.ReceiveFriendRequestFragment;
import com.wemessage.model.FriendInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReceiveFriendRequestAdapter extends RecyclerView.Adapter<ReceiveFriendRequestAdapter.Holder> {
    ArrayList<String> list;
    Activity context;
    DatabaseReference myRequestRef,userRef, requestRef, friendRef;
    ReceiveFriendRequestFragment requestFragment;

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    String currentUserId;



    public ReceiveFriendRequestAdapter(ArrayList<String> list, Activity context, ReceiveFriendRequestFragment requestFragment, String currentUserId) {
        this.list=list;
        this.context = context;
        this.requestFragment = requestFragment;
        this.currentUserId = currentUserId;
        myRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(currentUserId);
        requestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }


    @NonNull
    @Override
    public ReceiveFriendRequestAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_friend_request_list_layout_2, parent,false);

        return (new Holder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiveFriendRequestAdapter.Holder holder, int position) {
        String friendID = list.get(position);
        userRef.child(friendID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.tvName.setText(snapshot.child("name").getValue().toString());
                if (snapshot.hasChild("avatar"))
                {
                    Glide.with(context).load(snapshot.child("avatar").getValue().toString()).into(holder.ivAvatar);
                }
                if (snapshot.hasChild("status")){
                    holder.tvInfo.setText(snapshot.child("status").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.ivCancel.setOnClickListener(new View.OnClickListener() {
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

        holder.btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Chấp nhận kết bạn với: " + holder.tvName.getText().toString(), BaseTransientBottomBar.LENGTH_SHORT)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                acceptRequest(friendID);
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

    public void acceptRequest(String friendId)
    {
        String date = sdf.format(new Date());
        Map mapFriend = new HashMap();
        mapFriend.put(currentUserId+"/"+friendId+"/date", date);
        mapFriend.put(friendId+"/"+currentUserId+"/date", date);
        friendRef.updateChildren(mapFriend)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
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
                });
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView ivAvatar,ivCancel;
        TextView tvName, tvInfo;
        Button btnRequest;
        RelativeLayout layout;
        public Holder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivCancel = itemView.findViewById(R.id.ivCancel);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            btnRequest = itemView.findViewById(R.id.btnRequest);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
