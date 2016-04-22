package com.bc.ting;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * Created by An on 2016/4/21.
 */

public class TingActivity extends Activity {

    private String urlMusic = "http://yinyueshiting.baidu.com/data2/music/124732357/7317702194400128.mp3?xcode=f3ef569b13cda7c931189f16455400eb";

    private TextView txt_start_time;
    private TextView txt_over_time;
    private SeekBar sb;
    private TextView txt_play;

    private MyBroadcastReceiver mainBroadcastReceiver;
    private IntentFilter intentFilter;

    private int totalProgress;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 2002) {
                sendBroadcastToService(TingConstants.ACTION_PLAY, null);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ting);

        intentFilter = new IntentFilter();
        intentFilter.addAction(TingConstants.ACTION_CURRENT_PROGRESS);

        mainBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(mainBroadcastReceiver, intentFilter);


        initView();
    }

    private void initView() {

        sb = (SeekBar) this.findViewById(R.id.sb);

        txt_start_time = (TextView) this.findViewById(R.id.txt_start_time);
        txt_over_time = (TextView) this.findViewById(R.id.txt_over_time);

        txt_play = (TextView) this.findViewById(R.id.txt_play_or_stop);
        txt_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String va = txt_play.getText().toString();

                if (va.equals("暂停")) {
                    sendBroadcastToService(TingConstants.ACTION_PAUSE, null);
                    txt_play.setText("播放");
                } else if (va.equals("播放")) {
                    sendBroadcastToService(TingConstants.ACTION_PLAY, null);
                    txt_play.setText("暂停");
                }


            }
        });

        startService();

        handler.sendEmptyMessageDelayed(2002, 3000);


    }


    private void startService() {
        Intent intent = new Intent(this, TingService.class);
        intent.putExtra("url", urlMusic);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    private void sendBroadcastToService(String action, String data) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("data", data);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcastToService(TingConstants.ACTION_STOP, null);

    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (TingConstants.ACTION_CURRENT_PROGRESS.equals(intent.getAction())) {

                int progress = intent.getIntExtra("progress", 0);
                txt_start_time.setText(timeconvert(progress));
                if (totalProgress != 0)
                    sb.setProgress((int) (progress * 100 / totalProgress));

            } else if (TingConstants.ACTION_CURRENT_STOP.equals(intent.getAction())) {
                unregisterReceiver(mainBroadcastReceiver);
            } else if (TingConstants.ACTION_TOTAL_PROGRESS.equals(intent.getAction())) {
                totalProgress = intent.getIntExtra("total", 0);

                txt_over_time.setText(timeconvert(totalProgress));

            }

        }
    }


    public String timeconvert(int time) {
        int min = 0, hour = 0;
        time /= 1000;
        min = time / 60;
        time %= 60;
        return min + ":" + time;
    }
}
