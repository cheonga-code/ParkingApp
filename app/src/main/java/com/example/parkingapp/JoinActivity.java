package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinActivity extends Base2Activity {

    EditText inputLayoutEtID, inputLayoutEtPW, inputLayoutEtCarNum;  //textInputLayout 관련 변수
    Button btnCheckID, btnJoinOK;
//    btnCheckCarNumber
    String joinNameStr, joinCarNumberStr, joinIDStr, joinPWStr, joinPW2Str, prefGetKEYStr;
    TextInputLayout inputLayoutJoinName, inputLayoutJoinCarNumber, inputLayoutJoinID, inputLayoutJoinPW, inputLayoutJoinPW2;
    TextInputEditText joinNameEt, joinCarNumberEt, joinIDEt, joinPWEt, joinPW2Et;

    //shared -> 저장되있는 불러올 키
    String SHAREDPREF_JSON_KEY = "userList";
    //shared 정의
    SharedPreferences pref;
    //JSONArray 정의
    JSONArray userArray;
    //JSONObject 정의
    JSONObject userObject, checkObject;

    //정규식 검사
        //영문 대소문자 / 숫자 로 이루어진 6~12자리 이내의 암호 정규식
    String regExpID = "^[a-zA-Z0-9]{6,12}$";
        //특수문자 / 문자 / 숫자 포함 형태의 8~15자리 이내의 암호 정규식
    String regExpPW = "^.*(?=^.{8,15}$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$";
        //숫자2+한글+숫자4로 이루어진 암호 정규식 (ex.12조1234)
    String regExpCarNum = "^\\d{2}[가|나|다|라|마|거|너|더|러|머|버|서|어|저|고|노|도|로|모|보|소|오|조|구|누|두|루|무|부|수|우|주|바|사|아|자|허|배|호|하\\x20]\\d{4}/*$";


    //정규식 검사 return value (공백확인, All정규식확인, 각각 정규식확인, 비밀번호확인, 차번호 중복확안, 아이디 중복확인)
    boolean returnEmpty, returnAllValid, returnValid, returnPWConfirm, returnDuplicIDCheck, returnBooleanValue;
//    returnDuplicCarNumCheck

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //id 연결
        joinNameEt = (TextInputEditText) findViewById(R.id.joinName);   //이름
        joinCarNumberEt = (TextInputEditText)findViewById(R.id.joinCarNumber); //차대번호
        joinIDEt = (TextInputEditText)findViewById(R.id.joinID);   //아이디
        joinPWEt = (TextInputEditText)findViewById(R.id.joinPW);   //비밀번호
        joinPW2Et = (TextInputEditText)findViewById(R.id.joinPW2); //비밀번호확인
//        btnCheckCarNumber = (Button)findViewById(R.id.btnCheckCarNumber);   //버튼 - 차번호 중복확인
        btnCheckID = (Button)findViewById(R.id.btnCheckID); //버튼 - 아이디 중복확인
        btnJoinOK = (Button)findViewById(R.id.btnJoinOK);   //버튼 - 회원가입 완료

        //inputLayout id 연결
        inputLayoutJoinName = (TextInputLayout)findViewById(R.id.inputLayoutJoinName);
        inputLayoutJoinCarNumber = (TextInputLayout)findViewById(R.id.inputLayoutJoinCarNumber);
        inputLayoutJoinID = (TextInputLayout)findViewById(R.id.inputLayoutJoinID);
        inputLayoutJoinPW = (TextInputLayout)findViewById(R.id.inputLayoutJoinPW);
        inputLayoutJoinPW2 = (TextInputLayout)findViewById(R.id.inputLayoutJoinPW2);

        //쉐어드에 저장된 데이터 불러오기
        prefIntoJson();

        //inputLayout 관련 메소드
        textInputLayout();

        //버튼 -회원가입 클릭시
        btnJoinOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //editText 에서 텍스트 가져와서 String 형변환
                editTextToString();

                //editText 공백 검사
                if(returnEmpty == true){
                    //editTextEmptyCheck() == true 이면
                    //editText 정규식 검사
                    editTextIsValidate();

                    if(returnAllValid == true){
                        //editTextIsValidate() == true 이면
                        //password 확인 검사
                        passwordConfirmCheck();

                        if(returnPWConfirm == true){
                            //passwordConfirmCheck() == true 이면

                            //중복체크 검사 - 아이디
                            if(returnDuplicIDCheck == true){

                                //중복체크 검사 성공시 - 아이디 && 차번호 둘다 중복 안되있으면
                                //쉐어드로 저장
                                userJsonIntoPref();

                                makeToast("회원가입에 성공했습니다.");

                                //로그인 화면으로 전환
                                Intent loginIntent = getIntent();
                                loginIntent.putExtra("joinID", joinIDEt.getText().toString());    //회원가입한 아이디 joinID 데이터 받아서 -> login 화면 id 에 자동 입력해주기
                                setResult(RESULT_OK, loginIntent);
                                finish();

                                //중복체크 검사 실패시 - 아이디 && 차번호 중 중복인게 있으면
                            }else if(returnDuplicIDCheck == false){
                                makeToast("아이디 중복 확인을 해주세요.");
                            }
                            //중복체크 검사 end
                        }//password 확인 검사 end
                    }//ditText 정규식 검사 end

                }else{
                    //editTextEmptyCheck() == false 이면
                    //editText 공백 검사
                    editTextEmptyCheck();

                }//editText 공백 검사 end

                makeLog("반환값 확인 ->"+" 공백확인 "+returnEmpty+" 정규식확인 "+returnAllValid+" 비밀번호확인 "+returnPWConfirm);
                makeLog("반환값 확인 ->"+" 아이디중복확인 "+returnDuplicIDCheck);

            }
        });
        //btnJoinOK 클릭이벤트 end

