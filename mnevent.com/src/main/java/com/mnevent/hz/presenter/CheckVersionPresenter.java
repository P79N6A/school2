package com.mnevent.hz.presenter;



import com.mnevent.hz.Activity.ProductoptionActivity;
import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.Utils.HttpUtils;
import com.mnevent.hz.Utils.MD5;
import com.mnevent.hz.Utils.MyHttp;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.interfaces.ICheckVersionView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/8/22.
 */

public class CheckVersionPresenter {
    private ICheckVersionView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;
    String serialNumber,password;

    public CheckVersionPresenter(ICheckVersionView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void getServerApkVersion(final String packagename){
        // com.freshtribes.seller
        serialNumber = PrefheUtils.getString(myApp.getApplicationContext(), "serialNumber", "");
        password = PrefheUtils.getString(myApp.getApplicationContext(), "password", "");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                String str = "apkname="+packagename + "&macid="+serialNumber +
                        "&timestamp="+timestamp + "&accesskey=" + password;
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(HttpUtils.apkver,
                        "apkname="+packagename + "&macid="+serialNumber +
                                "&timestamp="+timestamp + "&md5=" + md5);

                if(rtnstr.length()==0){
                    iView.downloadError("联网失败");
                } else {
                    // {"msg":"","code":0,"carriersn":"工厂车间"}
                    int code = 2;
                    String msg = "解析返回值出错啦";
                    String dlurl = "";
                    String apkver = "";
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        code = soapJson.getInt("code");
                        msg = soapJson.getString("msg");
                        if(code == 0) {
                            dlurl = soapJson.getString("dlurl");
                            String intapkverstr = soapJson.getString("apkver");
                            apkver = intapkverstr.substring(0,intapkverstr.length()-2) + "." +
                                    intapkverstr.substring(intapkverstr.length()-2,intapkverstr.length()-1) + "." +
                                    intapkverstr.substring(intapkverstr.length()-1,intapkverstr.length());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        msg = e.getMessage();
                    }
                    if(code == 0){
                        iView.downloadOK(dlurl,apkver);
                    } else {
                        iView.downloadError(msg);
                    }
                }
            }
        });

        thread.start();
    }

}

