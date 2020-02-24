package com.shivam.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shivam.musicplayer.Model.Songs;

import static android.content.ContentValues.TAG;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    private ArrayList<Songs> songsList;
    private Context context;
    int index = 0;

    private Intent playIntent;
    private MediaService mediaService;
    private ServiceConnection mediaConnection;
    boolean isServiceBound = false;

    private TextView songName,artistName;
    private ImageButton playBtn,pauseBtn,forwardBtn,reverseBtn,closeBtn;
    private SeekBar progressBar;
    private CircleImageView albumArt;

    private Animation rotateAnim;

    private Handler handlerSeekbar;
    private Runnable runnableSeekbar;

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.shivam.musicplayer";

    public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;

    public static final String[] WRITE_EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };

    private SharedPreferences musicPlayedPref;
    private SharedPreferences.Editor editor;

    private int pauseCurrentPosition;

    private int position;
    private Long id;
    private String artist;
    private String name;
    private String path;
    private Boolean allSongs;

    private Boolean fromIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        closeBtn = findViewById(R.id.closeBtn_id);
        playBtn = findViewById(R.id.playBtn_id);
        pauseBtn = findViewById(R.id.pauseBtn_id);
        forwardBtn = findViewById(R.id.forwardBtn_id);
        reverseBtn = findViewById(R.id.reverseBtn_id);
        progressBar = findViewById(R.id.songsProgressBar_id);
        songName = findViewById(R.id.songName_id);
        artistName = findViewById(R.id.songArtist_id);
        albumArt = findViewById(R.id.albumArt_id);

        musicPlayedPref = getSharedPreferences("lastPlayedPref",0);
        editor = musicPlayedPref.edit();
        context = getApplicationContext();

        rotateAnim = AnimationUtils.loadAnimation(PlayerActivity.this,R.anim.rotate);
        rotateAnim.setFillAfter(true);

        albumArt.animate().scaleX(1).scaleY(1).alpha(1).setDuration(250).setInterpolator(new DecelerateInterpolator());

        songsList = new ArrayList<>();
        context = getApplicationContext();

        mediaService = new MediaService();

        initialize();

        fromIntent = false;

        loadAudio();

        fromIntent = getIntent().getBooleanExtra("fromIntent",false);
        position = getIntent().getIntExtra("index",0);

//        setupRunnable();

        if (fromIntent) {

            songName.setText(songsList.get(position).getTitle());
            artistName.setText(songsList.get(position).getArtist());

            playBtn.animate().alpha(0).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
            pauseBtn.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
            playBtn.setElevation(4);
            pauseBtn.setElevation(5);

            progressBar.setMax(mediaService.getMediaDuration() / 1000);
//            runnableSeekbar.run();
            saveLastPlayedPref(true);

            animateCircle();

        }else {

            loadLastPlayedPref();

            Log.i(TAG, "onCreate: " + mediaService.isPlaying());

        }


        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playBtn.animate().alpha(0).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
                pauseBtn.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
                playBtn.setElevation(4);
                pauseBtn.setElevation(5);
                animateCircle();

                mediaService.setSelectedSong(position);

//                runnableSeekbar.run();

                progressBar.setMax(mediaService.getMediaDuration() / 1000);

                songName.setText(mediaService.getSongTitle());
                artistName.setText(mediaService.getSongArtist());

                saveLastPlayedPref(true);

//                Toast.makeText(context,String.valueOf( mediaService.getMediaDuration()), Toast.LENGTH_SHORT).show();

            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playBtn.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
                pauseBtn.animate().alpha(0).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
                playBtn.setElevation(5);
                pauseBtn.setElevation(4);

                albumArt.clearAnimation();

                mediaService.pauseSong();

                releasePlayer();

                songName.setText(mediaService.getSongTitle());
                artistName.setText(mediaService.getSongArtist());

                saveLastPlayedPref(false);

            }
        });

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mediaService.onSeekChange(progress);
                }
//                Toast.makeText(context, String.valueOf(progress), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (position < songsList.size() - 1){

                    mediaService.nextSong();
                    position++;
                    releasePlayer();
