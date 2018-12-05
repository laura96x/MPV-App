package com.example.hara.learninguimusicapp.Photo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hara.learninguimusicapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class SwipeAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<HashMap<String, String>> imageList;

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == o);
    }

    public SwipeAdapter(Context context, ArrayList<HashMap<String, String>> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.image_swipe,container,false);

        ImageView GalleryPreviewImg = itemView.findViewById(R.id.GalleryPreviewImg);
        String path = imageList.get(position).get(Function.KEY_PATH);

        Glide.with(context)
                .load(new File(path)) // Uri of the picture
                .into(GalleryPreviewImg);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        super.destroyItem(container, position, object);
    }
}
