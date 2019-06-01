package com.mnevent.hz.getui;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.util.Log;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.Utils.DateUtils;
import com.mnevent.hz.Utils.Gmethod;
import com.mnevent.hz.Utils.HttpUtils;
import com.mnevent.hz.Utils.LogUtils;
import com.mnevent.hz.Utils.MD5;
import com.mnevent.hz.Utils.MyHttp;
import com.mnevent.hz.Utils.OssUtil;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.litemolder.PathwayMolder;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android_serialport_api.ComTrackMagex;

/**
 * 个推服务
 */

public class GetuiService extends GTIntentService {

    String serialNumber,password;
    // 个推
    private String clientID = "";

    // 取得状态的值 中亚驱动板
    private String[] statusdetail = new String[]{"99","99","99","加热管1","加热管2","加热管3","加热管4","1号门开关","2号门开关","其它故障（有的话）","串口发送相关错误（有的话）"};

    private int[] number;
    List<PathwayMolder> pathlist = new ArrayList<>();

    List<PathwayMolder> pathlistes = new ArrayList<>();;

    List<PathwayMolder> pathlistess = new ArrayList<>();
    private Thread analazyThread;
    PathwayMolder molder;
    @Override
    public void onReceiveServicePid(Context context, int i) {
        Log.d("xx","getui:  " +  i+"");
    }

    @Override
    public void onReceiveClientId(Context context, String s) {
        Log.e(TAG, "onReceiveClientId -> " + "clientid = " + s);
        Log.d("xxxx","getui:  " + s);
    }