//        //버튼 -중복확인 차번호
//        btnCheckCarNumber.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                makeLog("차번호 중복확인 전 returnDuplicCarNumCheck :"+returnDuplicCarNumCheck);
//
//                //editText 에서 텍스트 가져와서 String 형변환
//                editTextToString();
//                //중복체크 검사
//                duplicateCheck("userCarNumber", joinCarNumberStr);
//                //위의 중복검사 리턴값(returnBooleanValue)을 returnDuplicCarNumCheck 에 담기
//                returnDuplicCarNumCheck = returnBooleanValue;
//
//                //중복체크 검사에서 return 값이 true 이면 중복 값 있는것 !!
//                if(returnDuplicCarNumCheck == false){
//                    makeToast("이미 가입된 차량번호가 있습니다.");
//                }else{
//                    makeToast("입력한 차량번호로 가입이 가능합니다.");
//                }
//
//                makeLog("차번호 중복확인 후 returnDuplicCarNumCheck :"+returnDuplicCarNumCheck);
//            }
//        });

        //버튼 -중복확인 아이디
        btnCheckID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makeLog("아이디 중복확인 전 returnDuplicIDCheck :"+returnDuplicIDCheck);

                //editText 에서 텍스트 가져와서 String 형변환
                editTextToString();
                //중복체크 검사
                duplicateCheck("userID", joinIDStr);
                //위의 중복검사 리턴값(returnBooleanValue)을 returnDuplicIDCheck 에 담기
                returnDuplicIDCheck = returnBooleanValue;

                //중복체크 검사에서 return 값이 true 이면 중복 값 있는것 !!
                if(returnDuplicIDCheck == false){
                    makeToast("이미 가입된 아이디가 있습니다.");
                }else{
                    makeToast("입력한 아이디로 가입이 가능합니다.");
                }

                makeLog("아이디 중복확인 후 returnDuplicIDCheck :"+returnDuplicIDCheck);

            }
        });


    }

    //저장된 쉐어드에서 데이터 불러오기
    public void prefIntoJson(){

        //shared 정의
        pref = getSharedPreferences(SHARED_NAME, SHARED_MODE);

        //shared 에 SHAREDPREF_JSON_KEY 데이터가 있다면
        if(pref.contains(SHAREDPREF_JSON_KEY) == true){

            String prefStrData = pref.getString(SHAREDPREF_JSON_KEY, "");

            //jsonArray 생성 -> jsonArray(쉐어드에 저장되어있던 데이터 -> Str 으로 변환한 형태)
            try {
                userArray = new JSONArray(prefStrData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            makeLog("집입시 쉐어드에 저장된 KEY_userList 데이터 불러오기 userArray  : "+userArray);

        }else{

            //데이터가 없다면 JSONArray 새로 생성
            userArray = new JSONArray();
            makeLog("집입시 쉐어드에 저장된 KEY_userList 데이터 없음");
        }
    }

    //editText -> String 형변환
    public void editTextToString(){

        joinNameStr = joinNameEt.getText().toString();
        joinCarNumberStr = joinCarNumberEt.getText().toString();
        joinIDStr = joinIDEt.getText().toString();
        joinPWStr = joinPWEt.getText().toString();
        joinPW2Str = joinPW2Et.getText().toString();

    }

    //editText 공백 검사
    public boolean editTextEmptyCheck(){

        returnEmpty = false;    //공백이 있을시 false 반환

        if(joinNameStr.length() == 0){
            makeToast("이름이 입력되지 않았습니다.");
            joinNameEt.requestFocus();

        }else if(joinCarNumberStr.length() == 0){
            makeToast("차대번호가 입력되지 않았습니다.");
            joinCarNumberEt.requestFocus();

        }else if(joinIDStr.length() == 0){
            makeToast("아이디가 입력되지 않았습니다.");
            joinIDEt.requestFocus();

        }else if(joinPWStr.length() == 0){
            makeToast("비밀번호가 입력되지 않았습니다.");
            joinPWEt.requestFocus();

        }else if(joinPW2Str.length() == 0){
            makeToast("비밀번호확인이 입력되지 않았습니다.");
            joinPW2Et.requestFocus();
        }

        //입력텍스트 모두 공백이 아닐때
        if(joinNameStr.length() != 0 && joinCarNumberStr.length() != 0 && joinIDStr.length() != 0 && joinPWStr.length() != 0 && joinPW2Str.length() != 0){
            returnEmpty = true;
        }

        return returnEmpty;
    }

    //editText 정규식 검사 - 아이디, 비밀번호, 차량번호
    public boolean editTextIsValidate(){

        returnAllValid = false; //정규식 패턴 하나라도 불일치시 false 반환

        if(isValidCheck(regExpID, joinIDStr) == false){
            makeToast("적합하지 않은 아이디 형식입니다.");
        }else if(isValidCheck(regExpPW, joinPWStr) == false){
            makeToast("적합하지 않은 비밀번호 형식입니다.");
        }else if(isValidCheck(regExpCarNum, joinCarNumberStr) == false){
            makeToast("적합하지 않은 차량번호 형식입니다.");
        }

        //정규식 패턴 모두 일치시 true 반환
        if(isValidCheck(regExpID, joinIDStr) == true && isValidCheck(regExpPW, joinPWStr) == true && isValidCheck(regExpCarNum, joinCarNumberStr) == true){
            returnAllValid = true;
        }

        return returnAllValid;
    }

    //정규식 검사 - 아이디, 비밀번호, 차량번호
    public boolean isValidCheck(String regExpStr, String editTextStr){

        returnValid = false;    //패턴 불일치시 false 반환

        Pattern p = Pattern.compile(regExpStr); //regExt 내가 정한 정규식 암호패턴
        Matcher m = p.matcher(editTextStr);     //String data 내가 입력한 텍스트 데이터
        if (m.matches()) {
            returnValid = true; //패턴 일치시 true 반환
        }else{
            returnValid = false;    //패턴 불일치시 false 반환
        }

        return returnValid;

    }

    //password 확인 검사 -> return boolean
    public boolean passwordConfirmCheck(){

        returnPWConfirm = false;    //비밀번호 불일치시 false 반환

        if(!joinPWStr.equals(joinPW2Str)){
            //패스워드 != 패스워드 확인 같지 않으면
            makeToast("비밀번호가 같지 않습니다. 다시 입력해주세요.");
            joinPW2Et.requestFocus();

        }else {

            //비밀번호 와 비밀번호확인이 공백 이면 -> joinPWStr.equals(joinPW2Str) 일치해서 리턴값이 true 나옴
            //그래서 밑의 조건문 추가해줌
            if(joinPWStr.length() != 0 && joinPW2Str.length() != 0){
                returnPWConfirm = true; //비밀번호 일치시 true 반환
            }
        }

        return  returnPWConfirm;
    }

    //user 제이슨 -> 쉐어드에 저장
    public void userJsonIntoPref(){

        //editor 정의
        SharedPreferences.Editor editor = pref.edit();

        //editText -> String 형변환
        editTextToString();

        //JSONObject 정의
        userObject = new JSONObject();

        try {
            userObject.put("userName", joinNameStr);
            userObject.put("userCarNumber", joinCarNumberStr);
            userObject.put("userID", joinIDStr);
            userObject.put("userPW", joinPWStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //JSONArray 에 넣기 (생성은 위에서 이미 했음)
        userArray.put(userObject);

        //JSONArray -> String 형변환
        String userArrayStrData = userArray.toString();

        makeLog("회원가입 성공시 쉐어드 KEY_userList 에 데이터 저장하기 userArray  : "+userArray);

        //쉐어드에 저장
        editor.putString(SHAREDPREF_JSON_KEY, userArrayStrData);
        editor.commit();

    }

    //중복확인 체크
    public boolean duplicateCheck(String prefGetKEY, String inputStr){

        makeLog("inputStr : "+inputStr);

        returnBooleanValue = true;

        //userArray 배열 for문 돌리기
        for(int i=0; i<userArray.length(); i++){

            checkObject = new JSONObject();

            try {
                checkObject = userArray.getJSONObject(i);
                prefGetKEYStr = checkObject.getString(prefGetKEY);
                makeLog("prefGetKEYStr "+i+"번 : "+prefGetKEYStr);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //비교 (prefGetKEY 쉐어드에서 얻어온 데이터 == 내가 입력한 데이터 중복되면) -> true 반환
            if(prefGetKEYStr.equals(inputStr)){
                returnBooleanValue = false;
                makeLog("for문 동안 returnDuplicateCheck "+i+"번 : "+returnBooleanValue);
                break;  //for문 빠져나가기
            }else {
                makeLog("for문 동안 returnDuplicateCheck "+i+"번 : "+returnBooleanValue);
            }
        }
        return returnBooleanValue;
    }

    //TextInputLayout 관련 메소드
    public void textInputLayout(){

        //아이디 입력 카운터 설정
        inputLayoutJoinID.setCounterEnabled(true);
        inputLayoutJoinID.setCounterMaxLength(12);  //아이디는 최대 12자

        //비밀번호 입력 카운터 설정
        inputLayoutJoinPW.setCounterEnabled(true);
        inputLayoutJoinPW.setCounterMaxLength(15);  //비밀번호는 최대 15자

        //에러 메세지 표시
        inputLayoutEtID = inputLayoutJoinID.getEditText();
        inputLayoutEtPW = inputLayoutJoinPW.getEditText();
        inputLayoutEtCarNum = inputLayoutJoinCarNumber.getEditText();

        //에러 메시지 띄우는 메소드 생성
        editTextSetError(inputLayoutEtID, inputLayoutJoinID, regExpID, "6~12자의 영문 대소문자와 숫자로만 입력해주세요.");
        editTextSetError(inputLayoutEtPW, inputLayoutJoinPW, regExpPW, "8~15자의 문자+숫자+특수문자 포함하여 입력해주세요.");
        editTextSetError(inputLayoutEtCarNum, inputLayoutJoinCarNumber, regExpCarNum, "숫자2+한글+숫자4 형태로 입력해주세요. ex.12보1234");
    }

    //에러 메시지 띄우는 메소드 생성
    public void editTextSetError(EditText editText, final TextInputLayout textInputLayout, final String regExp, final String message){

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(!s.toString().matches(regExp)){
                    textInputLayout.setError(message);
                }else{
                    textInputLayout.setError(null);  //null은 에러 메시지를 지워주는 기능
                }

            }
        });
    }

}


