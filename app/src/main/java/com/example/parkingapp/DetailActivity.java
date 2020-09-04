package com.example.parkingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import org.w3c.dom.Text;

import java.net.URI;

public class DetailActivity extends Base2Activity {

    TextView detailTitleTv, detailCntTv, detailDateTv, detailGpsTv;
    ImageView detailImg;
    Button btnDetailMemoOK, btnShareMemo, btnEditMemoGO;

    //자신을 실행한 인텐트 객체에서 데이터 얻기
    Intent intent;
    //인텐트에서 얻은 데이터 (MemolistActivity 에서 얻음)
    String getIntentTitleStr, getIntentCntStr, getIntentDateStr, getIntentGpsStr, getIntentSaveTimeStr, getIntentImgPathStr,
            editIntentTitleStr, editIntentCntStr, editIntentDateStr, editIntentGpsStr, editIntentSaveTimeStr, editIntentImgPathStr;   //editActivity 에서 넘겨받은 값

    //카카오 관련
//    private KakaoLink kakaoLink;
//    private KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //id 연결
        detailTitleTv = (TextView)findViewById(R.id.detailTitleTv);
        detailCntTv = (TextView)findViewById(R.id.detailCntTv);
        detailDateTv = (TextView)findViewById(R.id.detailDateTv);
        detailGpsTv = (TextView)findViewById(R.id.detailGpsTv);
        detailImg = (ImageView)findViewById(R.id.detailImg);

        btnDetailMemoOK = (Button)findViewById(R.id.btnDetailMemoOK);   //버튼 - 확인
        btnShareMemo = (Button)findViewById(R.id.btnShareMemo);         //버튼 - 공유
        btnEditMemoGO = (Button)findViewById(R.id.btnEditMemoGO);       //버튼 - 수정

        //자신을 실행한 인텐트 객체에서 데이터 얻기
        intent = getIntent();

        getIntentTitleStr = intent.getStringExtra("저장된 메모제목");
        getIntentCntStr = intent.getStringExtra("저장된 메모내용");
        getIntentDateStr = intent.getStringExtra("저장된 메모날짜");
        getIntentGpsStr = intent.getStringExtra("저장된 메모위치");
        getIntentSaveTimeStr = intent.getStringExtra("저장된 메모시간");
        getIntentImgPathStr = intent.getStringExtra("저장된 메모사진");

        //setText 해주기
        detailTitleTv.setText(getIntentTitleStr);
        detailCntTv.setText(getIntentCntStr);
        detailDateTv.setText(getIntentDateStr);
        detailGpsTv.setText(getIntentGpsStr);

        //setImg 해주기
//        Bitmap bitmap = BitmapFactory.decodeFile(getIntentImgPathStr);  //얻은 이미지 경로 비트맵으로 전환
//        editImg.setImageBitmap(bitmap);   //이미지 뷰에 비트맵 넣기
        detailImg.setImageURI(Uri.parse(getIntentImgPathStr));

//        setImageBitmapRotate(getIntentImgPathStr, detailImg);

        makeLog("\n리스트 Acty에서 받은 메모데이터 : "+getIntentTitleStr+"/"+getIntentCntStr+"/"+getIntentDateStr+"/"+getIntentGpsStr+"/"+getIntentSaveTimeStr+"/"+getIntentImgPathStr);

        //데이터 초기화 - 수정 안하고, 그냥 상세화면 보고 확인 클릭시
        setResultStrInit();

        //버튼 - 확인 클릭시
        btnDetailMemoOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //editText 데이터 얻어 -> Intent에 데이터 담기
                editTextIntoIntent();

