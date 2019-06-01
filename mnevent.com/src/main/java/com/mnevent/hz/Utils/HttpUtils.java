package com.mnevent.hz.Utils;

/**
 * Created by zyand on 2019/1/2.
 */

public class  HttpUtils {

    public static final String IP = "http://mac.freshtribes.com/macservice/";


    //产品库
    public static final String allGoods = IP+"getallgoods";
    //更新价格
    public static final String editGoods=IP+"makeupmis/carrier/editGoods";
    //商品详情
    public static final String getGoodDetail = IP+"makeupmis/carrier/getGoodDetail";
    //banner
    public static final String getAdver = IP+"makeupmis/carrier/getAdver";
    //上传参数
    public static final String getMcrealstatus = IP+"macpara";
    //上传商品信息
    public static final String getTrack = IP+"makeupmis/carrier/getTrack";
    //上传销售数据
    public static final String sale = IP+"makeupmis/carrier/sale";
    //更新版本
    public static final String apkver = IP+"apkver";

    //心跳上传
    public static final String macpara = IP+"macpara";

}
