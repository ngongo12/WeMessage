package com.wemessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wemessage.adapter.ReadFriendMessageAdapter;
import com.wemessage.adapter.ReadGroupMessageAdapter;
import com.wemessage.model.FriendInfo;
import com.wemessage.model.Message;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatWithGroupActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView tvFriendName, tvFriendStatus;
    EditText edMessage;
    ImageView btnSend, ivPicture, ivMicro, ivEmoji;
    RecyclerView rcv;
    Button btnJoin;
    LinearLayout layout_icon, layout_join;
    SwipeRefreshLayout swipeLayout;

    int numLimit = 8;

    //Các view của dialog send Image
    Button btnSendImg;
    ImageView ivChoosenImg;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    String groupId;
    FriendInfo friendInfo;

    //Các biến dành cho firebase
    DatabaseReference rootRef, messageRef, groupRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    String currentUserId;
    StorageReference imgRef;

    ArrayList<Message> list;

    ReadGroupMessageAdapter adapter;

    private static final int GalleryPick = 1;
    Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_group);

        //Ánh xạ các view
        toolbar = findViewById(R.id.toolbar);
        edMessage = findViewById(R.id.edMessage);
        btnSend = findViewById(R.id.ivSend);
        ivPicture = findViewById(R.id.ivPicture);
        ivMicro = findViewById(R.id.ivMicro);
        ivEmoji = findViewById(R.id.ivEmoji);
        btnJoin = findViewById(R.id.btnJoin);
        rcv = findViewById(R.id.rcv);
        layout_icon = findViewById(R.id.layout_icon);
        layout_join = findViewById(R.id.layout_join);
        swipeLayout = findViewById(R.id.swipeLayout);

        //Set layout cho rcv
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcv.setLayoutManager(layoutManager);
        rcv.setHasFixedSize(true);

        list = new ArrayList<>();

        layoutManager.setStackFromEnd(true);
        //layoutManager.setReverseLayout(true);

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        groupRef = rootRef.child("Groups");
        //messageRef = rootRef.child("Messages");
        imgRef = FirebaseStorage.getInstance().getReference().child("messages");

        //Xử lý toolbar
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinToGroup();
            }
        });

        tvFriendName = toolbar.findViewById(R.id.tvFriendName);
        tvFriendStatus = toolbar.findViewById(R.id.tvFriendStatus);

        getMyGroupInfo();
        readMessages();

        //Xử lý adapter
        adapter = new ReadGroupMessageAdapter(list, currentUserId, groupId, this);
        //adapter.startListening();
        rcv.setAdapter(adapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                try {
                    rcv.smoothScrollToPosition(rcv.getAdapter().getItemCount()+1);
                }
                catch (Exception e)
                {
                    rcv.smoothScrollToPosition(rcv.getAdapter().getItemCount());
                }

                edMessage.setText("");
                //edMessage.setFocusable(false);
            }
        });

        ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPicture();
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                numLimit += 5;
                //readMessages();
                swipeLayout.setRefreshing(false);
            }
        });

        //Sự kiện khi nhập vào edMessage
        edMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_icon.getLayoutParams();
                if(edMessage.getText().toString().equals(""))
                {
                    //Nếu ko có nội dung thì ẩn
                    btnSend.setVisibility(View.INVISIBLE);

                    //Hiện layout_icon
                    params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    layout_icon.setLayoutParams(params);
                }
                else
                {
                    //Nếu có nội dung thì hiện
                    btnSend.setVisibility(View.VISIBLE);

                    //Ẩn layout_icon
                    params.width = 0;
                    layout_icon.setLayoutParams(params);
                }
            }
        });
    }

    private void joinToGroup() {
        rootRef.child("Groups").child(groupId).child("members")
                .child(currentUserId)
                .child("last_seen").setValue(sdf.format(new Date()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ChatWithGroupActivity.this, "Gia nhập thành công", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ChatWithGroupActivity.this, "Có lỗi xảy ra, hã thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void readMessages() {
        //Thực hiện query
        messageRef.limitToLast(numLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Message item = dataSnapshot.getValue(Message.class);
                    list.add(item);
                }
                //Toast.makeText(ChatWithFriendActivity.this, ""+ list.size(), Toast.LENGTH_SHORT).show();
                //Xử lý adapter
                adapter = new ReadGroupMessageAdapter(list, currentUserId, groupId, ChatWithGroupActivity.this);
                //adapter.startListening();
                rcv.setAdapter(adapter);
                //adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Cập nhật thời gian đọc tin mới nhất
        groupRef.child(groupId).child("members").child(currentUserId).child("last_seen").setValue(sdf.format(new Date()));
    }

    private void sendMessage() {
        String messageContent = edMessage.getText().toString();
        if(messageContent.equals(""))
        {
            Toast.makeText(this, "Hãy nhập vào tin nhắn của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        //Tiến hành gửi
        send(messageContent, "text");
    }

    private void send(String content, String type) {

        DatabaseReference userMessageKeyRef = messageRef.push();
        String messagePushId = userMessageKeyRef.getKey();
        Map mapMessageContent = new HashMap();
        mapMessageContent.put("message", content);
        mapMessageContent.put("type", type);
        mapMessageContent.put("from", currentUserId);

        //Lấy thời gian gửi
        Date date = new Date();
        mapMessageContent.put("time", sdf.format(date));

        //Bỏ chung vào 1 map để gửi lên firebase

        messageRef.child(messagePushId).updateChildren(mapMessageContent).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(ChatWithGroupActivity.this, "Đã gửi", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(ChatWithGroupActivity.this, "Lỗi " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void scroolToBottom()
    {
        rcv.smoothScrollToPosition(list.size());
    }

    private void getMyGroupInfo() {
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        //Khai báo cho messageRef
        messageRef = rootRef.child("Groups").child(groupId).child("messages");

        rootRef.child("Groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("name"))
                {
                    tvFriendName.setText(snapshot.child("name").getValue().toString());
                }
                if (snapshot.hasChild("members"))
                {
                    tvFriendStatus.setText(snapshot.child("members").getChildrenCount() + " thành viên");
                }
                if (snapshot.hasChild("members"))
                {
                    //là thành viên thì ẩn
                    if (snapshot.child("members").hasChild(currentUserId))
                    {
                        layout_join.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Trước tiên xóa giá trị uri trước đó
        uriImage = null;

        //Lấy uri của hình được chọn
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            uriImage = data.getData();
            openDialogSendImage();
        }
        else
        {
            Toast.makeText(this, "Không lấy được ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);

        intent.setType("image/*");
        startActivityForResult(intent, GalleryPick);
    }

    private void openDialogSendImage() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.dialog_send_image,
                null
        );

        bottomSheetDialog.setContentView(view);

        btnSendImg = view.findViewById(R.id.btnSendImg);
        ivChoosenImg = view.findViewById(R.id.ivChoosenImg);

        //Set hình ảnh được chọn cho iv
        Glide.with(getApplicationContext()).load(uriImage).into(ivChoosenImg);

        //Hiển thị dialog
        bottomSheetDialog.create();
        bottomSheetDialog.show();

        //Xử lý nút gửi hình
        btnSendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uriImage == null)
                {
                    Toast.makeText(ChatWithGroupActivity.this, "Có lỗi trong quá trình lấy link ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Gửi trước hình ảnh lên storage
                //Tạo id cho ảnh
                String id = currentUserId.substring(0, 5) + LocalDateTime.now().toString().substring(5);
                Log.d("Loi", "id hình " + id);

                StorageReference filePath = imgRef.child(id + ".jpg");
                filePath.putFile(uriImage)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //Lấy được link tiến hành lưu trữ vào database
                                            send(uri.toString(), "image");
                                        }
                                    });
                                }
                                else
                                {
                                    Toast.makeText(ChatWithGroupActivity.this, "Gửi ảnh thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                //Đóng dialog
                bottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            rcv.smoothScrollToPosition(rcv.getAdapter().getItemCount()+1);
        }
        catch (Exception e)
        {
            rcv.smoothScrollToPosition(rcv.getAdapter().getItemCount());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Cập nhật thời gian đọc tin mới nhất
        groupRef.child(groupId).child("members").child(currentUserId).child("last_seen").setValue(sdf.format(new Date()));
    }
}