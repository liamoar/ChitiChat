package com.example.welcome.chitichat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All user");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(UsersActivity.this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_view_layout,
                UsersViewHolder.class,
                mUsersDatabase

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setUserImage(model.getThumb_image(), getApplicationContext());
                //for profile that is to view other profile by clicking in view
                final String user_id=getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i=new Intent(UsersActivity.this,ProfileActivity.class);
                        i.putExtra("user_id",user_id);
                        startActivity(i);
                    }
                });
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView mUserNameView = mView.findViewById(R.id.tv_userView_name);
            mUserNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.tv_userView_status);
            userStatusView.setText(status);
        }
        public void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView=mView.findViewById(R.id.userView_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profile).into(userImageView);
        }
    }

}
