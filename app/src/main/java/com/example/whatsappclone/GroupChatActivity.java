package com.example.whatsappclone;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {


    private Toolbar mtoolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView scrollView;
    private TextView displayTextmessage;


    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private FirebaseAuth mAuth;

    private DatabaseReference roofRef, GroupNameref, GroupMessageKeyRef;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, currentGroupName + " ", Toast.LENGTH_SHORT).show();

        IntializedField();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        roofRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameref = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        getUserInfo();

    }

    @Override
    protected void onStart() {
        super.onStart();

        ///// we use this b/c to show message in real time ..not need to refresh..
        GroupNameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
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
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {

            String chatDate=(String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String)((DataSnapshot)iterator.next()).getValue();
            String chatName=(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String)((DataSnapshot)iterator.next()).getValue();

            displayTextmessage.append(chatName +" :\n" + chatMessage +"\n"+ chatTime +"      "+ chatDate +"\n\n\n");

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }

    private void getUserInfo() {

        roofRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void IntializedField() {

        mtoolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);

        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextmessage = (TextView) findViewById(R.id.group_chat_text_display);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);

    }

    public void SendMessageButton(View view) {

        SaveMessageInfoToDatabase();

        userMessageInput.setText("");
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameref.push().getKey();

        if (TextUtils.isEmpty(message)) {

            Toast.makeText(this, "Message can't be empty ! ", Toast.LENGTH_SHORT).show();
        } else {

            Calendar calendarForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate = currentDateFormat.format(calendarForDate.getTime());


            Calendar calendarForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh mm,a");
            currentTime = currentTimeFormat.format(calendarForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameref.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameref.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);


        }
    }
}
