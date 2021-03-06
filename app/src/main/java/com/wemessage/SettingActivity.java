package com.wemessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView ivAvatar, ivWallpaper;
    EditText etName, etInfo;
    Button btnSave;
    RadioButton rdNam,rdNu;
    TextView tvBirth;
    DatePickerDialog datePickerDialog;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

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

        //??nh x??? c??c view
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivWallpaper = findViewById(R.id.ivWallpaper);
        etName=findViewById(R.id.etName);
        etInfo=findViewById(R.id.etInfo);
        btnSave=findViewById(R.id.btnSave);
        rdNam=findViewById(R.id.rdNam);
        rdNu=findViewById(R.id.rdNu);
        tvBirth=findViewById(R.id.tvBirth);


        //X??? l?? toolbar
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Kh???i t???o c??c bi???n d??nh cho firebase
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
                avatarIntent.setAction(Intent.ACTION_GET_CONTENT);
                avatarIntent.setType("image/*");

                startActivityForResult(avatarIntent, GalleryPick);
            }
        });

        ivWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent avatarIntent = new Intent();
                avatarIntent.setAction(Intent.ACTION_GET_CONTENT);
                avatarIntent.setType("image/*");

                startActivityForResult(avatarIntent, WallpaperPick);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate())
                {
                    return;
                }

                //L??u l???i th??ng tin

                Map infoMap = new HashMap();
                String name=etName.getText().toString();
                //userRef.child(currentUserId).child("name").setValue(name);
                infoMap.put("name", name);

                //info
                infoMap.put("status", etInfo.getText().toString());

                if (rdNam.isChecked()){
                    //userRef.child(currentUserId).child("gender").setValue("Nam");
                    infoMap.put("gender", "Nam");
                }
                if (rdNu.isChecked()){
                    //userRef.child(currentUserId).child("gender").setValue("N???");
                    infoMap.put("gender", "N???");
                }
                String ngaySinh=tvBirth.getText().toString();
                //userRef.child(currentUserId).child("birthday").setValue(ngaySinh);
                infoMap.put("birthday", ngaySinh);

                //Save to database
                userRef.child(currentUserId).updateChildren(infoMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(SettingActivity.this, "C???p nh???t th??ng tin th??nh c??ng", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(SettingActivity.this, "C???p nh???t th??ng tin th???t b???i. " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        datePickerDialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                tvBirth.setText(sdf.format(calendar.getTime()));
            }
        }, 1990, 0, 1);
        tvBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
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
                    etName.setText(snapshot.child("name").getValue().toString());
                }
                if (snapshot.hasChild("status"))
                {
                    etInfo.setText(snapshot.child("status").getValue().toString());
                }
                if (snapshot.hasChild("birthday"))
                {
                    tvBirth.setText(snapshot.child("birthday").getValue().toString());

                    //Set l???i ng??y cho datepicker
                    Calendar calendar = Calendar.getInstance();
                    try {
                        calendar.setTime(sdf.parse(tvBirth.getText().toString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    datePickerDialog.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                }
                if (snapshot.hasChild("gender"))
                {
                    if(snapshot.child("gender").getValue().toString().equals("Nam"))
                    {
                        rdNam.setChecked(true);
                    }
                    else
                    {
                        rdNu.setChecked(true);
                    }
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

        //Ch??? l???y requestCode b???ng 1 trong 2 c??i pick ko l???y c???a crop
        if(requestCode == GalleryPick || requestCode == WallpaperPick)
        {
            requestId = requestCode;
        }
        //L???y h??nh cho avatar
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null)
        {
            Uri uriImage = data.getData();
            CropImage.activity(uriImage)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        //L???y h??nh cho wallpaper
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

            //C???p nh???t cho avatar
            if(resultCode == RESULT_OK && requestId == GalleryPick)
            {
                //L???y uri k???t qu??? tr??? v???
                Uri resultUri = result.getUri();

                StorageReference filePath = avatarRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingActivity.this, "C???p nh???t avatar th??nh c??ng", Toast.LENGTH_SHORT).show();

                            //L???y link h??nh ???? up l??n
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    userRef.child(currentUserId).child("avatar").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(SettingActivity.this, "L??u tr??? link th??nh c??ng", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(SettingActivity.this, "L??u tr??? link th???t b???i " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(SettingActivity.this, "C???p nh???t avatar th???t b???i \n"+ task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            //C???p nh???t cho wallpaper
            if(resultCode == RESULT_OK && requestId == WallpaperPick)
            {
                //L???y uri k???t qu??? tr??? v???
                Uri resultUri = result.getUri();

                StorageReference filePath = wallRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingActivity.this, "C???p nh???t ???nh b??a th??nh c??ng", Toast.LENGTH_SHORT).show();

                            //L???y link h??nh ???? up l??n
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    userRef.child(currentUserId).child("wallpaper").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(SettingActivity.this, "L??u tr??? link th??nh c??ng", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(SettingActivity.this, "L??u tr??? link th???t b???i " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(SettingActivity.this, "C???p nh???t ???nh b??a th???t b???i \n"+ task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    public boolean validate(){

        if (etName.getText().length() == 0) {
            Toast.makeText(this, "T??n kh??ng ???????c b??? tr???ng !", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etInfo.getText().length() == 0){
            Toast.makeText(this, "TTh??ng tin kh??ng ???????c b??? tr???ng !", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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