package com.example.parkingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MemoActivity extends Base2Activity {

//    static private String SHARED_NAME = "shared_file";
//    static private int SHARED_MODE = MODE_PRIVATE;

    TextView parkingTitle;
    EditText memoTitleEt, memoCntEt, memoDateEt, memoGpsEt;
    ImageView memoImg;
    Button btnCamera, btnGallery, btnMemoSave, btnGoogleMapGO;

    //shared -> 저장되있는 불러올 키
    String SHAREDPREF_JSON_KEY = "memoList";
    //shared 정의
    SharedPreferences pref;
    //JSONArray 정의
    JSONArray memoArray;

    //코드
    private final int CAMERA_CODE = 20;
    private final int GALLERY_CODE = 30;
    //이미지 관련
    private Uri photoURI;
    private String currentPhotoPath;    //실제 사진 파일 경로 -> 현재 사용중인 사진의 경로(디바이스 내 파일 경로)
    String mImageCaptureName;           //이미지 이름
    String prefSaveImgPath = "0";       //쉐어드프리퍼런스에 저장할 이미지 경로
//    String prefSaveImgPath = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        //id 연결
        parkingTitle = (TextView)findViewById(R.id.parkingTitle);   //주차이름 -> 실내주차 기록 & 야외주차 기록
        memoImg = (ImageView)findViewById(R.id.memoImg);            //메모사진
        memoTitleEt = (EditText)findViewById(R.id.memoTitleEt);     //메모제목
        memoCntEt = (EditText)findViewById(R.id.memoCntEt);         //메모내용
        memoDateEt = (EditText)findViewById(R.id.memoDateEt);       //메모날짜
        memoGpsEt = (EditText)findViewById(R.id.memoGpsEt);         //메모위치좌표

        btnCamera = (Button)findViewById(R.id.btnCamera);           //버튼 카메라
        btnGallery = (Button)findViewById(R.id.btnGallery);         //버튼 갤러리
        btnMemoSave = (Button)findViewById(R.id.btnMemoSave);       //버튼 메모저장
        btnGoogleMapGO = (Button)findViewById(R.id.btnGoogleMapGO); //버튼 구글맵으로 감

        //memoTitle 인텐트에서 데이터 받아옴 -> name "inParking" : value "실내기록 주차"
        Intent intent = getIntent();    //getIntent() 함수로 자신을 실행했던 인텐트 객체를 얻는다.
        String data = intent.getStringExtra("MemoTitle");   //그리고 그 안에 담긴 Extra 데이터를 getXXXExtra() 함수를 이용하여 얻는다
        parkingTitle.setText(data);

        //쉐어드에서 저장된 memoList 데이터 불러오기 -> String -> JSONArray 형태로 변환
        prefIntoJson();

        //현재 날짜&시간 받아오기
        SimpleDateFormat dateFormat = new SimpleDateFormat ( "yyyy년 MM월dd일");
        Date time = new Date();
        String dateFormatStr = dateFormat.format(time); //메모 저장 날짜
        memoDateEt.setText(dateFormatStr);

        // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                makeToast("권한 설정 완료");
            } else {
                makeToast("권한 설정 요청");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        //btnCamera 클릭이벤트 -> 카메라 앱 접근
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //카메라 앱으로 인텐트 전달
                selectCamera();
            }
        });

        //btnGallery 클릭이벤트 -> 앨범 앱 접근
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //갤러리 앱으로 인텐트 전달
                selectGallery();
            }
        });

        //btnMemoSave 클릭이벤트 -> 메모 shared 저장
        btnMemoSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //메모 json 형태 -> shared 에 저장
                memoIntoShared();

