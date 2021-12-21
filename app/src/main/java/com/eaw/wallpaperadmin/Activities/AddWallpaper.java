package com.eaw.wallpaperadmin.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.eaw.wallpaperadmin.Models.WallpaperModel;
import com.eaw.wallpaperadmin.R;
import com.eaw.wallpaperadmin.Utils.CommonUtils;
import com.eaw.wallpaperadmin.Utils.CompressImage;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class AddWallpaper extends AppCompatActivity {

    Button upload;
    ImageView pickImage;
    private static final int REQUEST_CODE_CHOOSE = 23;
    private ArrayList<String> mSelected = new ArrayList<>();
    private String imageUrl;
    ProgressBar progress;
    EditText title;
    String category = "";

    DatabaseReference mDatabase;
    CheckBox featured;
    private boolean isFeatured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_wallpaper);
        getPermissions();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        this.setTitle("Upload Wallpaper");
        upload = findViewById(R.id.upload);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        featured = findViewById(R.id.featured);
        progress = findViewById(R.id.progress);
        title = findViewById(R.id.title);
        pickImage = findViewById(R.id.pickImage);
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMatisse();
            }
        });

        featured.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isPressed()) {
                    if (b) {
                        isFeatured = true;
                    } else {
                        isFeatured = false;
                    }
                }
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected.size() == 0) {
                    CommonUtils.showToast("Please select wallpaper");
                } else {
                    uploadImg();
                }
            }
        });


    }

    private void uploadImg() {
        try {
            progress.setVisibility(View.VISIBLE);
            String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

            Uri file = Uri.fromFile(new File(imageUrl));

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

            final StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        @SuppressWarnings("VisibleForTests")
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content

                            String downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                            riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String profileImageUrl = task.getResult().toString();
                                    Log.i("URL", profileImageUrl);
                                    String key = mDatabase.push().getKey();
                                    WallpaperModel model = new WallpaperModel(key,
                                            title.getText().toString(), profileImageUrl, category, System.currentTimeMillis(), isFeatured);
                                    mDatabase.child("Wallpapers").child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            CommonUtils.showToast("Uploaded");
                                            progress.setVisibility(View.INVISIBLE);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            mDatabase.child("Errors").child("dbError").child(mDatabase.push().getKey()).setValue(e.getMessage());

                                        }
                                    });


                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            mDatabase.child("Errors").child("picUploadError").child(mDatabase.push().getKey()).setValue(exception.getMessage());

                            CommonUtils.showToast("There was some error uploading pic");


                        }
                    });
        } catch (Exception e) {
            mDatabase.child("Errors").child("mainError").child(mDatabase.push().getKey()).setValue(e.getMessage());
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && data != null) {
            mSelected = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

//            try {
            CompressImage image = new CompressImage(AddWallpaper.this);
            imageUrl = image.compressImage("" + mSelected.get(0));
            Glide.with(AddWallpaper.this).load(mSelected.get(0)).into(pickImage);
//            }catch (Exception e){
//                mDatabase.child("Errors").child("imgError").child(mDatabase.push().getKey()).setValue(e.getMessage());
//            }
        }
    }

    private void initMatisse() {
        Options options = Options.init()
                .setRequestCode(REQUEST_CODE_CHOOSE)                                           //Request code for activity results
                .setCount(1)
                .setMode(Options.Mode.Picture)//Number of images to restict selection count
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                ;                                       //Custom Path For media Storage

        Pix.start(this, options);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {


            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
