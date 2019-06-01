package com.freshtribes.icecream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.presenter.LoadingPresenter;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.view.ILoadingView;

import java.text.SimpleDateFormat;


public class LoadingActivity extends Activity  implements ILoadingView,View.OnClickListener{
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    private ThreadGroup tg = new ThreadGroup("LoadingActivityThreadGroup");

    private LoadingPresenter lp;
    private static final int dispHandler_rtnLoadDataOver = 0x101;
    private static final int dispHandler_dispMessage = 0x102;

    // 底部的提示语
    private TextView tvhintinfo;

    //mode按键-------------------
    protected static final int Goto_Menu = 0x540; // Handle的消息标志：跳转到维护页面
    // 广播
    private ModeBroadCastReceiver modeswitch;
    //mode按键-------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // 生成Presenter
        lp = new LoadingPresenter(this,tg,(MyApp)getApplication());

        tvhintinfo = (TextView)findViewById(R.id.hintinfo);
        tvhintinfo.setText("正在准备基础数据......");

        // 获取参数
        String starti = "appstart";
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null) {
            String param = b.getString("param");
            starti = param;
            if(starti==null) starti = "appstart";
        }


        try {
            // 获取设备信息
            TelephonyManager tm = (TelephonyManager) LoadingActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            String imsi = tm.getSubscriberId();
            String mtype = android.os.Build.MODEL; // 手机型号
            String mtyb = android.os.Build.BRAND;//手机品牌
            LogUtils.i("手机IMEI号：" + imei + ";手机IESI号：" + imsi + ";手机型号：" + mtype + ";手机品牌：" + mtyb );
        }catch(SecurityException e){
            e.printStackTrace();
        }


    //应用程序最大可用内存
        int maxMemory = ((int) Runtime.getRuntime().maxMemory())/1024/1024;
        //应用程序已获得内存
        long totalMemory = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
        //应用程序已获得内存中未使用内存
        long freeMemory = ((int) Runtime.getRuntime().freeMemory())/1024/1024;
        LogUtils.i("LoadingActivity OnStart" + starti + " " + Gmethod.getAppVersion(LoadingActivity.this)+" ---> 应用程序最大可用内存="+maxMemory+"M,应用程序已获得内存="+totalMemory+"M,应用程序已获得内存中未使用内存="+freeMemory+"M");

        // 发广播,告诉VMSelfStart,已经启动了,请持续监测,发现没启动需要启动本apk哦!
        if (!starti.startsWith("fromvmselfstart")) {
            // 说明不是来自于监测的线程
            Intent intentMonitor = new Intent("com.mengdeman.vmselfstart.monitor.START");
            sendBroadcast(intentMonitor);
        }

        // 读取配置文件数据
        lp.getSettingInfo(starti,sdf.format(System.currentTimeMillis()));

        Intent intentRestart = new Intent("com.ubox.auto_power_shut");
        intentRestart.putExtra("power_time", "02:06");
        intentRestart.putExtra("shut_time", "02:01");
        intentRestart.putExtra("effective", false);
        sendBroadcast(intentRestart);
    }

    @Override
    public void loadingDataOver(String status){
        Message msg = dispHandler.obtainMessage(dispHandler_rtnLoadDataOver);
        msg.obj = status;
        dispHandler.sendMessage(msg);
    }

    @Override
    public void dispMessage(String msgstr){
        Message msg = dispHandler.obtainMessage(dispHandler_dispMessage);
        msg.obj = msgstr;
        dispHandler.sendMessage(msg);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent=new Intent("com.mengdeman.vmselfstart.monitor.STOP");
        sendBroadcast(intent);
        finish();

        System.exit(0);

    }

    //内部类
    // 定义一个mode开关的接收器
    class ModeBroadCastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if (action.equals("android.intent.action.ENG_MODE_SWITCH")) {

                Message msg = dispHandler.obtainMessage();
                msg.what = Goto_Menu;
                dispHandler.sendMessage(msg);

            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        //生成广播处理-----------------------------
        modeswitch = new ModeBroadCastReceiver();
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter("android.intent.action.ENG_MODE_SWITCH");
        //注册
        LoadingActivity.this.registerReceiver(modeswitch, intentFilter);
        //生成广播处理-----------------------------
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        //解除注册广播-----------------------------
        LoadingActivity.this.unregisterReceiver(modeswitch);
        //解除注册广播-----------------------------
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
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
                case Goto_Menu:
                    // 关闭presenter的所有线程，然后跳转
                    lp.setStopThread();
                    while(tg.activeCount()>0)for(int i=0;i<1000;i++);
                    if(!tg.isDestroyed()) tg.destroy();

                    Intent toMenu2 = new Intent(LoadingActivity.this, MenuActivity.class);
                    startActivity(toMenu2);
                    LoadingActivity.this.finish();
                    break;


                case dispHandler_rtnLoadDataOver:

                    long start = System.currentTimeMillis();

                    // 关闭presenter的所有线程，然后跳转
                    lp.setStopThread();
                    long tgnow = System.currentTimeMillis();

                    while(tg.activeCount()>0){
                        for(int i=0;i<1000;i++);
                        if(System.currentTimeMillis() - tgnow >  3000) break;
                        //LogUtils.i("Loading "+tg.activeCount()+" for循环结束"+(System.currentTimeMillis()-start));
                    }
                    tg.destroy();

                    String status = msg.obj.toString();
                    if(status.equals("Tech mode")){
                        tvhintinfo.setText("当前为维护模式，请切换为用户模式后，再重新启动...");
                    } else {
                        if (((MyApp) getApplication()).getMainMacID().length() == 0) {
                            Intent toMenu = new Intent(LoadingActivity.this, MacParaActivity.class);
                            startActivity(toMenu);
                            LoadingActivity.this.finish();
                        } else {
                            LogUtils.i("Loading 检测完后，进入Main" + (System.currentTimeMillis() - start));

                            Intent toMenu = new Intent(LoadingActivity.this, MainActivity.class);
                            startActivity(toMenu);
                            LoadingActivity.this.finish();
                        }
                    }
                    break;

                case dispHandler_dispMessage:
                    tvhintinfo.setText("正在准备基础数据......" + msg.obj);
                    break;

                default:
                    break;
            }
            return false;
        }
    });
}
