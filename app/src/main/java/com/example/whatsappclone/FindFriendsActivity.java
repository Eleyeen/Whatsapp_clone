package com.example.whatsappclone;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecycleList;
    private DatabaseReference usersRef;
    private AdaptorForOnlineUsers adaptorForonlineUsers;
    private ArrayList<Contacts> uploads;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        FindFriendsRecycleList=(RecyclerView)findViewById(R.id.find_friends_recycler_List);
        FindFriendsRecycleList.setLayoutManager(new LinearLayoutManager(this));

        mAuth= FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        // supprot.v7. import used here
        mToolbar=(Toolbar)findViewById(R.id.main_find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        uploads = new ArrayList<Contacts>();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contacts upload = snapshot.getValue(Contacts.class);
                    if(!upload.getUid().equals(currentUserID)){


                        uploads.add(upload);

                        adaptorForonlineUsers = new AdaptorForOnlineUsers(getApplicationContext(), uploads);


                        FindFriendsRecycleList.setAdapter(adaptorForonlineUsers);
                        adaptorForonlineUsers.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
