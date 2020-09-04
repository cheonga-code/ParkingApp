package com.example.parkingapp;

public class MemolistVO {

    String memoItemTitleStr;
    String memoItemCntStr;
    String memoItemDateStr;
    String memoItemGpsStr;
    String memoItemSaveTimeStr;
    String memoItemImgPathStr;

    public MemolistVO(String memoItemTitleStr, String memoItemCntStr, String memoItemDateStr, String memoItemGpsStr, String memoItemSaveTimeStr, String memoItemImgPathStr) {
        this.memoItemTitleStr = memoItemTitleStr;
        this.memoItemCntStr = memoItemCntStr;
        this.memoItemDateStr = memoItemDateStr;
        this.memoItemGpsStr = memoItemGpsStr;
        this.memoItemSaveTimeStr = memoItemSaveTimeStr;
        this.memoItemImgPathStr = memoItemImgPathStr;
    }

    public String getMemoItemTitleStr() {
        return memoItemTitleStr;
    }

    public void setMemoItemTitleStr(String memoItemTitleStr) {
        this.memoItemTitleStr = memoItemTitleStr;
    }

    public String getMemoItemCntStr() {
        return memoItemCntStr;
    }

    public void setMemoItemCntStr(String memoItemCntStr) {
        this.memoItemCntStr = memoItemCntStr;
    }

    public String getMemoItemDateStr() {
        return memoItemDateStr;
    }

    public void setMemoItemDateStr(String memoItemDateStr) {
        this.memoItemDateStr = memoItemDateStr;
    }

    public String getMemoItemGpsStr() {
        return memoItemGpsStr;
    }

    public void setMemoItemGpsStr(String memoItemGpsStr) {
        this.memoItemGpsStr = memoItemGpsStr;
    }

    public String getMemoItemSaveTimeStr() {
        return memoItemSaveTimeStr;
    }

    public void setMemoItemSaveTimeStr(String memoItemSaveTimeStr) {
        this.memoItemSaveTimeStr = memoItemSaveTimeStr;
    }

    public String getMemoItemImgPathStr() {
        return memoItemImgPathStr;
    }

    public void setMemoItemImgPathStr(String memoItemImgPathStr) {
        this.memoItemImgPathStr = memoItemImgPathStr;
    }
}



