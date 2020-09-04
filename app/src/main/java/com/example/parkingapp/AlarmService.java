package com.example.parkingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {

    String TAG = "LogActivity";
    String classname = "AlarmService";

    private MediaPlayer mediaPlayer;
    private boolean isRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, classname+"-onStartCommand");

        //Foreground 에서 실행되면 Notification 을 보여줘야함
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Orao(26)버전 이후 버전부터는 channel 이 필요함
            String channelId = createNotificationChannel();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            Notification notification = builder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();

            startForeground(1, notification);
        }

        //Receiver 에서 받은 intent 데이터
        String state = intent.getStringExtra("state");
        Log.d("LogActivity", "AlarmService : state 상태 : "+intent.getStringExtra("state"));
        Log.d("LogActivity", "AlarmService : isRunning상태 : "+isRunning);

        if(isRunning==false && state.equals("on")){

            Log.d("LogActivity", "AlarmService : 1");
            //알람음 재생 OFF, 알람음 시작 상태
            mediaPlayer = MediaPlayer.create(this, R.raw.piano);
            mediaPlayer.start();
            Log.d("LogActivity", "AlarmService : Alarm Start ♬♫♪♩ ♬♫♪♩ ♬♫♪♩");

            isRunning = true;
            Log.d("LogActivity", "AlarmService : 알람 후 isRunning상태 : "+isRunning);

            stopSelf(); //알람음이 끝나면 자동으로 스스로 서비스 종료시킴
            Log.d("LogActivity", "AlarmService : stopSelf() 시킴");

        }else if(isRunning==true && state.equals("off")){
            Log.d("LogActivity", "AlarmService : 2");
            //알람음 재생 ON, 알람음 중지 상태
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            Log.d("LogActivity", "AlarmService : Alarm Stop ♬♫♪♩ ♬♫♪♩ ♬♫♪♩");

            isRunning = false;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                stopForeground(true);
            }
        }

        return START_NOT_STICKY;

//        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi (Build.VERSION_CODES.O)
    private String createNotificationChannel(){

        String channelId = "Alarm";
        String ChannelName = getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(channelId, ChannelName, NotificationManager.IMPORTANCE_NONE);

        channel.setSound(null, null);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        return channelId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, classname+"-onCreate");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, classname+"=onDestroy");
    }

}
