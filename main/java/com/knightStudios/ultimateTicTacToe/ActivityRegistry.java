package com.knightStudios.ultimateTicTacToe;

import android.app.Activity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.knightStudios.ultimateTicTacToe.GameActivity.gameMusic;
import static com.knightStudios.ultimateTicTacToe.MainActivity.memory;
import static com.knightStudios.ultimateTicTacToe.MainActivity.slowMusic;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.cheering;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.failure;

/**
 * This Class is used to shut down the whole application at once by use of finishAll.
 *
 * created by ritterd on 04.08.2018
 */
 class ActivityRegistry {

    private static List<Activity> _activities;



    static void register(Activity activity) {
        if(_activities == null) {
            _activities = new ArrayList<>();
        }
        _activities.add(activity);
    }



    static void finishAll() {


        if (gameMusic != null){
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;
        }
        if (slowMusic != null){
            slowMusic.stop();
            slowMusic.release();
            slowMusic = null;
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

        for (Activity activity : _activities) {
            activity.finish();
        }
    }
}