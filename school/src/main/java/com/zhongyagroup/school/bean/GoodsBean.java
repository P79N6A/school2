package com.zhongyagroup.school.bean;

/**
 * Created by Administrator on 2017/10/13.
 */

public class GoodsBean {

    public String goodscode = "";
    public String goodsname = "";

    public int payprice = 1000;

    // 存有此商品的货道编号(10,11,12,20) 货道编号从1开始的
    public String mainsaleing = "";  // 有货的轨道编号（有错误的轨道，被当做无货）
    public String mainsaleout = ""; // 有这个商品的轨道编号


}
