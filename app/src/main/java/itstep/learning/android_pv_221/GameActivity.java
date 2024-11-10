package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class GameActivity extends AppCompatActivity {
   private final String bestScoreFilename = "best_score.2048";
    private final int N = 4;
    private int[][] cells = new int[N][N];
    private int[][] undo;
    private int prevScore;
    private TextView[][] tvCells = new TextView[N][N];
    private final Random random = new Random();
    private Animation spawnAnimation, collapseAnimation, bestScoreAnimation;
    private int score, bestScore;
    private TextView tvScore, tvBestScore;
    private boolean playOn = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spawnAnimation = AnimationUtils.loadAnimation( this, R.anim.game_spawn ) ;
        collapseAnimation = AnimationUtils.loadAnimation( this, R.anim.game_collapse ) ;
        bestScoreAnimation = AnimationUtils.loadAnimation( this, R.anim.best_score ) ;

        LinearLayout gameField = findViewById( R.id.game_ll_field );
        tvScore = findViewById(R.id.game_tv_score);
        tvBestScore = findViewById(R.id.game_tv_best_score);

        findViewById(R.id.game_btn_new).setOnClickListener( v -> onNewGameClick() );
        findViewById(R.id.game_btn_undo).setOnClickListener(v -> undoMove() );

        gameField.post( () -> {
            int vw = this.getWindow().getDecorView().getWidth();
            int fieldMargin = 20;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    vw - 2 * fieldMargin,
                    vw - 2 * fieldMargin
            );
            layoutParams.setMargins( fieldMargin, fieldMargin, fieldMargin, fieldMargin );
            layoutParams.gravity = Gravity.CENTER;
            gameField.setLayoutParams( layoutParams );
        } );

        gameField.setOnTouchListener( new OnSwipeListener(GameActivity.this ){

            @Override
            public void OnSwipeBottom() {
                if( canMoveBottom() ){
                    saveField();
                    moveBottom();
                    spawnCell();
                    showField();
                }
                else{
                    Toast.makeText(GameActivity.this, "No Bottom Move", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void OnSwipeLeft() {
               if( canMoveLeft() ){
                   saveField();
                   moveLeft();
                   spawnCell();
                   showField();
               }
               else{
                   Toast.makeText(GameActivity.this, "No left Move", Toast.LENGTH_SHORT).show();
               }
            }

            @Override
            public void OnSwipeRight() {
                if( canMoveRight() ) {
                    saveField();
                    moveRight();
                    spawnCell();
                    showField();
                }
                else {
                    Toast.makeText(GameActivity.this, "No Right Move", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void OnSwipeTop() {
                if( canMoveTop() ) {
                    saveField();
                    moveTop();
                    spawnCell();
                    showField();
                }
                else {
                    Toast.makeText(GameActivity.this, "No Top Move", Toast.LENGTH_SHORT).show();
                }
            }
        });

        initField();
        spawnCell();
        showField();
    }

    private void checkWin() {
        if (playOn){
            return;
        }
        boolean found2048 = false;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (cells[i][j] == 2048) {
                    found2048 = true;
                    break;
                }
            }
            if (found2048) break;
        }
        if (found2048) {
            showWinDialog();
        }
    }

    private void showWinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поздравляем!")
                .setMessage("Вы собрали 2048! Хотите начать новую игру или продолжить?")
                .setPositiveButton("Новая игра", (dialog, which) -> onNewGameClick())
                .setNegativeButton("Продолжить", (dialog, which) -> {
                    playOn = true;
                    dialog.dismiss();
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveField() {
        prevScore = score;
        undo = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i], 0, undo[i], 0, N);
        }

    }

    private void undoMove(){
        if(undo == null){
            showMessage();
            return;
        }
        score = prevScore;
        for (int i = 0; i < N; i++) {
            System.arraycopy( undo[i], 0, cells[i], 0, N);
        }
        undo = null;
        showField();

    }

    private void showMessage(){
        new AlertDialog
                .Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle("Warning")
                .setIcon( android.R.drawable.ic_dialog_alert )
                .setMessage("It is impossible to undo the move")
                .setNeutralButton("Close", (dlg, btn) ->{})
                .setPositiveButton("Subscription", (dlg, btn) -> Toast.makeText(this, "Soon...", Toast.LENGTH_SHORT ).show() )
                .setNegativeButton("Exit", (dlg, btn) -> finish() )
                .setCancelable(false)
                .show();

    }

    private boolean canMoveLeft(){
        for (int i = 0; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if ( cells[i][j] != 0  && (cells[i][j-1] == 0 || cells[i][j-1] == cells[i][j] ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canMoveRight() {
        for (int i = 0; i < N; i++) {
            for (int j = N - 2; j >= 0; j--) { // Начинаем с предпоследней ячейки в строке и идем влево
                if (cells[i][j] != 0 && (cells[i][j + 1] == 0 || cells[i][j + 1] == cells[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canMoveTop() {
        for (int j = 0; j < N; j++) {
            for (int i = 1; i < N; i++) { // Начинаем со второй строки и идем вниз
                if (cells[i][j] != 0 && (cells[i - 1][j] == 0 || cells[i - 1][j] == cells[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canMoveBottom() {
        for (int j = 0; j < N; j++) {
            for (int i = N - 2; i >= 0; i--) { // Начинаем с предпоследней строки и идем вверх
                if (cells[i][j] != 0 && (cells[i + 1][j] == 0 || cells[i + 1][j] == cells[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveLeft(){
        for (int i = 0; i < N; i++) {
            int j0 = -1;
            for (int j = 0; j < N; j++) {
                if(cells[i][j] != 0){
                    if( j0 == -1 ){
                        j0 = j;
                    }
                    else {
                        if( cells[i][j] == cells[i][j0]){
                            cells[i][j] *= 2;
                            score += cells[i][j];
                            tvCells[i][j].setTag( collapseAnimation );
                            cells[i][j0] = 0;
                            j0 = -1;
                        }
                        else{
                            j0 = j;
                        }
                    }
                }
            }
            j0 = -1;
            for (int j = 0; j < N; j++) {
                if(cells[i][j] == 0){
                    if( j0 == -1 ){
                        j0 = j;
                    }
                }
                else if( j0 != -1 ){
                    cells[i][j0] = cells[i][j];
                    tvCells[i][j0].setTag( tvCells[i][j].getTag() );
                    cells[i][j] = 0;
                    tvCells[i][j].setTag( null );
                    j0 += 1;
                }
            }
        }
    }

    private boolean moveRight() {
        boolean result = false;
        for( int i = 0; i < N; i++ ) {
            boolean wasShift;
            do {
                wasShift = false;
                for (int j = N - 1; j > 0; j--) {
                    if (cells[i][j - 1] != 0 && cells[i][j] == 0) {
                        cells[i][j] = cells[i][j - 1];
                        cells[i][j - 1] = 0;
                        wasShift = result = true;
                    }
                }
            } while( wasShift );
            // Collapse
            for( int j = N - 1; j > 0; j-- ) {          //  [2 2 4 4]
                if( cells[i][j - 1] == cells[i][j] && cells[i][j] != 0 ) {
                    cells[i][j] *= 2;                   //  [2 2 4 8]
                    score += cells[i][j];
                    tvCells[i][j].setTag( collapseAnimation );
                    // cells[i][j - 1] = 0;             //  [2 2 0 8]
                    for( int k = j - 1; k > 0; k-- ) {  //  [2 2 2 8]
                        cells[i][k] = cells[i][k - 1];
                    }
                    cells[i][0] = 0;                    //  [0 2 2 8]
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean moveTop() {
        boolean result = false;

        for (int j = 0; j < N; j++) {  // Проходим по каждому столбцу
            int i0 = -1;

            // Объединение ячеек в текущем столбце при движении вверх
            for (int i = 0; i < N; i++) {
                if (cells[i][j] != 0) {
                    if (i0 == -1) {
                        i0 = i;
                    } else {
                        if (cells[i][j] == cells[i0][j]) {
                            cells[i][j] *= 2;
                            score += cells[i][j];
                            tvCells[i][j].setTag(collapseAnimation);
                            cells[i0][j] = 0;
                            result = true;
                            i0 = -1;
                        } else {
                            i0 = i;
                        }
                    }
                }
            }

            i0 = -1;

            // Сдвиг ячеек вверх в текущем столбце
            for (int i = 0; i < N; i++) {
                if (cells[i][j] == 0) {
                    if (i0 == -1) {
                        i0 = i;
                    }
                } else if (i0 != -1) {
                    cells[i0][j] = cells[i][j];
                    tvCells[i0][j].setTag(tvCells[i][j].getTag());
                    cells[i][j] = 0;
                    tvCells[i][j].setTag(null);
                    i0 += 1;
                    result = true;
                }
            }
        }

        return result;
    }

    private boolean moveBottom() {
        boolean result = false;

        for (int j = 0; j < N; j++) {
            boolean wasShift;

            do {
                wasShift = false;
                for (int i = N - 1; i > 0; i--) {
                    if (cells[i - 1][j] != 0 && cells[i][j] == 0) {
                        cells[i][j] = cells[i - 1][j];
                        cells[i - 1][j] = 0;
                        wasShift = result = true;
                    }
                }
            } while (wasShift);

            for (int i = N - 1; i > 0; i--) {
                if (cells[i - 1][j] == cells[i][j] && cells[i][j] != 0) {
                    cells[i][j] *= 2;
                    score += cells[i][j];
                    tvCells[i][j].setTag(collapseAnimation);

                    for (int k = i - 1; k > 0; k--) {
                        cells[k][j] = cells[k - 1][j];
                    }
                    cells[0][j] = 0;
                    result = true;
                }
            }
        }

        return result;
    }

    private void saveBestScore(){
        try( FileOutputStream fos = openFileOutput(bestScoreFilename, Context.MODE_PRIVATE);
            DataOutputStream writer = new DataOutputStream( fos );
        ){
            writer.writeInt( bestScore );
            writer.flush();
        }
        catch (IOException e) {
            Log.e( "GameActivity::saveBestScore",
                    e.getMessage() != null ? e.getMessage() : "Error writing file"
            );
        }
    }

    private void loadBestScore(){
        try(FileInputStream fis = openFileInput(bestScoreFilename );
            DataInputStream reader = new DataInputStream( fis );
        ){
            bestScore = reader.readInt();
        }
        catch (IOException e) {
            Log.e( "GameActivity::loadBestScore",
                    e.getMessage() != null ? e.getMessage() : "Error reading file"
            );
        }
    }

    private void onNewGameClick(){
        cells = new int[N][N];
        tvCells = new TextView[N][N];
        undo = null;
        playOn = false;
        initField();
        spawnCell();
        showField();
    }

    private boolean spawnCell(){
        List<Coordinates> freeCells = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if(cells[i][j] == 0){
                    freeCells.add( new Coordinates( i,j ) );
                }
            }
        }

        if( freeCells.isEmpty() ){
            return false;
        }

        Coordinates randomCoordinates = freeCells.get( random.nextInt( freeCells.size() ) );
        cells[randomCoordinates.x][randomCoordinates.y] =
                random.nextInt(10) == 0 ? 4 : 2;
        tvCells[randomCoordinates.x][randomCoordinates.y].setTag( spawnAnimation );
        return true;
    }

    private void initField(){
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
               // cells[i][j] = (int) Math.pow( 2, i * N + j);
                cells[0][0] = 0;
                tvCells[i][j] = findViewById(
                        getResources().getIdentifier(
                                "game_cell_" + i + j,
                                "id",
                                getPackageName()
                        )
                );
            }
        }
        score = 0;
        loadBestScore();
    }

    private void showField(){
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) );
                tvCells[i][j].getBackground().setColorFilter(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        cells[i][j] <= 2048
                                                ? "game_tile_" + cells[i][j]
                                                : "game_tile_other",
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        ),
                        PorterDuff.Mode.SRC_ATOP);

                tvCells[i][j].setTextColor( getResources().getColor(
                        getResources().getIdentifier(
                                cells[i][j] <= 2048 ? "game_text_" + cells[i][j] : "game_text_other",
                                "color",
                                getPackageName()
                        ),
                        getTheme()
                ) );
                if( tvCells[i][j].getTag() instanceof Animation ) {
                    tvCells[i][j].startAnimation( (Animation) tvCells[i][j].getTag() );
                    tvCells[i][j].setTag( null );
                }
            }
        }
        tvScore.setText( getString(R.string.game_tv_score, String.valueOf( score )));
        if( score > bestScore ){
            bestScore = score;
            saveBestScore();
            tvBestScore.setTag( bestScoreAnimation );
        }
        tvBestScore.setText( getString(R.string.game_tv_best, String.valueOf( bestScore )));
        if(tvBestScore.getTag() instanceof Animation){
            tvBestScore.startAnimation((Animation) tvBestScore.getTag());
            tvBestScore.setTag( null );
        }
        checkWin();
    }

    class Coordinates{
        int x, y;

        public Coordinates(int i, int j) {
            this.x = i;
            this.y = j;
        }


    }
}