package com.example.hara.learninguimusicapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class VideoPlayer extends AppCompatActivity {

    private VideoView video;
    private MediaController ctlr;

    private Uri uri;
    private PlayerView playerView;
    private SimpleExoPlayer player;

    private int speed = 4;

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
         uri = Uri.parse(path);
//        Log.d("demo", "URI WORKS ");

        playerView = findViewById(R.id.player_view);

        Log.d("VideoPlsWork", "Kill me Tim, the pain is too much");








        /*

        video = findViewById(R.id.videoView);
        video.setVideoURI(uri);
        ctlr = new MediaController(this);
        ctlr.setMediaPlayer(video);

        video.setMediaController(ctlr);
        video.bringToFront();
        video.start();
        */
    }

    @Override
    protected void onStart() {
        super.onStart();

        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        Log.d("VideoPlsWork", "the pain is too much");


        playerView.setPlayer(player);
        Log.d("VideoPlsWork", "Anguish");

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this,"learninguimusicapp"));
        Log.d("VideoPlsWork", "pain");


        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
        Log.d("VideoPlsWork", "End it");

        ImageButton button= playerView.findViewById(R.id.speed_up_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSpeed(1);
                Log.d("VideoPlsWork", "Speed up button happens");
            }});
        ImageButton button2= playerView.findViewById(R.id.speed_down_button);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSpeed(-1);
                Log.d("VideoPlsWork", "Speed down button happens");
            }});

        Log.d("VideoPlsWork", "Stahp");

        player.prepare(mediaSource);
        player.setPlayWhenReady(true);




        Log.d("VideoPlsWork", "Death");


    }

    @Override
    protected void onStop() {
        super.onStop();

        playerView.setPlayer(null);
        player.release();
        player=null;

    }
    protected void changeSpeed(int c){
        float vidspeed = 1f;
        //speed=5
        //5*.20 =1

        if(speed+c>0 && (speed+c<=8)){
            speed+=c;
        }
        if(speed!=0){
            vidspeed=.25f*speed;
        }



   /*
        Toast toast = Toast.makeText(getApplicationContext(),
                "Changed speed to "+vidspeed+"x",
                Toast.LENGTH_SHORT);

        toast.show();

*/
        PlaybackParameters param = new PlaybackParameters(vidspeed);
        player.setPlaybackParameters(param);

    }









}