                //전 화면으로 돌아감
                setResult(RESULT_OK, intent);   //정상으로 처리되어 되돌린다는 것을 명시함
                finish();   //자신을 종료
            }
        });

        //버튼 - 공유 클릭시
        btnShareMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "보낼 내용";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, message );
                intent.setPackage("com.kakao.talk");
                startActivity(intent);
            }
        });

        //버튼 - 수정 클릭시
        btnEditMemoGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //수정 화면으로 인텐트 전환
                Intent editIntent = new Intent(getApplicationContext(), EditActivity.class);
                editIntent.putExtra("메모제목", getIntentTitleStr);
                editIntent.putExtra("메모내용", getIntentCntStr);
                editIntent.putExtra("메모날짜", getIntentDateStr);
                editIntent.putExtra("메모위치", getIntentGpsStr);
                editIntent.putExtra("메모사진", getIntentImgPathStr);
                makeLog("\n상세 Acty에서 수정 Acty에 보낼 메모데이터 : "+getIntentTitleStr+"/"+getIntentCntStr+"/"+getIntentDateStr+"/"+getIntentGpsStr+"/"+getIntentImgPathStr);
                startActivityForResult(editIntent, 10);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10 && resultCode == RESULT_OK){

//            int addPriceNum = data.getIntExtra("price", 0);

            //EditActivity 넘겨받은 수정한 메모 데이터
            editIntentTitleStr = data.getStringExtra("수정한 메모제목");
            editIntentCntStr = data.getStringExtra("수정한 메모내용");
            editIntentDateStr = data.getStringExtra("수정한 메모날짜");
            editIntentGpsStr = data.getStringExtra("수정한 메모위치");
            editIntentSaveTimeStr = data.getStringExtra("수정한 메모시간");
            editIntentImgPathStr = data.getStringExtra("수정한 메모사진");

            //TextView 에 setText 하기
            detailTitleTv.setText(editIntentTitleStr);
            detailCntTv.setText(editIntentCntStr);
            detailDateTv.setText(editIntentDateStr);
            detailGpsTv.setText(editIntentGpsStr);

            //setImg 해주기
//            Bitmap bitmap = BitmapFactory.decodeFile(editIntentImgPathStr);  //얻은 이미지 경로 비트맵으로 전환
//            detailImg.setImageBitmap(bitmap);   //이미지 뷰에 비트맵 넣기
            detailImg.setImageURI(Uri.parse(editIntentImgPathStr));
//            setImageBitmapRotate(editIntentImgPathStr, detailImg);

            makeLog("\n수정 Acty에서 받은 수정한 메모데이터 : "+editIntentTitleStr+"/"+editIntentCntStr+"/"+editIntentDateStr+"/"+editIntentGpsStr+"/"+editIntentSaveTimeStr+"/"+editIntentImgPathStr);

        }
    }

    //데이터 초기화 - 수정 안하고, 그냥 상세화면 보고 확인 클릭시
    //editIntentTitleStr 등.. -> null 값 방지
    public void setResultStrInit(){

        editIntentTitleStr = getIntentTitleStr;
        editIntentCntStr = getIntentCntStr;
        editIntentDateStr = getIntentDateStr;
        editIntentGpsStr = getIntentGpsStr;
        editIntentSaveTimeStr = getIntentSaveTimeStr;
        editIntentImgPathStr = getIntentImgPathStr;
    }

    //editText 데이터 얻어 -> Intent에 데이터 담기
    public void editTextIntoIntent(){

        intent.putExtra("수정할 메모제목", editIntentTitleStr);
        intent.putExtra("수정할 메모내용", editIntentCntStr);
        intent.putExtra("수정할 메모날짜", editIntentDateStr);
        intent.putExtra("수정할 메모위치", editIntentGpsStr);
        intent.putExtra("수정할 메모시간", editIntentSaveTimeStr);
        intent.putExtra("수정할 메모사진", editIntentImgPathStr);

        makeLog("\n상세 Acty에서 리스트 Acty에 보낼 수정할 메모데이터 : "+editIntentTitleStr+"/"+editIntentCntStr+"/"+editIntentDateStr+"/"+editIntentGpsStr+"/"+editIntentSaveTimeStr+"/"+editIntentImgPathStr);

    }

}
