package com.example.whatsappclone;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {


    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImageRef;
    private Uri ImageUri;
    private ProgressDialog progressDialog;
    private String retrieveProfilePic;
    private String photoStringLink;

    public Toolbar settingToolBar;
    private DatabaseReference statusRef;
    private String name;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initailizedFeilds();
        retrieveUserInfo();

        userName.setVisibility(View.INVISIBLE);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");
        progressDialog = new ProgressDialog(this);

    }

    private void retrieveUserInfo() {

        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {

                    String retireveUsername = dataSnapshot.child("name").getValue().toString();
                    String retireveUserStatus = dataSnapshot.child("status").getValue().toString();
                    retrieveProfilePic = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retireveUsername);
                    userStatus.setText(retireveUserStatus);

                    Picasso.get().load(retrieveProfilePic).into(userProfileImage);

                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {

                    String retireveUsername = dataSnapshot.child("name").getValue().toString();
                    String retireveUserStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retireveUsername);
                    userStatus.setText(retireveUserStatus);


                } else {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingActivity.this, "pls set and update profile info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initailizedFeilds() {

        userProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        userName = (EditText) findViewById(R.id.name);
        userStatus = (EditText) findViewById(R.id.status);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        settingToolBar=(Toolbar)findViewById(R.id.setting_tooldbar);
        setSupportActionBar(settingToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    public void updateButton(View view) {

         name = userName.getText().toString();
         status = userStatus.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name shouldn't be empty", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Status shouldn't be empty", Toast.LENGTH_SHORT).show();
        } else {



            HashMap profileMap = new HashMap();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", name);
            profileMap.put("image",photoStringLink);
            profileMap.put("status", status);
            rootRef.child("Users").child(currentUserID).updateChildren(profileMap);

            SendUserToMainActivity();
            Toast.makeText(SettingActivity.this, "Profile Updated successfully !", Toast.LENGTH_SHORT).show();
        }
    }

    private void SendUserToMainActivity() {

        Intent MainIntent = new Intent(this, MainActivity.class);
        MainIntent.putExtra("checker","done");
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(MainIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        SendUserToMainActivity();
    }

    public void UserProfileImage(View view) {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {

            ImageUri = data.getData();

            progressDialog.setTitle("Saving Image Now");
            progressDialog.setMessage("Please Wait, Image is gonna Save !");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
            filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(SettingActivity.this, "Profile Image updated Successfully !", Toast.LENGTH_SHORT).show();

                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            photoStringLink = uri.toString();

                            rootRef.child("Users").child(currentUserID).child("image").setValue(photoStringLink).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(SettingActivity.this, "Image saved in database successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(SettingActivity.this, "Error " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                    });
                }
            });

                    }
                }
            }
