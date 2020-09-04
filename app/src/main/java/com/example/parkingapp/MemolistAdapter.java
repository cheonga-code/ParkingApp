package com.example.parkingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MemolistAdapter extends RecyclerView.Adapter<MemolistAdapter.MemolistViewHolder> {

    Context context;

    //아이템을 위한 데이터만 ArrayList 형태로 보관 -> 앞에 static 붙이면 절대 안됨 !!!!!!!!!!
    ArrayList<MemolistVO> items = new ArrayList<MemolistVO>();

    //test중
    ArrayList<MemolistVO> keywordArrList = new ArrayList<MemolistVO>();

    OnItemClickListener listener;

    //인터페이스 구현
    public static interface OnItemClickListener{
        public void OnItemClick(RecyclerView.ViewHolder holder, View view, int position);
    }


    public MemolistAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return items.size();   //몇개의 아이템이 들어가 있는지를 알라줌
    }

    @NonNull
    @Override
    public MemolistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //뷰홀더 객체 생성
//        LayoutInflater inflater = LayoutInflater.from(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_memolist, parent, false);    //리스트내의 항목을 표시하기위한 뷰 생성
        MemolistViewHolder vh = new MemolistViewHolder(itemView);   //해당 뷰를 관리할 뷰홀더 생성

        return vh;  //뷰를 담고있는 뷰홀더 객체를 리턴
    }

    @Override
    public void onBindViewHolder(@NonNull MemolistViewHolder holder, final int position) {

        //인자를 통해 전달된 ViewHolder 객체에 position에 기반한 데이터를 할당(표시) 함

        final MemolistVO item = items.get(position);    //items 라는 것에 데이터가 들어있음 -> 즉, 리사이클러뷰에서 몇번째가 보여야되는지 알려줌
        holder.setItem(item);   //n번째 item의 데이터를 뷰에 setItem(String데이터 -> setText -> TextView에) 함

        holder.setOnItemClickListener(listener);

        //삭제버튼 클릭시
        holder.btnDeleteMemoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //알럿 빌더 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                //빌더에 정보입력
                builder.setTitle("삭제 확인")
                        .setMessage("삭제 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //삭제 진행
                                items.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, items.size());
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                //알럿 생성
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }

    //추가
    //데이터 추가할때 필요한 메소드
    //mAdapter.addItem(new MemolistVO(memoTitleData, memoCntData, memoDateData, memoGpseData, memoSaveTimeData));
    public void addItem(MemolistVO item){
        items.add(item);
    }

    //키워드 검색 관련 메소드 -> KeywordArrayList 에 추가
    public void addKeyWordItem(MemolistVO item){
        keywordArrList.add(item);
    }

    public void addItems(ArrayList<MemolistVO> items){
        this.items = items;
    }

    //아이템 위치 순번 가져올때 필요한 메소드
    //MemolistVO itemNum = mAdapter.getItem(position)
    public MemolistVO getItem(int position){
        return items.get(position);
    }

    //아이템 클릭 리스터 메소드
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    //추가 end

    //뷰홀더 클래스
    public class MemolistViewHolder extends RecyclerView.ViewHolder {

        //의미 그대로 아이템을 위한 view를 holder 담아두기 위해 하는 역할
        TextView memoItemTitleTv, memoItemCntTv, memoItemDateTv, memoItemGpsTv, memoItemSaveTimeTv;
        ImageView memoItemImg;
        Button btnDeleteMemoItem;

        MemolistVO memolistVO;

        OnItemClickListener listener;   //뷰홀더 안에도 설정 (어댑터와 마찬가지로)

        public MemolistViewHolder(@NonNull final View itemView) {
            super(itemView);

            memoItemTitleTv = (TextView) itemView.findViewById(R.id.memoItemTitleTv);       //메모 제목
            memoItemCntTv = (TextView) itemView.findViewById(R.id.memoItemCntTv);           //메모 내용
            memoItemDateTv = (TextView) itemView.findViewById(R.id.memoItemDateTv);         //메모 날짜
            memoItemGpsTv = (TextView) itemView.findViewById(R.id.memoItemGpsTv);           //메모 위치
            memoItemSaveTimeTv = (TextView) itemView.findViewById(R.id.memoItemSaveTimeTv); //메모 저장한 시간
            memoItemImg = (ImageView) itemView.findViewById(R.id.memoItemImg);                  //메모 사진
            btnDeleteMemoItem = (Button) itemView.findViewById(R.id.btnDeleteMemoItem);     //삭제버튼

            //각각 아이템 뷰에 클릭 리스터 이벤트 설정 -> 각각의 아이템 뷰가 클릭됬을 때
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //위의 어댑터에 등록한 리스너로 넘겨준다
                    int position = getAdapterPosition();    //클릭한 아이템의 위치을 알수있다

                    if(listener != null){
                        listener.OnItemClick(MemolistViewHolder.this, v, position);

                    }

                }
            });

        }

        //데이터 설정
        public void setItem(MemolistVO item){

            memoItemTitleTv.setText(item.getMemoItemTitleStr());
            memoItemCntTv.setText(item.getMemoItemCntStr());
            memoItemDateTv.setText(item.getMemoItemDateStr());
            memoItemGpsTv.setText(item.getMemoItemGpsStr());
            memoItemSaveTimeTv.setText(item.getMemoItemSaveTimeStr());

            //이미지 셋팅
            String imgPath = item.getMemoItemImgPathStr();
            Log.d("LogActivity", "item 이미지 경로 : "+imgPath);

            //이미지 경로값 = "0" 이 아닐때
            if(!item.getMemoItemImgPathStr().equals("0")) {
                //이미지 Glide 라이브러리 사용하여 로딩
                Glide.with(context)
                        .load(imgPath)                    //imageView에 url을 로드 시켜준다
//                        .diskCacheStrategy(DiskCacheStrategy.ALL) //캐시 관련
                        .override(100, 100)  //이미지를 지정한 크기만큼 불러옴 -> 로딩 속도 빠르게&메모리 절약하고 싶을때 유용
                        .placeholder(R.drawable.ic_imgload_default)  //이미지가 로딩하는동안 보여질 이미지
                        .error(R.drawable.ic_imgload_error)        //이미지 불러오데 실패했을 경우 보여질 이미지
                        .centerCrop()
                        .into(memoItemImg);   //imageView에 url을 로드 시켜준다
            }else{
                //이미지 경로값 = "0" 이면 기본 이미지 띄움
                memoItemImg.setImageResource(R.drawable.ic_imgload_default);
            }

        }

        //아이템 클릭 리스터 메소드  //뷰홀더 안에도 설정 (어댑터와 마찬가지로)
        public void setOnItemClickListener(OnItemClickListener listener){
            this.listener = listener;
        }


    }


}






























