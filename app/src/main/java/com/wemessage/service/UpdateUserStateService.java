package com.wemessage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateUserStateService extends Service {

    String currentUserId;
    String state;
    String time;
    DatabaseReference rootRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentUserId = intent.getStringExtra("id");
        state = intent.getStringExtra("state");
        time = intent.getStringExtra("time");
        rootRef.child("Users").child(currentUserId).child("state").setValue(state);
        rootRef.child("Users").child(currentUserId).child("time").setValue(time);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
