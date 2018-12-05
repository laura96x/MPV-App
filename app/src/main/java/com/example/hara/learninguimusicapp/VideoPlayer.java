package com.example.hara.learninguimusicapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayer extends AppCompatActivity {

    private VideoView video;
    private MediaController ctlr;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // remove the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.videoplayer);
        String value = null;
//        Log.d("demo", "Created ");
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            value = extras.getString("KEY");
        }
        Cursor cursor = null;
        if(value!=null) {

            cursor = getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Video.Media.TITLE + " LIKE ?",
                    new String[]{"%" + value + "%"},
                    MediaStore.Video.Media.TITLE + " ASC");

        }
        String path;

        cursor.moveToNext();
        path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
//        Log.d("demo", path);
        Uri uri = Uri.parse(path);
//        Log.d("demo", "URI WORKS ");

        video = findViewById(R.id.videoView);
        video.setVideoURI(uri);
        ctlr = new MediaController(this);
        ctlr.setMediaPlayer(video);

        video.setMediaController(ctlr);
        video.bringToFront();
        video.start();
    }

}
