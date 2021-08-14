package com.example.whatsappclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText InputPhoneNo, InputVerificationCode;
    private Button SendVeriCodeNo;
    private Button verifyButton;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private ProgressDialog progressDialog;

    private String mVerificationId;
    public PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        InputPhoneNo = (EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        SendVeriCodeNo = (Button) findViewById(R.id.send_verification_button);
        verifyButton = (Button) findViewById(R.id.verify_button);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        SendVeriCodeNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setTitle("Phone Verification");
                progressDialog.setMessage("Please wait, while we are authenticaing your phone...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                String phoneNumber = InputPhoneNo.getText().toString().trim();

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(getApplicationContext(), "Pls, Enter the Phone Number Here", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                } else {

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            mCallbacks);

                }
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {


                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                progressDialog.dismiss();

                SendVeriCodeNo.setVisibility(View.VISIBLE);
                InputPhoneNo.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

                Toast.makeText(PhoneLoginActivity.this, "Invalid PhoneNo, Please Enter your valid phone Number with your country code.", Toast.LENGTH_SHORT).show();
                Log.d("invalid", "Error is "+e.getMessage());
            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "Code has been Sent , pls verify your Account", Toast.LENGTH_SHORT).show();


                SendVeriCodeNo.setVisibility(View.INVISIBLE);
                InputPhoneNo.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
                // ...
            }
        };


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations, you are loggin successfuly...", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        } else {
                            Toast.makeText(PhoneLoginActivity.this, "Error " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendVeriCodeNo.setVisibility(View.INVISIBLE);
                InputPhoneNo.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode)) {

                    Toast.makeText(getApplicationContext(), "Please Write Enter your PhoneNo First", Toast.LENGTH_SHORT).show();
                } else {

                    progressDialog.setTitle("Code Verification");
                    progressDialog.setMessage("Please wait, while we are verification teh code ...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

    }

    private void SendUserToMainActivity() {

        Intent PhoneIntent = new Intent(this, MainActivity.class);
        startActivity(PhoneIntent);
        finish();
    }


}
