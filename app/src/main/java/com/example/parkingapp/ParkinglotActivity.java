package com.example.parkingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class ParkinglotActivity extends BaseActivity implements OnMapReadyCallback{

    //    TextView testJSON;
    TextView recyPrkNullTv;
    JSONObject prkInfoObject;       //공공데이터 파싱에 필요한 JSONObject
    SearchView prkSearchBar;        //검색바
    LinearLayout prkSearchCnt;      //검색바 컨텐츠
    String beforePrkAdrStr = "";    //이전 주차장 주소
    String inputStr = "";           //검색바에 입력한 지역명 텍스트
    String searchNullStr = "검색한 지역명에 해당하는 데이터가 없습니다.";  //검색한 데이터가 없을때 나타내줄 텍스트

    String prkNameData, prkAdrData, prkRatesData;   //prkArraylist 에 넣을 데이터
    RecyclerView recyPrklist;
    ParkinglotAdapter prkAdapter;

    //구글맵 관련 참조 변수
    private GoogleMap mMap;
    private Marker currentMarker = null;
    private SupportMapFragment mapFragment;
    private Location mCurrentLocatiion;
    private LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private static final int PERMISSION_REQUEST_CODE = 100;    //권한 요청 코드 (onRequestPermissionsResult 에서)
    boolean needRequest = false;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초 = 위치가 Update 되는 주기
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000 * 30; // 위치 획득후 업데이트되는 주기 -> (500 = 0.5초 단위로 화면 갱신됨)

    // 앱을 실행하기 위해 필요한 퍼미션을 정의
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    //Place API 관련 변수
//    List<Marker> previous_marker = null;
    Button btnPlaceSearch;

    // 구글 서버로 부터 받아온 데이터를 저장할 리스트
    ArrayList<Double> lat_list;         //위도값
    ArrayList<Double> lng_list;         //경도값
    ArrayList<String> name_list;        //이름값
    ArrayList<String> vicinity_list;    //대략적인 주소값
    // 지도의 표시한 마커(주변장소표시)를 관리하는 객체를 담을 리스트
    ArrayList<Marker> markers_list;
    // 다이얼로그를 구성하기 위한 배열
    String[] category_name_array={"모두","카페","공원"};
    // types 값 배열
    String[] category_value_array={"all","cafe","park"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_search);

        //테스트 중
//        testJSON = (TextView)findViewById(R.id.testJSON);
        recyPrkNullTv = (TextView) findViewById(R.id.recyPrkNullTv);
        prkSearchBar = (SearchView) findViewById(R.id.prkSearchBar);
        prkSearchCnt = (LinearLayout) findViewById(R.id.prkSearchCnt);
        btnPlaceSearch = (Button) findViewById(R.id.btnPlaceSearch);

        //공공데이터 파싱에 필요한 JSONObject
        prkInfoObject = new JSONObject();

        //검색바 터치시 관련
        prkSearchBar.setIconifiedByDefault(false);  //검색바 펼쳐진 상태에서 시작 설정
//        CharSequence squery = prkSearchBar.getQuery();

        //검색바 내용 초깃값 = 안보이게 설정
        prkSearchCnt.setVisibility(View.GONE);

        makeLog("prkSearchBar.isFocusable() : "+prkSearchBar.isFocusable());
        makeLog("prkSearchBar.isIconified() : "+prkSearchBar.isIconified());


        //SearchView 에 text 를 검색하거나 변경되는 경우 호출
        prkSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                makeToast("검색버튼클릭 : "+query);
                inputStr = query;
                makeLog("검색한 글자 : " + inputStr);
                makeLog("버튼 누름");

                prkSearchCnt.setVisibility(View.VISIBLE);

                //리사이클러뷰 초기셋팅
                recyInit();

                //Async스레드를 시작
                new JsonLoadingTask().execute();

                //검색바 내용 검색시  = 보이게 설정
                prkSearchCnt.setVisibility(View.VISIBLE);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                makeToast("입력하고있는단어 : "+newText);
                return false;
            }
        });

        //서치뷰 x버튼 클릭시 이벤트
        prkSearchBar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //검색바 내용 검색시  = 안보이게 설정
                prkSearchCnt.setVisibility(View.GONE);
