package com.mnevent.hz.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.MainActivity;
import com.mnevent.hz.Service.HeartbeatService;
import com.mnevent.hz.Utils.LogUtils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals("com.dbjtech.waiqin.destroy")) {
            //TODO
            //在这里写重新启动service的相关操作
            LogUtils.i("BootReceiver::"+"重启server");
            Intent intents = new Intent(MyApp.mApplication, HeartbeatService.class);
            MyApp.mApplication.startService(intents);
        }
    }
}
