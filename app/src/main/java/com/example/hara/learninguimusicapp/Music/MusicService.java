package com.example.hara.learninguimusicapp.Music;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.example.hara.learninguimusicapp.MainActivity;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private final IBinder musicBind = new MusicBinder();
    private int inSongPosition;

    private boolean isPlaying = false;
    MainActivity mainActivity;

    public void onCreate(){
        super.onCreate();

        songPosition = 0;
        player = new MediaPlayer();

        initMusicPlayer();

//        mainActivity = (MainActivity) get();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void playSong(){
        player.reset();
        isPlaying = true;
        //get song
        Song playSong = songs.get(songPosition);
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getApplicationContext(),trackUri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);
        Log.d("demo", "millSecond " + millSecond);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    public void setSong(int songIndex){
        songPosition = songIndex;
    }

    public int getDuration1(){
        return player.getDuration();
    }
    public int getDuration2(){
        return songs.get(getSongPosition()).getDuration();
    }

    public boolean isPlaying(){
        // can't use player.isPlaying()
        // since player.start() isn't called in playSong method, player.isPlaying() = false; until resume however
        return isPlaying;
    }

    public void pausePlayer(){
        inSongPosition = player.getCurrentPosition();
        player.pause();
        isPlaying = false;
    }

    public int getCurrentPositionInSong() {
        return player.getCurrentPosition();
    }

    public int getSongPosition() {
        return songPosition;
    }

    public void resumePlayer(){
        player.seekTo(getCurrentPositionInSong());
        player.start();
        isPlaying = true;
    }

    public void seekToPosition(int posn){
        player.seekTo(posn);
    }

    public void playPrev(){
        songPosition--;
        if( songPosition < 0){
            songPosition = songs.size() - 1;
        }
        playSong();
    }

    public void playNext(){
        songPosition++;
        if(songPosition == songs.size()) {
            songPosition = 0;
        }

        playSong();
    }
}

