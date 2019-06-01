package com.freshtribes.icecream.presenter;


import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.IMenuView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/8/22.
 */

public class MenuPresenter {
    private IMenuView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    public MenuPresenter(IMenuView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void getVersionInfo(final String packagename){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                if(myApp.getMainMacID().length() > 0){
                    int code = 2;
                    String msg = "解析返回值出错";
                    long trackplanid = 0l;
                    String apkver = "1.0.0";
                    // 获取服务器的广告ID
                    long timestamp = System.currentTimeMillis();
                    String str = "macid="+ myApp.getMainMacID() + "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                    String md5 = MD5.GetMD5Code(str);

                    String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/trackplaninfo",
                            "macid="+myApp.getMainMacID() + "&timestamp="+timestamp + "&md5=" + md5);

                    if(rtnstr.length() > 0){
                        try {
                            JSONObject soapJson = new JSONObject(rtnstr);
                            code = soapJson.getInt("code");
                            msg = soapJson.getString("msg");
                            if(code == 0) {
                                trackplanid = soapJson.getLong("planid");
                            } else {
                                iView.getVersionError(msg);
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //msg = e.getMessage();
                        }
                    } else {
                        iView.getVersionError("联网失败");
                        return;
                    }

                    timestamp = System.currentTimeMillis();
                    str = "apkname="+packagename + "&macid="+myApp.getMainMacID() + "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                    md5 = MD5.GetMD5Code(str);

                    rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/apkver",
                            "apkname="+packagename + "&macid="+myApp.getMainMacID() + "&timestamp="+timestamp + "&md5=" + md5);
                    code = 2;
                    msg = "解析返回值出错";
                    if(rtnstr.length() > 0){
                        try {
                            JSONObject soapJson = new JSONObject(rtnstr);
                            code = soapJson.getInt("code");
                            msg = soapJson.getString("msg");
                            if(code == 0) {
                                String intapkverstr = soapJson.getString("apkver");
                                apkver = intapkverstr.substring(0,intapkverstr.length()-2) + "." +
                                        intapkverstr.substring(intapkverstr.length()-2,intapkverstr.length()-1) + "." +
                                        intapkverstr.substring(intapkverstr.length()-1,intapkverstr.length());
                            } else {
                                iView.getVersionError(msg);
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //msg = e.getMessage();
                        }
                    } else {
                        iView.getVersionError("联网失败");
                        return;
                    }

                    iView.rtnGetVersionInfo(0,trackplanid,apkver);

                } else {
                    iView.rtnGetVersionInfo(0l,0l,"1.0.0");
                }
            }
        });

        thread.start();
    }

}

