package com.wemessage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    Button btnChange, btnBack;
    TextInputLayout tilNewPass, tilReNewPass;
    EditText edNewPass, edReNewPass;
    String email, reNewPass, newPass;
    ProgressDialog progressDialog;
    String fileName = "userLog.txt";

    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        btnBack = findViewById(R.id.btnBack);
        btnChange = findViewById(R.id.btnChange);
        tilNewPass = findViewById(R.id.tilNewPass);
        edNewPass = findViewById(R.id.edNewPass);
        tilReNewPass = findViewById(R.id.tilReNewPass);
        edReNewPass = findViewById(R.id.edReNewPass);

        //Khởi tạo progessDialog
        progressDialog = new ProgressDialog(this);

        //Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //Khởi tạo DatabaseReferences
        rootRef = FirebaseDatabase.getInstance().getReference();

        //Nút trở về btnBack
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Nút đăng ký btnChange
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mAuth.getCurrentUser().getEmail();
                newPass = edNewPass.getText().toString();
                reNewPass = edReNewPass.getText().toString();
                if(validate())
                {
                    changePassword();
                    savingPreferences();
                }
            }
        });
    }

    private void changePassword() {
        //progressDialog.setTitle("Tạo tài khoản mới");
        progressDialog.setMessage("Đang đổi mật khẩu, hãy chờ 1 lát...");
        progressDialog.show();
        mAuth.getCurrentUser().updatePassword(newPass)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                        finish();
                    }
                });
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validate()
    {
        if (newPass.length() < 6)
        {
            tilNewPass.setError("Mật khẩu ít nhất phải 6 ký tự");
        }
        if (!newPass.equals(reNewPass))
        {
            tilReNewPass.setError("Mật khẩu nhập lại không đúng");
            return false;
        }

        return true;
    }

    private void savingPreferences()
    {
        //Tạo đối tượng
        SharedPreferences sp = getSharedPreferences(fileName, MODE_PRIVATE);

        //Tạo đối tượng editor để lưu thay đổi
        SharedPreferences.Editor editor = sp.edit();

        //Lưu mới vào
        editor.putString("email", email);
        editor.putString("pass", newPass);

        //chấp nhận lưu xuống file
        editor.commit();
    }
}