package com.freshtribes.icecream.bean;

/**
 * Created by Administrator on 2017/10/13.
 */

public class GoodsBean {

    public String goodscode = "";
    public String goodsname = "";

    public int paycash = 1000;
    public int payali = 1000;
    public int paywx = 1000;
    public int paymember = 1000;

    // 存有此商品的货道编号(10,11,12,20)
    public String mainsaleing = "";  // 可以销售的轨道编号（有错误的轨道，被当做无货）
    public String mainsaleout = "";  // 已售完的轨道编号

}
