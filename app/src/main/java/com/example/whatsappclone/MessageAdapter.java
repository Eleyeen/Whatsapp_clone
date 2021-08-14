package com.example.whatsappclone;

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Context m;

    public MessageAdapter(List<Messages> userMessageList, Context m){

        this.userMessageList=userMessageList;
        this.m=m;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);

        mAuth=FirebaseAuth.getInstance();

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        //this i you person who is online right now..
        final String messageSenderID=mAuth.getCurrentUser().getUid();

        //Message is model class..pojo class
        final Messages messages=userMessageList.get(position);


        //message is model class here from where we retrieve data
        final String fromUSerId=messages.getFrom();
        final String fromMessageType=messages.getType();


        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUSerId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("image")){

                        String RetrieveImage=dataSnapshot.child("image").getValue().toString();

                        Picasso.get().load(RetrieveImage).placeholder(R.drawable.accountyellow).into(holder.receiverProfileImage);


                    if(fromMessageType.equals("text")){

                        holder.receiverMessageText.setVisibility(View.INVISIBLE);
                        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
                        holder.senderMessageText.setVisibility(View.INVISIBLE);

                        if(fromUSerId.equals(messageSenderID)){

                            holder.senderMessageText.setVisibility(View.VISIBLE);
                            holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                            holder.senderMessageText.setTextColor(Color.BLACK);
                            holder.senderMessageText.setText(messages.getMessage());
                        }else {


                            holder.receiverMessageText.setVisibility(View.VISIBLE);
                            holder.receiverProfileImage.setVisibility(View.VISIBLE);
                            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                            holder.receiverMessageText.setTextColor(Color.BLACK);
                            holder.receiverMessageText.setText(messages.getMessage());
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

       public TextView senderMessageText, receiverMessageText;
       public CircleImageView receiverProfileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            senderMessageText=(TextView)itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=(TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }
}
