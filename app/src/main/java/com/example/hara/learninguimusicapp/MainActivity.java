package com.example.hara.learninguimusicapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hara.learninguimusicapp.Music.ArtistFragment;
import com.example.hara.learninguimusicapp.Music.ArtistSongsFragment;
import com.example.hara.learninguimusicapp.Music.MusicFragment;
import com.example.hara.learninguimusicapp.Music.MusicService;
import com.example.hara.learninguimusicapp.Music.Song;
import com.example.hara.learninguimusicapp.Photo.AlbumPicturesFragment;
import com.example.hara.learninguimusicapp.Photo.Function;
import com.example.hara.learninguimusicapp.Photo.GalleryPreview;
import com.example.hara.learninguimusicapp.Photo.MapComparator;
import com.example.hara.learninguimusicapp.Photo.PhotosFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        HomeFragment.onHomeFragment,
        MusicFragment.onMusicFragment,
        ArtistFragment.onArtistFragment,
        PhotosFragment.onPhotoFragment,
        AlbumPicturesFragment.onAlbumPicturesFragment,
        VideoFragment.onVideoFragment {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private boolean backButtonIsEnabled = false;

    private int container = R.id.fragment_container;
    public static String galleryPathKey = "path";
    private static final int REQUEST_PERMISSION_KEY = 1;
    // For Video
    // I removed all the static keys that were used for bundles
    // Instead, I used the newInstance of the fragments
    private ArrayList<String> videoList;

    private LoadAlbum loadAlbumTask;
    private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

    private ArrayList<Song> songList;
    private Intent playIntent;
    private MusicService musicSrv;
    private boolean musicBound = false;
    private boolean songIsPaused = false;
    private SlidingUpPanelLayout slidingPanel;
    private RelativeLayout musicPanel;
    private LinearLayout panelTop;
    private TextView songName, songArtist, startTime, endTime;
    private ImageButton play_pause_small, play_pause_main, next, prev;
    private ImageButton repeat, shuffle;
    private boolean onRepeat = false;
    private boolean onShuffle = false;
    private SeekBar songTimeBar;
    private static String API_KEY = "a63919552f80e55c1e3addbb93ee9b86";

    MediaPlayer mediaPlayer;
    Runnable runnable;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Home");

        //////////////////////////////////////////////////////////
        // THE TOOLBAR AND THE NAV MENU
        //////////////////////////////////////////////////////////

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        //////////////////////////////////////////////////////////
        // ASK FOR PERMISSIONS
        //////////////////////////////////////////////////////////

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }

        //////////////////////////////////////////////////////////
        // GET THE SONGS AND VIDEOS ON THE PHONE
        //////////////////////////////////////////////////////////

        getSongList(); // does the sort as well
        getVidList();// Mine doesn't sort because I am better than your dual screen bull

        //////////////////////////////////////////////////////////
        // THE MUSIC SLIDING BAR VARIABLES AND CLICK LISTENERS
        //////////////////////////////////////////////////////////

        slidingPanel = findViewById(R.id.slidingPanel);
        musicPanel = findViewById(R.id.musicPanel);
        panelTop = findViewById(R.id.panel_top_part);
        songName = findViewById(R.id.slider_song_title);
        songArtist = findViewById(R.id.slider_song_artist);
        play_pause_small = findViewById(R.id.play_pause_button_small);
        play_pause_main = findViewById(R.id.play_pause_button_main);
        repeat = findViewById(R.id.repeat_button);
        shuffle = findViewById(R.id.shuffle_button);
        songTimeBar = findViewById(R.id.song_time_seekbar);
        next = findViewById(R.id.next_button);
        prev = findViewById(R.id.previous_button);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);

        // hide music panel until a song is playing
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        // open or close music panel when clicked, dragging it still works
        panelTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                } else {
                    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            }
        });

        play_pause_small.setOnClickListener(playPauseIconListener);
        play_pause_main.setOnClickListener(playPauseIconListener);

        next.setOnClickListener(playNext);
        prev.setOnClickListener(playPrev);

        handler = new Handler();

        songTimeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicSrv.seekToPosition(progress);
                    changeSeekbar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("demo", "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("demo", "onStopTrackingTouch");
            }
        });



    }

    private void changeSeekbar() {
        int time = musicSrv.getCurrentPositionInSong();
        songTimeBar.setProgress(time);
        int min = (int)Math.floor(time / 1000 / 60);
        int sec = (int)Math.round(time / 1000 % 60);
        String extraZero;
        if (sec < 10) {
            extraZero = "0";
        } else {
            extraZero = "";
        }
        startTime.setText(min + ":" + extraZero + sec);
        if (musicSrv.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
        if (songTimeBar.getProgress() == songTimeBar.getMax()) {
            Log.d("demo", "MAXXXXX");
            musicSrv.playNext();
            songTimeBar.setMax(songList.get(musicSrv.getSongPosition()).getDuration());
            updateMusicBarText(musicSrv.getSongPosition());
            updateSongEndTime(musicSrv.getSongPosition());
        }

    }

    //////////////////////////////////////////////////////////
    // CREATE OPTIONS MENU (3 VERTICAL DOTS)
    //////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //////////////////////////////////////////////////////////
    // HANDLE BACK PRESS FUNCTION
    //////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        Log.d("demo", "pop stack: " + getSupportFragmentManager().getBackStackEntryCount());
        if (slidingPanel != null &&
                (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            // if music bar is open
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            // if music bar is closed
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                // if nav menu is open
                drawer.closeDrawer(GravityCompat.START);
            } else {
                // if nav is closed
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    Log.d("demo", "pop");
                    getSupportFragmentManager().popBackStack();
                    if (backButtonIsEnabled) {
                        enableViews(false);
                    }
                } else {
                    Log.d("demo", "no more pop");
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(container, new HomeFragment())
                            .commit();
                }
            }
        }
    }

    //////////////////////////////////////////////////////////
    // HANDLE CLICK LISTENERS OF NAV MENU ITEMS
    //////////////////////////////////////////////////////////

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d("demo", "main menu " + menuItem);
        clearBackStack();
        Bundle bundle;
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
//                setTitle("Home");
                Log.d("demo", "menu clicked: nav_home");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, new HomeFragment())
                        .commit();
                break;
            case R.id.nav_music:
