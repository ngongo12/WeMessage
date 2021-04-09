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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    Button btnSignUp, btnLogin;
    TextInputLayout tilEmail, tilPass;
    EditText edEmail, edPass;
    String email, pass;
    ProgressDialog progressDialog;

    String fileName = "userLog.txt";

    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("Loi", "onCreate: mở login");

        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        tilEmail = findViewById(R.id.tilEmail);
        tilPass = findViewById(R.id.tilPass);
        edEmail = findViewById(R.id.edEmail);
        edPass = findViewById(R.id.edPass);

        //Khởi tạo progessDialog
        progressDialog = new ProgressDialog(this);

        //Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //Khởi tạo DatabaseReferences
        rootRef = FirebaseDatabase.getInstance().getReference();

        //Lấy email từ log
        restoringPreferences();

        //Nút đăng ký
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Nút đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = edEmail.getText().toString();
                pass = edPass.getText().toString();
                if(validate())
                {
                    login();
                }
            }
        });
    }

    private void login() {
        //progressDialog.setTitle("Đăng nhập");
        progressDialog.setMessage("Đang đăng nhập, chờ chút...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    savingPreferences();
                    gotoMainActivity();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else
                {
                    Log.d("Loi", "onComplete: " + task.getException().toString());
                    Toast.makeText(LoginActivity.this, "Đăng nhập không thành công", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validate()
    {
        //Kiểm tra email trống
        if(email.trim().length() == 0)
        {
            tilEmail.setError("Email không được bỏ trống");
            return false;
        }
        else
        {
            tilEmail.setErrorEnabled(false);
        }

        //Kiểm tra email hợp lệ không
        String regex = "\\w+@\\w+(\\.+\\w+){1,2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if(!matcher.matches())
        {
            tilEmail.setError("Email không hợp lệ");
            return false;
        }
        else
        {
            tilEmail.setErrorEnabled(false);
        }

        //Kiểm tra password ít hơn 6 ký tự
        if(pass.trim().length() < 6)
        {
            tilPass.setError("Mật khẩu ít nhất 6 ký tự");
            return false;
        }
        else
        {
            tilPass.setErrorEnabled(false);
        }

        //Kiểm tra email hợp lệ không
        String regexPass = "[\\w]{6,}";
        Pattern patternPass = Pattern.compile(regexPass);
        Matcher matcherPass = patternPass.matcher(pass);
        if(!matcherPass.matches())
        {
            tilPass.setError("Mật khẩu chứa ký tự không hợp lệ");
            return false;
        }
        else
        {
            tilPass.setErrorEnabled(false);
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
        editor.putString("pass", pass);

        //chấp nhận lưu xuống file
        editor.commit();
    }

    private void restoringPreferences()
    {
        SharedPreferences sp = getSharedPreferences(fileName, MODE_PRIVATE);

        String email = sp.getString("email","");
        String pass = sp.getString("pass", "");
        edEmail.setText(email);
        edPass.setText(pass);
    }
}