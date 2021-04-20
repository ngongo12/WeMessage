package com.wemessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InfoMyFriendActivity extends AppCompatActivity {
    TextView tvName,tvBirth,tvGender,tvStatus;
    Button btnChat, btnRequest, btnCancelRequest, btnDeleteFriend, btnAccept;
    ImageView ivAvatar, ivWallpaper;
    Toolbar toolbar;

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    DatabaseReference userRef, friendRef, requestRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    String friendId, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_my_friend);

        //Ánh xạ
        toolbar = findViewById(R.id.toolbar);
        tvName=findViewById(R.id.tvName);
        tvBirth=findViewById(R.id.tvBirth);
        tvGender=findViewById(R.id.tvGender);
        tvStatus=findViewById(R.id.tvInfo);
        ivAvatar=findViewById(R.id.ivAvatar);
        ivWallpaper=findViewById(R.id.ivWallpaper);
        btnChat=findViewById(R.id.btnChat);
        btnRequest=findViewById(R.id.btnRequest);
        btnCancelRequest=findViewById(R.id.btnCancelRequest);
        btnDeleteFriend = findViewById(R.id.btnDeleteFriend);
        btnAccept = findViewById(R.id.btnAccept);

        //Xử lý toolbar
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Intent intent = getIntent();
        friendId = intent.getStringExtra("friendId");
        if (friendId == null)
        {
            return;
        }

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        requestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        displayInfo();
        displayButton();

        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRequest();
            }
        });

        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequest();
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoMyFriendActivity.this, ChatWithFriendActivity.class);
                intent.putExtra("myFriendId", friendId);
                startActivity(intent);
            }
        });

        btnDeleteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFriend();
            }
        });
    }

    private void deleteFriend() {
        Snackbar.make(toolbar, "Bạn muốn xóa bạn với: " + tvName.getText().toString() , BaseTransientBottomBar.LENGTH_SHORT)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Xóa bên mình
                        friendRef.child(currentUserId).child(friendId)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        //Xóa được bên mình thì xóa bên đối phương
                                        friendRef.child(friendId).child(currentUserId)
                                                .removeValue(new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                        Toast.makeText(InfoMyFriendActivity.this, "Đã xóa bạn bè thành công", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                        finish();
                    }
                })
                .setTextColor(getResources().getColor(R.color.snackbar_text))
                .setActionTextColor(getResources().getColor(R.color.snackbar_text))
                .show();
    }

    public void acceptRequest()
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
                        requestRef.child(currentUserId).child(friendId).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                requestRef.child(friendId).child(currentUserId)
                                        .removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                Toast.makeText(InfoMyFriendActivity.this, "Xóa yêu cầu kết bạn thành công", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                });
    }

    public void cancelRequest()
    {
        //Xóa trạng thái request từ id của mình
        requestRef.child(currentUserId).child(friendId).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                requestRef.child(friendId).child(currentUserId)
                        .removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                Toast.makeText(InfoMyFriendActivity.this, "Xóa yêu cầu kết bạn thành công", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    public void addRequest()
    {
        //Thêm vào friend request của myId với giá trị type send
        requestRef.child(currentUserId).child(friendId).child("type").setValue("send")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(InfoMyFriendActivity.this, "Gửi yêu cầu kết bạn thành công", Toast.LENGTH_SHORT).show();
                    }
                });
        //Thêm vào friend request của myId với giá trị type received
        requestRef.child(friendId).child(currentUserId).child("type").setValue("receive");
    }

    private void displayInfo() {
        userRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("name"))
                    {
                        tvName.setText(snapshot.child("name").getValue().toString());
                    }
                    if (snapshot.hasChild("status"))
                    {
                        tvStatus.setText(snapshot.child("status").getValue().toString());
                    }
                    if (snapshot.hasChild("birthday"))
                    {
                        tvBirth.setText(snapshot.child("birthday").getValue().toString());
                    }
                    if (snapshot.hasChild("gender"))
                    {
                        tvGender.setText(snapshot.child("gender").getValue().toString());
                    }

                    if (snapshot.hasChild("avatar"))
                    {
                        Glide.with(getApplicationContext()).load(snapshot.child("avatar").getValue().toString()).into(ivAvatar);
                    }
                    if (snapshot.hasChild("wallpaper"))
                    {
                        Glide.with(getApplicationContext()).load(snapshot.child("wallpaper").getValue().toString()).into(ivWallpaper);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayButton() {
        //So sánh để hiển thị các button
        friendRef.child(currentUserId).child(friendId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    //Là bạn bè
                    btnChat.setVisibility(View.VISIBLE);
                    btnDeleteFriend.setVisibility(View.VISIBLE);
                    btnCancelRequest.setVisibility(View.GONE);
                    btnRequest.setVisibility(View.GONE);
                    btnAccept.setVisibility(View.GONE);
                }
                else
                {
                    //Không là bạn bè
                    requestRef.child(currentUserId).child(friendId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists())
                            {
                                if (snapshot.hasChild("type"))
                                {

                                    if(snapshot.child("type").getValue().toString().equals("send"))
                                    {
                                        btnChat.setVisibility(View.GONE);
                                        btnDeleteFriend.setVisibility(View.GONE);
                                        btnCancelRequest.setVisibility(View.VISIBLE);
                                        btnRequest.setVisibility(View.GONE);
                                        btnAccept.setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        btnChat.setVisibility(View.GONE);
                                        btnDeleteFriend.setVisibility(View.GONE);
                                        btnCancelRequest.setVisibility(View.GONE);
                                        btnRequest.setVisibility(View.GONE);
                                        btnAccept.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                            else
                            {
                                //Trương hợp còn lại: chưa yêu cầu gì cả
                                btnChat.setVisibility(View.GONE);
                                btnDeleteFriend.setVisibility(View.GONE);
                                btnCancelRequest.setVisibility(View.GONE);
                                btnRequest.setVisibility(View.VISIBLE);
                                btnAccept.setVisibility(View.GONE);
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
    }
}