//                setTitle("Music");
                Log.d("demo", "menu clicked: nav_music");
                MusicFragment musicFragment = MusicFragment.newInstance(songList);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, musicFragment, "musicFrag")
                        .commit();
                break;
            case R.id.nav_videos:
//                setTitle("Videos");
                VideoFragment videoFragment = VideoFragment.newInstance(videoList);
                Log.d("demo", "menu clicked: nav_videos");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, videoFragment)
                        .commit();
                break;
            case R.id.nav_photos:
//                setTitle("Photos");
                Log.d("demo", "menu clicked: nav_photos");
                PhotosFragment photosFragment = PhotosFragment.newInstance(albumList);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, photosFragment)
                        .commit();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                break;
            default:
                navigationView.setCheckedItem(menuItem.getItemId());
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //////////////////////////////////////////////////////////
    // TOOLBAR CLICK LISTENER (MAY BE UNNECESSARY)
    //////////////////////////////////////////////////////////

    @Override
    public void onClick(View v) {
        Log.d("demo", "in main onClick");
        clearBackStack();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, new HomeFragment())
                .commit();
        navigationView.setCheckedItem(R.id.nav_home);
//        setTitle("Home");
        if (backButtonIsEnabled) {
            Log.d("demo", "backButtonIsEnabled " + backButtonIsEnabled);
            enableViews(false);
        }
    }

    //////////////////////////////////////////////////////////
    // FRAGMENT INTERFACES
    //////////////////////////////////////////////////////////

    @Override
    public void setFragmentTitle(String title) {
        setTitle(title);
    }

    @Override
    public void fromHomeToOther(int num) {
        Log.d("demo", "in main.fromHomeToOther" + num);
        Bundle bundle;
        switch (num) {
            case 0: // music
//                setTitle("Music");
                MusicFragment musicFragment = MusicFragment.newInstance(songList);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, musicFragment, "musicFrag")
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_music);
                break;
            case 1: // video
//                setTitle("Video");
                VideoFragment videoFragment = VideoFragment.newInstance(videoList);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, videoFragment)
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_videos);
                break;
            case 2: // photos
