package com.example.welcome.chitichat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ViewPager mviewPager;
    private SectionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mviewPager=findViewById(R.id.main_viewpager);

        toolbar=(Toolbar)findViewById(R.id.mainpage_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chit Chat");
   //tabs
        mSectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        mviewPager.setAdapter(mSectionPagerAdapter);
        mTabLayout=findViewById(R.id.maintabs);
        mTabLayout.setupWithViewPager(mviewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            SendToStart();

        }
    }

    private void SendToStart() {

        Intent i=new Intent(MainActivity.this,StartActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()== R.id.main_logout_btn){
             FirebaseAuth.getInstance().signOut();
             SendToStart();
         }

         if(item.getItemId() == R.id.main_accoutnsetting_btn){
            Intent i = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(i);

        }

        if(item.getItemId() == R.id.main_users_btn){
            Intent i = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(i);

        }

         return true;
    }
}


