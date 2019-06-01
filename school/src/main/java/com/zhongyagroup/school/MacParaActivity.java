package com.zhongyagroup.school;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.zhongyagroup.school.app.MyApp;
import com.zhongyagroup.school.util.MD5;
import com.zhongyagroup.school.util.MyHttp;
import com.zhongyagroup.school.util.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MacParaActivity extends Activity implements View.OnClickListener{
    private static final int dispHandler_rtnGetCarrier = 0x101;

    private Button backBtn;
    private Button getCarrierBtn;

    private EditText accesskeyTV;
    private EditText maincodeTV;

    private EditText carrierET;

    private String[] mainsubgeneral = {"3","4","5","6","7"};
    private Spinner maingeneraltrack;
    private Spinner mainlevel1;
    private Spinner mainlevel2;
    private Spinner mainlevel3;
    private Spinner mainlevel4;
    private Spinner mainlevel5;
    private Spinner mainlevel6;
    private Spinner mainlevel7;
    private String[] mainsublevel = {"2","3","4","5","6","7","8","9","10"};

    private RelativeLayout mainlevel4rl;
    private RelativeLayout mainlevel5rl;
    private RelativeLayout mainlevel6rl;
    private RelativeLayout mainlevel7rl;

    private Spinner spinnerTrackOutType;
    private String[] oneTrackOutType = {"0:多的先出","1:前面的先出","2:后面的先出"};

    private String initFour;

    private MyApp myApp;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mac_para);

        myApp = (MyApp)getApplication();

        initFour = ((MyApp)getApplication()).getAccessKey() + ";" + ((MyApp)getApplication()).getMainMacID() ;

        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(this);
        getCarrierBtn = findViewById(R.id.btn_getcarrier);
        getCarrierBtn.setOnClickListener(this);

        accesskeyTV = findViewById(R.id.accesskeyet);
        accesskeyTV.setText(myApp.getAccessKey());
        //光标停到最后的位置
        CharSequence text = accesskeyTV.getText();
        if (text != null) {
            Spannable spanText = (Spannable)text;
            Selection.setSelection(spanText, text.length());
        }

        maincodeTV = findViewById(R.id.main_nameinput);
        maincodeTV.setText(myApp.getMainMacID());

        carrierET = findViewById(R.id.carriername);
        carrierET.setText("");

        mainlevel4rl = findViewById(R.id.maingeneraltrack4);
        mainlevel5rl = findViewById(R.id.maingeneraltrack5);
        mainlevel6rl = findViewById(R.id.maingeneraltrack6);
        mainlevel7rl = findViewById(R.id.maingeneraltrack7);

        switch (((MyApp)getApplication()).getMainGeneralLevelNum()){
            case 3:
                mainlevel4rl.setVisibility(View.INVISIBLE);
            case 4:
                mainlevel5rl.setVisibility(View.INVISIBLE);
            case 5:
                mainlevel6rl.setVisibility(View.INVISIBLE);
            case 6:
                mainlevel7rl.setVisibility(View.INVISIBLE);
            case 7:
            default:
        }

        spinnerTrackOutType = (Spinner) findViewById(R.id.SpinnerTrackOutType);
        ArrayAdapter<String> oneleveladapterouttype = new ArrayAdapter<String>(this,R.layout.myspinnertext,oneTrackOutType);
        //设置下拉列表的风格
        oneleveladapterouttype.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinnerTrackOutType.setAdapter(oneleveladapterouttype);
        //添加事件Spinner事件监听
        spinnerTrackOutType.setOnItemSelectedListener(new OneTrackOutTypeSpinnerSelectedListener());
        //设置默认值
        spinnerTrackOutType.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<oneTrackOutType.length;i++){
            if(Integer.parseInt(oneTrackOutType[i].split(":")[0]) == ((MyApp)getApplication()).getTrackouttype()){
                spinnerTrackOutType.setSelection(i);
                break;
            }
        }

        maingeneraltrack = findViewById(R.id.SpinnerMainGeneralTrack);
        ArrayAdapter<String> maingeneraladapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsubgeneral);
        //设置下拉列表的风格
        maingeneraladapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        maingeneraltrack.setAdapter(maingeneraladapter);
        //添加事件Spinner事件监听
        maingeneraltrack.setOnItemSelectedListener(new MainGeneralSpinnerSelectedListener());
        //设置默认值
        maingeneraltrack.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsubgeneral.length;i++){
            if(Integer.parseInt(mainsubgeneral[i]) == ((MyApp) getApplication()).getMainGeneralLevelNum()){
                maingeneraltrack.setSelection(i);
                break;
            }
        }


        mainlevel1 = findViewById(R.id.SpinnerMainGeneralTrackLevel1);
        ArrayAdapter<String> mainlevel1adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel1adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel1.setAdapter(mainlevel1adapter);
        //添加事件Spinner事件监听
        mainlevel1.setOnItemSelectedListener(new Mainlevel1SpinnerSelectedListener());
        //设置默认值
        mainlevel1.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel1TrackCount()){
                mainlevel1.setSelection(i);
                break;
            }
        }

        mainlevel2 = findViewById(R.id.SpinnerMainGeneralTrackLevel2);
        ArrayAdapter<String> mainlevel2adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel2adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel2.setAdapter(mainlevel2adapter);
        //添加事件Spinner事件监听
        mainlevel2.setOnItemSelectedListener(new Mainlevel2SpinnerSelectedListener());
        //设置默认值
        mainlevel2.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel2TrackCount()){
                mainlevel2.setSelection(i);
                break;
            }
        }

        mainlevel3 = findViewById(R.id.SpinnerMainGeneralTrackLevel3);
        ArrayAdapter<String> mainlevel3adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel3adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel3.setAdapter(mainlevel3adapter);
        //添加事件Spinner事件监听
        mainlevel3.setOnItemSelectedListener(new Mainlevel3SpinnerSelectedListener());
        //设置默认值
        mainlevel3.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel3TrackCount()){
                mainlevel3.setSelection(i);
                break;
            }
        }

        mainlevel4 = findViewById(R.id.SpinnerMainGeneralTrackLevel4);
        ArrayAdapter<String> mainlevel4adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel4adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel4.setAdapter(mainlevel4adapter);
        //添加事件Spinner事件监听
        mainlevel4.setOnItemSelectedListener(new Mainlevel4SpinnerSelectedListener());
        //设置默认值
        mainlevel4.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel4TrackCount()){
                mainlevel4.setSelection(i);
                break;
            }
        }

        mainlevel5 = findViewById(R.id.SpinnerMainGeneralTrackLevel5);
        ArrayAdapter<String> mainlevel5adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel5adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel5.setAdapter(mainlevel5adapter);
        //添加事件Spinner事件监听
        mainlevel5.setOnItemSelectedListener(new Mainlevel5SpinnerSelectedListener());
        //设置默认值
        mainlevel5.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel5TrackCount()){
                mainlevel5.setSelection(i);
                break;
            }
        }

        mainlevel6 = findViewById(R.id.SpinnerMainGeneralTrackLevel6);
        ArrayAdapter<String> mainlevel6adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel6adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel6.setAdapter(mainlevel6adapter);
        //添加事件Spinner事件监听
        mainlevel6.setOnItemSelectedListener(new Mainlevel6SpinnerSelectedListener());
        //设置默认值
        mainlevel6.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel6TrackCount()){
                mainlevel6.setSelection(i);
                break;
            }
        }

        mainlevel7 = findViewById(R.id.SpinnerMainGeneralTrackLevel7);
        ArrayAdapter<String> mainlevel7adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel7adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel7.setAdapter(mainlevel7adapter);
        //添加事件Spinner事件监听
        mainlevel7.setOnItemSelectedListener(new Mainlevel7SpinnerSelectedListener());
        //设置默认值
        mainlevel7.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel7TrackCount()){
                mainlevel7.setSelection(i);
                break;
            }
        }

    }

    class OneTrackOutTypeSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selectedCom = Integer.parseInt(oneTrackOutType[arg2].split(":")[0]);
            if(selectedCom != ((MyApp)getApplication()).getTrackouttype()){
                ((MyApp)getApplication()).setTrackouttype(selectedCom);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("trackouttype", ((MyApp)getApplication()).getTrackouttype());
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class MainGeneralSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsubgeneral[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevelNum()){
                ((MyApp) getApplication()).setMainGeneralLevelNum(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevelNum", selected);
                editor.apply();//提交修改

                mainlevel4rl.setVisibility(View.VISIBLE);
                mainlevel5rl.setVisibility(View.VISIBLE);
                mainlevel6rl.setVisibility(View.VISIBLE);
                mainlevel7rl.setVisibility(View.VISIBLE);
                switch (((MyApp)getApplication()).getMainGeneralLevelNum()){
                    case 3:
                        mainlevel4rl.setVisibility(View.INVISIBLE);
                    case 4:
                        mainlevel5rl.setVisibility(View.INVISIBLE);
                    case 5:
                        mainlevel6rl.setVisibility(View.INVISIBLE);
                    case 6:
                        mainlevel7rl.setVisibility(View.INVISIBLE);
                    case 7:
                    default:
                }

            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel1SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel1TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel1TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel1TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel2SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel2TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel2TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel2TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel3SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel3TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel3TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel3TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel4SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel4TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel4TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel4TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel5SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel5TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel5TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel5TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel6SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel6TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel6TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel6TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel7SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel7TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel7TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel7TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_back:
                String newaccesskey = accesskeyTV.getText().toString();

                if(newaccesskey.length() != 8){
                    AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("访问服务器密码必须为8位字母数字的混合")
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                } else {
                    boolean isletter = true;
                    boolean isdigit = false;

                    for(int i=0;i<8;i++){
                        if(Character.isDigit(newaccesskey.charAt(i))){
                            isdigit = true;
                        }
//                        只有数字也可以
//                        if(Character.isLetter(newaccesskey.charAt(i))){
//                            isletter = true;
//                        }
                    }
                    if(isdigit && isletter){
                        String newFour = newaccesskey + ";" + maincodeTV.getText().toString() ;
                        if(initFour.equals(newFour)) {

                            SharedPreferences sp =
                                    MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();//获取编辑器

                            editor.putString("accessKey", newaccesskey);
                            editor.putString("mainMacID", maincodeTV.getText().toString());
                            editor.apply();//提交修改

                            ((MyApp) getApplication()).setAccessKey(newaccesskey);
                            ((MyApp) getApplication()).setMainMacID(maincodeTV.getText().toString());



                            Intent toMenu = new Intent(MacParaActivity.this, MenuActivity.class);
                            startActivity(toMenu);
                            MacParaActivity.this.finish();
                        } else {
                            AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("提示")
                                    .setMessage("请先点击【获取所属运营商】验证输入信息通过后，再【返回】")
                                    .setPositiveButton("确定", null)
                                    .create();
                            al.setCancelable(false);
                            al.show();
                        }
                    } else {
                        AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("提示")
                                .setMessage("访问服务器密码必须为8位字母数字的混合")
                                .setPositiveButton("确定", null)
                                .create();
                        al.setCancelable(false);
                        al.show();
                    }
                }

                break;

            case R.id.btn_getcarrier:
                carrierET.setText("");
                final String testaccesskey = accesskeyTV.getText().toString();

                if(testaccesskey.length() != 8){
                    AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("访问服务器密码必须为8位字母数字的混合")
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                } else {
                    boolean isletter = true;
                    boolean isdigit = false;

                    for (int i = 0; i < 8; i++) {
                        if (Character.isDigit(testaccesskey.charAt(i))) {
                            isdigit = true;
                        }
//                        if (Character.isLetter(testaccesskey.charAt(i))) {
//                            isletter = true;
//                        }
                    }
                    if (isdigit && isletter) {
                        mProgressDialog = ProgressDialog.createLoadingDialog(MacParaActivity.this,"联网获取信息,请稍候...");
                        mProgressDialog.show();

                        final String mainmacid = maincodeTV.getText().toString();
                        final String maintype = "general";

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                long timestamp = System.currentTimeMillis();
                                String str = "macid="+mainmacid + "&maintype="+maintype + "&submacid="+ "&timestamp="+timestamp + "&accesskey=" + testaccesskey;
                                String md5 = MD5.GetMD5Code(str);

                                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/getcarrier",
                                        "macid="+mainmacid + "&submacid=" + "&maintype="+maintype +
                                                "&timestamp="+timestamp + "&md5=" + md5);

                                if(rtnstr.length()==0){

                                    HashMap<String,Object> hm = new HashMap<>();
                                    hm.put("code",1);
                                    hm.put("msg","联网失败");
                                    hm.put("carriername","");
                                    Message message = dispHandler.obtainMessage(dispHandler_rtnGetCarrier);
                                    message.obj = hm;
                                    dispHandler.sendMessage(message);

                                } else {
                                    // {"msg":"","code":0,"carriersn":"工厂车间"}
                                    int code = 2;
                                    String msg = "解析返回值出错啦";
                                    String carriersn = "";
                                    try {
                                        JSONObject soapJson = new JSONObject(rtnstr);
                                        code = soapJson.getInt("code");
                                        msg = soapJson.getString("msg");
                                        if (code == 0)
                                            carriersn = soapJson.getString("carriersn");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                    HashMap<String, Object> hm = new HashMap<>();
                                    hm.put("code", code);
                                    hm.put("msg", msg);
                                    hm.put("carriername", carriersn);
                                    Message message = dispHandler.obtainMessage(dispHandler_rtnGetCarrier);
                                    message.obj = hm;
                                    dispHandler.sendMessage(message);
                                }
                            }

                        },"取得运营商名称").start();

                    } else {
                        AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("提示")
                                .setMessage("访问服务器密码必须为8位字母数字的混合")
                                .setPositiveButton("确定", null)
                                .create();
                        al.setCancelable(false);
                        al.show();
                    }
                }
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
                    case dispHandler_rtnGetCarrier:

                        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }

                        HashMap<String,Object> hm = (HashMap<String,Object>)msg.obj;
                        int rtn_code = (Integer)hm.get("code");
                        String rtn_msg = (String)hm.get("msg");
                        String rtn_carriername = (String)hm.get("carriername");

                        if(rtn_code == 0){
                            carrierET.setText(rtn_carriername);
                            initFour = accesskeyTV.getText().toString() + ";" + maincodeTV.getText().toString() ;
                        } else {
                            AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("提示")
                                    .setMessage("错误信息："+rtn_msg)
                                    .setPositiveButton("确定", null)
                                    .create();
                            al.setCancelable(false);
                            al.show();
                        }


                        break;


                    default:
                        break;
                }
                return false;
            }
        });
}
