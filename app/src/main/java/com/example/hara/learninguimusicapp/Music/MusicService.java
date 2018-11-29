package com.example.hara.learninguimusicapp.Music;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private final IBinder musicBind = new MusicBinder();
    private int inSongPosition;

    private boolean isPlaying = false;

    public void onCreate(){
        super.onCreate();

        songPosition = 0;
        player = new MediaPlayer();

        initMusicPlayer();
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
        playNext();
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

    public int getDuration(){
        return player.getDuration();
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

