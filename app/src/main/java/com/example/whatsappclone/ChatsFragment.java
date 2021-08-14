package com.example.whatsappclone;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private View PrivateChatView;
    private RecyclerView chatsView;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference chatRef;
    private DatabaseReference UserRef;
    private String calledBy="";
    private String requestUserStatus;
    private DatabaseReference ContactRef;
    private DatabaseReference userRef;

    public ChatsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        PrivateChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsView = (RecyclerView) PrivateChatView.findViewById(R.id.chats_list);
        chatsView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return PrivateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {

                final String userIDs=getRef(position).getKey();
                final String[] requestProfileImage = {"default image"};
                final String[] requestUsername = {"dd"};

                UserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {

                                requestProfileImage[0] = dataSnapshot.child("image").getValue().toString();

                                requestUsername[0] = dataSnapshot.child("name").getValue().toString();
                                requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(requestUsername[0]);
                                holder.userStatus.setText("Last Seen: " + "\n" + "Date " + " Time");

                                Picasso.get().load(requestProfileImage[0]).placeholder(R.drawable.accountyellow).into(holder.profileImage);
                            }
                            else {

                                requestUsername[0] = dataSnapshot.child("name").getValue().toString();
                                requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(requestUsername[0]);
                                holder.userStatus.setText("Last Seen: " +"\n"+ "Date "+" Time");

                            }
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("visit_user_id",userIDs);
                                chatIntent.putExtra("visit_user_name", requestUsername[0]);
                                chatIntent.putExtra("visit_user_image", requestProfileImage[0]);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view=LayoutInflater.from(getContext()).inflate(R.layout.users_display_layout,parent,false);
                ChatsViewHolder chatsViewHolder=new ChatsViewHolder(view);
                return chatsViewHolder;
            }
        };
        chatsView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            userName = (TextView) itemView.findViewById(R.id.user_profile_name);
            userStatus = (TextView) itemView.findViewById(R.id.user_profile_status);
            profileImage = (CircleImageView) itemView.findViewById(R.id.user_profile_image);
        }
    }
}
