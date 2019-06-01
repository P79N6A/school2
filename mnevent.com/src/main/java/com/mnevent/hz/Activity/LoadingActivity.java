package com.mnevent.hz.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.TextView;


import com.igexin.sdk.PushManager;
import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.MainActivity;
import com.mnevent.hz.R;
import com.mnevent.hz.Utils.Log;
import com.mnevent.hz.Utils.LogUtils;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.getui.GetuiPushService;
import com.mnevent.hz.getui.GetuiService;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.litemolder.ProductlibMolder;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

import android_serialport_api.ComQRIC;

/**
 * 打开app的加载界面
 */
public class LoadingActivity extends Activity {

    private MyApp myApp;

    private static final int dispHandler_goto_menu = 0x100;
    private static final int dispHandler_goto_disp = 0x101;
    // 底部的提示语
    private TextView tvhintinfo;

    SQLiteDatabase db;
    //数据库数据
    List<PathwayMolder> patall;

    //编号
    String serialNumber;
    //密码
    String password;

    String classname = "";

    String[] codelist = new String[]{"03021","03042","03044","03051","03060","05222","05223","05224","05226","11020","11021","11022"};

    private List<String> inv = new ArrayList<>();

    PathwayMolder molder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        classname = getClass().getSimpleName();
        LogUtils.d("ACTIVITY："+getClass().getSimpleName());
        myApp = (MyApp)getApplication();
        db = Connector.getDatabase();
     //   LitePal.deleteAll(ProductlibMolder.class);

        // 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), GetuiService.class);
        // 为第三方自定义推送服务
        PushManager.getInstance().initialize(this.getApplicationContext(), GetuiPushService.class);
        LogUtils.d(classname + ":::cid:"+PushManager.getInstance().getClientid(getApplicationContext()) );


        //Log.d("zlc","patall.get(z)"+Proall.get(1).getMolder().getCode());
        tvhintinfo = findViewById(R.id.hintinfo);
        tvhintinfo.setText("Basic data is being prepared......");


    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d(classname + "start.....");
        serialNumber = PrefheUtils.getString(LoadingActivity.this, "serialNumber", "");
        password = PrefheUtils.getString(LoadingActivity.this, "password", "");

        //获取数据库数据
        patall = LitePal.findAll(PathwayMolder.class);
        LogUtils.d(classname + "数据库数据："+patall.size());
        new Thread(new getSPIfoThread()).start();
    }


    /**
     * 判断PathwayMolder数据库里面是否有正确的商品
     */
    private class getSPIfoThread extends Thread{
        @Override
        public void run() {

            boolean isSetted = false;
            if(!TextUtils.isEmpty(serialNumber) && !TextUtils.isEmpty(password)){
                for(int i=0;i<patall.size();i++){

                    if(patall.get(i).getPds().equals("1") /*&& Integer.parseInt(patall.get(i).getInventory()) > 0*/ && patall.get(i).getNummax()>0 && patall.get(i).getUp().equals("1")){

                        isSetted = true;
                        break;
                    }
                }
            }



            // 暂停2秒
            SystemClock.sleep(1000);

            if(isSetted){
                dispHandler.sendEmptyMessage(dispHandler_goto_disp);

            } else {
                dispHandler.sendEmptyMessage(dispHandler_goto_menu);
            }

        }

    }

    /**
     * Handle线程，显示处理结果
     */
    Handler dispHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // 处理消息
            switch (msg.what) {

                case dispHandler_goto_disp:
                    Intent toDisp = new Intent(LoadingActivity.this, MainActivity.class);

                    startActivity(toDisp);

                 //   LoadingActivity.this.finish();
                    break;

                case dispHandler_goto_menu:
                    Intent toMenu = new Intent(LoadingActivity.this, MenuActivity.class);
                    startActivity(toMenu);
                   // LoadingActivity.this.finish();
                    break;

                default:
                    break;
            }
            return  false;
        }
    });


}
