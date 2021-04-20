package com.wemessage.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.ChatWithFriendActivity;
import com.wemessage.ChatWithGroupActivity;
import com.wemessage.R;
import com.wemessage.model.Messages;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ReadGroupMessageAdapter extends RecyclerView.Adapter<ReadGroupMessageAdapter.MessageHolder> {

    String currentUserId, groupId;
    Context context;
    ArrayList<Messages> list;

    DatabaseReference userRef;

    //audio
    MediaPlayer player;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    public ReadGroupMessageAdapter(ArrayList<Messages> list, String currentUserId, String groupId, Context context) {
        this.currentUserId = currentUserId;
        this.context = context;
        this.groupId = groupId;
        this.list = list;
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_box, parent, false);
        return new ReadGroupMessageAdapter.MessageHolder(view);
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
            //Lấy avatar
            userRef.child(list.get(position).getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChild("avatar"))
                    {
                        Glide.with(context).load(snapshot.child("avatar").getValue().toString()).into(holder.ivAvater);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        if(list.get(position).getType().equals("text"))
        {
            //Hiện layout text
            holder.layout_text.setVisibility(View.VISIBLE);
            holder.layout_img.setVisibility(View.GONE);
            holder.layout_audio.setVisibility(View.GONE);
            if(list.get(position).getFrom().equals(currentUserId))
            {
                holder.tvSend.setText(list.get(position).getMessage());
                holder.tvSend.setVisibility(View.VISIBLE);
                holder.tvReceive.setVisibility(View.GONE);
                holder.tvSend.setTypeface(null, Typeface.NORMAL);
            }
            else
            {
                holder.tvReceive.setText(list.get(position).getMessage());
                holder.tvReceive.setVisibility(View.VISIBLE);
                holder.tvSend.setVisibility(View.GONE);
                holder.tvReceive.setTypeface(null, Typeface.NORMAL);
            }
        }
        else if(list.get(position).getType().equals("image"))
        {
            //Hiện layout image
            holder.layout_img.setVisibility(View.VISIBLE);
            holder.layout_text.setVisibility(View.GONE);
            holder.layout_audio.setVisibility(View.GONE);
            if(list.get(position).getFrom().equals(currentUserId))
            {
                Glide.with(context).load(list.get(position).getMessage()).into(holder.ivSend);
                holder.ivSend.setVisibility(View.VISIBLE);
                holder.ivReceive.setVisibility(View.GONE);
            }
            else
            {
                Glide.with(context).load(list.get(position).getMessage()).into(holder.ivReceive);
                holder.ivReceive.setVisibility(View.VISIBLE);
                holder.ivSend.setVisibility(View.GONE);
            }
        }

        else if (list.get(position).getType().equals("audio"))
        {
            //Hiện layout audio
            holder.layout_img.setVisibility(View.GONE);
            holder.layout_text.setVisibility(View.GONE);
            holder.layout_audio.setVisibility(View.VISIBLE);

            if(list.get(position).getFrom().equals(currentUserId))
            {
                holder.ivSpeakerL.setVisibility(View.GONE);
                holder.ivSpeakerR.setVisibility(View.VISIBLE);
                holder.ivSpeakerR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        readAudio(list.get(position).getMessage());
                    }
                });
            }
            else
            {
                holder.ivSpeakerL.setVisibility(View.VISIBLE);
                holder.ivSpeakerR.setVisibility(View.GONE);
                holder.ivSpeakerL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        readAudio(list.get(position).getMessage());
                    }
                });
            }

        }
        else if(list.get(position).getType().equals("hide"))
        {
            holder.layout_text.setVisibility(View.VISIBLE);
            holder.layout_img.setVisibility(View.GONE);
            holder.layout_audio.setVisibility(View.GONE);
            if(list.get(position).getFrom().equals(currentUserId))
            {
                //Ẩn hết
                holder.tvSend.setText(list.get(position).getMessage());
                holder.tvSend.setVisibility(View.GONE);
                holder.tvReceive.setVisibility(View.GONE);
                holder.tvSend.setTypeface(null, Typeface.ITALIC);
            }
            else
            {
                holder.tvReceive.setText(list.get(position).getMessage());
                holder.tvReceive.setVisibility(View.VISIBLE);
                holder.tvSend.setVisibility(View.GONE);
                holder.tvReceive.setText("\"Tin nhắn đã bị thu hồi\"");
                holder.tvReceive.setTypeface(null, Typeface.ITALIC);
            }

        }

        holder.layout_all.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((ChatWithGroupActivity)context).getMessage(list.get(position));
                return false;
            }
        });

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

    public void readAudio(String uri)
    {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(uri);
            player.prepare();
            player.start();
        }
        catch (IOException e) {
            Log.e("Loi", "prepare() failed");
        }
    }

    public void stopAudio()
    {
        player.release();
        player = null;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MessageHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        TextView tvReceive, tvSend, tvTime;
        RelativeLayout layout_text, layout_img, layout_audio;
        LinearLayout layout_all;
        ImageView ivReceive, ivSend, ivAvater, ivSpeakerL, ivSpeakerR;
        CardView cover;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnCreateContextMenuListener(this);

            tvReceive = itemView.findViewById(R.id.tvReceive);
            tvSend = itemView.findViewById(R.id.tvSend);
            tvTime = itemView.findViewById(R.id.tvTime);
            cover = itemView.findViewById(R.id.cover);
            layout_text = itemView.findViewById(R.id.layout_text);
            layout_img = itemView.findViewById(R.id.layout_img);
            layout_audio = itemView.findViewById(R.id.layout_audio);
            layout_all = itemView.findViewById(R.id.layout_all);
            ivReceive = itemView.findViewById(R.id.ivReceive);
            ivSend = itemView.findViewById(R.id.ivSend);
            ivSpeakerL = itemView.findViewById(R.id.ivSpeakerL);
            ivSpeakerR = itemView.findViewById(R.id.ivSpeakerR);
            ivAvater = itemView.findViewById(R.id.ivAvatar);



            //Ẩn các layout
            layout_img.setVisibility(View.GONE);
            layout_text.setVisibility(View.GONE);
            layout_audio.setVisibility(View.GONE);

            tvTime.setVisibility(View.GONE);



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

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Bạn muốn xóa?");
            menu.add(Menu.NONE, R.id.delete_message,Menu.NONE, "Xóa tin nhắn");
        }
    }
}