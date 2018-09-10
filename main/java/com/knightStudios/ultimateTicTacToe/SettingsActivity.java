package com.knightStudios.ultimateTicTacToe;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import static com.knightStudios.ultimateTicTacToe.GameActivity.gameMusic;
import static com.knightStudios.ultimateTicTacToe.MainActivity.slowMusic;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.cheering;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.failure;

public class SettingsActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //register to shut all down at once
        ActivityRegistry.register(this);

        //Initialize switch positions
        Switch music = findViewById(R.id.music_switch);
        music.setChecked(MainActivity.memory.getBoolean("MusicEnabled",true));

        Switch sound = findViewById(R.id.sound_switch);
        sound.setChecked(MainActivity.memory.getBoolean("SoundEnabled",true));
    }



    //If music is enabled, disable it and the other way round
    public void toggleMusic(View v){
        SharedPreferences.Editor editor = MainActivity.memory.edit();

        //if the music is enabled, disable it
        if(MainActivity.memory.getBoolean("MusicEnabled",true)){


            //check which MusicPlayer is currently playing
            if(MainActivity.memory.getString("MusicPlayer", "main").equals("main")){
                slowMusic.pause();
            }
            else{
                gameMusic.pause();
            }

            editor.putBoolean("MusicEnabled", false);
        }

        //if the music is disabled, enable it
        else{

            //check which MusicPlayer is currently playing
            if(MainActivity.memory.getString("MusicPlayer", "main").equals("main")){
                slowMusic.start();
            }
            else{
                gameMusic.start();
            }

            editor.putBoolean("MusicEnabled", true);
        }
        editor.apply();
    }



    //If sound is enabled, disable it and the other way round
    public void toggleSound(View v){
        SharedPreferences.Editor editor = MainActivity.memory.edit();
        editor.putBoolean("SoundEnabled", !MainActivity.memory.getBoolean("SoundEnabled",true));
        editor.apply();
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(MainActivity.memory.getBoolean("MusicEnabled", true) && (slowMusic != null || gameMusic != null)){
            if(MainActivity.memory.getString("MusicPlayer", "main").equals("main")){
                slowMusic.pause();
            }
            else{
                gameMusic.pause();
            }

        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if(MainActivity.memory.getBoolean("MusicEnabled", true)){
            if(MainActivity.memory.getString("MusicPlayer", "main").equals("main")){
                slowMusic.start();
            }
            else{
                gameMusic.start();
            }
        }
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



    //Setting Activity is above the others
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition( R.anim.slide_in_top, R.anim.slide_out_bottom);
    }



    public void sendMail(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support_knightstudios@mail.de"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback Ultimate Tic Tac Toe");
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_mail)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Es sind keine Mail-Clients installiert, weshalb die Mail nicht versendet werden kann.", Toast.LENGTH_LONG).show();
        }
    }



    public void startPlayStore(View view) {
        String url = "https://play.google.com/store/apps/details?id=com.knightStudios.ultimate_tictactoe";

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        startActivity(intent);
    }
}