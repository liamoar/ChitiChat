package com.example.welcome.chitichat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

     private  TextInputLayout email;
     private TextInputLayout password;
     private Button login;
    private Toolbar toolbar;
     private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        progressDialog =new ProgressDialog(this);
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login to your account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        email=(TextInputLayout) findViewById(R.id.tvlogin_email);
        password=(TextInputLayout) findViewById(R.id.tvlogin_password);
        login=findViewById(R.id.btn_login_login);






       login.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String display_email=email.getEditText().getText().toString();
               String display_password=password.getEditText().getText().toString();

               if(!TextUtils.isEmpty(display_email) || !TextUtils.isEmpty(display_password)){
                   progressDialog.setTitle("Logging in");
                   progressDialog.setMessage("please wait..");
                   progressDialog.setCanceledOnTouchOutside(false);
                   progressDialog.show();

                   loginUser(display_email,display_password);

               }
           }
       });

    }

    private void loginUser(String display_email, String display_password) {
        mAuth.signInWithEmailAndPassword(display_email, display_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();

                           String  current_user_id= mAuth.getCurrentUser().getUid();
                           String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent i=new Intent(LoginActivity.this,MainActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                }
                           });




                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.hide();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

}
