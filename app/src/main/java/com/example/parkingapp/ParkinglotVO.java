package com.example.parkingapp;

public class ParkinglotVO {

    String prkItemNameStr;         //주차장 이름
    String prkItemAdrStr;          //주차장 주소
    String prkItemRatesStr;        //기본 주차 요금

    public ParkinglotVO(String prkItemNameStr, String prkItemAdrStr, String prkItemRatesStr) {
        this.prkItemNameStr = prkItemNameStr;
        this.prkItemAdrStr = prkItemAdrStr;
        this.prkItemRatesStr = prkItemRatesStr;
    }

    public String getPrkItemNameStr() {
        return prkItemNameStr;
    }

    public void setPrkItemNameStr(String prkItemNameStr) {
        this.prkItemNameStr = prkItemNameStr;
    }

    public String getPrkItemAdrStr() {
        return prkItemAdrStr;
    }

    public void setPrkItemAdrStr(String prkItemAdrStr) {
        this.prkItemAdrStr = prkItemAdrStr;
    }

    public String getPrkItemRatesStr() {
        return prkItemRatesStr;
    }

    public void setPrkItemRatesStr(String prkItemRatesStr) {
        this.prkItemRatesStr = prkItemRatesStr;
    }
}
