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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.R;
import com.wemessage.fragment.SuggestionFragment;
import com.wemessage.model.FriendInfo;

import java.util.ArrayList;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.Holder> {

    Activity context;
    ArrayList<FriendInfo> list;
    SuggestionFragment fragment;
    DatabaseReference myRequestRef, myFriendRef;

    static int numOfHide = 0;

    String currentUserId;

    public SuggestionAdapter(Activity context, ArrayList<FriendInfo> list, SuggestionFragment fragment, String currentUserId) {
        this.context = context;
        this.list = list;
        this.fragment = fragment;

        numOfHide = 0;

        this.currentUserId = currentUserId;
        myRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(currentUserId);
        myFriendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);

    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_friend_request_list_layout, parent,false);
        return (new Holder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.tvName.setText(list.get(position).getName());
        if(list.get(position).getStatus() != null)
            holder.tvInfo.setText(list.get(position).getStatus());

        //Nếu có avatar thì hiển thị không thì mặc định
        if (list.get(position).getAvatar() != null)
        {
            Glide.with(context).load(list.get(position).getAvatar()).into(holder.ivAvatar);
        }
        else
        {
            Glide.with(context).load(R.drawable.default_avatar).into(holder.ivAvatar);
        }

        //Ẩn các user ko có trong yêu cầu kết bạn
        myRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(list.get(position).getUid()))
                {
                    //Nếu có trong danh sách yêu cầu kết bạn thì ẩn đi
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.layout.getLayoutParams();
                    params.height = 0;
                    holder.layout.setLayoutParams(params);

                    numOfHide++;
                }
                else
                {
                    //Ẩn các user có trong friends
                    myFriendRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(list.get(position).getUid()))
                            {
                                //Nếu có trong danh sách yêu cầu kết bạn thì ẩn đi
                                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.layout.getLayoutParams();
                                params.height = 0;
                                holder.layout.setLayoutParams(params);
                                holder.btnRequest.setVisibility(View.GONE);

                                numOfHide++;
                            }
                            else
                            {
                                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.layout.getLayoutParams();
                                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                holder.layout.setLayoutParams(params);
                                holder.btnRequest.setVisibility(View.VISIBLE);
                                numOfHide--;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        holder.btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.addRequest(list.get(position).getUid());
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
        Button btnRequest;
        RelativeLayout layout;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            btnRequest = itemView.findViewById(R.id.btnRequest);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
