package com.freshtribes.icecream.presenter;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.IStockSupplyView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/22.
 */

public class StockSupplyPresenter {
    private IStockSupplyView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    public StockSupplyPresenter(IStockSupplyView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void saveStockSupply(final ArrayList<Map<String,String>> arrayList){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                ArrayList<Map<String,String>> newList = new ArrayList<>();

                // 数据发送到服务器
                for(int i=0;i<arrayList.size();i++){
                    Map<String,String> hm = arrayList.get(i);
                    int changelocal = Integer.parseInt(hm.get("supplynum")) - Integer.parseInt(hm.get("stocknum"));
                    if (changelocal != 0) {
                        Gmethod.doHm(newList,hm.get("trackno").replace("副柜 > 轨道：","1").replace("轨道：","").replace("货道：",""),
                                hm.get("trackgoodscode"),hm.get("trackgoodsname"),hm.get("batch"),hm.get("endday"),changelocal);
                    }

                }

                String data = "";
                for(int i=0;i<newList.size();i++){
                    Map<String,String> hm = newList.get(i);
                    data = data + hm.get("trackgoodscode") + "," +
                            hm.get("trackgoodsname") + "," +
                            hm.get("batch") + "," +
                            hm.get("endday") + "," +
                            hm.get("change") + "," +
                            hm.get("detail") + ";";
                }
                if(data.length() > 0){
                    data = data.substring(0,data.length()-1);
                }

                String data_encode = "";
                try {
                    data_encode = URLEncoder.encode(data.replace(" ","N"),"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                long timestamp = System.currentTimeMillis();
                String str = "data="+data_encode + "&macid="+myApp.getMainMacID()  +"&supplycode=" + myApp.getTempSupplyCode() + "&userid=" + myApp.getTempUserId() +
                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendsupplyresult",
                        "macid="+myApp.getMainMacID() + "&data="+data.replace(" ","N") +"&supplycode=" + myApp.getTempSupplyCode() + "&userid=" + myApp.getTempUserId() +
                                "&timestamp="+timestamp + "&md5=" + md5);

                if(rtnstr.length()==0){
                    iView.saveError("联网失败");
                } else {
                    int code = 2;
                    String msg = "解析返回值出错啦";
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        code = soapJson.getInt("code");
                        msg = soapJson.getString("msg");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(code == 0){
                        // 数据保存到本机数据库

                        try {
                            SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                            for(int i=0;i<arrayList.size();i++) {
                                Map<String, String> hm = arrayList.get(i);
                                String trackno = hm.get("trackno");
                                int supplynum = Integer.parseInt(hm.get("supplynum"));
                                int changelocal = supplynum - Integer.parseInt(hm.get("stocknum"));
                                if (changelocal != 0) {
                                    if (trackno.contains("副柜 > 轨道：")) {
                                        // 无副柜
                                    } else {
                                        int inttrackno = Integer.parseInt(trackno.replace("轨道：", ""));

                                        db.execSQL("update trackmaingeneral set numnow=" + supplynum + " where id=" + inttrackno);

                                        myApp.getTrackMainGeneral()[inttrackno - 10].setNumnow(supplynum);
                                    }
                                }
                            }
                            db.close();

                            iView.saveOK();

                        } catch (SQLException e) {
                            e.printStackTrace();

                            iView.saveError(e.getMessage());

                        }

                    } else {
                        iView.saveError(msg);
                    }
                }


            }
        });

        thread.start();

    }





}