//                //home 화면으로 되돌아감
//                Intent homeIntent = new Intent(MemoActivity.this, HomeActivity.class);
//                startActivity(homeIntent);
//                finish();

                //자신을 실행했던 Home 액티비티로 돌아감
                Intent backIntent = getIntent();
                setResult(RESULT_OK, backIntent);
                finish();
            }
        });

        //btnGoogleMapGO 클릭이벤트 -> 구글맵 지도 띄움
        btnGoogleMapGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //구글맵 액티비티 화면으로 감
                Intent googleIntent = new Intent(MemoActivity.this, GoogleActivity.class);
                startActivity(googleIntent);
            }
        });


    }

    //권한 요청 - 마쉬멜로우 버전 관련
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        makeLog("=====onRequestPermissionsResult=====");
        makeToast("onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            makeLog("=====Permission: " + permissions[0] + "was " + grantResults[0]);
            makeToast("Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    //카메라 앱으로 인텐트 전달
    public void selectCamera(){

//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(cameraIntent, CAMERA_CODE);

        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)){

            //외부저장소가 현재 read와 write를 할수있는 상태인지 확인
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //이 인텐트를 수행할수 있는 액티비티를 찾는거고 그게 없으면 null값을 리턴합니다.
            //먼저 인텐트를 처리할 수 있는 앱이 존재하는지를 확인하기 위하여 Intent 오브젝트를 사용해 resolveActivity() 메서드를 호출한다.
            //결과가 null이 아니면 인텐트를 처리할 수 있는 앱이 최소 하나는 존재한다는 뜻
            if(cameraIntent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;
                try{
                    //createImageFile() 메소드 호출 -> 해당 함수의 return형이 File이며 이 File은 임시파일로 사용할 것이다.
                    photoFile = createImageFile();
                }catch (IOException e){
                    e.printStackTrace();
                }

                if(photoFile != null){
                    //getUriForFile 의 두번째 인자는 Manifest provider의 authorites와 일치해야 함
                    //photoURI 란 변수가 위의 임시파일의 위치를 가지고 있다.
                    //photoURI : file:// 로 시작, FileProvider (Content Provider 하위) 는 conent:// 로 시작
                    //누가(7.0) 이상부터는 file://로 시작되는 Uri 값을 다른 앱과 주고 받기가 불가능해졌다.
                    photoURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
//                    makeLog("[3] selectCamera() photoFile : "+ photoFile.toString());
                    makeLog("[2] selectCamera() photoUri : "+ photoURI.toString()); //보안상 안전 -> 이 photoURI 값을 쉐어드에 저장하면되는것 같은데..?

                    //putExtra 두번째 매개변수에 해당 파일의 URI 값을 전달한다.
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(cameraIntent, CAMERA_CODE);
                }

            }

        }


    }

    //이미지를 만드는 createImageFile 메소드
    private File createImageFile() throws IOException {

        //pathvalue = file_path.xml의 path 값과 같은 이름이어야한다.
        File dir = new File(Environment.getExternalStorageDirectory() + "/pathvalue/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //이미지 파일 이름 정하기 -> 오늘날짜_시간.png
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mImageCaptureName = timeStamp + ".png";

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/pathvalue/" + mImageCaptureName);
        //새로 생성된 파일의 해당 위치를 currentPhotoPath 란 변수에 저장하고, 저장한 파일을 리턴한다.
        //currentPhotoPath(절대경로) 와 Uri 값 비교
        //Uri -> file://storage/emulated..
        //currentPhotoPath -> storage/emulated.. (앞에 file:// 제외됨)
        currentPhotoPath = storageDir.getAbsolutePath();

        //내가 찍은 사진 앨범에 저장함
//        makeLog("[1] selectCamera() storageDir : "+storageDir);
        makeLog("[1] selectCamera() currentPhotoPath : "+currentPhotoPath);

        return storageDir;

    }

    //갤러리 앱으로 인텐트 전달
    public void selectGallery(){

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_CODE);
    }

    //startActivityForResult 결과
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 카메라 촬영 후 이미지 섬네일 가져오기
        if(requestCode == CAMERA_CODE && resultCode == RESULT_OK){

            //카메라로 찍은 사진 ImageView에 적용시키기
            getPictureForPhoto();
        }

        // 갤러리 앨범에서 이미지 가져오기
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){

            //갤러리에서 사진 가져와서 ImageView에 적용시키기
            makeLog("갤러리 imagePath(data.getData()) : "+data.getData()); // -> 요게 절대경로 인가??
            sendPicture(data.getData());
        }
    }

    //카메라로 찍은 사진 ImageView에 적용시키기
    private void getPictureForPhoto() {
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        makeLog("카메라 imagePath : "+currentPhotoPath); // -> 요게 절대경로 인가??
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currentPhotoPath);
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
        memoImg.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기
        //쉐어드에 저장할 이미지 변수에 경로 넣기
        prefSaveImgPath = currentPhotoPath;

    }

    //갤러리에서 사진 가져와서 ImageView에 적용시키기
    private void sendPicture(Uri imgUri) {

        String imagePath = getRealPathFromURI(imgUri); // path 경로
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
        makeLog("갤러리 imagePath : "+imagePath); // -> 요게 절대경로 인가??
        memoImg.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기

        //쉐어드에 저장할 이미지 변수에 경로 넣기
        prefSaveImgPath = imagePath;

    }

    //사진의 절대경로 구하기 (uri -> 절대경로)
    private String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
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

    //쉐어드에 저장된 데이터 -> 불러오기
    private void prefIntoJson() {

        pref = getSharedPreferences(SHARED_NAME, SHARED_MODE);

        if(pref.contains(SHAREDPREF_JSON_KEY) == true){

            String prefStrData = pref.getString(SHAREDPREF_JSON_KEY, "");
            makeLog("prefStrDate : "+prefStrData);

            //jsonArray 생성 -> jsonArray(쉐어드에 저장되어있던 데이터 -> Str 으로 변환한 형태)
            try {
                memoArray = new JSONArray(prefStrData);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            makeLog("memoArray(prefStrDate) : "+memoArray);
        }else{
            memoArray = new JSONArray();
        }

    }

    //메모 json 형태 -> shared 에 저장
    public void memoIntoShared(){

        // editor 정의
        SharedPreferences.Editor editor = pref.edit();

        // 메모 저장한 시간 데이터 받아오기
        SimpleDateFormat timeFormat = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        String timeFormatStr = timeFormat.format(time); //메모 저장 시간

        // string 으로 변환
        String memoTitleStr = memoTitleEt.getText().toString();
        String memoCntStr = memoCntEt.getText().toString();
        String memoDateStr = memoDateEt.getText().toString();
        String memoGpsStr = memoGpsEt.getText().toString();

        // jsonObject 생성
        JSONObject memoObject = new JSONObject();

        try {
            memoObject.put("memoTitle",memoTitleStr);
            memoObject.put("memoCnt",memoCntStr);
            memoObject.put("memoDate",memoDateStr);
            memoObject.put("memoGps",memoGpsStr);
            memoObject.put("memoSaveTime",timeFormatStr);   //메모를 저장한 시간
            memoObject.put("memoImgPath", prefSaveImgPath);     //메모 이미지
            makeLog("쉐어드에 저장한 이미지 경로 prefSaveImgPath :"+prefSaveImgPath);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        makeLog("memoObject : "+memoObject);

        memoArray.put(memoObject);

        String jsonArrayStrData = memoArray.toString();
        makeLog("jsonArrayStrData : "+jsonArrayStrData);

        // json 데이터 -> editor 에 저장
        editor.putString(SHAREDPREF_JSON_KEY, jsonArrayStrData);
        //editor 에 최종 적용
        editor.apply();

    }



}
