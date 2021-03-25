package com.wemessage;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class SignupActivity extends AppCompatActivity {

    Button btnSignUp, btnBack;
    TextInputLayout tilEmail, tilPass, tilRePass;
    EditText edEmail, edPass, edRePass;
    String email, pass, rePass;
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnBack = findViewById(R.id.btnBack);
        btnSignUp = findViewById(R.id.btnSignUp);
        tilEmail = findViewById(R.id.tilEmail);
        tilPass = findViewById(R.id.tilPass);
        edEmail = findViewById(R.id.edEmail);
        edPass = findViewById(R.id.edPass);
        tilRePass = findViewById(R.id.tilRePass);
        edRePass = findViewById(R.id.edRePass);

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
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Nút đăng ký btnSignUp
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = edEmail.getText().toString();
                pass = edPass.getText().toString();
                rePass = edRePass.getText().toString();
                if(validate())
                {
                    createNewUser();
                }
            }
        });
    }

    private void createNewUser() {
        //progressDialog.setTitle("Tạo tài khoản mới");
        progressDialog.setMessage("Đang tạo tài khoản, hãy chờ 1 lát...");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            //Lấy Uid của user mới tạo
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            //Thêm user mới tạo vào Firebase -> Users
                            rootRef.child("Users").child(currentUserId).setValue("");

                            //Thêm thông tin mặc định cho user mới như name, status
                            HashMap<String, String> profileMap = new HashMap<>();
                            profileMap.put("uid", currentUserId);
                            profileMap.put("name", email.substring(0, email.indexOf("@")));
                            profileMap.put("status", "Người dùng mới");
                            rootRef.child("Users").child(currentUserId).setValue(profileMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this, "Tạo thông tin mặc định thành công", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Log.d("Loi", "onComplete: " + task.getException().toString());
                                            }
                                        }
                                    });

                            Toast.makeText(SignupActivity.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            gotoMainActivity();
                        }
                        else
                        {
                            Log.d("Loi", "onComplete: " + task.getException().toString());
                            Toast.makeText(SignupActivity.this, "Tạo tài không thành công", Toast.LENGTH_SHORT).show();
                            Toast.makeText(SignupActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
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

        //Kiểm tra password ít hơm 6 ký tự
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

        //Kiểm tra repassword nhập lại có khớp vói password không
        if(!rePass.equals(pass))
        {
            tilRePass.setError("Mật khẩu không khớp");
            return false;
        }
        else
        {
            tilRePass.setErrorEnabled(false);
        }

        return true;
    }
}