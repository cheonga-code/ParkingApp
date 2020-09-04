package com.example.parkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class ViewActivity extends Base2Activity {

    //shared -> 저장되있는 불러올 키
    String SHAREDPREF_JSON_KEY = "memoList";
    //shared 정의
    SharedPreferences pref;
    //JSONArray 정의
    JSONArray viewArray;
    //JSONObject 정의
    JSONObject viewObject;
    //JSONArray 배열 안 객체 중 마지막 index 값
    int lastIndex;

    TextView viewTitleTv, viewCntTv, viewDateTv, viewGpsTv;
    EditText viewTitleEt, viewCntEt;
    ImageView viewImg;
    Button btnMemoEdit;

    //쉐어드에 저장된 데이터 String 값으로 받아오기
    String viewTitleStr, viewDateStr, viewGpsStr, viewCntStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        //id 연결
        viewImg = (ImageView)findViewById(R.id.viewImg);
        btnMemoEdit = (Button)findViewById(R.id.btnMemoEdit);
        //EditText
        viewTitleEt = (EditText)findViewById(R.id.viewTitleEt);
        viewCntEt = (EditText)findViewById(R.id.viewCntEt);
        //TextView
        viewTitleTv = (TextView)findViewById(R.id.viewTitleTv);
        viewCntTv = (TextView)findViewById(R.id.viewCntTv);
        viewDateTv = (TextView) findViewById(R.id.viewDateTv);
        viewGpsTv = (TextView)findViewById(R.id.viewGpsTv);

        // shared 정의
        pref = getSharedPreferences(SHARED_NAME, SHARED_MODE);

        // shared에 저장된 데이터 -> jsonArray형태로 변환
        String prefStrData = pref.getString(SHAREDPREF_JSON_KEY,"");

        //JSONArray 에 String형 prefStrData 데이터 넣기
        try {
            viewArray = new JSONArray(prefStrData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //viewArray 배열에서 마지막 index 값 구하기
        lastIndex = viewArray.length() -1;
        //viewObject 생성
        viewObject = new JSONObject();

        try {
            //viewArray 배열의 마지막 인덱스에 해당하는 JSON 객체를 viewObject 에 담기
            viewObject = viewArray.getJSONObject(lastIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String testStr = viewObject.toString();
        Log.d("ViewActivity","memoList 마지막 index 데이터 : "+testStr);

//        // String 값으로 받아오기
//        viewTitleStr = pref.getString("memoTitle", "");
//        viewCntStr = pref.getString("memoCnt", "");
//        viewDateStr = pref.getString("memoDate", "");
//        viewGpsStr = pref.getString("memoGps", "");

        //viewObject 에서 값 뽑아서 String 변수에 넣기
        try {
            viewTitleStr = viewObject.getString("memoTitle");
            viewCntStr = viewObject.getString("memoCnt");
            viewDateStr = viewObject.getString("memoDate");
            viewGpsStr = viewObject.getString("memoGps");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // TextView 에 String 값 셋팅
        viewTitleTv.setText(viewTitleStr);
        viewCntTv.setText(viewCntStr);
        viewDateTv.setText(viewDateStr);
        viewGpsTv.setText(viewGpsStr);

        //btnMemoEdit 클릭시 이벤트 -> 메모 수정
        btnMemoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // editText 얻은 데이터 -> String 변수에 담기
                String editTitleStr = viewTitleEt.getText().toString();
                String editCntStr = viewCntEt.getText().toString();

                // editor 정의
                SharedPreferences.Editor editor = pref.edit();

                // editor 에 데이터 저장
//                editor.putString("memoTitle", editTitleStr);
//                editor.putString("memoCnt", editCntStr);

                //test 중 수정된 데이터의 String 값 -> viewObject 에 덮어 씌우기
                Log.d("ViewActivity","수정데이터 넣기 전 viewObject : "+viewObject);
                Log.d("ViewActivity","수정데이터 넣기 전 viewArray : "+viewArray);

                //마지막 index json객체 삭제
                viewArray.remove(lastIndex);
                Log.d("ViewActivity","마지막객체 삭제 후 viewArray : "+viewArray);

                try {
                    viewObject.put("memoTitle",editTitleStr);
                    viewObject.put("memoCnt",editCntStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("ViewActivity","수정데이터 넣은 후 viewObject : "+viewObject);
                //jsonArray 에 수정된 jsonObject 데이터 덮어 씌우기 (put 넣기)
                viewArray.put(viewObject);
                Log.d("ViewActivity","수정데이터 넣은 후 viewArray : "+viewArray);
                //jsonArray 데이터를 String 형변환
                String jsonArrayStrData = viewArray.toString();
                //jsonArray 데이터를 editor 에 추가
                editor.putString(SHAREDPREF_JSON_KEY, jsonArrayStrData);
                //editor 에 최종 적용
                editor.commit();

                //홈 화면으로 되돌아가기
                Intent homeIntent = new Intent(ViewActivity.this, HomeActivity.class);
                startActivity(homeIntent);

            }
        });

    }

    //viewTitleTvClicked 클릭시
    public void viewTitleTvClicked(View view) {

        ViewSwitcher viewTitleSwitcher = (ViewSwitcher)findViewById(R.id.viewTitleSwitcher);
        viewTitleSwitcher.showNext();
        viewTitleEt.setText(viewTitleStr);
    }

    //viewCntTvClicked 클릭시
    public void viewCntTvClicked(View view) {

        ViewSwitcher viewCntSwitcher = (ViewSwitcher)findViewById(R.id.viewCntSwitcher);
        viewCntSwitcher.showNext();
        viewCntEt.setText(viewCntStr);
    }

}