//                setTitle("Photos");
                PhotosFragment photosFragment = PhotosFragment.newInstance(albumList);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, photosFragment, "photofrag")
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_photos);
                break;
            default:
                break;
        }
    }

    @Override
    public void fromArtistToTheirSongs(ArrayList<Song> array){
        setTitle(array.get(0).getArtist());
        enableViews(true);
        Log.d("demo", "way back here " + array.toString());
        ArtistSongsFragment artistSongsFragment = ArtistSongsFragment.newInstance(array);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, artistSongsFragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void pauseMusicForVideo() {
        Log.d("demo", "songIsPaused " + songIsPaused);
        if (!songIsPaused) {
            playPauseMusic();
        }
    }

    @Override
    public void fromAlbumToPictures(String title) {
        setTitle(title);
        AlbumPicturesFragment albumPicturesFragment = AlbumPicturesFragment.newInstance(title);
//        Log.d("demo", "in main fromAlbumToPictures " + title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, albumPicturesFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void fromPictureToGallery(String path) {
        Intent intent = new Intent(MainActivity.this, GalleryPreview.class);
        intent.putExtra(galleryPathKey, path);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////
    // REPLACE NAV BUTTON WITH BACK ARROW WHEN IN AlbumPicturesFragment
    //////////////////////////////////////////////////////////

    @Override
    public void getBackButton() {
        Log.d("demo", "in main.getBackButton");
        enableViews(true);
    }

    private void enableViews(boolean enable) {

        // To keep states of ActionBar and ActionBarDrawerToggle synchronized,
        // when you enable on one, you disable on the other.
        // And as you may notice, the order for this operation is disable first, then enable - VERY VERY IMPORTANT.
        if(enable) {
            //You may not want to open the drawer on swipe from the left in this case
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            // Remove hamburger
            toggle.setDrawerIndicatorEnabled(false);
            // Show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.
            if(!backButtonIsEnabled) {
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Doesn't have to be onBackPressed
                        onBackPressed();
                    }
                });

                backButtonIsEnabled = true;
            }

        } else {
            //You must regain the power of swipe for the drawer.
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

            // Remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Show hamburger
            toggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            toggle.setToolbarNavigationClickListener(null);
            backButtonIsEnabled = false;
        }

        // So, one may think "Hmm why not simplify to:
        // .....
        // getSupportActionBar().setDisplayHomeAsUpEnabled(enable);
        // mDrawer.setDrawerIndicatorEnabled(!enable);
        // ......
        // To re-iterate, the order in which you enable and disable views IS important #dontSimplify.
    }

    //////////////////////////////////////////////////////////
    // GET VIDEOS & MUSIC FROM PHONE AND SET UP PLAY INTENT
    //////////////////////////////////////////////////////////

    public void getVidList(){
        videoList = new ArrayList<>();

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor videoCursor = contentResolver.query(videoUri,null,null,null,null);

        if (videoCursor != null && videoCursor.moveToFirst()) {
            int videoTitle = videoCursor.getColumnIndex(MediaStore.Video.Media.TITLE);
            int videoDuration = videoCursor.getColumnIndex(MediaStore.Video.Media.DURATION);
            do {
                String currentTitle = videoCursor.getString(videoTitle);

                videoList.add(currentTitle// + "\n" + currentDuration );
                );

            } while (videoCursor.moveToNext());

        }

        }

    public void getSongList() {
        songList = new ArrayList<>();

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            // get columns
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            int albumId = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            // TODO - not sure how to get album image, perhaps it's just my emulator
            Cursor albumCursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID + "=?",
                    new String[] {String.valueOf(albumId)},
                    null);

            long thisId;
            int thisDuration;
            String thisTitle;
            String thisArtist, thisAlbum, thisAlbumImage;

            // add songs to list
            do {

                thisId = musicCursor.getLong(idColumn);
                thisTitle = musicCursor.getString(titleColumn);
                thisArtist = musicCursor.getString(artistColumn);
                thisAlbum = musicCursor.getString(albumColumn);
                thisDuration = musicCursor.getInt(durationColumn);


                songList.add(new Song(thisId, thisDuration, thisTitle, thisArtist, thisAlbum));

            } while (musicCursor.moveToNext());
        }

        // sort music list
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }

    public void playSong(Song clickedSong) {
        musicSrv.onCompletion(mediaPlayer);
        // this method is called from SongAdapter.getView
        musicSrv.setSong(songList.indexOf(clickedSong));
        musicSrv.playSong();
        // show music bar
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        // update play and pause images
        play_pause_small.setImageResource(R.drawable.pause_button);
        play_pause_main.setImageResource(R.drawable.pause_button_inverse);
        // update song text
        updateMusicBarText(songList.indexOf(clickedSong));
        // update seek bar properties
        songTimeBar.setMax(clickedSong.getDuration());
        startTime.setText("0:00");
//        endTime.setText(clickedSong.getMin() + ":" + clickedSong.getSec());
        updateSongEndTime(musicSrv.getSongPosition());

        // continuously update seek bar
        changeSeekbar();
    }

    // connect to the service
    public ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    //////////////////////////////////////////////////////////
    // GET PHOTOS FROM PHONE & PERMISSIONS
    //////////////////////////////////////////////////////////

    public class LoadAlbum extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            albumList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;


            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.DATE_MODIFIED};
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor cursorInternal = getContentResolver().
                    query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                            null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});

            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = Function.getCount(getApplicationContext(), album);

                albumList.add(Function.mappingInbox(album, path, timestamp, Function.convertToTime(timestamp), countPhoto));
            }
            cursor.close();
            Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "desc")); // Arranging photo album by timestamp descending
            return xml;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("demo", "onRequestPermissionsResult Yes");
                    loadAlbumTask = new LoadAlbum();
                    loadAlbumTask.execute();
                } else {
                    Log.d("demo", "Noooo");
                    Toast.makeText(MainActivity.this, "You must accept permissions.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        } else {
            loadAlbumTask = new LoadAlbum();
            loadAlbumTask.execute();
        }
    }

    //////////////////////////////////////////////////////////
    // CLEAR THE BACK STACK
    //////////////////////////////////////////////////////////

    private void clearBackStack() {
        Log.d("demo" , "in main.clearBackStack");
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(0);
            getSupportFragmentManager().popBackStack(entry.getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    //////////////////////////////////////////////////////////
    // MUSIC CLICK LISTENERS
    //////////////////////////////////////////////////////////

    View.OnClickListener playPauseIconListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            playPauseMusic();
        }
    };

    public void playPauseMusic(){
        // I made this into a separate method so that the music will pause when clicking a video
        if (songIsPaused) {
            // was playing, now paused
            play_pause_small.setImageResource(R.drawable.pause_button);
            play_pause_main.setImageResource(R.drawable.pause_button_inverse);
            musicSrv.resumePlayer();
        }
        else {
            // was paused, now playing
            play_pause_small.setImageResource(R.drawable.play_button);
            play_pause_main.setImageResource(R.drawable.play_button_inverse);
            musicSrv.pausePlayer();

        }
        changeSeekbar();
        songIsPaused = !songIsPaused;
    }

    View.OnClickListener playPrev = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            musicSrv.playPrev();
//            changeButtonToPause();
            updateMusicBarText(musicSrv.getSongPosition());
            updateSongEndTime(musicSrv.getSongPosition());

        }
    };

    View.OnClickListener playNext = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            musicSrv.playNext();
//            changeButtonToPause();
            updateMusicBarText(musicSrv.getSongPosition());
            updateSongEndTime(musicSrv.getSongPosition());
        }
    };
    public void changeButtonToPause() { // changed the name
        if (songIsPaused) {
            play_pause_small.setImageResource(R.drawable.pause_button);
            play_pause_main.setImageResource(R.drawable.pause_button_inverse);
            songIsPaused = !songIsPaused;
        }
        changeSeekbar();
    }

    public void updateMusicBarText(int currentPosition) {
        changeButtonToPause();
        songName.setText(songList.get(currentPosition).getTitle());
        songArtist.setText(songList.get(currentPosition).getArtist());
    }

    public void updateSongEndTime(int currentPosition) {
        int sec = songList.get(currentPosition).getSec();
        String extraZero;
        if (sec < 10) {
            extraZero = "0";
        } else {
            extraZero = "";
        }
        endTime.setText(songList.get(currentPosition).getMin() + ":" + extraZero + sec);

    }

}
