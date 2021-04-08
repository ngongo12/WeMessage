package com.wemessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.adapter.MainViewPagerAdapter;
import com.wemessage.fragment.OtherFragment;
import com.wemessage.service.UpdateUserStateService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    TextView tvSearch;
    ImageView ivSearch;
    MainViewPagerAdapter pagerAdapter;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    Date date;

    DatabaseReference rootRef, userRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ánh xạ các view
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.botton_nav);
        toolbar = findViewById(R.id.toolbar);

        //Xử lý toolbar
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        tvSearch = toolbar.findViewById(R.id.tvSearch);
        ivSearch = toolbar.findViewById(R.id.ivSearch);

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSearchActivity();
            }
        });
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSearchActivity();
            }
        });


        //Khởi tạo pagerAdapter
        pagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());

        //Gắn adapter cho viewpager
        viewPager.setAdapter(pagerAdapter);

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        //Liên kết giữa bottom navigation và viewpager
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.item_message: viewPager.setCurrentItem(0); break;
                    case R.id.item_friend: viewPager.setCurrentItem(1); break;
                    case R.id.item_group: viewPager.setCurrentItem(2); break;
                    case R.id.item_other: viewPager.setCurrentItem(3); break;

                }
                return true;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case 0: bottomNavigationView.setSelectedItemId(R.id.item_message); break;
                    case 1: bottomNavigationView.setSelectedItemId(R.id.item_friend); break;
                    case 2: bottomNavigationView.setSelectedItemId(R.id.item_group); break;
                    case 3: bottomNavigationView.setSelectedItemId(R.id.item_other); break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void gotoSearchActivity() {
        Intent intent = new Intent(MainActivity.this, SearchFriendActivity.class);
        startActivity(intent);
    }


    private void updateUserState(String state)
    {
        Intent intent = new Intent(getApplicationContext(), UpdateUserStateService.class);
        intent.putExtra("id", currentUserId);
        intent.putExtra("state", state);
        intent.putExtra("time", sdf.format(new Date()));
        startService(intent);
    }

    public void logout()
    {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Cập nhật trạng thái người dùng khi online
        updateUserState("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //Cập nhật trạng thái người dùng khi offline
        updateUserState("offline");
        super.onDestroy();
    }
}