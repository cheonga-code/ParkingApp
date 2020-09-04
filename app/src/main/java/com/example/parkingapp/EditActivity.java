package com.example.parkingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditActivity extends Base2Activity {

    TextView editTitleEt, editCntEt, editDateEt, editGpsEt;
    Button btnEditMomoOK, btnEditCamera, btnEditGallery;
    ImageView editImg;

    //자신을 실행한 인텐트 객체에서 데이터 얻기
    Intent intent;
    String getIntentTitleStr, getIntentCntStr, getIntentDateStr, getIntentGpsStr, getIntentImgPathStr;   //인텐트에서 얻은 데이터

    //이미지 관련
    private final int CAMERA_CODE = 2001;
    private final int GALLERY_CODE = 2002;
    private Uri photoURI;
    private String currentPhotoPath;    //실제 사진 파일 경로 -> 현재 사용중인 사진의 경로(디바이스 내 파일 경로)
    String mImageCaptureName;           //이미지 이름
    String prefSaveImgPath;             //쉐어드프리퍼런스에 저장할 이미지 경로

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //id 연결
        editTitleEt = (EditText)findViewById(R.id.editTitleEt);
        editCntEt = (EditText)findViewById(R.id.editCntEt);
        editDateEt = (EditText)findViewById(R.id.editDateEt);
        editGpsEt = (EditText)findViewById(R.id.editGpsEt);
        editImg = (ImageView)findViewById(R.id.editImg);

        btnEditMomoOK = (Button)findViewById(R.id.btnEditMemoOK);   //버튼 - 메모저장
        btnEditCamera = (Button)findViewById(R.id.btnEditCamera);   //버튼 - 카메라
        btnEditGallery = (Button)findViewById(R.id.btnEditGallery); //버튼 - 앨범

        //자신을 실행한 인텐트 객체에서 데이터 얻기
        intent = getIntent();

        getIntentTitleStr = intent.getStringExtra("메모제목");
        getIntentCntStr = intent.getStringExtra("메모내용");
        getIntentDateStr = intent.getStringExtra("메모날짜");
        getIntentGpsStr = intent.getStringExtra("메모위치");
        getIntentImgPathStr = intent.getStringExtra("메모사진");

        //setText 해주기
        editTitleEt.setText(getIntentTitleStr);
        editCntEt.setText(getIntentCntStr);
        editDateEt.setText(getIntentDateStr);
        editGpsEt.setText(getIntentGpsStr);

        //이미지 경로 초기셋팅
        prefSaveImgPath = getIntentImgPathStr;
        //setImg 해주기
//        Bitmap bitmap = BitmapFactory.decodeFile(getIntentImgPathStr);  //얻은 이미지 경로 비트맵으로 전환
//        editImg.setImageBitmap(bitmap);   //이미지 뷰에 비트맵 넣기
        editImg.setImageURI(Uri.parse(getIntentImgPathStr));
//        setImageBitmapRotate(getIntentImgPathStr, editImg);

        makeLog("\n상세 Acty에서 받은 메모데이터 : "+getIntentTitleStr+"/"+getIntentCntStr+"/"+getIntentDateStr+"/"+getIntentGpsStr+"/"+getIntentImgPathStr);

        //버튼 - 수정완료 클릭시
        btnEditMomoOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //editText 데이터 얻어 -> Intent에 데이터 담기
                editTextIntoIntent();

                //전 화면으로 돌아감
                setResult(RESULT_OK, intent);   //정상으로 처리되어 되돌린다는 것을 명시함
                finish();   //자신을 종료
            }
        });

        //btnCamera 클릭이벤트 -> 카메라 앱 접근
        btnEditCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //카메라 앱으로 인텐트 전달
                selectCamera();
            }
        });

        //btnGallery 클릭이벤트 -> 앨범 앱 접근
        btnEditGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //갤러리 앱으로 인텐트 전달
                selectGallery();
            }
        });

    }

    //editText 데이터 얻어 -> Intent에 데이터 담기
    public void editTextIntoIntent(){

        //editText 에서 getText() 하기
        String editTitleStr = editTitleEt.getText().toString();
        String editCtnStr = editCntEt.getText().toString();
        String editDateStr = editDateEt.getText().toString();
        String editGpsStr = editGpsEt.getText().toString();

        //현재 시간 데이터 구하기
        // 메모 저장한 시간 데이터 받아오기
        SimpleDateFormat timeFormat = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        String timeFormatStr = timeFormat.format(time); //메모 저장 시간

        intent.putExtra("수정한 메모제목", editTitleStr);
        intent.putExtra("수정한 메모내용", editCtnStr);
        intent.putExtra("수정한 메모날짜", editDateStr);
        intent.putExtra("수정한 메모위치", editGpsStr);
        intent.putExtra("수정한 메모시간", timeFormatStr);
        intent.putExtra("수정한 메모사진", prefSaveImgPath);
        makeLog("\n수정 Acty에서 상세 Acty에 보낼 수정한 메모데이터 : "+editTitleStr+"/"+editCtnStr+"/"+editDateStr+"/"+editGpsStr+"/"+timeFormatStr+"/"+prefSaveImgPath);

    }

    //카메라 앱으로 인텐트 전달
    public void selectCamera(){

        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)){

            //외부저장소가 현재 read와 write를 할수있는 상태인지 확인
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //이 인텐트를 수행할수 있는 액티비티를 찾는거고 그게 없으면 null값을 리턴합니다.
            if(cameraIntent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;
                try{
                    //createImageFile() 메소드 호출 -> 해당 함수의 return형이 File이며 이 File은 임시파일로 사용할 것이다.
                    photoFile = createImageFile();
                }catch (IOException e){
                    e.printStackTrace();
                }

                if(photoFile != null){
                    //photoURI 란 변수가 위의 임시파일의 위치를 가지고 있다.
                    photoURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);

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
        currentPhotoPath = storageDir.getAbsolutePath();

        //내가 찍은 사진 -> 앨범에 저장함
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
        editImg.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기
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
        editImg.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기

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

}
