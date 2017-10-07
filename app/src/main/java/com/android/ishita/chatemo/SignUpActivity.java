package com.android.ishita.chatemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


public class SignUpActivity extends AppCompatActivity{

    // UI references.
    private AutoCompleteTextView mPhoneNumber;
    private EditText mOneTimePassword;
    private View mProgressView;
    private View mLoginFormView;
    private com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationID;
    private PhoneAuthCredential phoneAuthCredential;
    private FirebaseAuth mAuth;
    private Button verify_button;
    private Button verification_otp;
    private static String phone_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth= FirebaseAuth.getInstance();
        mOneTimePassword=(EditText)findViewById(R.id.one_time_password);
        //mOneTimePassword.setVisibility(View.GONE);
        verify_button= (Button)findViewById(R.id.verify_phone_button);
        verification_otp= (Button)findViewById(R.id.verify_button);
        verify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();

            }
        });

    }

    private void signUp() {
        mPhoneNumber = (AutoCompleteTextView) findViewById(R.id.phone);
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredentials) {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Verified!", Toast.LENGTH_SHORT).show();
                Log.v("SignUpActivity","verified!");
                phoneAuthCredential = phoneAuthCredentials;
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(), "Sign up failed", Toast.LENGTH_SHORT).show();
                if (e instanceof FirebaseAuthInvalidCredentialsException)
                    Toast.makeText(getApplicationContext(), "Sign up failed due to 1", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                //mOneTimePassword.setVisibility(View.VISIBLE);

                verificationID = s;
                super.onCodeSent(s, forceResendingToken);
                verification_otp.setEnabled(true);

            }
        };
        View focusView = null;
        boolean cancel = false;
        phone_number = mPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(phone_number)) {
            mPhoneNumber.setError("This is required!");
            focusView = mPhoneNumber;
            cancel = true;
        } else if (phone_number.length() != 10) {
            mPhoneNumber.setError("Wrong number!");
            focusView = mPhoneNumber;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(mPhoneNumber.getText().toString(), 60, TimeUnit.SECONDS, this, mCallbacks);

        }

        verification_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp= mOneTimePassword.getText().toString();
                View focusView=null;
                boolean cancel= false;
                if(TextUtils.isEmpty(otp)){
                    focusView=mOneTimePassword;
                    mOneTimePassword.setError("This can't be empty!");
                    cancel=true;
                }
                if(cancel){
                    focusView.requestFocus();
                }
                else
                phoneAuthCredential = PhoneAuthProvider.getCredential(verificationID, mOneTimePassword.getText().toString());
            }
        });

    }

    private void signInWithPhoneAuthCredentials(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_SHORT).show();
                    FirebaseUser user= task.getResult().getUser();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Sign in failed!",Toast.LENGTH_SHORT).show();
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                        Toast.makeText(getApplicationContext(),"Invalid Code!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mLoginFormView= findViewById(R.id.login_form);
            mProgressView=findViewById(R.id.login_progress);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}