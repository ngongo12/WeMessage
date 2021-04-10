package com.wemessage.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.R;
import com.wemessage.model.FriendInfo;
import com.wemessage.model.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReadFriendMessageAdapter extends RecyclerView.Adapter<ReadFriendMessageAdapter.MessageHolder> {

    String currentUserId, friendId;
    FriendInfo friendInfo;
    Context context;
    ArrayList<Message> list;

    DatabaseReference userRef;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public ReadFriendMessageAdapter(ArrayList<Message> list,String currentUserId, String friendId, FriendInfo friendInfo, Context context) {
        this.currentUserId = currentUserId;
        this.friendInfo = friendInfo;
        this.context = context;
        this.friendId = friendId;
        this.list = list;
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_box, parent, false);
        return new ReadFriendMessageAdapter.MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        holder.tvTime.setText(list.get(position).getTime());

        if (list.get(position).getFrom().equals(currentUserId))
        {
            //Là tin nhắn của mình thì ẩn avatar
            holder.cover.setVisibility(View.GONE);
        }
        else
        {
            holder.cover.setVisibility(View.VISIBLE);
        }

        if(list.get(position).getType().equals("text"))
        {
            //Hiện layout text
            //holder.layout_text.setLayoutParams(holder.paramsHien);
            holder.layout_text.setVisibility(View.VISIBLE);
            if(list.get(position).getFrom().equals(currentUserId))
            {
                holder.tvSend.setText(list.get(position).getMessage());
                holder.tvReceive.setVisibility(View.INVISIBLE);
            }
            else
            {
                holder.tvReceive.setText(list.get(position).getMessage());
                holder.tvSend.setVisibility(View.INVISIBLE);
            }
        }
        else if(list.get(position).getType().equals("image"))
        {
            //Hiện layout image
            //holder.layout_img.setLayoutParams(holder.paramsHien);
            holder.layout_img.setVisibility(View.VISIBLE);
            if(list.get(position).getFrom().equals(currentUserId))
            {
                Glide.with(context).load(list.get(position).getMessage()).into(holder.ivSend);
                holder.ivReceive.setVisibility(View.GONE);
            }
            else
            {
                Glide.with(context).load(list.get(position).getMessage()).into(holder.ivReceive);
                holder.ivSend.setVisibility(View.GONE);
            }
        }

        //Test
        holder.layout_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Loi", "pos: " + position);
                Log.d("Loi", "mes: " + list.get(position).getMessage());
                Log.d("Loi", "type: " + list.get(position).getType());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView tvReceive, tvSend, tvTime;
        RelativeLayout layout_text, layout_img;
        LinearLayout layout_all;
        ImageView ivReceive, ivSend, ivAvater;
        CardView cover;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            tvReceive = itemView.findViewById(R.id.tvReceive);
            tvSend = itemView.findViewById(R.id.tvSend);
            tvTime = itemView.findViewById(R.id.tvTime);
            cover = itemView.findViewById(R.id.cover);
            layout_text = itemView.findViewById(R.id.layout_text);
            layout_img = itemView.findViewById(R.id.layout_img);
            layout_all = itemView.findViewById(R.id.layout_all);
            ivReceive = itemView.findViewById(R.id.ivReceive);
            ivSend = itemView.findViewById(R.id.ivSend);
            ivAvater = itemView.findViewById(R.id.ivAvatar);



            //Ẩn các layout
            layout_img.setVisibility(View.GONE);
            layout_text.setVisibility(View.GONE);

            tvTime.setVisibility(View.GONE);

            userRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChild("avatar"))
                    {
                        Glide.with(context).load(snapshot.child("avatar").getValue().toString()).into(ivAvater);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            layout_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //tvTime.setVisibility(View.VISIBLE);
                }
            });

            layout_all.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                    {
                        tvTime.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        tvTime.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
