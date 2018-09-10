package com.knightStudios.ultimateTicTacToe;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import static com.knightStudios.ultimateTicTacToe.GameActivity.gameMusic;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.cheering;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.failure;


public class MainActivity extends AppCompatActivity {

    public static SharedPreferences memory;
    private boolean backPressed = false;
    public static MediaPlayer slowMusic;
    public static InterstitialAd myAd;
    public static boolean aiGame = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        MobileAds.initialize(this, "ca-app-pub-4850295519510041~1526542502");


        //register to shut all down at once
        ActivityRegistry.register(this);

        //Initialize slow music
        slowMusic = MediaPlayer.create(MainActivity.this, R.raw.fragmented_slow);
        slowMusic.setLooping(true);


        //saves various data (right now only whether music is enabled or disabled)
        memory = getApplicationContext().getSharedPreferences("SavedData", 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_instructions:
                Intent startInstructions = new Intent( this, InstructionsActivity.class);
                startActivity(startInstructions);
                overridePendingTransition( R.anim.slide_in_bottom, R.anim.slide_out_top);
                return true;

            case R.id.action_settings:
                Intent startSettings = new Intent( this, SettingsActivity.class);
                startActivity(startSettings);
                overridePendingTransition( R.anim.slide_in_bottom, R.anim.slide_out_top);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(memory.getBoolean("MusicEnabled", true) && slowMusic != null){
            slowMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //save current media player
        SharedPreferences.Editor editor = memory.edit();
        editor.putString("MusicPlayer", "main");
        editor.apply();

        if(memory.getBoolean("MusicEnabled", true)){
            slowMusic.start();
        }

        myAd = new InterstitialAd(this);
        //Test id: ca-app-pub-3940256099942544/1033173712 My id: ca-app-pub-4850295519510041/1658258655
        myAd.setAdUnitId("ca-app-pub-4850295519510041/1658258655");
        myAd.loadAd(new AdRequest.Builder().build());
    }

    /**
     * When the activity is destroyed the media player has to be released
     */
    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        if (slowMusic != null){
            slowMusic.stop();
            slowMusic.release();
            slowMusic = null;
        }
        if (gameMusic != null){
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;
        }
        if (cheering != null){
            cheering.stop();
            cheering.release();
            cheering = null;
        }
        if (failure != null){
            failure.stop();
            failure.release();
            failure = null;
        }
    }*/


    @Override
    public void onBackPressed() {
        if(backPressed){
            backPressed = false;
            ActivityRegistry.finishAll();
        }
        else{

            //Leaving Toast
            Toast quitToast = Toast.makeText(getApplicationContext(), getString(R.string.exit), Toast.LENGTH_SHORT);
            LinearLayout toastLayout = (LinearLayout) quitToast.getView();
            TextView toastTextView = (TextView) toastLayout.getChildAt(0);
            toastTextView.setTextSize(getResources().getDimension(R.dimen.text_size) / getResources().getDisplayMetrics().density);
            quitToast.show();

            backPressed = true;
            new Handler().postDelayed(() -> backPressed = false, 3000);
        }
    }



    public void start2Players(View v){
        Intent players = new Intent(this, MenuActivity.class);
        aiGame = false;
        startActivity(players);
    }

    public void startKI(View v){
        Intent ki = new Intent(this, MenuActivity.class);
        aiGame = true;
        startActivity(ki);
    }

}