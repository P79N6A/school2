package com.zhongyagroup.school.bean;

/**
 * Created by Administrator on 2017/8/22.
 */

public class TrackBean {

    private String goodscode = "";
    private String goodsname = "";

    private int payprice = 1000;

    private int nummax = 0;
    private int numnow = 0;

    private String errorcode = "";  // 空值表示没有故障
    private String errortime = "";  // yyyymmddhhmmss


    public String getGoodscode() {
        return goodscode;
    }

    public void setGoodscode(String goodscode) {
        this.goodscode = goodscode;
    }

    public String getGoodsname() {
        return goodsname;
    }

    public void setGoodsname(String goodsname) {
        this.goodsname = goodsname;
    }

    public int getPayprice() {
        return payprice;
    }

    public void setPayprice(int payprice) {
        this.payprice = payprice;
    }

    public int getNummax() {
        return nummax;
    }

    public void setNummax(int nummax) {
        this.nummax = nummax;
    }

    public int getNumnow() {
        return numnow;
    }

    public void setNumnow(int numnow) {
        this.numnow = numnow;
    }

    public String getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(String errorcode) {
        this.errorcode = errorcode;
    }

    public String getErrortime() {
        return errortime;
    }

    public void setErrortime(String errortime) {
        this.errortime = errortime;
    }
}
