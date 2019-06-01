package com.zhongyagroup.school;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhongyagroup.school.app.MyApp;
import com.zhongyagroup.school.util.Gmethod;
import com.zhongyagroup.school.util.LogUtils;
import com.zhongyagroup.school.util.MD5;
import com.zhongyagroup.school.util.MyHttp;
import com.zhongyagroup.school.util.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class MenuActivity extends Activity implements View.OnClickListener{
    private Dialog mProgressDialog;

    private static final int dispHandler_rtnGetServerError = 0x101;
    private static final int dispHandler_rtnGetServerOK = 0x102;

    private MyApp myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        myApp = (MyApp)getApplication();

        SimpleDateFormat sdfSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        myApp.setTempSupplyCode(sdfSSS.format(System.currentTimeMillis()));

        TextView mainmacidtv = findViewById(R.id.mainmacid);
        mainmacidtv.setText("主机编号：" + ((MyApp) getApplication()).getMainMacID());


        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        Button stocksupplybtn = findViewById(R.id.btn_stocksupply);
        stocksupplybtn.setOnClickListener(this);

        Button stockcheckbtn = findViewById(R.id.btn_stockcheck);
        stockcheckbtn.setOnClickListener(this);


        Button checkversionbtn = findViewById(R.id.btn_checkversion);
        checkversionbtn.setOnClickListener(this);

        Button testbtnmain = findViewById(R.id.btn_testmain);
        testbtnmain.setOnClickListener(this);
        if(getError(1).length()>0){
            findViewById(R.id.test_newnoticemain).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.test_newnoticemain).setVisibility(View.INVISIBLE);
        }

        Button macparabtn = findViewById(R.id.btn_macpara);
        macparabtn.setOnClickListener(this);

        Button mainmaxbtn = findViewById(R.id.btn_mainmax);
        mainmaxbtn.setOnClickListener(this);


        Button goodspricebtn = findViewById(R.id.btn_goodsprice);
        goodspricebtn.setOnClickListener(this);


        Button exitbtn = findViewById(R.id.btn_exit);
        exitbtn.setOnClickListener(this);


        String laststockcheck = ((MyApp)getApplication()).getStockchecktime();
        if(laststockcheck.length() == 19){
            // 计算和现在时间的差距
            SimpleDateFormat sdfl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long chamin = Gmethod.getDistanceMin(laststockcheck,sdfl.format(System.currentTimeMillis()));
            if(chamin < 120){
                TextView tv = findViewById(R.id.last2hourstockcheck);
                tv.setText("最近盘点："+laststockcheck.substring(5));
            }
        }

        TextView supplycodetv = findViewById(R.id.supplycode);
        supplycodetv.setText("补货单号："+((MyApp)getApplication()).getTempSupplyCode());

        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.createLoadingDialog(MenuActivity.this, "正在连接服务器查看最新版本信息,请稍候...");
        }
        //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
        mProgressDialog.show();

        final String packagename = Gmethod.getPackageName(MenuActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(myApp.getMainMacID().length() > 0){
                    int code = 2;
                    String msg = "解析返回值出错";
                    long advertplanid = 0l;
                    long trackplanid = 0l;
                    String apkver = "1.0.0";

                    long timestamp = System.currentTimeMillis();
                    String str = "apkname="+packagename + "&macid="+myApp.getMainMacID() +
                            "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                    String md5 = MD5.GetMD5Code(str);

                    String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/apkver",
                            "apkname="+packagename + "&macid="+myApp.getMainMacID() +
                                    "&timestamp="+timestamp + "&md5=" + md5);
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
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //msg = e.getMessage();
                        }
                    } else {
                        //iView.getVersionError("联网失败");
                        Message msg2 = dispHandler.obtainMessage(dispHandler_rtnGetServerError);
                        msg2.obj = "联网失败";
                        dispHandler.sendMessage(msg2);
                        return;
                    }
                    if(code != 0){
                        //iView.getVersionError(msg);
                        Message msg2 = dispHandler.obtainMessage(dispHandler_rtnGetServerError);
                        msg2.obj = msg;
                        dispHandler.sendMessage(msg2);
                        return;
                    }

                    //iView.rtnGetVersionInfo(advertplanid,trackplanid,apkver);

                    Message msg2 = dispHandler.obtainMessage(dispHandler_rtnGetServerOK);
                    msg2.obj = "" + advertplanid + "," + trackplanid + "," + apkver;
                    dispHandler.sendMessage(msg2);

                } else {
                    //iView.rtnGetVersionInfo(0l,0l,"1.0.0");

                    Message msg2 = dispHandler.obtainMessage(dispHandler_rtnGetServerOK);
                    msg2.obj = "" + 0 + "," + 0 + "," + "1.0.0";
                    dispHandler.sendMessage(msg2);
                }
            }
        },"查看服务器端信息").start();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                Intent toLoading = new Intent(MenuActivity.this, LoadingActivity.class);
                Bundle dataLoading = new Bundle();
                dataLoading.putString("param", "menurestart");
                toLoading.putExtras(dataLoading);
                startActivity(toLoading);
                MenuActivity.this.finish();
                break;

            case R.id.btn_stocksupply:

                TextView tv = findViewById(R.id.last2hourstockcheck);
                if(tv.getText().toString().replace("最近盘点时间：无","").length() == 0){
                    AlertDialog al = new AlertDialog.Builder(MenuActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("你还没有库存盘点，进去后只能查看，继续吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent toStockSupply = new Intent(MenuActivity.this, StockSupplyActivity.class);
                                    Bundle data = new Bundle();
                                    data.putString("param", "readonly");
                                    toStockSupply.putExtras(data);
                                    startActivity(toStockSupply);
                                    MenuActivity.this.finish();
                                }
                            })
                            .setNegativeButton("取消",null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                } else {
                    Intent toStockSupply = new Intent(MenuActivity.this, StockSupplyActivity.class);
                    Bundle data = new Bundle();
                    data.putString("param", "modify");
                    toStockSupply.putExtras(data);
                    startActivity(toStockSupply);
                    MenuActivity.this.finish();
                }
                break;

            case R.id.btn_stockcheck:
                Intent toStockCheck = new Intent(MenuActivity.this,StockCheckActivity.class);
                startActivity(toStockCheck);
                MenuActivity.this.finish();
                break;


            case R.id.btn_goodsprice:
                Intent togoodsprice = new Intent(MenuActivity.this,GoodspriceActivity.class);
                startActivity(togoodsprice);
                MenuActivity.this.finish();
                break;

            case R.id.btn_checkversion:
                Intent tocheckversion = new Intent(MenuActivity.this,CheckVersionActivity.class);
                startActivity(tocheckversion);
                MenuActivity.this.finish();
                break;

            case R.id.btn_testmain: {
                Intent totest = new Intent(MenuActivity.this, TestActivity.class);
                Bundle datamain = new Bundle();
                datamain.putString("param", "main");
                totest.putExtras(datamain);
                startActivity(totest);
                MenuActivity.this.finish();
            }
                break;

            case R.id.btn_macpara:
                Intent toMacPara = new Intent(MenuActivity.this, MacParaActivity.class);
                startActivity(toMacPara);
                MenuActivity.this.finish();
                break;

            case R.id.btn_mainmax:
                Intent toMainMax = new Intent(MenuActivity.this, MaxActivity.class);
                Bundle datamain = new Bundle();
                datamain.putString("param", "main");
                toMainMax.putExtras(datamain);
                startActivity(toMainMax);
                MenuActivity.this.finish();
                break;



            case R.id.btn_exit:
                Intent intent=new Intent("com.mengdeman.vmselfstart.monitor.STOP");
                sendBroadcast(intent);

                MenuActivity.this.finish();

                // 日志关闭
                LogUtils.closePrintWritter();

                System.exit(0);

                break;

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
                case dispHandler_rtnGetServerOK:

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    String[] idthree = msg.obj.toString().split(",");

                    if(Integer.parseInt(idthree[2].replace(".","")) > Integer.parseInt(Gmethod.getAppVersion(MenuActivity.this).replace(".",""))){
                        findViewById(R.id.checkversion_newnotice).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.checkversion_newnotice).setVisibility(View.INVISIBLE);
                    }

                    break;

                case dispHandler_rtnGetServerError:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    AlertDialog al = new AlertDialog.Builder(MenuActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("错误信息：" + msg.obj)
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();

                    break;

                default:
                    break;
            }
            return  false;
        }
    });


    /**
     * mainorsub 1表示主机，2表示幅柜
     * @return 有错误的轨道编号
     */
    protected String getError(int mainorsub){
        String errortrack = "";

        if(mainorsub == 1) {
            if (((MyApp) getApplication()).getMainMacType().equals("general")) {
                int[] levelnum = new int[]{((MyApp) getApplication()).getMainGeneralLevel1TrackCount(),
                        ((MyApp) getApplication()).getMainGeneralLevel2TrackCount(),
                        ((MyApp) getApplication()).getMainGeneralLevel3TrackCount(),
                        ((MyApp) getApplication()).getMainGeneralLevel4TrackCount(),
                        ((MyApp) getApplication()).getMainGeneralLevel5TrackCount(),
                        ((MyApp) getApplication()).getMainGeneralLevel6TrackCount(),
                        ((MyApp) getApplication()).getMainGeneralLevel7TrackCount()};
                for (int i = 0; i < ((MyApp) getApplication()).getMainGeneralLevelNum(); i++) {
                    for (int j = 0; j < levelnum[i]; j++) {
                        if (((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + j].getErrorcode().length() > 0) {
                            errortrack = errortrack + ","  + (i * 10 + j + 10);
                        }
                    }
                }
            }
        }


        if(errortrack.length() == 0){
            return "";
        } else {
            return errortrack.substring(1);   // 把最后一个，去掉
        }
    }

}
