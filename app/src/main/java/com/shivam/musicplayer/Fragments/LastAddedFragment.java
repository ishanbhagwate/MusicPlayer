package com.shivam.musicplayer.Fragments;


import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.shivam.musicplayer.Adapters.LastAddedAdapter;
import com.shivam.musicplayer.Adapters.SongsAdapter;
import com.shivam.musicplayer.CustomTouchListener;
import com.shivam.musicplayer.Model.Songs;
import com.shivam.musicplayer.MusicService;
import com.shivam.musicplayer.PlayerActivity;
import com.shivam.musicplayer.R;
import com.shivam.musicplayer.onItemClickListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LastAddedFragment extends Fragment {

    private RecyclerView recyclerView;
    private Context context;
    private LastAddedAdapter lastAddedAdapter;
    private View view;

    private ArrayList<Songs> songsList;

    private CardView cardView;

    private MusicService player;
    boolean serviceBound = false;


    private SlidingUpPanelLayout slidingUpPanelLayout;


    public LastAddedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_last_added,container,false);

        return view;
    }

    @Override
    public void onViewCreated( View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songsList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.lastAddedSongsRecyclerView_id);
        context = getActivity();
        cardView = view.findViewById(R.id.allslidingPanelLayout2_id);

        cardView.animate().setInterpolator(new DecelerateInterpolator()).setDuration(400).alpha(1).scaleX(1).scaleY(1).translationY(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            int hasReadExtPerm = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int hasRecordPerm = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO);

            if (( hasReadExtPerm == PackageManager.PERMISSION_GRANTED )&& ( hasRecordPerm == PackageManager.PERMISSION_GRANTED)){

                loadAudio();
                setupRecyclerView();

            }else {

                Dexter.withActivity(getActivity())
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {

                                if (report.areAllPermissionsGranted()){

                                    Toast.makeText(getActivity(), "Granted", Toast.LENGTH_SHORT).show();
                                    loadAudio();
                                    setupRecyclerView();

                                }else if (report.isAnyPermissionPermanentlyDenied()){

                                    Toast.makeText(getActivity(), "Denied", Toast.LENGTH_SHORT).show();

                                }

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                                token.continuePermissionRequest();

                            }
                        }).check();

            }
        }else {

            loadAudio();
            setupRecyclerView();

        }

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            //we have bound to LocalService, cast the Ibinder and get LocalService instance

            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(player, "Service Bound", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            serviceBound = false;

        }
    };

    @Override
    public void onSaveInstanceState( Bundle outState) {

        outState.putBoolean("ServiceState",serviceBound);

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onViewStateRestored( Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            serviceBound = savedInstanceState.getBoolean("ServiceState");
        }

    }


    private void setupRecyclerView() {

        if (songsList.size() > 0){

            SongsAdapter adapter = new SongsAdapter(songsList,getActivity());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addOnItemTouchListener(new CustomTouchListener(getActivity(), new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {

                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("index",index);
                    intent.putExtra("fromIntent",true);
                    context.startActivity(intent);

                }
            }));
        }
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setAdapter(new SongsAdapter(songsList));

    }

    private void loadAudio(){

        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC;
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED;
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

}
