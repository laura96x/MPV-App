package com.example.hara.learninguimusicapp.Photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hara.learninguimusicapp.R;

import java.io.File;

public class GalleryPreview extends AppCompatActivity {
    // shows the picture by itself
    private ImageView GalleryPreviewImg;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.gallery_preview);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        GalleryPreviewImg = findViewById(R.id.GalleryPreviewImg);

        Glide.with(GalleryPreview.this)
                .load(new File(path)) // Uri of the picture
                .into(GalleryPreviewImg);
    }

}
