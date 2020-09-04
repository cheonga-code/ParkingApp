package com.example.parkingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Base2Activity extends AppCompatActivity {

    //쉐어드 관련
    static String SHARED_NAME = "shared_file";
    static int SHARED_MODE = MODE_PRIVATE;

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
        Log.d(TAG, classname+"-onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, classname+"-onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, classname+"-onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, classname+"-onPause");
        super.onPause();
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

    //사진 ImageView에 적용시키기
    public void setImageBitmapRotate(String str, ImageView img) {
        Bitmap bitmap = BitmapFactory.decodeFile(str);
        makeLog("카메라 imagePath : "+str); // -> 요게 절대경로 인가??
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }

//        Bitmap rotateReturnBitmap = rotate(bitmap, exifDegree);
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(rotateReturnBitmap, 700, 300, true);
//        img.setImageBitmap(resizedBitmap);//이미지 뷰에 비트맵 넣기
        img.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기

    }

    //사진의 회전값 가져오기
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //사진을 정방향대로 회전하기 -> 회전된 섬네일 이미지를 돌려주는 함수
    private Bitmap rotate(Bitmap src, float degree) {
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

}
