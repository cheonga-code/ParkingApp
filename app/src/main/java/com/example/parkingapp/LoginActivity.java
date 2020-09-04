package com.example.parkingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.LocusId;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Base2Activity {

    EditText loginIDEt, loginPWEt;
    Button btnLogin, btnJoinGO;
    CheckBox btnAutoLogin;
    //prefJoinIDStr, prefJoinPWStr, prefJoinName -> 쉐어드에서 추출한 값
    String loginIDStr, loginPWStr, prefJoinIDStr, prefJoinPWStr, prefJoinName;
    Boolean returnEmpty, returnEquals;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String SHAREDPREF_JSON_KEY = "userList"; //shared -> 저장되있는 불러올 키
    JSONArray userArray;
    JSONObject userObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //shared 정의
        pref = getSharedPreferences(SHARED_NAME, SHARED_MODE);

        //editor정의
        editor = pref.edit();

        //id 연결
        loginIDEt = (EditText)findViewById(R.id.loginID);
        loginPWEt = (EditText)findViewById(R.id.loginPW);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnJoinGO = (Button)findViewById(R.id.btnJoinGO);
        btnAutoLogin = (CheckBox)findViewById(R.id.btnAutoLogin);

        //회원가입 클릭시 -> 회원가입 액티비티로 전환
        btnJoinGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinIntent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivityForResult(joinIntent, 10);
            }
        });

        //로그인 클릭시 -> home화면으로 넘어감
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //editText 에서 텍스트 가져와서 String 형변환
                editTextToString();

                //editText 공백 검사
                if(editTextEmptyCheck() == true) {
                    //editTextEmptyCheck() == true 이면

                    //회원가입한 아이디와 맞는 비밀번호인지 검사
                    if(idEqualsPwCheck() == true){

                        //가입한 아이디로 쉐어드에 저장된 이름 추출
                        try {
                            prefJoinName = userObject.get("userName").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //자동로그인 CheckBox 체크 여부
                        autoLoginCheck();

                        //로그인 아이디 저장 (자동로그인 여부 상관없이)
                        editor.putString("loginID", prefJoinIDStr);
                        editor.putString("loginName", prefJoinName);
                        //최종 반영
                        editor.commit();

                        //홈 화면으로 전환
                        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
//                        homeIntent.putExtra("가입이름",prefJoinName);
                        makeLog("가입이름 : "+prefJoinName);
                        startActivity(homeIntent);
                        finish();
                    }
                }
//                else{
//                    //editTextEmptyCheck() == false 이면
//                    //editText 공백 검사
//                    editTextEmptyCheck();
//
//                }//editText 공백 검사 end

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10 && resultCode == RESULT_OK){
            String joinID = data.getStringExtra("joinID");

            loginIDEt.setText(joinID);    //회원가입한 id -> 로그인할 id 영역에 자동 입력해준다.
        }
    }

    //editText -> String 형변환
    public void editTextToString(){
        loginIDStr = loginIDEt.getText().toString();
        loginPWStr = loginPWEt.getText().toString();
    }

    //editText 공백 검사
    public boolean editTextEmptyCheck(){

        returnEmpty = false;    //공백이 있을시 false 반환

        if(loginIDStr.length() == 0){
            makeToast("아이디가 입력되지 않았습니다.");
            loginIDEt.requestFocus();

        }else if(loginPWStr.length() == 0){
            makeToast("비밀번호가 입력되지 않았습니다.");
            loginPWEt.requestFocus();

        }

        //입력텍스트 모두 공백이 아닐때
        if(loginIDStr.length() != 0 && loginPWStr.length() != 0){
            returnEmpty = true;
        }

        return returnEmpty;
    }

    //회원가입한 아이디와 비밀번호 같은지 검사
    public boolean idEqualsPwCheck(){

        returnEquals = false;

        //저장된 쉐어드에서 데이터 불러오기
        // -> 쉐어드에서 추출한 데이터 id값과 pw값 같은지 검사
        prefDataCheck();

        return returnEquals;
    }

    //저장된 쉐어드에서 데이터 불러오기
    public void prefDataCheck(){

        //shared 에 SHAREDPREF_JSON_KEY 데이터가 있다면
        if(pref.contains(SHAREDPREF_JSON_KEY) == true){

            String prefStrData = pref.getString(SHAREDPREF_JSON_KEY, "");

            //jsonArray 생성 -> jsonArray(쉐어드에 저장되어있던 데이터 -> Str 으로 변환한 형태)
            try {
                userArray = new JSONArray(prefStrData);

                for(int i=0; i < userArray.length() ; i++){

                    userObject = new JSONObject();
                    userObject = userArray.getJSONObject(i);

                    //쉐어드에서 추출한 가입 id 값
                    prefJoinIDStr = userObject.get("userID").toString();
                    makeLog("userObject.get(\"userID\");"+prefJoinIDStr);    //aaaa11, bbbb222, aaaa111

                    //가입한 아이디와 입력한 아이디 같으면
                    if(loginIDStr.equals(prefJoinIDStr)){
                        prefJoinPWStr = userObject.get("userPW").toString();
                        makeLog("userObject.get(\"userPW\");"+prefJoinPWStr);

                        if(loginPWStr.equals(prefJoinPWStr)){
                            makeToast("로그인에 성공하였습니다.");
                            returnEquals = true;
                        }else{
                            makeToast("비밀번호가 틀렸습니다. 다시 입력해주세요.");
                        }
                        break;
                    }

//                    else {
//                        makeToast("해당 아이디가 존재하지 않습니다.");    //문제 : for문 도는 동안 계속 호출됨..
//                    }
                }//for문 끝

                //for문 다 돌렸는데 가입한 아이디 중 입력한 아이디가 없을 경우 toast 메세지 출력
                if(!loginIDStr.equals(prefJoinIDStr)){
                    makeToast("해당 아이디가 존재하지 않습니다.");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{
            //shared 에 SHAREDPREF_JSON_KEY 데이터가 없다면
            makeToast("가입되지 않은 아이디입니다.");
        }
    }

    //자동로그인 체크 여부
    public void autoLoginCheck(){

        //체크박스 체크된 경우
        if(btnAutoLogin.isChecked()){
            editor.putString("autoLoginID", loginIDStr);
            editor.putString("autoLoginPW", loginPWStr);
            editor.putString("autoLoginName", prefJoinName);
        }else{
        //체크 해제한 경우
//            editor.remove("autoLoginID");
            editor.putString("autoLoginID", "");
            editor.putString("autoLoginPW", "");
            editor.putString("autoLoginName", "");
        }

//        editor.commit();    //최종반영

    }

    //자동로그인 여부에 따라 -> 바로 홈화면으로 진입할 수도 있음
    @Override
    protected void onResume() {
        super.onResume();

        String prefAutoIDStr = pref.getString("autoLoginID", "");
        String prefAutPWStr = pref.getString("autoLoginPW", "");
        String prefAutNameStr = pref.getString("autoLoginName", "");

        if(prefAutoIDStr != "" && prefAutPWStr != ""){
            makeToast(prefAutNameStr+"님 자동로그인 되었습니다.");
            //홈 화면으로 바로 전환
            Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }
}
