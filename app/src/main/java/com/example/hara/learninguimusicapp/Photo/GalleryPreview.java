package com.example.hara.learninguimusicapp.Photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hara.learninguimusicapp.MainActivity;
import com.example.hara.learninguimusicapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class GalleryPreview extends AppCompatActivity {
    // shows the picture by itself
    private ImageView GalleryPreviewImg;
    private String path;
    private ArrayList<HashMap<String, String>> imageList;
    private int position;

    ViewPager viewPager;
    SwipeAdapter swipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.gallery_preview);

        Intent intent = getIntent();
        imageList = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra(MainActivity.galleryPathKey);
        position = intent.getIntExtra(MainActivity.galleryPositionKey, 0);

        viewPager = findViewById(R.id.viewPager);
        swipeAdapter = new SwipeAdapter(this, imageList);

        viewPager.setAdapter(swipeAdapter);

        // set current item to whichever is clicked
        viewPager.setCurrentItem(position);

    }

}
