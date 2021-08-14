package com.example.whatsappclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class StatusFragment extends Fragment implements AdaptorForStatus.DeleteAdapterEvent{


    private RecyclerView statusRecyclerView;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference UserRef;
    private CircleImageView statusImage;
    private ImageView statusRound;
    private ImageView statusPlusIcon;
    private DatabaseReference statusRef;
    private ImageView Addstatus;
    private String currentDate;
    private String currentTime;
    private int GalleryPick=1;
    private ProgressDialog progressDialog;
    private Uri ImageUri;
    private StorageReference StorageStatusRef;
    private String photoStringLink;
    private String image;
    private String name;
    public ArrayList<StatusModel> uploads;
    private AdaptorForStatus adaptorForStatus;
    private String pushid;
    private DatabaseReference detailStatusRef;
    private String uploadId;
    private String comingid;
    private String comingnpic;

    public StatusFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_status, container, false);

         progressDialog=new ProgressDialog(getContext());

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd,yyyy");
        currentDate=currentDateFormat.format(calendar.getTime());

        Calendar calendar2=Calendar.getInstance();
        SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
        currentTime=currentTimeFormat.format(calendar2.getTime());


        statusImage = (CircleImageView) v.findViewById(R.id.statusImage);
        statusRound = (ImageView) v.findViewById(R.id.statusround);
        statusPlusIcon = (ImageView) v.findViewById(R.id.addicon);
        Addstatus = (ImageView) v.findViewById(R.id.addStatus);

        statusRecyclerView = (RecyclerView) v.findViewById(R.id.statusRecyclerView);
        statusRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();





        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        detailStatusRef = FirebaseDatabase.getInstance().getReference();
        statusRef = FirebaseDatabase.getInstance().getReference().child("Statuses");
        StorageStatusRef = FirebaseStorage.getInstance().getReference().child("Status Image");


        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    image=dataSnapshot.child("image").getValue().toString();
                    name=dataSnapshot.child("name").getValue().toString();
                    pushid=dataSnapshot.child("pushid").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.accountyellow).into(statusImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        Addstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, GalleryPick);

            }
        });

        //recycelrView ka kam







        return v;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {

            ImageUri = data.getData();

            progressDialog.setTitle("Saving Image Now");
            progressDialog.setMessage("Please Wait, Image is gonna Save !");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            StorageReference filePath = StorageStatusRef.child(currentUserID + ".jpg");
            filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(getContext(), "Profile Image updated Successfully !", Toast.LENGTH_SHORT).show();

                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            photoStringLink = uri.toString();

                            // String uploadId = statusRef.push().getKey();


                            HashMap profileMap = new HashMap();
                            profileMap.put("uid", currentUserID);
                            profileMap.put("name", name);
                            profileMap.put("time", currentTime);
                            profileMap.put("date", currentDate);
                            profileMap.put("image", photoStringLink);
                            statusRef.child(pushid).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Image saved in database successfully", Toast.LENGTH_SHORT).show();

                                        uploadId = UserRef.push().getKey();

                                        HashMap info=new HashMap();
                                        info.put("image",photoStringLink);

                                        detailStatusRef.child("DetailStatusesImages").child(currentUserID).child(uploadId).updateChildren(info);

                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Error " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                    });
                }
            });

        }
    }

    @Override
    public void onStart() {
        super.onStart();



        uploads= new ArrayList<>();
            uploads.clear();

            statusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        StatusModel upload=snapshot.getValue(StatusModel.class);
                        if(!upload.getUid().equals(currentUserID)) {

                            uploads.add(upload);

                            adaptorForStatus = new AdaptorForStatus(StatusFragment.this::OnDeleteClick,getContext(), uploads);

                            statusRecyclerView.setAdapter(adaptorForStatus);




                        }

                        if(upload.getUid().equals(currentUserID)){

                               comingnpic=upload.getImage();
                        }

                    }
                }



                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    @Override
    public void OnDeleteClick(String id) {

        comingid=id;

      statusPlusIcon.setVisibility(View.INVISIBLE);
        statusRound.setVisibility(View.VISIBLE);
        Picasso.get().load(comingnpic).placeholder(R.drawable.accountyellow).into(statusImage);

    }

}
