package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PriceActivity extends Base2Activity {

    EditText priceAddEt;
    Button btnPriceAdd;

    int priceNum = 0;   //초깃값 세팅
    String priceStr = "0"; //초깃값 세팅

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price);

        //id연결
        priceAddEt = (EditText)findViewById(R.id.priceAddEt);
        btnPriceAdd = (Button)findViewById(R.id.btnPriceAdd);

        //요금 추가 버튼 클릭시 이벤트
        btnPriceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //priceAddEt -> string으로 받아와서 -> int 변환
                priceStr = priceAddEt.getText().toString();
                try {
                    priceNum = Integer.parseInt(priceStr);
                } catch(Exception e) {
                    //에러시 수행
                    e.printStackTrace(); //오류 출력
                }

                Intent intent = getIntent();    //자신을 실행한 인텐트 객체를 얻는다
                intent.putExtra("price", priceNum); //버튼 누를때 인텐트 객체에 요금 데이터를 담는다.
                setResult(RESULT_OK, intent);   //정상으로 처리되어 되돌린다는 것을 명시함
                finish();   //자신을 종료
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //다이얼로그 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("카드 충전 하기")             //제목 설정
                .setMessage("카드 충전을 계속 진행하시겠습니까?")      //메세지 설정
                .setCancelable(false)                  //백버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //확인 버튼 클릭시 설정
                        dialog.cancel();
                    }

                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //취소 버튼 클릭시 설정
                        Toast.makeText(getApplicationContext(), "카드 충전 취소", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성

        dialog.show();    // 알림창 띄우기
    }

}
