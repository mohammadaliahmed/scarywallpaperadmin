package com.eaw.wallpaperadmin.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.eaw.wallpaperadmin.Adapter.ImagesAdapter;
import com.eaw.wallpaperadmin.Models.WallpaperModel;
import com.eaw.wallpaperadmin.R;
import com.eaw.wallpaperadmin.Utils.CommonUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Splash extends AppCompatActivity {

    ImageView addWallpaper;
    RecyclerView recyclerView;
    ImagesAdapter adapter;
    private ArrayList<WallpaperModel> itemList = new ArrayList<>();
    DatabaseReference mDatabase;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        recyclerView = findViewById(R.id.recyclerView);
        progress = findViewById(R.id.progress);
        addWallpaper = findViewById(R.id.addWallpaper);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        addWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Splash.this, AddWallpaper.class));
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new ImagesAdapter(this, itemList, new ImagesAdapter.AdapterCallback() {
            @Override
            public void onDelete(WallpaperModel model) {
                showAlert(model);
            }
        });
        recyclerView.setAdapter(adapter);
        getDataFromDB();

    }

    private void showAlert(WallpaperModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Do you want to delete this wallpaper? ");

        // add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDatabase.child("Wallpapers").child(model.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        CommonUtils.showToast("Deleted");
                    }
                });

            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getDataFromDB() {
        progress.setVisibility(View.VISIBLE);
        mDatabase.child("Wallpapers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    itemList.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        WallpaperModel model = snapshot1.getValue(WallpaperModel.class);
                        itemList.add(model);
                    }
                    Collections.sort(itemList, new Comparator<WallpaperModel>() {
                        @Override
                        public int compare(WallpaperModel listData, WallpaperModel t1) {
                            Long ob1 = listData.getTime();
                            Long ob2 = t1.getTime();
                            return ob2.compareTo(ob1);
                        }
                    });
                    progress.setVisibility(View.GONE);
                    adapter.setItemList(itemList);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}
