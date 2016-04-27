package com.bc.ting;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class TingService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {


    private MediaPlayer player;
    private String[] urlMusic;

    private int position;

    private boolean isFirstPlays;
    private boolean isOver;

    private MyBroadcastReceiver mbr;
    private IntentFilter intentFilter;

    private int totalTime;


    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter = new IntentFilter();
        intentFilter.addAction(TingConstants.ACTION_PLAY);
        intentFilter.addAction(TingConstants.ACTION_PAUSE);
        intentFilter.addAction(TingConstants.ACTION_STOP);
        intentFilter.addAction(TingConstants.ACTION_SD_PROGRESS);
        intentFilter.addAction(TingConstants.ACTION_LAST_MUSIC);
        intentFilter.addAction(TingConstants.ACTION_NEST_MUSIC);
        intentFilter.addAction(TingConstants.ACTION_TIME);

        mbr = new MyBroadcastReceiver();
        registerReceiver(mbr, intentFilter);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        urlMusic = intent.getStringArrayExtra("url");
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }


    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (TingConstants.ACTION_PLAY.equals(intent.getAction())) {

                Log.e("----------", "播放命令+service");

                if (!isPlay()) {
                    if (!isFirstPlays) {
                        isFirstPlays = true;
                        prepareMusic(urlMusic[position]);
                    } else {
                        player.start();
                        sendCurrentProgress();
                        isOver = false;
                    }


                }


            } else if (TingConstants.ACTION_PAUSE.equals(intent.getAction())) {
                Log.e("----------", "暂停命令+service");
                if (isPlay()) {
                    player.pause();
                    isOver = true;

                }
            } else if (TingConstants.ACTION_STOP.equals(intent.getAction())) {
                if (!isPlay()) {
                    Log.e("----------", "停止命令+service");
                    isFirstPlays = false;
                    isOver = true;
                    player.stop();
                    player.release();
                    player = null;
                    //unregisterReceiver(mbr);


                }
            } else if (TingConstants.ACTION_SD_PROGRESS.equals(intent.getAction())) {
                int currP = Integer.parseInt(intent.getStringExtra("data"));
                if (isPlay()) {
                    player.seekTo(currP);
                }


            } else if (TingConstants.ACTION_NEST_MUSIC.equals(intent.getAction())) {
                if (isPlay()) {
                    player.stop();
                }
                position++;
                prepareMusic(urlMusic[position % urlMusic.length]);

                Log.e("--------", "下标:" + position % urlMusic.length);

            } else if (TingConstants.ACTION_LAST_MUSIC.equals(intent.getAction())) {
                if (isPlay()) {
                    player.stop();
                }
                if (position > 0) {
                    position--;
                } else {
                    position = urlMusic.length - 1;
                }

                Log.e("--------", "下标:" + position);
                prepareMusic(urlMusic[position]);
            } else if (TingConstants.ACTION_TIME.equals(intent.getAction())) {

                String va = intent.getStringExtra("time");
                Log.e("---------", va);
                if (isPlay()) {
                    sendInfoToActivity(TingConstants.ACTION_SET_TEXT, 0);
                    player.stop();
                    isOver = true;
                    isFirstPlays = false;
                    player.release();
                    player = null;


                }


            }
        }

    }


    private boolean isPlay() {
        return player != null && player.isPlaying();
    }

    /**
     * 准备
     */
    private void prepareMusic(String urls) {
        player = new MediaPlayer();
        player.reset();
        Uri songUri = Uri.parse(urls);
        try {
            player.setDataSource(getApplicationContext(), songUri);

        } catch (IOException e) {
            Log.e("----------", "播放命令" + e.getMessage());

        }

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.prepareAsync();


    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isFirstPlays = true;
        mp.start();

        totalTime = player.getDuration();

        // Log.e("------------", "服务总时间" + totalTime);

        sendInfoToActivity(TingConstants.ACTION_TOTAL_PROGRESS, totalTime);
        sendCurrentProgress();
        isOver = false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        prepareMusic(urlMusic[(position + 1) % urlMusic.length]);
        position = position + 1;
    }


    /**
     * 总进度
     */
    private void sendInfoToActivity(String action, int data) {
        Intent totalIntent = new Intent();
        totalIntent.setAction(action);
        totalIntent.putExtra("total", data);
        sendBroadcast(totalIntent);
    }

    /**
     * 发送当前进度
     */
    private void sendCurrentProgress() {


        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent currentTimeIntent = new Intent();
                while (!isOver) {
                    int currentTime = player.getCurrentPosition();
                    currentTimeIntent.setAction(TingConstants.ACTION_CURRENT_PROGRESS);
                    currentTimeIntent.putExtra("progress", currentTime);
                    sendBroadcast(currentTimeIntent);

                    //Log.e("----------", "时间:" + currentTime);

                    if (totalTime == currentTime) {
                        isOver = true;
                    }
                }

            }
        }).start();


    }


}
