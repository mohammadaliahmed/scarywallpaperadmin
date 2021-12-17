package com.eaw.wallpaperadmin.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eaw.wallpaperadmin.Models.WallpaperModel;
import com.eaw.wallpaperadmin.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    Context context;
    ArrayList<WallpaperModel> itemList;
    AdapterCallback callback;
    public ImagesAdapter(Context context, ArrayList<WallpaperModel> itemList,AdapterCallback callback) {
        this.context = context;
        this.itemList = itemList;
        this.callback = callback;

    }

    public void setItemList(ArrayList<WallpaperModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wallpaper_item_layout, parent, false);
        ImagesAdapter.ViewHolder viewHolder = new ImagesAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        WallpaperModel model = itemList.get(position);
        Glide.with(context).load(model.getPicUrl()).into(holder.image);
//        holder.title.setText(model.getTitle());
        holder.cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onDelete(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image,cross;
        TextView title;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            cross = itemView.findViewById(R.id.cross);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
        }
    }
    public  interface AdapterCallback{
        public void onDelete(WallpaperModel model);
    }
}
