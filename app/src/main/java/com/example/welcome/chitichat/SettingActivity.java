package com.example.welcome.chitichat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

    private DatabaseReference userdatabase;
    private FirebaseUser currentuser;
    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button change_image;
    private Button change_status;
    private static final int GALLARY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mDisplayImage = findViewById(R.id.setting_image);
        mName = findViewById(R.id.tv_setting_name);
        mStatus = findViewById(R.id.tv_setting_status);
        change_image = findViewById(R.id.btn_setting_image);
        change_status = findViewById(R.id.btn_setting_status);

        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentuser.getUid();

        mImageStorage = FirebaseStorage.getInstance().getReference();
        userdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        userdatabase.keepSynced(true);
        userdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                //saving value to layout
                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default")) {

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(mDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();
                Intent i = new Intent(SettingActivity.this, StatusActivity.class);
                i.putExtra("status_value", status_value);
                startActivity(i);

            }
        });

        change_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "select image"), GALLARY_PICK);

                /* //direct  folder kholne ani crop garna ko lagi
                CropImage.activity()
                 .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
                 */
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500,500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("please wait");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                //using compresser to compress image and bitmap to upload in firebase
                File thumb_file=new File(resultUri.getPath());

                String current_user_id = currentuser.getUid();

              Bitmap thumb_bitmap=new Compressor(this)
                      .setMaxHeight(640)
                      .setMaxWidth(480)
                      .setQuality(80)
                      .compressToBitmap(thumb_file);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

              //ends here
                StorageReference filepath = mImageStorage.child("Profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath=mImageStorage.child("Profile_images").child("thumbs").child(current_user_id + ".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    String thumb_download_url=task.getResult().getDownloadUrl().toString();

                                    if(task.isSuccessful()){
                                        Map update_hashMap=new HashMap();
                                        update_hashMap.put("image",download_url);
                                        update_hashMap.put("thumb_image",thumb_download_url);
                                        userdatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingActivity.this, "success uploading in database", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }else{
                                        Toast.makeText(SettingActivity.this, "Error uploading thumbnail", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    }

                                }
                            });

                        } else {
                            Toast.makeText(SettingActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(25);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
