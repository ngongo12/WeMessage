package com.wemessage.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.wemessage.R;
import com.wemessage.model.FriendInfo;
import com.wemessage.model.Message;

public class ReadFriendMessageAdapter extends FirebaseRecyclerAdapter<Message, ReadFriendMessageAdapter.MessageHolder> {

    String currentUserId;
    FriendInfo friendInfo;
    Context context;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ReadFriendMessageAdapter(@NonNull FirebaseRecyclerOptions<Message> options, String currentUserId, FriendInfo friendInfo, Context context) {
        super(options);
        this.currentUserId = currentUserId;
        this.friendInfo = friendInfo;
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull Message model) {
        holder.tvTime.setText(model.getTime());

        //Nếu là tin nhắn của mình thì ẩn avatar
        if(model.getFrom().equals(currentUserId))
        {
            holder.cover.setVisibility(View.INVISIBLE);
        }

        if(model.getType().equals("text"))
        {
            Log.d("Loi", "onBindViewHolder: " + model.getMessage());
            //Hiện layout text
            holder.layout_text.setLayoutParams(holder.paramsHien);
            if(model.getFrom().equals(currentUserId))
            {
                holder.tvSend.setText(model.getMessage());
                holder.tvReceive.setVisibility(View.INVISIBLE);
            }
            else
            {
                holder.tvReceive.setText(model.getMessage());
                holder.tvSend.setVisibility(View.INVISIBLE);
            }
        }
        if(model.getType().equals("image"))
        {
            //Hiện layout image
            holder.layout_img.setLayoutParams(holder.paramsHien);
            if(model.getFrom().equals(currentUserId))
            {
                Glide.with(context).load(model.getMessage()).into(holder.ivSend);
                holder.ivReceive.setVisibility(View.GONE);
            }
            else
            {
                Glide.with(context).load(model.getMessage()).into(holder.ivReceive);
                holder.ivSend.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_box, parent, false);
        return new ReadFriendMessageAdapter.MessageHolder(view);
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView tvReceive, tvSend, tvTime;
        RelativeLayout layout_text, layout_img;
        ImageView ivReceive, ivSend;
        CardView cover;
        LinearLayout.LayoutParams paramsAn, paramsHien;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            tvReceive = itemView.findViewById(R.id.tvReceive);
            tvSend = itemView.findViewById(R.id.tvSend);
            tvTime = itemView.findViewById(R.id.tvTime);
            cover = itemView.findViewById(R.id.cover);
            layout_text = itemView.findViewById(R.id.layout_text);
            layout_img = itemView.findViewById(R.id.layout_img);
            ivReceive = itemView.findViewById(R.id.ivReceive);
            ivSend = itemView.findViewById(R.id.ivSend);

            paramsAn = (LinearLayout.LayoutParams) layout_img.getLayoutParams();
            paramsHien = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsAn.height = 0;

            //Ẩn các layout
            layout_img.setLayoutParams(paramsAn);
            layout_text.setLayoutParams(paramsAn);
        }
    }
}
