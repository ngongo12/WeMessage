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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
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


    private void updateUserState(String state)
    {
        Map stateMap = new HashMap<>();
        stateMap.put("state", state);
        stateMap.put("time", sdf.format(new Date()));
        rootRef.child("Users").child(currentUserId).updateChildren(stateMap);
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
        //Cập nhật trạng thái người dùng khi offline
        updateUserState("offline");
    }
}