//                prkSearchBar.clearFocus();
                return false;
            }
        });

        //구글맵 관련
        //구글맵 권한 허용 관련 start
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)    //정확도를 최우선적으로 고려
                .setInterval(UPDATE_INTERVAL_MS)                        //위치가 Update 되는 주기
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);        //위치 획득후 업데이트되는 주기

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //권한 허용 관련 end

        makeToast("GPS가 켜져있습니다.");

        //맵을 빌드함
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        //Place API 관련

        lat_list=new ArrayList<>();
        lng_list=new ArrayList<>();
        name_list=new ArrayList<>();
        vicinity_list=new ArrayList<>();
        markers_list=new ArrayList<>();

        btnPlaceSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeToast("눌리고있음");
//                showPlaceInformation(currentPosition);
                showCategoryList();
            }
        });

    }

    //리사이클러뷰 셋팅
    public void recyInit(){

        //id 연결
        recyPrklist = (RecyclerView)findViewById(R.id.recyPrklist);
        //레이아웃 매니저 객체 생성
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //리사이클러뷰에 레이아웃 매니저 설정
        recyPrklist.setLayoutManager(layoutManager);
        //어댑터 객체 생성
        prkAdapter = new ParkinglotAdapter(this);

        //데이터 넣기
//        prkAdapter.addItem(new ParkinglotVO(String prkItemNameStr, String prkItemAdrStr, String prkItemRatesStr));

//        //리사이클러뷰에 어댑터 설정
//        recyPrklist.setAdapter(prkAdapter);
    }

    //AsyncTask 구현
    private class JsonLoadingTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strs) {
            return getJsonText();
        } // doInBackground : 백그라운드 작업을 진행한다.

        @Override
        protected void onPostExecute(String result) {

//            //setText 하기
//            testJSON.setText(result);
            //액티비티 진입시 -> XML 기본값 셋팅은 리사이클러뷰 GONE 상태
            //데이터가 있을 때 처리
            recyPrkNullTv.setVisibility(View.GONE);     //Null텍스트 GONE
            recyPrklist.setVisibility(View.VISIBLE);    //리사이클러뷰 VISIBLE
            //리사이클러뷰에 어댑터 설정
            recyPrklist.setAdapter(prkAdapter);
            prkAdapter.notifyDataSetChanged();

            //데이터가 없을 때 처리
            if(result.equals("0"+searchNullStr)){
//                makeLog("로그 탔나 확인1");
                recyPrklist.setVisibility(View.GONE);          //리사이클러뷰 GONE
                recyPrkNullTv.setVisibility(View.VISIBLE);     //Null텍스트 VISIBLE
                recyPrkNullTv.setText(searchNullStr);
//                makeLog("로그 탔나 확인2");
            }

        } // onPostExecute : 백그라운드 작업이 끝난 후 UI 작업을 진행한다.

    }
    // JsonLoadingTask

    //getJsonText() 메소드
    public String getJsonText() {

        StringBuffer sb = new StringBuffer("0");

        makeLog("sb 시작 : "+sb);

        try {

            //주어진 URL 문서의 내용을 문자열로 얻는다 -> getStringFromURI() 메소드 생성
            String startIndex = "1";
            String endIndex = "7";
//            String location = "강남";
            String jsonPage = getStringFromURI(
                    "http://openapi.seoul.go.kr:8088/4252445873776c6438394c4b66534b/json/GetParkInfo/"
                            +startIndex+"/"
                            +endIndex+"/"
                            +inputStr);

            //읽어들인 JSON 포맷의 데이터를 JSON 객체로 변환
            JSONObject jsonObject = new JSONObject(jsonPage);
            makeLog("jsonObject : " + jsonObject);

            //GetParkInfo 객체 얻어오기
            prkInfoObject = jsonObject.getJSONObject("GetParkInfo");
            makeLog("prkInfoObject : " + prkInfoObject);

            //"row" 값은 배열로 구성 되어있으므로 JSON 배열생성
            JSONArray jsonArray = prkInfoObject.getJSONArray("row");
            makeLog("jsonArray : " + jsonArray);

            //배열 크기만큼 반복하면서 주차장이름&주소 의 값을 추출함 -> "prkplceNm" & "lnmadr"
            for (int i = 0; i < jsonArray.length(); i++) {

                //i번째 배열 할당
                jsonObject = jsonArray.getJSONObject(i);

                //주차장 이름 & 주소 값 추출
                String PARKING_NAME_STR = jsonObject.getString("PARKING_NAME"); //주차장 이름
                String ADDR_STR = jsonObject.getString("ADDR");                 //주차장 주소
                String RATES_STR = jsonObject.getString("RATES");           //기본 주차 요금
//                String TIME_RATE_STR = jsonObject.getString("TIME_RATE");   //기본 주차 시간(분 단위)
//                String ADD_RATES = jsonObject.getString("ADD_RATES");       //추가 단위 요금
//                String ADD_TIME_RATE_STR = jsonObject.getString("ADD_TIME_RATE");   //추가 단위 시간(분 단위)

                makeLog("이전 주차장 주소 : "+beforePrkAdrStr);

                //이전 주차장 주소 와 현재 주차장 주소 비교
                if(!beforePrkAdrStr.equals(ADDR_STR)){
                    //같지 않으면 추가

                    beforePrkAdrStr = ADDR_STR; //지금 주차장으로 데이터 갱신
                    makeLog("주차장 주소 : " + ADDR_STR);

                    //StringBuffer 출력할 값을 저장
                    sb.append("주차장 이름 : " + PARKING_NAME_STR + "\n");
                    sb.append("주차장 주소 : " + ADDR_STR + "\n");
                    sb.append("주차장 기본 요금 : " + RATES_STR + "\n");

                    //ParkinglotVO 에 데이터 넣기
                    prkAdapter.addItem(new ParkinglotVO(PARKING_NAME_STR, ADDR_STR, RATES_STR));

                }else{
                    //같으면 for문 빠져나가기
                    makeLog("=====주차장 주소 같아서 for문 break 함=====");
                    break;
                }

            }//for문 끝

            //다시 빈값 만들어주기
            //이유는? 재 검색시 이전 데이터 있으면 충돌하는 경우 생김
            beforePrkAdrStr = "";

        } catch (Exception e) {
            e.printStackTrace();
        }

        makeLog("sb 끝 : \n"+sb);

        //데이터 없을때 처리
        if(sb == null || sb.toString().equals("0")){
            //searchNullStr = "검색한 지역명에 해당하는 데이터가 없습니다."
            sb.append(searchNullStr);
        }

        return sb.toString();
    }

    // getStringFromUrl : 주어진 URL의 문서의 내용을 문자열로 반환
    public String getStringFromURI(String pUri){

        BufferedReader bufferedReader = null;
        HttpURLConnection urlConnection = null;

        //읽어온 데이터를 저장할 StringBuffer객체 생성
        StringBuffer page = new StringBuffer();

        try {
            //[URL 지정과 접속]
            //웹서버 URL 지정
            URL url = new URL(pUri);

            //웹서버 URL 접속
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream contentStream = urlConnection.getInputStream();

            //[웹문서 소스를 버퍼에 저장]
            //데이터를 버퍼에 기록
            bufferedReader = new BufferedReader(new InputStreamReader(contentStream, "UTF-8"));
            makeLog("bufferedReader : "+bufferedReader.toString());

            String line = null;
//            String page = "";

            //버퍼의 웹문서 소스를 줄 단위로 읽어(line), page 에 저장
            while((line = bufferedReader.readLine())!=null){
                makeLog("line : "+line);
//                page += line;
                page.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //자원해제
            try {
                bufferedReader.close();
                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        makeLog("page : "+page.toString());
        return page.toString();
    }

    //구글맵을 띄울준비가 됬으면 자동호출된다 ->  null -> googleMap 객체를 받으면 준비가 된것으로 판단
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //GPS를 찾지 못하는 장소에 있을 경우 지도의 초기 위치 서울로 설정 (런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전)
        setDefaultLocation();

        //런타임 퍼미션 처리
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED
                &&  hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates(); // 3. 위치 업데이트 시작

        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                makeToast("이 앱을 실행하려면 위치 접근 권한이 필요합니다.");

                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);

            }
        }

        //
        mMap.setMyLocationEnabled(true);    //현재위치 버튼
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                makeLog("onMapClick :");
                //지도 클릭시 - 검색바 내용  = 안보이게 설정
                prkSearchCnt.setVisibility(View.GONE);
                prkSearchBar.clearFocus();
            }
        });


    }

    //위치콜백
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                makeLog("onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocatiion = location;
            }
        }
    };

    //위치 업데이트 메소드
    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {
            makeLog("startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();

        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                makeLog("startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            makeLog("startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    //마커 하나 찍기
    public void oneMarker() {
        // 서울 여의도에 대한 위치 설정
        LatLng seoul = new LatLng(37.52487, 126.92723);

        // 구글 맵에 표시할 마커에 대한 옵션 설정  (알파는 좌표의 투명도이다.)
        MarkerOptions makerOptions = new MarkerOptions();
        makerOptions
                .position(seoul)
                .title("원하는 위치(위도, 경도)에 마커를 표시했습니다.")
                .snippet("여기는 여의도인거같네여!!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .alpha(0.5f);

        // 마커를 생성한다. showInfoWindow를 쓰면 처음부터 마커에 상세정보가 뜨게한다. (안쓰면 마커눌러야뜸)
        mMap.addMarker(makerOptions); //.showInfoWindow();

        //정보창 클릭 리스너
        mMap.setOnInfoWindowClickListener(infoWindowClickListener);

        //마커 클릭 리스너
        mMap.setOnMarkerClickListener(markerClickListener);

        //카메라를 여의도 위치로 옮긴다.
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));
        //처음 줌 레벨 설정 (해당좌표=>서울, 줌레벨(16)을 매개변수로 넣으면 된다.) (위에 코드대신 사용가능)(중첩되면 이걸 우선시하는듯)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 16));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "눌렀습니다!!", Toast.LENGTH_LONG);
                return false;
            }
        });

    }


    //정보창 클릭 리스너
    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            String markerId = marker.getId();
            Toast.makeText(getApplicationContext(), "정보창 클릭 Marker ID : "+markerId, Toast.LENGTH_SHORT).show();
        }
    };

    //마커 클릭 리스너
    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            String markerId = marker.getId();
            //선택한 타겟위치
            LatLng location = marker.getPosition();
            Toast.makeText(getApplicationContext(), "마커 클릭 Marker ID : "+markerId+"("+location.latitude+" "+location.longitude+")", Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    //현재 주소 얻어오는 메소드
    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    //
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //현재 위치 셋팅 메소드
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

    }


    //지도의 초기위치를 서울로 이동하는 메소드
    public void setDefaultLocation() {

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }

    //런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;

    }

    //ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSION_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if(check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();

            }else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다. 2가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    makeToast("퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ");
                }
            }

        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        makeLog("onActivityResult : GPS 활성화 되있음");
                        needRequest = true;

                        return;
                    }
                }
                break;
        }
    }

    //여기서 부턴 Place API 관련 메소드

    //주변 카테고리 리스트
    private void showCategoryList() {

        getNearbyPlace();
//
//        // 카테고리를 선택 할 수 있는 리스트를 띄운다.
//        AlertDialog.Builder builder=new AlertDialog.Builder(this);
//        builder.setTitle("장소 타입 선택");
//        ArrayAdapter<String> adapter= new ArrayAdapter<String>(
//                this,android.R.layout.simple_list_item_1,category_name_array
//        );
//        DialogListener listener=new DialogListener();
//        builder.setAdapter(adapter,listener);
//        builder.setNegativeButton("취소",null);
//        builder.show();
    }
    // 다이얼로그의 리스너
