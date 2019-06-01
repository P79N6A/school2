package com.zhongyagroup.school.service;

import android.content.Context;
import android.util.Log;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.zhongyagroup.school.util.LogUtils;

import java.io.UnsupportedEncodingException;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class SellerIntentService extends GTIntentService{

    public SellerIntentService(){
    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        String data = new String(msg.getPayload());
        LogUtils.i("收到透传的数据："+data);
        if(data.length() > 7 && data.substring(0,7).equals("alipay,")){
            //MainActivity.queuealipay.offer(data.substring(7));
            //AliqrpayActivity.queuealipay.offer(data.substring(7));
        } else if(data.length() > 7 && data.substring(0,7).equals("umspay,")){
            //MainActivity.queueumspay.offer(data.substring(7));
            //AlifacepayActivity.queueumspay.offer(data.substring(7));
        } else if(data.length() > 7 && data.substring(0,7).equals("weixin,")){
            //MainActivity.queueweixin.offer(data.substring(7));
            //WxqrpayActivity.queueweixin.offer(data.substring(7));
        } else if(data.length() > 7 && data.substring(0,7).equals("tranin,")){
            //MainActivity.queuetraninmac.offer(data.substring(7));
        } else if(data.length() > 8 && data.substring(0,8).equals("tranout,")){
            //MainActivity.queuetranoutmac.offer(data.substring(8));
        } else if(data.length() > 9 && data.substring(0,9).equals("stockout,")){
            //MainActivity.queuestockoutmac.offer(data.substring(9));
        } else if(data.length() > 11 && data.substring(0,11).equals("checkstock,")){
            //MainActivity.queuecheckstockmac.offer(data.substring(11));
        } else if(data.length() > 13 && data.substring(0,13).equals("supplyresult,")) {
            //MainActivity.queuesupplyresultmac.offer(data.substring(13));
        } else if(data.length() > 5 && data.substring(0,5).equals("push,")){
            //MainActivity.queuepush.offer(data.substring(5));
        } else if(data.length() > 8 && data.substring(0,8).equals("pushlog,")){
            //MainActivity.queuepushgetlog.offer(data.substring(8));
            //MainNewActivity.queuepushgetlog.offer(data.substring(8));
            //AlimainActivity.queuepushgetlog.offer(data.substring(8));
        } else if(data.length() > 10 && data.substring(0,10).equals("pushphoto,")){
            //MainActivity.queuepushgetphoto.offer(data.substring(10));
            //MainNewActivity.queuepushgetphoto.offer(data.substring(10));
            //AlimainActivity.queuepushgetphoto.offer(data.substring(10));
        } else {
            // 无法识别的数据
            LogUtils.e("收到无法识别的透传数据："+data);
        }
    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        Log.e(TAG, "onReceiveClientId -> " + "clientid = " + clientid);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
    }
}
