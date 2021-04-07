package com.wemessage.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wemessage.MainActivity;
import com.wemessage.R;
import com.wemessage.SettingActivity;

public class OtherFragment extends Fragment {

    String fileName = "userLog.txt";

    TextView tvName;
    ImageView ivAvatar, ivWallpaper;
    SwitchCompat scDark;
    LinearLayout layoutEdit, layoutLogout, layoutRePass;

    DatabaseReference userRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Ánh xạ các view
        tvName = getView().findViewById(R.id.tvName);
        layoutEdit = getView().findViewById(R.id.layoutEdit);
        layoutLogout = getView().findViewById(R.id.layoutLogout);
        layoutRePass = getView().findViewById(R.id.layoutRePass);
        ivAvatar = getView().findViewById(R.id.ivAvatar);
        ivWallpaper = getView().findViewById(R.id.ivWallpaper);
        scDark = getView().findViewById(R.id.scDark);

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        
        layoutLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).logout();
            }
        });

        layoutEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });

        displayInfo();
        restoringDarkMode();
        scDark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savingDarkMode();
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }

    public void displayInfo()
    {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("name"))
                {
                    tvName.setText(snapshot.child("name").getValue().toString());
                }
                if(getActivity() != null) {
                    if (snapshot.hasChild("avatar")) {
                        Glide.with(getContext()).load(snapshot.child("avatar").getValue().toString()).into(ivAvatar);
                    }
                    if (snapshot.hasChild("wallpaper")) {
                        Glide.with(getContext()).load(snapshot.child("wallpaper").getValue().toString()).into(ivWallpaper);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void savingDarkMode()
    {
        //Tạo đối tượng
        SharedPreferences sp = getActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);

        //Tạo đối tượng editor để lưu thay đổi
        SharedPreferences.Editor editor = sp.edit();

        //Lưu mới vào
        editor.putBoolean("dark", scDark.isChecked());

        //chấp nhận lưu xuống file
        editor.commit();
    }

    private void restoringDarkMode()
    {
        //Tạo đối tượng
        SharedPreferences sp = getActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);

        boolean isDark = sp.getBoolean("dark", false);
        scDark.setChecked(isDark);
    }

}
