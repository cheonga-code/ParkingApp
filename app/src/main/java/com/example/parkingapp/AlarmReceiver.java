package com.example.parkingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        Log.d("LogActivity", "AlarmReceiver : onReceive() 진입..");
        //액티비티에서 받은 state 상태
        String stateStr = intent.getStringExtra("state");

        //상태가 on 이면 -> notification 알림 띄우고 & startService()
        if(stateStr.equals("on")){
            Log.d("LogActivity", "AlarmReceiver : Notification 생성 ");

            //알람 생성 메소드
            createNotification();

//            //서비스로 인텐트 보낸다.
//            Intent serviceIntent = new Intent(context, AlarmService.class);
//            serviceIntent.putExtra("state", intent.getStringExtra("state"));
//            Log.d("LogActivity", "AlarmReceiver : state 상태 : "+intent.getStringExtra("state"));
//
//            //Oreo(26) 버전 이후부터는 Background 에서 실행을 금지하기때문에 Foreground 에서 실행해야함
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//                //오레오 버전 이후
//                context.startForegroundService(serviceIntent);
//            }else{
//                //오레오 버전 이전
//                context.startService(serviceIntent);
//                Log.d("LogActivity", "AlarmReceiver : startService 로 보냄 ");
//            }

        }else if(stateStr.equals("off")){
            //상태가 off 이면 -> notification 알림 취소 & stopService()
            Log.d("LogActivity", "AlarmReceiver : Notification 제거 ");

            //알람 제거 메소드
            removeNotification();


//            //서비스로 인텐트 보낸다.
//            Intent serviceIntent = new Intent(context, AlarmService.class);
//            serviceIntent.putExtra("state", intent.getStringExtra("state"));
//            Log.d("LogActivity", "AlarmReceiver : state 상태 : "+intent.getStringExtra("state"));
//
//            context.stopService(serviceIntent);
//            Log.d("LogActivity", "리시버 종료함");

        }


    }

    //알림 생성 메소드
    public void createNotification(){

        //알람매니저 정의
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("LogActivity", "AlarmReceiver : NotificationManager 띠링 !! 푸시 알림 !! ");
        //알람 설정한 시각 표시
        Calendar calendar = Calendar.getInstance();   // 현재 시간을 위한 Calendar 객체를 구한다.
        int hourCurrent = calendar.get(calendar.HOUR_OF_DAY);
        int minuteCurrent = calendar.get(calendar.MINUTE);
        int secondCurrent = calendar.get(calendar.SECOND);
        Log.d("LogActivity", "알람 울린 시간 : "+hourCurrent+"시 "+minuteCurrent+"분 "+secondCurrent+"초");

        //오레오 이상일 때
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        //팬딩인텐트 정의 -> PendingIntent 개체로 정의된 콘텐츠 인텐트를 지정
        Intent homeIntent = new Intent(context, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, homeIntent, 0);

        //빌더 정의
        Notification.Builder builder = new Notification.Builder(context);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("주차 알림");
        builder.setContentText("설정한 알람시간이 되었습니다.");    //정의된 PendingIntent 전달
        builder.setContentIntent(pendingIntent);
//           builder.setFullScreenIntent(pendingIntent, true);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setColor(Color.RED);
        builder.setAutoCancel(true);    // 사용자가 탭을 클릭하면 자동 제거

        //알람 실행 -> notify // id값은 정의해야하는 각 알림의 고유한 int값 -> 알람 삭제할때 이 아이디값으로 동일하게 주면 삭제된다
        notificationManager.notify(1, builder.build());

    }

    //알람 제거 메소드
    private void removeNotification() {

        // Notification 제거
        NotificationManagerCompat.from(context).cancel(1);
    }


}
