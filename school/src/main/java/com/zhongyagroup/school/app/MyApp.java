package com.zhongyagroup.school.app;

import android.app.Application;

import com.zhongyagroup.school.bean.TrackBean;

public class MyApp extends Application {
    private final String serverurl = "http://mac.freshtribes.com/macservice";

    private String mainMacID = "";
    private String mainMacType = "general";  //   "drink" or "general"            “饮料机”，“综合机”
    private String accessKey = "";

    private int mainGeneralLevelNum;
    private int mainGeneralLevel1TrackCount;
    private int mainGeneralLevel2TrackCount;
    private int mainGeneralLevel3TrackCount;
    private int mainGeneralLevel4TrackCount;
    private int mainGeneralLevel5TrackCount;
    private int mainGeneralLevel6TrackCount;
    private int mainGeneralLevel7TrackCount;

    private TrackBean[] trackMainGeneral = new TrackBean[70];

    private String stockchecktime;
    private String tempSupplyCode;
    // ----------------------------------------------

    public String getTempSupplyCode() {
        return tempSupplyCode;
    }

    public void setTempSupplyCode(String tempSupplyCode) {
        this.tempSupplyCode = tempSupplyCode;
    }

    public String getStockchecktime() {
        return stockchecktime;
    }

    public void setStockchecktime(String stockchecktime) {
        this.stockchecktime = stockchecktime;
    }

    public int getMainGeneralLevelNum() {
        return mainGeneralLevelNum;
    }

    public void setMainGeneralLevelNum(int mainGeneralLevelNum) {
        this.mainGeneralLevelNum = mainGeneralLevelNum;
    }

    public int getMainGeneralLevel1TrackCount() {
        return mainGeneralLevel1TrackCount;
    }

    public void setMainGeneralLevel1TrackCount(int mainGeneralLevel1TrackCount) {
        this.mainGeneralLevel1TrackCount = mainGeneralLevel1TrackCount;
    }

    public int getMainGeneralLevel2TrackCount() {
        return mainGeneralLevel2TrackCount;
    }

    public void setMainGeneralLevel2TrackCount(int mainGeneralLevel2TrackCount) {
        this.mainGeneralLevel2TrackCount = mainGeneralLevel2TrackCount;
    }

    public int getMainGeneralLevel3TrackCount() {
        return mainGeneralLevel3TrackCount;
    }

    public void setMainGeneralLevel3TrackCount(int mainGeneralLevel3TrackCount) {
        this.mainGeneralLevel3TrackCount = mainGeneralLevel3TrackCount;
    }

    public int getMainGeneralLevel4TrackCount() {
        return mainGeneralLevel4TrackCount;
    }

    public void setMainGeneralLevel4TrackCount(int mainGeneralLevel4TrackCount) {
        this.mainGeneralLevel4TrackCount = mainGeneralLevel4TrackCount;
    }

    public int getMainGeneralLevel5TrackCount() {
        return mainGeneralLevel5TrackCount;
    }

    public void setMainGeneralLevel5TrackCount(int mainGeneralLevel5TrackCount) {
        this.mainGeneralLevel5TrackCount = mainGeneralLevel5TrackCount;
    }

    public int getMainGeneralLevel6TrackCount() {
        return mainGeneralLevel6TrackCount;
    }

    public void setMainGeneralLevel6TrackCount(int mainGeneralLevel6TrackCount) {
        this.mainGeneralLevel6TrackCount = mainGeneralLevel6TrackCount;
    }

    public int getMainGeneralLevel7TrackCount() {
        return mainGeneralLevel7TrackCount;
    }

    public void setMainGeneralLevel7TrackCount(int mainGeneralLevel7TrackCount) {
        this.mainGeneralLevel7TrackCount = mainGeneralLevel7TrackCount;
    }


    // 同一个商品时，如何确定出货的货道
    private int trackouttype; // "0:多的先出","1:前面的先出","2:后面的先出"

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }


    public String getServerurl() {
        return serverurl;
    }

    public String getMainMacID() {
        return mainMacID;
    }

    public void setMainMacID(String mainMacID) {
        this.mainMacID = mainMacID;
    }

    public String getMainMacType() {
        return mainMacType;
    }

    public void setMainMacType(String mainMacType) {
        this.mainMacType = mainMacType;
    }


    public TrackBean[] getTrackMainGeneral() {
        return trackMainGeneral;
    }

    public void setTrackMainGeneral(TrackBean[] trackMainGeneral) {
        this.trackMainGeneral = trackMainGeneral;
    }

    public int getTrackouttype() {
        return trackouttype;
    }

    public void setTrackouttype(int trackouttype) {
        this.trackouttype = trackouttype;
    }
}
