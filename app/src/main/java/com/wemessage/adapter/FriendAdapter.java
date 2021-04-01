package com.wemessage.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wemessage.ChatWithFriendActivity;
import com.wemessage.MainActivity;
import com.wemessage.R;
import com.wemessage.fragment.MessageFragment;
import com.wemessage.model.FriendInfo;

public class FriendAdapter extends FirebaseRecyclerAdapter<FriendInfo, FriendAdapter.FriendHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */

    String currentUserId;
    MessageFragment fragment;
    public FriendAdapter(@NonNull FirebaseRecyclerOptions options, MessageFragment fragment, String currentUserId) {
        super(options);
        this.fragment = fragment;
        this.currentUserId = currentUserId;
    }


    @NonNull
    @Override
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_message_layout, parent, false);
        return new FriendAdapter.FriendHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendHolder holder, int position, @NonNull FriendInfo model) {
        if(model.getUid() != null)
        {
            if(model.getUid().equals(currentUserId))
            {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.layout.getLayoutParams();
                params.height = 0;
                holder.layout.setLayoutParams(params);
                holder.layout.setVisibility(View.GONE);
                return;
            }
        }
        holder.tvName.setText(model.getName());
        holder.tvStatus.setText(model.getStatus());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.gotoChatActivity(model.getUid()==null?"null":model.getUid());
            }
        });
    }

    public class FriendHolder extends RecyclerView.ViewHolder
    {
        RelativeLayout layout;
        TextView tvName, tvStatus;

        public FriendHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvMessage);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
