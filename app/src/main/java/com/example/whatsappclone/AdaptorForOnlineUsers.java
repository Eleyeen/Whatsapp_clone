package com.example.whatsappclone;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdaptorForOnlineUsers  extends RecyclerView.Adapter<AdaptorForOnlineUsers.ImageViewHolder> {


    private Context mcontext;
    private List<Contacts> uploads;

    public AdaptorForOnlineUsers(Context applicationContext, List<Contacts> uploads){
        this.uploads=uploads;
        mcontext=applicationContext;

    }

    @NonNull
    @Override
    public AdaptorForOnlineUsers.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(mcontext).inflate(R.layout.users_display_layout,parent,false);
        return new AdaptorForOnlineUsers.ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdaptorForOnlineUsers.ImageViewHolder holder, final int position) {
        final Contacts contacts= uploads.get(position);
        holder.userName.setText(contacts.getName());
        holder.userStatus.setText(contacts.getStatus());
        Picasso.get().load(contacts.getImage()).into(holder.profileImage);

        holder.online.setVisibility(View.INVISIBLE);

        if(contacts.getState().equals("online")){

            holder.online.setVisibility(View.VISIBLE);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String visit_user_id=contacts.getUid();

                Intent intent=new Intent(mcontext,ProfileActivity.class);
                intent.putExtra("visit_user_id",visit_user_id);
                mcontext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView online;
        TextView userName , userStatus;
        CircleImageView profileImage;

        public ImageViewHolder(View itemView) {
            super(itemView);


            userName=(TextView)itemView.findViewById(R.id.user_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_profile_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.user_profile_image);
            online=(ImageView)itemView.findViewById(R.id.online);
        }
    }
}
