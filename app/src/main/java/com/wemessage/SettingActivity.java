package com.wemessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.EventListener;
import java.util.Set;

public class SettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView ivAvatar, ivWallpaper;

    private static final int GalleryPick = 1;
    private static final int WallpaperPick = 2;
    int requestId = 1;

    StorageReference avatarRef, wallRef;
    DatabaseReference userRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //Ánh xạ các view
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivWallpaper = findViewById(R.id.ivWallpaper);


        //Xử lý toolbar
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");

        //Khởi tạo các biến dành cho firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        avatarRef = FirebaseStorage.getInstance().getReference().child("avatar");
        wallRef = FirebaseStorage.getInstance().getReference().child("wallpaper");

        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent avatarIntent = new Intent();
                avatarIntent.setAction(Intent.ACTION_PICK);
                avatarIntent.setType("image/*");

                startActivityForResult(avatarIntent, GalleryPick);
            }
        });

        ivWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent avatarIntent = new Intent();
                avatarIntent.setAction(Intent.ACTION_PICK);
                avatarIntent.setType("image/*");

                startActivityForResult(avatarIntent, WallpaperPick);
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
                    //tvName.setText(snapshot.child("name").getValue().toString());
                }
                if (snapshot.hasChild("avatar"))
                {
                    Glide.with(getApplicationContext()).load(snapshot.child("avatar").getValue().toString()).into(ivAvatar);
                }
                if (snapshot.hasChild("wallpaper"))
                {
                    Glide.with(getApplicationContext()).load(snapshot.child("wallpaper").getValue().toString()).into(ivWallpaper);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Chỉ lấy requestCode bằng 1 trong 2 cái pick ko lấy của crop
        if(requestCode == GalleryPick || requestCode == WallpaperPick)
        {
            requestId = requestCode;
        }
        //Lấy hình cho avatar
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            Uri uriImage = data.getData();
            CropImage.activity(uriImage)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        //Lấy hình cho wallpaper
        if (requestCode == WallpaperPick && resultCode == RESULT_OK && data != null)
        {
            Uri uriImage = data.getData();
            CropImage.activity(uriImage)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16, 9)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //Cập nhật cho avatar
            if(resultCode == RESULT_OK && requestId == GalleryPick)
            {
                //Lấy uri kết quả trả về
                Uri resultUri = result.getUri();

                StorageReference filePath = avatarRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingActivity.this, "Cập nhật avatar thành công", Toast.LENGTH_SHORT).show();

                            //Lấy link hình đã up lên
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    userRef.child(currentUserId).child("avatar").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(SettingActivity.this, "Lưu trữ link thành công", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(SettingActivity.this, "Lưu trữ link thất bại " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(SettingActivity.this, "Cập nhật avatar thất bại \n"+ task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            //Cập nhật cho wallpaper
            if(resultCode == RESULT_OK && requestId == WallpaperPick)
            {
                //Lấy uri kết quả trả về
                Uri resultUri = result.getUri();

                StorageReference filePath = wallRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingActivity.this, "Cập nhật ảnh bìa thành công", Toast.LENGTH_SHORT).show();

                            //Lấy link hình đã up lên
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    userRef.child(currentUserId).child("wallpaper").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(SettingActivity.this, "Lưu trữ link thành công", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(SettingActivity.this, "Lưu trữ link thất bại " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(SettingActivity.this, "Cập nhật ảnh bìa thất bại \n"+ task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayInfo();
    }
}