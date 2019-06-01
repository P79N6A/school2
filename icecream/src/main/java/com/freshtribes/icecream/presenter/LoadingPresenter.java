package com.freshtribes.icecream.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.util.Log;


import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.bean.TrackBean;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.ILoadingView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import android_serialport_api.ComTrackMagex;

/**
 * Created by Administrator on 2017/8/22.
 */

public class LoadingPresenter {
    private ILoadingView iLoadingView;
    
    private ThreadGroup threadGroup;
    private boolean isStopThread = false;
    
    private MyApp myApp;
            
    public LoadingPresenter(ILoadingView view, ThreadGroup th, MyApp myApp){
        iLoadingView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void getSettingInfo(final String starti,final String mactime){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                long nowstart = System.currentTimeMillis();
                try {
                    SharedPreferences sp =  myApp.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);

                    myApp.setMainMacID(sp.getString("mainMacID", ""));
                    myApp.setAccessKey(sp.getString("accessKey", "00000000"));

                    myApp.setMainGeneralLevelNum(sp.getInt("mainGeneralLevelNum", 7));
                    myApp.setMainGeneralLevel1TrackCount(sp.getInt("mainGeneralLevel1TrackCount", 10));
                    myApp.setMainGeneralLevel2TrackCount(sp.getInt("mainGeneralLevel2TrackCount", 10));
                    myApp.setMainGeneralLevel3TrackCount(sp.getInt("mainGeneralLevel3TrackCount", 10));
                    myApp.setMainGeneralLevel4TrackCount(sp.getInt("mainGeneralLevel4TrackCount", 10));
                    myApp.setMainGeneralLevel5TrackCount(sp.getInt("mainGeneralLevel5TrackCount", 10));
                    myApp.setMainGeneralLevel6TrackCount(sp.getInt("mainGeneralLevel6TrackCount", 10));
                    myApp.setMainGeneralLevel7TrackCount(sp.getInt("mainGeneralLevel7TrackCount", 10));

                    myApp.setTrackplanid(sp.getLong("trackplanid",0l));
                    myApp.setTrackplanname(sp.getString("trackplanname",""));

                    //myApp.setTrackplanid(0);

                    myApp.setHaveFacePay(sp.getInt("havefacepay",0));
                    myApp.setMichave(sp.getInt("michave",0));
                    myApp.setHaveMember(sp.getInt("havemember",0));

                    myApp.setHaveAlipay(sp.getInt("havealipay",1));//缺省是标准支付宝
                    myApp.setHavaWeixin(sp.getInt("haveweixin",1));//缺省是标准微信

                    myApp.setHaveUms(sp.getInt("haveums",0));// 中国银联

                    myApp.setHaveZXAlipay(sp.getInt("havezxalipay",0));
                    myApp.setHaveZXWxpay(sp.getInt("havezxwxpay",0));

                    myApp.setDevicetype(sp.getInt("devicetype",0));// 默认是中亚驱动板
                    myApp.setMainMacType(sp.getString("mactype","icecream"));// 默认是冰激凌机器，可以改为普通综合机

                    // 最近一次的盘点时间
                    myApp.setStockchecktime(sp.getString("stockchecktime",""));

                    for(int i=10;i<=79;i++){
                        int step = sp.getInt("step"+i,3);
                        myApp.getMagexStep()[i-10] = step;
                    }

                    myApp.setMagexallstep(sp.getInt("allstep",18));

                } catch (Exception ex){
                    ex.printStackTrace();
                }

                LogUtils.i("Loading配置文件读完：" + (System.currentTimeMillis()-nowstart));
                // 读取数据库文件,先判断数据库文件是否存在
                // 取得数据库默认路径
                String dbfilepath = myApp.getDatabasePath("vmdata.db").getAbsolutePath();
                File dbfile = new File(dbfilepath);
                boolean dbfileexist = dbfile.exists();

                String sqlstr = "";
                // 70个元素
                for(int i = 10; i <= 79; i++) if(myApp.getTrackMainGeneral()[i-10]==null)  myApp.getTrackMainGeneral()[i - 10] = new TrackBean();

                if(!dbfileexist){
                    // 首次运行
                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                    LogUtils.i("没有数据库文件，需要重新生成");

                    db.execSQL("CREATE TABLE trackmaingeneral(id INTEGER PRIMARY KEY,"  //从1到23为止
                            + "goodscode nvarchar(5),"
                            + "mingcheng nvarchar(24),"
                            + "batchinfo nvarchar(24),"
                            + "endday nvarchar(14),"
                            + "paycash int,"   //最大：999.99
                            + "payali int,"
                            + "paywx int,"
                            + "paymember int,"
                            + "nummax int,"
                            + "numnow int,"
                            + "cansale int,"
                            + "errorcode nvarchar(8),"
                            + "errortime nvarchar(14))");
                    for (int i = 10; i <= 79; i++)
                    {
                        sqlstr = "INSERT INTO trackmaingeneral(id,goodscode,mingcheng,batchinfo,endday,paycash,payali,paywx,paymember,nummax,numnow,cansale,errorcode,errortime) " +
                                "VALUES (" + i + ",'','','','',1000,1000,1000,1000,10,0,1,'','')";
                        db.execSQL(sqlstr);
                    }

                    db.execSQL("CREATE TABLE saledata(saletime nvarchar(14) PRIMARY KEY,"
                            + "isup int DEFAULT 1,"  //1表示已经上传到服务器了
                            + "trackno nvarchar(8),"
                            + "goodscode nvarchar(5),"
                            + "mingcheng nvarchar(24),"
                            + "batch nvarchar(24),"
                            + "endday nvarchar(14),"
                            + "price int,"
                            + "payway nvarchar(10),"// 现金，支付宝，微信，实体会员卡，微信会员付款码，第三方接口
                            + "payinfo nvarchar(99))");
                    // (现金cash：内容为空
                    //  支付宝alipay：商户交易号,支付宝交易号,支付者buyer_logon_id
                    //  微信wxpay：商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注)
                    //  实体会员卡iccard：会员卡物理卡号
                    //  微信会员付款码wxpaycode：微信会员付款码
                    //  第三方接口thirdpaycode：第三方接口码）

                    // 商品和价格表
                    db.execSQL("CREATE TABLE goodsname(goodscode nvarchar(5) PRIMARY KEY,"
                            + "goodsname nvarchar(24),"
                            + "paycash int,"   //最大：999.99
                            + "payali int,"
                            + "paywx int,"
                            + "paymember int)");

                    db.close();

                    LogUtils.i("Loading新生成一个数据库文件完：" + (System.currentTimeMillis()-nowstart));

                } else {
                    // 读取
                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                    LogUtils.i("数据库文件已存在，进行数据读取");

                    Cursor cgoodsname = db.rawQuery("select count(*) as ishave from sqlite_master where type='table' and name='goodsname'",null);
                    cgoodsname.moveToNext();
                    int ishavegoodsname = cgoodsname.getInt(cgoodsname.getColumnIndex("ishave"));
                    if(ishavegoodsname==0){
                        db.execSQL("CREATE TABLE goodsname(goodscode nvarchar(5) PRIMARY KEY,"
                                + "goodsname nvarchar(24),"
                                + "paycash int,"   //最大：999.99
                                + "payali int,"
                                + "paywx int,"
                                + "paymember int)");
                    }
                    cgoodsname.close();

                    //db.execSQL("delete from saledata");

                    // 综合机 10-79
                    Cursor c = db.rawQuery("SELECT id,goodscode,mingcheng,batchinfo,endday,paycash,payali,paywx,paymember,nummax,numnow,cansale,errorcode,errortime " +
                            "FROM trackmaingeneral", null);
                    while (c.moveToNext())
                    {
                        int huohao = c.getInt(c.getColumnIndex("id")); // 取出的值是从10开始的 : 10-79
                        myApp.getTrackMainGeneral()[huohao - 10].setGoodscode(c.getString(1));
                        myApp.getTrackMainGeneral()[huohao - 10].setGoodsname(c.getString(2));
                        myApp.getTrackMainGeneral()[huohao - 10].setBatchinfo(c.getString(3));
                        myApp.getTrackMainGeneral()[huohao - 10].setEndday(c.getString(4));
                        myApp.getTrackMainGeneral()[huohao - 10].setPaycash(c.getInt(5));
                        myApp.getTrackMainGeneral()[huohao - 10].setPayali(c.getInt(6));
                        myApp.getTrackMainGeneral()[huohao - 10].setPaywx(c.getInt(7));
                        myApp.getTrackMainGeneral()[huohao - 10].setPaymember(c.getInt(8));
                        myApp.getTrackMainGeneral()[huohao - 10].setNummax(c.getInt(9));
                        myApp.getTrackMainGeneral()[huohao - 10].setNumnow(c.getInt(10));
                        myApp.getTrackMainGeneral()[huohao - 10].setCansale(c.getInt(11));
                        myApp.getTrackMainGeneral()[huohao - 10].setErrorcode(c.getString(12));
                        myApp.getTrackMainGeneral()[huohao - 10].setErrortime(c.getString(13));
                    }
                    c.close();

                    db.close();

                    LogUtils.i("Loading读取现有数据库文件完：" + (System.currentTimeMillis()-nowstart));
                }

                // 发送启动数据给服务器
                if(myApp.getMainMacID().length() > 0){
                    long timestamp = System.currentTimeMillis();
                    String str = "macid="+myApp.getMainMacID() + "&mactime="+ mactime + "&starti="+starti  +
                            "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                    String md5 = MD5.GetMD5Code(str);

                    String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/appstartlog",
                            "macid="+myApp.getMainMacID() + "&mactime="+ mactime + "&starti="+starti  +
                                    "&timestamp="+timestamp + "&md5=" + md5);

                    if(rtnstr.length()==0){
                        // iLoadingView.dispMessage("联网失败");
                        // 暂停1秒后，在试一次
                        try{
                            Thread.sleep(1000);
                        }catch (InterruptedException e){}

                        timestamp = System.currentTimeMillis();
                        str = "macid="+myApp.getMainMacID() + "&mactime="+ mactime + "&starti="+starti  +
                                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                        md5 = MD5.GetMD5Code(str);
                        rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/appstartlog",
                                "macid="+myApp.getMainMacID() + "&mactime="+ mactime + "&starti="+starti  +
                                        "&timestamp="+timestamp + "&md5=" + md5);
                        if(rtnstr.length()==0){
                            // 暂停2秒后，在试一次
                            try{
                                Thread.sleep(2000);
                            }catch (InterruptedException e){}

                            timestamp = System.currentTimeMillis();
                            str = "macid="+myApp.getMainMacID() + "&mactime="+ mactime + "&starti="+starti  +
                                    "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                            md5 = MD5.GetMD5Code(str);
                            rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/appstartlog",
                                    "macid="+myApp.getMainMacID() + "&mactime="+ mactime + "&starti="+starti  +
                                            "&timestamp="+timestamp + "&md5=" + md5);

                            if(rtnstr.length()==0){
                                iLoadingView.dispMessage("联网失败");
                            } else {
                                // {"msg":"","code":0,"carriersn":"工厂车间"}
                                int code = 2;
                                String msg = "解析返回值出错啦";
                                try {
                                    JSONObject soapJson = new JSONObject(rtnstr);
                                    code = soapJson.getInt("code");
                                    msg = soapJson.getString("msg");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if(code != 0){
                                    iLoadingView.dispMessage(msg);
                                }
                            }
                        } else {
                            // {"msg":"","code":0,"carriersn":"工厂车间"}
                            int code = 2;
                            String msg = "解析返回值出错啦";
                            try {
                                JSONObject soapJson = new JSONObject(rtnstr);
                                code = soapJson.getInt("code");
                                msg = soapJson.getString("msg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(code != 0){
                                iLoadingView.dispMessage(msg);
                            }
                        }

                    } else {
                        // {"msg":"","code":0,"carriersn":"工厂车间"}
                        int code = 2;
                        String msg = "解析返回值出错啦";
                        try {
                            JSONObject soapJson = new JSONObject(rtnstr);
                            code = soapJson.getInt("code");
                            msg = soapJson.getString("msg");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(code != 0){
                            iLoadingView.dispMessage(msg);
                        }
                    }

                    LogUtils.i("Loading连到后台完：" + (System.currentTimeMillis()-nowstart));
                }

                String status = "";
                if(myApp.getDevicetype() == 1 || myApp.getDevicetype() == 2){ // 0:中亚冰激凌升降机","1:Magex有主控_皮带或弹簧","2:Magex有主控_链板"
                    ComTrackMagex comTrackMagex = new ComTrackMagex("");
                    comTrackMagex.openSerialPort();
                    status = comTrackMagex.getStatus();
                    comTrackMagex.closeSerialPort();

                    if(status.contains("User mode")){
                        status = "";
                    } else {
                        status = "Tech mode";
                    }
                }

                try{
                    Thread.sleep(1000);
                }catch (InterruptedException ex){
                    ex.printStackTrace();
                }

                LogUtils.i("Loading暂停完：" + (System.currentTimeMillis()-nowstart));
                // 读取数据结束告诉activity
                iLoadingView.loadingDataOver(status);

            }
        },"loading线程");

        thread.start();

    }

}

