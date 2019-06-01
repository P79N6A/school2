package com.freshtribes.icecream.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.IMacParaView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Administrator on 2017/8/22.
 */

public class MacParaPresenter {
    private IMacParaView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    public MacParaPresenter(IMacParaView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void getCarrier(final String mainmacid, final String submmacid, final String maintype, final String accesskey){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                String str = "macid="+mainmacid + "&maintype="+maintype + "&submacid="+submmacid  +
                        "&timestamp="+timestamp + "&accesskey=" + accesskey;
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/getcarrier",
                        "macid="+mainmacid + "&submacid="+submmacid + "&maintype="+maintype +
                        "&timestamp="+timestamp + "&md5=" + md5);
                Log.i("ss",rtnstr);
                if(rtnstr.length()==0){
                    iView.getCarrier(1,"联网失败","");
                } else {
                    // {"msg":"","code":0,"carriersn":"工厂车间"}
                    int code = 2;
                    String msg = "解析返回值出错啦";
                    String carriersn = "";
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        code = soapJson.getInt("code");
                        msg = soapJson.getString("msg");
                        if(code == 0)
                            carriersn = soapJson.getString("carriersn");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    iView.getCarrier(code,msg,carriersn);
                }
            }
        });

        thread.start();

    }

}

