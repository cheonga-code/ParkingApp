package com.example.parkingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ParkinglotAdapter extends RecyclerView.Adapter<ParkinglotAdapter.ParkinglotViewHolder> {

    Context context;    //context 객체

    //아이템을 위한 데이터만 ArrayList 형태로 보관
    ArrayList<ParkinglotVO> prkArraylist = new ArrayList<ParkinglotVO>();

    public ParkinglotAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return prkArraylist.size(); //ArrayList에 아이템 몇개 들어가있는지 사이즈 알려줌
    }

    @NonNull
    @Override
    public ParkinglotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //뷰홀더 객체 생성
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        //아이템 내의 항목을 표시하기위한 뷰 생성
        View view = inflater.inflate(R.layout.item_parkinglist, parent, false);
        //해당 뷰를 관리할 뷰홀더 생성
        ParkinglotViewHolder vh = new ParkinglotViewHolder(view);

        return vh;  //뷰를 담고 있는 뷰홀더 객체를 리턴
    }

    @Override
    public void onBindViewHolder(@NonNull ParkinglotViewHolder holder, int position) {

        //위의 onCreateViewHolder에서 인자를 통해 전달된 뷰홀더 객체 vh에 position을 기반한 데이터를 할당함
        ParkinglotVO parkinglotVO = prkArraylist.get(position);
        holder.setItem(parkinglotVO);

    }

    //addItem 메소드 추가 -> 데이터 추가할때 필요
    public void addItem(ParkinglotVO parkinglotVO){
        prkArraylist.add(parkinglotVO);
    }

    //getItem 메소드 추가 -> 아이템 클릭시 -> 클릭한 position 알때 필요
    public ParkinglotVO getItem(int position){
        return prkArraylist.get(position);
    }

    //뷰홀더 클래스
    public class ParkinglotViewHolder extends RecyclerView.ViewHolder {

        TextView prkItemNameTv, prkItemAdrTv, prkItemRatesTv;

        public ParkinglotViewHolder(@NonNull View itemView) {
            super(itemView);

            prkItemNameTv = (TextView) itemView.findViewById(R.id.prkItemNameTv);   //주차장 이름
            prkItemAdrTv = (TextView) itemView.findViewById(R.id.prkItemAdrTv);     //주차장 주소
            prkItemRatesTv = (TextView) itemView.findViewById(R.id.prkItemRatesTv); //기본 주차 요금
        }

        //데이터 -> View에 셋팅
        public void setItem(ParkinglotVO item){

            prkItemNameTv.setText(item.getPrkItemNameStr());
            prkItemAdrTv.setText(item.getPrkItemAdrStr());
            prkItemRatesTv.setText(item.getPrkItemRatesStr());
        }
    }
}
