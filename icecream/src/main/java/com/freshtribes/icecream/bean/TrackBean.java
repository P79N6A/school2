package com.freshtribes.icecream.bean;

/**
 * Created by Administrator on 2017/8/22.
 */

public class TrackBean {

    private String goodscode = "";
    private String goodsname = "";
    private String batchinfo = "";
    private String endday = "";

    private int paycash = 1000;
    private int payali = 1000;
    private int paywx = 1000;
    private int paymember = 1000;

    private int nummax = 10;
    private int numnow = 0;

    private int cansale = 1;  // 1可以销售，0表示不能销售（强制停止的意思），这是由后台强制锁定机器的轨道的时候使用，目前暂不开放

    private String errorcode = "";  // 空值表示没有故障
    private String errortime = "";  // yyyymmddhhmmss


    public int getCansale() {
        return cansale;
    }

    public void setCansale(int cansale) {
        this.cansale = cansale;
    }

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

    public String getBatchinfo() {
        return batchinfo;
    }

    public void setBatchinfo(String batchinfo) {
        this.batchinfo = batchinfo;
    }

    public String getEndday() {
        return endday;
    }

    public void setEndday(String endday) {
        this.endday = endday;
    }

    public int getPaycash() {
        return paycash;
    }

    public void setPaycash(int paycash) {
        this.paycash = paycash;
    }

    public int getPayali() {
        return payali;
    }

    public void setPayali(int payali) {
        this.payali = payali;
    }

    public int getPaywx() {
        return paywx;
    }

    public void setPaywx(int paywx) {
        this.paywx = paywx;
    }

    public int getPaymember() {
        return paymember;
    }

    public void setPaymember(int paymember) {
        this.paymember = paymember;
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
