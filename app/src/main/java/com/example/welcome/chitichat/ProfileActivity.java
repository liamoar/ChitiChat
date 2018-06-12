package com.example.welcome.chitichat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileStatus;
    private TextView mProfileFriend;
    private Button mProfileSendRestbtn, mProfileDeclineRestbtn;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationdatabase;
    private FirebaseUser mCurrent_user;

    private ProgressDialog mProgress;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationdatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.tv_profile_name);
        mProfileStatus = findViewById(R.id.tv_profile_status);
        mProfileFriend = findViewById(R.id.tv_profile_friends);
        mProfileSendRestbtn = findViewById(R.id.btn_profile_sendReqest);
        mProfileDeclineRestbtn = findViewById(R.id.btn_profile_declineReqest);

        mProgress = new ProgressDialog(ProfileActivity.this);  //1--7--
        mProgress.setTitle("loading user data");
        mProgress.setMessage("Please wait while we load user data");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mCurrent_state = "not_friends";

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();


                //part---17---showing image status and name in profile activity
                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.profile).into(mProfileImage);

                //---------------------------friends list/request feature----
                mFriendReqDatabase.child(mCurrent_user.getUid()).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild(user_id)) {

                                    String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                                    if (req_type.equals("received")) {

                                        mCurrent_state = "req_received";
                                        mProfileSendRestbtn.setText("Accept Friend Request");

                                        mProfileDeclineRestbtn.setVisibility(View.VISIBLE);
                                        mProfileDeclineRestbtn.setEnabled(true);

                                    } else if (req_type.equals("sent")) {
                                        mCurrent_state = "req_sent";
                                        mProfileSendRestbtn.setText("Cancel Friend Request");

                                        mProfileDeclineRestbtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineRestbtn.setEnabled(false);
                                    }
                                    mProgress.dismiss();
                                } else {
                                    mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(user_id)) {
                                                mCurrent_state = "friends";
                                                mProfileSendRestbtn.setText("UnFriend this person");

                                                mProfileDeclineRestbtn.setVisibility(View.INVISIBLE);
                                                mProfileDeclineRestbtn.setEnabled(false);
                                            }
                                            mProgress.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                            mProgress.dismiss();
                                        }
                                    });
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendRestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendRestbtn.setEnabled(false);

                //-----------------Not Freind state-------------

                if (mCurrent_state.equals("not_friends")) {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("received")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        HashMap<String,String> notificationData= new HashMap<>();
                                                        notificationData.put("from",mCurrent_user.getUid());
                                                        notificationData.put("type", "request");

                                                        mNotificationdatabase.child(user_id).push().setValue(notificationData)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                mProfileSendRestbtn.setEnabled(true);
                                                                mCurrent_state = "req_sent";
                                                                mProfileSendRestbtn.setText("Cancel Friend Request");

                                                                mProfileDeclineRestbtn.setVisibility(View.INVISIBLE);
                                                                mProfileDeclineRestbtn.setEnabled(false);
                                                            }
                                                        }) ;



                                                        //Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                //-----------------------------Cancel Request state---------

                if (mCurrent_state.equals("req_sent")) {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    mProfileSendRestbtn.setEnabled(true);
                                                    mCurrent_state = "not_friends";
                                                    mProfileSendRestbtn.setText("Send Friend Request");

                                                    mProfileDeclineRestbtn.setVisibility(View.INVISIBLE);
                                                    mProfileDeclineRestbtn.setEnabled(false);

                                                }
                                            });

                                }
                            });
                }

                //---------------Req received state-----

                if (mCurrent_state.equals("req_received")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    mProfileSendRestbtn.setEnabled(true);
                                                                                    mCurrent_state = "friends";
                                                                                    mProfileSendRestbtn.setText("UnFriend this person");

                                                                                    mProfileDeclineRestbtn.setVisibility(View.INVISIBLE);
                                                                                    mProfileDeclineRestbtn.setEnabled(false);

                                                                                }
                                                                            });

                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                }
            }
        });

    }
}
