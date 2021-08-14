package com.example.whatsappclone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private String messageReceiverID, messageReceiverName;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private String messageReceiverimage;
    private Toolbar mToolbar;
    private EditText inputMessage;
    private FirebaseAuth mAuth;
    private String messageSenderID;
    private DatabaseReference rootRef;
    public ImageButton sendMsgBtn;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private DatabaseReference userRef;
    private String calledBy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverimage = getIntent().getExtras().get("visit_user_image").toString();


        initializeController();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverimage).placeholder(R.drawable.accountyellow).into(userImage);

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String messageText = inputMessage.getText().toString();

                if (TextUtils.isEmpty(messageText)) {
                    Toast.makeText(getApplicationContext(), "Write message First", Toast.LENGTH_SHORT).show();
                } else {

                    String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
                    String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

                    DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

                    String messagePushID = userMessageKeyRef.getKey();

                    Map messageBodyType = new HashMap();
                    messageBodyType.put("message", messageText);
                    messageBodyType.put("type", "text");
                    messageBodyType.put("from", messageSenderID);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageBodyType);
                    messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageBodyType);

                    rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChatActivity.this, "Message Sent Successfully !", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            inputMessage.setText("");
                        }
                    });
                }
            }
        });


    }

    private void initializeController() {

        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionViewBar = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionViewBar);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        inputMessage = (EditText) findViewById(R.id.input_message);
        sendMsgBtn = (ImageButton) findViewById(R.id.send_message_button);

        messageAdapter = new MessageAdapter(messagesList, getApplicationContext());
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_user_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.send_order_option_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.videocall) {

            Intent intent = new Intent(getApplicationContext(), VideoCallActivity.class);
            intent.putExtra("user_visit_id", messageReceiverID);
            startActivity(intent);
            finish();

        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();


        rootRef.child("Message").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkForReceivingCall();

    }

    private void checkForReceivingCall() {

        //for vidio call
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("ringing")) {

                            calledBy = dataSnapshot.child("ringing").getValue().toString();


                            //achanak sy open hoti ha jab call ati ha
                            Intent callingintent = new Intent(getApplicationContext(), VideoCallActivity.class);
                            callingintent.putExtra("user_visit_id", calledBy);
                            startActivity(callingintent);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
