package com.zhongyagroup.school;

import android.app.Activity;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhongyagroup.school.app.MyApp;
import com.zhongyagroup.school.util.Gmethod;
import com.zhongyagroup.school.util.MD5;
import com.zhongyagroup.school.util.MyHttp;
import com.zhongyagroup.school.util.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class CheckVersionActivity extends Activity implements View.OnClickListener{
    private Dialog mProgressDialog;
    //private TextView mProcessTV;

    private static final int dispHandler_rtnGetServerError = 0x101;
    private static final int dispHandler_rtnGetServerOK = 0x102;
    private static final int dispHandler_apk_down_percent = 0x103;
    private static final int dispHandler_apk_down_ok = 0x104;

    private Button backBtn;
    private Button downBtn;
    private RelativeLayout downrl;

    private TextView localVersion;
    private TextView serverVersion;

    private String localapkversion = ""; // 1.0.0
    private String serverapkversion = "";  // 1.0.1
    private String newapkdlurl = ""; // http://.....

    private String UPDATE_SERVERAPK = "school.apk";

    MyApp myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_version);

        myApp = (MyApp)getApplication();

        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(this);

        downBtn = findViewById(R.id.submitdlupdate_bt);
        downBtn.setOnClickListener(this);

        downrl = findViewById(R.id.input_title03);
        downrl.setVisibility(View.INVISIBLE);

        localVersion  = (TextView)findViewById(R.id.nowversion);
        serverVersion = (TextView)findViewById(R.id.dbversion);

        localapkversion = Gmethod.getAppVersion(CheckVersionActivity.this);
        localVersion.setText("本机程序版本：" + localapkversion);

        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.createLoadingDialog(CheckVersionActivity.this, "正在连接服务器查看最新版本信息,请稍候...");
        }
        //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
        mProgressDialog.show();

        final String packagename = Gmethod.getPackageName(CheckVersionActivity.this);

         new Thread(new Runnable() {
             @Override
             public void run() {
                 long timestamp = System.currentTimeMillis();
                 String str = "apkname="+packagename + "&macid="+myApp.getMainMacID() +
                         "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                 String md5 = MD5.GetMD5Code(str);

                 String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/apkver",
                         "apkname="+packagename + "&macid="+myApp.getMainMacID() +
                                 "&timestamp="+timestamp + "&md5=" + md5);

                 if(rtnstr.length()==0){
                     //iView.downloadError("联网失败");

                     Message msg = dispHandler.obtainMessage(dispHandler_rtnGetServerError);
                     msg.obj = "联网失败";
                     dispHandler.sendMessage(msg);
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
                         //iView.downloadOK(dlurl,apkver);

                         serverapkversion = apkver;
                         newapkdlurl = dlurl;

                         dispHandler.sendEmptyMessage(dispHandler_rtnGetServerOK);
                     } else {
                         //iView.downloadError(msg);
                         Message msg2 = dispHandler.obtainMessage(dispHandler_rtnGetServerError);
                         msg2.obj = msg;
                         dispHandler.sendMessage(msg2);
                     }
                 }
             }
         },"获取服务器端应用版本").start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_back:

                Intent toMenu = new Intent(CheckVersionActivity.this, MenuActivity.class);
                startActivity(toMenu);
                CheckVersionActivity.this.finish();

                break;

            case R.id.submitdlupdate_bt:
                Intent intent=new Intent("com.mengdeman.vmselfstart.monitor.STOP");
                sendBroadcast(intent);

                if (mProgressDialog == null) {
                    mProgressDialog = ProgressDialog.createLoadingDialog(CheckVersionActivity.this, "正在连接服务器查看最新版本信息,请稍候...");
                }
                ((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在下载...");
                mProgressDialog.show();

                downFile(newapkdlurl);
                break;

            default:
                break;
        }
    }

    /**
     * 下载apk
     */
    public void downFile(final String urlstr) {
        new Thread() {
            public void run() {

                try {

                    //创建按一个URL实例
                    URL url = new URL(urlstr);
                    //创建一个HttpURLConnection的链接对象
                    HttpURLConnection httpConn =(HttpURLConnection)url.openConnection();
                    //获取所下载文件的InputStream对象
                    InputStream inputStream=httpConn.getInputStream();

                    long length = httpConn.getContentLength();
                    Log.i("school","共大小：" + length);


                    FileOutputStream fileOutputStream = null;
                    if (inputStream != null) {
                        File file = new File(Environment.getExternalStorageDirectory(), UPDATE_SERVERAPK);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] b = new byte[1024];
                        int charb = -1;
                        int count = 0;
                        while ((charb = inputStream.read(b)) != -1) {
                            fileOutputStream.write(b, 0, charb);
                            count += charb;

                            Message msg = new Message();
                            msg.what = dispHandler_apk_down_percent;
                            msg.obj = "" + (count*100)/length;
                            dispHandler.sendMessage(msg);
                        }
                    }
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }

                    dispHandler.sendEmptyMessage(dispHandler_apk_down_ok);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * Handle线程，显示处理结果
     */
    Handler dispHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // 处理消息
            switch (msg.what) {
                case dispHandler_apk_down_ok:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), UPDATE_SERVERAPK)), "application/vnd.android.package-archive");
                    startActivity(intent);

                    break;

                case dispHandler_apk_down_percent:
                    ((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在下载(" + msg.obj + "%)...");
                    break;

                case dispHandler_rtnGetServerOK:

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    serverVersion.setText("服务器端的最新版本：" + serverapkversion);

                    if(Integer.parseInt(serverapkversion.replace(".","")) > Integer.parseInt(localapkversion.replace(".",""))){
                        downrl.setVisibility(View.VISIBLE);
                    }

                    break;

                case dispHandler_rtnGetServerError:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    AlertDialog al = new AlertDialog.Builder(CheckVersionActivity.this)
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
}
