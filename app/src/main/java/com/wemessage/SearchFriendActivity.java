package com.wemessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.model.FriendInfo;

import java.util.ArrayList;

public class SearchFriendActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText edSearch;
    TextView tvNotFound;
    RecyclerView rcv;

    DatabaseReference userRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    String currentUserId;

    ArrayList<FriendInfo> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        //Ánh xạ các view
        toolbar = findViewById(R.id.toolbar);
        edSearch = toolbar.findViewById(R.id.edSearch);
        tvNotFound = findViewById(R.id.tvNotFound);
        rcv = findViewById(R.id.rcv);

        //Xử lý toolbar
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        list = new ArrayList<>();

        edSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                {
                    //request Keyboard
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
                else
                {
                    //request Keyboard
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });

        //Xử lý search
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = edSearch.getText().toString().trim();
                if(!str.equals("")) {
                    tvNotFound.setText("");
                }
                search(str);
            }
        });
    }

    private void search(String s) {
        //userRef.orderByChild("name").startAt(s).endAt(s+"\uf8ff") không đúng lắm
        userRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
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
                if (list.size() < 1)
                {
                    tvNotFound.setText("Không tìm thấy: " + s);
                }
                else
                {
                    tvNotFound.setText("Tìm thấy " + list.size() +" kết quả tương ứng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}