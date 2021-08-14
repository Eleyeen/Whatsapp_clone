package com.example.whatsappclone;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class VideoCallActivity extends AppCompatActivity {


    private TextView nameContact;
    private ImageView profileImage, cancelBtn, makeCallBtn;

    String receiverUserID = "", receiverUserImage = "", receiverUserName = "";
    String senderUserID = "", senderUserImage = "", senderUserName = "", checker = "";
    private DatabaseReference userRef;

    private String callingID = "", ringingId = "";

   // private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        senderUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        receiverUserID = getIntent().getStringExtra("user_visit_id");

       // mediaPlayer = MediaPlayer.create(this, R.raw.audio);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        nameContact = findViewById(R.id.userNAme);
        profileImage = findViewById(R.id.backImage);
        cancelBtn = findViewById(R.id.cancel_call);
        makeCallBtn = findViewById(R.id.make_call);



        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


             //   mediaPlayer.stop();
                //cancel call herre
                checker = "clicked";

                Log.d("checked ", "clicked working fine here");
                CancelUserCalling();


            }
        });

        makeCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              //  mediaPlayer.stop();

                final HashMap<String, Object> callingpickUpMap = new HashMap<>();
                callingpickUpMap.put("picked", "picked");

                userRef.child(senderUserID).child("Ringing").updateChildren(callingpickUpMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                           // mediaPlayer.stop();

                            Intent intent = new Intent(getApplicationContext(), AttendVideoCallActivity.class);
                            startActivity(intent);

                        }
                    }
                });
            }
        });

        getandsetReceiveProfileInfo();


    }

    private void CancelUserCalling() {

       // mediaPlayer.stop();

        //from sender side ..   ....cancel from user sender side
        userRef.child(senderUserID).child("Calling")
                .addListenerForSingleValueEvent((new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")) {

                            callingID=dataSnapshot.child("calling").getValue().toString();

                            userRef.child(callingID)
                                    .child("Ringing")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    //yahan jis ko ring ja rhi ha us k ringing node ko delete kr rhy hain ..
                                    if(task.isSuccessful()){

                                        userRef.child(senderUserID)
                                                .child("Calling")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){
                                                    Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
                                                    startActivity(intent);
                                                    finish();

                                                   // mediaPlayer.stop();
                                                }
                                            }
                                        });
                                    }

                                }
                            });

                        }
                        else {

                            Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
                            startActivity(intent);



                         //   mediaPlayer.stop();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }));



      //  mediaPlayer.stop();
        // from receiver side......mean....cancel from receiver side..
        userRef.child(senderUserID).child("Ringing")
                .addListenerForSingleValueEvent((new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")) {

                            ringingId=dataSnapshot.child("ringing").getValue().toString();

                            userRef.child(ringingId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    //yahan jis ko ring ja rhi ha us k ringing node ko delete kr rhy hain ..
                                    if(task.isSuccessful()){

                                        userRef.child(senderUserID)
                                                .child("Ringing")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
                                                    startActivity(intent);
                                                    finish();

                                                   // mediaPlayer.stop();
                                                }
                                            }
                                        });
                                    }

                                }
                            });

                        }
                        else {

                            Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
                            startActivity(intent);
                            finish();

                            //mediaPlayer.stop();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }));
    }


    private void getandsetReceiveProfileInfo() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(receiverUserID).exists()) {
                    receiverUserImage = dataSnapshot.child(receiverUserID).child("image").getValue().toString();
                    receiverUserName = dataSnapshot.child(receiverUserID).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.accountyellow).into(profileImage);
                }
                if (dataSnapshot.child(senderUserID).exists()) {

                    senderUserImage = dataSnapshot.child(senderUserID).child("image").getValue().toString();
                    senderUserName = dataSnapshot.child(senderUserID).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        Log.d("checked ", "clicked when preess cancel");
        super.onStart();

      //  mediaPlayer.start();

        userRef.child(receiverUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {



                    if(!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing")){

                        Log.d("checked ", "if clicked");


                        final HashMap<String,Object> callingInfo = new HashMap<>();
                        callingInfo.put("calling", receiverUserID);


                        userRef.child(senderUserID).child("Calling").updateChildren(callingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    final HashMap<String, Object> ringingInfo = new HashMap<>();
                                    ringingInfo.put("ringing", senderUserID);

                                    userRef.child(receiverUserID).child("Ringing").updateChildren(ringingInfo);
                                }
                            }
                        });




                }else if(checker.equals("clicked")){
                    Log.d("checked ", "else if clicked");

                    checker="clicked";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //receiver ki call ha
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(senderUserID).hasChild("Ringing") && !dataSnapshot.child(senderUserID).hasChild("Calling")) {

                    makeCallBtn.setVisibility(View.VISIBLE);
                }

                if (dataSnapshot.child(receiverUserID).child("Ringing").hasChild("picked")) {

                    //mediaPlayer.stop();

                    Intent intent=new Intent(getApplicationContext(),AttendVideoCallActivity.class);
                    intent.putExtra("userid",receiverUserID);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
