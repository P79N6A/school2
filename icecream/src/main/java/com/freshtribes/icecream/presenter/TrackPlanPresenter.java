package com.freshtribes.icecream.presenter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.ITrackPlanView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/22.
 */

public class TrackPlanPresenter {
    private ITrackPlanView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    public TrackPlanPresenter(ITrackPlanView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void getServerTrackPlan(){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                String str = "macid="+ myApp.getMainMacID() +
                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/trackplaninfo",
                        "macid="+myApp.getMainMacID() +
                                "&timestamp="+timestamp + "&md5=" + md5);
                LogUtils.i("获取的货道方案结果:"+rtnstr);

                if(rtnstr.length()==0){
                    iView.rtnServerTrackPlan(1,"联网失败",0l,"","","",null);
                } else {
                    //{"msg":"","code":0,"maintrackflag":"00000000011111000001111100000111110000000000000000000000000000000000000000000000",
                    //        "planname":"201708暑期特惠货道方案","planid":1,"comment":"无",
                    //        "subtrackflag":"1111100000111110000011111000001111100000111110000000000000000000000000",
                    //        "goodprice":[{"code":"130001","price":"1000_1000_1000_1000","index":10},
                    //    {"code":"130001","price":"1000_1000_1000_1000","index":11},
                    //    {"code":"130001","price":"1000_1000_1000_1000","index":12},
                    //    {"code":"130001","price":"1000_1000_1000_1000","index":13},
                    //    {"code":"130001","price":"1000_1000_1000_1000","index":14},
                    //    {"code":"130002","price":"1000_1000_1000_1000","index":20},
                    //    {"code":"130002","price":"1000_1000_1000_1000","index":21},
                    //    {"code":"130001","price":"1000_1000_1000_1000","index":110},
                    //    {"code":"130001","price":"1000_1000_1000_1000","index":111}
                    //     ]
                    // }
                    int code = 2;
                    String msg = "解析返回值出错啦";
                    long planid = 0l;
                    String planname = "";
                    String maintrackflag = "00000000000000000000000000000000000000000000000000000000000000000000000000000000";
                    String subtrackflag = "0000000000000000000000000000000000000000000000000000000000000000000000";
                    ArrayList<HashMap<String,Object>> arrayList = new ArrayList<>();
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        code = soapJson.getInt("code");
                        msg = soapJson.getString("msg");
                        if(code == 0) {
                            planid = soapJson.getLong("planid");
                            planname = soapJson.getString("planname");
                            maintrackflag = soapJson.getString("maintrackflag");
                            subtrackflag = soapJson.getString("subtrackflag");

                            JSONArray goodpricearray = soapJson.getJSONArray("goodprice");
                            for(int i=0;i<goodpricearray.length();i++){
                                JSONObject member = goodpricearray.getJSONObject(i);
                                String goodscode = member.getString("code");
                                int index = member.getInt("index");
                                String goodsprice = member.getString("price");
                                HashMap<String,Object> hm = new HashMap<>();
                                hm.put("code",goodscode);
                                hm.put("price",goodsprice.replace(" ",""));
                                hm.put("index",index);
                                arrayList.add(hm);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        code = 2;
                        msg = "解析返回值出错啦"+e.getMessage();
                    }

                    iView.rtnServerTrackPlan(code,msg,planid,planname,maintrackflag,subtrackflag,arrayList);
                }
            }
        });

        thread.start();

    }

    public void downpng(final String[] goodscodearray,final ArrayList<HashMap<String,Object>> arrayList){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                try {
                    // 判断是否有文件夹存在
                    File file = new File(Environment.getExternalStorageDirectory() + "/goodspng/");
                    if(!file.exists()){
                        boolean irtn=  file.mkdirs();
                        LogUtils.i( "文件夹" + Environment.getExternalStorageDirectory() + "/goodspng/" + "的mkdir结果:" + irtn);
                    }
                    boolean iserror = false;
                    for(String goodscode:goodscodearray) {
                        String goodsname = "";
                        String pngurl = "";

                        long timestamp = System.currentTimeMillis();
                        String str = "goodscode=" + goodscode + "&macid="+ myApp.getMainMacID() +
                                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                        String md5 = MD5.GetMD5Code(str);

                        String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/goodsinfo",
                                "macid="+myApp.getMainMacID() + "&goodscode=" + goodscode +
                                        "&timestamp="+timestamp + "&md5=" + md5);
                        if(rtnstr.length()==0){
                            iserror = true;
                            iView.downgoodsinfoerror("取得商品编号="+goodscode+"的信息时，联网失败");
                            break;
                        } else {
                            int code = 2;
                            String msg = "解析返回值出错啦";

                            try {
                                JSONObject soapJson = new JSONObject(rtnstr);
                                code = soapJson.getInt("code");
                                msg = soapJson.getString("msg");
                                if(code == 0) {
                                    goodsname = soapJson.getString("goodsname");
                                    pngurl = soapJson.getString("pngurl");
                                    // 名称知道了，要设置
                                    iView.setgoodsname(goodscode,goodsname);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();

                                code = 2;
                                msg = "解析返回值出错啦"+e.getMessage();
                            }
                            if(code != 0){
                                iserror = true;
                                iView.downgoodsinfoerror("取得商品编号="+goodscode+"的信息时，出错了："+msg);
                                break;
                            }
                        }

                        // 已经存在了,那么先删除,再下载图片
                        file = new File( Environment.getExternalStorageDirectory() + "/goodspng/" + Gmethod.getFileName(pngurl));
                        if (file.exists()) {
                            boolean deleteresult = file.delete();
                            LogUtils.i("文件" + Environment.getExternalStorageDirectory() + "/goodspng/" + Gmethod.getFileName(pngurl) + "的delete结果:" + deleteresult);
                        }

                        iView.downgoodsinfodoing("正在下载文件："+Gmethod.getFileName(pngurl));

                        try {
                            LogUtils.i("URL=" + pngurl);
                            URL url = new URL(pngurl);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(5000);
                            conn.setRequestMethod("GET");
                            conn.setDoInput(true);
                            if (conn.getResponseCode() == 200) {

                                InputStream is = conn.getInputStream();
                                FileOutputStream fos = new FileOutputStream(file);
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    fos.write(buffer, 0, len);
                                }
                                is.close();
                                fos.close();

                            }


                        } catch (Exception ex) {
                            ex.printStackTrace();

                            iserror = true;
                            iView.downgoodsinfoerror("正在下载文件："+ Gmethod.getFileName(pngurl)+"出错了:"+ex.getMessage());

                            break;
                        }

                    }

                    if(!iserror) {
                        // 开始做下架处理
                        ArrayList<Map<String,String>> newList = new ArrayList<>();

                        for(int i=0;i<arrayList.size();i++){
                            HashMap<String,Object> hmarray = arrayList.get(i);
                            String code = (String)hmarray.get("code");
                            int index = (Integer)hmarray.get("index");

                            if(index >= 110){
                                // 副柜，暂时没有
                            } else {
                                if(!myApp.getTrackMainGeneral()[index - 10].getGoodscode().equals(code)) {
                                    // 说明商品有变化
                                    if (myApp.getTrackMainGeneral()[index - 10].getNumnow() > 0) {
                                        Gmethod.doHm(newList, "" + index,
                                                myApp.getTrackMainGeneral()[index - 10].getGoodscode(),
                                                myApp.getTrackMainGeneral()[index - 10].getGoodsname(),
                                                myApp.getTrackMainGeneral()[index - 10].getBatchinfo(),
                                                myApp.getTrackMainGeneral()[index - 10].getEndday(),
                                                (-1) * myApp.getTrackMainGeneral()[index - 10].getNumnow());
                                    }
                                }
                            }
                        }

                        String data = "";
                        for(int i=0;i<newList.size();i++){
                            Map<String,String> hm = newList.get(i);
                            data = data + "" + hm.get("trackgoodscode") + "," +
                                    //hm.get("trackgoodsname") + "," +
                                    "" + "," +
                                    hm.get("batch") + "," +
                                    hm.get("endday") + "," +
                                    hm.get("change") + "," +
                                    hm.get("detail") + ";";
                        }

                        int codejsono = 0;
                        String msg = "";
                        if(data.length() > 0){
                            data = data.substring(0,data.length()-1);
                            // 有数据变法才需要通知后台
                            long timestamp = System.currentTimeMillis();
                            String str = "data="+data + "&macid="+myApp.getMainMacID() + "&supplycode=" + myApp.getTempSupplyCode() + "&type=4" + "&userid=" + myApp.getTempUserId()+
                                    "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                            String md5 = MD5.GetMD5Code(str);

                            String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendstockevent",
                                    "macid="+myApp.getMainMacID() + "&data="+data + "&type=4" + "&supplycode=" + myApp.getTempSupplyCode() + "&userid=" + myApp.getTempUserId() +
                                            "&timestamp="+timestamp + "&md5=" + md5);

                            if(rtnstr.length()==0) {
                                iView.downgoodsinfoerror("联网失败");
                            } else {
                                codejsono = 2;
                                msg = "解析返回值出错啦";
                                try {
                                    JSONObject soapJson = new JSONObject(rtnstr);
                                    codejsono = soapJson.getInt("code");
                                    msg = soapJson.getString("msg");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    msg = e.getMessage();
                                }
                            }
                        }


                        if(codejsono == 0){
                            // 完成数据库的修改
                            SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                            for(int i=0;i<arrayList.size();i++){
                                HashMap<String,Object> hmarray = arrayList.get(i);
                                String code = (String)hmarray.get("code");
                                int index = (Integer)hmarray.get("index");
                                String name = (String)hmarray.get("name");
                                String price = (String)hmarray.get("price");
                                //System.out.println("====="+price);
                                String[] pricea = price.split("_");
                                int paycash = Integer.parseInt(pricea[0]);
                                int payali = Integer.parseInt(pricea[1]);
                                int paywx = Integer.parseInt(pricea[2]);
                                int paymember = Integer.parseInt(pricea[3]);

                                if(index >= 110){
                                    // 副柜，暂时没有
                                } else {

                                    if(!myApp.getTrackMainGeneral()[index - 10].getGoodscode().equals(code)) {
                                        db.execSQL("update trackmaingeneral set numnow=0,goodscode='" + code + "',mingcheng='" + name + "',paycash=" + paycash +
                                                ",payali=" + payali + ",paywx=" + paywx + ",paymember=" + paymember + " where id=" + index);
                                        myApp.getTrackMainGeneral()[index - 10].setNumnow(0);
                                    } else {
                                        db.execSQL("update trackmaingeneral set goodscode='" + code + "',mingcheng='" + name + "',paycash=" + paycash +
                                                ",payali=" + payali + ",paywx=" + paywx + ",paymember=" + paymember + " where id=" + index);
                                    }
                                    myApp.getTrackMainGeneral()[index - 10].setGoodscode(code);
                                    myApp.getTrackMainGeneral()[index - 10].setGoodsname(name);
                                    myApp.getTrackMainGeneral()[index - 10].setPaycash(paycash);
                                    myApp.getTrackMainGeneral()[index - 10].setPayali(payali);
                                    myApp.getTrackMainGeneral()[index - 10].setPaywx(paywx);
                                    myApp.getTrackMainGeneral()[index - 10].setPaymember(paymember);
                                }

                            }
                            db.close();

                            iView.downgoodsinfook();
                        } else {
                            iView.downgoodsinfoerror(msg);
                        }




                    } 

                } catch (Exception e){
                    e.printStackTrace();

                    iView.downgoodsinfoerror(e.getMessage());
                }
            }
        });

        thread.start();

    }

}

