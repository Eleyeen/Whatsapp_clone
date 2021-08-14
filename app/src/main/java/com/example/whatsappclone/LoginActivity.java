package com.example.whatsappclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private FirebaseUser currentUser;
    private EditText UserEmail,userPassword;
    private TextView NeedAccountLink, ForgetPassowrdLink;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference userRef;
    private String pushid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializefields();

        progressDialog=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

    }

    private void initializefields() {

        UserEmail=(EditText)findViewById(R.id.login_email);
        userPassword=(EditText)findViewById(R.id.login_password);
        ForgetPassowrdLink=(TextView)findViewById(R.id.forget_password_link);
        NeedAccountLink=(TextView) findViewById(R.id.already_have_account);


    }

    @Override
    protected void onStart() {
        super.onStart();

        /////he is online here ..send to mainativity
        if (currentUser != null) {
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity() {



        Intent MainIntent = new Intent(this, MainActivity.class);
        MainIntent.putExtra("checker","not");
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    public void LoginButton(View view) {

        String email=UserEmail.getText().toString();
        String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email shouldn't be empty", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password shouldn't be empty", Toast.LENGTH_SHORT).show();
        }else {

            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please Wait, While we are creating new account for you!");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String currentUserID=mAuth.getCurrentUser().getUid();

                        String deviceToken= FirebaseInstanceId.getInstance().getToken();
                        userRef.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(LoginActivity.this, "Login successfully !", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                                progressDialog.dismiss();
                            }
                        });

                    }else {
                        Toast.makeText(LoginActivity.this, "Error "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    }
                }
            });
        }
    }

    public void PhoneButton(View view) {
        Intent phoneIntent = new Intent(this, PhoneLoginActivity.class);
        startActivity(phoneIntent);

    }

    public void NeedAccountButton(View view) {

        SendUserToRegisterActivity();
    }
}