    @Override
    public synchronized void onReceiveMessageData(final Context context, GTTransmitMessage gtTransmitMessage) {
        LogUtils.d("Getui:"+gtTransmitMessage.getPayload()+":"+gtTransmitMessage.getPayloadId()+":"+gtTransmitMessage.getMessageId()+":"+gtTransmitMessage.getTaskId());
        serialNumber = PrefheUtils.getString(MyApp.mApplication, "serialNumber", "");

        password = PrefheUtils.getString(MyApp.mApplication, "password", "");
        String info = new String(gtTransmitMessage.getPayload());
        Log.d("xx","getui:  " + info);
        String content = info.substring(0,info.lastIndexOf(","));
        String time = info.substring(info.indexOf(","),info.length()-1);
        OssUtil.uploadFile(this, Environment.getExternalStorageDirectory().getPath() , "/mylogs/",time);
        Shipment(content);
        Intent intent = new Intent();
        intent.setAction("shipment");
        Bundle bundle = new Bundle();
        //出货状态1：开始出货
        bundle.putString("type","1");
        intent.putExtras(bundle);
        MyApp.mApplication.sendBroadcast(intent);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {
        Log.d("xx","getui:  " + b);
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        Log.d("xxcc","getui:  " + "ss");
    }


    private void Shipment(final String goods){

        new Thread(new Runnable() {
            @Override
            public void run() {

                pathlist.clear();
                pathlistess.clear();
                pathlist = LitePal.findAll(PathwayMolder.class);
                Log.d("zlc","pathlist.get(i)"+goods+","+pathlist.get(0).getGoodscode()+","+pathlist.get(0).getInventory()+""+pathlist.get(0).getMergecode()+","+pathlist.get(0).getErrorcode());
                Log.d("zlc","pathlist.get(i)"+pathlist.get(2).getGoodscode()+","+pathlist.get(2).getInventory()+""+pathlist.get(2).getMergecode()+","+pathlist.get(2).getErrorcode());
                for (int i = 0;i < pathlist.size();i++){
                    if(pathlist.get(i).getGoodscode().equals(goods) && Integer.parseInt(pathlist.get(i).getInventory())>0 && pathlist.get(i).getMergecode().equals("01") && pathlist.get(i).getErrorcode().equals("1")){
                        pathlistess.add(pathlist.get(i));
                        Log.d("zlc","pathlist.get(i)"+pathlist.get(i).getCode());
                    }
                }
                if (pathlistes.size() == 0){
                    return;
                }
                ComTrackMagex comTrack = new ComTrackMagex("");
                comTrack.openSerialPort();
                String code = pathlistess.get(0).getCode();
                LogUtils.d("MainActivity:::zlc::code:" + code);
                String rtn[] = comTrack.vend_out_ind(
                        Integer.parseInt(code.substring(0, 1)), Integer.parseInt(code.substring(1, 2)+1),

                        Integer.parseInt(pathlistess.get(0).getPrice()), "", "", "");
                LogUtils.d("MainActivity:::zlc::aplayssss:" + rtn[0]);
                comTrack.closeSerialPort();
                if(rtn[0].length() == 0){
                    LogUtils.d("MainActivity:::zlc::aplayssss:" + "成功");
                    molder = new PathwayMolder();
                    molder.setTime(DateUtils.getCurrentTime_Today()+"");
                    molder.setInventory(Integer.parseInt(pathlistess.get(0).getInventory())-1+"");
                    molder.updateAll("code = ?",code);
                    Intent intent = new Intent();
                    intent.setAction("shipment");
                    Bundle bundle = new Bundle();
                    //出货状态2：出货成功
                    bundle.putString("type","2");
                    intent.putExtras(bundle);
                    MyApp.mApplication.sendBroadcast(intent);
                }else {
                    LogUtils.d("MainActivity:::zlc::aplayssss:" + "失败");
                    molder = new PathwayMolder();
                    molder.setErrorcode("0");
                    molder.setErrortime(DateUtils.getCurrentTime_Today()+"");
                    molder.updateAll("code = ?",code);
                    Intent intent = new Intent();
                    intent.setAction("shipment");
                    Bundle bundle = new Bundle();
                    //出货状态1：开始失败
                    bundle.putString("type","3");
                    intent.putExtras(bundle);
                    MyApp.mApplication.sendBroadcast(intent);
                }
                initDate();
            }
        }).start();


    }

    private void initDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{ Thread.sleep(300000); } catch(Exception e){ e.printStackTrace();}
                pathlist.clear();
                pathlistes.clear();
                pathlist = LitePal.findAll(PathwayMolder.class);
                number= new int[]{0,0,0,0,0,0,0};
               /* for (int i = 0;i<pathlist.size();i++){
                    if(pathlist.get(i).getMergecode().equals("01") && pathlist.get(i).getErrorcode().equals("1")){
                        pathlistes.add(pathlist.get(i));
                    }
                }*/
                for (int i = 0;i < pathlist.size();i++){
                    if(pathlist.get(i).getCode().substring(0,1).equals("1")){
                        number[0] = number[0]+1;
                    } else if(pathlist.get(i).getCode().substring(0,1).equals("2")){
                        number[1] = number[1]+1;
                    } else if(pathlist.get(i).getCode().substring(0,1).equals("3")){
                        number[2] = number[2]+1;
                    } else if(pathlist.get(i).getCode().substring(0,1).equals("4")){
                        number[3] = number[3]+1;
                    } else if(pathlist.get(i).getCode().substring(0,1).equals("5")){
                        number[4] = number[4]+1;
                    } else if(pathlist.get(i).getCode().substring(0,1).equals("6")){
                        number[5] = number[5]+1;
                    } else if(pathlist.get(i).getCode().substring(0,1).equals("7")){
                        number[6] = number[6]+1;
                    }
                }

                ArrayList<HashMap<String,Object>> al = new ArrayList<>();


                //温度
                String temperature = PrefheUtils.getString(MyApp.mApplication, "temperature", "");
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

                String mdsrc = "adplanid="+0
                        + "&bills="+bills
                        + "&coins="+coins
                        + "&driverver="+"1.0.0"
                        + "&macerror="+mainError_tmp
                        + "&macid="+serialNumber
                        + "&macidsub="+""
                        + "&macstop="+"0"
                        + "&mactime="+((new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()))
                        + "&mainver="+"1.0.0"
                        + "&pushid="+getClientID()
                        + "&signalabc="+signale;



                mdsrc = mdsrc +"&temp1=" + (statusdetail[0].equals("温度1") ? "-" : temperature)
                        + "&temp2=" + (statusdetail[2].equals("温度3") ? "-" : temperature);
                mdsrc = mdsrc + "&track="+number[0]+","+number[1]+","+number[2]+","+number[3]+","+number[4]+","+number[5]+","+number[6];
                for (int i = 0;i<pathlist.size();i++){
                    mdsrc = mdsrc+"&track"+pathlist.get(i).getCode()+"="+pathlist.get(i).getGoodscode()
                            +","+pathlist.get(i).getNummax()+","+pathlist.get(i).getInventory()+","+(pathlist.get(i).getErrorcode().equals("1")?"":pathlist.get(i).getErrorcode()+"_"+pathlist.get(i).getErrortime())+","+
                            1+","+
                            ""+","+
                            ""+","+pathlist.get(i).getPrice()+"_"+pathlist.get(i).getPrice()+"_"+pathlist.get(i).getPrice()+"_"+pathlist.get(i).getPrice();


                    Gmethod.setNowStock(al,pathlist.get(i).getGoodscode(),Integer.parseInt(pathlist.get(i).getInventory()),Integer.parseInt(pathlist.get(i).getPrice()));
                }
                mdsrc = mdsrc
                        + "&trackplanid=0"
                        + "&upsoftver="+Gmethod.getAppVersion(MyApp.mApplication);
                long time = System.currentTimeMillis();
                String str = mdsrc.replace("&signalabc="+signale,"&signalabc="+signale_encode) +
                        "&timestamp="+time + "&accesskey=" + password;
                String md5 = MD5.GetMD5Code(str);
                //LogUtils.d("BasicparameterActivity:::zlc::numbers"+numbers);

                String ymds = DateUtils.getDateTimeByMillisecond(time + "", "yyyyMMddHHmmss");

                String dr = "-";
                if(statusdetail[4].contains("开着")) dr = "open";
                else if(statusdetail[4].contains("关着")) dr = "close";

                String rtnstr = (new MyHttp()).post(HttpUtils.getMcrealstatus,
                        mdsrc + "&timestamp="+time + "&md5=" + md5+"&nowstock="+Gmethod.getNowStockStr(al)+"&door="+dr+"&pkg=hz");

                LogUtils.d("心跳帧接口访问：rtnstr = "+rtnstr);



            }
        }).start();

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
}
