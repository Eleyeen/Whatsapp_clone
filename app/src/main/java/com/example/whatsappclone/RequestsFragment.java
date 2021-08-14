package com.example.whatsappclone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {

    private View RequestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference ChatRequestsRef, ContactRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RequestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestList = (RecyclerView) RequestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        return RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById(R.id.acceptBtn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.cancelBtn).setVisibility(View.VISIBLE);

                final String list_users_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("received")) {

                                usersRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        final String requestUsername;
                                        String requestUserStatus;
                                        if (dataSnapshot.hasChild("image")) {

                                            requestUsername = dataSnapshot.child("name").getValue().toString();
                                            requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                            final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                            holder.userName.setText(requestUsername);
                                            holder.userStatus.setText("He wants to join with you !");
                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.accountyellow).into(holder.profileImage);
                                        } else {
                                            requestUsername = dataSnapshot.child("name").getValue().toString();
                                            requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(requestUsername);
                                            holder.userStatus.setText("He wants to join with you !");
                                        }
                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                final CharSequence options[] = new CharSequence[]{

                                                        "Accept",
                                                        "Cancel"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUsername + " Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if (i == 0) {

                                                            ContactRef.child(currentUserID).child(list_users_id).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                        ContactRef.child(list_users_id).child(currentUserID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {

                                                                                    ChatRequestsRef.child(currentUserID).child(list_users_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                ChatRequestsRef.child(list_users_id).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {

                                                                                                            Toast.makeText(getContext(), "New Contact is Saved !", Toast.LENGTH_SHORT).show();
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

                                                                }
                                                            });
                                                        }
                                                        if (i == 1) {

                                                            ChatRequestsRef.child(currentUserID).child(list_users_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        ChatRequestsRef.child(list_users_id).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {

                                                                                    Toast.makeText(getContext(), "Contact is deleted!", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });
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

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                RequestViewHolder requestViewHolder = new RequestViewHolder(view);
                return requestViewHolder;
            }
        };
        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, userStatus;
        CircleImageView profileImage;
        private Button acceptButton, cancelButton;

        public RequestViewHolder(View itemView) {
            super(itemView);

            userName = (TextView) itemView.findViewById(R.id.user_profile_name);
            userStatus = (TextView) itemView.findViewById(R.id.user_profile_status);
            profileImage = (CircleImageView) itemView.findViewById(R.id.user_profile_image);
            acceptButton = (Button) itemView.findViewById(R.id.acceptBtn);
            cancelButton = (Button) itemView.findViewById(R.id.cancelBtn);
        }
    }
}
