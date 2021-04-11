package com.wemessage.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.R;
import com.wemessage.fragment.MessageFragment;
import com.wemessage.fragment.MyGroupFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyGroupAdapter extends RecyclerView.Adapter<MyGroupAdapter.Holder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    Context context;
    String  currentUserId;
    MyGroupFragment fragment;
    ArrayList<String> list;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    DatabaseReference messRef, userRef, groupRef;

    public MyGroupAdapter(ArrayList<String> list, MyGroupFragment fragment, String currentUserId, Context context) {
        this.fragment = fragment;
        this.currentUserId = currentUserId;
        this.list = list;
        this.context = context;
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef= FirebaseDatabase.getInstance().getReference().child("Groups");
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_message_layout, parent, false);
        return new MyGroupAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String id = list.get(position);
        messRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(id).child("messages");
        //Set ten
        groupRef.child(id).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    holder.tvName.setText(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Set avatar
        groupRef.child(id).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int index = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    String uid = dataSnapshot.getKey();
                    userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(index == 0) {
                                if (snapshot.hasChild("avatar")) {
                                    Glide.with(context).load(snapshot.child("avatar").getValue().toString()).into(holder.ivAvatar);
                                }
                            }
                            else
                            {
                                if (snapshot.hasChild("avatar")) {
                                    Glide.with(context).load(snapshot.child("avatar").getValue().toString()).into(holder.ivAvatarR);
                                }
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


        //Set message
        messRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Chưa có tin nhắn nào
                if (snapshot.getChildrenCount()<1)
                {
                    holder.tvMessage.setVisibility(View.GONE);
                    return;
                }
                else
                {
                    holder.tvMessage.setVisibility(View.VISIBLE);
                }
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    //Phần nội dung tin nhắn
                    String text = "";

                    //tin nhắn của mình
                    if (dataSnapshot.child("from").getValue().toString().equals(currentUserId))
                    {
                        text += "Bạn: ";
                    }
                    if (dataSnapshot.child("type").getValue().toString().equals("image"))
                    {
                        text += "[Hình ảnh]";
                    }
                    if (dataSnapshot.child("type").getValue().toString().equals("text"))
                    {
                        text += dataSnapshot.child("message").getValue().toString();
                    }
                    holder.tvMessage.setText(text);

                    //Phần thời gian
                    //Thời gian cuối cùng tin nhắn được gửi
                    Date timeSend;
                    try {
                        timeSend = sdf.parse(dataSnapshot.child("time").getValue().toString());
                    } catch (ParseException e) {
                        //Lấy mặc định
                        timeSend = new Date();
                        e.printStackTrace();
                    }

                    //Xử lý tvTime
                    Long time = (new Date()).getTime() - timeSend.getTime();
                    //Trả về miligiây

                    //Trả về phút
                    time = time / 60/1000;

                    if (time <= 60)
                    {
                        holder.tvTime.setText(time+" phút");
                    }
                    else {
                        //trả về giờ
                        time /= 60;
                        if (time <= 24) {
                            holder.tvTime.setText(time + " giờ");
                        }
                        else {
                            //trả về tuần
                            time /= 24;
                            if (time <= 7) {
                                holder.tvTime.setText(time + " ngày");
                            } else {
                                holder.tvTime.setText(new SimpleDateFormat("dd/MM/yy").format(timeSend));
                            }
                        }
                    }

                    Date finalTimeSend = timeSend;
                    messRef.child(currentUserId).child(id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild("last_seen"))
                            {
                                Date lastSeen;
                                try {
                                    lastSeen = sdf.parse(snapshot.child("last_seen").getValue().toString());
                                } catch (ParseException e) {
                                    lastSeen = new Date();
                                    e.printStackTrace();
                                }

                                if(lastSeen.compareTo(finalTimeSend) < 0) //Chưa xem
                                {
                                    holder.tvMessage.setTypeface(Typeface.DEFAULT_BOLD);
                                    holder.tvName.setTypeface(Typeface.DEFAULT_BOLD);
                                }
                                else
                                {
                                    holder.tvMessage.setTypeface(Typeface.DEFAULT);
                                    holder.tvName.setTypeface(Typeface.DEFAULT);
                                }
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

        /*holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.gotoChatActivity(list.get(position));
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class Holder extends RecyclerView.ViewHolder
    {
        RelativeLayout layout;
        TextView tvName, tvMessage, tvTime;
        ImageView ivAvatar, ivAvatarR;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            layout = itemView.findViewById(R.id.layout);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivAvatarR = itemView.findViewById(R.id.ivAvatarR);
        }
    }
}
