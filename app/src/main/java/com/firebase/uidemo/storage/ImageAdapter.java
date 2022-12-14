package com.firebase.uidemo.storage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.uidemo.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private ArrayList<String> imageList;
    private ArrayList<String> imageNameList;

    public ImageAdapter(ArrayList<String> imageList, ArrayList<String> imageNameList, Context context) {
        this.imageList = imageList;
        this.imageNameList = imageNameList;
        this.context = context;
    }

    private Context context;
    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_images_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        // loading the images from the position
        holder.imageName.setText(imageNameList.get(position));
        Glide.with(holder.itemView.getContext()).load(imageList.get(position)).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView imageName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivListImage);
            imageName = itemView.findViewById(R.id.tvImageName);
        }
    }
}
