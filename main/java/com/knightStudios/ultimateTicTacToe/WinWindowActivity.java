package com.knightStudios.ultimateTicTacToe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.easyandroidanimations.library.BlinkAnimation;
import com.easyandroidanimations.library.PuffInAnimation;
import com.google.android.gms.ads.AdListener;

import static com.knightStudios.ultimateTicTacToe.GameActivity.gameMusic;
import static com.knightStudios.ultimateTicTacToe.MainActivity.slowMusic;

/**
 * The Activity that is shown after the game is finished. It leads either back to GameActivity (RESTART)
 * or to MainActivity (NEW GAME).
 */
public class WinWindowActivity extends AppCompatActivity {

    public static MediaPlayer cheering;
    public static MediaPlayer failure;
    private boolean adShown = false;

    /**
     * Depending on the outcome of the game the headline, its text color, the image and the name on
     * the cup is changed.
     *
     *
     * @param savedInstanceState the saved state (?)
     */
    @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_window);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //register to shut all down at once
        ActivityRegistry.register(this);


        TextView headline = findViewById(R.id.headline);
        String outcome = getIntent().getExtras().getString("outcome");

        //draw => change nothing
        if (!outcome.equals(getString(R.string.draw))){

            //show outcome if it is no draw (default)
            new BlinkAnimation(headline).setDuration(2000).setNumOfBlinks(2).animate();
            headline.setText(outcome);

            //get outcome picture (0 = draw, 1 = cup, 2 = sad smiley)
            ViewFlipper picture = findViewById(R.id.picture);

            //a human player won => show cup and starting cheering sounds if enabled
            if(!outcome.equals(getString(R.string.you_lost))){

                //show cup
                picture.setDisplayedChild(1);

                //start cheering
                if (MainActivity.memory.getBoolean("SoundEnabled", true)) {
                    cheering = MediaPlayer.create(WinWindowActivity.this, R.raw.cheering);
                    cheering.start();
                }

                //a human player won => put his name on the cup

                TextView winnerCup = findViewById(R.id.winner);
                new PuffInAnimation(winnerCup).setDuration(1500).animate();
                winnerCup.setText(getIntent().getExtras().getString("winner"));

                //the first player won
                if (getIntent().getExtras().getString("winner").
                        equals(getIntent().getExtras().getString("name1Input"))){
                    headline.setTextColor(getIntent().getExtras().getInt("color1"));
                    winnerCup.setTextColor(getIntent().getExtras().getInt("color1"));
                }

                //the second player won
                else{
                    headline.setTextColor(getIntent().getExtras().getInt("color2"));
                    winnerCup.setTextColor(getIntent().getExtras().getInt("color2"));
                }

            }



            //the player loses => show sad smiley and play disappointed sounds
            else{
                //show sad smiley
                picture.setDisplayedChild(2);

                //start disappointed sounds
                if (MainActivity.memory.getBoolean("SoundEnabled", true)) {
                    failure = MediaPlayer.create(WinWindowActivity.this, R.raw.failure);
                    failure.start();
                }
            }
        }
    }



    /**
     * Overwrites the standard with my own menu.
     * @param menu the standard menu
     * @return whether it worked or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }



    /**
     * Decides where the menu entry lead. There are two entries: Instructions and Settings.
     * Both are "located above" the WinWindowActivity as represented in the animations.
     * @param item the clicked menu entry
     * @return whether the user was led were he wanted to go
     */
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



    /**
     * When the activity is paused, the sounds stop as well.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(cheering != null) {
            cheering.pause();
        }
        if(failure != null) {
            failure.pause();
        }
    }



    /**
     * When the activity is resumed, the sound that was played before is resumed. If the ad was shown before,
     * the new game is started.
     */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = MainActivity.memory.edit();
        editor.putString("MusicPlayer", "main");
        editor.apply();

        //on resume after showing an ad start a new game
        Intent newGame = new Intent(this, MainActivity.class);
        if (adShown){
            startActivity(newGame);
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



    /**
     * Restart the game by starting GameActivity with the same data as last game.
     * Override the animation to show you're going "back"(left) when restarting.
     * @param v the clicked button for restart
     */
    public void restart(View v) {
        Intent restart = new Intent(this, GameActivity.class);
        Bundle b = getIntent().getExtras();
        restart.putExtra("name1Input", b.getString("name1Input"));
        restart.putExtra("name2Input", b.getString("name2Input"));
        restart.putExtra("color1", b.getInt("color1"));
        restart.putExtra("color2", b.getInt("color2"));
        restart.putExtra("sign1Input", b.getChar("sign1Input"));
        restart.putExtra("sign2Input", b.getChar("sign2Input"));
        restart.putExtra("ultimateSelected", b.getBoolean("ultimateSelected"));
        restart.putExtra("restarted", true);

        startActivity(restart);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }



    /**
     * Start a new game. But first show a interstitial ad (if it could be loaded during the game)
     * @param v the clicked new game button
     */
    public void newGame(View v){
        if (MainActivity.myAd.isLoaded()) {
            MainActivity.myAd.show();
            adShown = true;
        }
        else{
            Intent newGame = new Intent(this, MainActivity.class);
            startActivity(newGame);
        }
    }

}