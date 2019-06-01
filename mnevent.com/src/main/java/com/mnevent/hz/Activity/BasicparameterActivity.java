package com.mnevent.hz.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.igexin.sdk.PushManager;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import com.mnevent.hz.Adapter.HierarchyAdapter;
import com.mnevent.hz.R;
import com.mnevent.hz.Utils.Log;
import com.mnevent.hz.Utils.MD5;
import com.mnevent.hz.Utils.MyHttp;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.bean.PressBean;
import com.mnevent.hz.bean.UploadInformationBean;

import com.mnevent.hz.Utils.BasicparameterPopuwindow;
import com.mnevent.hz.Utils.CommonUtils2;
import com.mnevent.hz.Utils.DateUtils;
import com.mnevent.hz.Utils.Gmethod;
import com.mnevent.hz.Utils.HttpUtils;
import com.mnevent.hz.Utils.LogUtils;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.Utils.Tishitextpopuwindow;
import com.mnevent.hz.Utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android_serialport_api.ComTrackMagex;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

/**
 * Created by Administrator on 2018/12/24.
 * 机器的基本信息
 */

public class BasicparameterActivity extends Activity {

    @BindView(R.id.titlename)
    TextView titlename;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.topbar_title)
    RelativeLayout topbarTitle;

    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.master)
    TextView master;
    @BindView(R.id.serial_number)
    EditText serialNumber;
    @BindView(R.id.phone)
    EditText phone;
    @BindView(R.id.orbital_recy)
    RecyclerView orbitalRecy;
    @BindView(R.id.orbitalnumber)
    TextView orbitalnumber;
    @BindView(R.id.pathway)
    RelativeLayout pathway;
    private List<String> inv = new ArrayList<>();
    PathwayMolder molder;


    String motorAddress14;
    String motorAddress58;
    String motorSteps14;
    String motorSteps58;
    List<PathwayMolder> patall;


    UploadInformationBean uploadInformationBean;

    private List<PressBean> press = new ArrayList<>();
    private List<PressBean> presstwo = new ArrayList<>();


    private String[] number = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private String[] texts = new String[]{"0、无Magex主控，无升降驱动盘", "1、有Magex主控制器"};
    private String[] textes = new String[]{"普通综合机"};
    private int[] xians = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    private List<PressBean> hierarchy = new ArrayList<>();
    private int hierar = 0;

    List<PathwayMolder> all;

    HierarchyAdapter adapter2;

    PromptDialog dialog;
    int Tracknumber = 0;
    // 个推
    private String clientID = "";

    // 温度,化霜温度，制冷开始温度，制冷停止温度，门（closed，openning）,其它故障（有的话）,串口发送相关错误（有的话）
    private String[] statusdetail5 = new String[]{"-","-","-","-","-","其它故障（有的话）","串口发送相关错误（有的话）"};

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    dialog.dismiss();

                        finish();
                        //选择驱动
                        PrefheUtils.putString(BasicparameterActivity.this, "drive", master.getText().toString());
                        //编号
                        PrefheUtils.putString(BasicparameterActivity.this, "serialNumber", serialNumber.getText().toString());
                        //密码
                        PrefheUtils.putString(BasicparameterActivity.this, "password", password.getText().toString());
                        //手机号
                        PrefheUtils.putString(BasicparameterActivity.this, "phone", phone.getText().toString());

                      //  ToastUtil.showToast(BasicparameterActivity.this,uploadInformationBean.getMessage());

                    break;
                case 2:
                    dialog.showLoading("正在获取数据...");
                    break;
                case 3:
                    Tracknumber = 9;
                    Loading_track(9);
                    break;
                case 4:
                    LogUtils.d("BasicparameterActivity:::zlc"+"cccccc");
                    orbitaldata();
                    master.setText("0、无Magex主控，无升降驱动盘");
                    ToastUtil.showToast(BasicparameterActivity.this,"当前机器没有Magex主控制");
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtils.d("BasicparameterActivity:"+ getClass().getSimpleName());
        dialog = new PromptDialog(BasicparameterActivity.this);
        setContentView(R.layout.activity_basic_parameter);
        ButterKnife.bind(this);

        initDate();
        initView();
    }

    private void initDate() {
        hierarchy.clear();
        PressBean bean = new PressBean();
        bean.setPres("人脸识别");
        bean.setPd(0);
        press.add(bean);

        PressBean bean1 = new PressBean();
        bean1.setPres("启用麦克风");
        bean1.setPd(0);
        press.add(bean1);

        PressBean bean2 = new PressBean();
        bean2.setPres("启用会员");
        bean2.setPd(0);
        press.add(bean2);

        PressBean bean3 = new PressBean();
        bean3.setPres("启用标准支付宝");
        bean3.setPd(0);
        presstwo.add(bean3);

        PressBean bean4 = new PressBean();
        bean4.setPres("启用标准微信");
        bean4.setPd(0);
        presstwo.add(bean4);

        PressBean bean5 = new PressBean();
        bean5.setPres("启用杭州移领支付宝");
        bean5.setPd(0);
        presstwo.add(bean5);

        PressBean bean6 = new PressBean();
        bean6.setPres("启用杭州移领微信");
        bean6.setPd(0);
        presstwo.add(bean6);

        PressBean bean7 = new PressBean();
        bean7.setPres("启用中信银行支付宝");
        bean7.setPd(0);
        presstwo.add(bean7);

        PressBean bean8 = new PressBean();
        bean8.setPres("启用中信银行微信");
        bean8.setPd(0);
        presstwo.add(bean8);

        PressBean bean9 = new PressBean();
        bean9.setPres("第一层的轨道数");
        bean9.setPd(0);
        bean9.setNumber(0);
        hierarchy.add(bean9);

        PressBean bean10 = new PressBean();
        bean10.setPres("第二层的轨道数");
        bean10.setPd(0);
        bean10.setNumber(0);
        hierarchy.add(bean10);

        PressBean bean11 = new PressBean();
        bean11.setPres("第三层的轨道数");
        bean11.setPd(0);
        bean11.setNumber(0);
        hierarchy.add(bean11);

        PressBean bean12 = new PressBean();
        bean12.setPres("第四层的轨道数");
        bean12.setPd(0);
        bean12.setNumber(0);
        hierarchy.add(bean12);

        PressBean bean13 = new PressBean();
        //bean13.setPres("第五层轨道数");
        bean13.setPres("第五层的轨道数");
        bean13.setPd(0);
        bean13.setNumber(0);
        hierarchy.add(bean13);

        PressBean bean14 = new PressBean();
        bean14.setPres("第六层的轨道数");
        bean14.setPd(0);
        bean14.setNumber(0);
        hierarchy.add(bean14);

        PressBean bean15 = new PressBean();
        bean15.setPres("第七层的轨道数");
        bean15.setPd(0);
        bean15.setNumber(0);
        hierarchy.add(bean15);

       /* PressBean bean16 = new PressBean();
        bean16.setPres("第八层的轨道数");
        bean16.setPd(0);
        bean16.setNumber(0);
        hierarchy.add(bean16);*/


    }

    private void initView() {


        String drive = PrefheUtils.getString(BasicparameterActivity.this, "drive", "");

        String serialNumbers = PrefheUtils.getString(BasicparameterActivity.this, "serialNumber", "");

        String passwords = PrefheUtils.getString(BasicparameterActivity.this, "password", "");

        String phones = PrefheUtils.getString(BasicparameterActivity.this, "phone", "");

        LinearLayoutManager manager2 = new LinearLayoutManager(BasicparameterActivity.this);
        orbitalRecy.setLayoutManager(manager2);

        adapter2 = new HierarchyAdapter(BasicparameterActivity.this, hierarchy, hierar, all, 10);
        if(!TextUtils.isEmpty(drive)){
            password.setText(passwords);
            master.setText(drive);
            serialNumber.setText(serialNumbers);
            phone.setText(phones);
            if(drive.equals("0、无Magex主控，无升降驱动盘")){
                Tracknumber = 10;
              //  adapter2 = new HierarchyAdapter(BasicparameterActivity.this, hierarchy, hierar, all, 10);
               Loading_track(10);
            }else{
                Tracknumber = 9;
              //  adapter2 = new HierarchyAdapter(BasicparameterActivity.this, hierarchy, hierar, all, 9);
                Loading_track(9);
            }
        }


    }

    /**
     * 软键盘隐藏
     */
    public void hintKeyBoard() {
        //拿到InputMethodManager
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        if (imm.isActive() && getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @OnClick({R.id.btn_back, R.id.master, R.id.orbitalnumber})
    public void onViewClicked(View view) {
        final BasicparameterPopuwindow popuwindow;
        switch (view.getId()) {
            case R.id.btn_back:

                //返回上级界面并保存配置数据
                hintKeyBoard();

                final Tishitextpopuwindow tishitextpopuwindow = new Tishitextpopuwindow(BasicparameterActivity.this);
                tishitextpopuwindow.setTitle("提示");
               // tishitextpopuwindow.setTitle("prompt");
                tishitextpopuwindow.setWidth(600);
                tishitextpopuwindow.setHeight(400);
                tishitextpopuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_basic_parameter, null), Gravity.CENTER, 0, 0);

                tishitextpopuwindow.SetOnclieke(new Tishitextpopuwindow.Onclike() {
                    @Override
                    public void OkOnclike() {

                        if (!TextUtils.isEmpty(master.getText().toString()) && !TextUtils.isEmpty(serialNumber.getText().toString()) && !TextUtils.isEmpty(password.getText().toString()) && !TextUtils.isEmpty(phone.getText().toString())) {
                            LogUtils.d("BasicparameterActivity::zlc:::master.getText().toString()" + master.getText().toString());


                            tishitextpopuwindow.dismiss();
                            // 纸币器和硬币器的状态，先不加上纸币器硬币器
                            String bills = "nolink";
                            String coins = "nolink";
                            String mainError_tmp = "";
                            String signale = dispNetStatus();
                            String signale_encode = "";
                            try {
                                signale_encode = URLEncoder.encode(signale,"UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            ArrayList<HashMap<String,Object>> al = new ArrayList<>();
                            String mdsrc = "adplanid="+0
                                    + "&bills="+bills
                                    + "&coins="+coins
                                    + "&driverver="+"1.0.0"
                                    + "&macerror="+mainError_tmp
                                    + "&macid="+serialNumber.getText().toString()
                                    + "&macidsub="+""
                                    + "&macstop="+"0"
                                    + "&mactime="+((new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()))
                                    + "&mainver="+"1.0.0"
                                    + "&pushid="+getClientID()
                                    + "&signalabc="+signale
                                    + "&temp1="+statusdetail5[0]
                                    + "&temp2="+"-";

                            String numbers = "";
                            for (int i = 0;i<7;i++){
                                numbers+=10+",";
                            }
                            mdsrc = mdsrc+"&track="+numbers.substring(0,numbers.length()-1);
                            patall = LitePal.findAll(PathwayMolder.class);
                            for (int i = 0;i<patall.size();i++){
                                mdsrc = mdsrc+"&track"+patall.get(i).getCode()+"="+patall.get(i).getGoodscode()
                                        +","+patall.get(i).getNummax()+","+patall.get(i).getInventory()+","+(patall.get(i).getErrorcode().equals("1")?"":patall.get(i).getErrorcode()+"_"+patall.get(i).getErrortime())+","+
                                        1+","+
                                        ""+","+
                                        ""+","+patall.get(i).getPrice()+"_"+patall.get(i).getPrice()+"_"+patall.get(i).getPrice()+"_"+patall.get(i).getPrice();

                                    Gmethod.setNowStock(al,patall.get(i).getGoodscode(),Integer.parseInt(patall.get(i).getInventory()),Integer.parseInt(patall.get(i).getPrice()));
                            }
                            mdsrc = mdsrc
                                    + "&trackplanid=0"
                                    + "&upsoftver="+Gmethod.getAppVersion(BasicparameterActivity.this);
                            Log.d("zlc","mdsrc========"+mdsrc);
                            long time = System.currentTimeMillis();
                            String str = mdsrc.replace("&signalabc="+signale,"&signalabc="+signale_encode) +
                                    "&timestamp="+time + "&accesskey=" + password.getText().toString();
                            String md5 = MD5.GetMD5Code(str);
                            LogUtils.d("BasicparameterActivity:::zlc::numbers"+numbers);

                            String ymds = DateUtils.getDateTimeByMillisecond(time + "", "yyyyMMddHHmmss");

                            String dr = "-";
                            if(statusdetail5[4].contains("开着")) dr = "open";
                            else if(statusdetail5[4].contains("关着")) dr = "close";

                            dialog.showLoading("加载中...");

                            String rtnstr = (new MyHttp()).post(HttpUtils.getMcrealstatus,
                                    mdsrc + "&timestamp="+time + "&md5=" + md5+"&nowstock="+Gmethod.getNowStockStr(al)+"&door="+dr+"&pkg=hz");

                            LogUtils.i("心跳，发送基本参数的返回值："+rtnstr);

                            int code = 2;
                            String msg = "解析返回值出错";
                            if(rtnstr.length() > 0){
                                try {
                                    JSONObject soapJson = new JSONObject(rtnstr);
                                    code = soapJson.getInt("code");
                                    msg = soapJson.getString("msg");
                                    if(code == 0){
                                        // 发送成功后，设置为true
                                        Log.d("zlc","ok..............................");
                                        handler.sendEmptyMessage(1);
                                    }else{
                                        ToastUtil.showToast(BasicparameterActivity.this,"错误"+msg);
                                        LogUtils.d("BasicparameterActivity::请求网络错误：："+msg);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    //msg = e.getMessage();
                                }
                            }

                            /* OkGo.<String>post(HttpUtils.getMcrealstatus).tag(BasicparameterActivity.this)
                                    .params("adplanid",mdsrc)
                                    .params("timestamp",time)
                                    .params("md5",md5)
                                    .params("nowstock", Gmethod.getNowStockStr(al))
                                    .params("door",dr)
                                    .params("pkg","school")
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onSuccess(Response<String> response) {
                                            uploadBean(response.body().toString());
                                        }
                                        @Override
                                        public void onFinish() {
                                            super.onFinish();
                                            dialog.dismiss();


                                        }

                                        @Override
                                        public void onError(Response<String> response) {
                                            super.onError(response);
                                            ToastUtil.showToast(BasicparameterActivity.this,"Network failure!");
                                        }
                                    });*/

                        } else if (TextUtils.isEmpty(master.getText().toString())) {
                            ToastUtil.showToast(BasicparameterActivity.this, "请选择驱动！");
                            //ToastUtil.showToast(BasicparameterActivity.this, "Please select driver！");
                            tishitextpopuwindow.dismiss();
                        } else if (TextUtils.isEmpty(serialNumber.getText().toString())) {
                            ToastUtil.showToast(BasicparameterActivity.this, "请输入编号！");
                            //ToastUtil.showToast(BasicparameterActivity.this, "Please enter the number！");
                            tishitextpopuwindow.dismiss();
                        } else if (TextUtils.isEmpty(password.getText().toString())) {
                            ToastUtil.showToast(BasicparameterActivity.this, "请输入密码！");
                            //ToastUtil.showToast(BasicparameterActivity.this, "Please enter your password！");
                            tishitextpopuwindow.dismiss();
                        } else if (TextUtils.isEmpty(phone.getText().toString())) {
                            ToastUtil.showToast(BasicparameterActivity.this, "请输入手机号！");
                            //ToastUtil.showToast(BasicparameterActivity.this, "Please enter your mobile phone number！");
                            tishitextpopuwindow.dismiss();
                        }

                    }
                });


                break;
            case R.id.master:
                popuwindow = new BasicparameterPopuwindow(BasicparameterActivity.this, texts, 0);
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_basic_parameter, null), Gravity.CENTER, 0, 0);
                popuwindow.SetOnclike(new BasicparameterPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {

                        master.setText(texts[position]);
                        //if (master.getText().toString().equals("0、无中亚主控无升降直连驱动板")) {
                        if (master.getText().toString().equals("0、无Magex主控，无升降驱动盘")) {
                            dialog.showLoading("Data acquisition in progress...");
                            LitePal.deleteAll(PathwayMolder.class);
                            orbitaldata();
                           /* List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);
                            adapter2.setupdatess(all,10);*/
                            Tracknumber = 10;
                            Loading_track(10);

                        } else {

                            handler.sendEmptyMessage(2);
                            LitePal.deleteAll(PathwayMolder.class);
                            getSerialDate();
                           /* List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);
                            adapter2.setupdatess(all,9);*/

                        }

                        popuwindow.dismiss();

                    }
                });
                break;

            case R.id.orbitalnumber:
                popuwindow = new BasicparameterPopuwindow(BasicparameterActivity.this, number, 7);
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_basic_parameter, null), Gravity.CENTER, 0, 0);
                popuwindow.SetOnclike(new BasicparameterPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {

                        orbitalnumber.setText(position + 1 + "");

                        List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);
                        PathwayMolder molder = new PathwayMolder();
                        for (int j = 0; j < all.size(); j++) {
                            if (Integer.parseInt(all.get(j).getPost()) > (position + 1) * Tracknumber - 1) {
                                LogUtils.d("BasicparameterActivity:主轨道:"+ j + "j");

                                molder.setPds("0");
                                molder.updateAll("post = ?", j + "");
                                //  molder.updateAll("post > ?",(position+1)*9-1+"");
                            }/*else{
                                Log.d("zlc",j+"+j");

                                molder.setPds("1");
                                molder.updateAll("post = ?",j+"");
                            }*/

                        }

                        List<PathwayMolder> all1 = LitePal.findAll(PathwayMolder.class);
                        adapter2.setupdate(position + 1, all1, hierarchy);
                        for (int i = 0; i < all1.size(); i++) {
                            LogUtils.d("BasicparameterActivity::更新:::"+ all1.get(i).getPds() + ":" + all1.get(i).getPost());
                        }
                        popuwindow.dismiss();

                    }
                });
                break;
        }
    }


    public String dispNetStatus(){
        // 获取联网方式,显示出来
        //TextView netstatus = findViewById(R.id.netstatus);
        String netstatus_txt = "";
        ConnectivityManager mConnectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);

        //检查网络连接
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info == null ) {
            netstatus_txt = "上网: 未知";
        } else {
            int netType = info.getType();
            int netSubtype = info.getSubtype();

            //LogUtils.i("netType="+netType + ";netSubtype="+netSubtype);

            if (netType == ConnectivityManager.TYPE_ETHERNET){
                netstatus_txt = "上网: 网线";
            } else if (netType == ConnectivityManager.TYPE_WIFI) {  //WIFI
                netstatus_txt = "上网: 无线";
            } else if (netType == ConnectivityManager.TYPE_MOBILE){// && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS && !mTelephony.isNetworkRoaming()) {   //MOBILE
                netstatus_txt = "4G";
            } else {
                netstatus_txt = "上网: 无";
            }
        }



        if(netstatus_txt.equals("上网: 无") || netstatus_txt.equals("上网: 未知")){
            return "0";
        } else if(netstatus_txt.equals("上网: 网线")){
            return "网线";
        } else if(netstatus_txt.equals("上网: 无线")) {
            return "无线";
        } else {
            int dbmlevel = getMobileDbmlevel();



            return ""+dbmlevel;
        }
    }


    public int getMobileDbmlevel() {
        int dbm = -999;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            List<CellInfo> cellInfoList = tm.getAllCellInfo();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (null != cellInfoList) {
                    for (CellInfo cellInfo : cellInfoList) {
                        if (cellInfo instanceof CellInfoGsm) {
                            CellSignalStrengthGsm cellSignalStrengthGsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                            dbm = cellSignalStrengthGsm.getDbm();
                            //Log.e("66666", "cellSignalStrengthGsm" + cellSignalStrengthGsm.toString());
                        } else if (cellInfo instanceof CellInfoCdma) {
                            CellSignalStrengthCdma cellSignalStrengthCdma = ((CellInfoCdma) cellInfo).getCellSignalStrength();
                            dbm = cellSignalStrengthCdma.getDbm();
                            //Log.e("66666", "cellSignalStrengthCdma" + cellSignalStrengthCdma.toString());
                        } else if (cellInfo instanceof CellInfoWcdma) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                CellSignalStrengthWcdma cellSignalStrengthWcdma = ((CellInfoWcdma) cellInfo).getCellSignalStrength();
                                dbm = cellSignalStrengthWcdma.getDbm();
                                //Log.e("66666", "cellSignalStrengthWcdma" + cellSignalStrengthWcdma.toString());
                            }
                        } else if (cellInfo instanceof CellInfoLte) {
                            CellSignalStrengthLte cellSignalStrengthLte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                            dbm = cellSignalStrengthLte.getDbm();
                            //Log.e("66666", "cellSignalStrengthLte.getAsuLevel()\t" + cellSignalStrengthLte.getAsuLevel());
                            //Log.e("66666", "cellSignalStrengthLte.getCqi()\t" + cellSignalStrengthLte.getCqi());
                            //Log.e("66666", "cellSignalStrengthLte.getDbm()\t " + cellSignalStrengthLte.getDbm());
                            //Log.e("66666", "cellSignalStrengthLte.getLevel()\t " + cellSignalStrengthLte.getLevel());
                            //Log.e("66666", "cellSignalStrengthLte.getRsrp()\t " + cellSignalStrengthLte.getRsrp());
                            //Log.e("66666", "cellSignalStrengthLte.getRsrq()\t " + cellSignalStrengthLte.getRsrq());
                            //Log.e("66666", "cellSignalStrengthLte.getRssnr()\t " + cellSignalStrengthLte.getRssnr());
                            //Log.e("66666", "cellSignalStrengthLte.getTimingAdvance()\t " + cellSignalStrengthLte.getTimingAdvance());
                        }
                    }
                }
            }
        } catch(SecurityException e){
            e.printStackTrace();
        }

        if (dbm >= -75) {
            return 5;
        } else if (dbm >= -85) {
            //59-79
            return  4;
        } else if (dbm >= -95) {
            //39-59
            return  3;
        } else if (dbm >= -100) {
            //19-39
            return  2;
        } else if (dbm >= -105) {
            //19-39
            return  1;
        } else {
            //0-19
            return  0;
        }
    }




    // 获取个推的客户端ID
    public String getClientID(){
        if(clientID.length() > 0) return clientID;
        String tmp_clientid =  PushManager.getInstance().getClientid(this.getApplicationContext());
        if(tmp_clientid == null || tmp_clientid.length() == 0){
            return "";
        } else {
            clientID = tmp_clientid;
            return clientID;
        }
    }
    /**
     * 上传数据解析
     * @param json
     */
    private void uploadBean(String json) {
        Gson gson = new Gson();
        uploadInformationBean = gson.fromJson(json, UploadInformationBean.class);
        handler.sendEmptyMessage(1);
    }

    /**
     * 无主控数据库
     */
    private void orbitaldata() {
        inv.clear();
        for (int i = 1; i < 8; i++) {
            for (int j = 0; j < 10; j++) {
                inv.add(i + "" + j);
            }
        }

        if (LitePal.count(PathwayMolder.class) == 0) {
            for (int i = 0; i < inv.size(); i++) {
                molder = new PathwayMolder();
                molder.setCode(inv.get(i));
                molder.setErrorcode("1");
                molder.setMergecode("01");
                molder.setNummax(6);
                molder.setNumnow(0);
                molder.setPds("1");
                molder.setSteps("03");
              //  molder.setPart(6);
                molder.setPost(i + "");
                molder.setPrecode("");
                molder.setName("");
                molder.setImage(R.drawable.noimage + "");
                molder.setPd(0);
                molder.setUp("0");
                molder.setPrice("0");
                molder.setTexts("");
                molder.setPretype("");
                molder.setInventory("0");
                molder.setChain(19);
                molder.setGoodscode("");
                molder.setErrortime("");
                molder.setCansale(1);
                molder.save();
            }
        }
        Tracknumber = 9;
        Loading_track(9);
    }

    /**
     * 选择每层的轨道数量
     *
     * @param positions
     */
    private void Choose_to_orbit(final int positions) {
        if (!TextUtils.isEmpty(master.getText().toString())) {
            if (master.getText().toString().equals("0、无Magex主控，无升降驱动盘")) {

                final BasicparameterPopuwindow popuwindow = new BasicparameterPopuwindow(BasicparameterActivity.this, number, 10);
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_basic_parameter, null), Gravity.CENTER, 0, 0);
                popuwindow.SetOnclike(new BasicparameterPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {
                        LogUtils.d("BasicparameterActivity::无驱动每层轨道数:position" + position);
                        PathwayMolder molder = new PathwayMolder();
                        List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);
                        for (int i = positions * 10; i < (positions + 1) * 10; i++) {

                            if (Integer.parseInt(all.get(i).getPost()) < positions * 10 + position + 1) {
                                LogUtils.d("BasicparameterActivity:::zlc"+ i + position + "?" + all.get(i).getPost());

                                molder.setPds("1");
                                molder.updateAll("post = ?", i + "");
                            } else {

                                molder.setPds("0");
                                molder.updateAll("post = ?", i + "");
                            }
                        }
                        List<PathwayMolder> all1 = LitePal.findAll(PathwayMolder.class);
                        for (int i = 0; i < all.size(); i++) {
                            LogUtils.d("BasicparameterActivity:::无驱动更新每层轨道数:"+ all1.get(i).getPds() + ":" + all1.get(i).getCode());
                        }
                        adapter2.setupdatees(all1, positions);
                        popuwindow.dismiss();
                    }
                });


            } else {
                final BasicparameterPopuwindow popuwindow = new BasicparameterPopuwindow(BasicparameterActivity.this, number, 9);
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_basic_parameter, null), Gravity.CENTER, 0, 0);
                popuwindow.SetOnclike(new BasicparameterPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {
                        LogUtils.d("BasicparameterActivity:::有magex主控更新每层轨道数:position" + position);
                        PathwayMolder molder = new PathwayMolder();
                        List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);
                        for (int i = positions * 9; i < (positions + 1) * 9; i++) {

                            if (Integer.parseInt(all.get(i).getPost()) < positions * 9 + position + 1) {

                                molder.setPds("1");
                                molder.updateAll("post = ?", i + "");
                            } else {

                                molder.setPds("0");
                                molder.updateAll("post = ?", i + "");
                            }
                        }
                        List<PathwayMolder> all1 = LitePal.findAll(PathwayMolder.class);

                        adapter2.setupdatees(all1, positions);
                        popuwindow.dismiss();
                    }
                });
            }
        }
    }


    /**
     * 有主控的数据获取
     */
    private void getSerialDate() {
      new Thread(new Runnable() {
          @Override
          public void run() {
              ComTrackMagex magex = new ComTrackMagex("");

              magex.openSerialPort();
              String status = magex.getStatus();
              LogUtils.d("BasicparameterActivity:::有magex:::status:" + status+"//////////"+magex.gettemp());
              String temperature = magex.gettemp().substring(magex.gettemp().indexOf("(") + 1, magex.gettemp().indexOf(")"));
              PrefheUtils.putString(BasicparameterActivity.this,"temperature",temperature);
              statusdetail5[0] = temperature;


              motorAddress14 = magex.getMotorAddress14();
              motorAddress58 = magex.getMotorAddress58();
              motorSteps14 = magex.getMotorSteps14();
              motorSteps58 = magex.getMotorSteps58();

              LogUtils.d("BasicparameterActivity:::有magex::motorSteps14:" + motorSteps14);
              LogUtils.d("BasicparameterActivity:::有magex::motorAddress14:" + motorAddress14);
              LogUtils.d("BasicparameterActivity:::有magex::motorAddress58:" + motorAddress58);
              LogUtils.d("BasicparameterActivity:::有magex::motorSteps58:" + motorSteps58);

              String address = motorAddress14 + "," + motorAddress58;
              String steps = motorSteps14 + "," + motorSteps58;
              magex.closeSerialPort();
              LogUtils.d("BasicparameterActivity:::有magex::motorSteps14::::"+motorSteps14);
              if(TextUtils.isEmpty(motorAddress14)){


                handler.sendEmptyMessage(4);
                  return;
              }
              patall = LitePal.findAll(PathwayMolder.class);
              if (LitePal.count(PathwayMolder.class) == 0) {
                  for (int a = 1; a < 8; a++) {
                      for (int b = 0; b < (address.split(",")[a - 1].substring(0, 18).length()) / 2; b++) {
                          int i = (address.split(",")[a - 1].substring(0, 18).length()) / 2 * (a - 1) + b;
                          molder = new PathwayMolder();
                          molder.setCode(a + "" + (b));
                          molder.setErrorcode("1");
                          molder.setMergecode(address.split(",")[a - 1].substring(b * 2, b * 2 + 2));
                          molder.setChain(19);
                          if(steps.split(",")[a - 1].substring(b * 2, b * 2 + 2).equals("9F")){
                              molder.setNummax(19/3);
                              molder.setSteps("03");
                          }else {
                              molder.setNummax(19 / (Integer.parseInt(steps.split(",")[a - 1].substring(b * 2, b * 2 + 2))));
                              molder.setSteps(steps.split(",")[a - 1].substring(b * 2, b * 2 + 2));
                          }
                          molder.setNumnow(0);
                          molder.setPd(0);
                          molder.setPost(i + "");

                          //molder.setPart(19 / (Integer.parseInt(steps.split(",")[a - 1].substring(b * 2, b * 2 + 2))));
                          molder.setPrecode("");
                          molder.setName("");
                          molder.setImage(R.drawable.noimage + "");
                          molder.setPd(0);
                          molder.setUp("0");
                          molder.setPds("1");
                          molder.setPrice("0");
                          molder.setTexts("");
                          molder.setPretype("");
                          molder.setInventory("0");
                          molder.setGoodscode("");
                          molder.setErrortime("");
                          molder.setCansale(1);
                          molder.save();

                      }
                  }
              } else {
                  for (int a = 1; a < 9; a++) {
                      for (int b = 0; b < (address.split(",")[a - 1].substring(0, 18).length()) / 2; b++) {
                          PathwayMolder molderss = new PathwayMolder();
                          int i = a * 10 + b * 2;
                          int i1 = a * 10 + b * 2 + 2;

                          molderss.setMergecode(address.split(",")[a - 1].substring(b * 2, b * 2 + 2));
                          molderss.setSteps(steps.split(",")[a - 1].substring(b * 2, b * 2 + 2));
                          molderss.setNummax(patall.get(0).getChain() / (Integer.parseInt(steps.split(",")[a - 1].substring(b * 2, b * 2 + 2))));
                          molderss.updateAll("post = ?", (address.split(",")[a - 1].substring(0, 18).length()) / 2 * (a - 1) + b + "");
                          //Log.d("zlc",molder.getId()+"\n");
                          // molder.updateAll((address.split(",")[a-1].substring(0,18).length())/2*a+b+"1");
                      }
                  }

              }
              handler.sendEmptyMessage(3);
          }
      }).start();



    }

    /**
     * 显示设置轨道
     *
     * @param es
     */
    private void Loading_track(int es) {
        dialog.dismiss();
        if(all != null) {
            all.clear();

        }
        all = LitePal.findAll(PathwayMolder.class);
        LogUtils.d("BasicparameterActivity:::zlc::all.size:"+all.size());
        for (int i = 1; i <= all.size() / es; i++) {

            for (int j = 0; j < es; j++) {
                if (all.get(i * es - es).getPds().equals("1")) {
                    xians[i - 1] = 1;
                    LogUtils.d("BasicparameterActivity:::zlc]i-1" + i);
                }
            }
        }
        hierar = 0;
        for (int a = 0; a < xians.length; a++) {
            hierar += xians[a];
        }
        LogUtils.d("BasicparameterActivity:::zlc::hierar:" + hierar);
        orbitalnumber.setText(hierar + "");
        pathway.setVisibility(View.VISIBLE);



        if (!TextUtils.isEmpty(master.getText().toString())) {
            if (master.getText().toString().equals("0、无Magex主控，无升降驱动盘")) {
                adapter2.setupdateas(hierarchy, hierar, all, 10);
            } else {
                adapter2.setupdateas(hierarchy, hierar, all, 9);
            }
        }
        orbitalRecy.setAdapter(adapter2);

        adapter2.SetOnclike(new HierarchyAdapter.Onclike() {
            @Override
            public void btonclike(final int positions) {


                Choose_to_orbit(positions);


            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
