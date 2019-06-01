package com.mnevent.hz.litemolder;

import org.litepal.crud.LitePalSupport;

/**
 * Created by zyand on 2019/5/21.
 */

public class TrackstatusMolder extends LitePalSupport {

    private int id;

    private String code;//轨道号

    private int nummax ;//最大存放数
    private int numnow ;//现在存放数量

    private String errorcode ;  // 1表示没有故障，0表示有故障

    private String errortime;   //故障发生时间

    private String mergecode ;  //1表示合并后可用的，0表示被禁用的,2表示被合并的

    private String steps ; //表示1次所走的步数



    private String pds ; //是否存在，0不存在，1存在

    private int chain;//链条

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getMergecode() {
        return mergecode;
    }

    public void setMergecode(String mergecode) {
        this.mergecode = mergecode;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getPds() {
        return pds;
    }

    public void setPds(String pds) {
        this.pds = pds;
    }

    public int getChain() {
        return chain;
    }

    public void setChain(int chain) {
        this.chain = chain;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
