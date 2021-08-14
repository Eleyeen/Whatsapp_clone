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
import android.widget.Button;
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

public class ContactsFragment extends Fragment {

    private View ContactView;
    private RecyclerView myContactList;
    private DatabaseReference ContactRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String calledBy="";

    public ContactsFragment() {


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ContactView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        myContactList = (RecyclerView) ContactView.findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));

        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");



        return ContactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //we are doing video code here......




        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull Contacts model) {

                //jis ka account samny ho..like kisi opposite id ka
                final String userId=getRef(position).getKey();


                userRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild("image")){

                            String UserImage=dataSnapshot.child("image").getValue().toString();
                            String ProfileName=dataSnapshot.child("name").getValue().toString();
                            String ProfileStatus=dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(ProfileName);
                            holder.userStatus.setText(ProfileStatus);

                            Picasso.get().load(UserImage).placeholder(R.drawable.accountyellow).into(holder.profileImage);
                        }else {

                            String ProfileName=dataSnapshot.child("name").getValue().toString();
                            String ProfileStatus=dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(ProfileName);
                            holder.userStatus.setText(ProfileStatus);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ContactViewHolder contactViewHolder=new ContactViewHolder(view);
                return  contactViewHolder;

            }
        };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;


        public ContactViewHolder(View itemView) {
            super(itemView);

            userName=(TextView)itemView.findViewById(R.id.user_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_profile_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.user_profile_image);

        }
    }


}
