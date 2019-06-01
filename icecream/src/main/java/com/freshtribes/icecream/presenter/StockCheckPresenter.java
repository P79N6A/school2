package com.freshtribes.icecream.presenter;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.IStockCheckView;

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

public class StockCheckPresenter {
    private IStockCheckView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    public StockCheckPresenter(IStockCheckView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void saveStockCheck(final ArrayList<Map<String,String>> arrayList){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Map<String,String>> newList = new ArrayList<>();

                // 数据发送到服务器
                for(int i=0;i<arrayList.size();i++){
                    Map<String,String> hm = arrayList.get(i);
                    int changelocal = Integer.parseInt(hm.get("realnum")) - Integer.parseInt(hm.get("xitongnum"));
                    if (changelocal != 0) {
                        Gmethod.doHm(newList,hm.get("trackno").replace("副柜 > 轨道：","1").replace("轨道：","").replace("货道：",""),
                                hm.get("trackgoodscode"),hm.get("trackgoodsname"),hm.get("batch"),hm.get("endday"),changelocal);
                    }

                }

                String data = "";
                for(int i=0;i<newList.size();i++){
                    Map<String,String> hm = newList.get(i);
                    data = data + hm.get("trackgoodscode") + "," +
                            // hm.get("trackgoodsname") + "," +
                            "" + "," +
                            hm.get("batch") + "," +
                            hm.get("endday") + "," +
                            hm.get("change") + "," +
                            hm.get("detail") + ";";
                }
                if(data.length() > 0){
                    data = data.substring(0,data.length()-1);
                }

                long timestamp = System.currentTimeMillis();
                String str = "data="+data + "&macid="+myApp.getMainMacID()  + "&supplycode=" + myApp.getTempSupplyCode() + "&userid=" + myApp.getTempUserId() +
                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendcheckresult",
                        "macid="+myApp.getMainMacID() + "&data="+data +  "&supplycode=" + myApp.getTempSupplyCode() + "&userid=" + myApp.getTempUserId()+
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
                                int realnum = Integer.parseInt(hm.get("realnum"));
                                int changelocal = realnum - Integer.parseInt(hm.get("xitongnum"));
                                if (changelocal != 0) {
                                    if (trackno.contains("副柜 > 轨道：")) {
                                        // 副柜没有
                                    } else  {
                                        int inttrackno = Integer.parseInt(trackno.replace("轨道：", ""));

                                        db.execSQL("update trackmaingeneral set numnow=" + realnum + " where id=" + inttrackno);

                                        myApp.getTrackMainGeneral()[inttrackno - 10].setNumnow(realnum);
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

//    private void doHm(ArrayList<Map<String,String>> newList,String trackno,String code,String name,String batch,String endday,int change){
//        boolean ishave = false;
//        for(int i=0;i<newList.size();i++){
//            Map<String,String> hm = newList.get(i);
//
//            if(hm.get("trackgoodscode").equals(code) &&
//                    hm.get("trackgoodsname").equals(name) &&
//                    hm.get("batch").equals(batch) &&
//                    hm.get("endday").equals(endday)){
//                ishave = true;
//
//                int changetotal = Integer.parseInt(hm.get("change").replace("+",""));
//                changetotal = changetotal + change;
//
//                if(changetotal > 0){
//                    hm.put("change","+"+changetotal);
//                } else if(changetotal < 0){
//                    hm.put("change",""+changetotal);
//                } else {
//                    hm.put("change","0");
//                }
//
//                String detail = hm.get("detail");
//                if(change > 0){
//                    detail = detail + "_" + trackno+ "+" + change;
//                } else {
//                    detail = detail + "_" + trackno+ "" + change;
//                }
//                hm.put("detail",detail);
//
//                break;
//            }
//
//        }
//
//        if(!ishave){
//            Map<String,String> hm = new HashMap<>();
//            hm.put("trackgoodscode",code);
//            hm.put("trackgoodsname", name);
//            hm.put("batch",batch);
//            hm.put("endday",endday);
//
//            if(change > 0){
//                hm.put("change","+"+change);
//                hm.put("detail",trackno+"+"+change);
//            } else{
//                hm.put("change",""+change);
//                hm.put("detail",trackno+""+change);
//            }
//            newList.add(hm);
//        }
//    }



}

