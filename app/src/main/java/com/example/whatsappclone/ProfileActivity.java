package com.example.whatsappclone;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID;
    private CircleImageView profileImage;
    private TextView userName, userStatus;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String current_State;
    private Button sendMessgebtn;
    private DatabaseReference chatRequestRef;
    private Button CancelMessgebtn;
    private DatabaseReference ContactsRef;
    private DatabaseReference NotificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recieverUserID = getIntent().getExtras().get("visit_user_id").toString();
        Toast.makeText(this, "ID is " + recieverUserID, Toast.LENGTH_SHORT).show();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        initializedField();
        retrieveInfo();
    }

    private void retrieveInfo() {

        userRef.child(recieverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {

                    String UserImage = dataSnapshot.child("image").getValue().toString();
                    String UserName = dataSnapshot.child("name").getValue().toString();
                    String UserStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(UserImage).placeholder(R.drawable.accountyellow).into(profileImage);
                    userName.setText(UserName);
                    userStatus.setText(UserStatus);


                    ManageChatRequests();
                } else {

                    String UserName = dataSnapshot.child("name").getValue().toString();
                    String UserStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(UserName);
                    userStatus.setText(UserStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Error ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ManageChatRequests() {
        //when we go back to other activity it will not show send request button...we shold implment this...check steps

        chatRequestRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(recieverUserID)) {

                    String request_type = dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {

                        current_State = "request_sent";
                        sendMessgebtn.setText("Cancel Chat Request");
                    }
                    else if(request_type.equals("received")){

                        current_State="request_received";
                        sendMessgebtn.setText("Accept Chat Request");

                        CancelMessgebtn.setVisibility(View.VISIBLE);
                        CancelMessgebtn.setEnabled(true);

                        CancelMessgebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CancelChatRequest();
                            }
                        });
                    }else {
                        ContactsRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.hasChild(recieverUserID)){

                                    current_State="friends";
                                    sendMessgebtn.setText("Remove this contact");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        ////////////////////////////////is that your id?..it will not show your id

        if (!recieverUserID.equals(currentUserID)) {

            sendMessgebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessgebtn.setEnabled(false);

                    if (current_State.equals("new")) {
                        sendChatReq();
                    }
                    if (current_State.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if(current_State.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (current_State.equals("friends")) {
                        RemoveSpecificContacts();
                    }
                }
            });

        } else {
            sendMessgebtn.setVisibility(View.INVISIBLE);
        }

    }

    private void RemoveSpecificContacts() {

        ContactsRef.child(currentUserID).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                   ContactsRef.child(recieverUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                sendMessgebtn.setEnabled(true);
                                current_State="new";
                                sendMessgebtn.setText("Send Message");

                                CancelMessgebtn.setVisibility(View.INVISIBLE);
                                CancelMessgebtn.setEnabled(false);

                            }else {
                                Toast.makeText(ProfileActivity.this, "Error "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Toast.makeText(ProfileActivity.this, "", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(ProfileActivity.this, "Error "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void AcceptChatRequest() {

        ContactsRef.child(currentUserID).child(recieverUserID).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    ContactsRef.child(recieverUserID).child(currentUserID).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){


                                chatRequestRef.child(currentUserID).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            chatRequestRef.child(recieverUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){

                                                        sendMessgebtn.setEnabled(true);
                                                        current_State="friends";
                                                        sendMessgebtn.setText("Remove This Contacts");

                                                        CancelMessgebtn.setVisibility(View.INVISIBLE);
                                                        CancelMessgebtn.setEnabled(false);

                                                    }else {
                                                        Toast.makeText(ProfileActivity.this, "Error "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                            Toast.makeText(ProfileActivity.this, "", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(ProfileActivity.this, "Error "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                Toast.makeText(ProfileActivity.this, "", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(ProfileActivity.this, "Error "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    Toast.makeText(ProfileActivity.this, "", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(ProfileActivity.this, "Error "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void CancelReq() {

    }

    private void CancelChatRequest() {

        chatRequestRef.child(currentUserID).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    //currentuserid called as senderuserID
                    chatRequestRef.child(recieverUserID).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendMessgebtn.setEnabled(true);
                                current_State = "new";
                                sendMessgebtn.setText("Send Message");


                                CancelMessgebtn.setVisibility(View.INVISIBLE);
                                CancelMessgebtn.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatReq() {
        chatRequestRef.child(currentUserID).child(recieverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            chatRequestRef.child(recieverUserID).child(currentUserID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                //adding notification node here

                                                HashMap<String,String> chatNotificationMap=new HashMap<>();
                                                chatNotificationMap.put("from",currentUserID);
                                                chatNotificationMap.put("type","request");

                                                NotificationRef.child(recieverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){

                                                            sendMessgebtn.setEnabled(true);
                                                            current_State = "request_sent";
                                                            sendMessgebtn.setText("Cancel Chat Request");

                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void initializedField() {
        userName = (TextView) findViewById(R.id.sendername);
        userStatus = (TextView) findViewById(R.id.senderstatus);
        profileImage = (CircleImageView) findViewById(R.id.profilepicz);
        sendMessgebtn = (Button) findViewById(R.id.sendmsgBtn);
        CancelMessgebtn = (Button) findViewById(R.id.cancelmsgBtn);


        current_State = "new";
    }

}
