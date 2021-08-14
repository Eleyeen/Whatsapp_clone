package com.example.whatsappclone;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class DetailStatusActivity extends AppCompatActivity {


    private StoriesProgressView storiesProgressView;
    private ImageView image;

    private int counter = 0;

    IFirebaseLoadDone iFirebaseLoadDone;



    private String currentUserID;
    private DatabaseReference statusRef;
    private ArrayList<DetailStatusModel> uploads;
    private String visit_id;
    private int[] resources;
    private DatabaseReference userRef;
    private String pushId;
    private String uploadId;
    private DatabaseReference detailStatusRef;
    private Button revert,resume,pause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_detail_status);

        image = (ImageView) findViewById(R.id.image);
        storiesProgressView = (StoriesProgressView) findViewById(R.id.stories);


        currentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        statusRef = FirebaseDatabase.getInstance().getReference().child("Statuses");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        detailStatusRef = FirebaseDatabase.getInstance().getReference();

        uploads=new ArrayList<>();

        visit_id=getIntent().getStringExtra("uid");
        Log.d("uid",visit_id);

        revert=findViewById(R.id.revert);
        pause=findViewById(R.id.pause);
        resume=findViewById(R.id.resume);

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                storiesProgressView.resume();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                storiesProgressView.pause();
            }
        });
        revert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();

            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });


        iFirebaseLoadDone=new IFirebaseLoadDone() {
            @Override
            public void onIFirebaseLoadSuccess(List<DetailStatusModel> statusModels) {

                storiesProgressView.setStoriesCount(uploads.size());

                storiesProgressView.setStoryDuration(4000L);

                Picasso.get().load((uploads.get(0).getImage())).into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        storiesProgressView.startStories();

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

                storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
                    @Override
                    public void onNext() {
                        if(counter < uploads.size()){
                            counter++;

                            Picasso.get().load((uploads.get(counter).getImage())).into(image);

                        }
                    }

                    @Override
                    public void onPrev() {

                        if(counter < 0){
                            counter--;

                            Picasso.get().load((uploads.get(counter).getImage())).into(image);

                        }
                    }

                    @Override
                    public void onComplete() {

                        counter=0;
                        Toast.makeText(DetailStatusActivity.this, "Done", Toast.LENGTH_SHORT).show();

                         finish();
                    }
                });


            }

            @Override
            public void onIFirebaseLoadFailed(String message) {

                Toast.makeText(DetailStatusActivity.this, ""+message, Toast.LENGTH_SHORT).show();
            }
        };

    }
    @Override
    protected void onStart() {
        super.onStart();

        detailStatusRef.child("DetailStatusesImages").child(visit_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    DetailStatusModel Dupload=snapshot.getValue(DetailStatusModel.class);

                        uploads.add(Dupload);

                }
                Log.d("uploads",uploads.toString());
                iFirebaseLoadDone.onIFirebaseLoadSuccess(uploads);
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                    iFirebaseLoadDone.onIFirebaseLoadFailed(databaseError.getMessage());
            }
        });
    }


    }

