package com.example.parkingapp;

import androidx.annotation.Nullable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MypageActivity extends BaseActivity {

    static private String SHARED_NAME = "shared_file";

    TextView totalCardPrice;
    Button btnCard, btnLogout, btnUserOut;  //btnUserOut는 회원탈퇴
    int memoPriceNum;

    SharedPreferences pref;
    String SHAREDPREF_JSON_KEY = "userList"; //shared -> 저장되있는 불러올 키
    JSONArray userArray;
    JSONObject userObject;

    String loginUserID; //로그인한 유저아이디
    String prefUserID, prefUserName;  //쉐어드에 저장된 아이디&이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_mypage);

        //쉐어드 객체 생성
        pref = getSharedPreferences(SHARED_NAME, MODE_PRIVATE);

        //카드요금 계산
        totalCardPrice = (TextView)findViewById(R.id.totalCardPrice);   //총 카드요금
        btnCard = (Button)findViewById(R.id.btnCard); //카드요금 입력버튼 -> 클릭시 PriceActivity 로 인텐트 이동
        btnLogout = (Button)findViewById(R.id.btnLogout);   //로그아웃
        btnUserOut = (Button)findViewById(R.id.btnUserOut); //회원탈퇴

        //쉐어드에 저장된 카드 요금 불러와 setText 해주기
        String textText = pref.getString("totalPrice","");
        totalCardPrice.setText(textText);
        makeLog("쉐어드에서 불려와진 데이터 삽입 TextView(textText) :"+textText);

        //버튼 - 요금추가 클릭시
        btnCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent priceIntent = new Intent(MypageActivity.this, PriceActivity.class);
                startActivityForResult(priceIntent,50);
            }
        });

        //버튼 - 로그아웃 클릭시
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //빌더 생성
                final AlertDialog.Builder builder = new AlertDialog.Builder(MypageActivity.this);
                //빌더에 정보 입력
                builder.setTitle("로그아웃 확인")
                        .setMessage("정말 로그아웃 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //로그아웃 메소드 -> 쉐어드에서 자동로그인 관련 값 null 로 만든다
                                logout();
                                makeToast("로그아웃 되었습니다.");

                                //로그인 화면으로 전환
                                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(loginIntent);
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                //알럿다이얼로그 생성
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        //버튼 - 회원탈퇴 클릭시
        btnUserOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //다일얼로그 Builder 객체를 만들어 정보를 셋팅한다
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MypageActivity.this);
                //다이얼로그 제목
                alertDialogBuilder.setTitle("회원탈퇴 확인");
                alertDialogBuilder.setMessage("정말 탈퇴 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //회원탈퇴 진행

                                //로그인한 아이디
                                loginUserID = pref.getString("loginID", "");

                                //쉐어드에서 유저리스트 뽑아내기 -> 쉐어드 에디터에 저장
                                prefOutUserDataReSave();

                                //회원탈퇴 했으니 -> 로그인 화면으로 전환
                                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(loginIntent);
                                finish();

                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //회원탈퇴 취소
                                dialog.cancel();

                            }
                        });

                //다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();
                //다이얼로그 보여주기
                alertDialog.show();

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //주차 요금 계산
        if(requestCode == 50 && resultCode == RESULT_OK){

            //totalPrice(TextView) -> int 변환
            memoPriceNum = Integer.parseInt(totalCardPrice.getText().toString());

            Log.d("MypageActivity","totalPrice.getText().toString()) :"+totalCardPrice.getText().toString());
            Log.d("MypageActivity","memoPriceNum :"+memoPriceNum);

            int addPriceNum = data.getIntExtra("price", 0);  //PriceActivity에서 넘겨받은 price 요금값

            Log.d("MypageActivity","addPriceNum :"+addPriceNum);

            int totalPriceNum = memoPriceNum + addPriceNum;

            Log.d("MypageActivity","totalPriceNum :"+totalPriceNum);

            String totalPriceStr = String.valueOf(totalPriceNum);

            Log.d("MypageActivity","totalPriceStr :"+totalPriceStr);

            totalCardPrice.setText(totalPriceStr);

        }
    }

    //로그아웃
    public void logout(){
        //editor정의
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("autoLoginID", "");
        editor.putString("autoLoginPW", "");
        editor.putString("autoLoginName", "");

        editor.putString("loginID", "");
        editor.putString("loginName", "");

        editor.commit();    //최종반영
    }

    //회원탈퇴
    public void prefOutUserDataReSave(){

        String prefUserlitStrData = pref.getString(SHAREDPREF_JSON_KEY, "");

        try {
            userArray = new JSONArray(prefUserlitStrData);

            makeLog("userObject 삭제 전 userArray : "+userArray);

            for(int i=0; i < userArray.length() ; i++){

                userObject = new JSONObject();
                userObject = userArray.getJSONObject(i);

                prefUserID = userObject.getString("userID");
                prefUserName = userObject.getString("userName");

                makeLog("쉐어드에 저장된 아이디 : "+prefUserID);

                //로그인한 아이디 = 쉐어드에 저장된 아이디 같으면 탈퇴 -> jsonObject 없앰
                if(loginUserID.equals(prefUserID)){
//                    userArray 에서 삭제할 userObject의 index번호
//                    userArray.getJSONObject(i).remove();
                    makeLog("로그인한 아이디 : "+loginUserID+"와 일치");
                    makeLog("이 아이디의 유저이름은 : "+prefUserName);

                    userArray.remove(i);
                    makeLog("userObject 삭제 완료");
                    makeLog("userObject 삭제 후 userArray : "+userArray);

                    break; //로그인 == 회원가입 아이디 같은거 찾았으면 for문 종료
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //변경된 jsonArray 쉐어드에 적용
        SharedPreferences.Editor editor = pref.edit();
        //userArrayAfterData -> 탈퇴 진행 후 변경된 데이터
        String userArrayAfterData = userArray.toString();
        //쉐어드 에디터에 적용
        editor.putString(SHAREDPREF_JSON_KEY, userArrayAfterData);

        //자동로그인 관련 값들도 null 처리 해줘야함
        editor.putString("autoLoginID", "");
        editor.putString("autoLoginPW", "");
        editor.putString("autoLoginName", "");

        //쉐어드 최종반영
        editor.commit();

        makeLog("쉐어드 최종 변경 완료됨 : "+userArrayAfterData);

        makeToast(prefUserName+"님 회원탈퇴가 완료되었습니다.");

    }

    @Override
    int getContentViewId() {
        return R.layout.activity_mypage;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.botNav04;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        //종료되기 전 저장 -> 쉐어드를 shared_file 이름, 기본모드로 설정
//        SharedPreferences sf = getSharedPreferences(SHARED_NAME, MODE_PRIVATE);
//        //저장하기 위해 에디터를 이용
//        SharedPreferences.Editor editor = sf.edit();
//        String getText = totalCardPrice.getText().toString();   //총카드금액 getText 변수에 저장
//        editor.putString("totalPrice", getText);    //에디터에 totalPrice (KEY) : getText (VALUE) 를 저장
//        editor.apply();    //최종 저장 (commit = 동기적으로 값을 저장하고 결과를 리턴함)
//
//        Log.d("MypageActivity","종료하면서 쉐어드에 데이터 저장 TextView(getText) :"+getText);


    }
}
