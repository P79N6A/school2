package com.freshtribes.icecream.presenter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.ISaleCountView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/22.
 */

public class SaleCountPresenter {
    private ISaleCountView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    public SaleCountPresenter(ISaleCountView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

//     db.execSQL("CREATE TABLE saledata(saletime nvarchar(14),"
//             + "isup int DEFAULT 1,"  //1表示已经上传到服务器了
//             + "trackno nvarchar(8),"
//             + "goodscode nvarchar(6),"
//             + "mingcheng nvarchar(24),"
//             + "batch nvarchar(24),"
//             + "endday nvarchar(14),"
//             + "price int,"
//             + "payway nvarchar(10),"// 现金，支付宝，微信，实体会员卡，微信会员付款码，第三方接口
//             + "payinfo nvarchar(99))");
    public void getSaleData(List<String> list, String fromdatatime){
        try {
            SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

//            db.execSQL("insert into saledata values('2018-05-26 12:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 11:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 10:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 09:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 08:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 07:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 06:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 05:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 04:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 03:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 02:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-25 01:01:01',1,'010','130001','雪碧','','',500,'cash','')");
//            db.execSQL("insert into saledata values('2018-05-26 12:02:01',1,'010','130001','雪碧','','',500,'alipay','kok890789709,087097lkp')");
//            db.execSQL("insert into saledata values('2018-05-26 11:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 10:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 09:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 08:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 07:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 06:02:01',1,'010','130001','雪碧','','',505,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 05:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 04:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 03:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 02:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-25 01:02:01',1,'010','130001','雪碧','','',500,'alipay','')");
//            db.execSQL("insert into saledata values('2018-05-26 12:02:02',1,'010','130001','雪碧','','',500,'wxpay','kok890789709,087097lkp')");
//            db.execSQL("insert into saledata values('2018-05-26 11:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 10:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 09:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 08:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 07:02:02',1,'010','130001','雪碧','','',50,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 06:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 05:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 04:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 03:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 02:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-25 01:02:02',1,'010','130001','雪碧','','',500,'wxpay','')");
//            db.execSQL("insert into saledata values('2018-05-26 12:02:03',1,'010','130001','雪碧','','',50,'iccard','kok890789709,087097lkp')");
//            db.execSQL("insert into saledata values('2018-05-26 11:02:03',1,'010','130001','雪碧','','',500,'iccard','')");
//            db.execSQL("insert into saledata values('2018-05-26 10:02:03',1,'010','130001','雪碧','','',500,'wxpaycode','')");
//            db.execSQL("insert into saledata values('2018-05-26 09:02:03',1,'010','130001','雪碧','','',500,'wxpaycode','')");
//            db.execSQL("insert into saledata values('2018-05-26 08:02:03',1,'010','130001','雪碧','','',500,'wxpaycode','')");
//            db.execSQL("insert into saledata values('2018-05-26 07:02:03',1,'010','130001','雪碧','','',500,'wxpaycode','')");
//            db.execSQL("insert into saledata values('2018-05-26 06:02:03',1,'010','130001','雪碧','','',500,'facepay','')");
//            db.execSQL("insert into saledata values('2018-05-26 05:02:03',1,'010','130001','雪碧','','',500,'facepay','')");
//            db.execSQL("insert into saledata values('2018-05-26 04:02:03',1,'010','130001','雪碧','','',500,'facepay','')");
//            db.execSQL("insert into saledata values('2018-05-16 03:02:03',0,'010','130001','雪碧','','',500,'facepay','')");
//            db.execSQL("insert into saledata values('2018-05-16 02:02:03',0,'010','130001','雪碧','','',500,'facepay','')");
//            db.execSQL("insert into saledata values('2018-05-15 01:02:03',0,'010','130001','雪碧','','',500,'facepay','')");

            String formattime = fromdatatime.replace("-","").replace(" ","").replace(":","");
            Cursor c = db.rawQuery("SELECT saletime,isup,trackno,goodscode,mingcheng,batch,endday,price,payway,payinfo " +
                    "FROM saledata where saletime >'" + formattime + "' order by saletime DESC", null);

            long totalsum = 0l;
            long totalcount = 0l;
            long cashsum = 0l;
            long cashcount = 0;
            long membersum = 0l;
            long membercount = 0l;
            long alisum = 0l;
            long alicount = 0l;
            long wxsum = 0l;
            long wxcount = 0l;
            long facesum = 0l;
            long facecount = 0l;
            while (c.moveToNext()) {
                String paymethod = "现金";
                switch (c.getString(8)){
                    case "alipay":
                        paymethod = "支付宝";
                        break;
                    case "wxpay":
                        paymethod = "微信";
                        break;
                    case "iccard":
                        paymethod = "ic卡";
                        break;
                    case "wxpaycode":
                        paymethod = "会员";
                        break;
                    case "facepay":
                        paymethod = "人脸支付";
                        break;
                    case "thirdpaycode":
                        paymethod = "第三方";
                        break;
                    default:
                        paymethod = "现金";
                        break;
                }

                String sale = c.getString(0);
                String saletime = sale.substring(4,6)+"-"+sale.substring(6,8)+" "+sale.substring(8,10)+":"+sale.substring(10,12);
                String tracknostr = c.getString(2);
                if(tracknostr.length() == 3 && tracknostr.substring(0,1).equals("0")){
                    tracknostr = "主"+tracknostr.substring(1,3);
                } else if(tracknostr.length() == 3 && tracknostr.substring(0,1).equals("1")){
                    tracknostr = "副"+tracknostr.substring(1,3);
                }
                list.add("" +
                        saletime+"" + "   " +
                        (c.getInt(1)==1?"√":"-") + "   " +
                        tracknostr + "   " +
                        Gmethod.tranFenToFloat2(c.getInt(7)) + "   " +
                        paymethod + "   " +
                        c.getString(3) + "" +
                        c.getString(4) + "   " +
                        c.getString(9));

                if(paymethod.equals("现金")) {
                    cashsum = cashsum + c.getInt(7);
                    cashcount++;
                } else if(paymethod.equals("支付宝")) {
                    alisum = alisum + c.getInt(7);
                    alicount++;
                } else if(paymethod.equals("微信")){
                    wxsum = wxsum + c.getInt(7);
                    wxcount++;
                } else if(paymethod.equals("人脸支付")){
                    facesum = facesum + c.getInt(7);
                    facecount++;
                } else {
                    membersum = membersum + c.getInt(7);
                    membercount++;
                }

                totalsum = totalsum + c.getInt(7);
                totalcount++;

            }

            c.close();

            db.close();

            iView.rtnSaleCount(totalsum,totalcount,cashsum,cashcount,alisum,alicount,wxsum,wxcount,facesum,facecount,membersum,membercount);

        } catch (Exception e){
            e.printStackTrace();

            iView.rtnSaleCountError(e.getMessage());
        }
    }

}

