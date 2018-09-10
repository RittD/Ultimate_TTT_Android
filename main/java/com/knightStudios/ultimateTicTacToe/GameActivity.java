package com.knightStudios.ultimateTicTacToe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.easyandroidanimations.library.BlinkAnimation;
import com.easyandroidanimations.library.BounceAnimation;
import com.easyandroidanimations.library.FadeInAnimation;
import com.easyandroidanimations.library.FlipHorizontalAnimation;
import com.easyandroidanimations.library.ShakeAnimation;


import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.knightStudios.ultimateTicTacToe.MainActivity.aiGame;
import static com.knightStudios.ultimateTicTacToe.MainActivity.slowMusic;
import static com.knightStudios.ultimateTicTacToe.MenuActivity.easyMode;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.cheering;
import static com.knightStudios.ultimateTicTacToe.WinWindowActivity.failure;


/**
 * The Activity that controls the whole game from the start in the MenuActivity
 * until the end of the game leading to the WinWindowActivity.
 *
 *
 *
 * Annotation 1:
 * In many methods an integer is used to represent a field.
 * -1 stands for the whole field in ultimate mode
 * 0 stands for the whole field in classic mode
 * 1-9 (counting from left to right and top to bottom) is the n.th small field in ultimate mode
 *
 *
 *
 * Annotation 2:
 * To support a better feeling while playing and the included animations there are several short waiting times
 * included in the player's and the AI's turns:
 *
 * Player's turn:
 * - classic mode: 200 ms after turn is over (show FadeInAnimation (100 ms) and let time to realize turn of opponent)
 * - ultimate mode: 200 ms after sign putting (show FadeInAnimation (100 ms) and let time to realize a field was won...)
 *                  400 ms after turn is over (show BounceAnimation (400 ms) and make it clear whose turn it is)
 *
 * AI's turn:
 * - classic mode: 300 - 400 ms before sign putting (time to "think")
 *                 200 ms after turn is over (show FadeInAnimation (100 ms) and let time to realize turn of opponent)
 * - ultimate mode: 400 - 500 ms before the sign putting (time to "think")
 *                  200 ms after the sign putting (show FadeInAnimation (100 ms) and let time to realize a field was lost...)
 *                  400 ms after turn is over (show BounceAnimation(400 ms) and make it clear its the player's turn)
 *
 *
 *
 * created by ritterd on 04.08.2018
 */

public class GameActivity extends AppCompatActivity {

    private String      currentPlayer;
    private String      otherPlayer;
    private char        currentPlayerSign;
    private char        otherPlayerSign;
    private boolean     currentPlayerStarted    = true;
    private boolean[]   markedCellsByCurrent    = new boolean[9];
    private boolean[]   blockedFields           = new boolean[9];
    private boolean     classicMode             = false;
    private int         currentField            = -1;
    private int         nextField               = -1;
    private boolean     gameOver                = false;
    private int         currentColor;
    private int         otherColor;
    private boolean     withinTurn              = false;

    public static       MediaPlayer gameMusic;
    private View[]      winningRow;



    //0. NECESSARY OVERRIDES:

    /**
     * Load activity_game, the toolbar, the media player and build scoreboard and gamefield depending on
     * the users input in the MenuActivity. Shuffle who begins and send a toast.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //register to shut all down at once
        ActivityRegistry.register(this);


        //Prepare media player
        gameMusic = MediaPlayer.create(GameActivity.this, R.raw.solarsail_faster);
        gameMusic.setLooping(true);


        //get input from menu
        initializePlayers();


        //check whether classic game is started
        if(!getIntent().getExtras().getBoolean("ultimateSelected")){
            classicMode = true;
        }



        if(classicMode){
            initializeClassicField();
        }


        //Set text sizes of the small and big TextViews
        setTextSizes();


        buildScoreboard();

        //Shuffle who starts:
        if(new Random().nextDouble() >= 0.5){
            swapPlayers();
        }

        markCurrentPlayer();

        //Message who begins:
        Toast beginnerToast = Toast.makeText(getApplicationContext(), currentPlayer+" "+getString(R.string.begins)+"!", Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) beginnerToast.getView();
        TextView toastTextView = (TextView) toastLayout.getChildAt(0);
        toastTextView.setTextSize(getResources().getDimension(R.dimen.text_size_big) / getResources().getDisplayMetrics().density);
        beginnerToast.show();


        //start AI turn if other player starts and it's an AI game
        if(!currentPlayerStarted && aiGame){
            if(classicMode){
                aiTurnClassic();
            }
            else{
                aiTurnUltimate();
            }
        }
    }



    /**
     * Pause the activity and the music if necessary.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(MainActivity.memory.getBoolean("MusicEnabled", true) && gameMusic != null){
           gameMusic.pause();
        }
    }



    /**
     * Resume the activity and start the music if it is enabled in SettingsActivity
     * (default: enabled).
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences.Editor editor = MainActivity.memory.edit();
        editor.putString("MusicPlayer", "game");
        editor.apply();

        if(MainActivity.memory.getBoolean("MusicEnabled", true)){
            gameMusic.start();
        }
    }


    /*
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
     * Overwrite the given menu by a self-made menu
     * @param menu the standard menu
     * @return whether it could load the menu or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }



    /**
     * Builds the intent to the activity chosen by clicking on a menu item and switch the activity.
     * @param item clicked item from the menu
     * @return  whether it worked or not
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
     * When the game is started for the first time the back button leads back to the menu "left" of the game screen.
     * When the game was restarted the back button leads back to the winning screen "right" of the game screen.
     * That's why the standard transition animation has to be overwritten for the second case to support this "positioning logic".
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getIntent().hasExtra("restarted")) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }





    //1. BEFORE GAME:

    /**
     * Initialize player names, colors and signs.
     */
    private void initializePlayers() {
        //Player 1:

        //Name:
        currentPlayer = getIntent().getExtras().getString("name1Input").length() > 0 ?
                getIntent().getExtras().getString("name1Input") : getString(R.string.default_player_1);


        //Color:
        currentColor = getIntent().getExtras().getInt("color1");

        //Sign:
        currentPlayerSign = getIntent().getExtras().getChar("sign1Input") > 0 ?
                getIntent().getExtras().getChar("sign1Input") : 'X';


        //Player 2:

        //Name:
        otherPlayer = getIntent().getExtras().getString("name2Input").length() > 0 ?
                getIntent().getExtras().getString("name2Input") : getString(R.string.default_player_2);


        //Color:
        otherColor = getIntent().getExtras().getInt("color2");


        //Sign:
        otherPlayerSign = getIntent().getExtras().getChar("sign2Input")>0 ?
                getIntent().getExtras().getChar("sign2Input") : 'O';

    }