//                runnableSeekbar.run();
                    songName.setText(mediaService.getSongTitle());
                    artistName.setText(mediaService.getSongArtist());

                }else {

                    position = 0;
                    mediaService.nextSong();
                    mediaService.nextSong();
                    releasePlayer();
//                runnableSeekbar.run();
                    songName.setText(mediaService.getSongTitle());
                    artistName.setText(mediaService.getSongArtist());

                }

                playBtn.animate().alpha(0).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
                pauseBtn.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
                playBtn.setElevation(4);
                pauseBtn.setElevation(5);

                animateCircle();

                saveLastPlayedPref(true);

            }
        });

        reverseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (position == 0){

                    position = songsList.size() - 1;
                    mediaService.previousSong();
                    songName.setText(mediaService.getSongTitle());
                    artistName.setText(mediaService.getSongArtist());

                }else {

                    mediaService.previousSong();
                    position--;
                    releasePlayer();
//                runnableSeekbar.run();
                    songName.setText(mediaService.getSongTitle());
                    artistName.setText(mediaService.getSongArtist());

                }

                playBtn.animate().alpha(0).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
                pauseBtn.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
                playBtn.setElevation(4);
                pauseBtn.setElevation(5);

                animateCircle();

                saveLastPlayedPref(true);

            }
        });



    }


    private void bindService(){

        if (mediaConnection == null){

            mediaConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    MediaService.PlayerBinder playerBinder = (MediaService.PlayerBinder) service;
                    mediaService = playerBinder.getService();
                    isServiceBound = true;

                    mediaService.setSongList(songsList);

//                    Toast.makeText(context, "Service bound", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                    isServiceBound = false;

                }
            };

        }

    }

    private void unBindService(){

        unbindService(mediaConnection);
        isServiceBound = false;

        editor.putBoolean("isServiceBound", isServiceBound);

        Log.i(TAG, "unBindService: "+ isServiceBound);

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

//        outState.putBoolean("ServiceState",isServiceBound);

        editor.putBoolean("isServiceBound", isServiceBound);

        Log.i(TAG, "onRestoreInstanceState: "+ isServiceBound);

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

//        isServiceBound = savedInstanceState.getBoolean("ServiceState");

        isServiceBound = musicPlayedPref.getBoolean("isServiceBound",false);

        Log.i(TAG, "onRestoreInstanceState: "+ isServiceBound);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!isServiceBound) {

            bindService();

            playIntent = new Intent(this, MediaService.class);
            if (!fromIntent){
                playIntent.putExtra("notFromIntent",true);
            }else {
                playIntent.putExtra("position",position);
            }
            bindService(playIntent, mediaConnection, BIND_AUTO_CREATE);
            startService(playIntent);

            fromIntent = false;

            Log.i(TAG, "onStart: bound");

        }else {

            //service is active

        }

    }


    private void loadAudio(){

        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC;
        String sortOrder = MediaStore.Audio.Media.TITLE;
        Cursor cursor = contentResolver.query(uri,null,selection,null ,sortOrder);

        if (cursor != null && cursor.getCount() > 0){

            songsList = new ArrayList<>();
            while (cursor.moveToNext()){

                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));

                //save to audioList
                songsList.add(new Songs(title,artist,data,album, songUri));

            }
            cursor.close();
        }
    }


    //TODO:Implement another permission request method
    private void initialize() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(WRITE_EXTERNAL_STORAGE_PERMS, AUDIO_PERMISSION_REQUEST_CODE);
        } else {

            //TODO: incomplete
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        releasePlayer();

    }

    @Override
    protected void onStop() {
        super.onStop();

        unBindService();

    }

    private void saveLastPlayedPref(boolean isPlaying){

        musicPlayedPref = getSharedPreferences("lastPlayedPref",MODE_PRIVATE);
        editor = musicPlayedPref.edit();
        editor.putInt("position",position);
        editor.putString("song",songName.getText().toString());
        editor.putString("artist",artistName.getText().toString());
        editor.putBoolean("isPlaying",isPlaying);
        editor.apply();

    }


    private void loadLastPlayedPref() {

        musicPlayedPref = getSharedPreferences("lastPlayedPref",MODE_PRIVATE);
        position = musicPlayedPref.getInt("position",0);
        songName.setText(musicPlayedPref.getString("song",null));
        artistName.setText(musicPlayedPref.getString("artist",null));

        if (musicPlayedPref.getBoolean("isPlaying",false)){
            playBtn.animate().alpha(0).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
            pauseBtn.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator());
            playBtn.setElevation(4);
            pauseBtn.setElevation(5);

        }

    }

    private void setupRunnable(){

        handlerSeekbar = new Handler();
        runnableSeekbar = new Runnable() {
            @Override
            public void run() {

                if (mediaService.isPlaying()){

                    progressBar.setProgress(mediaService.getCurrentPosition() / 1000);

                }
                handlerSeekbar.postDelayed(runnableSeekbar,500);
            }
        };

    }

    private void releasePlayer() {

        if (handlerSeekbar != null && runnableSeekbar != null){
            handlerSeekbar.removeCallbacks(runnableSeekbar);
        }

    }

    public void animateCircle(){

        albumArt.startAnimation(rotateAnim);

    }

    public void stopAnimation(){

        albumArt.clearAnimation();

    }

    public List<Songs> getSongList(){

        //retireve song info
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,null,null,null,MediaStore.MediaColumns.TITLE);

        if (musicCursor != null && musicCursor.moveToFirst()){

            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int path = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(path);
//                songsList.add(new Songs(thisId,thisTitle,thisArtist,thisPath, album));
            }
            while (musicCursor.moveToNext());

        }

        return songsList;

    }

    public List<Songs> getLastAddedSongsList(){

        //retireve song info
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,null,null,null,MediaStore.MediaColumns.DATE_MODIFIED);

        if (musicCursor != null && musicCursor.moveToLast()){

            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int path = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(path);
//                songsList.add(new Songs(thisId,thisTitle,thisArtist,thisPath, album));
            }
            while (musicCursor.moveToPrevious());

        }

        return songsList;

    }


}
