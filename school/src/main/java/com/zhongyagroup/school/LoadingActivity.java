package com.zhongyagroup.school;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zhongyagroup.school.app.MyApp;
import com.zhongyagroup.school.bean.TrackBean;
import com.zhongyagroup.school.util.Gmethod;
import com.zhongyagroup.school.util.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;

import android_serialport_api.ComTrack;

public class LoadingActivity extends Activity {
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final int dispHandler_rtnLoadDataOver = 0x101;

    private MyApp myApp;
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

        myApp = (MyApp)getApplication();

        tvhintinfo = (TextView)findViewById(R.id.hintinfo);
        tvhintinfo.setText("正在准备基础数据......");

        // 获取参数
        String starti = "appstart";
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null) {
            String param = b.getString("param");
            starti = param;
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sp =  myApp.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);

                    myApp.setMainMacID(sp.getString("mainMacID", ""));
                    myApp.setMainMacType(sp.getString("mainMacType", "general"));
                    myApp.setAccessKey(sp.getString("accessKey", "00000000"));

                    myApp.setMainGeneralLevelNum(sp.getInt("mainGeneralLevelNum", 6));
                    myApp.setMainGeneralLevel1TrackCount(sp.getInt("mainGeneralLevel1TrackCount", 10));
                    myApp.setMainGeneralLevel2TrackCount(sp.getInt("mainGeneralLevel2TrackCount", 10));
                    myApp.setMainGeneralLevel3TrackCount(sp.getInt("mainGeneralLevel3TrackCount", 10));
                    myApp.setMainGeneralLevel4TrackCount(sp.getInt("mainGeneralLevel4TrackCount", 10));
                    myApp.setMainGeneralLevel5TrackCount(sp.getInt("mainGeneralLevel5TrackCount", 10));
                    myApp.setMainGeneralLevel6TrackCount(sp.getInt("mainGeneralLevel6TrackCount", 10));
                    myApp.setMainGeneralLevel7TrackCount(sp.getInt("mainGeneralLevel7TrackCount", 10));

                    myApp.setStockchecktime(sp.getString("stockchecktime",""));

                    myApp.setTrackouttype(sp.getInt("trackouttype",0));//"0:多的先出","1:前面的先出","2:后面的先出"

                } catch (Exception ex){
                    ex.printStackTrace();
                }

                if(myApp.getMainMacID().contains("test")){
                    // 如果是测试的机器编号
                    ComTrack.isTestMode = true;
                }

                // 读取数据库文件,先判断数据库文件是否存在
                // 取得数据库默认路径
                String dbfilepath = myApp.getDatabasePath("vmdata.db").getAbsolutePath();
                File dbfile = new File(dbfilepath);
                boolean dbfileexist = dbfile.exists();

                String sqlstr = "";
                for(int i = 10; i <= 79; i++) {
                    if(myApp.getTrackMainGeneral()[i-10]==null){
                        myApp.getTrackMainGeneral()[i - 10] = new TrackBean();
                    }
                }

                if(!dbfileexist){
                    // 首次运行
                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                    LogUtils.i("没有数据库文件，需要重新生成");

                    db.execSQL("CREATE TABLE trackmaingeneral(id INTEGER PRIMARY KEY,"  //从10到79为止
                            + "goodscode nvarchar(5),"
                            + "mingcheng nvarchar(24),"
                            + "payprice int,"   //最大：999.99
                            + "nummax int,"
                            + "numnow int,"
                            + "errorcode nvarchar(8),"
                            + "errortime nvarchar(14))");
                    for (int i = 10; i <= 79; i++)
                    {
                        sqlstr = "INSERT INTO trackmaingeneral(id,goodscode,mingcheng,payprice,nummax,numnow,errorcode,errortime) " +
                                "VALUES (" + i + ",'','',1000,10,0,'','')";
                        db.execSQL(sqlstr);
                    }

                    // 商品和价格表
                    db.execSQL("CREATE TABLE goodsname(goodscode nvarchar(5) PRIMARY KEY,"
                            + "goodsname nvarchar(24),"
                            + "payprice int)");

                    db.close();

                } else {
                    // 读取
                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                    LogUtils.i("数据库文件已存在，进行数据读取");

                    // 综合机 10-79
                    Cursor c = db.rawQuery("SELECT id,goodscode,mingcheng,payprice,nummax,numnow,errorcode,errortime " +
                            "FROM trackmaingeneral", null);
                    while (c.moveToNext())
                    {
                        int huohao = c.getInt(c.getColumnIndex("id")); // 取出的值是从10开始的 : 10-79
                        myApp.getTrackMainGeneral()[huohao - 10].setGoodscode(c.getString(1));
                        myApp.getTrackMainGeneral()[huohao - 10].setGoodsname(c.getString(2));
                        myApp.getTrackMainGeneral()[huohao - 10].setPayprice(c.getInt(3));

                        myApp.getTrackMainGeneral()[huohao - 10].setNummax(c.getInt(4));
                        myApp.getTrackMainGeneral()[huohao - 10].setNumnow(c.getInt(5));

                        myApp.getTrackMainGeneral()[huohao - 10].setErrorcode(c.getString(6));
                        myApp.getTrackMainGeneral()[huohao - 10].setErrortime(c.getString(6));
                    }
                    c.close();

                    db.close();
                }


                // 对驱动板进行复位操作
                if(!ComTrack.isTestMode){
                    // 无主控的驱动板进行复位操作
                    ComTrack.snindex = 0;
                    ComTrack comTrack =  new ComTrack("");
                    comTrack.openSerialPort();
                    comTrack.reset();
                    comTrack.closeSerialPort();
                }

                dispHandler.sendEmptyMessage(dispHandler_rtnLoadDataOver);

            }
        },"启动线程"+sdf.format(System.currentTimeMillis())).start();
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
                    Intent toMenu2 = new Intent(LoadingActivity.this, MenuActivity.class);
                    startActivity(toMenu2);
                    LoadingActivity.this.finish();
                    break;

                case dispHandler_rtnLoadDataOver:

                    if (myApp.getMainMacID().length() == 0) {
                        Intent toMenu = new Intent(LoadingActivity.this, MacParaActivity.class);
                        startActivity(toMenu);
                        LoadingActivity.this.finish();
                    } else {
                        Intent toMenu = new Intent(LoadingActivity.this, MainActivity.class);
//                            Intent toMenu = new Intent(LoadingActivity.this, MenuActivity.class);
                        startActivity(toMenu);
                        LoadingActivity.this.finish();
                    }

                    break;



                default:
                    break;
            }
            return false;
        }
    });

}
