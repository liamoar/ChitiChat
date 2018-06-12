package com.example.welcome.chitichat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

     private Button startregbtn;
     private Button startloginbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        startregbtn=findViewById(R.id.btn_start_register);
        startloginbtn=findViewById(R.id.btn_start_login);

        startregbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(i);

            }
        });

        startloginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(StartActivity.this,LoginActivity.class);
                startActivity(i);

            }
        });
    }

}
