package com.tranchikhang.freaking_math;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.sql.Time;
import java.util.Random;

public class GameManager implements Handler.Callback, MessageType {

    private final int MYTIME = 100 ;

    class QuesTion{
        public String question;
        public boolean result;
        public QuesTion(String q, int r) {
            this.question = q;
            this.result = (r!=0)?true:false;
        }
    }

    private Handler MainHandler;
    private Context context;
    private Handler GameHandler;
    private HandlerThread BackGroundThread;
    private int GameLever = 0;
    private final int Easyrange = 100;
    private boolean Running = false;
    private int TimeCountDown = 3000;

    public GameManager(Context context, Handler mainHandler, int lever) {
        this.GameLever = lever;
        this.MainHandler = mainHandler;
        this.context = context;
        BackGroundThread = new HandlerThread("BackGround",Process.THREAD_PRIORITY_BACKGROUND);
        BackGroundThread.start();
        GameHandler = new Handler(BackGroundThread.getLooper(),  this);
    }

    public QuesTion CreateQuestion() {
        String question;
        switch (GameLever) {
            case 0: // easy
                return makeEasyQuestion();
        }
        return null;
    }

    public QuesTion makeEasyQuestion() {
        StringBuilder Q = new StringBuilder();
        Random random = new Random();
        int a = 0;
        int b=0;
        a = random.nextInt(Easyrange);
        b = random.nextInt(Easyrange);
        int result = random.nextInt(2);
        int answer = random.nextInt(2*Easyrange);
        int pt = random.nextInt(2);
        if(pt==0) { // phep cong pt=0
            int tempanswer = a+b;
            if(result!=0) { // kq dung gan dap an bang ket qua chinh xac
                answer = tempanswer;
            } else {
                while(answer==tempanswer) { // random cho den khi sai thi dung
                    answer = random.nextInt(2*Easyrange);
                }
            }
        } else { // phep tru
            while(a-b < 0) { //  dam bao ket qua luon duong
                a = random.nextInt(Easyrange);
                b = random.nextInt(Easyrange);
            }
            int tempanswer = a-b;
            if(result!=0) { // kq dung gan dap an bang ket qua chinh xac
                answer = tempanswer;
            } else {
                while(answer==tempanswer) { // random cho den khi sai thi dung
                    answer = random.nextInt(2*Easyrange);
                }
            }
        }
        Q.append(a);
        if(0==pt)
        Q.append(" + ");
        else Q.append(" - ");
        Q.append(b);
        Q.append(" = ");
        Q.append(answer);
        QuesTion ret = new QuesTion(Q.toString(),result);
        return ret;
    }

    public Handler getHandler() {
        return this.GameHandler;
    }

    private void nextGame() {
        GameHandler.removeMessages(COUNTDOWN);
        Running = true;
        TimeCountDown = 3000;
        QuesTion Q = makeEasyQuestion();
        Message msg = MainHandler.obtainMessage(SHOWQUESTION);
        msg.obj = Q;
        MainHandler.sendMessage(msg);
        countDown();
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what) {
            case STARTGAME:
                nextGame();
                break;
            case WRONGANSWER:
                wrongAnswer();
                break;
            case COUNTDOWN:
                countDown();
                break;
            case NEXTGAME:
                nextGame();
                break;
            case CONTINUE:
                if(!Running&&TimeCountDown>0) {
                    Running = true;
                    countDown();
                }
                break;
            case PAUSE:
                if(Running) {
                    GameHandler.removeMessages(COUNTDOWN);
                    Running = false;
                }
                break;
        }
        return false;
    }

    private void countDown() {
        if (TimeCountDown>0&&Running){
            TimeCountDown-=MYTIME;
            // update time bar
            Message msg = MainHandler.obtainMessage(UPDATETIME);
            msg.arg1 = TimeCountDown;
            MainHandler.sendMessage(msg);
            GameHandler.sendEmptyMessageDelayed(COUNTDOWN,MYTIME);
        } else if(Running) { // het gio
            endGame();
        }
    }

    private void wrongAnswer() {
        endGame();
    }

    private void endGame() {
        TimeCountDown = 0;
        GameHandler.removeMessages(COUNTDOWN);
        MainHandler.sendEmptyMessage(ENDGAME);
        Running = false;
    }
    public void quick() {
        BackGroundThread.quit();
        Log.d("Check","quick thread");
    }
}