    /**
     * Fill the scoreboard with the player's names, color them and change the score
     * to the player's sign if classic mode is activated.
     */
    private void buildScoreboard() {

        //Find the TextViews for the scoreboard
        TextView player1 = findViewById(R.id.player1);
        TextView player2 = findViewById(R.id.player2);

        //Add colons
        String name1Colon = currentPlayer+":";
        String name2Colon = otherPlayer+":";

        //Set the inserted names
        player1.setText(name1Colon);
        player2.setText(name2Colon);

        //Color the names
        player1.setTextColor(currentColor);
        player2.setTextColor(otherColor);

        //Color the signs/scores
        TextView score1 = findViewById(R.id.score1);
        TextView score2 = findViewById(R.id.score2);
        score1.setTextColor(currentColor);
        score2.setTextColor(otherColor);


        //signs instead of score
        if(classicMode){
            score1.setText(String.valueOf(currentPlayerSign));
            score2.setText(String.valueOf(otherPlayerSign));
        }

    }



    /**
     * Set the text sizes of all TextViews depending on the screen size and density.
     */
    private void setTextSizes() {

        //Big TextViews:

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        float widthBig = (dpWidth-60)/4;
        float widthSmall= widthBig/3;

        TextView t1= findViewById(R.id.t1);
        t1.setTextSize(widthBig);

        TextView t2= findViewById(R.id.t2);
        t2.setTextSize(widthBig);

        TextView t3= findViewById(R.id.t3);
        t3.setTextSize(widthBig);

        TextView t4= findViewById(R.id.t4);
        t4.setTextSize(widthBig);

        TextView t5= findViewById(R.id.t5);
        t5.setTextSize(widthBig);

        TextView t6= findViewById(R.id.t6);
        t6.setTextSize(widthBig);

        TextView t7= findViewById(R.id.t7);
        t7.setTextSize(widthBig);

        TextView t8= findViewById(R.id.t8);
        t8.setTextSize(widthBig);

        TextView t9= findViewById(R.id.t9);
        t9.setTextSize(widthBig);


        //Adjust text size of all small TextViews:
        TextView[] cells = getRelatedCells(-1);
        for(int j=0; j < 81; j++){
            cells[j].setTextSize(widthSmall);
        }

    }



    /**
     * Switch all fields when classic mode is chosen.
     */
    private void initializeClassicField() {
        for(int i=1; i<=9; i++){
            switchField(i);
        }
    }



    /**
     * Marks the current players name and score by increasing its text size and underlining the name.
     * This method has to be called before each turn.
     */
    private void markCurrentPlayer(){
        TextView current;
        TextView currentScore;
        TextView other;
        TextView otherScore;

        if(currentPlayerStarted){
            current = findViewById(R.id.player1);
            currentScore = findViewById(R.id.score1);
            other = findViewById(R.id.player2);
            otherScore = findViewById(R.id.score2);
        }
        else{
            current = findViewById(R.id.player2);
            currentScore = findViewById(R.id.score2);
            other = findViewById(R.id.player1);
            otherScore = findViewById(R.id.score1);
        }

        /* Swap Text Sizes (getTextSize returns px while setTextSize needs sp)
           (default: 22sp (standard text size)
         */
        float marked = getResources().getDimension(R.dimen.text_size_big) / getResources().getDisplayMetrics().density;
        other.setTextSize(TypedValue.COMPLEX_UNIT_PX, current.getTextSize());
        otherScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, current.getTextSize());
        current.setTextSize(marked);
        currentScore.setTextSize(marked);



