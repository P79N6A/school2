package com.freshtribes.icecream.presenter;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.baiduai.APIService;
import com.freshtribes.icecream.baiduai.exception.FaceError;
import com.freshtribes.icecream.baiduai.model.RegResult;
import com.freshtribes.icecream.baiduai.utils.OnResultListener;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.view.IFaceLoginView;
import com.freshtribes.icecream.view.IMainView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android_serialport_api.ComLift;
import android_serialport_api.ComQRIC;
import android_serialport_api.ComTrackMagex;

/**
 * Created by Administrator on 2017/8/22.
 */

public class MainPresenter {
    private IMainView iView;

    private ThreadGroup threadGroup;
    private boolean isStopThread = false;

    private MyApp myApp;

    private ComLift comlift;
    private ComTrackMagex comTrackMagex;
    private ComQRIC comQRIC;

    // 当前串口的状态
    private int comlift_status = 0;  // 0表示串口空闲，1表示取得状态中，2表示出货中
    // 是否正在访问数据库
    private boolean isdbaccessing = false;
    // 心跳用的秒计数器
    private int heart_secondcount = -1;
    // 是否发送了基本设置信息
    private boolean sentSetpara = false;

    // 取得状态的值 中亚驱动板
    private String[] statusdetail = new String[]{"99","99","99","加热管1","加热管2","加热管3","加热管4","1号门开关","2号门开关","其它故障（有的话）","串口发送相关错误（有的话）"};
    // Magex有主控的驱动板
    private String getTempRtn = "";
    private String getStatusRtn = "";

    public MainPresenter(IMainView view, ThreadGroup th, MyApp myApp){
        iView = view;
        threadGroup = th;
        this.myApp = myApp;
    }

    public void setStopThread(){
        isStopThread = true;
    }

