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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaMetadataRetriever;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Random;

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
    private static final int REQUEST_PERMISSION_KEY = 1;

    private ArrayList<String> videoList;

    private LoadAlbum loadAlbumTask;
    private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();
    public static String galleryPathKey = "path";
    public static String galleryPositionKey = "position";

    // original list of songs from the phone
    private ArrayList<Song> originalSongList;
    // current list for the Music Service
    private ArrayList<Song> currentSongList;
    // potential list
    // when you go to a view with some songs, nextSongList will populate with those songs
    // nextSongList will become currentSongList when you click a song (in playSong() method below)
    private ArrayList<Song> nextSongList;

    private ArrayList<Song> shuffleSongList;

    private Intent playIntent;
    private MusicService musicSrv;
    private boolean songIsPaused = false, musicBound;
    private SlidingUpPanelLayout slidingPanel;
    private LinearLayout panelTop;
    private TextView songName, songArtist, startTime, endTime;
    private ImageButton play_pause_small, play_pause_main, next, prev;
    private ImageButton repeat, shuffle;
    private boolean onRepeat = false;
    private boolean onShuffle = false;
    private SeekBar songTimeBar;
    private ImageView smallCover, largeCover;

    private boolean sortASC = true;

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
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(container, new HomeFragment())
                    .commit();
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
        // THE MUSIC SLIDING BAR VARIABLES AND CLICK LISTENERS
        //////////////////////////////////////////////////////////

        slidingPanel = findViewById(R.id.slidingPanel);
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
        smallCover = findViewById(R.id.song_cover_small);
        largeCover = findViewById(R.id.song_cover_big);

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

        shuffle.setOnClickListener(shuffleSongs);
        repeat.setOnClickListener(repeatSong);

        shuffleSongList = new ArrayList<>();

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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    //////////////////////////////////////////////////////////
    // CREATE OPTIONS MENU (3 VERTICAL DOTS)
    //////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        return false;
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
        clearBackStack();
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                Log.d("demo", "menu clicked: nav_home");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, new HomeFragment())
                        .commit();
                break;
            case R.id.nav_music:
                Log.d("demo", "menu clicked: nav_music");
                nextSongList = originalSongList;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, MusicFragment.newInstance(originalSongList,sortASC))
                        .commit();
                break;
            case R.id.nav_videos:
                Log.d("demo", "menu clicked: nav_videos");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, VideoFragment.newInstance(videoList))
                        .commit();
                break;
            case R.id.nav_photos:
                Log.d("demo", "menu clicked: nav_photos");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, PhotosFragment.newInstance(albumList))
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
        if (backButtonIsEnabled) {
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
                nextSongList = originalSongList;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, MusicFragment.newInstance(originalSongList, sortASC))
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_music);
                break;
            case 1: // video
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, VideoFragment.newInstance(videoList))
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_videos);
                break;
            case 2: // photos
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, PhotosFragment.newInstance(albumList))
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
        // called from ArtistFragment
        Log.d("demo", "main.fromArtistToTheirSongs " + array.toString());
        setTitle(array.get(0).getArtist());
        enableViews(true); // show back button instead of hamburger
        nextSongList = array;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, ArtistSongsFragment.newInstance(array))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void setNextMusicList(ArrayList<Song> list) {
        // called from MusicFragment.onCreateView >> viewPager.setOnScrollChangeListener
        nextSongList = list;
    }

    @Override
    public void sortCurrentMusicList(boolean sort) {
        sortASC = sort;
//        Log.d("demo", "sortCurrentMusicList " + currentSongList.toString());
        if (sort) { // in ascending order, A-Z
            Collections.sort(currentSongList, new Comparator<Song>() {
                @Override
                public int compare(Song a, Song b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
        } else { // in descending order, Z-A
            Collections.sort(currentSongList, new Comparator<Song>() {
                @Override
                public int compare(Song a, Song b) {
                    return b.getTitle().compareTo(a.getTitle());
                }
            });
        }
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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, AlbumPicturesFragment.newInstance(title))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void fromPictureToGallery(ArrayList<HashMap<String, String>> imageList, int position) {
        Log.d("demo", "fromPictureToGallery " + position);
        Intent intent = new Intent(MainActivity.this, GalleryPreview.class);
        intent.putExtra(galleryPathKey, imageList);
        intent.putExtra(galleryPositionKey, position);
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
        originalSongList = new ArrayList<>();

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            // get columns
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int column_index = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

            long thisId;
            int thisDuration;
            String thisTitle;
            String thisArtist, thisAlbum, thisAlbumImage;
            String pathId;
            byte[] art;

            // add songs to list
            do {
                Bitmap bitmap = null;
                thisId = musicCursor.getLong(idColumn);
                thisTitle = musicCursor.getString(titleColumn);
                thisArtist = musicCursor.getString(artistColumn);
                thisAlbum = musicCursor.getString(albumColumn);
                thisDuration = musicCursor.getInt(durationColumn);
                pathId = musicCursor.getString(column_index);
                metaRetriver.setDataSource(pathId);

                try {
                    art = metaRetriver.getEmbeddedPicture();
                    Options opt = new Options();
                    opt.inSampleSize = 2;
                    bitmap = BitmapFactory .decodeByteArray(art, 0, art.length,opt);
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_music);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                originalSongList.add(new Song(thisId, thisDuration, thisTitle, thisArtist, thisAlbum, bitmap));

            } while (musicCursor.moveToNext());
        }

        // sort music list
        Collections.sort(originalSongList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        currentSongList = originalSongList;
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
        currentSongList = nextSongList;
        setMusicList(currentSongList);

        // this method is called from SongAdapter.getView
        musicSrv.setSong(currentSongList.indexOf(clickedSong));
        musicSrv.playSong();

        // show music bar
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        // update everything in music bar
        updateMusicBarContent(musicSrv.getSongPosition());
        changeSeekbar();
    }

    // connect to the service
    public ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            setMusicList(originalSongList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void setMusicList(ArrayList<Song> list) {
//        Log.d("demo", "main.setMusicList " + list.toString());
        musicSrv.setList(list);

    }

    //////////////////////////////////////////////////////////
    // GET PERMISSIONS AND GET MUSIC, VIDEOS, AND PHOTOS
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
            getSongList(); // does the sort as well
            getVidList();// Mine doesn't sort because I am better than your dual screen bull
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
            Log.d("demo", "main playPauseIconListener onClick");
            playPauseMusic();
        }
    };

    public void playPauseMusic(){
        Log.d("demo", "main playPauseMusic");
        Log.d("demo", "main songIsPaused " + songIsPaused);
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
        songIsPaused = !songIsPaused;
    }

    View.OnClickListener playPrev = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            musicSrv.playPrev();
            updateMusicBarContent(musicSrv.getSongPosition());
        }
    };

    View.OnClickListener playNext = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            musicSrv.playNext();
            updateMusicBarContent(musicSrv.getSongPosition());
        }
    };

    public void changeButtonToPause() { // changed the name
        if (songIsPaused) {
            play_pause_small.setImageResource(R.drawable.pause_button);
            play_pause_main.setImageResource(R.drawable.pause_button_inverse);
            songIsPaused = !songIsPaused;
        }

    }

    public void updateMusicBarContent(int currentPosition) {
        ArrayList<Song> localArray;
        if (onShuffle) {
            localArray = shuffleSongList;
        } else {
            localArray = currentSongList;
        }
        Song currentSong = localArray.get(currentPosition);
        changeButtonToPause();
        songIsPaused = false;

        songName.setText(currentSong.getTitle());
        songArtist.setText(currentSong.getArtist());

        songTimeBar.setMax(currentSong.getDuration());

        int sec = currentSong.getSec();
        String extraZero;
        if (sec < 10) {
            extraZero = "0";
        } else {
            extraZero = "";
        }
        endTime.setText(currentSong.getMin() + ":" + extraZero + sec);

        if (currentSong.getImage() != null) {
            smallCover.setImageBitmap(currentSong.getImage());
            largeCover.setImageBitmap(currentSong.getImage());
        } else {
            smallCover.setImageResource(R.drawable.default_music);
            largeCover.setImageResource(R.drawable.default_music);
        }

    }

    View.OnClickListener shuffleSongs = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            shuffleSongList.clear();

            onShuffle = !onShuffle;

            if (onShuffle) {
                shuffle.setImageResource(R.drawable.shuffle_black);
                // make shuffle list
                Random random = new Random();
                for (int i = 0; i < currentSongList.size(); i++) {
                    int nextIndex;
                    Song nextSong;
                    do {
                        nextIndex = random.nextInt(currentSongList.size());
                        nextSong = currentSongList.get(nextIndex);
                    } while (shuffleSongList.contains(nextSong));

                    shuffleSongList.add(nextSong);
                }
                setMusicList(shuffleSongList);

                Log.d("demo", "list " + shuffleSongList);

            } else {
                shuffle.setImageResource(R.drawable.shuffle_white);

                setMusicList(currentSongList);
                Log.d("demo", "list " + currentSongList);
            }

            Log.d("demo", "onShuffle " + onShuffle);

        }
    };

    View.OnClickListener repeatSong = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!onRepeat) {
                repeat.setImageResource(R.drawable.repeat_black);
            } else {
                repeat.setImageResource(R.drawable.repeat_white);
            }
            onRepeat = !onRepeat;
            Log.d("demo", "onRepeat " + onRepeat);
        }
    };

    //////////////////////////////////////////////////////////
    // MUSIC SEEK BAR
    //////////////////////////////////////////////////////////

    private void changeSeekbar() {
        int time = musicSrv.getCurrentPositionInSong();
        songTimeBar.setProgress(time);
        int min = (int)Math.floor(time / 1000 / 60);
        int sec = (int)Math.round(time / 1000 % 60);

        // update current song time
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

        // when seek bar reaches the end
        if (songTimeBar.getProgress() == songTimeBar.getMax()) {
            if (onRepeat) {
                musicSrv.playSong(); // replay current song
            } else {
                musicSrv.playNext();
                updateMusicBarContent(musicSrv.getSongPosition());
            }
        }
    }

}
