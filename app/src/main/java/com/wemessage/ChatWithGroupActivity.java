package com.wemessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
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
import com.wemessage.adapter.ReadGroupMessageAdapter;
import com.wemessage.model.FriendInfo;
import com.wemessage.model.Messages;

import java.io.File;
import java.io.IOException;
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
    boolean isExit = false;

    int numLimit = 8;

    Messages item;

    //Các view của dialog send Image
    TextView tvTimeRecord;
    Button btnSendImg, btnSendRecord;
    ImageView ivChoosenImg;
    AppCompatToggleButton btnRecord;

    BottomSheetDialog imgDialog, audioDialog;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    String groupId;
    FriendInfo friendInfo;

    Handler handler = new Handler();
    int second = 0;
    boolean isRunning = true;
    Runnable runnable;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private static String fileName;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    

    //Các biến dành cho firebase
    DatabaseReference rootRef, messageRef, groupRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    String currentUserId;
    StorageReference imgRef, audioRef;

    ArrayList<Messages> list;

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
        audioRef = FirebaseStorage.getInstance().getReference().child("audio");

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
                readMessages();
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

        ivMicro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    public void getMessage(Messages message)
    {
        item = message;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.delete_message:
            {
                deleteMessage();
            }
        }
        return super.onContextItemSelected(item);
    }

    private void deleteMessage() {
        //Chỉ xóa được tin nhắn của mình
        //Đổi tin nhắn về type hide
        //Với type hide thì id của mình ko hiện gì còn người còn lại thì thấy tin nhắn bị gỡ
        if (item != null) {
            if (item.getFrom().equals(currentUserId)) {
                messageRef.orderByChild("time").equalTo(item.getTime())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        //Đã lây được key
                                        //Bắt đầu ẩn
                                        dataSnapshot.getRef().child("type").setValue("hide").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(ChatWithGroupActivity.this, "Ẩn thành công", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(ChatWithGroupActivity.this, "Lỗi: không tìm được message ", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                item = null;
            }
            else
            {
                Toast.makeText(this, "Bạn không thể xóa tin nhắn này", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "Lỗi khi lấy message", Toast.LENGTH_SHORT).show();
        }
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
                            isExit = false;
                            Toast.makeText(ChatWithGroupActivity.this, "Gia nhập thành công", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ChatWithGroupActivity.this, "Có lỗi xảy ra, hã thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Tạo menu ở toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.exit_group:
            {
                exitGroup();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void exitGroup() {
        Snackbar.make(toolbar, "Bạn muốn rời khỏi nhóm này " , BaseTransientBottomBar.LENGTH_SHORT)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isExit = true;
                        groupRef.child(groupId).child("members").child(currentUserId)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        Toast.makeText(ChatWithGroupActivity.this, "Rời khỏi nhóm thành công", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        finish();
                    }
                })
                .setTextColor(getResources().getColor(R.color.snackbar_text))
                .setActionTextColor(getResources().getColor(R.color.snackbar_text))
                .show();
    }

    private void readMessages() {
        //Thực hiện query
        messageRef.limitToLast(numLimit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Messages item = dataSnapshot.getValue(Messages.class);
                    list.add(item);
                }
                //Toast.makeText(ChatWithGroupActivity.this, ""+ list.size(), Toast.LENGTH_SHORT).show();
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
                        readMessages();
                    }
                    {
                        //Nếu ko phải thì lúc thoát ra ko cập nhật last_seen
                        isExit = true;
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
        imgDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.dialog_send_image,
                null
        );

        imgDialog.setContentView(view);

        btnSendImg = view.findViewById(R.id.btnSendImg);
        ivChoosenImg = view.findViewById(R.id.ivChoosenImg);

        //Set hình ảnh được chọn cho iv
        Glide.with(getApplicationContext()).load(uriImage).into(ivChoosenImg);

        //Hiển thị dialog
        imgDialog.create();
        imgDialog.show();

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
                imgDialog.dismiss();
            }
        });
    }

    public void openRecordAudioDialog()
    {
        fileName = getExternalCacheDir().getAbsolutePath() + "temp.3gp";
        audioDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.dialog_send_voice,
                null
        );

        audioDialog.setContentView(view);

        btnSendRecord = view.findViewById(R.id.btnSendRecord);
        btnRecord = view.findViewById(R.id.tgRecord);
        tvTimeRecord = view.findViewById(R.id.tvTimeRecord);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnRecord.isChecked())
                {
                    isRunning = true;
                    startRecording();
                    showTimeRecord();
                    btnRecord.setEnabled(false);
                }
                else
                {
                    stopRecording();
                }
                btnSendRecord.setVisibility(View.VISIBLE);
            }
        });

        audioDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopRecording();
                second = 0;
                isRunning = false;
            }
        });


        //Hiển thị dialog
        audioDialog.create();
        audioDialog.show();

        //Xử lý nút gửi hình
        btnSendRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAudio();
                //Đóng dialog
                audioDialog.dismiss();
            }
        });
    }

    //Record
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("Loi", "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    public void showTimeRecord()
    {
        if(tvTimeRecord != null && isRunning)
        {
            runnable = new Runnable() {
                @Override
                public void run() {
                    String time = "";
                    int s = second % 60;
                    int m = second / 60;
                    if (m < 10)
                    {
                        time += "0";
                    }
                    time += m+":";
                    if (s < 10)
                    {
                        time += "0";
                    }
                    time += s;
                    tvTimeRecord.setText(time);
                    second++;
                    showTimeRecord();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    public void uploadAudio()
    {
        Uri uri = Uri.fromFile(new File(fileName));
        //Tạo id cho audio
        String id = currentUserId.substring(0, 5) + LocalDateTime.now().toString().substring(5);
        Log.d("Loi", "id  " + id);
        audioRef.child(id).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    audioRef.child(id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Lấy được link tiến hành lưu trữ vào database
                            send(uri.toString(), "audio");
                            Toast.makeText(ChatWithGroupActivity.this, "Gửi thành công", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(ChatWithGroupActivity.this, "Gửi thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            rcv.smoothScrollToPosition(rcv.getAdapter().getItemCount());
        }
        catch (Exception e)
        {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Nếu không có lệnh ròi khỏi nhóm thì cập nhật ngày xem
        if (!isExit) {
            //Cập nhật thời gian đọc tin mới nhất
            groupRef.child(groupId).child("members").child(currentUserId).child("last_seen").setValue(sdf.format(new Date()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 999: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openRecordAudioDialog();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "khong co permission", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    public void requestPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Record audio needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Record audio access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.RECORD_AUDIO}
                                    , 999);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]
                                    {Manifest.permission.RECORD_AUDIO},
                            999);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                openRecordAudioDialog();
            }
        }
        else {
            openRecordAudioDialog();
        }
    }
}