package com.example.parkingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    //쉐어드 관련
    static String SHARED_NAME = "shared_file";
    static int SHARED_MODE = MODE_PRIVATE;

    //바텀네비게이션 바 관련
    protected BottomNavigationView navigationView;

    //로그 관련
    String classname = getClass().getSimpleName().trim();
    String TAG = "LogActivity";

    //로그
    public void makeLog(String strData){
        Log.d(TAG, classname+"-"+strData);
    }
    //토스트메세지 띄우기
    public void makeToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        Log.d(TAG, classname+"-onCreate");

        navigationView = (BottomNavigationView) findViewById(R.id.botNavbarCnt);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();

        Log.d(TAG, classname+"-onStart");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, classname+"-onResume");
        super.onResume();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);

        Log.d(TAG, classname+"-onPause");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, classname+"-onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, classname+"+onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, classname+"=onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.botNav01:
                startActivity(new Intent(this, HomeActivity.class));
                finish();   //액티비티가 전환되면 그 전 화면 없앤다. 뒤로가기할때 바로 종료하기 위해
                return true;

            case R.id.botNav02:
                startActivity(new Intent(this, MemolistActivity.class));
                finish();
                return true;

            case R.id.botNav03:
                startActivity(new Intent(this, ParkinglotActivity.class));
                finish();
                return true;

            case R.id.botNav04:
                startActivity(new Intent(this, MypageActivity.class));
                finish();
                return true;

        }

//        updateNavigationBarState(item.getItemId());

        return false;
    }

//    private void updateNavigationBarState(int actionId){
//        Menu menu = navigationView.getMenu();
//
//        for (int i = 0, size = menu.size(); i < size; i++) {
//            MenuItem item = menu.getItem(i);
//            item.setChecked(item.getItemId() == actionId);
//        }
//    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        item.setChecked(true);

//        Menu menu = navigationView.getMenu();
//        for (int i = 0, size = menu.size(); i < size; i++) {
//            MenuItem item = menu.getItem(i);
//            makeLog("네비게이션 item :"+item);   //Home ParkingList Search MyPage
////            makeLog("네비게이션 size :"+size);   //4 4 4 4
//            makeLog("네비게이션 i :"+i);         //0 1 2 3
//
//            boolean shouldBeChecked = item.getItemId() == itemId;
//            makeLog("네비게이션 item.getItemId() :"+item.getItemId());
//            makeLog("네비게이션 itemId :"+itemId);
//
//            if (shouldBeChecked) {
//                item.setChecked(true);
//                makeLog("네비게이션 item11 :"+item);
//                break;
//            }
//        }
    }

    //상속받은 클래스에서 해당 액티비티 layout을 리턴한다.
    //return R.layout.activity_parking;
    abstract int getContentViewId();

    //상속받은 클래스에서 해당 BottomNavigationView id를 리턴한다.
    //return R.id.botNav04; -> botnav_menu.xml 에 적은 아이디
    abstract int getNavigationMenuItemId();


}

