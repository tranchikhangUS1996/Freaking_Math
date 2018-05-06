package com.tranchikhang.freaking_math;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

public class playActivity extends AppCompatActivity implements Handler.Callback,MessageType,View.OnClickListener {

    private GameManager manager;
    private Handler BackGroundHandler;
    private int Score = 0;
    private boolean Result = false;
    private boolean Ready = false;
    private ProgressBar progressBar;
    private ConstraintLayout constraintLayout;
    private Dialog dialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String[] BackGroundColors = {"#9C27B0","#FF5722","#03A9F4","#009688","#607D8B","#00E676"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Handler mainHandler = new Handler(getMainLooper(),this);
        manager = new GameManager(this,mainHandler,0);
        BackGroundHandler = manager.getHandler();
        progressBar = (ProgressBar) findViewById(R.id.ProTime);
        progressBar.setMax(3000);
        constraintLayout = (ConstraintLayout) findViewById(R.id.Backgroud);
        dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_layout);
        sharedPreferences = getSharedPreferences(getString(R.string.HighScoreKey),MODE_PRIVATE);
        editor = sharedPreferences.edit();
        BackGroundHandler.sendEmptyMessage(STARTGAME);

    }

    @Override
    protected void onResume() {
        super.onResume();
        BackGroundHandler.sendEmptyMessage(CONTINUE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BackGroundHandler.sendEmptyMessage(PAUSE);
    }

    public void onCheck(View view) {
        if(Ready) {
            boolean choose = false;
            if(view.getId()==R.id.True) choose = true;
            if(choose==Result) {
                Score++;
                updateScore(Score);
                BackGroundHandler.sendEmptyMessage(NEXTGAME);
            } else {
                BackGroundHandler.sendEmptyMessage(WRONGANSWER);
            }
            Ready = false;
        }
    }

    private void updateScore(int MyScore) {
        TextView tv_score = (TextView) findViewById(R.id.Score);
        tv_score.setText(String.valueOf(MyScore));
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what) {
            case SHOWQUESTION:
                showQuestion((GameManager.QuesTion)msg.obj);
                break;
            case UPDATETIME:
                updateTime(msg.arg1);
                break;
            case ENDGAME:
                Ready = false;
                endGame();
                break;
        }
        return false;
    }

    private void endGame() {
        // update diem, show diem
        int record = sharedPreferences.getInt(getString(R.string.HighScoreKey),0);
        if(Score>record) { // update best
            editor.putInt(getString(R.string.HighScoreKey),Score);
            editor.commit();
        }
        showScore(Score,record);
    }

    private void showScore(int score, int record) {
        TextView tv_new_score = (TextView) dialog.findViewById(R.id.new_score);
        TextView tv_record = (TextView) dialog.findViewById(R.id.best_score);
        TextView tv_best = (TextView) dialog.findViewById(R.id.best);
        tv_new_score.setText(String.valueOf(score));
        if(score > record) {
            tv_best.setText("New Best");
            tv_record.setText(String.valueOf(score));
        }
        else{
            tv_best.setText(R.string.best);
            tv_record.setText(String.valueOf(record));
        }
        ImageView imv_startAgain = (ImageView) dialog.findViewById(R.id.play_again);
        imv_startAgain.setOnClickListener(this);
        ImageView imv_home = (ImageView) dialog.findViewById(R.id.backhome);
        imv_home.setOnClickListener(this);
        dialog.show();
    }

    private void updateTime(int time) {
        progressBar.setProgress(time);
    }

    public void showQuestion(GameManager.QuesTion obj) {
        setBackGround();
        Result = obj.result;
        TextView tv_question = (TextView) findViewById(R.id.PhepTinh);
        tv_question.setText(obj.question);
        Ready = true;
    }

    private void setBackGround() {
        Random random = new Random();
        int index = random.nextInt(BackGroundColors.length);
        constraintLayout.setBackgroundColor(Color.parseColor(BackGroundColors[index]));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.quick();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.backhome) {
            dialog.dismiss();
            finish();
        } else {
            dialog.dismiss();
            Score = 0;
            updateScore(Score);
            Ready = false;
            BackGroundHandler.sendEmptyMessage(STARTGAME);
        }
    }
}
