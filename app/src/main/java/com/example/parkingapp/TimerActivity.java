package com.example.parkingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends Base2Activity {

    TextView alarmTv, timerTv, timerStartTv, timerEndTv, timerDurationTv;
    Button btnAlarmCreate, btnAlarmRemove, btnTimerStart, btnTimerStop;
    TimePicker timePicker;
    Calendar calendar;

    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    TimerTask timerTask;

    SimpleDateFormat dataFormat;
    Date startDate, endDate;

    int alarmIdx = 1001;
    int second = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timePicker = (TimePicker)findViewById(R.id.timePicker);
        alarmTv = (TextView)findViewById(R.id.alarmTv);
        timerTv = (TextView)findViewById(R.id.timerTv);
        timerStartTv  = (TextView)findViewById(R.id.timerStartTv);
        timerEndTv  = (TextView)findViewById(R.id.timerEndTv);
        timerDurationTv = (TextView)findViewById(R.id.timerDurationTv);

        btnAlarmCreate = (Button)findViewById(R.id.btnAlarmCreate);
        btnAlarmRemove = (Button)findViewById(R.id.btnAlarmRemove);
        btnTimerStart = (Button)findViewById(R.id.btnTimerStart);
        btnTimerStop = (Button)findViewById(R.id.btnTimerStop);

        dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
//            @Override
//            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                makeLog("설정 시작 : "+hourOfDay+"시 "+minute+"분");
//
////                //테스트중
////                Calendar calNow = Calendar.getInstance();   // 현재 시간을 위한 Calendar 객체를 구한다.
////                int hourCurrent = calNow.get(calendar.HOUR_OF_DAY);
////                int minuteCurrent = calNow.get(calendar.MINUTE);
////                int secondCurrent = calNow.get(calendar.SECOND);
////                makeLog("알람 시작한 시간 : "+hourCurrent+"시 "+minuteCurrent+"분 "+secondCurrent+"초");
////
////                Calendar calSet = (Calendar)calNow.clone();   // 바로 위에서 구한 객체를 복제 한다.
////
////                calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);   // 시간 설정
////                calSet.set(Calendar.MINUTE, minute);        // 분 설정
////                calSet.set(Calendar.SECOND, 0);               // 초는 '0'으로 설정
////                calSet.set(Calendar.MILLISECOND, 0);       // 밀리 초도  '0' 으로 설정
////
////                if(calSet.compareTo(calNow) <= 0){            // 설정한 시간과 현재 시간 비교
////
////                    // 만약 설정한 시간이 현재 시간보다 이전이면
////                    calSet.add(Calendar.DATE, 1);  // 설정 시간에 하루를 더한다.
////                }
////
////                setAlarm(calSet);  // 주어진 시간으로 알람을 설정한다.
////                //테스트중 end
//
//            }
//        });


        btnAlarmCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //알림 생성
                setAlarm();
//                createNotification();
            }
        });

        btnAlarmRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //알림 제거
                removeAlarm();
//                removeNotification();
            }
        });


        btnTimerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //타이머 시작
                startTimer();
            }
        });

        btnTimerStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //타이머 정지
                stopTimer();
            }
        });


    }

    //테스트중
    private void setAlarm() {

        //캘린더 객체 생성
        calendar = Calendar.getInstance();

        int hourCurrent = calendar.get(calendar.HOUR_OF_DAY);
        int minuteCurrent = calendar.get(calendar.MINUTE);
        int secondCurrent = calendar.get(calendar.SECOND);
        makeLog("Alarm 발생시킨 시간 : "+hourCurrent+"시 "+minuteCurrent+"분 "+secondCurrent+"초");

        //시간설정
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        calendar.set(Calendar.MINUTE, timePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);

        //현재시간보다 이전이면
        if(calendar.before(Calendar.getInstance())){
            //다음날로 설정
            calendar.add(Calendar.DATE, 1);
        }

        //Receiver 설정
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        //state 값이 on 이면 알림시작, off 이면 중지
        alarmIntent.putExtra("state", "on");

        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        //알람매니저 시작
        startAlarm();

    }

    private void startAlarm(){

        //알람매니저 호출
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        //알람매니저 설정
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 6000, pendingIntent);
            //-> 앱 종료되도 알람 계속~ 1분에 한번씩 울리는 현상 발생.... 매우 크리티컬한 문제 !!!!

        //알람 설정한 시각 표시
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        alarmTv.setText("알람 설정 "+format.format(calendar.getTime()));

        makeLog("Alarm 설정 시간 : "+format.format(calendar.getTime()));
    }

    private void removeAlarm(){

        if(pendingIntent == null){
            makeLog("pendingIntent null 이다..");
            return;
        }

        //알람 해제
        alarmManager.cancel(pendingIntent);
//        pendingIntent.cancel(); //인텐트 해제

        //알람중지 Brodcast
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("state", "off");
        sendBroadcast(intent);

        makeLog("알람 취소했다..");
        alarmTv.setText("알람 취소");

        pendingIntent = null;
    }

    private void startTimer(){

        //시작날짜
        String currentDate = dataFormat.format(new Date());
        makeLog("시작 String currentDate : "+currentDate);
        timerStartTv.setText("타이머 시작한 시간 : "+currentDate);

        try {
            startDate = dataFormat.parse(currentDate);
            makeLog("시작 Date startDate : "+startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //타이머 객체 생성
        Timer timer = new Timer();

        //핸들러 생성
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                //실행하고자하는 내용
                second ++;

//                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
//
//                String timeStr = format.toString(second);

                timerTv.setText("타이머 시간 : "+second);
//                super.handleMessage(msg);
            }
        };

        //타이머 테스크 설정
        timerTask = new TimerTask() {
            @Override
            public void run() {
                //실행하고자하는 내용
//                second ++;
//                timerTv.setText("타이머 시간 : "+second);
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
            }
        };

        //1초에 한번 timerTask를 실행하도록 설정
        timer.schedule(timerTask, 0, 1000);
//      timer.schedule(타이머테스크 이름, 초기 딜레이 시간, 반복 시간);
    }

    private void stopTimer(){

        //끝날짜
        String currentDate = dataFormat.format(new Date());
        makeLog("끝 String currentDate : "+currentDate);
        timerEndTv.setText("타이머 끝낸 시간 : "+currentDate);
        try {
            endDate = dataFormat.parse(currentDate);
            makeLog("끝 Date endDate : "+endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long hour = (endDate.getTime() - startDate.getTime()) / (60*60*1000);
        long min = (endDate.getTime() - startDate.getTime()) / (60*1000);
        long sec = (endDate.getTime() - startDate.getTime()) / (1000);
        makeLog("끝 endDate.getTime() : "+endDate.getTime());
        makeLog("끝 startDate.getTime() : "+startDate.getTime());
        makeLog("끝 시간차이 : "+hour+"시간"+min+"분"+sec+"초 경과");

        timerDurationTv.setText("타이머 경과 시간 : "+hour+"시간 "+min+"분 "+sec+"초");

//        timerTv.setText((int)sec);

        //타이머 테스크 중지
        timerTask.cancel();
        makeLog("타이머 마지막 실행 시간 : "+timerTask.scheduledExecutionTime());

    }

}