//    class DialogListener implements DialogInterface.OnClickListener{
//
//        @Override
//        public void onClick(DialogInterface dialogInterface, int i) {
//            // 사용자가 선택한 항목 인덱스번째의 type 값을 가져온다.
//            String type=category_value_array[i];
//            // 주변 정보를 가져온다
//            getNearbyPlace(type);
//        }
//    }
    //주변 정보 가져오기
//    public void getNearbyPlace(String type_keyword){
//        NetworkThread thread = new NetworkThread(type_keyword);
//        thread.start();
//    }

    public void getNearbyPlace(){
        NetworkThread thread = new NetworkThread();
        thread.start();
    }

    //주변 정보 가져오는 스레드
    class NetworkThread extends Thread{

//        String type_keyword;
//
//        public NetworkThread(String type_keyword){
//            this.type_keyword=type_keyword;
//        }

        @Override
        public void run() {
            try{
                //데이터를 담아놓을 리스트를 초기화한다.
                lat_list.clear();
                lng_list.clear();
                name_list.clear();
                vicinity_list.clear();

//                if(type_keyword!=null && type_keyword.equals("all")==false){
//                    type = "&types="+type_keyword;
//                }

                // 접속할 페이지 주소
                String site="https://maps.googleapis.com/maps/api/place/nearbysearch/json";
                site+="?location="+location.getLatitude()+","+location.getLongitude()
                        +"&radius=1500"
//                        +"&type=restaurant&keyword=cruise"
                        +"&type=parking"
                        +"&language=ko"
//                        +type
                        +"&key=AIzaSyCzRswuFQXbNRfF4wKKqHu9jBoftMPeMgg";

                makeLog("site 주소 : "+site);

                // 접속
                URL url = new URL(site);
                URLConnection conn = url.openConnection();
                // 스트림 추출
                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is,"utf-8");
                BufferedReader br = new BufferedReader(isr);
                String str = null;
                StringBuffer buf = new StringBuffer();
                // 읽어온다
                do{
                    str = br.readLine();
                    if(str != null){
                        buf.append(str);
                    }
                }while(str != null);

                String rec_data = buf.toString();

                // JSON 데이터 분석
                JSONObject root=new JSONObject(rec_data);
                makeLog("root : "+root);
                //status 값을 추출한다.
                String status = root.getString("status");
                makeLog("status : "+status);
                // 가져온 값이 있을 경우에 지도에 표시한다.
                if(status.equals("OK")){
                    //results 배열을 가져온다
                    JSONArray resultsArray = root.getJSONArray("results");
                    makeLog("resultsArray : "+resultsArray);
                    // 개수만큼 반복한다.
                    for(int i=0; i<resultsArray.length() ; i++){
                        // 객체를 추출한다.(장소하나의 정보)
                        JSONObject obj1 = resultsArray.getJSONObject(i);
                        // 위도 경도 추출
                        JSONObject geometry = obj1.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");
                        makeLog("location : "+ lat+"위도 / "+ lng+"경도");
                        // 장소 이름 추출
                        String name = obj1.getString("name");
                        makeLog("name : "+name);
                        // 대략적인 주소 추출
                        String vicinity = obj1.getString("vicinity");
                        makeLog("vicinity : "+vicinity);
                        // 데이터를 담는다.
                        lat_list.add(lat);
                        lng_list.add(lng);
                        name_list.add(name);
                        vicinity_list.add(vicinity);
                    }
                    showMarker();
                }else{
                    makeToast("가져온 데이터가 없습니다.");
                }
            }catch (Exception e){e.printStackTrace();}
        }
    }

    // 지도에 마커를 표시한다
    public void showMarker(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 지도에 마커를 표시한다.
                // 지도에 표시되어있는 마커를 모두 제거한다.
                for(Marker marker : markers_list){
                    marker.remove();
                }
                markers_list.clear();
                // 가져온 데이터의 수 만큼 마커 객체를 만들어 표시한다.
                for(int i= 0 ; i< lat_list.size() ; i++){
                    // 값 추출
                    double lat = lat_list.get(i);
                    double lng = lng_list.get(i);
                    String name = name_list.get(i);
                    String vicinity = vicinity_list.get(i);
                    // 생성할 마커의 정보를 가지고 있는 객체를 생성
                    MarkerOptions options = new MarkerOptions();
                    // 위치설정
                    LatLng pos = new LatLng(lat,lng);
                    options.position(pos);
                    // 말풍선이 표시될 값 설정
                    options.title(name);
                    options.snippet(vicinity);
                    // 아이콘 설정
                    //BitmapDescriptor icon= BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
                    //options.icon(icon);
                    // 마커를 지도에 표시한다.
                    Marker marker= mMap.addMarker(options);
                    markers_list.add(marker);
                }
            }
        });
    }



    //네비 관련 메소드
    @Override
    int getContentViewId() {
        return R.layout.activity_parkinglot;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.botNav03;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (checkPermission()) {
            makeLog("onStart : 위치 권한이 없습니다.");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null) {
            makeLog("onStop : cLocationUpdates 중지 합니다.");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

}
