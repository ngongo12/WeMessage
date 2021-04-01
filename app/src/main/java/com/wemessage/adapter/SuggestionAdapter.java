package com.wemessage.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.wemessage.R;
import com.wemessage.model.FriendInfo;

import java.util.ArrayList;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.Holder> {

    Activity context;
    ArrayList<FriendInfo> list;

    public SuggestionAdapter(Activity context, ArrayList<FriendInfo> list) {
        this.context = context;
        this.list = list;
        Log.d("Loi", "SuggestionAdapter: " + list.size());
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
        holder.tvInfo.setText(list.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView ivAvatar;
        TextView tvName, tvInfo;
        Button btnRequest;

        public Holder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            btnRequest = itemView.findViewById(R.id.btnRequest);

        }
    }
}
