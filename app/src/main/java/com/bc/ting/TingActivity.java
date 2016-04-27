package com.bc.ting;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;


/**
 * Created by An on 2016/4/21.
 */

public class TingActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    private String[] urlMusic = {"http://yinyueshiting.baidu.com/data2/music/124732357/7317702194400128.mp3?xcode=f3ef569b13cda7c931189f16455400eb"
            , "http://yinyueshiting.baidu.com/data2/music/64361431/58704156129600128.mp3?xcode=3c631d4d3884ca05dcd0b1435228577d"};

    private TextView txt_start_time;
    private TextView txt_over_time;
    private SeekBar sb;
    private TextView txt_play;
    private TextView txt_timing;

    private MyBroadcastReceiver mainBroadcastReceiver;
    private IntentFilter intentFilter;

    /**
     * 当前时间
     */
    private long nowTime;


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
        intentFilter.addAction(TingConstants.ACTION_TOTAL_PROGRESS);
        intentFilter.addAction(TingConstants.ACTION_SET_TEXT);

        mainBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(mainBroadcastReceiver, intentFilter);


        initView();
    }

    private void initView() {

        sb = (SeekBar) this.findViewById(R.id.sb);
        sb.setOnSeekBarChangeListener(this);

        txt_start_time = (TextView) this.findViewById(R.id.txt_start_time);
        txt_over_time = (TextView) this.findViewById(R.id.txt_over_time);

        txt_over_time.setText(timeconvert(ShareUtil.getShareToInt(TingActivity.this, "totalProgress")));
        sb.setMax(ShareUtil.getShareToInt(TingActivity.this, "totalProgress"));

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

        this.findViewById(R.id.txt_next_music).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        sendBroadcastToService(TingConstants.ACTION_NEST_MUSIC, "");

                    }
                });

        this.findViewById(R.id.txt_last_music).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendBroadcastToService(TingConstants.ACTION_LAST_MUSIC, "");
                    }
                });


        txt_timing = (TextView) this.findViewById(R.id.txt_timing);
        txt_timing.setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingClockDialog();
                    }
                });


        String vas = ShareUtil.getShare(this, "time_off");
        txt_timing.setText(vas == "" ? "定时关闭" : vas);
        if ("定时关闭".equals(txt_timing.getText().toString())) {
            txt_timing.setEnabled(true);
        } else {
            txt_timing.setEnabled(false);
        }


        startService();

        handler.sendEmptyMessageDelayed(2002, 100);


    }


    private void startService() {
        Intent intent = new Intent(this, TingService.class);
        intent.putExtra("url", urlMusic);
        startService(intent);
    }

    private void stopService() {
        Intent intent = new Intent(this, TingService.class);
        stopService(intent);
    }


    private void sendBroadcastToService(String action, String data) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("data", data);
        sendBroadcast(intent);
    }


    /**
     * 设置闹钟对话框
     */
    private void settingClockDialog() {

        View view = getLayoutInflater().inflate(R.layout.settingtime, null);
        final EditText edt = (EditText) view.findViewById(R.id.edt_time);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                TingActivity.this);
        builder.setView(view);
        builder.setPositiveButton(R.string.con_firm, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                txt_timing.setText(edt.getText().toString() + "min");
                txt_timing.setEnabled(false);

                ShareUtil.setShare(TingActivity.this, edt.getText().toString() + "min", "time_off");

                setClock(edt.getText().toString());

            }
        });// 确定
        builder.setNegativeButton(R.string.can_cel, null);// 取消

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcastToService(TingConstants.ACTION_STOP, null);
        unregisterReceiver(mainBroadcastReceiver);


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e("-----------", "手动进度:" + seekBar.getProgress());
        sendBroadcastToService(TingConstants.ACTION_SD_PROGRESS, seekBar.getProgress() + "");

    }


    private AlarmManager alarmManager;


    private void setClock(String t) {

        Intent timeIntent = new Intent();
        timeIntent.setAction(TingConstants.ACTION_TIME);
        timeIntent.putExtra("time", "即将关闭!");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, timeIntent, 0);

        long times = System.currentTimeMillis() + Integer.parseInt(t) * 60 * 1000;

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, times, pendingIntent);


    }


    private class MyBroadcastReceiver extends BroadcastReceiver {


        private int totalProgress;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (TingConstants.ACTION_CURRENT_PROGRESS.equals(intent.getAction())) {

                int progress = intent.getIntExtra("progress", 0);
                txt_start_time.setText(timeconvert(progress));

                sb.setProgress(progress);

            } else if (TingConstants.ACTION_TOTAL_PROGRESS.equals(intent.getAction())) {
                totalProgress = intent.getIntExtra("total", 0);

                ShareUtil.setShareToInt(TingActivity.this, totalProgress, "totalProgress");

                // Log.e("------------", "总时间" + totalProgress);

                txt_over_time.setText(timeconvert(totalProgress));
                sb.setMax(totalProgress);

            } else if (TingConstants.ACTION_SERVICE_STOP.equals(intent.getAction())) {
                stopService();
            } else if (TingConstants.ACTION_SET_TEXT.equals(intent.getAction())) {
                txt_play.setText("播放");
                sb.setProgress(0);
                txt_timing.setEnabled(true);
                txt_timing.setText("定时关闭");
                ShareUtil.setShare(TingActivity.this, "定时关闭", "time_off");

            }

        }
    }


    public String timeconvert(int time) {
        int min = 0, hour = 0;
        time /= 1000;
        min = time / 60;
        time %= 60;
        if (min < 10) {
            return time >= 10 ? ("0" + min + ":" + time) : ("0" + min + ":" + "0" + time);

        } else if (min >= 10) {

            return time >= 10 ? (min + ":" + time) : (min + ":" + "0" + time);
        }
        return min + ":" + time;
    }
}
