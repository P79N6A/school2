package com.freshtribes.icecream.presenter;


import android.util.Log;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.baiduai.APIService;
import com.freshtribes.icecream.baiduai.exception.FaceError;
import com.freshtribes.icecream.baiduai.model.RegResult;
import com.freshtribes.icecream.baiduai.utils.OnResultListener;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.IFaceLoginView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Administrator on 2017/8/22.
 */

public class FaceLoginPresenter {
    private IFaceLoginView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    public FaceLoginPresenter(IFaceLoginView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    public void sendsms(final String mobile){
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {

                int code = 2;
                String msg = "解析返回值出错";
                // 获取服务器的广告ID
                long timestamp = System.currentTimeMillis();
                String str = "macid="+myApp.getMainMacID()  + "&mobile="+mobile + "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/baidusendsms",
                        "macid="+myApp.getMainMacID()  + "&mobile="+mobile + "&timestamp="+timestamp + "&md5=" + md5);

                if(rtnstr.length() > 0){
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        code = soapJson.getInt("code");
                        msg = soapJson.getString("msg");
                        if(code == 0) {
                            iView.sendsmsresult("");
                            return;
                        } else {
                            iView.sendsmsresult(msg);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //msg = e.getMessage();
                        iView.sendsmsresult(e.getMessage());
                        return;
                    }
                } else {
                    iView.sendsmsresult("联网失败");
                    return;
                }

            }
        });

        thread.start();
    }

    public void faceadd(final String mobile,final String smscode,final File imagefile){
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {

                int code = 2;
                String msg = "联网失败";
                // 获取服务器的广告ID
                long timestamp = System.currentTimeMillis();
                String str = "macid="+myApp.getMainMacID()  + "&mobile="+mobile +  "&smscode="+smscode +"&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/baiduaddface",
                        "macid="+myApp.getMainMacID()  + "&mobile="+mobile +  "&smscode="+smscode+ "&timestamp="+timestamp + "&md5=" + md5);
                LogUtils.i("baiduaddface,rtnstr:"+rtnstr);

                String user_info = "";
                String group_id = "";
                long uid = 0;
                String baidufacetime = "";
                String token = "";

                if(rtnstr.length() > 0){
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        code = soapJson.getInt("code");
                        msg = soapJson.getString("msg");
                        if(code == 0) {
                            user_info =  soapJson.getString("user_info");
                            group_id = soapJson.getString("group_id");
                            uid = Long.parseLong(soapJson.getString("uid"));
                            baidufacetime = soapJson.getString("baidufacetime");
                            token = soapJson.getString("accesstoken");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        msg = "异常:"+e.getMessage();
                    }
                }

                if(msg.length() > 0){
                    iView.baidufaceadd(msg);
                    return;
                }

                final String inner_baidufacetime = baidufacetime;

                // 开始上传图片
                APIService.getInstance().reg_chen(new OnResultListener<RegResult>() {
                    @Override
                    public void onResult(RegResult result) {
                        Log.i("wtf", "orientation->" + result.getJsonRes());

                        if(inner_baidufacetime.length() > 0){
                            iView.baidufaceadd("");
                        } else {
                            iView.baidufaceplus();
                        }
                    }

                    @Override
                    public void onError(FaceError error) {
                        iView.baidufaceadd(error.getErrorMessage());

                    }

                }, imagefile, ""+uid, user_info,group_id,token);

            }
        });

        thread.start();
    }


    public void addplus(final String mobile){
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                //int code = 2;
                String msg = "联网失败";
                // 获取服务器的广告ID
                long timestamp = System.currentTimeMillis();
                String str = "macid=" + myApp.getMainMacID() + "&mobile=" + mobile + "&timestamp=" + timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl() + "/baiduaddfaceplus",
                        "macid=" + myApp.getMainMacID() + "&mobile=" + mobile + "&timestamp=" + timestamp + "&md5=" + md5);
                LogUtils.i("baiduaddfaceplus,rtnstr:"+rtnstr);
                if (rtnstr.length() > 0) {
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        //code = soapJson.getInt("code");
                        msg = soapJson.getString("msg");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        msg = "异常:" + e.getMessage();
                    }
                }
                iView.baidufaceadd(msg);

            }
        });

        thread.start();
    }
}

