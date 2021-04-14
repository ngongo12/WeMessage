package com.wemessage.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReceiveMessageService extends Service {

    String currentUserId;
    DatabaseReference myRef;

    Handler handler = new Handler();
    Runnable chay;
    boolean isRunning = true;
    int tang = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Mở service", Toast.LENGTH_SHORT).show();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        currentUserId = intent.getStringExtra("id");

        //myRef = FirebaseDatabase.getInstance().getReference().child("Notification").child(currentUserId);
        waitingMessage();

        return START_STICKY;
    }

    @Override
    public ComponentName startForegroundService(Intent service) {


        waitingMessage();

        return super.startForegroundService(service);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Đóng service", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.setAction("stop_service");
        sendBroadcast(intent);
        handler.removeCallbacks(chay);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void waitingMessage()
    {
        if(isRunning)
        {
            chay = new Runnable() {
                @Override
                public void run() {
                    tang++;
                    Toast.makeText(ReceiveMessageService.this, "Tăng lên: "+tang, Toast.LENGTH_SHORT).show();
                    waitingMessage();
                };
            };
            handler.postDelayed(chay,5000);
        }
    }
}
