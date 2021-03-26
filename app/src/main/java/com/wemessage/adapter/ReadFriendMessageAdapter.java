package com.wemessage.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.wemessage.R;
import com.wemessage.model.FriendInfo;
import com.wemessage.model.Message;

public class ReadFriendMessageAdapter extends FirebaseRecyclerAdapter<Message, ReadFriendMessageAdapter.MessageHolder> {

    String currentUserId;
    FriendInfo friendInfo;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ReadFriendMessageAdapter(@NonNull FirebaseRecyclerOptions<Message> options, String currentUserId, FriendInfo friendInfo) {
        super(options);
        this.currentUserId = currentUserId;
        this.friendInfo = friendInfo;
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
        CardView cover;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            tvReceive = itemView.findViewById(R.id.tvReceive);
            tvSend = itemView.findViewById(R.id.tvSend);
            tvTime = itemView.findViewById(R.id.tvTime);
            cover = itemView.findViewById(R.id.cover);
        }
    }
}
