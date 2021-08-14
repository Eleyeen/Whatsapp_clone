package com.example.whatsappclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaCas;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AttendVideoCallActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY = "46955014";
    private static String SESSION_ID = "1_MX40Njk1NTAxNH5-MTYwMjc0ODU3NjkwNn4wNURyeWlGeWpOZDF3MzFTWUpjdmwxRUh-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Njk1NTAxNCZzaWc9MDQyOTNkNjY4NjQzNmM1MGZkNWU4ODRiYTZmMGQzMDlmMjg3Y2RjZDpzZXNzaW9uX2lkPTFfTVg0ME5qazFOVEF4Tkg1LU1UWXdNamMwT0RVM05qa3dObjR3TlVSeWVXbEdlV3BPWkRGM016RlRXVXBqZG13eFJVaC1mZyZjcmVhdGVfdGltZT0xNjAyNzQ4NjE3Jm5vbmNlPTAuODU5ODU5ODU4OTU3NTYyNiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjA1MzQ0MjE0JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";

    private static final String LOG_TAG = AttendVideoCallActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private ImageView closeVideoChatBtn;
    private DatabaseReference userRef;

    private String userID = "";

    //framLayout both publisher and subscriber


    private FrameLayout mSubscriberViewController;
    private FrameLayout mPublisherViewController;

    //sessions

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private String receiverId;
    private String comingReceiverID;

    //mp3 file


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_video_call);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

       receiverId = getIntent().getStringExtra("userid");



        closeVideoChatBtn = findViewById(R.id.close_chat_video);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(userID).hasChild("Ringing")) {

                            userRef.child(userID).child("Ringing").removeValue();

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }





                            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                            startActivity(intent);
                            finish();


                        }

                        if (dataSnapshot.child(userID).hasChild("Calling")) {

                            userRef.child(userID).child("Calling").removeValue();


                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }




                            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                            startActivity(intent);
                            finish();
                        } else {

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                ////

            }
        });


        requestPermissions();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, AttendVideoCallActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    public void requestPermissions() {

        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if (EasyPermissions.hasPermissions(this, perms)) {


            mSubscriberViewController = findViewById(R.id.subscriber_container);
            mPublisherViewController = findViewById(R.id.publisher_container);

            //1..initialize session and connect to the session..
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(AttendVideoCallActivity.this);
            mSession.connect(TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, "This App Needs Mic And Camera Permission, Please Allow..", RC_VIDEO_APP_PERM, perms);
        }
    }


    //2..publishing stream to session..
    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        mPublisherViewController.addView(mPublisher.getView());


        if (mPublisher.getView() instanceof GLSurfaceView) {


            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);

        }
        mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(Session session) {

    }

    //3..subsriving the stream to session..
    @Override
    public void onStreamReceived(Session session, Stream stream) {

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();

            mSession.subscribe(mSubscriber);

            mSubscriberViewController.addView(mSubscriber.getView());

        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        if (mSubscriber != null) {

            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
        finish();

        mSubscriberViewController.removeAllViews();
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }
}
