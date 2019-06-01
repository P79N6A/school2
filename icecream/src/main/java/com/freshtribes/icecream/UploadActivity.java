package com.freshtribes.icecream;

import android.app.Activity;
import android.os.Bundle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.util.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UploadActivity extends Activity  implements View.OnClickListener{
    private static String TAG = "UploadActivity";

    private Button backBtn;

    private String[] filename = new String[]{"","","","","","",""};
    private TextView[] nametv = new TextView[7];
    private Button[] uploadbtn = new Button[7];

    private Dialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(this);

        nametv[0] = findViewById(R.id.filename1);
        nametv[1] = findViewById(R.id.filename2);
        nametv[2] = findViewById(R.id.filename3);
        nametv[3] = findViewById(R.id.filename4);
        nametv[4] = findViewById(R.id.filename5);
        nametv[5] = findViewById(R.id.filename6);
        nametv[6] = findViewById(R.id.filename7);

        uploadbtn[0] = findViewById(R.id.btn_upload1);
        uploadbtn[1] = findViewById(R.id.btn_upload2);
        uploadbtn[2] = findViewById(R.id.btn_upload3);
        uploadbtn[3] = findViewById(R.id.btn_upload4);
        uploadbtn[4] = findViewById(R.id.btn_upload5);
        uploadbtn[5] = findViewById(R.id.btn_upload6);
        uploadbtn[6] = findViewById(R.id.btn_upload7);

        File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/mylogs/");
        File[] files = folder.listFiles();
        Log.i(TAG,"文件的个数：" + files.length);

        List<String> items = new ArrayList<String>();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            items.add(file.getName());
        }
        Collections.sort(items, String.CASE_INSENSITIVE_ORDER);

        for(int i=0;i<(items.size()>7?7:items.size());i++){
            filename[i] = items.get(items.size()-1-i);
        }

        for(int i=0;i<7;i++){
            if(filename[i].length()==0) {
                nametv[i].setText("");
            } else {
                File sizeFile = new File(Environment.getExternalStorageDirectory().getPath() + "/mylogs/" + filename[i]);
                long size = sizeFile.length();
                if(size > 30*1000*1000) {
                    nametv[i].setText(filename[i] + "          " + size / 1000 + "kb         大于30M，无法上传！");
                    uploadbtn[i].setVisibility(View.INVISIBLE);
                } else {
                    nametv[i].setText(filename[i] + "          " + size / 1000 + "kb");
                    uploadbtn[i].setVisibility(View.VISIBLE);
                    uploadbtn[i].setOnClickListener(this);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_back:

                Intent toMenu = new Intent(UploadActivity.this, MenuActivity.class);
                startActivity(toMenu);
                UploadActivity.this.finish();

                break;

            case R.id.btn_upload1:
                StartUpload(((MyApp)getApplication()).getMainMacID(),filename[0]);
                break;
            case R.id.btn_upload2:
                StartUpload(((MyApp)getApplication()).getMainMacID(),filename[1]);
                break;
            case R.id.btn_upload3:
                StartUpload(((MyApp)getApplication()).getMainMacID(),filename[2]);
                break;
            case R.id.btn_upload4:
                StartUpload(((MyApp)getApplication()).getMainMacID(),filename[3]);
                break;
            case R.id.btn_upload5:
                StartUpload(((MyApp)getApplication()).getMainMacID(),filename[4]);
                break;
            case R.id.btn_upload6:
                StartUpload(((MyApp)getApplication()).getMainMacID(),filename[5]);
                break;
            case R.id.btn_upload7:
                StartUpload(((MyApp)getApplication()).getMainMacID(),filename[6]);
                break;


            default:
                break;
        }
    }

    private void StartUpload(final String macid,final String filename){
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.createLoadingDialog(UploadActivity.this, "正在上传(0),请稍候...");
        }
        //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
        mProgressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                /* 上传文件至Server的方法 */
                String end = "\r\n";
                String twoHyphens = "--";
                String boundary = "----WebKitFormBoundaryRX0YPSgKU1TFNqC";
                try {
                    URL url = new URL("http://mac.freshtribes.com/crash/uploadfile");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    /* 允许Input、Output，不使用Cache */
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setUseCaches(false);
                    con.setConnectTimeout(12000);// 连接超时 12秒
                    con.setReadTimeout(600000);// 读取超时 单位毫秒       600秒
                    /* 设置传送的method=POST */
                    con.setRequestMethod("POST");
                    /* setRequestProperty */
                    con.setRequestProperty("Connection", "Keep-Alive");
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    /* 设置DataOutputStream */
                    DataOutputStream ds = new DataOutputStream(con.getOutputStream());
                    ds.writeBytes(twoHyphens + boundary + end);
                    ds.writeBytes("Content-Disposition: form-data; " + "name=\"macid\"" + end + end);
                    ds.writeBytes(macid+ end);

                    ds.writeBytes(twoHyphens + boundary + end);
                    ds.writeBytes("Content-Disposition: form-data; name=\"upload\"; filename=\"" + filename + "\"" + end);
                    ds.writeBytes("Content-Type: text/plain" + end + end);
                    /* 取得文件的FileInputStream */
                    FileInputStream fStream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/mylogs/" + filename);
                    /* 设置每次写入1024bytes */
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = -1;
                    long bytes = 0;
                    /* 从文件读取数据至缓冲区 */
                    while ((length = fStream.read(buffer)) != -1) {
                        /* 将资料写入DataOutputStream中 */
                        ds.write(buffer, 0, length);
                        try{
                            Thread.sleep(10);
                        }catch (InterruptedException ex){
                            ex.printStackTrace();
                        }

                        bytes = bytes + length;

                        Message msg = msgHandler.obtainMessage(100);
                        msg.obj = bytes/1000 + "kb";
                        msgHandler.sendMessage(msg);
                    }
                    fStream.close();

                    ds.writeBytes(end);
                    ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
                    /* close streams */
                    ds.flush();


                    String backstr = "";
                    /**
                     * 获取响应码  200=成功
                     * 当响应成功，获取响应的流
                     */
                    int res = con.getResponseCode();
                    if(res == 200) {

                        /* 取得Response内容 */
                        InputStream reader = con.getInputStream();
                        int ch;
                        StringBuffer b = new StringBuffer();
                        while ((ch = reader.read()) != -1) {
                            b.append((char) ch);
                        }
                        reader.close();

                        // 收到的内容
                        backstr = b.toString().trim();

                        if(backstr.contains("upload.html")){
                            backstr = "";
                        }
//
//                        if (backstr.length() > 0) {
//                            try {
//                                JSONObject soapJson = new JSONObject(backstr);
//                                int code = soapJson.getInt("code");
//                                String msg = soapJson.getString("msg");
//                                if (code == 0) {
//                                    backstr = "";
//                                } else {
//                                    backstr = msg;
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                //msg = e.getMessage();
//                            }
//                        } else {
//                            backstr = "联网失败！";
//                        }

                    } else {
                        backstr = "返回错误响应码："+res;
                    }
                    ds.close();


                    Message msg2 = msgHandler.obtainMessage(101);
                    msg2.obj = backstr;
                    msgHandler.sendMessage(msg2);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Handle线程，接收线程过来的消息
     */
    Handler msgHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // 处理消息
            switch (msg.what) {

                case 100:
                    ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在上传(" + msg.obj.toString()+ "),请稍候...");
                    break;

                case 101:
                    String backstr = msg.obj.toString();
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    AlertDialog al = new AlertDialog.Builder(UploadActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage((backstr.length()==0?"上传成功！":"上传失败："+backstr))
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                default:
                    break;

            }
            return false;
        }
    });
}
