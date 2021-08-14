package com.example.whatsappclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {


    private FirebaseUser currentUser;
    private EditText UserEmail, userPassword;
    private TextView Alreadyhaveaccount;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private DatabaseReference RoofRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializefields();

        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        RoofRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser !=null){

            Intent loginIntent = new Intent(this, MainActivity.class);
            startActivity(loginIntent);
            finish();

        }
    }

    private void initializefields() {

        UserEmail = (EditText) findViewById(R.id.login_email);
        userPassword = (EditText) findViewById(R.id.login_password);
        Alreadyhaveaccount = (TextView) findViewById(R.id.already_have_account);

    }

    public void createAccountButton(View view) {

        String email = UserEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email shouldn't be empty", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password shouldn't be empty", Toast.LENGTH_SHORT).show();
        } else {

           /*String currentUserID=auth.getCurrentUser().getUid();
            RoofRef.child("Users").child(currentUserID).setValue("");*/

            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please Wait, While we are creating new account for you!");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                       /* String deviceToken = FirebaseInstanceId.getInstance().getToken();

                       RoofRef.child(currentUser.getUid()).child("device_token").setValue(deviceToken);
*/
                        Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                        SendUserToMainActivity();

                    } else {
                        Toast.makeText(RegisterActivity.this, "Error" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }

    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(this, SettingActivity.class);
        MainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    public void alreadyAccountButton(View view) {
        SendUserToLoginActivity();
    }
}
