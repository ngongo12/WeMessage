package com.wemessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.adapter.FriendsForGroupAdapter;
import com.wemessage.model.FriendInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CreateGroupActivity extends AppCompatActivity {

    Toolbar toolbar;
    //Của toolbar
    TextView tvGroupName, tvSoNguoi;

    EditText edGroupName, edSearch;
    ImageView ivCheck;
    RelativeLayout layout_name, layout_search;
    Button btnCreate;
    RecyclerView rcv;

    ArrayList<FriendInfo> list;
    FriendsForGroupAdapter adapter;
    Map<String, String> mapChoosen;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    DatabaseReference usersRef, groupRef;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        //Ánh xạ các view
        toolbar = findViewById(R.id.toolbar);
        tvGroupName = toolbar.findViewById(R.id.tvGroupName);
        tvSoNguoi = toolbar.findViewById(R.id.tvSoNguoi);

        edGroupName = findViewById(R.id.edGroupName);
        edSearch = findViewById(R.id.edSearch);
        ivCheck = findViewById(R.id.ivCheck);
        layout_name = findViewById(R.id.layout_name);
        layout_search = findViewById(R.id.layout_search);
        btnCreate = findViewById(R.id.btnCreate);
        rcv = findViewById(R.id.rcv);

        list = new ArrayList<>();
        mapChoosen = new HashMap();

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        //Set layout cho rcv
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcv.setLayoutManager(layoutManager);
        adapter = new FriendsForGroupAdapter(list, this, mapChoosen);

        rcv.setAdapter(adapter);

        //Xử lý toolbar
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvGroupName.getText().toString().trim().equals("Chưa đặt tên") || mapChoosen.size() >= 2) {
                    openDialog();
                }
                else
                {
                    finish();
                }
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_name.setVisibility(View.VISIBLE);
            }
        });

        edGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //Ẩn hiện nút submit
                if(edGroupName.getText().toString().trim().equals(""))
                {
                    ivCheck.setVisibility(View.GONE);
                }
                else
                {
                    ivCheck.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Khi nhấn vào ivCheck sẽ hiển thị tên group lên tvGroupName trên toolbar
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvGroupName.setText(edGroupName.getText().toString().trim());
                showButtonCreate();
                layout_name.setVisibility(View.GONE);
            }
        });

        //Xử lý lấy danh sách
        displayRCV("");

        //search
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayRCV(edSearch.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Nút tạo nhóm
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
    }

    private void createGroup() {
        //Lấy key của group mới tạo
        String groupId = groupRef.push().getKey();

        mapChoosen.put(currentUserId, null); //Map này chứa member

        Map mapGroup = new HashMap();
        Map mapMember = new HashMap();


        String date = sdf.format(new Date());
        for (String s : mapChoosen.keySet())
        {
            //thêm sẵn last_seen
            //mapMember.put(s+"/date", date);
            mapGroup.put("members/" + s + "/last_seen", date);
        }

        //Set dữ liệu chuẩn bị đổ lên firebase
        mapGroup.put("name", tvGroupName.getText().toString().trim());
        mapGroup.put("owner", currentUserId);
        //mapGroup.put("members", mapMember);




        //Đổ lên firebase
        groupRef.child(groupId).updateChildren(mapGroup).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(CreateGroupActivity.this, "Tạo nhóm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    Toast.makeText(CreateGroupActivity.this, "Tạo nhóm thất bại", Toast.LENGTH_SHORT).show();
                    Log.d("Loi", "onComplete: nhóm " + task.getException().toString());
                }
            }
        });
    }

    public void showButtonCreate()
    {
        //Chưa đặt tên hoặc số thành viên nhở hơn 2 thì ẩn (mỗi nhóm ít nhất 3 người)
        if (tvGroupName.getText().toString().equals("Chưa đặt tên") || mapChoosen.size() < 2)
        {
            btnCreate.setVisibility(View.GONE);
        }
        else
        {
            btnCreate.setVisibility(View.VISIBLE);
        }
    }

    private void displayRCV(String s) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    if (!dataSnapshot.getKey().equals(currentUserId))
                    {
                        FriendInfo item = dataSnapshot.getValue(FriendInfo.class);

                        //Trong tên có chứa nội dung tìm kiếm thì thêm vào list
                        if (item.getName().toLowerCase().contains(s.toLowerCase())) {
                            Log.d("Loi", "onDataChange: " + item.getName());
                            list.add(item);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getChoosenMap(Map map)
    {
        mapChoosen.putAll(map);
        tvSoNguoi.setText("Đã chọn: " + mapChoosen.size());
        showButtonCreate();
    }

    @Override
    public void onBackPressed() {
        if (!tvGroupName.getText().toString().trim().equals("Chưa đặt tên") || mapChoosen.size() >= 2) {
            openDialog();
        }
        else
        {
            CreateGroupActivity.super.onBackPressed();
        }
    }
    public void openDialog()
    {
        //Su dung Alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn muốn hủy tạo nhóm: " + tvGroupName.getText().toString() + "?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Thoát",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CreateGroupActivity.super.onBackPressed();
                    }
                }
        );
        //Không thoát
        builder.setNegativeButton(
                "Ở lại",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }
        );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}