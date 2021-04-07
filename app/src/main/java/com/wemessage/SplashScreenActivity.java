package com.wemessage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {

    String fileName = "userLog.txt";
    String email = "", pass = "";
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        //Lấy tài khoản từ lần đăng nhập trước
        autoLogin();
        restoringDarkMode();
    }

    private void autoLogin() {
        //restoringPreferences(); tạm tắt
        if(!(email+pass).equals(""))
        {
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        gotoMainActivity();
                    }
                    else
                    {
                        Log.d("Loi", "onComplete: " + task.getException().toString());
                        gotoLoginActiviy();
                    }
                }
            });
        }
        else
        {
            gotoLoginActiviy();
        }
    }

    private void gotoLoginActiviy() {
        //Chuyển đến LoginActivity
        Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoMainActivity() {
        //Chuyển đến MainActivity
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void restoringPreferences()
    {
        SharedPreferences sp = getSharedPreferences(fileName, MODE_PRIVATE);

        email = sp.getString("email","");
        pass = sp.getString("pass", "");
    }

    private void restoringDarkMode()
    {
        //Tạo đối tượng
        SharedPreferences sp = getSharedPreferences(fileName, Context.MODE_PRIVATE);

        boolean isDark = sp.getBoolean("dark", false);
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}