    // 打开串口后，就进行状态的读取
    public void openVMC(final int devicetype,String comid,String qriccomid){
        if(devicetype==0) {
            comlift = new ComLift(comid);
            comlift.openSerialPort();
        } else  {
            comTrackMagex = new ComTrackMagex(comid);
            comTrackMagex.openSerialPort();
        }

        if(qriccomid != null){
            comQRIC = new ComQRIC(qriccomid);
            comQRIC.openSerialPort();
        }

        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                int count = 60;
                while(!isStopThread) {
                    // 1秒刷新1次
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    count++;
                    if(count > 60 && comlift_status == 0) {
                        count = 0;


                        if(devicetype==0) {
                            comlift_status = 1;
                            // 取得的状态信息保存起来，心跳时要用的
                            statusdetail = comlift.getStatus();

                            comlift_status = 0;

                            if (isStopThread) break;
                            else {
                                // 当然还有告诉Activity
                                iView.rtnGetStatus(statusdetail[0],statusdetail[7]);// 0:温度（字符型）  7::-表示位置，0表示关着，1表示开着
                            }
                        } else {
                            comlift_status = 1;
                            // 取得的状态信息保存起来，心跳时要用的
                            getTempRtn = comTrackMagex.gettemp();
                            getStatusRtn = comTrackMagex.getStatus();

                            comlift_status = 0;

                            if (isStopThread) break;
                            else {
                                if(getTempRtn.length()==0) getTempRtn = "-";
                                else {
                                    getTempRtn = getTempRtn.substring(getTempRtn.indexOf("(")+1,getTempRtn.length()-1);
                                }
                                if(getStatusRtn.length()==0) getStatusRtn = "-";
                                else {
                                    if(getStatusRtn.contains("Door open")) getStatusRtn = "1";
                                    else if(getStatusRtn.contains("Door closed")) getStatusRtn = "0";

                                    if(!getStatusRtn.contains("Delivery ok") || !getStatusRtn.contains("Machine ok")){
                                        iView.rtnDispStatus(getStatusRtn);
                                    }
                                }
                                // 当然还有告诉Activity
                                iView.rtnGetStatus(getTempRtn,getStatusRtn);
                            }
                        }

                    }
                }
                if(devicetype==0) {
                    comlift.closeSerialPort();
                    comlift = null;
                } else {
                    comTrackMagex.closeSerialPort();
                    comTrackMagex = null;
                }

                if(comQRIC != null){
                    comQRIC.closeSerialPort();
                }
            }
        });
        thread.start();
    }

    public void vendoutind(final int han,final int lie,final int fen,final String goodscode,final String payway,final String payinfo){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                while(comlift_status!=0){
                    // 1秒刷新1次
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if(isStopThread) return;
                }

                comlift_status = 2;
                String[] rtn = null;
                if(comlift != null ){
                    rtn = comlift.vend_out_ind(han,lie,fen,goodscode,payway,payinfo);
                } else {
                    rtn = comTrackMagex.vend_out_ind(han,lie,fen,goodscode,payway,payinfo);
                }
                comlift_status = 0;

                if(rtn[0].length()>0) {

                    if (rtn[0].contains("出货命令发送3次，对方无应答") || rtn[0].contains("出货命令发送3次对方无应答")) rtn[0] = "11";
                    else if (rtn[0].contains("返回错误应答:非法功能")) rtn[0] = "12";
                    else if (rtn[0].contains("返回错误应答:非法地址")) rtn[0] = "13";
                    else if (rtn[0].contains("返回错误应答:非法数据")) rtn[0] = "14";
                    else if (rtn[0].contains("返回错误应答:未知")) rtn[0] = "15";
                    else if (rtn[0].contains("正确应答的命令状态字:系统正忙")) rtn[0] = "16";
                    else if (rtn[0].contains("正确应答的命令状态字:货道故障")) rtn[0] = "17";
                    else if (rtn[0].contains("正确应答的命令状态字:升降装置故障")) rtn[0] = "18";
                    else if (rtn[0].contains("正确应答的命令状态字:移门装置故障")) rtn[0] = "19";
                    else if (rtn[0].contains("正确应答的命令状态字:红外检测故障")) rtn[0] = "20";
                    else if (rtn[0].contains("正确应答的命令状态字:取货门装置故障")) rtn[0] = "21";
                    else if (rtn[0].contains("正确应答的命令状态字:其它故障")) rtn[0] = "22";
                    else if (rtn[0].contains("收到应答的字节长度不对")) rtn[0] = "23";


                    else if (rtn[0].contains("配置错误")) rtn[0] = "31";
                    else if (rtn[0].contains("设备不可用")) rtn[0] = "32";
                    else if (rtn[0].contains("发送出货指令成功，查询出货结果中")) rtn[0] = "33";
                    else if (rtn[0].contains("DeliveryError{未知}")) rtn[0] = "34";
                    else if (rtn[0].contains("DeliveryError{红外未检测到货物}")) rtn[0] = "35";
                    else if (rtn[0].contains("DeliveryError{硬件故障")){
                        rtn[0] = rtn[0].replace("DeliveryError{硬件故障","").replace("}","");
                    } else if (rtn[0].contains("DeliveryErrorOther{未知出货错误"))  rtn[0] = "39";
                    else if (rtn[0].contains("收到应答的字节长度不对")) rtn[0] = "40";


                    else rtn[0] = "99";
                }

                if(rtn[0].length()>0){
                    // 那么有轨道故障了，要保存到数据库
                    LogUtils.e("弹簧机 -- 出货时发生错误(出货失败啦)轨道："+han+lie+":错误信息：" + rtn[0]);
                    String saleTime = "";
                    try {
                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        saleTime = sdf.format(date);
                    } catch (Exception e) {}

                    myApp.getTrackMainGeneral()[han*10+lie-10].setErrorcode(rtn[0]);
                    myApp.getTrackMainGeneral()[han*10+lie-10].setErrortime(saleTime);

                    // 修改数据库中的now库存数量
                    int dbwhile = 0;
                    while (isdbaccessing && dbwhile < 400) {
                        dbwhile++;
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isdbaccessing = true;
                    try {
                        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                        db.execSQL("update trackmaingeneral set errorcode=" + myApp.getTrackMainGeneral()[han*10+lie-10].getErrorcode()
                                + ",errortime=" + myApp.getTrackMainGeneral()[han*10+lie-10].getErrortime()
                                + " where id=" + (han*10+lie));
                        LogUtils.i("update trackmaingeneral set numnow=" + myApp.getTrackMainGeneral()[han*10+lie-10].getErrorcode()
                                + ",errortime=" + myApp.getTrackMainGeneral()[han*10+lie-10].getErrortime()
                                + " where id=" + (han*10+lie));

                        db.close();
                    } catch (SQLException e) {
                        LogUtils.e(" 执行SQL命令" + e.toString());
                    }
                    isdbaccessing = false;

                    // 让主页面进行商品显示的刷新
                    iView.HaveErrorFreshDisp();

                    heart_secondcount = 175;// 5秒后，发送心跳
                } else {
                    boolean iszero = false;
                    // 正常出货
                    myApp.getTrackMainGeneral()[han*10+lie-10].setNumnow(myApp.getTrackMainGeneral()[han*10+lie-10].getNumnow() - 1);
                    myApp.getTrackMainGeneral()[han*10+lie-10].setErrorcode("");
                    myApp.getTrackMainGeneral()[han*10+lie-10].setErrortime("");
                    if (myApp.getTrackMainGeneral()[han*10+lie-10].getNumnow() < 0) {
                        myApp.getTrackMainGeneral()[han*10+lie-10].setNumnow(0);
                    }
                    if(myApp.getTrackMainGeneral()[han*10+lie-10].getNumnow() == 0) iszero = true;


                    // 修改数据库中的now库存数量
                    int dbwhile = 0;
                    while (isdbaccessing && dbwhile < 400) {
                        dbwhile++;
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isdbaccessing = true;
                    try {
                        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                        db.execSQL("update trackmaingeneral set errorcode='',errortime='',numnow=" + myApp.getTrackMainGeneral()[han*10+lie-10].getNumnow()
                                + " where id=" + (han*10+lie));
                        LogUtils.i("update trackmaingeneral set errorcode='',errortime='',numnow=" + myApp.getTrackMainGeneral()[han*10+lie-10].getNumnow()
                                + " where id=" + (han*10+lie));
                        db.close();
                    } catch (SQLException e) {
                        LogUtils.e(" 执行SQL命令" + e.toString());
                    }
                    isdbaccessing = false;

                    // 如果数量变为0的话，那么要让画面刷新的
                    if(iszero){
                        iView.HaveErrorFreshDisp();
                    }

                    heart_secondcount = 175;// 5秒后，发送心跳
                }

                if (!isStopThread) {
                    iView.rtnVendOutInd(rtn);
                }

            }
        });
        thread.start();
    }

    public void closeVMC(){
        if(comlift != null) {
            comlift.closeSerialPort();
            comlift = null;
        }
        if(comTrackMagex != null){
            comTrackMagex.closeSerialPort();
            comTrackMagex = null;
        }
        if(comQRIC != null){
            comQRIC.closeSerialPort();
        }
    }

    public String getReceivedASCII(){
        return comQRIC.getReceivedASCII();
    }

    public void closeLED(){
        comQRIC.closeLED();
    }

    public void openLED(){
        comQRIC.openLED();
    }



    /**
     * 心跳线程
     */
    public void heart_thread(final long adplanid,final long trackplanid,final String upsoftver){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                try{ Thread.sleep(5000); } catch(Exception e){ e.printStackTrace();}

                while(!isStopThread){
                    // 1秒
                    try{ Thread.sleep(1000); } catch(Exception e){ e.printStackTrace();}

                    heart_secondcount = heart_secondcount + 1;
                    if (heart_secondcount > 180 || heart_secondcount == 0 ) { // 180秒，3分钟  或者首次进来时
                        heart_secondcount = 0;

                        // 每隔3分钟刷新联网方式
                        String signale = iView.dispNetStatus();
                        String signale_encode = "";
                        try {
                            signale_encode = URLEncoder.encode(signale,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        // 纸币器和硬币器的状态，先不加上纸币器硬币器
                        String bills = "nolink";
                        String coins = "nolink";

                        String drinkError_tmp = "";
                        String mainError_tmp = "";
                        String subError_tmp = "";

                        if (!sentSetpara) {
                            ArrayList<HashMap<String,Object>> al = new ArrayList<>();
                            // 发送基本信息
                            String mdsrc = "";

                            // 只有综合机
                            int[] mainTrack = {0,0,0,0,0,0,0};
                            mainTrack[0] = myApp.getMainGeneralLevel1TrackCount();
                            if(myApp.getMainGeneralLevelNum() == 7){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                                mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
                                mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
                                mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
                                mainTrack[6] = myApp.getMainGeneralLevel7TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 6){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                                mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
                                mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
                                mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 5){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                                mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
                                mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 4){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                                mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 3){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 2){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                            }
                            LogUtils.i("statusdetail:"+statusdetail.length+";"+statusdetail[0]);
                            mdsrc = "adplanid="+adplanid
                                    + "&bills="+bills
                                    + "&coins="+coins
                                    + "&driverver="+"1.0.0"
                                    + "&macerror="+mainError_tmp
                                    + "&macid="+myApp.getMainMacID()
                                    + "&macidsub="+""
                                    + "&macstop="+"0"
                                    + "&mactime="+((new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()))
                                    + "&mainver="+"1.0.0"
                                    + "&pushid="+iView.getClientID()
                                    + "&signalabc="+signale;
                            if(comlift!=null) {
                                mdsrc = mdsrc +"&temp1=" + (statusdetail[0].equals("温度1") ? "-" : statusdetail[0])
                                        + "&temp2=" + (statusdetail[2].equals("温度3") ? "-" : statusdetail[2]);
                            } else {
                                mdsrc = mdsrc +"&temp1=" + (getTempRtn.length()==0?"-":getTempRtn)
                                        + "&temp2=-";
                            }
                            mdsrc = mdsrc + "&track="+mainTrack[0]+","+mainTrack[1]+","+mainTrack[2]+","+mainTrack[3]+","+mainTrack[4]+","+mainTrack[5]+","+mainTrack[6];
                            for(int i=1;i<=7;i++) {
                                for(int j=0;j<mainTrack[i-1];j++) {
                                    mdsrc = mdsrc + "&track"+i+j+"="+myApp.getTrackMainGeneral()[i*10+j-10].getGoodscode()+","+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getNummax()+","+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getNumnow()+","+
                                            (myApp.getTrackMainGeneral()[i*10+j-10].getErrorcode().length()==0?"":
                                            myApp.getTrackMainGeneral()[i*10+j-10].getErrorcode()+"_"+myApp.getTrackMainGeneral()[i*10+j-10].getErrortime())+","+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getCansale()+","+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getBatchinfo()+","+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getEndday()+","+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getPaycash()+"_"+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getPayali()+"_"+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getPaywx()+"_"+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getPaymember();

                                    Gmethod.setNowStock(al,myApp.getTrackMainGeneral()[i*10+j-10].getGoodscode(),
                                            myApp.getTrackMainGeneral()[i*10+j-10].getNumnow(),
                                            myApp.getTrackMainGeneral()[i*10+j-10].getPaycash());
                                }
                            }
                            mdsrc = mdsrc
                                    + "&trackplanid="+trackplanid
                                    + "&upsoftver="+upsoftver;


                            long timestamp = System.currentTimeMillis();
                            String str = mdsrc.replace("&signalabc="+signale,"&signalabc="+signale_encode) +
                                    "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                            String md5 = MD5.GetMD5Code(str);

                            String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/macpara",
                                    mdsrc + "&timestamp="+timestamp + "&md5=" + md5+"&nowstock="+Gmethod.getNowStockStr(al)+"&pkg=icecream");
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
                                        sentSetpara = true;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    //msg = e.getMessage();
                                }
                            }

                            if(!sentSetpara){
                                // 加快速度,180-120=60  1分钟后继续
                                heart_secondcount = 150;
                            }

                        } else {
                            ArrayList<HashMap<String,Object>> al = new ArrayList<>();
                            // 发送库存心跳
                            String mdsrc = "";

                            // 只有综合机
                            int[] mainTrack = {0,0,0,0,0,0,0};
                            mainTrack[0] = myApp.getMainGeneralLevel1TrackCount();
                            if(myApp.getMainGeneralLevelNum() == 7){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                                mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
                                mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
                                mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
                                mainTrack[6] = myApp.getMainGeneralLevel7TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 6){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                                mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
                                mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
                                mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 5){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                                mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
                                mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 4){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                                mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 3){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                                mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
                            } else if(myApp.getMainGeneralLevelNum() == 2){
                                mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
                            }

                            mdsrc = "bills="+bills
                                    + "&coins="+coins
                                    + "&macerror="+mainError_tmp
                                    + "&macid="+myApp.getMainMacID()
                                    + "&macidsub="+""
                                    + "&macstop="+"0"
                                    + "&mactime="+((new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()))
                                    + "&pushid="+iView.getClientID()
                                    + "&signalabc="+signale;
                            if(comlift!=null) {
                                mdsrc = mdsrc +"&temp1=" + (statusdetail[0].equals("温度1") ? "-" : statusdetail[0])
                                        + "&temp2=" + (statusdetail[2].equals("温度3") ? "-" : statusdetail[2]);
                            } else {
                                mdsrc = mdsrc +"&temp1=" + (getTempRtn.length()==0?"-":getTempRtn)
                                        + "&temp2=-";
                            }
                            mdsrc = mdsrc + "&track="+mainTrack[0]+","+mainTrack[1]+","+mainTrack[2]+","+mainTrack[3]+","+mainTrack[4]+","+mainTrack[5]+","+mainTrack[6];
                            for(int i=1;i<=7;i++) {
                                for(int j=0;j<mainTrack[i-1];j++) {
                                    mdsrc = mdsrc + "&track"+i+j+"="+myApp.getTrackMainGeneral()[i*10+j-10].getNumnow()+","+
                                            (myApp.getTrackMainGeneral()[i*10+j-10].getErrorcode().length()==0?"":
                                            myApp.getTrackMainGeneral()[i*10+j-10].getErrorcode()+"_"+myApp.getTrackMainGeneral()[i*10+j-10].getErrortime())+","+
                                            myApp.getTrackMainGeneral()[i*10+j-10].getCansale();

                                    Gmethod.setNowStock(al,myApp.getTrackMainGeneral()[i*10+j-10].getGoodscode(),
                                            myApp.getTrackMainGeneral()[i*10+j-10].getNumnow(),
                                            myApp.getTrackMainGeneral()[i*10+j-10].getPaycash());
                                }
                            }

                            long timestamp = System.currentTimeMillis();
                            String str = mdsrc.replace("&signalabc="+signale,"&signalabc="+signale_encode)  +
                                    "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                            String md5 = MD5.GetMD5Code(str);

                            String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/macstock",
                                    mdsrc + "&timestamp="+timestamp + "&md5=" + md5+"&door="+(getStatusRtn.length()==0?"-":(getStatusRtn.equals("1")?"open":"close"))+"&nowstock="+Gmethod.getNowStockStr(al));
                            LogUtils.i("心跳，发送库存的返回值："+rtnstr);

                            //应用程序最大可用内存
                            int maxMemory = ((int) Runtime.getRuntime().maxMemory())/1024/1024;
                            //应用程序已获得内存
                            long totalMemory = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
                            //应用程序已获得内存中未使用内存
                            long freeMemory = ((int) Runtime.getRuntime().freeMemory())/1024/1024;
                            LogUtils.v("Main OnResume---> 应用程序最大可用内存="+maxMemory+"M,应用程序已获得内存="+totalMemory+"M,应用程序已获得内存中未使用内存="+freeMemory+"M");


                            // 上传上次的销售记录啦
                            // 数据库操作中...
                            int dbwhile = 0;
                            while (isdbaccessing && dbwhile < 400) {
                                dbwhile++;
                                try {
                                    Thread.sleep(10);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            isdbaccessing = true;

                            SQLiteDatabase dbisup = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                            Cursor isupcursor = dbisup.rawQuery("SELECT saletime,isup,trackno,goodscode,mingcheng,batch,endday,price,payway,payinfo FROM saledata "
                                    + " where isup = 0 "
                                    + "order by saletime limit 1", null);
                            if (isupcursor.moveToNext())
                            {
                                String goodscode = isupcursor.getString(isupcursor.getColumnIndex("goodscode"));
                                if(goodscode.length()>0)
                                    goodscode = "" + Integer.parseInt(goodscode);
                                String saletime = isupcursor.getString(isupcursor.getColumnIndex("saletime"));
                                String trackno = isupcursor.getString(isupcursor.getColumnIndex("trackno"));
                                //String goodscode = isupcursor.getString(isupcursor.getColumnIndex("goodscode"));
                                //String mingcheng = isupcursor.getString(isupcursor.getColumnIndex("mingcheng"));
                                String batch = isupcursor.getString(isupcursor.getColumnIndex("batch"));
                                String endday = isupcursor.getString(isupcursor.getColumnIndex("endday"));
                                String price = "" + isupcursor.getInt(isupcursor.getColumnIndex("price"));
                                String payway = isupcursor.getString(isupcursor.getColumnIndex("payway"));
                                String payinfo = isupcursor.getString(isupcursor.getColumnIndex("payinfo"));

                                isupcursor.close();
                                dbisup.close();
                                isdbaccessing = false;

                                timestamp = System.currentTimeMillis();
                                str = "batch="+batch + "&endday="+endday+"&goodscode="+goodscode+
                                        "macid="+myApp.getMainMacID()+"&mactime="+ saletime.replace("-","").replace(":","").replace(" ","")+
                                        "&payinfo="+payinfo+ "&payway="+payway + "&price="+price +"&track="+trackno+
                                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                                md5 = MD5.GetMD5Code(str);

                                String soaprtn = (new MyHttp()).post(myApp.getServerurl()+"/sale",
                                        "batch="+batch + "&endday="+endday+"&goodscode="+goodscode+
                                                "macid="+myApp.getMainMacID()+"&mactime="+ saletime.replace("-","").replace(":","").replace(" ","")+
                                                "&payinfo="+payinfo+ "&payway="+payway + "&price="+price +"&track="+trackno+
                                                "&timestamp="+timestamp +  "&md5=" + md5);

                                LogUtils.i("心跳中的发送销售记录的结果返回:" + soaprtn);

                                if(soaprtn.length() > 0){
                                    try{
                                        JSONObject soapJson = new JSONObject(soaprtn);

                                        String code = soapJson.getString("code");
                                        int isup = 1;
                                        if(!code.equals("0")) {
                                            LogUtils.e("心跳中的发送销售记录,服务端返回错误信息("+soapJson.getString("msg")+")，此次强制设为2");
                                            isup = 2;
                                        }

                                        // 数据库操作中...
                                        dbwhile = 0;
                                        while (isdbaccessing && dbwhile < 400) {
                                            dbwhile++;
                                            try {
                                                Thread.sleep(10);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        isdbaccessing = true;

                                        SQLiteDatabase dbsale = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                                        dbsale.execSQL("update saledata set isup = " + isup + " where saletime = '" + saletime + "'");
                                        LogUtils.i("上传销售记录失败：saletime = " + saletime + ";isup="+isup);
                                        dbsale.close();
                                        isdbaccessing = false;

                                    } catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                // 没有未上传的销售数据
                                isupcursor.close();
                                dbisup.close();
                                isdbaccessing = false;
                            }
                        }
                    }
                }
            }
        });
        thread.start();

    }

    /**
     * payway:cash,alipay,wxpay,iccard,wxpaycode,thirdpaycode,facepay
     * paypara:"",ali[0] + "," + ali[1] + "," + ali[2],                               商户交易号,支付宝交易号,支付者buyer_logon_id,fen
     *            wx[0] + "," + wx[1] + "," + wx[2] + "," + wx[3] + "," + wx[4],      商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注),fen
     *            t_paypara.substring(0).replace(",iccard", ""),
     *            t_paypara.substring(1).replace(",wxpaycode", ""
     *            t_paypara.substring(1).replace(",thirdpaycode", "")
     *            t_paypara.substring(1).replace(",facepay", "")
     */
    public void send_SaleDataThread(String payway,String saletime,String pricefen,
                                    String trackno,String goodscode,String paypara){
        final String t_payway = payway;
        final String t_saletime = saletime;
        final int t_pricefen = Integer.parseInt(pricefen);
        final String t_trackno = trackno; // 3位数
        String code_tmp =  goodscode;
//        if(code_tmp.length()==1) code_tmp = "0000"+code_tmp;
//        else if(code_tmp.length()==2) code_tmp = "000"+code_tmp;
//        else if(code_tmp.length()==3) code_tmp = "00"+code_tmp;
//        else if(code_tmp.length()==4) code_tmp = "0"+code_tmp;
//        else if(code_tmp.length()>5) code_tmp = code_tmp.substring(0,5);
        final String t_code = code_tmp;

        String tmppayinfo = "";
        if(payway.equals("cash")) {
            tmppayinfo = "";
        } else if(payway.equals("umspay")) {
            String[] ali = paypara.split(",");
            if (ali.length >= 3) {
                tmppayinfo = ali[0] + "," + ali[1] + "," + ali[2];
            }
        } else if(payway.equals("alipay")) {
            String[] ali = paypara.split(",");
            if (ali.length >= 3) {
                tmppayinfo = ali[0] + "," + ali[1] + "," + ali[2];
            }
        } else if(payway.equals("wxpay")) {
            String[] wx = paypara.split(",");
            if (wx.length >= 5) {
                tmppayinfo = wx[0] + "," + wx[1] + "," + wx[2] + "," + wx[3] + "," + wx[4];
            }
        } else if(payway.equals("iccard")) {
            tmppayinfo = paypara.substring(0).replace(",iccard", "");
        } else if(payway.equals("wxpaycode")) {
            tmppayinfo = paypara.substring(0).replace(",wxpaycode", "");
            if(tmppayinfo.length()>32) tmppayinfo = tmppayinfo.substring(tmppayinfo.length()-32);
        } else if(payway.equals("thirdpaycode")) {
            tmppayinfo = paypara.substring(0).replace(",thirdpaycode", "");
        } else if(payway.equals("facepay")) {
            tmppayinfo = paypara.substring(0).replace(",facepay", "");
        }

        final String t_paypara = tmppayinfo;

        LogUtils.i("销售要发送的记录：payway="+t_payway+";saletime="+t_saletime+";fen="+t_pricefen+";trackno="+t_trackno+";code="+t_code+";paypara="+t_paypara);

        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                String l_saletime = t_saletime;

                // 数据库操作中...
                int dbwhile = 0;
                while (isdbaccessing && dbwhile < 400) {
                    dbwhile++;
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isdbaccessing = true;

                // 插入销售数据
                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                Cursor isupcursor = db.rawQuery("SELECT saletime FROM saledata  where saletime = '" + l_saletime + "'", null);
                if (isupcursor.moveToNext()){
                    // 说明已经存在了的
                    isupcursor.close();
                    db.close();
                    isdbaccessing = false;
                } else {
                    isupcursor.close();

                    String mingcheng = "";
                    String batchinfo = "";
                    String endday = "";
                    // 通过货道号取得相应名称，批次信息，保质期日期
                    //if (t_trackno.substring(0, 1).equals("0")) {
                        // 表示为主机的出货
                        mingcheng = myApp.getTrackMainGeneral()[Integer.parseInt(t_trackno.substring(1, 3)) - 10].getGoodsname();
                        batchinfo = myApp.getTrackMainGeneral()[Integer.parseInt(t_trackno.substring(1, 3)) - 10].getBatchinfo();
                        endday = myApp.getTrackMainGeneral()[Integer.parseInt(t_trackno.substring(1, 3)) - 10].getEndday();
                    //}

                    db.execSQL("insert into saledata values('" + l_saletime + "',1,'" + t_trackno + "','" + t_code + "','" + mingcheng
                            + "','" + batchinfo + "','" + endday + "'," + t_pricefen + ",'"
                            + t_payway + "','" + t_paypara + "')");
                    db.close();
                    isdbaccessing = false;


                    // 所有参数合法
                    long timestamp = System.currentTimeMillis();
                    String str = "batch=" + batchinfo + "&endday=" + endday + "&goodscode=" + t_code +
                            "&macid=" + myApp.getMainMacID() + "&mactime=" + l_saletime +
                            "&payinfo=" + t_paypara + "&payway=" + t_payway + "&price=" + t_pricefen + "&track=" + t_trackno +
                            "&timestamp=" + timestamp + "&accesskey=" + myApp.getAccessKey();
                    String md5 = MD5.GetMD5Code(str);

                    String soaprtn = (new MyHttp()).post(myApp.getServerurl() + "/sale",
                            "batch=" + batchinfo + "&endday=" + endday + "&goodscode=" + t_code +
                                    "&macid=" + myApp.getMainMacID() + "&mactime=" + l_saletime +
                                    "&payinfo=" + t_paypara + "&payway=" + t_payway + "&price=" + t_pricefen + "&track=" + t_trackno +
                                    "&timestamp=" + timestamp + "&md5=" + md5);

                    LogUtils.i("发送销售记录的结果返回:" + soaprtn);

                    if (soaprtn.length() > 0) {
                        try {
                            JSONObject soapJson = new JSONObject(soaprtn);

                            String code = soapJson.getString("code");
                            if (!code.equals("0")) {
                                // 数据库操作中...
                                dbwhile = 0;
                                while (isdbaccessing && dbwhile < 400) {
                                    dbwhile++;
                                    try {
                                        Thread.sleep(10);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                isdbaccessing = true;

                                SQLiteDatabase dbsale = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                                dbsale.execSQL("update saledata set isup = 0 where saletime = '" + l_saletime + "'");
                                LogUtils.i("上传销售记录失败：saletime = " + l_saletime);
                                dbsale.close();
                                isdbaccessing = false;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 数据库操作中...
                        dbwhile = 0;
                        while (isdbaccessing && dbwhile < 400) {
                            dbwhile++;
                            try {
                                Thread.sleep(10);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        isdbaccessing = true;

                        SQLiteDatabase dbsale = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                        dbsale.execSQL("update saledata set isup = 0 where saletime = '" + l_saletime + "'");
                        LogUtils.i("上传销售记录失败：saletime = " + l_saletime);

                        dbsale.close();
                        isdbaccessing = false;
                    }
                }
            }
        });
        thread.start();
    }

    public void refund_alipay(final String outtradeno,final int fen){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                String str = "macid=" + myApp.getMainMacID()+ "&outtradeno="+outtradeno+"&price="+fen+
                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/alipayrefund",
                        "macid=" + myApp.getMainMacID() + "&outtradeno="+outtradeno +"&price="+fen+ "&timestamp="+timestamp + "&md5=" + md5);
                LogUtils.i("支付宝退款接口,商户交易号："+outtradeno+";退款的返回值："+rtnstr);
            }
        });
        thread.start();
    }

    public void refund_umspay(final String outtradeno,final int fen){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                String str = "macid=" + myApp.getMainMacID()+ "&outtradeno="+outtradeno+"&price="+fen+
                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/umspayrefund",
                        "macid=" + myApp.getMainMacID() + "&outtradeno="+outtradeno +"&price="+fen+ "&timestamp="+timestamp + "&md5=" + md5);
                LogUtils.i("支付宝退款接口,商户交易号："+outtradeno+";退款的返回值："+rtnstr);
            }
        });
        thread.start();
    }

    public void refund_wxpay(final String outtradeno,final int fen){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                String str = "macid=" + myApp.getMainMacID()+ "&outtradeno="+outtradeno+"&price="+fen+
                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/wxpayrefund",
                        "macid=" + myApp.getMainMacID() + "&outtradeno="+outtradeno+"&price="+fen+ "&timestamp="+timestamp + "&md5=" + md5);
                LogUtils.i("微信退款接口,商户交易号："+outtradeno+";退款的返回值："+rtnstr);
            }
        });
        thread.start();

    }

    public void refund_zxalipay(final String outtradeno,final int fen){
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                String str = "macid=" + myApp.getMainMacID()+ "&outtradeno="+outtradeno+"&price="+fen+
                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/zxalipayrefund",
                        "macid=" + myApp.getMainMacID() + "&outtradeno="+outtradeno +"&price="+fen+ "&timestamp="+timestamp + "&md5=" + md5);
                LogUtils.i("中信银行支付宝退款接口,商户交易号："+outtradeno+";退款的返回值："+rtnstr);
            }
        });
        thread.start();
    }

    public void refund_zxwxpay(final String outtradeno,final int fen){
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                String str = "macid=" + myApp.getMainMacID()+ "&outtradeno="+outtradeno+"&price="+fen+
                        "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/zxwxpayrefund",
                        "macid=" + myApp.getMainMacID() + "&outtradeno="+outtradeno+"&price="+fen+ "&timestamp="+timestamp + "&md5=" + md5);
                LogUtils.i("中信银行微信退款接口,商户交易号："+outtradeno+";退款的返回值："+rtnstr);
            }
        });
        thread.start();

    }


    public void checkFaceUserMoney(final String uid,final int pricefen){
        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {

                int code = 2;
                String msg = "解析返回值出错";

                String paycode = "";

                long timestamp = System.currentTimeMillis();
                String str = "macid="+myApp.getMainMacID()  + "&uid="+uid + "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/baiduwxusermoney",
                        "macid="+myApp.getMainMacID()  + "&uid="+uid + "&timestamp="+timestamp + "&md5=" + md5);

                if(rtnstr.length() > 0){
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        code = soapJson.getInt("code");
                        msg = soapJson.getString("msg");
                        if(code == 0) {
                            int serverfen = soapJson.getInt("balance");
                            if(serverfen >= pricefen) {
                                iView.rtnFaceMoneyCheck("");
                            } else {
                                iView.rtnFaceMoneyCheck("亲，你的余额不足。");
                            }
                            return;
                        } else {
                            iView.rtnFaceMoneyCheck("服务端返回错误:"+msg);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //msg = e.getMessage();
                        iView.rtnFaceMoneyCheck(e.getMessage());
                        return;
                    }
                } else {
                    iView.rtnFaceMoneyCheck("联网失败");
                    return;
                }

            }
        });

        thread.start();
    }

    public void traninmac(String supplycode,String tranin_data){
        ArrayList<Map<String,String>> newList = new ArrayList<>();

        // 开始处理  10,batch,endday,5;12,batch,endday,10
        String[] dataA = tranin_data.split(";");
        for(int i=0;i<dataA.length;i++) {
            String[] single = dataA[i].split(",");
            String trackno = single[0];
            String goodscode = "";
            goodscode = myApp.getTrackMainGeneral()[Integer.parseInt(trackno) - 10].getGoodscode();

            int num = Integer.parseInt(single[3]);
            String batch = single[1];
            String endday = single[2];

            Gmethod.doHm(newList, trackno,
                    goodscode, "", batch, endday, num);
        }

        String data = "";
        for(int i=0;i<newList.size();i++){
            Map<String,String> hm = newList.get(i);
            data = data + hm.get("trackgoodscode") + "," +
                    //hm.get("trackgoodsname") + "," +
                    "" + "," +
                    hm.get("batch") + "," +
                    hm.get("endday") + "," +
                    hm.get("change") + "," +
                    hm.get("detail") + ";";
        }
        if(data.length() > 0){
            data = data.substring(0,data.length()-1);
        }

        long timestamp = System.currentTimeMillis();
        String str = "data="+data + "&macid="+myApp.getMainMacID() + "&supplycode=" + supplycode+ "&type=3" + "&userid=" + myApp.getTempUserId() +
                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
        String md5 = MD5.GetMD5Code(str);

        String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendstockevent",
                "macid="+myApp.getMainMacID() + "&data="+data +  "&supplycode=" + supplycode + "&type=3"  + "&userid=" + myApp.getTempUserId()+
                        "&timestamp="+timestamp + "&md5=" + md5);
        if(rtnstr.length()>0) {
            int code = 2;
            String msg = "解析返回值出错啦";
            try {
                JSONObject soapJson = new JSONObject(rtnstr);
                code = soapJson.getInt("code");
                msg = soapJson.getString("msg");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (code == 0) {
                // 数据保存到本机数据库
                // 数据库操作中...
                int dbwhile = 0;
                while (isdbaccessing && dbwhile < 400) {
                    dbwhile++;
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isdbaccessing = true;

                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                try {
                    for (int i = 0; i < dataA.length; i++) {
                        String[] dataAA = dataA[i].split(",");
                        if (dataAA.length == 4) {
                            // 收到数据了，那么进行处理

                                int inttrackno = Integer.parseInt(dataAA[0]);
                                int newnow = myApp.getTrackMainGeneral()[inttrackno - 10].getNumnow() + Integer.parseInt(dataAA[3]);

                                if(newnow > myApp.getTrackMainGeneral()[inttrackno - 10].getNummax()){
                                    newnow = myApp.getTrackMainGeneral()[inttrackno - 10].getNummax();
                                }

                                db.execSQL("update trackmaingeneral set numnow=" + newnow
                                        + ",batchinfo='" + dataAA[1] + "',endday='" + dataAA[2]
                                        + "' where id=" + inttrackno);

                                myApp.getTrackMainGeneral()[inttrackno - 10].setNumnow(newnow);
                                myApp.getTrackMainGeneral()[inttrackno - 10].setBatchinfo(dataAA[1]);
                                myApp.getTrackMainGeneral()[inttrackno - 10].setEndday(dataAA[2]);

                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }


                db.close();
                isdbaccessing = false;

                // 启动心跳
                heart_secondcount = 175;// 5秒后，发送心跳(完整的)
                sentSetpara = false;

                // 刷新屏幕
                // 让主页面进行商品显示的刷新
                iView.HaveErrorFreshDisp();
            }
        }
    }

    public void tranoutmac(String supplycode,String tranout_data){
        ArrayList<Map<String,String>> newList = new ArrayList<>();

        // 开始处理  10,batch,endday,5;12,batch,endday,10
        String[] dataA = tranout_data.split(";");
        for(int i=0;i<dataA.length;i++) {
            String[] single = dataA[i].split(",");
            String trackno = single[0];
            String goodscode = "";
            goodscode = myApp.getTrackMainGeneral()[Integer.parseInt(trackno) - 10].getGoodscode();
            int num = -1 * Integer.parseInt(single[3]);
            String batch = single[1];
            String endday = single[2];

            Gmethod.doHm(newList, trackno,
                    goodscode, "", batch, endday, num);
        }

        String data = "";
        for(int i=0;i<newList.size();i++){
            Map<String,String> hm = newList.get(i);
            data = data + hm.get("trackgoodscode") + "," +
                    //hm.get("trackgoodsname") + "," +
                    "" + "," +
                    hm.get("batch") + "," +
                    hm.get("endday") + "," +
                    hm.get("change") + "," +
                    hm.get("detail") + ";";
        }
        if(data.length() > 0){
            data = data.substring(0,data.length()-1);
        }

        long timestamp = System.currentTimeMillis();
        String str = "data="+data + "&macid="+myApp.getMainMacID() + "&supplycode=" + supplycode+ "&type=2&userid=" + myApp.getTempUserId() +
                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
        //System.out.println(str);
        String md5 = MD5.GetMD5Code(str);

        String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendstockevent",
                "macid="+myApp.getMainMacID() + "&data="+data +  "&supplycode=" + supplycode + "&type=2&userid=" + myApp.getTempUserId()+
                        "&timestamp="+timestamp + "&md5=" + md5);

        if(rtnstr.length()>0) {
            int code = 2;
            String msg = "解析返回值出错啦";
            try {
                JSONObject soapJson = new JSONObject(rtnstr);
                code = soapJson.getInt("code");
                msg = soapJson.getString("msg");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (code == 0) {
                // 数据保存到本机数据库
                // 数据库操作中...
                int dbwhile = 0;
                while (isdbaccessing && dbwhile < 400) {
                    dbwhile++;
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isdbaccessing = true;

                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                try {
                    for (int i = 0; i < dataA.length; i++) {
                        String[] dataAA = dataA[i].split(",");
                        if (dataAA.length == 4) {

                                int inttrackno = Integer.parseInt(dataAA[0]);
                                int newnow = myApp.getTrackMainGeneral()[inttrackno - 10].getNumnow() - Integer.parseInt(dataAA[3]);

                                if(newnow < 0){
                                    newnow = 0;
                                }

                                db.execSQL("update trackmaingeneral set numnow=" + newnow
                                        + ",batchinfo='" + (newnow==0?"":dataAA[1]) + "',endday='" + (newnow==0?"":dataAA[2])
                                        + "' where id=" + inttrackno);

                                myApp.getTrackMainGeneral()[inttrackno - 10].setNumnow(newnow);
                                myApp.getTrackMainGeneral()[inttrackno - 10].setBatchinfo(newnow==0?"":dataAA[1]);
                                myApp.getTrackMainGeneral()[inttrackno - 10].setEndday(newnow==0?"":dataAA[2]);
                            }

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }


                db.close();
                isdbaccessing = false;

                // 启动心跳
                heart_secondcount = 175;// 5秒后，发送心跳(完整的)
                sentSetpara = false;

                // 刷新屏幕
                // 让主页面进行商品显示的刷新
                iView.HaveErrorFreshDisp();
            }
        }
    }

    public void stockoutmac(String supplycode,String tranout_data){
        ArrayList<Map<String,String>> newList = new ArrayList<>();

        // 开始处理  10,batch,endday,5;12,batch,endday,10
        String[] dataA = tranout_data.split(";");
        for(int i=0;i<dataA.length;i++) {
            String[] single = dataA[i].split(",");
            String trackno = single[0];
            String goodscode = "";

            goodscode = myApp.getTrackMainGeneral()[Integer.parseInt(trackno) - 10].getGoodscode();

            int num = -1 * Integer.parseInt(single[3]);
            String batch = single[1];
            String endday = single[2];

            Gmethod.doHm(newList, trackno,
                    goodscode, "", batch, endday, num);
        }

        String data = "";
        for(int i=0;i<newList.size();i++){
            Map<String,String> hm = newList.get(i);
            data = data + hm.get("trackgoodscode") + "," +
                    //hm.get("trackgoodsname") + "," +
                    "" + "," +
                    hm.get("batch") + "," +
                    hm.get("endday") + "," +
                    hm.get("change") + "," +
                    hm.get("detail") + ";";
        }
        if(data.length() > 0){
            data = data.substring(0,data.length()-1);
        }

        long timestamp = System.currentTimeMillis();
        String str = "data="+data + "&macid="+myApp.getMainMacID() + "&supplycode=" + supplycode+ "&type=4&userid=" + myApp.getTempUserId() +
                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
        //System.out.println(str);
        String md5 = MD5.GetMD5Code(str);

        String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendstockevent",
                "macid="+myApp.getMainMacID() + "&data="+data +  "&supplycode=" + supplycode + "&type=4&userid=" + myApp.getTempUserId()+
                        "&timestamp="+timestamp + "&md5=" + md5);

        if(rtnstr.length()>0) {
            int code = 2;
            String msg = "解析返回值出错啦";
            try {
                JSONObject soapJson = new JSONObject(rtnstr);
                code = soapJson.getInt("code");
                msg = soapJson.getString("msg");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (code == 0) {
                // 数据保存到本机数据库
                // 数据库操作中...
                int dbwhile = 0;
                while (isdbaccessing && dbwhile < 400) {
                    dbwhile++;
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isdbaccessing = true;

                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                try {
                    for (int i = 0; i < dataA.length; i++) {
                        String[] dataAA = dataA[i].split(",");
                        if (dataAA.length == 4) {

                                int inttrackno = Integer.parseInt(dataAA[0]);
                                int newnow = myApp.getTrackMainGeneral()[inttrackno - 10].getNumnow() - Integer.parseInt(dataAA[3]);

                                if(newnow < 0){
                                    newnow = 0;
                                }

                                db.execSQL("update trackmaingeneral set numnow=" + newnow
                                        + ",batchinfo='" + (newnow==0?"":dataAA[1]) + "',endday='" + (newnow==0?"":dataAA[2])
                                        + "' where id=" + inttrackno);

                                myApp.getTrackMainGeneral()[inttrackno - 10].setNumnow(newnow);
                                myApp.getTrackMainGeneral()[inttrackno - 10].setBatchinfo(newnow==0?"":dataAA[1]);
                                myApp.getTrackMainGeneral()[inttrackno - 10].setEndday(newnow==0?"":dataAA[2]);

                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }


                db.close();
                isdbaccessing = false;

                // 启动心跳
                heart_secondcount = 175;// 5秒后，发送心跳(完整的)
                sentSetpara = false;

                // 刷新屏幕
                // 让主页面进行商品显示的刷新
                iView.HaveErrorFreshDisp();
            }
        }
    }

    public void checkstockmac(String supplycode,String checkstock_data){
        ArrayList<Map<String,String>> newList = new ArrayList<>();

        if(checkstock_data.length() > 0) {
            // 开始处理  10,batch,endday,5;12,batch,endday,10
            String[] dataA = checkstock_data.split(";");
            for (int i = 0; i < dataA.length; i++) {
                String[] single = dataA[i].split(",");
                String trackno = single[0];
                String goodscode = "";
                String batch = "";
                String endday = "";

                    goodscode = myApp.getTrackMainGeneral()[Integer.parseInt(trackno) - 10].getGoodscode();
                    batch = myApp.getTrackMainGeneral()[Integer.parseInt(trackno) - 10].getBatchinfo();
                    endday = myApp.getTrackMainGeneral()[Integer.parseInt(trackno) - 10].getEndday();

                int num = Integer.parseInt(single[1]);


                Gmethod.doHm(newList, trackno,
                        goodscode, "", batch, endday, num);
            }
        }

        String data = "";
        for(int i=0;i<newList.size();i++){
            Map<String,String> hm = newList.get(i);
            data = data + hm.get("trackgoodscode") + "," +
                    //hm.get("trackgoodsname") + "," +
                    "" + "," +
                    hm.get("batch") + "," +
                    hm.get("endday") + "," +
                    hm.get("change") + "," +
                    hm.get("detail") + ";";
        }
        if(data.length() > 0){
            data = data.substring(0,data.length()-1);
        }

        long timestamp = System.currentTimeMillis();
        String str = "data="+data + "&macid="+myApp.getMainMacID()  + "&supplycode=" + supplycode + "&userid=" + myApp.getTempUserId() +
                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
        String md5 = MD5.GetMD5Code(str);

        String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendcheckresult",
                "macid="+myApp.getMainMacID() + "&data="+data +  "&supplycode=" + supplycode + "&userid=" + myApp.getTempUserId()+
                        "&timestamp="+timestamp + "&md5=" + md5);

        if(rtnstr.length()>0) {
            int code = 2;
            String msg = "解析返回值出错啦";
            try {
                JSONObject soapJson = new JSONObject(rtnstr);
                code = soapJson.getInt("code");
                msg = soapJson.getString("msg");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (code == 0) {
                if(checkstock_data.length() > 0) {
                    String[] dataA = checkstock_data.split(";");

                    // 数据保存到本机数据库
                    // 数据库操作中...
                    int dbwhile = 0;
                    while (isdbaccessing && dbwhile < 400) {
                        dbwhile++;
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isdbaccessing = true;

                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                    try {
                        for (int i = 0; i < dataA.length; i++) {
                            String[] dataAA = dataA[i].split(",");
                            if (dataAA.length == 2) {
                                //int num = Integer.parseInt(dataAA[1]);

                                // 收到数据了，那么进行处理

                                    int inttrackno = Integer.parseInt(dataAA[0]);

                                    int newnow = myApp.getTrackMainGeneral()[inttrackno - 10].getNumnow() + Integer.parseInt(dataAA[1]);

                                    if (newnow < 0) {
                                        newnow = 0;
                                    }
                                    if (newnow > myApp.getTrackMainGeneral()[inttrackno - 10].getNummax()) {
                                        newnow = myApp.getTrackMainGeneral()[inttrackno - 10].getNummax();
                                    }
                                    myApp.getTrackMainGeneral()[inttrackno - 10].setNumnow(newnow);
                                    if(newnow==0) {
                                        db.execSQL("update trackmaingeneral set numnow=0,batchinfo='',endday='' where id=" + inttrackno);
                                        myApp.getTrackMainGeneral()[inttrackno - 10].setBatchinfo(newnow == 0 ? "" : dataAA[1]);
                                        myApp.getTrackMainGeneral()[inttrackno - 10].setEndday(newnow == 0 ? "" : dataAA[2]);
                                    } else {
                                        db.execSQL("update trackmaingeneral set numnow=" + newnow + " where id=" + inttrackno);
                                    }

                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                    db.close();
                    isdbaccessing = false;

                    // 启动心跳
                    heart_secondcount = 175;// 5秒后，发送心跳(完整的)
                    sentSetpara = false;

                    // 刷新屏幕
                    // 让主页面进行商品显示的刷新
                    iView.HaveErrorFreshDisp();
                }
            }
            // 修改配置文件
            // 保存到配置文件
            SharedPreferences sp =
                    myApp.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();//获取编辑器
            String nowtime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(System.currentTimeMillis());
            editor.putString("stockchecktime", nowtime);
            editor.apply();//提交修改
            myApp.setStockchecktime(nowtime);
        }
    }

    public void supplyresultmac(String supplycode,long tranplanid,String supplyresult_data){
        // 判断货道方案有无变更
        long nowmactranplanid = myApp.getTrackplanid();
        long server_planid = 0l;
        String server_planname = "";

        ArrayList<HashMap<String,Object>> arrayList = new ArrayList<>();
        if(tranplanid != nowmactranplanid){
            LogUtils.i("货道方案ID，有变更，那么要进行更新");
            long timestamp = System.currentTimeMillis();
            String str = "macid="+ myApp.getMainMacID() +
                    "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
            String md5 = MD5.GetMD5Code(str);

            String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/trackplaninfo",
                    "macid="+myApp.getMainMacID() +
                            "&timestamp="+timestamp + "&md5=" + md5);

            if(rtnstr.length() > 0){
                try {
                    JSONObject soapJson = new JSONObject(rtnstr);
                    int code = soapJson.getInt("code");
                    if(code == 0) {
                        server_planid = soapJson.getLong("planid");
                        server_planname = soapJson.getString("planname");

                        if(tranplanid == server_planid) {
                            JSONArray goodpricearray = soapJson.getJSONArray("goodprice");
                            for (int i = 0; i < goodpricearray.length(); i++) {
                                JSONObject member = goodpricearray.getJSONObject(i);
                                String goodscode = member.getString("code");
                                int index = member.getInt("index");
                                String goodsprice = member.getString("price");

                                HashMap<String, Object> hm = new HashMap<>();
                                hm.put("code", goodscode);
                                hm.put("price", goodsprice.replace(" ", ""));
                                hm.put("index", index);
                                arrayList.add(hm);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }

        String sb = ",";
        // 获取所有的商品code
        for(int i=0;i<arrayList.size();i++){
            HashMap<String,Object> hmarray = arrayList.get(i);
            String code = (String)hmarray.get("code");
            if(sb.indexOf(","+code+",") < 0){
                // 说明没有找到
                sb = sb + code + ",";
            }
        }
        if(sb.length() == 1) sb = "";
        else sb = sb.substring(1,sb.length()-1);

        if(sb.length() > 0){
            String[] goodscodearray = sb.split(",");
            // 有商品
            try {
                // 判断是否有文件夹存在
                File file = new File(Environment.getExternalStorageDirectory() + "/goodspng/");
                if(!file.exists()){
                    boolean irtn=  file.mkdirs();
                    LogUtils.i( "文件夹" + Environment.getExternalStorageDirectory() + "/goodspng/" + "的mkdir结果:" + irtn);
                }
                //boolean iserror = false;
                for(String goodscode:goodscodearray) {
                    String goodsname = "";
                    String pngurl = "";

                    LogUtils.i("获取商品的名称和下载地址：" + goodscode);

                    long timestamp = System.currentTimeMillis();
                    String str = "goodscode=" + goodscode + "&macid="+ myApp.getMainMacID() +
                            "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                    String md5 = MD5.GetMD5Code(str);

                    String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/goodsinfo",
                            "macid="+myApp.getMainMacID() + "&goodscode=" + goodscode +
                                    "&timestamp="+timestamp + "&md5=" + md5);
                    if(rtnstr.length() > 0){
                        try {
                            JSONObject soapJson = new JSONObject(rtnstr);
                            int code = soapJson.getInt("code");
                            // msg = soapJson.getString("msg");
                            if(code == 0) {
                                goodsname = soapJson.getString("goodsname");
                                pngurl = soapJson.getString("pngurl");
                                // 名称知道了，要设置
                                setgoodsname(goodscode,goodsname,arrayList);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    // 已经存在了,那么先删除,再下载图片
                    file = new File( Environment.getExternalStorageDirectory() + "/goodspng/" + Gmethod.getFileName(pngurl));
                    if (file.exists()) {
                        boolean deleteresult = file.delete();
                        LogUtils.i("文件" + Environment.getExternalStorageDirectory() + "/goodspng/" + Gmethod.getFileName(pngurl) + "的delete结果:" + deleteresult);
                    }

                    try {
                        LogUtils.i("URL=" + pngurl);
                        URL url = new URL(pngurl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        if (conn.getResponseCode() == 200) {

                            InputStream is = conn.getInputStream();
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            is.close();
                            fos.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }

                int dbwhile = 0;
                while (isdbaccessing && dbwhile < 400) {
                    dbwhile++;
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isdbaccessing = true;
                // 图片文件下载完成后，进行数据库的修改
                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                for(int i=0;i<arrayList.size();i++){
                    HashMap<String,Object> hmarray = arrayList.get(i);
                    String code = (String)hmarray.get("code");
                    int index = (Integer)hmarray.get("index");
                    String name = (String)hmarray.get("name");
                    String price = (String)hmarray.get("price");
                    //System.out.println("====="+price);
                    String[] pricea = price.split("_");
                    int paycash = Integer.parseInt(pricea[0]);
                    int payali = Integer.parseInt(pricea[1]);
                    int paywx = Integer.parseInt(pricea[2]);
                    int paymember = Integer.parseInt(pricea[3]);



                            if(!myApp.getTrackMainGeneral()[index - 10].getGoodscode().equals(code)) {
                                db.execSQL("update trackmaingeneral set numnow=0,goodscode='" + code + "',mingcheng='" + name + "',paycash=" + paycash +
                                        ",payali=" + payali + ",paywx=" + paywx + ",paymember=" + paymember + " where id=" + index);
                                myApp.getTrackMainGeneral()[index - 10].setNumnow(0);
                            } else {
                                db.execSQL("update trackmaingeneral set goodscode='" + code + "',mingcheng='" + name + "',paycash=" + paycash +
                                        ",payali=" + payali + ",paywx=" + paywx + ",paymember=" + paymember + " where id=" + index);
                            }
                            myApp.getTrackMainGeneral()[index - 10].setGoodscode(code);
                            myApp.getTrackMainGeneral()[index - 10].setGoodsname(name);
                            myApp.getTrackMainGeneral()[index - 10].setPaycash(paycash);
                            myApp.getTrackMainGeneral()[index - 10].setPayali(payali);
                            myApp.getTrackMainGeneral()[index - 10].setPaywx(paywx);
                            myApp.getTrackMainGeneral()[index - 10].setPaymember(paymember);


                }
                db.close();
                isdbaccessing = false;

            } catch (Exception e){
                e.printStackTrace();
            }
        }

        if(server_planid!=0 && server_planid != nowmactranplanid){
            SharedPreferences sp =
                    myApp.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();//获取编辑器

            editor.putLong("trackplanid", server_planid);
            editor.putString("trackplanname", server_planname);

            editor.apply();//提交修改
        }

        ArrayList<Map<String,String>> newList = new ArrayList<>();
        // 更新轨道的商品信息
        String[] dataA = supplyresult_data.split(";");
        for(int i=0;i<dataA.length;i++) {
            String[] single = dataA[i].split(",");
            String trackno = single[0];
            String goodscode = "";

            goodscode = myApp.getTrackMainGeneral()[Integer.parseInt(trackno) - 10].getGoodscode();

            int num = Integer.parseInt(single[3]);
            String batch = single[1];
            String endday = single[2];

            Gmethod.doHm(newList, trackno,
                    goodscode, "", batch, endday, num);
        }

        String data = "";
        for(int i=0;i<newList.size();i++){
            Map<String,String> hm = newList.get(i);
            data = data + hm.get("trackgoodscode") + "," +
                    hm.get("trackgoodsname") + "," +
                    hm.get("batch") + "," +
                    hm.get("endday") + "," +
                    hm.get("change") + "," +
                    hm.get("detail") + ";";
        }
        if(data.length() > 0){
            data = data.substring(0,data.length()-1);
        }

        String data_encode = "";
        try {
            data_encode = URLEncoder.encode(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        long timestamp = System.currentTimeMillis();
        String str = "data="+data_encode + "&macid="+myApp.getMainMacID()  +"&supplycode=" + supplycode + "&userid=" + myApp.getTempUserId() +
                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
        String md5 = MD5.GetMD5Code(str);

        String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendsupplyresult",
                "macid="+myApp.getMainMacID() + "&data="+data +"&supplycode=" + supplycode + "&userid=" + myApp.getTempUserId() +
                        "&timestamp="+timestamp + "&md5=" + md5);

        if(rtnstr.length()>0) {
            int code = 2;
            String msg = "解析返回值出错啦";
            try {
                JSONObject soapJson = new JSONObject(rtnstr);
                code = soapJson.getInt("code");
                msg = soapJson.getString("msg");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (code == 0) {

                int dbwhile = 0;
                while (isdbaccessing && dbwhile < 400) {dbwhile++;try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}}
                isdbaccessing = true;
                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                try {
                    for (int i = 0; i < dataA.length; i++) {
                        String[] dataAA = dataA[i].split(",");
                        if (dataAA.length == 4) {
                            // 收到数据了，那么进行处理

                                int inttrackno = Integer.parseInt(dataAA[0]);
                                int newnow = myApp.getTrackMainGeneral()[inttrackno - 10].getNumnow() + Integer.parseInt(dataAA[3]);

                                if (newnow < 0) {
                                    newnow = 0;
                                }
                                if (newnow > myApp.getTrackMainGeneral()[inttrackno - 10].getNummax()) {
                                    newnow = myApp.getTrackMainGeneral()[inttrackno - 10].getNummax();
                                }

                                db.execSQL("update trackmaingeneral set numnow=" + newnow
                                        + ",batchinfo='" + (newnow ==0 ? "" : dataAA[1]) + "',endday='" + (newnow ==0 ? "" : dataAA[2])
                                        + "' where id=" + inttrackno);

                                myApp.getTrackMainGeneral()[inttrackno - 10].setNumnow(newnow);
                                myApp.getTrackMainGeneral()[inttrackno - 10].setBatchinfo(newnow == 0 ? "" : dataAA[1]);
                                myApp.getTrackMainGeneral()[inttrackno - 10].setEndday(newnow == 0 ? "" : dataAA[2]);

                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                db.close();
                isdbaccessing = false;
            }
        }
    }

    // 货道商品(价格)变更			10/batch/20180203/11002;11///11003/100_200_200_201
    public void pushdata_track_goods_price(String pushdata){
        String[] array = pushdata.split(";");

        int dbwhile = 0;
        while (isdbaccessing && dbwhile < 400) {dbwhile++;try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}}
        isdbaccessing = true;
        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

        for(int i=0;i<array.length;i++) {
            String[] s = array[i].split("/");

            int track = Integer.parseInt(s[0]);
            String batchinfo = s[1];
            String endday = s[2];
            String goodscode = s[3];
            String pricestr = "";
            int price_cash = 0;
            int price_alipay = 0;
            int price_wx = 0;
            int price_member = 0;
            if(s.length == 5){
                pricestr = s[4];
                String[] p = pricestr.split("_");
                price_cash = Integer.parseInt(p[0]);
                price_alipay = Integer.parseInt(p[1]);
                price_wx = Integer.parseInt(p[2]);
                price_member = Integer.parseInt(p[3]);
            }

            // 取得这个商品名称
            HashMap<String,Integer> pricemap = new HashMap<>();
            String goodsname = getGoodsName(goodscode,pricemap);
            if(goodsname.length() > 0){
                // 说明本来就有的商品
                if(pricestr.length() == 0){

                        myApp.getTrackMainGeneral()[track-10].setGoodscode(goodscode);
                        myApp.getTrackMainGeneral()[track-10].setGoodsname(goodsname);
                        myApp.getTrackMainGeneral()[track-10].setPaycash(pricemap.get("cash"));
                        myApp.getTrackMainGeneral()[track-10].setPayali(pricemap.get("alipay"));
                        myApp.getTrackMainGeneral()[track-10].setPaywx(pricemap.get("wxpay"));
                        myApp.getTrackMainGeneral()[track-10].setPaymember(pricemap.get("member"));

                        db.execSQL("update trackmaingeneral set goodscode='" + goodscode
                                + "',batchinfo='" + batchinfo + "',endday='" + endday + "',mingcheng='"+goodsname+"',paycash="+pricemap.get("cash")
                                + ",payali="+pricemap.get("alipay")+",paywx="+pricemap.get("wxpay")+",paymember="+pricemap.get("member")
                                + " where id=" + track);

                } else {

                        myApp.getTrackMainGeneral()[track-10].setGoodscode(goodscode);
                        myApp.getTrackMainGeneral()[track-10].setGoodsname(goodsname);
                        myApp.getTrackMainGeneral()[track-10].setPaycash(price_cash);
                        myApp.getTrackMainGeneral()[track-10].setPayali(price_alipay);
                        myApp.getTrackMainGeneral()[track-10].setPaywx(price_wx);
                        myApp.getTrackMainGeneral()[track-10].setPaymember(price_member);

                        db.execSQL("update trackmaingeneral set goodscode='" + goodscode
                                + "',batchinfo='" + batchinfo + "',endday='" + endday + "',mingcheng='"+goodsname+"',paycash="+price_cash
                                + ",payali="+price_alipay+",paywx="+price_wx+",paymember="+price_member
                                + " where id=" + track);


                    // 所有这个商品的价格都要改（不含batch，endday）
                    for(int k=0;k<70;k++){
                        if(myApp.getTrackMainGeneral()[k].getGoodscode().equals(goodscode)){
                            myApp.getTrackMainGeneral()[k].setPaycash(price_cash);
                            myApp.getTrackMainGeneral()[k].setPaywx(price_wx);
                            myApp.getTrackMainGeneral()[k].setPayali(price_alipay);
                            myApp.getTrackMainGeneral()[k].setPaymember(price_member);
                        }

                    }
                    db.execSQL("update trackmaingeneral set paycash="+price_cash
                            + ",payali="+price_alipay+",paywx="+price_wx+",paymember="+price_member
                            + " where goodscode='" + goodscode+"'");

                }

            } else {
                // 没有这个商品的话，要先下载这个商品图片，同时取得这个商品名称
                //String goodsname = "";
                String pngurl = "";

                LogUtils.i("获取商品的名称和下载地址：" + goodscode);

                long timestamp = System.currentTimeMillis();
                String str = "goodscode=" + goodscode + "&macid="+ myApp.getMainMacID() + "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                String md5 = MD5.GetMD5Code(str);

                String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/goodsinfo",
                        "macid="+myApp.getMainMacID() + "&goodscode=" + goodscode + "&timestamp="+timestamp + "&md5=" + md5);
                if(rtnstr.length() > 0){
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        int code = soapJson.getInt("code");
                        // msg = soapJson.getString("msg");
                        if(code == 0) {
                            goodsname = soapJson.getString("goodsname");
                            pngurl = soapJson.getString("pngurl");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                // 已经存在了,那么先删除,再下载图片
                File file = new File( Environment.getExternalStorageDirectory() + "/goodspng/" + Gmethod.getFileName(pngurl));
                if (file.exists()) {
                    boolean deleteresult = file.delete();
                    LogUtils.i("文件" + Environment.getExternalStorageDirectory() + "/goodspng/" + Gmethod.getFileName(pngurl) + "的delete结果:" + deleteresult);
                }

                try {
                    LogUtils.i("URL=" + pngurl);
                    URL url = new URL(pngurl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    if (conn.getResponseCode() == 200) {

                        InputStream is = conn.getInputStream();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        is.close();
                        fos.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // 这个轨道要改商品（含batch，endday，价格，商品名称）
                if(pricestr.length() == 0){
                    price_cash = 1000;
                    price_alipay = 1000;
                    price_wx = 1000;
                    price_member = 1000;
                }

                // 这个轨道要改商品（含batch，endday，价格）

                    myApp.getTrackMainGeneral()[track-10].setGoodscode(goodscode);
                    myApp.getTrackMainGeneral()[track-10].setGoodsname(goodsname);
                    myApp.getTrackMainGeneral()[track-10].setPaycash(price_cash);
                    myApp.getTrackMainGeneral()[track-10].setPayali(price_alipay);
                    myApp.getTrackMainGeneral()[track-10].setPaywx(price_wx);
                    myApp.getTrackMainGeneral()[track-10].setPaymember(price_member);

                    db.execSQL("update trackmaingeneral set goodscode='" + goodscode
                            + "',batchinfo='" + batchinfo + "',endday='" + endday + "',mingcheng='"+goodsname+"',paycash="+price_cash
                            + ",payali="+price_alipay+",paywx="+price_wx+",paymember="+price_member
                            + " where id=" + track);

            }
        }
        db.close();
        isdbaccessing = false;
    }

    private String getGoodsName(String goodscode,Map<String,Integer> price4){
        int[] mainTrack = {0,0,0,0,0,0,0};
        mainTrack[0] = myApp.getMainGeneralLevel1TrackCount();
        if(myApp.getMainGeneralLevelNum() == 7){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
            mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
            mainTrack[6] = myApp.getMainGeneralLevel7TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 6){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
            mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 5){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 4){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 3){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 2){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
        }

        // 主柜
        for(int i=1;i<=7;i++) {
            for(int j=0;j<mainTrack[i-1];j++) {
                if(myApp.getTrackMainGeneral()[i*10+j-10].getGoodscode().equals(goodscode)) {
                    price4.put("cash",myApp.getTrackMainGeneral()[i*10+j-10].getPaycash());
                    price4.put("alipay",myApp.getTrackMainGeneral()[i*10+j-10].getPayali());
                    price4.put("wxpay",myApp.getTrackMainGeneral()[i*10+j-10].getPaywx());
                    price4.put("member",myApp.getTrackMainGeneral()[i*10+j-10].getPaymember());
                    return myApp.getTrackMainGeneral()[i * 10 + j - 10].getGoodsname();
                }
            }
        }

        price4.put("cash",1000);
        price4.put("alipay",1000);
        price4.put("wxpay",1000);
        price4.put("member",1000);
        return "";
    }

    // 货道在库数变更			10/3;13/4;14/2
    public void pushdata_track_nownum(String pushdata){
        String[] array = pushdata.split(";");

        int dbwhile = 0;
        while (isdbaccessing && dbwhile < 400) {dbwhile++;try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}}
        isdbaccessing = true;
        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);


        for(int i=0;i<array.length;i++) {
            String[] s = array[i].split("/");

            int track = Integer.parseInt(s[0]);
            int nownum = Integer.parseInt(s[1]);

            if(nownum > myApp.getTrackMainGeneral()[track-10].getNummax()){
                nownum = myApp.getTrackMainGeneral()[track-10].getNummax();
            }
            myApp.getTrackMainGeneral()[track-10].setNumnow(nownum);

            db.execSQL("update trackmaingeneral set numnow=" + nownum + " where id=" + track);

        }
        db.close();
        isdbaccessing = false;
    }

    // 商品价格变更			11002/100_101_101_100;11003/150_1_1_1
    public void pushdata_goods_price(String pushdata){
        String[] array = pushdata.split(";");

        int dbwhile = 0;
        while (isdbaccessing && dbwhile < 400) {dbwhile++;try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}}
        isdbaccessing = true;
        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);


        for(int i=0;i<array.length;i++) {
            String[] s = array[i].split("/");

            String goodscode = s[0];
            String[] p = s[1].split("_");
            int price_cash = Integer.parseInt(p[0]);
            int price_alipay = Integer.parseInt(p[1]);
            int price_wx = Integer.parseInt(p[2]);
            int price_member = Integer.parseInt(p[3]);

            // 所有这个商品的价格都要改
            for(int k=0;k<70;k++){
                if(myApp.getTrackMainGeneral()[k].getGoodscode().equals(goodscode)){
                    myApp.getTrackMainGeneral()[k].setPaycash(price_cash);
                    myApp.getTrackMainGeneral()[k].setPaywx(price_wx);
                    myApp.getTrackMainGeneral()[k].setPayali(price_alipay);
                    myApp.getTrackMainGeneral()[k].setPaymember(price_member);
                }
            }
            db.execSQL("update trackmaingeneral set paycash="+price_cash
                    + ",payali="+price_alipay+",paywx="+price_wx+",paymember="+price_member
                    + " where goodscode='" + goodscode+"'");

        }
        db.close();
        isdbaccessing = false;
    }

    // 货道停售可售			10/0;11/0;13/1;20/0;49/1
    public void pushdata_track_cansale(String pushdata){
        String[] array = pushdata.split(";");

        int dbwhile = 0;
        while (isdbaccessing && dbwhile < 400) {dbwhile++;try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}}
        isdbaccessing = true;
        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);


        for(int i=0;i<array.length;i++) {
            String[] s = array[i].split("/");

            int track = Integer.parseInt(s[0]);
            int cansale = Integer.parseInt(s[1]);

            myApp.getTrackMainGeneral()[track-10].setCansale(cansale);
            db.execSQL("update trackmaingeneral set cansale=" + cansale + " where id=" + track);

        }
        db.close();
        isdbaccessing = false;
    }

    // 整机停售可售			0或者1
    public void pushdata_cansale(String pushdata){
        int[] mainTrack = {0,0,0,0,0,0,0};
        mainTrack[0] = myApp.getMainGeneralLevel1TrackCount();
        if(myApp.getMainGeneralLevelNum() == 7){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
            mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
            mainTrack[6] = myApp.getMainGeneralLevel7TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 6){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
            mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 5){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 4){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 3){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 2){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
        }


        int dbwhile = 0;
        while (isdbaccessing && dbwhile < 400) {dbwhile++;try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}}
        isdbaccessing = true;
        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

        int cansale = 0;
        if(pushdata.equals("1")) cansale = 1;

        // 主柜
        for(int i=1;i<=7;i++) {
            for(int j=0;j<mainTrack[i-1];j++) {
                myApp.getTrackMainGeneral()[i*10+j-10].setCansale(cansale);
                db.execSQL("update trackmaingeneral set cansale=" + cansale + " where id=" + (i*10+j));
            }
        }


        db.close();
        isdbaccessing = false;
    }

    // 对应的商品名称
    private void setgoodsname(String goodscode,String goodsname,ArrayList<HashMap<String,Object>> arrayList){
        for(int i=0;i<arrayList.size();i++){
            HashMap<String,Object> hmarray = arrayList.get(i);
            String code = (String)hmarray.get("code");
            if(code.equals(goodscode)){
                hmarray.put("name",goodsname);
            }
        }
    }
}

