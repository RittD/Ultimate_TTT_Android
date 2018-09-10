package com.knightStudios.ultimateTicTacToe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import static com.knightStudios.ultimateTicTacToe.GameActivity.gameMusic;
import static com.knightStudios.ultimateTicTacToe.MainActivity.memory;
import static com.knightStudios.ultimateTicTacToe.MainActivity.slowMusic;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.cheering;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.failure;


public class InstructionsActivity extends AppCompatActivity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //register to shut all down at once
        ActivityRegistry.register(this);
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(memory.getBoolean("MusicEnabled", true) && (slowMusic != null || gameMusic != null)){
            if(memory.getString("MusicPlayer", "main").equals("main")){
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
        if(memory.getBoolean("MusicEnabled", true)){
            if(memory.getString("MusicPlayer", "main").equals("main")){

                MainActivity.slowMusic.start();
            }
            else{
                gameMusic.start();
            }
        }
    }



    /**
     * When the activity is destroyed the media player has to be released
     */
    //@Override
    /*protected void onDestroy() {
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


    //Instruction Activity is above the others
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition( R.anim.slide_in_top, R.anim.slide_out_bottom);
    }
}