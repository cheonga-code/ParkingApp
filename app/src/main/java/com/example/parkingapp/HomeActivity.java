package com.example.parkingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    TextView homeJoinNameTv;
    Button btnInParking, btnOutParking, btnMemoView, btnTimerSetting;

    String userNameStr;

    //광고 안 이미지 slider 관련
    ImageView adImg;
    int adPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);

        //id 연결
        homeJoinNameTv = (TextView)findViewById(R.id.homeJoinName);
        btnInParking = (Button)findViewById(R.id.btnInParking);
        btnOutParking = (Button)findViewById(R.id.btnOutParking);
        btnMemoView = (Button)findViewById(R.id.btnMemoView);
        btnTimerSetting = (Button)findViewById(R.id.btnTimerSetting);   //타이머 설정

        btnInParking.setOnClickListener(this);
        btnOutParking.setOnClickListener(this);
        btnMemoView.setOnClickListener(this);
        btnTimerSetting.setOnClickListener(this);

        //원래, 로그인 화면에서 인텐트로 데이터 받은 값 setText 했는데,
        //다른 액티비티 갔다오면 운전자 이름 null값 되므로 쉐어드에서 데이터 받아오는 코드로 바꿈
        SharedPreferences pref = getSharedPreferences(SHARED_NAME, SHARED_MODE);
        userNameStr = pref.getString("loginName", "");

        //특정문자열에 색상 입히기
        final SpannableStringBuilder sp = new SpannableStringBuilder("안녕하세요, "+userNameStr+" 운전자님");
        sp.setSpan(new ForegroundColorSpan(Color.RED), 7, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        homeJoinNameTv.append(sp);

//        homeJoinNameTv.setText("안녕하세요, "+userNameStr+" 운전자님");

        //adAutoSlider 광고영역 핸들러 이벤트 start
        adImg = (ImageView)findViewById(R.id.homeAdCnt);
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                adSlideThread();
            }
        };
        Thread myThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        mHandler.sendMessage(mHandler.obtainMessage());
                        Thread.sleep(3000);
                    } catch (Throwable t) {
                    }
                }
            }
        });
        myThread.start();   //3초 딜레이 후 시작
        //adAutoSlider 광고영역 핸들러 이벤트 end


    }

    @Override
    protected void onResume() {
        super.onResume();
        //다른화면 갔다오면 운전자 이름 null값 되므로 쉐어드에서 데이터 받아오기
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_home;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.botNav01;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnInParking:
                Intent inPrkMemoIntent = new Intent(this, MemoActivity.class);
                inPrkMemoIntent.putExtra("MemoTitle","실내주차 기록");
                startActivityForResult(inPrkMemoIntent, 1001);
                break;
            case R.id.btnOutParking:
                Intent outPrkMemoIntent = new Intent(this, MemoActivity.class);
                outPrkMemoIntent.putExtra("MemoTitle","야외주차 기록");
                startActivityForResult(outPrkMemoIntent, 1002);
                break;
            case R.id.btnMemoView:
                Intent viewIntent = new Intent(this, ViewActivity.class);
                startActivity(viewIntent);
                break;
            case R.id.btnTimerSetting:
                Intent timerIntent = new Intent(this, TimerActivity.class);
                startActivity(timerIntent);
                break;
        }

    }

    //광고영역 이미지 바꿔주는 메소드
    private void adSlideThread() {

        int mod = adPage % 4;
        switch (mod) {
            case 0:
                adPage++;
                adImg.setImageResource(R.drawable.ad_img01);
                break;
            case 1:
                adPage++;
                adImg.setImageResource(R.drawable.ad_img02);
                break;
            case 2:
                adPage++;
                adImg.setImageResource(R.drawable.ad_img03);
                break;
            case 3:
                adPage = 0;
                adImg.setImageResource(R.drawable.ad_img04);
                break;
        }
    }

}