        //Swap underlining
        String currentText = "<u>"+currentPlayer+":</u>";
        String otherText = otherPlayer+":";
        current.setText(Html.fromHtml(currentText));
        other.setText(otherText);
    }





    //2. INSERTING SIGNS:

    /**
     * This method is called when a field in classic mode is clicked. It represents the whole turn
     * of this player.
     * First it is checked whether the clicked field was clickable. If so the player's sign is placed
     * in this TextView in the player's color.
     * Then the win condition is checked
     * @param v the clicked TextView
     */
    public void inputClassic(View v){
        //Only useful if user tries to click on blocked fields in ultimate mode. These are not caught by the method isClickable!
        if(classicMode){

            //don't allow another input if it's still the AI's turn
            if(withinTurn){
                return;
            }

            TextView clicked = findViewById(v.getId());
            String currentSign = String.valueOf(currentPlayerSign);


            //try to insert the players sign
            if(isClickable(clicked)){
                withinTurn = true;

                //Set players sign
                new FadeInAnimation(clicked).setDuration(100).animate();
                clicked.setText(currentSign);
                clicked.setTextColor(currentColor);

                //wait for fade in animation
                new Handler().postDelayed(() -> {
                    //Check win condition
                    if (checkWinCondition(0)) {
                        endGame("iWin");
                        return;
                    }


                    //The game ends when there are no unblocked fields left
                    if (checkFieldBlocked(0)) {
                        endGame("draw");
                        return;
                    }

                    //switch players and mark the next player
                    swapPlayers();
                    markCurrentPlayer();

                    withinTurn = false;

                    if (aiGame) {
                        aiTurnClassic();
                    }
                },200);
            }

        }
    }



    /**
     * This method is called when a field in ultimate mode is clicked. It represents the whole turn
     * of this player.
     * First it is checked whether the clicked TextView was allowed to be clicked. If so the frame
     * that could be around the field is removed and the player's sign is set in the field.
     * Then the win condition for the current and after that the whole field is checked.
     * If the player did not win the current field is checked whether it is blocked as well as
     * the whole field after that to check for a draw.
     *
     * @param v the clicked TextView
     */
    public void inputUltimate(View v) {

        if(withinTurn){
            return;
        }

        TextView clicked = findViewById(v.getId());
        currentField = getFieldOfCell(clicked);

        //try to insert the players sign
        if(isClickable(clicked)){

            withinTurn = true;

            //Reset marked frame if existing
            removeFrame(currentField);


            //set players sign
            new FadeInAnimation(clicked).setDuration(100).animate();
            clicked.setText(String.valueOf(currentPlayerSign));
            clicked.setTextColor(currentColor);

            //wait for FadeInAnimation
            new Handler().postDelayed(() -> {
                //check whether field is finished
                if(checkWinCondition(currentField)){
                    switchField(currentField);
                    blockedFields[currentField - 1] = true;
                    incrementScore();
                }

                //check whether the whole field is finished
                if(checkWinCondition(-1)){
                    endGame("iWin");
                    return;
                }

                //mark the field as blocked if necessary
                if(checkFieldBlocked(currentField)){
                    setFieldBlocked(currentField);
                }

                //The game ends when there are no unblocked fields left. The outcome is decided by the points of the players
                if(checkFieldBlocked(-1)){
                    checkOutcomeByPoints();
                    return;
                }


                //switch players
                swapPlayers();
                markCurrentPlayer();

                //choose where next Player has to click
                limitClickability(getIndexOfCell(clicked));

                //wait for BounceAnimation (mark for next field)
                new Handler().postDelayed(()-> {
                    withinTurn = false;
                    if(aiGame){
                        aiTurnUltimate();
                    }
                },400);



            },200);

        }
    }



    /**
     * Does a turn for the computer in classic mode. It takes the most important cell if existing (in hard mode)
     * and a random one from the clickable fields if there is none. In easy mode the AI does not pick an important cell
     * with a possibility of 30%.
     */
    private void aiTurnClassic(){

        withinTurn = true;

        //wait before turn ("thinking")
        new Handler().postDelayed(() -> {

            TextView importantCell = findMostImportantCell(0),
                    randomCell = pickRandomEmptyCellFrom(filterPossibleCells(0)),
                    picked;

            //included the 30% chance that the AI does a wrong decision
            if(easyMode){
                Double d = new Random().nextDouble();
                picked =  d >= 0.3 && importantCell != null ? importantCell : randomCell;
            }
            else {
                picked = importantCell != null ? importantCell : randomCell;
            }
            //set the new current field
            if (picked != null){
                currentField = getFieldOfCell(picked);
            }

            new FadeInAnimation(picked).setDuration(100).animate();
            picked.setText(String.valueOf(currentPlayerSign));
            picked.setTextColor(currentColor);

            //wait for FadeInAnimation
            new Handler().postDelayed(()-> {
                if (checkWinCondition(0)) {
                    endGame("iLose");
                    return;
                }

                if (checkFieldBlocked(0)) {
                    endGame("draw");
                    return;
                }


                //switch players
                swapPlayers();
                markCurrentPlayer();


                withinTurn = false;
            },200);
        },new Random().nextInt(100) + 300);

    }



    /**
     * Does a turn for the computer in ultimate mode. It takes the most important clickable cell if existing (in hard mode)
     * and a random one from the best remaining cells (for the exact filter see filterPossibleCells) otherwise (in hard mode).
     * In easy mode the filter is disabled and with a 20% chance the AI misses an important cell.
     */
    private void aiTurnUltimate(){
        withinTurn = true;

        //wait before turn ("thinking")
        new Handler().postDelayed(() -> {

            TextView importantCell = findMostImportantCell(nextField),
                    randomCell = pickRandomEmptyCellFrom(filterPossibleCells(nextField)),
                    picked;

            //in easy mode the possible cells are never filtered and with a 20% chance an important cell is missed
            if(easyMode){
                Double d = new Random().nextDouble();
                picked =  d >= 0.2 && importantCell != null ? importantCell : randomCell;
            }
            else{
                picked =  importantCell != null ? importantCell : randomCell;
            }


            //set the new current field
            if (picked != null){
                currentField = getFieldOfCell(picked);
            }

            new FadeInAnimation(picked).setDuration(100).animate();
            picked.setText(String.valueOf(currentPlayerSign));
            picked.setTextColor(currentColor);

            //wait for fade in animation before continuing
            new Handler().postDelayed(() -> {

                //If there was a marked frame from the last turn, remove it
                removeFrame(currentField);

                //Check whether field is finished
                if (checkWinCondition(currentField)) {
                    switchField(currentField);
                    blockedFields[currentField - 1] = true;
                    incrementScore();


                    if (checkWinCondition(-1)) {
                        endGame("iLose");
                        return;
                    }
                }


                //mark the field as blocked if necessary
                if (checkFieldBlocked(currentField)) {
                    setFieldBlocked(currentField);


                    //The game ends when there are no unblocked fields left. The outcome is decided by the points of the players
                    if (checkFieldBlocked(0)) {
                        checkOutcomeByPoints();
                        return;
                    }
                }


                //switch players
                swapPlayers();
                markCurrentPlayer();

                //choose where next Player has to click
                limitClickability(getIndexOfCell(picked));

                //wait for BounceAnimation (mark for next field)
                new Handler().postDelayed(() -> withinTurn = false, 400);

            },200);
        },new Random().nextInt(100) + 400);

    }





    //3. CHECK WIN CONDITIONS:


    /**
     * Checks the win condition for either:
     * - the whole field in classic mode (field = 0)
     * - the whole field in ultimate mode (field = -1)
     * - a smaller field in ultimate mode (1 <= field <= 9)
     *
     * @param field the field on which the win condition shall be checked
     * @return whether the current player or AI has won this field
     */
    private boolean checkWinCondition(int field) {

        //check the whole field (classic mode)
        if(field == 0){

            TextView[] cells = getRelatedCells(0);

            for (int i = 0; i < 9; i++) {

                //save in markedCellsByCurrent[] whether checked cell is marked by current
                if(cells[i].getText().length() > 0 && cells[i].getText().charAt(0) == currentPlayerSign) {
                    markedCellsByCurrent[i] = true;
                }
            }
        }

        //check the whole field (ultimate mode)
        else if(field == -1){
            boolean[] finishedFields=new boolean[9];

            for(int i=0; i<9;i++){
                finishedFields[i] = checkWinCondition(i+1);

            }
            //Check whether current player wins with his finished fields
            System.arraycopy(finishedFields,0,markedCellsByCurrent,0,finishedFields.length);
        }

        //check a smaller field (ultimate mode)
        else {
            TextView[] cells = getRelatedCells(field);

            for (int i = 0; i < 9; i++) {

                //save in markedCellsByCurrent[] whether checked cell is marked by current
                if (cells[i].getText().length() > 0 && cells[i].getText().charAt(0) == currentPlayerSign) {
                    markedCellsByCurrent[i] = true;
                }
            }
        }

        boolean finished = true;

        //get winning row
        if(markedCellsByCurrent[0]&& markedCellsByCurrent[1] && markedCellsByCurrent[2]){
            winningRow = new View[]{findViewById(R.id.v1),findViewById(R.id.v2),findViewById(R.id.v3)};
        }
        else if(markedCellsByCurrent[3]&& markedCellsByCurrent[4] && markedCellsByCurrent[5]){
            winningRow = new View[]{findViewById(R.id.v4),findViewById(R.id.v5),findViewById(R.id.v6)};
        }
        else if(markedCellsByCurrent[6]&& markedCellsByCurrent[7] && markedCellsByCurrent[8]){
            winningRow = new View[]{findViewById(R.id.v7),findViewById(R.id.v8),findViewById(R.id.v9)};
        }
        else if(markedCellsByCurrent[0]&& markedCellsByCurrent[3] && markedCellsByCurrent[6]){
            winningRow = new View[]{findViewById(R.id.v1),findViewById(R.id.v4),findViewById(R.id.v7)};
        }
        else if(markedCellsByCurrent[1]&& markedCellsByCurrent[4] && markedCellsByCurrent[7]){
            winningRow = new View[]{findViewById(R.id.v2),findViewById(R.id.v5),findViewById(R.id.v8)};
        }
        else if(markedCellsByCurrent[2]&& markedCellsByCurrent[5] && markedCellsByCurrent[8]){
            winningRow = new View[]{findViewById(R.id.v3),findViewById(R.id.v6),findViewById(R.id.v9)};
        }
        else if (markedCellsByCurrent[0]&& markedCellsByCurrent[4] && markedCellsByCurrent[8]){
            winningRow = new View[]{findViewById(R.id.v1),findViewById(R.id.v5),findViewById(R.id.v9)};
        }
        else if(markedCellsByCurrent[2]&& markedCellsByCurrent[4] && markedCellsByCurrent[6]){
            winningRow = new View[]{findViewById(R.id.v3),findViewById(R.id.v5),findViewById(R.id.v7)};
        }
        else{

            //necessary to overwrite winningrow in ultimate mode when the whole field is checked otherwise
            //it would the winning row of a smaller field
            winningRow = null;
            finished = false;
        }



        //reset markedCellsByCurrent
        for(int i=0; i<markedCellsByCurrent.length;i++){
            markedCellsByCurrent[i]=false;
        }


        return finished;

    }



    /**
     * Auxiliary method to check the outcome of the game by points. It is only called when all fields
     * in ultimate mode are blocked.
     */
    private void checkOutcomeByPoints() {

        //current player has more points than his opponent
        if (getScore(currentPlayer) > getScore(otherPlayer)) {

            //current player is an AI
            if(aiGame && (currentPlayer.equals(getString(R.string.easy_ai)) ||
                    currentPlayer.equals(getString(R.string.hard_ai)))){
                endGame("iLose");
            }
            else {
                endGame("iWin");
            }
        }

        //current player has less points than his opponent
        else if (getScore(otherPlayer) > getScore(currentPlayer)) {

            //current player plays against an AI
            if (aiGame && (otherPlayer.equals(getString(R.string.easy_ai)) ||
                    otherPlayer.equals(getString(R.string.hard_ai)))){
                endGame("iLose");
            }
            else {
                swapPlayers();
                endGame("iWin");
            }
        }

        //Both player have the same amount of points
        else {
            endGame("draw");
        }
    }



    /**
     * Get the score of a player (won fields in ultimate mode and 0 in classic mode)
     * @param player the player of whom the score is returned
     * @return score of player
     */
    private int getScore(String player) {

        if(!classicMode) {
            TextView score;

            if (player.equals(currentPlayer)) {
                score = currentPlayerStarted ? (TextView) findViewById(R.id.score1) : (TextView) findViewById(R.id.score2);
            } else {
                score = currentPlayerStarted ? (TextView) findViewById(R.id.score2) : (TextView) findViewById(R.id.score1);
            }

            return score.getText().charAt(0) - 48;
        }
        //default score (in classic mode) is 0
        return 0;
    }



    /**
     * Increments the current player's score.
     */
    private void incrementScore() {
        TextView score = currentPlayerStarted ? (TextView) findViewById(R.id.score1) : (TextView) findViewById(R.id.score2);
        String newScoreStr=String.valueOf(getScore(currentPlayer)+1);
        score.setText(newScoreStr);
    }





    //4. THE GAME ENDS

    /**
     * The game ends when either:
     * - the win condition in classic or ultimate mode is fulfilled -> XY wins if one player fulfilled it
     *                                                              -> XY loses if the AI fulfilled it
     * - the field is full in classic mode without one player winning -> DRAW
     * - all fields are blocked in ultimate mode -> DRAW if both player have the same amount of points
     *                                           -> XY wins if one player has more points
     *                                           -> XY loses if the AI has more points
     *
     * This method is called when the game is finished and it leads to callWinActivity. It ends the game
     * (no more inputs possible), highlights the winning row (if existing) and makes a toast with the outcome.
     *
     * @param outcome is the state in which the game ends
     */
    private void endGame(String outcome) {
        gameOver = true;
        unmarkCurrentPlayer();
        LinearLayout toastLayout;
        TextView toastTextView;

        switch(outcome){

            case "iWin":

                //highlight the winning row or the player's scores otherwise (let the scores blink a little bit longer)
                if (winningRow != null) {
                    for (View w : winningRow) {
                        new BlinkAnimation(w).setDuration(2000).animate();
                    }
                }
                else{
                    new BlinkAnimation(findViewById(R.id.score1)).setDuration(2500).animate();
                    new BlinkAnimation(findViewById(R.id.score2)).setDuration(2500).animate();
                }


                //Toast player wins
                Toast winnerToast = Toast.makeText(getApplicationContext(),
                        aiGame ? getString(R.string.you_won) : currentPlayer+" "+getString(R.string.won),
                        Toast.LENGTH_SHORT);

                toastLayout = (LinearLayout) winnerToast.getView();
                toastTextView = (TextView) toastLayout.getChildAt(0);
                toastTextView.setTextSize(getResources().getDimension(R.dimen.text_size_big) / getResources().getDisplayMetrics().density);
                winnerToast.show();

                break;


            case "draw":

                //Toast Draw
                Toast drawMessage = Toast.makeText(getApplicationContext(), getString(R.string.draw), Toast.LENGTH_SHORT);
                toastLayout = (LinearLayout) drawMessage.getView();
                toastTextView = (TextView) toastLayout.getChildAt(0);
                toastTextView.setTextSize(getResources().getDimension(R.dimen.text_size_big) / getResources().getDisplayMetrics().density);
                drawMessage.show();

                break;


            case "iLose":

                //highlight the winning row
                for(View w : winningRow){
                    new BlinkAnimation(w).setDuration(2000).animate();
                }

                //Toast player loses
                Toast loserToast = Toast.makeText(getApplicationContext(), getString(R.string.you_lost), Toast.LENGTH_SHORT);
                toastLayout = (LinearLayout) loserToast.getView();
                toastTextView = (TextView) toastLayout.getChildAt(0);
                toastTextView.setTextSize(getResources().getDimension(R.dimen.text_size_big) / getResources().getDisplayMetrics().density);
                loserToast.show();

                break;

            default:
                System.out.println("No valid outcome");
                return;
        }

        callWinActivity(outcome);

    }



    /**
     * The method to unmark the name of the current player. It is only called when the game ends.
     */
    private void unmarkCurrentPlayer(){
        TextView current, currentScore, other;
        if (currentPlayerStarted){
            current = findViewById(R.id.player1);
            currentScore = findViewById(R.id.score1);
            other = findViewById(R.id.player2);
        }
        else{
            current = findViewById(R.id.player2);
            currentScore = findViewById(R.id.score2);
            other = findViewById(R.id.player1);
        }

        String withColon = currentPlayer+":";
        current.setText(withColon);
        current.setTextSize(TypedValue.COMPLEX_UNIT_PX, other.getTextSize());
        currentScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, other.getTextSize());
    }


    /**
     * The last method before the WinWindowActivity is called. It prepares all the data (depending on
     * the outcome of the game) that it needs and saves it in the intent that is used to start WinWindowActivity.
     *
     * @param outcome the state in which the game ended
     */
    private void callWinActivity(String outcome){

        final Intent newGameIntent = new Intent(this, WinWindowActivity.class);

        switch (outcome){
            case "iWin":
                newGameIntent.putExtra("winner", currentPlayer);
                newGameIntent.putExtra("outcome", currentPlayer+" "+getString(R.string.won));
                break;
            case "iLose":
                newGameIntent.putExtra("outcome",getString(R.string.you_lost));
                break;
            case "draw":
                newGameIntent.putExtra("outcome",getString(R.string.draw));
                break;
            default:
                System.out.println("No valid outcome!");
                return;
        }

        if(!currentPlayerStarted) {
            swapPlayers();
        }
        newGameIntent.putExtra("name1Input", currentPlayer);
        newGameIntent.putExtra("name2Input", otherPlayer);
        newGameIntent.putExtra("color1", currentColor);
        newGameIntent.putExtra("color2", otherColor);
        newGameIntent.putExtra("sign1Input", currentPlayerSign);
        newGameIntent.putExtra("sign2Input", otherPlayerSign);
        newGameIntent.putExtra("ultimateSelected", !classicMode);


        //wait so that you can see the result
        new Handler().postDelayed(() -> startActivity(newGameIntent), classicMode ? 2000 : 3000);

    }






    //5. SUB METHODS:



    /**
     * Return the TextViews within the given field:
     * -1 = the whole field in ultimate mode
     * 0 = the whole field in classic mode,
     * 1-9 = the smaller fields in ultimate mode.
     * @param field an integer between -1 and 9 representing a field
     * @return an array of all TextViews within the given field
     */
    private TextView[] getRelatedCells(int field) {

        TextView[] cells;

        cells = field == -1 ? new TextView[81] : new TextView[9];

        switch(field){

            case -1:
                for (int i = 0; i < 9; i++){
                    TextView[] part = getRelatedCells(i + 1);
                    System.arraycopy(part, 0, cells, 9 * i, 9);
                }
                break;

            case 0:
                cells[0] = findViewById(R.id.t1);
                cells[1] = findViewById(R.id.t2);
                cells[2] = findViewById(R.id.t3);
                cells[3] = findViewById(R.id.t4);
                cells[4] = findViewById(R.id.t5);
                cells[5] = findViewById(R.id.t6);
                cells[6] = findViewById(R.id.t7);
                cells[7] = findViewById(R.id.t8);
                cells[8] = findViewById(R.id.t9);
                break;

            case 1:
                cells[0] = findViewById(R.id.a111);
                cells[1] = findViewById(R.id.a112);
                cells[2] = findViewById(R.id.a113);
                cells[3] = findViewById(R.id.a121);
                cells[4] = findViewById(R.id.a122);
                cells[5] = findViewById(R.id.a123);
                cells[6] = findViewById(R.id.a131);
                cells[7] = findViewById(R.id.a132);
                cells[8] = findViewById(R.id.a133);
                break;

            case 2:
                cells[0] = findViewById(R.id.a211);
                cells[1] = findViewById(R.id.a212);
                cells[2] = findViewById(R.id.a213);
                cells[3] = findViewById(R.id.a221);
                cells[4] = findViewById(R.id.a222);
                cells[5] = findViewById(R.id.a223);
                cells[6] = findViewById(R.id.a231);
                cells[7] = findViewById(R.id.a232);
                cells[8] = findViewById(R.id.a233);
                break;

            case 3:
                cells[0] = findViewById(R.id.a311);
                cells[1] = findViewById(R.id.a312);
                cells[2] = findViewById(R.id.a313);
                cells[3] = findViewById(R.id.a321);
                cells[4] = findViewById(R.id.a322);
                cells[5] = findViewById(R.id.a323);
                cells[6] = findViewById(R.id.a331);
                cells[7] = findViewById(R.id.a332);
                cells[8] = findViewById(R.id.a333);
                break;

            case 4:
                cells[0] = findViewById(R.id.a411);
                cells[1] = findViewById(R.id.a412);
                cells[2] = findViewById(R.id.a413);
                cells[3] = findViewById(R.id.a421);
                cells[4] = findViewById(R.id.a422);
                cells[5] = findViewById(R.id.a423);
                cells[6] = findViewById(R.id.a431);
                cells[7] = findViewById(R.id.a432);
                cells[8] = findViewById(R.id.a433);
                break;

            case 5:
                cells[0] = findViewById(R.id.a511);
                cells[1] = findViewById(R.id.a512);
                cells[2] = findViewById(R.id.a513);
                cells[3] = findViewById(R.id.a521);
                cells[4] = findViewById(R.id.a522);
                cells[5] = findViewById(R.id.a523);
                cells[6] = findViewById(R.id.a531);
                cells[7] = findViewById(R.id.a532);
                cells[8] = findViewById(R.id.a533);
                break;

            case 6:
                cells[0] = findViewById(R.id.a611);
                cells[1] = findViewById(R.id.a612);
                cells[2] = findViewById(R.id.a613);
                cells[3] = findViewById(R.id.a621);
                cells[4] = findViewById(R.id.a622);
                cells[5] = findViewById(R.id.a623);
                cells[6] = findViewById(R.id.a631);
                cells[7] = findViewById(R.id.a632);
                cells[8] = findViewById(R.id.a633);
                break;

            case 7:
                cells[0] = findViewById(R.id.a711);
                cells[1] = findViewById(R.id.a712);
                cells[2] = findViewById(R.id.a713);
                cells[3] = findViewById(R.id.a721);
                cells[4] = findViewById(R.id.a722);
                cells[5] = findViewById(R.id.a723);
                cells[6] = findViewById(R.id.a731);
                cells[7] = findViewById(R.id.a732);
                cells[8] = findViewById(R.id.a733);
                break;

            case 8:
                cells[0] = findViewById(R.id.a811);
                cells[1] = findViewById(R.id.a812);
                cells[2] = findViewById(R.id.a813);
                cells[3] = findViewById(R.id.a821);
                cells[4] = findViewById(R.id.a822);
                cells[5] = findViewById(R.id.a823);
                cells[6] = findViewById(R.id.a831);
                cells[7] = findViewById(R.id.a832);
                cells[8] = findViewById(R.id.a833);
                break;

            case 9:
                cells[0] = findViewById(R.id.a911);
                cells[1] = findViewById(R.id.a912);
                cells[2] = findViewById(R.id.a913);
                cells[3] = findViewById(R.id.a921);
                cells[4] = findViewById(R.id.a922);
                cells[5] = findViewById(R.id.a923);
                cells[6] = findViewById(R.id.a931);
                cells[7] = findViewById(R.id.a932);
                cells[8] = findViewById(R.id.a933);

                break;
        }

        return cells;
    }


    /**
     * Checks whether the given TextView is allowed to be clicked.
     * A TextView shall not be clickable if either:
     * - the player did not click within the field his opponent forced him to pick and he is not
     *   allowed to choose freely (there is still free space in the forced field)
     * - the game is over (prevents fast clickers in classic mode from placing more signs)
     * - the field is already blocked (in ultimate mode only)
     * - the TextView is already filled with a player's sign
     * @param click the clicked TextView
     * @return whether it is allowed to click the clicked TextView or not
     */
    private boolean isClickable(TextView click) {

        return (currentField == nextField || nextField == -1)
                && !blockedFields[getFieldOfCell(click)-1]
                && !gameOver
                && click.getText().length() == 0;

    }



    /**
     * In classic mode: Switch the fields before start of the game to have a classic field.
     * In ultimate mode: Switch finished fields and mark them as won by the current player.
     *
     * @param field to be switched (1-9)
     */
    private void switchField(int field){

        String currentSign = String.valueOf(currentPlayerSign);

        //There won't be a problem as long as field is an allowed integer!
        ViewSwitcher toBeSwitched = (ViewSwitcher) Objects.requireNonNull(findFieldByNumber(field)).getChildAt(0);

        //animate the switching of the field in ultimate mode
        if(!classicMode){
            new FlipHorizontalAnimation(toBeSwitched).setDuration(400).setDegrees(180).animate();
            toBeSwitched.showNext();
        }
        else{
            toBeSwitched.showNext();
        }


        //used to mark finished field in ultimate mode
        if(!classicMode){
            TextView replace = (TextView) toBeSwitched.getChildAt(1);
            replace.setText(currentSign);
            replace.setBackgroundColor(currentColor);

            //lighten up the background (50% visible)
            replace.getBackground().setAlpha(128);
        }

    }



    /**
     * Switch current and other player by switching the names, colors an signs of the current and the other player.
     * Mark whether the current player started the game in "currentPlayerStarted".
     */
    private void swapPlayers() {
        //Switch names
        String nameTmp = currentPlayer;
        currentPlayer = otherPlayer;
        otherPlayer = nameTmp;

        //Switch colors
        int colorTmp = currentColor;
        currentColor = otherColor;
        otherColor = colorTmp;

        //Switch signs
        char signTmp = currentPlayerSign;
        currentPlayerSign = otherPlayerSign;
        otherPlayerSign = signTmp;


        //Invert currentPlayerStarted
        currentPlayerStarted=!currentPlayerStarted;
    }




    //... FOR ULTIMATE ONLY:

    /**
     * Get a big field of the game field by its number
     * @param searchedField the number of the searched field (1-9)
     * @return the field of the inserted number
     */
    private FrameLayout findFieldByNumber(int searchedField) {

        switch (searchedField) {
            case 1:
                return findViewById(R.id.f1);

            case 2:
                return findViewById(R.id.f2);

            case 3:
                return findViewById(R.id.f3);

            case 4:
                return findViewById(R.id.f4);

            case 5:
                return findViewById(R.id.f5);

            case 6:
                return findViewById(R.id.f6);

            case 7:
                return findViewById(R.id.f7);

            case 8:
                return findViewById(R.id.f8);

            case 9:
                return findViewById(R.id.f9);

            default:
                return null;
        }
    }



    /**
     * Get the index (1-9) of the clicked cell.
     * @param clicked a TextView within a field that was clicked in this turn
     * @return the index of the clicked TextView
     */
    private int getIndexOfCell(TextView clicked) {
        //calculate position of cell
        return (clicked.getContentDescription().charAt(1)-48-1)*3+(clicked.getContentDescription().charAt(2)-48);

    }



    /**
     * Limit the possible inputs for next turn on one big field depending on the current players turn.
     * @param field is the index of the clicked field (1-9) within a big field
     */
    private void limitClickability(int field) {
        nextField = !checkFieldBlocked(field) ? field : -1;

        //mark next field if needed (nextField is not the whole field)
        FrameLayout toBeMarked = findFieldByNumber(nextField);
        if(toBeMarked != null){
            toBeMarked.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            new BounceAnimation(toBeMarked).setNumOfBounces(1).setBounceDistance(5).setDuration(400).animate();
        }

    }



    /**
     * Remove the frame around a field made to demonstrate the limited clickability.
     *
     * @param field that loses its frame
     */
    private void removeFrame(int field){
        FrameLayout resetColor = findFieldByNumber(field);

        if(resetColor != null){
            resetColor.setBackgroundColor(getResources().getColor(R.color.background_color));
        }
    }



    /**
     * Mark the given field as blocked by changing its background color, its opacity and shake it once.
     *
     * @param fieldToBeBlocked the field (1-9) that is supposed to be marked as blocked
     */
    private void setFieldBlocked(int fieldToBeBlocked) {

        //block the field
        blockedFields[fieldToBeBlocked - 1] = true;
        View v = findFieldByNumber(fieldToBeBlocked);
        new ShakeAnimation(v).setDuration(200).setNumOfShakes(1).animate();

        //No problems as long as the field is an integer between 1 and 9
        ViewSwitcher vs = (ViewSwitcher) Objects.requireNonNull(findFieldByNumber(fieldToBeBlocked)).getChildAt(0);
        TableLayout field = (TableLayout) vs.getChildAt(0);

        //change background color and opacity
        field.setBackgroundColor(getResources().getColor(R.color.blocked_field_background));
        field.setAlpha(0.5f);
    }





    //... FOR AI:

    /**
     * Checks whether there is a "important" cell in the current field that could be picked. If so,
     * return the most important one.
     *
     * The priority:
     *
     * Classic mode (0):
     * 1. a cell to win the whole game
     * 2. a cell the opponent could win with
     *
     *
     * Ultimate mode (whole field) (-1):
     * 1. a cell to win the whole game
     * 2. a cell to win a field the opponent could win with
     * 3. a cell the opponent could win with
     * 4. a cell to win a field
     * 5. a cell the opponent could win a field with
     *
     *
     * Ultimate mode (small field) (1 - 9):
     * 1. a cell to win the field without the opponent getting the next field
     * 2. a cell to win the field without the opponent winning next turn
     * 3. a cell the opponent could win the field with, without winning a field next turn
     * 4. a cell the opponent could win the field with, without winning next turn
     *
     *
     * @param fieldToSearch the index of the field to search within
     * @return the most important cell, null if nothing was found
     */
    private TextView findMostImportantCell(int fieldToSearch){

        TextView mostImportantCell = null;

        //classic mode:
        if (fieldToSearch == 0){

            //a cell to win the game
            if(findCellsToWinField(0).size() > 0){
                return findCellsToWinField(0).get(0);
            }
            else{
                //a cell the opponent could win with
                swapPlayers();
                if(findCellsToWinField(0).size() > 0) {
                    mostImportantCell = findCellsToWinField(0).get(0);
                    swapPlayers();
                    return mostImportantCell;
                }
                swapPlayers();
            }
        }



        //ultimate mode:
        else {

            //whole field possible:
            if (fieldToSearch == -1) {

                //find a cell to win (1.)
                if (findCellsToWinField(-1).size() > 0){
                    return findCellsToWinField(-1).get(0);
                }


                //find a cell the opponent could win with
                swapPlayers();
                if (findCellsToWinField(-1).size() > 0) {
                    mostImportantCell = findCellsToWinField(-1).get(0);

                    swapPlayers();

                    //find a cell to win the field the opponent could have won with (2.)
                    if (findCellsToWinField(getFieldOfCell(mostImportantCell)).size() > 0){
                        return findCellsToWinField(getFieldOfCell(mostImportantCell)).get(0);

                    }

                    //prevent the player from winning (3.)
                    return mostImportantCell;
                }

                swapPlayers();


                //find a cell to win a field (4.)
                for (int i = 0; i < 9; i++){
                    if (findCellsToWinField(i + 1).size() > 0){

                        //Found one
                        return findCellsToWinField(i + 1).get(0);
                    }
                }


                //find a cell the opponent could win a field with (5.)
                swapPlayers();
                for (int i = 0; i < 9; i++){

                    //found one
                    if (findCellsToWinField(i + 1).size() > 0){
                        mostImportantCell = findCellsToWinField(i + 1).get(0);
                        swapPlayers();
                        return mostImportantCell;
                    }
                }
                swapPlayers();


                //there is no important cell -> a random cell will be picked
            }



            //limited to a smaller field in ultimate mode:
            else{
                //necessary before searching for cells (isClickable will be called)
                currentField = fieldToSearch;


                //find the cells to win the current field
                for (TextView cell : findCellsToWinField(fieldToSearch)){

                    //TODO pimp the (hard) AI: remember the cell that would be picked when thinking about the next turn (e.g 922 is taken then 933 shouldn't be picked to prevent player from taking 911)
                    //TODO (maybe mark the current field as blocked if the possible turn would finish it)


                    //look for cells preventing the opponent from winning a field next turn
                    //while winning the current field
                    swapPlayers();
                    if(findCellsToWinField(getFieldOfCell(cell)).size() > 0){

                        markedCellsByCurrent[getFieldOfCell(cell)-1] = true;

                        //if the opponent gets the next field, make sure he at least does not win right away (2.)
                        //(ignore the possibility to win in this case)
                        if (!checkWinCondition(-1)){
                            mostImportantCell = cell;
                        }
                        swapPlayers();
                    }

                    //find a cell to win the current field with a cell that does not allow the opponent
                    //to get the next field (1.)
                    else{
                        swapPlayers();
                        return cell;
                    }
                }

                //give opponent the chance to win the next field but not the whole game while winning the current field (2.)
                if (mostImportantCell != null){
                    return mostImportantCell;
                }



                //find the cells the opponent needs to win the current field
                swapPlayers();
                for (TextView cell : findCellsToWinField(fieldToSearch)){

                    //look for cells preventing the opponent from winning a field next turn
                    if(findCellsToWinField(getFieldOfCell(cell)).size() > 0){

                        markedCellsByCurrent[getFieldOfCell(cell)-1] = true;

                        //if the opponent gets the next field, make sure he at least does not win right away (4.)
                        if (!checkWinCondition(-1)){
                            mostImportantCell = cell;
                        }
                    }

                    //find a cell to prevent the player from winning the next field as well as the current one (3.)
                    else{
                        swapPlayers();
                        return cell;
                    }
                }
                swapPlayers();

                //when preventing the opponent from winning the current field make sure he does not win next turn (4.)
                if (mostImportantCell != null){
                    return mostImportantCell;
                }
            }
        }

        //no important cell was found
        return null;
    }



    /**
     * Look for all cells in the given field the current player could win with.
     *
     * @param fieldToSearch the field where the AI looks for the cells (-1, 0 or 1 - 9)
     * @return a list of the searched cells in this field if existing, an empty list otherwise
     */
    private List<TextView> findCellsToWinField (int fieldToSearch){

        List<TextView> cellsToWin = new LinkedList<>();

        //the whole field in ultimate mode:
        if(fieldToSearch == -1){

            //check for all cells to win the whole game
            for (int i = 0; i < 9; i++){
                if (!blockedFields[i]) {
                    if (findCellsToWinField(i + 1).size() > 0) {
                        markedCellsByCurrent[i] = true;

                        if (checkWinCondition(-1)) {
                            cellsToWin.add(findCellsToWinField(i + 1).get(0));
                        }
                    }
                }
            }
        }


        //the classic field or a small field in ultimate mode
        else {

            //get the cells of the required field (0 = whole field in classic mode)
            TextView[] possibleCells = getRelatedCells(fieldToSearch);


            //look for the first cell to win the current field
            for (int i = 0; i < 9; i++) {
                if (isClickable(possibleCells[i])) {
                    markedCellsByCurrent[i] = true;
                    if (checkWinCondition(fieldToSearch)) {
                        cellsToWin.add(possibleCells[i]);
                    }
                }
            }
        }
        return cellsToWin;
    }



    /**
     * Before picking a complete random cell from the given field, check whether there are cells
     * that should be excluded because it would be a bad decision to pick them.
     *
     * 0. First, every not clickable cell has to be excluded. This is the only necessary step in classic mode.
     *
     * 1. The worst decision would be picking a cell that leads to the human player being able to pick
     * the next cell from the whole field. That's why we eliminate them firstly. Continue with the remaining cells.
     *
     * 2. Next eliminate all cells that lead to a field the human player could win the whole game with next turn.
     * Continue with the remaining cells.
     *
     * 3. Now eliminate all cells that lead to a field the human player could win next turn.
     *
     * 4. From the remaining cells eliminate all cells that lead to the human player being able to place his sign
     * in a field that the AI could win the game with (with just one turn).
     *
     * 5. From the remaining cells eliminate all cells that lead to the human player being able to prevent
     * the AI from winning a smaller field.
     *
     * 6. To make it easier eliminate also the cell that leads back to the current field because it could be e.g. a blocked field
     * next turn.
     *
     * The cells that remain after all these filters are equal important and therefore can be returned to be picked
     * randomly afterwards. If one filter filters out all the remaining cells from the filter before, return all
     * cells that fulfilled the filter before. These are not as good for the AI as those who fulfill all filters but
     * at least they are equally important.
     *
     *
     * @param fieldToCheck the integer representing a field (-1 - 9)
     * @return a list of the best possible choices to pick for the AI. They are equally good so the AI can pick randomly from them.
     */
    private List<TextView> filterPossibleCells(int fieldToCheck){
        List<TextView> possibleCells = new LinkedList<>();

        //necessary before calling isClickable
        currentField = fieldToCheck;

        //check which cells can be possibly picked
        for (TextView v : getRelatedCells(fieldToCheck)){
            if (isClickable(v)){
                possibleCells.add(v);
            }
        }


        //in classic mode every clickable cell is not bad (everything important was already checked in findMostImportantCell)
        //in easy mode the AI should not filter the possible cells at all in 40% of the cases
        if (classicMode || (easyMode && new Random().nextDouble() >= 0.6)){
            return possibleCells;
        }


        //1. Eliminate the cells leading to a blocked field.
        List<TextView> bestCells = new LinkedList<>();
        for (TextView v : possibleCells){
            if (!blockedFields[getIndexOfCell(v) - 1]){
                bestCells.add(v);
            }
        }

        //return all cells if all lead to a blocked field
        if (bestCells.isEmpty()){
            return possibleCells;
        }

        //bestCells can immediately be returned if there is only one cell that does not lead to a blocked field
        else if (bestCells.size() == 1){
            return bestCells;
        }


        //there are multiple cells that don't lead to a blocked field
        else{

            //necessary to be able to think about the cells of other fields
            nextField = -1;


            //2. Eliminate the cells that allow the human player to win next turn.
            possibleCells = bestCells;
            bestCells = new LinkedList<>();

            swapPlayers();
            for(TextView v : possibleCells){
                if (!findCellsToWinField(-1).contains(findMostImportantCell(getIndexOfCell(v)))){
                    bestCells.add(v);
                }
            }
            swapPlayers();

            //all cells (not leading to blocked fields) lead to the opponent being able to win next turn
            if (bestCells.isEmpty()){
                return possibleCells;
            }

            //bestCells can immediately be returned if there is only one cell that does not lead to a
            //blocked field and does not allow the player to win next turn
            else if (bestCells.size() == 1){
                return bestCells;
            }



            //multiple cells don't allow the opponent to choose freely next turn or win instantly
            else{

                //3. Eliminate the cells that allow the player to win the next field.
                possibleCells = bestCells;
                bestCells = new LinkedList<>();

                swapPlayers();
                for (TextView v : possibleCells){
                    if (findCellsToWinField(getIndexOfCell(v)).size() == 0){
                        bestCells.add(v);
                    }
                }
                swapPlayers();


                //all cells don't fulfill this filter, return all that fulfilled the last one
                if (bestCells.isEmpty()){
                    return possibleCells;
                }

                //only one cell fulfilled this condition
                else if (bestCells.size() == 1){
                    return bestCells;
                }


                //multiple cells don't allow the opponent to win a field next turn
                else{

                    //4. Eliminate the cells that allow the player to prevent the AI winning the game.
                    possibleCells = bestCells;
                    bestCells = new LinkedList<>();

                    for(TextView v : possibleCells){
                        if (!findCellsToWinField(-1).contains(findMostImportantCell(getIndexOfCell(v)))){
                            bestCells.add(v);
                        }
                    }

                    //all cells don't fulfill this filter, return all that fulfilled the last one
                    if (bestCells.isEmpty()){
                        return possibleCells;
                    }

                    //only one cell fulfilled this condition
                    else if (bestCells.size() == 1){
                        return bestCells;
                    }


                    //multiple cells don't allow the player to get a field for himself nor preventing the AI from winning next turn
                    else{

                        //5. Eliminate the cells that allow the player to prevent the AI from winning a field.
                        possibleCells = bestCells;
                        bestCells = new LinkedList<>();

                        for (TextView v : possibleCells){

                            if (findCellsToWinField(getIndexOfCell(v)).size() == 0){
                                bestCells.add(v);
                            }
                        }

                        //all cells don't fulfill the last filter, return all that fulfilled the last one
                        if (bestCells.isEmpty()){
                            return possibleCells;
                        }

                        //only one cell fulfills the first 5 conditions
                        else if (bestCells.size() == 1){
                            return bestCells;
                        }

                        //multiple cells fulfill condition 1-5
                        else{

                            //6. Eliminate the cell leading back to the current field if still existing.
                            possibleCells = bestCells;
                            bestCells = new LinkedList<>();

                            for (TextView v : possibleCells){
                                if(getIndexOfCell(v) != fieldToCheck){
                                    bestCells.add(v);
                                }
                            }

                            //the cell that fulfill all filters are returned
                            return bestCells;
                        }
                    }
                }
            }
        }
    }



    /**
     * Picks a random cell for the AI from the given clickable possible cells.
     *
     * @return a randomly chosen clickable TextView for the AI
     * @param possibleCells cells the AI could pick (classic mode: all clickable fields,
     *                     ultimate mode: the cells returned from filterBadCells)
     */
    private TextView pickRandomEmptyCellFrom(List<TextView> possibleCells){

        return possibleCells.get(Math.abs(new Random().nextInt() % possibleCells.size()));
    }



    /**
     * A method for ultimate mode only that returns the field index of a given TextView.
     * which are only the bigger fields.
     * @param cell a cell within a small field in ultimate mode
     * @return the field index (1-9)
     */
    private int getFieldOfCell(TextView cell) {
        return cell.getContentDescription().charAt(0)-48;
    }


    /**
     * Checks whether field is completely blocked.
     *
     * @param field is either -1 (the complete classic field), 0 (the complete ultimate field)
     *             or between 1 and 9 (smaller field in ultimate mode)
     * @return whether field is blocked or not
     */
    private boolean checkFieldBlocked(int field) {

        //checks whole field in classic mode
        if(field == 0){
            for(TextView tv : getRelatedCells(0)){
                if(isClickable(tv)){
                    return false;
                }
            }
            return true;
        }

        //checks whole field in ultimate mode
        else if(field == -1){
            for (int i = 0; i < 9; i++){
                if(!blockedFields[i]){
                    return false;
                }
            }
            return true;
        }

        //checks smaller field in ultimate mode
        else{
            if(!blockedFields[field - 1]) {
                for (TextView tv : getRelatedCells(field)) {
                    if (isClickable(tv)) {
                        return false;
                    }
                }
                blockedFields[field - 1] = true;
            }
            return true;
        }
    }
}
