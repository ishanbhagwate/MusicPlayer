package com.shivam.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.shivam.musicplayer.Adapters.ViewPagerAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView textView;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;

    public static final String[] WRITE_EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.mainToolbar_id);
        setSupportActionBar(toolbar);

        //runTimePermissions();

        viewPager = findViewById(R.id.mainViewPager_id);
        tabLayout = findViewById(R.id.tabLayout_id);
        textView= findViewById(R.id.text_id);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0);

        tabLayout.setupWithViewPager(viewPager,true);
        tabLayout.getTabAt(0).setText("All songs");
        tabLayout.getTabAt(1).setText("Last Added");

    }



    private void runTimePermissions() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
//                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(WRITE_EXTERNAL_STORAGE_PERMS, AUDIO_PERMISSION_REQUEST_CODE);
//        } else {
//
//            //TODO: incomplete
//        }

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()){

                            Toast.makeText(MainActivity.this, "Granted", Toast.LENGTH_SHORT).show();

                        }else if (report.isAnyPermissionPermanentlyDenied()){

                            Toast.makeText(MainActivity.this, "Denied", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                        token.continuePermissionRequest();

                    }
                }).check();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.playerBtn_id){

            Intent playerIntent = new Intent(MainActivity.this,PlayerActivity.class);
            startActivity(playerIntent);

        }

        return true;
    }


}
