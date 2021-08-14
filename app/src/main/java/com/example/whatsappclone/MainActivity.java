package com.example.whatsappclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabAccessorAdapter tabAccessorAdapter;
    private DatabaseReference roofRef;

    private FirebaseUser currentUser;
   // private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference statusRef;
    private String pushID;
    private String checker="not";
    private String comingName;
    private String comingImage;
    private String uploadId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusRef = FirebaseDatabase.getInstance().getReference().child("Statuses");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //currentUserID = mAuth.getCurrentUser().getUid();

        roofRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WhatsApp");


        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        tabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(tabAccessorAdapter);

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        uploadId = roofRef.child("Users").push().getKey();

            HashMap info2=new HashMap<>();
            info2.put("pushid",uploadId);

            roofRef.child("Users").child(currentUser.getUid()).updateChildren(info2);



        Log.d("checker","pushid "+uploadId);



        checker= getIntent().getStringExtra("checker");
        Log.d("checker",checker);

        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {

            verfilyExistance();



            if(checker.equals("done")){

                HashMap info=new HashMap<>();
                info.put("state","online");
                roofRef.child("Users").child(currentUser.getUid()).updateChildren(info);

                HashMap profileMap = new HashMap();
                profileMap.put("date", "hh");
                profileMap.put("image", "https://firebasestorage.googleapis.com/v0/b/testfirebase-9e9e1.appspot.com/o/Profile%20Image%2FEZMVsoQtYUS2SaMRaSUFDkXLuCz2.jpg?alt=media&token=d154dc08-51f4-4ffd-ac34-6899bcd8e856");
                profileMap.put("name", comingName);
                profileMap.put("time", "hh");
                profileMap.put("uid", currentUser.getUid());

                statusRef.child(uploadId).updateChildren(profileMap);

                checker="not";

                Log.d("checker","last");
            }



        }
    }

    private void verfilyExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        roofRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.child("name").exists())){
                     comingName=dataSnapshot.child("name").getValue().toString();
                     comingImage=dataSnapshot.child("image").getValue().toString();
                }else {
                    SendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);

    }

    private void SendUserToSettingActivity() {

        Intent settingIntent = new Intent(this, SettingActivity.class);
        settingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(settingIntent);
        finish();
    }
    //////////creating option mean like logout or setting wali jaga...


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_find_friends_options) {

            SendUserToFindFriendActivity();

        }
        if (item.getItemId() == R.id.main_createGroup_option) {
            requestNewGroup();
        }
        if (item.getItemId() == R.id.main_setting_option) {

            SendUserToSettingActivity();
        }
        if (item.getItemId() == R.id.main_logout_option) {
            mAuth.signOut();
            SendUserToLoginActivity();

            HashMap info=new HashMap<>();
            info.put("state","offline");
            roofRef.child("Users").child(currentUser.getUid()).updateChildren(info);
        }
        return true;
    }

    private void SendUserToFindFriendActivity() {

        Intent findFriendIntent = new Intent(this, FindFriendsActivity.class);
        startActivity(findFriendIntent);

    }

    private void requestNewGroup() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");

        final EditText groupNameField=new EditText(this);
        groupNameField.setHint("e.g soft code");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName=groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group Name...", Toast.LENGTH_SHORT).show();

                }else {

                    //group name is sending to fucntion
                    CreateNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupName) {

        roofRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+" "+"GroupName is Created Succesfully ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
