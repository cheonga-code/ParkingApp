package com.example.parkingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class MemolistActivity extends BaseActivity {

//    static private String SHARED_NAME = "shared_file";
//    static private int SHARED_MODE = MODE_PRIVATE;

    TextView recyMemoNullTv;    //메모리스트 null일때 보일 text
    RecyclerView recyMemolist = null;
    MemolistAdapter mAdapter;
    MemolistAdapter kAdapter;   //키워드 관련 어댑터

    //shared -> 저장되있는 불러올 키
    String SHAREDPREF_JSON_KEY = "memoList";
    //shared 정의
    SharedPreferences pref;
    //JSONArray 정의
    JSONArray memoArray, reSaveArray;
    //JSONObject 정의
    JSONObject memoObject, reSaveObject;

    static final int DETAIL_REQUEST = 1;  // The request code
    //클릭한 아이템 위치(순번?)
    MemolistVO itemNum;
    //detailActivity 에서 넘겨받은 데이터
    String detailIntentTitleStr, detailIntentCntStr, detailIntentDateStr, detailIntentGpsStr, detailIntentSaveTimeStr, detailIntentImgPathStr;

    SearchView memoSearchBar;       //검색바
    String inputStr = "";           //검색바에 입력한 지역명 텍스트
    String searchNullStr = "검색한 키워드에 해당하는 메모가 없습니다.";  //검색한 데이터가 없을때 나타내줄 텍스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_memolist);

        //리사이클러뷰 Null 값일 때 처리
        recyMemoNullTv = (TextView)findViewById(R.id.recyMemoNullTv);

        //리사이클러뷰 데이터 초기화
        recyInit();

        //아이템 클릭 이벤트
        mAdapter.setOnItemClickListener(new MemolistAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(RecyclerView.ViewHolder holder, View view, int position) {

                //아이템 위치(순번?)
                itemNum = mAdapter.getItem(position);

                makeLog("아이템 선택됨 : " + itemNum.getMemoItemTitleStr());
                makeLog("onClick(position) : "+position);

                //item 에 담겨있는 데이터 받아서 -> string 변수에 넣기
                String getItemTitleStr = itemNum.getMemoItemTitleStr();
                String getItemCntStr = itemNum.getMemoItemCntStr();
                String getItemDateStr = itemNum.getMemoItemDateStr();
                String getItemGpsStr = itemNum.getMemoItemGpsStr();
                String getItemSaveTimeStr = itemNum.getMemoItemSaveTimeStr();
                String getItemImgPathStr = itemNum.getMemoItemImgPathStr();

                //상세화면 DetailActivity 인텐트 띄우기
                Intent detailIntent = new Intent(MemolistActivity.this , DetailActivity.class);
                detailIntent.putExtra("저장된 메모제목", getItemTitleStr);
                detailIntent.putExtra("저장된 메모내용", getItemCntStr);
                detailIntent.putExtra("저장된 메모날짜", getItemDateStr);
                detailIntent.putExtra("저장된 메모위치", getItemGpsStr);
                detailIntent.putExtra("저장된 메모시간", getItemSaveTimeStr);
                detailIntent.putExtra("저장된 메모사진", getItemImgPathStr);;
                makeLog("\n리스트 Acty에서 상세 Acty에 보낼 메모데이터  : "+getItemTitleStr+"/"+getItemCntStr+"/"+getItemDateStr+"/"+getItemGpsStr+"/"+getItemSaveTimeStr+"/"+getItemImgPathStr);
                startActivityForResult(detailIntent, DETAIL_REQUEST);

            }
        });

        //메모리스트 검색시 관련
        memoSearchBar = (SearchView)findViewById(R.id.memoSearchBar);
        memoSearchBar.setIconifiedByDefault(false);  //검색바 펼쳐진 상태에서 시작 설정
