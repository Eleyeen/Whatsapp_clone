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

import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

class AdaptorForStatus  extends RecyclerView.Adapter<AdaptorForStatus.ImageViewHolder>{

    private Context mcontext;
    private List<StatusModel> uploads;
    DeleteAdapterEvent deleteAdapterEvent;

    public AdaptorForStatus(DeleteAdapterEvent deleteAdapterEvent, Context context, ArrayList<StatusModel> uploads) {
        mcontext=context;
        this.uploads=uploads;
        this.deleteAdapterEvent=deleteAdapterEvent;
    }




        @NonNull
        @Override
        public AdaptorForStatus.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(mcontext).inflate(R.layout.cardview_status,parent,false);
            return new AdaptorForStatus.ImageViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull AdaptorForStatus.ImageViewHolder holder, int position) {
            final StatusModel uploadCurrent = uploads.get(position);
            holder.statusName.setText(uploadCurrent.getName());
            holder.date.setText(uploadCurrent.getDate());
            holder.time.setText(uploadCurrent.getTime());


            Picasso.get().load(uploadCurrent.getImage())
                    .fit()
                    .placeholder(R.drawable.accountyellow)
                    .centerCrop()
                    .into(holder.statusImage);

            holder.round.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent=new Intent(mcontext,DetailStatusActivity.class);
                    intent.putExtra("uid",uploadCurrent.getUid());
                    mcontext.startActivity(intent);
                }
            });


            deleteAdapterEvent.OnDeleteClick(uploadCurrent.getUid());


        }

        @Override
        public int getItemCount() {
            return uploads.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{


            CircleImageView statusImage;
            TextView statusName,time,date;
            ImageView round;

            public ImageViewHolder(View itemView) {
                super(itemView);

                statusImage=itemView.findViewById(R.id.statusImagecard);
                statusName=itemView.findViewById(R.id.namecard);
                time=itemView.findViewById(R.id.timecard);
                date=itemView.findViewById(R.id.datecard);
                round=itemView.findViewById(R.id.statusround);


            }
        }
    public interface DeleteAdapterEvent{
        void OnDeleteClick(String id);
    }
}
