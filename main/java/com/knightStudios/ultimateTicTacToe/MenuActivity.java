package com.knightStudios.ultimateTicTacToe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import static com.knightStudios.ultimateTicTacToe.GameActivity.gameMusic;
import static com.knightStudios.ultimateTicTacToe.MainActivity.aiGame;
import static com.knightStudios.ultimateTicTacToe.MainActivity.memory;
import static com.knightStudios.ultimateTicTacToe.MainActivity.slowMusic;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.cheering;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.failure;


public class MenuActivity extends AppCompatActivity{

    public static boolean easyMode = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //register to shut all down at once
        ActivityRegistry.register(this);

        ToggleButtonGroupTableLayout symbolChoose = findViewById(R.id.symbolChoose);
        symbolChoose.onClick(findViewById(R.id.radioClassic));


        //replace "Player 1:" with "Player:" and "Player 2" with "AI:" in singleplayer mode
        if (aiGame) {
            TextInputLayout namePlayer = findViewById(R.id.first_name);
            namePlayer.setHint(getString(R.string.player));


            //replace secondName
            TextInputLayout secondName = findViewById(R.id.second_name);
            secondName.setVisibility(View.GONE);
            ViewSwitcher difficulty = findViewById(R.id.difficulty);
            difficulty.setVisibility(View.VISIBLE);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(memory.getBoolean("MusicEnabled", true) && slowMusic != null){
            MainActivity.slowMusic.pause();
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
            MainActivity.slowMusic.start();
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
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



    //Swap difficulty
    public void showOtherDifficulty(View v){
        ViewSwitcher difficulty = findViewById(R.id.difficulty);
        difficulty.showNext();
    }



    // On button-click
    public void startGame(View v) {
        TextView name1, name2;
        String namePlayer1, namePlayer2;

        //Build intent
        Intent intentGameActivity = new Intent(this, GameActivity.class);


        //Add additional data (names, color, signs and mode):


        //NAME INPUT:

        //find input
        name1 = findViewById(R.id.name1);
        namePlayer1 = name1.getText().toString();

        //second name could be "[difficulty] AI"
        if(!aiGame) {
            name2 = findViewById(R.id.name2);
            namePlayer2 = name2.getText().toString();
        }
        else{
            ViewSwitcher difficulty = findViewById(R.id.difficulty);
            Button currentDifficulty = (Button) difficulty.getCurrentView();
            namePlayer2 = currentDifficulty.getText().toString();
            easyMode = namePlayer2.equals(getString(R.string.easy_ai));
        }

        //names are not allowed to be the same nor empty
        if(!namePlayer1.equals("") && namePlayer1.equals(namePlayer2)){
            Toast sameNameToast = Toast.makeText(getApplicationContext(), getString(R.string.same_names), Toast.LENGTH_SHORT);
            LinearLayout toastLayout = (LinearLayout) sameNameToast.getView();
            TextView toastTextView = (TextView) toastLayout.getChildAt(0);
            toastTextView.setTextSize(getResources().getDimension(R.dimen.text_size) / getResources().getDisplayMetrics().density);
            sameNameToast.show();
            return;
        }

        intentGameActivity.putExtra("name1Input", namePlayer1);
        intentGameActivity.putExtra("name2Input", namePlayer2);



        //COLOR INPUT:

        RadioGroup color1 = findViewById(R.id.chooseColor1);

        switch(color1.getCheckedRadioButtonId()){

            case R.id.black1:
                intentGameActivity.putExtra("color1", getResources().getColor(R.color.black));
                break;

            case R.id.red1:
                intentGameActivity.putExtra("color1", getResources().getColor(R.color.red));
                break;

            case R.id.blue1:
                intentGameActivity.putExtra("color1", getResources().getColor(R.color.blue));
                break;

            case R.id.green1:
                intentGameActivity.putExtra("color1", getResources().getColor(R.color.green));
                break;

            //Error: No color input!
            default:
                Toast.makeText(getApplicationContext(),getString(R.string.error_color_choose),Toast.LENGTH_SHORT).show();
        }



        RadioGroup color2 = findViewById(R.id.chooseColor2);

        switch(color2.getCheckedRadioButtonId()) {

            case R.id.black2:

                intentGameActivity.putExtra("color2", getResources().getColor(R.color.black));
                break;

            case R.id.blue2:

                intentGameActivity.putExtra("color2", getResources().getColor(R.color.blue));
                break;

            case R.id.green2:

                intentGameActivity.putExtra("color2", getResources().getColor(R.color.green));
                break;

            //red is default:
            default:
                intentGameActivity.putExtra("color2", getResources().getColor(R.color.red));

        }

        //SIGN INPUT:

        ToggleButtonGroupTableLayout sign = findViewById(R.id.symbolChoose);

        switch(sign.getCheckedRadioButtonId()){

            case R.id.radioSquares:
                intentGameActivity.putExtra("sign1Input", '\u25A0');
                intentGameActivity.putExtra("sign2Input", '\u25A1');
                break;

            case R.id.radioCircles:
                intentGameActivity.putExtra("sign1Input", '\u25CF');
                intentGameActivity.putExtra("sign2Input", '\u25CB');
                break;

            case R.id.radioTriangles:
                intentGameActivity.putExtra("sign1Input", '\u25B2');
                intentGameActivity.putExtra("sign2Input", '\u25B3');
                break;

            default:
                intentGameActivity.putExtra("sign1Input", 'X');
                intentGameActivity.putExtra("sign2Input", 'O');
        }

        //Mode Input:
        RadioButton ultimateMode = findViewById(R.id.ultimate_mode);
        intentGameActivity.putExtra("ultimateSelected", ultimateMode.isChecked());



        //Switch to GameActivity
        startActivity(intentGameActivity);
    }



    //Checks the color input (the players shall not have the same color)
    /*public void checkPossible(View v){

        RadioButton other;
        RadioGroup otherGroup;

        switch(v.getId()){

            case R.id.black1:
                other = findViewById(R.id.black2);
                otherGroup = findViewById(R.id.chooseColor2);
                break;

            case R.id.black2:
                other = findViewById(R.id.black1);
                otherGroup = findViewById(R.id.chooseColor1);
                break;

            case R.id.red1:
                other = findViewById(R.id.red2);
                otherGroup = findViewById(R.id.chooseColor2);
                break;

            case R.id.red2:
                other = findViewById(R.id.red1);
                otherGroup = findViewById(R.id.chooseColor1);
                break;

            case R.id.blue1:
                other = findViewById(R.id.blue2);
                otherGroup = findViewById(R.id.chooseColor2);
                break;

            case R.id.blue2:
                other = findViewById(R.id.blue1);
                otherGroup = findViewById(R.id.chooseColor1);
                break;

            case R.id.green1:
                other = findViewById(R.id.green2);
                otherGroup = findViewById(R.id.chooseColor2);
                break;

            default:
                other = findViewById(R.id.green1);
                otherGroup = findViewById(R.id.chooseColor1);
        }

        if(other.isChecked()){
            otherGroup.clearCheck();
        }
    }*/

}