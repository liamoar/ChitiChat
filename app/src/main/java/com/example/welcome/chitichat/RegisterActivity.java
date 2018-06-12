package com.example.welcome.chitichat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private DatabaseReference database;
    private TextInputLayout name;
    private TextInputLayout email;
    private TextInputLayout password;
    private Button regbtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //progressbar
        progressDialog=new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();//firebase auth

        //toolbar
        toolbar=(Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //back button

        name=findViewById(R.id.tvlogin);
        email=findViewById(R.id.tvregemail);
        password=findViewById(R.id.tvregpassword);
        regbtn=findViewById(R.id.btnregcreate);

        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name=name.getEditText().getText().toString();
                String display_email=email.getEditText().getText().toString();
                String display_password=password.getEditText().getText().toString();
                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(display_email) || !TextUtils.isEmpty(display_password)){

                   progressDialog.setTitle("Regestring user");
                   progressDialog.setMessage("please wait..");
                   progressDialog.setCanceledOnTouchOutside(false);
                   progressDialog.show();
                    register_user(display_name,display_email,display_password);
                }

            }
        });
    }

    private void register_user(final String display_name, String display_email, String display_password) {

        mAuth.createUserWithEmailAndPassword(display_email, display_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                            String uid=current_user.getUid();


                            database=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            String device_token = FirebaseInstanceId.getInstance().getToken();

                            HashMap<String, String> userMap=new HashMap<>();
                            userMap.put("name",display_name);
                            userMap.put("status", "Hi there i am using chitchat app");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token",device_token);


                            database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        progressDialog.dismiss();
                                        Intent i=new Intent(RegisterActivity.this,MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Authentication failed... Try again..",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
}