//        CharSequence squery = prkSearchBar.getQuery();

        //SearchView 에 text 를 검색하거나 변경되는 경우 호출
        memoSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                makeToast("검색버튼클릭 : "+query);
                inputStr = query;   //입력한 글자
                makeLog("검색한 글자 : "+inputStr);
                makeLog("검색버튼 누름");

                //메모 키워드 찾기
                findMemoKeyword(inputStr);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                makeToast("입력하고있는단어 : "+newText);
                makeLog("입력하고있는단어 : "+newText);

                if(newText.equals("")){
                    makeLog("입력하고 있는단어 null이다 !!");

                    //리사이클러뷰가 GONE 인 상태라면 -> 보이게 하기
                    if(recyMemolist.getVisibility() == View.GONE){

                        makeLog("리사이클러뷰 안보여!!!");
                        recyMemoNullTv.setVisibility(View.GONE);     //Null텍스트 GONE
                        recyMemolist.setVisibility(View.VISIBLE);    //리사이클러뷰 VISIBLE
                        makeLog("리사이클러뷰 보이게 했음!!!");
                    }

                    makeLog("리사이클러뷰 mAdapter 셋팅 !!");
                    //리사이클러뷰에 원래 어뎁터 셋팅
                    recyMemolist.setAdapter(mAdapter);
                }

                return false;
            }
        });

    }

    //리사이클러뷰 초기화 관련 메소드
    private void recyInit(){

        //리사이클러뷰 id 연결 -> 지금은 껍데기 일뿐..
        recyMemolist = (RecyclerView)findViewById(R.id.recyMemolist);

//        recyMemolist.setHasFixedSize(true);

        //레이아웃 매니저 객체 생성 -> 어떤 방향으로 보이게 할것인지 LinearLayoutManager 를 통해 선언할 수 있음 -> 세로방향&가로방향
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //리사이클러뷰에 레이아웃 메니저 설정
        recyMemolist.setLayoutManager(layoutManager);

        //어댑터 객체 생성
        mAdapter = new MemolistAdapter(this);

        //쉐어드에서 데이터 불어와서 mAdapter에 데이터 추가하기
        memoPrefIntoJson();

//        //데이터 임시로 생성
//        mAdapter.addItem(new MemolistVO("메모제목1","메모내용1","메모날짜1","메모위치1"));
//        mAdapter.addItem(new MemolistVO("메모제목2","메모내용2","메모날짜2","메모위치2"));
//        mAdapter.addItem(new MemolistVO("메모제목3","메모내용3","메모날짜3","메모위치3"));

        //리사이클러뷰에 어댑터 설정
        recyMemolist.setAdapter(mAdapter);

    }

    //쉐어드에 저장된 데이터 불어와서 -> JSONArray -> JSONObject 형태로 변환
    private void memoPrefIntoJson() {

        pref = getSharedPreferences(SHARED_NAME, SHARED_MODE);

        String prefStrData = pref.getString(SHAREDPREF_JSON_KEY,"");

        //쉐어드 데이터 KEY 값이 존재하지 않을 때 처리
        if(!pref.contains(SHAREDPREF_JSON_KEY)){
            //recyNullTv 보이게 하기
            recyMemoNullTv.setVisibility(View.VISIBLE);
            recyMemolist.setVisibility(View.GONE);
        }else{
            recyMemoNullTv.setVisibility(View.GONE);
        }

        //JSONArray 에 String형 prefStrData 데이터 넣기
        try {
            memoArray = new JSONArray(prefStrData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //쉐어드 데이터 JSONArray 값이 null 일 때 처리
        if(memoArray.toString().equals("[]")){
            //recyNullTv 보이게 하기
            recyMemoNullTv.setVisibility(View.VISIBLE);
            recyMemolist.setVisibility(View.GONE);
        }else{
            recyMemoNullTv.setVisibility(View.GONE);
        }

        //for문을 돌려서 JSONArray -> JSONObject 형태로 뽑아주기 -> mAdapter.addItem 하기
        for(int i=0; i < memoArray.length(); i++){

            try {
                memoObject = memoArray.getJSONObject(i);

                String memoTitleData = memoObject.getString("memoTitle");
                String memoCntData = memoObject.getString("memoCnt");
                String memoDateData = memoObject.getString("memoDate");
                String memoGpseData = memoObject.getString("memoGps");
                String memoSaveTimeData = memoObject.getString("memoSaveTime");
                String memoImgPathData = memoObject.getString("memoImgPath");

                //mAdapter에 데이터 추가
                mAdapter.addItem(new MemolistVO(memoTitleData, memoCntData, memoDateData, memoGpseData, memoSaveTimeData, memoImgPathData));
//                mAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        // adapter의 값이 변경되었다는 것을 알려줍니다.
//        adapter.notifyDataSetChanged();
//        //notifyDataSetChanged()를 호출하지 않으면 data가 노출되지 않습니다.
//        //단, recyclerView.setAdapter() 함수가 data를 추가시켜준 뒤에 호출되었다면 data는 정상적으로 노출됩니다.

    }

    //데이터 저장
    @Override
    public void onPause() {
        super.onPause();

        memoJsonIntoPref();

    }

    //ArrayList -> JSONArray 변환 -> shared에 저장
    public void memoJsonIntoPref(){
        //editor 정의
        SharedPreferences.Editor editor = pref.edit();

        //쉐어드 안에 키 "memoList" 안의 데이터 모두 삭제
        editor.remove(SHAREDPREF_JSON_KEY); //shared_file.xml 파일 안에 memoList 태그 자체가 사라짐
        editor.apply();

        makeLog("다시 쉐어드에 저장/ arraylist.size() : "+mAdapter.items.size());

        //JSONArray&JSONObject 정의
        reSaveArray = new JSONArray();
//        reSaveObject = new JSONObject();

        // mAdapter.items.size() -> arraylist.size() 배열 사이즈
        for(int i=0; i < mAdapter.items.size() ; i++){

            try {
                reSaveObject = new JSONObject();

                //jsonObject 의 memoTitle, memoCnt, memoDate, memoGps 에 데이터 넣기
                reSaveObject.put("memoTitle", mAdapter.items.get(i).getMemoItemTitleStr());
                reSaveObject.put("memoCnt", mAdapter.items.get(i).getMemoItemCntStr());
                reSaveObject.put("memoDate", mAdapter.items.get(i).getMemoItemDateStr());
                reSaveObject.put("memoGps", mAdapter.items.get(i).getMemoItemGpsStr());
                reSaveObject.put("memoSaveTime", mAdapter.items.get(i).getMemoItemSaveTimeStr());
                reSaveObject.put("memoImgPath", mAdapter.items.get(i).getMemoItemImgPathStr());

                makeLog("다시 쉐어드에 저장/ reSaveObject : "+reSaveObject.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //jsonArray 에 jsonObject 추가
            reSaveArray.put(reSaveObject);
        }

        makeLog("다시 쉐어드에 저장/ reSaveArray : "+reSaveArray.toString());

        //memoArray -> String 형변환
        String newArrayStrData = reSaveArray.toString();

        //키 "memoList" 새롭게 데이터 추가
        editor.putString(SHAREDPREF_JSON_KEY, newArrayStrData);

        editor.apply();    //쉐어드 최종 반영
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == DETAIL_REQUEST && resultCode == RESULT_OK){

            //detailActivity 넘겨받은 수정할 메모 데이터
            detailIntentTitleStr = data.getStringExtra("수정할 메모제목");
            detailIntentCntStr = data.getStringExtra("수정할 메모내용");
            detailIntentDateStr = data.getStringExtra("수정할 메모날짜");
            detailIntentGpsStr = data.getStringExtra("수정할 메모위치");
            detailIntentSaveTimeStr = data.getStringExtra("수정할 메모시간");
            detailIntentImgPathStr = data.getStringExtra("수정할 메모사진");

            //클릭한 아이템 데이터에 넘겨받은 데이터 다시 넣기
            itemNum.setMemoItemTitleStr(detailIntentTitleStr);
            itemNum.setMemoItemCntStr(detailIntentCntStr);
            itemNum.setMemoItemDateStr(detailIntentDateStr);
            itemNum.setMemoItemGpsStr(detailIntentGpsStr);
            itemNum.setMemoItemSaveTimeStr(detailIntentSaveTimeStr);
            itemNum.setMemoItemImgPathStr(detailIntentImgPathStr);

            makeLog("\n상세 Acty에서 받은 수정할 메모데이터 : "+detailIntentTitleStr+"/"+detailIntentCntStr+"/"+detailIntentDateStr+"/"+detailIntentGpsStr+"/"+detailIntentSaveTimeStr+"/"+detailIntentImgPathStr);

            mAdapter.notifyDataSetChanged();

        }
    }

    //키워드 검색시 메소드
    public void findMemoKeyword(String inputStr){

        makeLog("키워드 검색 시작합니다..");
        makeLog("입력한 데이터 : "+inputStr);
        makeLog("mAdapter.items.size() : "+mAdapter.items.size());

        //어댑터 객체 다시 생성 - 키워드 관련 kAdapter
        kAdapter = new MemolistAdapter(this);

        for(int i=0; i < mAdapter.items.size() ; i++){

            String keyTit = mAdapter.items.get(i).getMemoItemTitleStr();
            String keyCnt = mAdapter.items.get(i).getMemoItemCntStr();
            String keyDate = mAdapter.items.get(i).getMemoItemDateStr();
            String keyGps = mAdapter.items.get(i).getMemoItemGpsStr();

            String keySaveTime = mAdapter.items.get(i).getMemoItemSaveTimeStr();
            String keyImgPath = mAdapter.items.get(i).getMemoItemImgPathStr();

            makeLog("아이템 "+i+"번째 탐색중...");

            //검색한 데이터 inputStr 와 같은 item 들은 -> keywordArrList 에 따로 담기
            if(keyTit.contains(inputStr) || keyCnt.contains(inputStr) || keyDate.contains(inputStr) || keyGps.contains(inputStr)){

                makeLog("입력한 키워드와 일치하는 데이터 발견 !!");

                kAdapter.addItem(new MemolistVO(keyTit,keyCnt,keyDate,keyGps,keySaveTime,keyImgPath));
//                kAdapter.notifyDataSetChanged();
            }

            //mAdapter.items.size 가 되면 for문 끝남
            if(i == mAdapter.items.size()-1){
                break;  //반복문 탈출
            }

        }//for문 끝

        makeLog("키워드 검색 끝났습니다..");
        makeLog("kAdapter.items.size() : "+kAdapter.items.size());

        //키워드로 찾은 데이터가 없을때 처리
        if(kAdapter.items.size() == 0){

            recyMemolist.setVisibility(View.GONE);          //리사이클러뷰 GONE
            recyMemoNullTv.setVisibility(View.VISIBLE);     //Null텍스트 VISIBLE
            recyMemoNullTv.setText(searchNullStr);

        }else{

            //키워드로 찾은 데이터가 있을때 처리
            recyMemoNullTv.setVisibility(View.GONE);     //Null텍스트 GONE
            recyMemolist.setVisibility(View.VISIBLE);    //리사이클러뷰 VISIBLE

            //리사이클러뷰 재 셋팅
            recyMemolist.setAdapter(kAdapter);

        }

    }

    @Override
    int getContentViewId() {
        return R.layout.activity_memolist;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.botNav02;
    }


}
