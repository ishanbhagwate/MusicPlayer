package com.shivam.musicplayer;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.shivam.musicplayer.Model.Songs;

import java.io.IOException;
import java.util.ArrayList;


import static android.content.ContentValues.TAG;

public class MediaService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final IBinder binder = new PlayerBinder();
    private MediaPlayer mediaPlayer;
    private int currentPausePosition;
    private int currentSeekProgress;

    public static final String ACTION_PLAY = "com.shivam.musicplayer.PLAY";
    public static final String ACTION_PAUSE = "com.shivam.musicplayer.PAUSE";
    public static final String ACTION_PREVIOUS = "com.shivam.musicplayer.PREVIOUS";
    public static final String ACTION_NEXT = "com.shivam.musicplayer.NEXT";
    public static final String ACTION_STOP = "com.shivam.musicplayer.STOP";

    private ArrayList<Songs> songsArrayList;
    private int SONG_POS = 0;
    private Uri songUri;
    private int mState = STATE_PAUSED;

    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSED = 1;

    SharedPreferences lastPlayedSong;
    SharedPreferences.Editor editor;

    public class PlayerBinder extends Binder { //service connection to play in background

        public MediaService getService(){

            return MediaService.this;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        //initialise the media player object
        mediaPlayer = new MediaPlayer();
        initPlayer();
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);

        loadAudio();

    }

    private void initPlayer() {

        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!mediaPlayer.isPlaying()){

            stopSelf();
            Log.i(TAG, "onDestroy: stopped");

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand: " + SONG_POS);
        if (intent != null){

            Log.i(TAG, "onStartCommand: onStartCommand called");

            if (intent.getBooleanExtra("notFromIntent",false)){

                //dont play media directly
                Log.i(TAG, "onStartCommand: NOT PLAYING DIRECTLY" + SONG_POS);
                SONG_POS = loadLastPlayedSong();

            }else {

                //play media directly
                SONG_POS = intent.getIntExtra("position",0);
                Log.i(TAG, "onStartCommand: PLAYING DIRECTLY" + SONG_POS);
                setSelectedSong(SONG_POS);

            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();

        Log.i(TAG, "onTaskRemoved: killed");
    }


    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG, "onBind: CALLED");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

//        stopSong();
//        stopSelf();
//        nextSong();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        playSong();
        storeLastSongPlayed();

    }

    public void setSongList(ArrayList<Songs> songsList) {
        songsArrayList = songsList;
    }

    public void setSelectedSong(int pos){

        SONG_POS = pos;
        setSongUri(songsArrayList.get(SONG_POS).getSongUri());
        startSong(songsArrayList.get(SONG_POS).getSongUri(), songsArrayList.get(SONG_POS).getTitle());

    }

    private void startSong(Uri songUri, String title) {

        mediaPlayer.reset();
        this.songUri = songUri;
        try {
            mediaPlayer.setDataSource(getApplicationContext(),songUri);
        } catch (IOException e) {
            Log.e(TAG, "startSong: Error setting data source",e );
        }
        mediaPlayer.prepareAsync();

    }

    public void playPauseSong(){

        if (mState == STATE_PAUSED){

            mState = STATE_PLAYING;
            mediaPlayer.start();

            Log.i(TAG, "playPauseSong: " + mState);

        }else {

            mState = STATE_PAUSED;
            mediaPlayer.pause();

            Log.i(TAG, "playPauseSong: " + mState);

        }

    }

    public void playSong(){

        if (mState == STATE_PAUSED){

            mediaPlayer.seekTo(currentPausePosition);
            mediaPlayer.start();
            currentPausePosition = 0;
            mState = STATE_PLAYING;
            storeLastSongPlayed();
        }else {

            mediaPlayer.start();
            mState = STATE_PLAYING;
            currentPausePosition = 0;
            storeLastSongPlayed();
        }

        Log.i(TAG, "playSong: " + mState);

    }

    public void stopSong() {

        mediaPlayer.stop();
        Log.i(TAG, "stopSong: STOPPED");
        System.exit(0);

    }

    public void previousSong() {

        if (SONG_POS == 0){
            Log.i(TAG, "previousSong: " + SONG_POS);
            SONG_POS = songsArrayList.size() - 1;
            startSong(songsArrayList.get(SONG_POS).getSongUri(),songsArrayList.get(SONG_POS).getTitle());
        }else {
            Log.i(TAG, "previousSong: " + SONG_POS);
            startSong(songsArrayList.get(SONG_POS - 1).getSongUri(),songsArrayList.get(SONG_POS - 1).getTitle());
            SONG_POS--;
        }



    }

    public void nextSong() {

        if (SONG_POS < songsArrayList.size() - 1){

            startSong(songsArrayList.get(SONG_POS + 1).getSongUri(),songsArrayList.get(SONG_POS + 1).getTitle());
            SONG_POS++;
            Log.i(TAG, "nextSong: " + SONG_POS);

        }else {
            Log.i(TAG, "nextSong: " + SONG_POS);
            SONG_POS = 0;
            startSong(songsArrayList.get(SONG_POS).getSongUri(),songsArrayList.get(SONG_POS).getTitle());
        }

    }

    public void pauseSong(){

        currentPausePosition = mediaPlayer.getCurrentPosition();
        mediaPlayer.pause();
        mState = STATE_PAUSED;
        Log.i(TAG, "pauseSong: PAUSED");

        storeLastSongPlayed();
    }

    public void onSeekChange(int currentProgress){

        currentSeekProgress = currentProgress;
        if (mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(currentSeekProgress);
        }

    }


    public int getMediaDuration(){

        if (mediaPlayer != null) {

            return mediaPlayer.getDuration();
        }

        return 0;
    }

    public int getCurrentPosition(){

        if (mediaPlayer != null) {
            Log.i(TAG, "getCurrentPosition: " + mediaPlayer.getCurrentPosition());
            return mediaPlayer.getCurrentPosition();
        }
        else {
            return 0;
        }
    }

    public String getSongTitle(){

        return songsArrayList.get(SONG_POS).getTitle();

    }

    public String getSongArtist(){

        return songsArrayList.get(SONG_POS).getArtist();

    }

    private void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }

    public void updateLastPlayedSong(){

        editor.putInt("position",SONG_POS);
        editor.apply();

        Log.i(TAG, "updateLastPlayedSong: " + SONG_POS);

    }


    public boolean isPlaying(){

        if (mediaPlayer != null){
            return mediaPlayer.isPlaying();
        }else {
            return false;
        }

    }

    private void loadAudio(){

        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC;
        String sortOrder = MediaStore.Audio.Media.TITLE;
        Cursor cursor = contentResolver.query(uri,null,selection,null ,sortOrder);

        if (cursor != null && cursor.getCount() > 0){

            songsArrayList = new ArrayList<>();
            while (cursor.moveToNext()){

                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));

                //save to audioList
                songsArrayList.add(new Songs(title,artist,data,album, songUri));

            }
            cursor.close();
        }
    }

    private void storeLastSongPlayed(){

        lastPlayedSong = getSharedPreferences("lastPlayed",MODE_PRIVATE);
        SharedPreferences.Editor editor = lastPlayedSong.edit();
        editor.putInt("position",SONG_POS);
        editor.apply();

    }

    private int loadLastPlayedSong(){

        lastPlayedSong = getSharedPreferences("lastPlayed",MODE_PRIVATE);
        return lastPlayedSong.getInt("position",0);

    }

}
