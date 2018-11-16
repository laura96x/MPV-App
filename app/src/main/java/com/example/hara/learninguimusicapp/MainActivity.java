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
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

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
        PhotosFragment.onPhotoFragment,
        AlbumPicturesFragment.onAlbumPicturesFragment,
        VideoFragment.onVideoFragment {

    private DrawerLayout drawer;
    NavigationView navigationView;

    int container = R.id.fragment_container;
    public static String albumNameKey = "album name";
    public static String albumListKey = "album list";
    public static String galleryPathKey = "path";
    public static String musicListKey = "songs";
    static final int REQUEST_PERMISSION_KEY = 1;
    //For Vids
    public static String videoListKey = "vids";
    ArrayList<String> vidList;

    LoadAlbum loadAlbumTask;
    GridView galleryGridView;
    ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

    ArrayList<Song> songList;
    Intent playIntent;
    MusicService musicSrv;
    boolean musicBound = false;
    SlidingUpPanelLayout slidingPanel;
    RelativeLayout musicPanel;
    LinearLayout panelTop;
    ImageButton play, pause, play_main, pause_main, next, prev;
    ImageButton repeat, shuffle;
    boolean onRepeat = false;
    boolean onShuffle = false;
    SeekBar songTimeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //////////////////////////////////////////////////////////
        // THE TOOLBAR AND THE NAV MENU
        //////////////////////////////////////////////////////////

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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
        getVidList();//Mine doesn't sort because I am better than your dual screen bull

        //////////////////////////////////////////////////////////
        // THE MUSIC SLIDING BAR VARIABLES AND CLICK LISTENERS
        //////////////////////////////////////////////////////////

        slidingPanel = findViewById(R.id.slidingPanel);
        musicPanel = findViewById(R.id.musicPanel);
        panelTop = findViewById(R.id.panel_top_part);
        play = findViewById(R.id.play_button);
        pause = findViewById(R.id.pause_button);
        play_main = findViewById(R.id.play_button_main);
        pause_main = findViewById(R.id.pause_button_main);
        repeat = findViewById(R.id.repeat_button);
        shuffle = findViewById(R.id.shuffle_button);
        songTimeBar = findViewById(R.id.song_time_seekbar);
        next = findViewById(R.id.next_button);
        prev = findViewById(R.id.previous_button);

        // hide music panel until a song is playing
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        // open or close panel when clicked, dragging it still works
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


        pause.setOnClickListener(playPauseIconListener);
        pause_main.setOnClickListener(playPauseIconListener);

        next.setOnClickListener(playNext);
        prev.setOnClickListener(playPrev);

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
                } else {
                    Log.d("demo", "no more pop");
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
                Log.d("demo", "menu clicked: nav_home");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, new HomeFragment())
                        .commit();
                break;
            case R.id.nav_music:
                Log.d("demo", "menu clicked: nav_music");
                MusicFragment musicFragment = new MusicFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, musicFragment)
                        .commit();
                bundle = new Bundle();
                bundle.putSerializable(musicListKey, songList);
                musicFragment.setArguments(bundle);
                break;
            case R.id.nav_videos:

                VideoFragment videoFragment = new VideoFragment();
                Log.d("demo", "menu clicked: nav_videos");
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, videoFragment)
                        .commit();
                bundle = new Bundle();
                bundle.putSerializable(videoListKey, vidList);
                videoFragment.setArguments(bundle);


                break;
            case R.id.nav_photos:
                Log.d("demo", "menu clicked: nav_photos");
                PhotosFragment photosFragment = new PhotosFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, photosFragment)
                        .commit();
                bundle = new Bundle();
                bundle.putSerializable(albumListKey, albumList);
                photosFragment.setArguments(bundle);
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
        setTitle("Home");
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
                Log.d("demo", "main clickyyyy " + songList.toString());
                MusicFragment musicFragment = new MusicFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, musicFragment)
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_music);
                bundle = new Bundle();
                bundle.putSerializable(musicListKey, songList);
                musicFragment.setArguments(bundle);
                break;
            case 1: // video
                VideoFragment videoFragment = new VideoFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, videoFragment)
                        .addToBackStack(null)
                        .commit();
                bundle = new Bundle();
                bundle.putSerializable(videoListKey, vidList);
                videoFragment.setArguments(bundle);
                navigationView.setCheckedItem(R.id.nav_videos);
                break;
            case 2: // photos
                PhotosFragment photosFragment = new PhotosFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(container, photosFragment, "photo frag")
                        .addToBackStack(null)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_photos);
                bundle = new Bundle();
                bundle.putSerializable(albumListKey, albumList);
                photosFragment.setArguments(bundle);
                break;
            default:
                break;
        }
    }

    @Override
    public void fromAlbumToPictures(String title) {
        AlbumPicturesFragment albumPicturesFragment = new AlbumPicturesFragment();
//        Log.d("demo", "in main fromAlbumToPictures " + title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(container, albumPicturesFragment)
                .addToBackStack(null)
                .commit();
        Bundle bundle = new Bundle();
        bundle.putString(albumNameKey, title);
        albumPicturesFragment.setArguments(bundle);
    }

    @Override
    public void fromPictureToGallery(String path) {
        Intent intent = new Intent(MainActivity.this, GalleryPreview.class);
        intent.putExtra(galleryPathKey, path);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////
    // ATTEMPT TO REPLACE NAV BUTTON WITH BACK ARROW IN PHOTOS
    //////////////////////////////////////////////////////////
    // TODO - come back to this for Sprint 3
    @Override
    public void getBackButton() {
        Log.d("demo", "in main.getBackButton");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d("demo", "in main.onSupportNavigateUp");
        onBackPressed();
        return true;
    }

    //////////////////////////////////////////////////////////
    // GET MUSIC FROM PHONE AND SET UP PLAY INTENT
    //////////////////////////////////////////////////////////
    public void getVidList(){
        vidList = new ArrayList<>();

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor videoCursor = contentResolver.query(videoUri,null,null,null,null);

        if(videoCursor != null && videoCursor.moveToFirst()) {
            int videoTitle = videoCursor.getColumnIndex(MediaStore.Video.Media.TITLE);
            int videoDuration = videoCursor.getColumnIndex(MediaStore.Video.Media.DURATION);
            do{
                String currentTitle = videoCursor.getString(videoTitle);

                vidList.add(currentTitle// + "\n" + currentDuration );
                );

            }while(videoCursor.moveToNext());

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
            double thisDuration;
            String thisTitle;
            String thisArtist, thisAlbum, thisAlbumImage;

            // add songs to list
            do {

                thisId = musicCursor.getLong(idColumn);
                thisTitle = musicCursor.getString(titleColumn);
                thisArtist = musicCursor.getString(artistColumn);
                thisAlbum = musicCursor.getString(albumColumn);
                thisDuration = musicCursor.getDouble(durationColumn) / 1000;


                songList.add(new Song(thisId, thisDuration, thisTitle, thisArtist, thisAlbum));

            } while (musicCursor.moveToNext());
        }

        // sort music list
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getName().compareTo(b.getName());
            }
        });

        Log.d("demo", "main after sort " + songList.toString());
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

    public void playSong(int position) {
        // this method is called from SongAdapter.getView
        musicSrv.setSong(position);
        musicSrv.playSong();
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED); // show music bar
        // TODO - set the contents of the sliding panel to the specified song
        // TODO - make another method
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


    private boolean iconIsPlay = false;
    View.OnClickListener playPauseIconListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            if (iconIsPlay) {
                pause.setImageResource(R.drawable.pause_button);
                pause_main.setImageResource(R.drawable.pause_button_inverse);
                musicSrv.resumePlayer();
            }
            else {
                pause.setImageResource(R.drawable.play_button);
                pause_main.setImageResource(R.drawable.play_button_inverse);
                musicSrv.pausePlayer();
            }

            iconIsPlay = !iconIsPlay;
        }
    };

    View.OnClickListener playPrev = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            musicSrv.playPrev();
//            changeSeekBarAndTimes();
            changeToPlay();
        }
    };

    View.OnClickListener playNext = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            musicSrv.playNext();
//            changeSeekBarAndTimes();
            changeToPlay();
        }
    };
    public void changeToPlay() {
        if (iconIsPlay == true) {
            pause.setImageResource(R.drawable.pause_button);
            pause_main.setImageResource(R.drawable.pause_button_inverse);
            iconIsPlay = false;
        }
    }


}
