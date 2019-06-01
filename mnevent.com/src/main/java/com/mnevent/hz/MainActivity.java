package com.mnevent.hz;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.igexin.sdk.PushManager;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.mnevent.hz.Activity.MenuActivity;
import com.mnevent.hz.Adapter.CarouselPagerAdapter;
import com.mnevent.hz.Adapter.MainAdapter;
import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.Service.HeartbeatService;
import com.mnevent.hz.Utils.DateUtils;
import com.mnevent.hz.Utils.GalleryTransformer;
import com.mnevent.hz.Utils.Gmethod;
import com.mnevent.hz.Utils.HttpUtils;
import com.mnevent.hz.Utils.Log;
import com.mnevent.hz.Utils.LogUtils;
import com.mnevent.hz.Utils.MD5;
import com.mnevent.hz.Utils.MyHttp;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.Utils.ToastUtil;
import com.mnevent.hz.View.CarouselViewPager;
import com.mnevent.hz.View.SimerTextview;
import com.mnevent.hz.bean.MNbean;
import com.mnevent.hz.litemolder.PathwayMolder;

import org.litepal.LitePal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android_serialport_api.ComQRIC;
import android_serialport_api.ComTrackMagex;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

   /* @BindView(R.id.recy)
    RecyclerView recy;
    @BindView(R.id.text)
    SimerTextview text;*/

    String serialNumber, password;
    @BindView(R.id.banner)
    CarouselViewPager banner;
    // 个推
    private String clientID = "";

    // 取得状态的值 中亚驱动板
    private String[] statusdetail = new String[]{"99", "99", "99", "加热管1", "加热管2", "加热管3", "加热管4", "1号门开关", "2号门开关", "其它故障（有的话）", "串口发送相关错误（有的话）"};

    private int[] number;

    List<PathwayMolder> pathlist = new ArrayList<>();


    private List<PathwayMolder> pathwayall;
    private List<PathwayMolder> pathwayalles = new ArrayList<>();


    ShipmentReceiver shipmentReceiver;


    ModeBroadCastReceiver modeswitch;


    String classname = "";


    List<PathwayMolder> pathlistes = new ArrayList<>();

    List<PathwayMolder> pathlistess = new ArrayList<>();

    PathwayMolder molder;

    MNbean mNbean;

    ComQRIC comQRIC;

    ImagePagerAdapter imageadapter;

    private int[] baner = new int[]{R.mipmap.mnx};

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                 //   text.setText("正在等待您的购买...");
                    break;

                case 2:
                  //  text.setText("正在等待您的购买...");
                    break;

                case 3:
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                    // super.onBackPressed();
                    finish();
                    break;
                case 4:
                    ToastUtil.showToast(MainActivity.this, "二维码错误！");
                    break;
                case 5:
                    if (mNbean.getResult().isExist() == true && mNbean.getResult().isIsReceive() == false) {
                        Shipment();
                    } else if (mNbean.getResult().isExist() == false && mNbean.getResult().isIsReceive() == false) {
                        ToastUtil.showToast(MainActivity.this, "该订单不存在！");
                    } else if (mNbean.getResult().isExist() == true && mNbean.getResult().isIsReceive() == true) {
                        ToastUtil.showToast(MainActivity.this, "该二维码已被使用！");
                    }
                    //comQRIC.openSerialPort();
                    break;
                case 6:
                  //  text.setText("出货成功，请取走您的商品...");
                    handler.sendEmptyMessageDelayed(1, 60000);
                    initDate();
                    break;

                case 7:
                   // text.setText("出货失败，请联系客服...");
                    handler.sendEmptyMessageDelayed(1, 60000);
                    initDate();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //轮播图
        initBanner();
        if (comQRIC == null) {
            comQRIC = new ComQRIC("/dev/ttyO3");
            comQRIC.openSerialPort();
        }

        classname = getClass().getSimpleName();
        serialNumber = PrefheUtils.getString(MyApp.mApplication, "serialNumber", "");
        password = PrefheUtils.getString(MyApp.mApplication, "password", "");
        initDate();
        initView();
        recepition();
        //心跳帧服务
        starttimeserver();
    }

    //轮播
    private void initBanner() {

            imageadapter = new ImagePagerAdapter(MainActivity.this, banner);
            banner.setOffscreenPageLimit(1);
            banner.setAdapter(imageadapter);
            // 设置轮播时间
            banner.setTimeOut(6);
            // 设置3d效果
        //    banner.setPageTransformer(true, new GalleryTransformer());
            // 设置已经有数据了，可以进行轮播
            banner.setHasData(true);
            // 开启轮播
            banner.startTimer();

    }

    /**
     * banner图适配器
     */
    public class ImagePagerAdapter extends CarouselPagerAdapter<CarouselViewPager> {



        public ImagePagerAdapter(MainActivity context, CarouselViewPager viewPager) {
            super(viewPager);

        }


        @Override
        public Object instantiateRealItem(ViewGroup container, final int position) {
            View localView = View.inflate(MainActivity.this, R.layout.item_barner, null);
            ImageView banners = localView.findViewById(R.id.items_banner);
            Glide.with(MainActivity.this).load(baner[position]).into(banners);
            container.addView(localView);
            return localView;
        }

        @Override
        public int getRealDataCount() {

            return baner.length;
        }
    }

    private void starttimeserver() {
        /*boolean serviceRunning = isServiceRunning(MyApp.mApplication, "com.mnevent.hz.Service.HeartbeatService");
        Log.d("zlc", "是否开启" + serviceRunning);
        if (serviceRunning == false) {
            Intent intent = new Intent(MainActivity.this, HeartbeatService.class);
            startService(intent);
        }*/
    }

    private void recepition() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("shipment");
        shipmentReceiver = new ShipmentReceiver();
        registerReceiver(shipmentReceiver, filter);

    }

    class ShipmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //comQRIC.closeSerialPort();
            Bundle extras = intent.getExtras();
            String type = extras.getString("type");
            Log.d("zlc", "接受到的数据：" + type);

            LogUtils.d(classname + "::获取到的二维码信息：" + type);
            if (TextUtils.isEmpty(type)) {
                handler.sendEmptyMessage(4);
                return;
            }
            OkGo.<String>post("https://dev.flyh5.cn/mengniu/checkOrderNo").tag(MainActivity.this)
                    .params("orderNo", type)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            String json = response.body().toString();
                            analysis(json);
                        }
                    });
           /* switch (type){

                case 1:
                    text.setText("正在出货，请稍等...");
                    break;
                case 2:
                    text.setText("出货成功，请取走您的商品...");
                    handler.sendEmptyMessageDelayed(1,60000);
                    initDate();
                    break;

                case 3:
                    text.setText("出货失败，请联系客服...");
                    handler.sendEmptyMessageDelayed(1,60000);
                    initDate();
                    break;
            }*/
        }
    }

    private void analysis(String json) {
        Log.d("ceshi", json);
        Gson gson = new Gson();
        mNbean = gson.fromJson(json, MNbean.class);
        handler.sendEmptyMessage(5);
    }


    private void Shipment() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                pathlist.clear();
                pathlistes.clear();
                pathlistess.clear();
                pathlist = LitePal.findAll(PathwayMolder.class);
                // Log.d("zlc","pathlist.get(i)"+goods+","+pathlist.get(0).getGoodscode()+","+pathlist.get(0).getInventory()+""+pathlist.get(0).getMergecode()+","+pathlist.get(0).getErrorcode());
                // Log.d("zlc","pathlist.get(i)"+pathlist.get(2).getGoodscode()+","+pathlist.get(2).getInventory()+""+pathlist.get(2).getMergecode()+","+pathlist.get(2).getErrorcode());
                for (int i = 0; i < pathlist.size(); i++) {
                    if (Integer.parseInt(pathlist.get(i).getInventory()) > 0 && pathlist.get(i).getMergecode().equals("01") && pathlist.get(i).getErrorcode().equals("1")) {
                        pathlistes.add(pathlist.get(i));
                        Log.d("zlc", "pathlist.get(i)" + pathlist.get(i).getCode());
                    }
                }
                int i = (int) (0 + Math.random() * pathlistess.size());
                LogUtils.d(classname + "获取的随机数为：" + i);
                pathlistess.add(pathlistes.get(i));
                LogUtils.d(classname + "出货的轨道为：" + pathlistes.get(0).getCode());
                if (pathlistess.size() == 0) {
                    return;
                }
                ComTrackMagex comTrack = new ComTrackMagex("");
                comTrack.openSerialPort();
                String code = pathlistess.get(0).getCode();
                LogUtils.d("MainActivity:::zlc::code:" + code);
                String rtn[] = comTrack.vend_out_ind(
                        Integer.parseInt(code.substring(0, 1)), Integer.parseInt(code.substring(1, 2)),

                        Integer.parseInt(pathlistess.get(0).getPrice()), "", "", "");
                LogUtils.d("MainActivity:::zlc::aplayssss:" + rtn[0]);
                comTrack.closeSerialPort();
                if (rtn[0].length() == 0) {
                    LogUtils.d("MainActivity:::zlc::aplayssss:" + "成功");
                    molder = new PathwayMolder();
                    molder.setTime(DateUtils.getCurrentTime_Today() + "");
                    molder.setInventory(Integer.parseInt(pathlistess.get(0).getInventory()) - 1 + "");
                    molder.updateAll("code = ?", code);
                    handler.sendEmptyMessage(6);
                } else {
                    LogUtils.d("MainActivity:::zlc::aplayssss:" + "失败");
                    molder = new PathwayMolder();
                    molder.setErrorcode("0");
                    molder.setErrortime(DateUtils.getCurrentTime_Today() + "");
                    molder.updateAll("code = ?", code);
                    handler.sendEmptyMessage(7);
                }
                initDate();
            }
        }).start();


    }


    private void initDate() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(300000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    pathlist.clear();

                    pathlist = LitePal.findAll(PathwayMolder.class);
                    number = new int[]{0, 0, 0, 0, 0, 0, 0};
               /* for (int i = 0;i<pathlist.size();i++){
                    if(pathlist.get(i).getMergecode().equals("01") && pathlist.get(i).getErrorcode().equals("1")){
                        pathlistes.add(pathlist.get(i));
                    }
                }*/
                    for (int i = 0; i < pathlist.size(); i++) {
                        if (pathlist.get(i).getCode().substring(0, 1).equals("1")) {
                            number[0] = number[0] + 1;
                        } else if (pathlist.get(i).getCode().substring(0, 1).equals("2")) {
                            number[1] = number[1] + 1;
                        } else if (pathlist.get(i).getCode().substring(0, 1).equals("3")) {
                            number[2] = number[2] + 1;
                        } else if (pathlist.get(i).getCode().substring(0, 1).equals("4")) {
                            number[3] = number[3] + 1;
                        } else if (pathlist.get(i).getCode().substring(0, 1).equals("5")) {
                            number[4] = number[4] + 1;
                        } else if (pathlist.get(i).getCode().substring(0, 1).equals("6")) {
                            number[5] = number[5] + 1;
                        } else if (pathlist.get(i).getCode().substring(0, 1).equals("7")) {
                            number[6] = number[6] + 1;
                        }
                    }

                    ArrayList<HashMap<String, Object>> al = new ArrayList<>();


                    //温度
                    String temperature = PrefheUtils.getString(MyApp.mApplication, "temperature", "");
                    String bills = "nolink";
                    String coins = "nolink";
                    String mainError_tmp = "";
                    String signale = dispNetStatus();
                    String signale_encode = "";

                    try {
                        signale_encode = URLEncoder.encode(signale, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    String mdsrc = "adplanid=" + 0
                            + "&bills=" + bills
                            + "&coins=" + coins
                            + "&driverver=" + "1.0.0"
                            + "&macerror=" + mainError_tmp
                            + "&macid=" + serialNumber
                            + "&macidsub=" + ""
                            + "&macstop=" + "0"
                            + "&mactime=" + ((new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()))
                            + "&mainver=" + "1.0.0"
                            + "&pushid=" + getClientID()
                            + "&signalabc=" + signale;


                    mdsrc = mdsrc + "&temp1=" + (statusdetail[0].equals("温度1") ? "-" : temperature)
                            + "&temp2=" + (statusdetail[2].equals("温度3") ? "-" : temperature);
                    mdsrc = mdsrc + "&track=" + number[0] + "," + number[1] + "," + number[2] + "," + number[3] + "," + number[4] + "," + number[5] + "," + number[6];
                    for (int i = 0; i < pathlist.size(); i++) {
                        mdsrc = mdsrc + "&track" + pathlist.get(i).getCode() + "=" + pathlist.get(i).getGoodscode()
                                + "," + pathlist.get(i).getNummax() + "," + pathlist.get(i).getInventory() + "," + (pathlist.get(i).getErrorcode().equals("1") ? "" : pathlist.get(i).getErrorcode() + "_" + pathlist.get(i).getErrortime()) + "," +
                                1 + "," +
                                "" + "," +
                                "" + "," + pathlist.get(i).getPrice() + "_" + pathlist.get(i).getPrice() + "_" + pathlist.get(i).getPrice() + "_" + pathlist.get(i).getPrice();


                        Gmethod.setNowStock(al, pathlist.get(i).getGoodscode(), Integer.parseInt(pathlist.get(i).getInventory()), Integer.parseInt(pathlist.get(i).getPrice()));
                    }
                    mdsrc = mdsrc
                            + "&trackplanid=0"
                            + "&upsoftver=" + Gmethod.getAppVersion(MyApp.mApplication);
                    long time = System.currentTimeMillis();
                    String str = mdsrc.replace("&signalabc=" + signale, "&signalabc=" + signale_encode) +
                            "&timestamp=" + time + "&accesskey=" + password;
                    String md5 = MD5.GetMD5Code(str);


                    String ymds = DateUtils.getDateTimeByMillisecond(time + "", "yyyyMMddHHmmss");

                    String dr = "-";
                    if (statusdetail[4].contains("开着")) dr = "open";
                    else if (statusdetail[4].contains("关着")) dr = "close";

                    LogUtils.d("BasicparameterActivity:::zlc::mdsrc" + mdsrc);
                    String rtnstr = (new MyHttp()).post(HttpUtils.getMcrealstatus,
                            mdsrc + "&timestamp=" + time + "&md5=" + md5 + "&nowstock=" + Gmethod.getNowStockStr(al) + "&door=" + dr + "&pkg=hz");

                    LogUtils.d("心跳帧接口访问：rtnstr = " + rtnstr);

                }
            }
        }).start();

        pathwayall = LitePal.findAll(PathwayMolder.class);

        pathwayalles.clear();
        for (int i = 0; i < pathwayall.size(); i++) {
            if (pathwayall.get(i).getPds().equals("1") && Integer.parseInt(pathwayall.get(i).getInventory()) > 0 && pathwayall.get(i).getUp().equals("1") && pathwayall.get(i).getErrorcode().equals("1")
                    && pathwayall.get(i).getMergecode().equals("01")) {
                LogUtils.d("MainActivity::zlc::Proalle.get(i).getUp():" + pathwayall.get(i).getGoodscode() + i);
                pathwayalles.add(pathwayall.get(i));
            }
        }


        for (int i = 0; i < pathwayalles.size() - 1; i++) {
            for (int j = pathwayalles.size() - 1; j > i; j--)
                if (pathwayalles.get(i).getGoodscode().equals(pathwayalles.get(j).getGoodscode())) {
                    LogUtils.d("去重:" + pathwayalles.get(j).getCode());
                    pathwayalles.remove(j);
                }
        }


    }

    private void initView() {

      /*  GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 4);
        MainAdapter adapter = new MainAdapter(MainActivity.this, pathwayalles);
        recy.setLayoutManager(manager);
        recy.setAdapter(adapter);*/
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //拦截返回键
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            //判断触摸UP事件才会进行返回事件处理
            if (event.getAction() == KeyEvent.ACTION_UP) {
                onBackPressed();
            }
            //只要是返回事件，直接返回true，表示消费掉
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    //设置鼠标右键直接进入管理界面
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        startActivity(intent);
        // super.onBackPressed();
        finish();
    }

    /**
     * 校验某个服务是否还存在
     */
    public boolean isServiceRunning(Context context, String serviceName) {
        // 校验服务是否还存在
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo info : services) {
            // 得到所有正在运行的服务的名称
            String name = info.service.getClassName();
            if (serviceName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //生成广播处理-----------------------------
        modeswitch = new ModeBroadCastReceiver();
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter("android.intent.action.ENG_MODE_SWITCH");
        //注册
        registerReceiver(modeswitch, intentFilter);
        //生成广播处理-----------------------------
        LogUtils.d(classname + "接收mode广播");
    }


    //内部类
    // 定义一个mode开关的接收器
    class ModeBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.ENG_MODE_SWITCH")) {
                LogUtils.d(classname + "收到mode开关的广播了！");
                handler.sendEmptyMessage(3);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        comQRIC.closeSerialPort();

        unregisterReceiver(shipmentReceiver);
        unregisterReceiver(modeswitch);
    }

    // 获取个推的客户端ID
    public String getClientID() {
        if (clientID.length() > 0) return clientID;
        String tmp_clientid = PushManager.getInstance().getClientid(this.getApplicationContext());
        if (tmp_clientid == null || tmp_clientid.length() == 0) {
            return "";
        } else {
            clientID = tmp_clientid;
            return clientID;
        }
    }

    public String dispNetStatus() {
        // 获取联网方式,显示出来
        //TextView netstatus = findViewById(R.id.netstatus);
        String netstatus_txt = "";
        ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);

        //检查网络连接
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info == null) {
            netstatus_txt = "上网: 未知";
        } else {
            int netType = info.getType();
            int netSubtype = info.getSubtype();

            //LogUtils.i("netType="+netType + ";netSubtype="+netSubtype);

            if (netType == ConnectivityManager.TYPE_ETHERNET) {
                netstatus_txt = "上网: 网线";
            } else if (netType == ConnectivityManager.TYPE_WIFI) {  //WIFI
                netstatus_txt = "上网: 无线";
            } else if (netType == ConnectivityManager.TYPE_MOBILE) {// && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS && !mTelephony.isNetworkRoaming()) {   //MOBILE
                netstatus_txt = "4G";
            } else {
                netstatus_txt = "上网: 无";
            }
        }


        if (netstatus_txt.equals("上网: 无") || netstatus_txt.equals("上网: 未知")) {
            return "0";
        } else if (netstatus_txt.equals("上网: 网线")) {
            return "网线";
        } else if (netstatus_txt.equals("上网: 无线")) {
            return "无线";
        } else {
            int dbmlevel = getMobileDbmlevel();


            return "" + dbmlevel;
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
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (dbm >= -75) {
            return 5;
        } else if (dbm >= -85) {
            //59-79
            return 4;
        } else if (dbm >= -95) {
            //39-59
            return 3;
        } else if (dbm >= -100) {
            //19-39
            return 2;
        } else if (dbm >= -105) {
            //19-39
            return 1;
        } else {
            //0-19
            return 0;
        }
    }
}
