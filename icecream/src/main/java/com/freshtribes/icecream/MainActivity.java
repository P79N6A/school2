package com.freshtribes.icecream;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.bean.GoodsBean;
import com.freshtribes.icecream.presenter.MainPresenter;
import com.freshtribes.icecream.service.AudioService;
import com.freshtribes.icecream.util.BitmapUtil;
import com.freshtribes.icecream.util.Create2DCodeUtil;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.MD5;
import com.freshtribes.icecream.util.MyExceptionHandler;
import com.freshtribes.icecream.util.MyHttp;
import com.freshtribes.icecream.util.MyPhoneStateListener;
import com.freshtribes.icecream.util.ProgressDialog;
import com.freshtribes.icecream.view.IMainView;
import com.igexin.sdk.PushManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainActivity extends Activity implements EventListener,IMainView,View.OnClickListener {


    private ThreadGroup tg = new ThreadGroup("MainActivityThreadGroup");
    private SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    private static final int REQUEST_FACE_LOGIN = 0x10;

    private static final int PAYPAGE_COUNTDOWN = 120; // 支付页面的倒计时

    // 推送过来的数据,放到此处
    public static ConcurrentLinkedQueue<String> queuealipay = new ConcurrentLinkedQueue<String>();
    public static ConcurrentLinkedQueue<String> queueweixin = new ConcurrentLinkedQueue<String>();
    public static ConcurrentLinkedQueue<String> queueumspay = new ConcurrentLinkedQueue<String>();
    public static ConcurrentLinkedQueue<String> queuetraninmac = new ConcurrentLinkedQueue<String>();
    public static ConcurrentLinkedQueue<String> queuetranoutmac = new ConcurrentLinkedQueue<String>();
    public static ConcurrentLinkedQueue<String> queuestockoutmac = new ConcurrentLinkedQueue<String>();
    public static ConcurrentLinkedQueue<String> queuecheckstockmac = new ConcurrentLinkedQueue<String>();
    public static ConcurrentLinkedQueue<String> queuesupplyresultmac = new ConcurrentLinkedQueue<String>();
    public static ConcurrentLinkedQueue<String> queuepush = new ConcurrentLinkedQueue<String>();

    private String AlipayCheckStr = "";
    private String WxpayCheckStr = "";
    private String UmspayCheckStr = "";

    private static final int Toast_Display = 0x100;

    private MainPresenter lp;
    private static final int dispHandler_Disp_GoodsImg_First = 0x105;
    private static final int dispHandler_Disp_GoodsImg_Refresh = 0x106;
    private static final int dispHandler_Disp_Temp = 0x107;

    // 底部的提示语
    private TextView tvhintinfo;

    // 要显示的商品图片及所在货道的列表，是一个页面变量
    private ArrayList<GoodsBean> goodslist = new ArrayList<>();
    private ArrayList<Bitmap> goodsbmplist = new ArrayList<>();

    private int willouttrack = 0;

    private static final int dispHandler_OutTrack_End = 0x201;
    private static final int dispHandler_OutTrack_NG3 = 0x202;
    private static final int dispHandler_OutTrack_NG = 0x203;

    // 商品图片
    private ImageView[] goodsupdown_btn = new ImageView[9];
    private ImageView[] goodsimagedisp = new ImageView[9];  // 商品图片
    private TextView[] goodsname = new TextView[9];
    private TextView[] goodsprice = new TextView[9];

    // 图片变灰
    ColorMatrix matrix0;
    private ColorMatrixColorFilter filter0;
    // 图片原图
    ColorMatrix matrix1;
    private ColorMatrixColorFilter filter1;
    // 总共有几页
    private int pagetotal = 0;
    private int pagenow = 0; // 0表示初值，1表示第一页


    // 支付区域
    //----------------------------------------------------------------------------------------------
    private RelativeLayout payrl;
    private ImageView goodsimage_sale;   // 商品图片
    private TextView payrl_goodsname;
    private TextView payrl_cashprice;
    private TextView payrl_memberprice;

    private String payinfo = "";

    private boolean isAlreadyPayOrCancel;  //true:已经支付成功了, 或者 放弃了

    protected static final int dispHandler_alipaycode_readyng_nonet = 0x450;
    protected static final int dispHandler_alipaycode_readyok = 0x452;
    protected static final int dispHandler_alipaycode_readcount = 0x453;
    protected static final int dispHandler_alipaycode_payok = 0x454;
    private TextView alipaycodetishi;
    public ImageView qrcodealipay;
    private Bitmap qrBitMapalipay;
    protected static final int dispHandler_umspaycode_readyng_nonet = 0x455;
    protected static final int dispHandler_umspaycode_readyok = 0x456;
    protected static final int dispHandler_umspaycode_readcount = 0x457;
    protected static final int dispHandler_umspaycode_payok = 0x458;
    private String queryzxalipayqueryurl = "";

    protected static final int dispHandler_weixin_readyng_nonet = 0x460;   // Handle的消息标志：weixinReady联网失败
    protected static final int dispHandler_weixin_readyok = 0x462;   // Handle的消息标志：weixinReady联网成功了，显示二维码
    protected static final int dispHandler_weixin_readcount = 0x463; // Handle的消息标志：weixinReady联网轮询用户支付结果
    protected static final int dispHandler_weixin_payok = 0x464;   // Handle的消息标志：weixin支付成功了
    private TextView weixintishi;
    public ImageView qrcodeweixin;
    private Bitmap qrBitMapweixin;
    private String queryzxwxpayqueryurl = "";

    protected static final int dispHandler_qric_tishi = 0x470;
    protected static final int dispHandler_qric_checkok = 0x471;
    private TextView qrictishi;

    //private boolean isDispPayRLing = false;  // 正在做显示处理，不接收其它的屏幕点击，但是VMC过来的选择是要相应的，等这个变量为false时
    private boolean isDispPayRL = false;
    private int paycountdown = PAYPAGE_COUNTDOWN;
    private TextView paycountdowntishi;
    protected static final int dispHandler_paying_count = 0x480;
    protected static final int dispHandler_Close_PayRL = 0x481;

    protected static final int dispHandler_facepay_moneyok = 0x490;
    protected static final int dispHandler_facepay_moneyng = 0x491;
    private String facepay_uid = "0";
    private String facepay_mobile = "";
    private String facepay_score = "";

    private Button closePayRLbtn;

    private boolean timethreadisrunning = false;
    private boolean qricthreadisrunning = false;
    private boolean alipaythreadisrunning = false;
    private boolean weixinthreadisrunning = false;
    private boolean umspaythreadisrunning = false;

    // 是否正在出货中
    private boolean isOuting = false;
    // 支付宝和微信的等待推送的线程
    private boolean isAlipayWxpayWaitingGetuiRunning = false;
    // 启动presenter的心跳
    private boolean isSendHeart = false;
    //----------------------------------------------------------------------------------------------
    private ImageView leftpageicon;
    private ImageView rightpageicon;
    private ImageView facepayicon;
    private ImageView facelogin;

    // 提示对话框
    private Dialog mProgressDialog;

    // 个推
    private String clientID = "";

    //TelephonyManager类的对象
    private TelephonyManager Tel;

    //MyPhoneStateListener类的对象，即设置一个监听器对象
    private MyPhoneStateListener MyListener;

    protected static final int dispHandler_Disp_NetStatus = 0x530;

    //mode按键-------------------
    protected static final int Goto_Menu = 0x540; // Handle的消息标志：跳转到维护页面
    protected static final int Goto_Loading = 0x541;
    // 广播
    private ModeBroadCastReceiver modeswitch;
    //mode按键-------------------


    // 百度语音------------------------------------
    // UI的Handler
    Handler mHandler = new Handler(Looper.getMainLooper());
    // 唤醒事件处理器
    private EventManager wp;
    // 识别事件处理器
    private EventManager asr;
    /**
     * 0: 方案1， 唤醒词说完后，直接接句子，中间没有停顿。
     * >0 : 方案2： 唤醒词说完后，中间有停顿，然后接句子。推荐4个字 1500ms
     * <p>
     * backTrackInMs 最大 15000，即15s
     */
    private int backTrackInMs = 1500;

    private static final int Audio_DispHandler_DispCount = 1000;
    private static final int Audio_DispHandler_UnDispCount = 1001;
    private static final int Audio_DispHandler_WakeUpOnSuccess = 1010;
    private static final int Audio_DispHandler_WakeUpOnFault = 1011;
    private static final int Audio_DispHandler_DispText = 1020;

    // ------------------- 播放mp3用 --------------------------
    AudioService musicService;
    MyConn conn=new MyConn();
    class MyConn implements ServiceConnection
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.MyBind mb = (AudioService.MyBind) service;
            musicService=mb.getService();
            LogUtils.i("生成music对象");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            conn=null;
        }
    }
    // ------------------- 播放mp3用 --------------------------

    // WakeUp.bin 唤醒词：小新小新，小部小部，小亚小亚
    // 词条
    // goodsname = 八宝粥,王老吉,冰红茶,可乐,雪碧,芬达,农夫山泉,娃哈哈,鲜橙多,水晶葡萄,水蜜桃,冰糖雪梨,花生牛奶
    // 说话
    // app.buy            = 我要买<goodsname>, 来一瓶<goodsname>,来一杯<goodsname>,来一罐<goodsname>,来一包<goodsname>,来一盒<goodsname>,来一根<goodsname>

    // 百度语音------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long start = System.currentTimeMillis();

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(MainActivity.this));
        MyExceptionHandler.macid = ((MyApp) getApplication()).getMainMacID();
        MyExceptionHandler.pkgname = Gmethod.getPackageName(MainActivity.this);
        MyExceptionHandler.version = Gmethod.getAppVersion(MainActivity.this);

        // SellerPushService 为第三方自定义推送服务
        PushManager.getInstance().initialize(this.getApplicationContext(), com.freshtribes.icecream.service.SellerPushService.class);
        // com.getui.demo.DemoIntentService 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), com.freshtribes.icecream.service.SellerIntentService.class);

        ((TextView) findViewById(R.id.macidtv)).setText("本机编号：" + ((MyApp) getApplication()).getMainMacID()
                +"   程序版本："+Gmethod.getAppVersion(MainActivity.this)+"");

        // 生成Presenter
        lp = new MainPresenter(this, tg, (MyApp) getApplication());

        // 左右翻页iv
        leftpageicon = findViewById(R.id.leftpagebtn);
        leftpageicon.setOnClickListener(this);
        rightpageicon = findViewById(R.id.rightpagebtn);
        rightpageicon.setOnClickListener(this);
        facepayicon = findViewById(R.id.face_pay);
        //facepayicon.setOnClickListener(this);
        if(((MyApp)getApplication()).getHaveFacePay()==1) {
            facepayicon = findViewById(R.id.face_pay);
            facepayicon.setOnClickListener(this);
        } else {
            findViewById(R.id.face_pay).setVisibility(View.GONE);
            findViewById(R.id.facepaytishi).setVisibility(View.GONE);
            findViewById(R.id.facepayresult).setVisibility(View.GONE);
        }
        if(((MyApp)getApplication()).getHaveMember()==0) {
            findViewById(R.id.iconiccard).setVisibility(View.GONE);
            findViewById(R.id.iconwxmember).setVisibility(View.GONE);
            findViewById(R.id.wxmembertishi).setVisibility(View.GONE);
            findViewById(R.id.qrictishi).setVisibility(View.GONE);
        }

        goodsupdown_btn[0] = findViewById(R.id.goodsbtn1);
        goodsupdown_btn[1] = findViewById(R.id.goodsbtn2);
        goodsupdown_btn[2] = findViewById(R.id.goodsbtn3);
        goodsupdown_btn[3] = findViewById(R.id.goodsbtn4);
        goodsupdown_btn[4] = findViewById(R.id.goodsbtn5);
        goodsupdown_btn[5] = findViewById(R.id.goodsbtn6);
        goodsupdown_btn[6] = findViewById(R.id.goodsbtn7);
        goodsupdown_btn[7] = findViewById(R.id.goodsbtn8);
        goodsupdown_btn[8] = findViewById(R.id.goodsbtn9);


        goodsimagedisp[0] = findViewById(R.id.goods1);
        goodsimagedisp[1] = findViewById(R.id.goods2);
        goodsimagedisp[2] = findViewById(R.id.goods3);
        goodsimagedisp[3] = findViewById(R.id.goods4);
        goodsimagedisp[4] = findViewById(R.id.goods5);
        goodsimagedisp[5] = findViewById(R.id.goods6);
        goodsimagedisp[6] = findViewById(R.id.goods7);
        goodsimagedisp[7] = findViewById(R.id.goods8);
        goodsimagedisp[8] = findViewById(R.id.goods9);


        goodsname[0] = findViewById(R.id.goodsname1);
        goodsname[1] = findViewById(R.id.goodsname2);
        goodsname[2] = findViewById(R.id.goodsname3);
        goodsname[3] = findViewById(R.id.goodsname4);
        goodsname[4] = findViewById(R.id.goodsname5);
        goodsname[5] = findViewById(R.id.goodsname6);
        goodsname[6] = findViewById(R.id.goodsname7);
        goodsname[7] = findViewById(R.id.goodsname8);
        goodsname[8] = findViewById(R.id.goodsname9);


        goodsprice[0] = findViewById(R.id.goodsprice1);
        goodsprice[1] = findViewById(R.id.goodsprice2);
        goodsprice[2] = findViewById(R.id.goodsprice3);
        goodsprice[3] = findViewById(R.id.goodsprice4);
        goodsprice[4] = findViewById(R.id.goodsprice5);
        goodsprice[5] = findViewById(R.id.goodsprice6);
        goodsprice[6] = findViewById(R.id.goodsprice7);
        goodsprice[7] = findViewById(R.id.goodsprice8);
        goodsprice[8] = findViewById(R.id.goodsprice9);

        for (int i = 0; i < 9; i++) {
            goodsupdown_btn[i].setVisibility(View.GONE);
            goodsupdown_btn[i].setOnClickListener(this);
            goodsupdown_btn[i].setImageAlpha(150);
            goodsimagedisp[i].setVisibility(View.GONE);
            goodsname[i].setVisibility(View.GONE);
            goodsprice[i].setVisibility(View.GONE);
        }

        matrix0 = new ColorMatrix();
        matrix0.setSaturation(0);
        filter0 = new ColorMatrixColorFilter(matrix0);
        matrix1 = new ColorMatrix();
        matrix1.setSaturation(1);
        filter1 = new ColorMatrixColorFilter(matrix1);

        // 和单片机交互
        tvhintinfo = findViewById(R.id.hintinfo);
        tvhintinfo.setText("正在启动......");

        // 支付区域的各个对象
        payrl = findViewById(R.id.salerl);
        payrl.setOnClickListener(null);
        payrl.setAlpha(0.98f);
        goodsimage_sale = findViewById(R.id.goodspng);

        payrl_goodsname = findViewById(R.id.goodsnametxt);
        payrl_goodsname.setText("");
        payrl_cashprice = findViewById(R.id.goodscashpricetv);
        payrl_cashprice.setText("");
        payrl_memberprice = findViewById(R.id.wxmembertishi);
        payrl_memberprice.setText("");

        alipaycodetishi = findViewById(R.id.alipaytishi);
        alipaycodetishi.setText("正在连接服务器...");
        qrcodealipay = findViewById(R.id.gd_qrcode_alipay);
        qrcodealipay.setVisibility(View.INVISIBLE);
        qrBitMapalipay = null;

        weixintishi = findViewById(R.id.weixintishi);
        weixintishi.setText("正在连接服务器...");
        qrcodeweixin = findViewById(R.id.gd_qrcode_weixin);
        qrcodeweixin.setVisibility(View.INVISIBLE);
        qrBitMapweixin = null;

        if(((MyApp) getApplication()).getHaveUms() == 1) {
            // 银联二维码支付：支付宝的位置保留，但是微信的位置就不显示了
            findViewById(R.id.gd_qrcode_weixin).setVisibility(View.INVISIBLE);
            findViewById(R.id.iconweixin).setVisibility(View.INVISIBLE);
            findViewById(R.id.weixintishi).setVisibility(View.INVISIBLE);

            // 然后把支付宝的logo换成银联的logo
            ((ImageView)findViewById(R.id.iconalipay)).setImageResource(R.drawable.umsicon);
        }

        qrictishi = findViewById(R.id.qrictishi);

        closePayRLbtn = findViewById(R.id.exitsale);
        closePayRLbtn.setOnClickListener(this);

        paycountdowntishi = findViewById(R.id.countdowntv);


        //MyPhoneStateListener类的对象，即设置一个监听器对象
        MyListener = new MyPhoneStateListener((ImageView) findViewById(R.id.imageViewsignal));
        //Return the handle to a system-level service by name.通过名字获得一个系统级服务
        //TelephonyManager类的对象
        Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Registers a listener object to receive notification of changes in specified telephony states.设置监听器监听特定事件的状态
        //Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); 这个语句放在onResume中

        String mtype = android.os.Build.MODEL; // 手机型号
        LogUtils.i("手机型号:"+mtype);

        if (mtype.contains("310") || mtype.contains("070")){
            // 打开串口(用缺省的串口标识符), vmc串口，扫码枪串口（如果没有扫码枪，则为NULL）
            lp.openVMC(((MyApp)getApplication()).getDevicetype(),"", "");
            LogUtils.i("310设备，打开vmc串口");
        } else if (mtype.contains("300")){
            // 打开串口(用缺省的串口标识符), vmc串口，扫码枪串口（如果没有扫码枪，则为NULL）
            lp.openVMC(((MyApp)getApplication()).getDevicetype(),"/dev/ttyO7", "/dev/ttyO6");
            LogUtils.i("300设备，打开vmc串口");
        } else {
            // RK3288
            lp.openVMC(((MyApp)getApplication()).getDevicetype(),"/dev/ttyS4", "/dev/ttyS3");
            LogUtils.i("RK3288，打开vmc串口");
        }

        dispHandler.sendEmptyMessage(dispHandler_Disp_GoodsImg_First);

        // 播放语音
        Intent intentSV = new Intent(MainActivity.this, AudioService.class);
        bindService(intentSV, conn, Context.BIND_AUTO_CREATE);

        if(((MyApp) getApplication()).getMichave()==1) {
            // 定义
            wp = EventManagerFactory.create(this, "wp");// 唤醒词事件管理器
            wp.registerListener(this); //  EventListener 中 onEvent方法

            // 定义
            asr = EventManagerFactory.create(this, "asr");// 识别事件管理器
            asr.registerListener(this);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    StartWPVoiceRecognize();
                }
            }, 2000);
            // 百度语音 --------------------------------------------------
        }
        LogUtils.i("Loading Main Create完成："+(System.currentTimeMillis()-start));
    }

    private void StartWPVoiceRecognize() {

        // 离线引擎加载
        Map<String, Object> params_load = new LinkedHashMap<String, Object>();
        params_load.put(SpeechConstant.DECODER, 2);// 0:在线 2.离在线融合(在线优先)
        params_load.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets:///baidu_speech_grammar.bsg");
        // 下面这段可选，用于生成SLOT_DATA参数， 用于动态覆盖ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH文件的词条部分
        JSONObject  json = new JSONObject();
        try {
            JSONArray ja = new JSONArray();
            for(int i=0;i<goodslist.size();i++){
                GoodsBean gb = goodslist.get(i);
                //gb.goodsname
                ja.put(gb.goodsname);
            }
            json.put("goodsname", ja);
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }
        params_load.put(SpeechConstant.SLOT_DATA, json.toString());
        // SLOT_DATA 参数添加完毕
        asr.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(params_load).toString(), null, 0, 0);

        // 开始唤醒词
        Map<String, Object> params_start = new LinkedHashMap<String, Object>();
        //params_start.put(SpeechConstant.ACCEPT_AUDIO_DATA, false);
        //params_start.put(SpeechConstant.DISABLE_PUNCTUATION, false);
        //params_start.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT,0);
        params_start.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
        params_start.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        //params_start.put(SpeechConstant.PID, 1536);
        wp.send(SpeechConstant.WAKEUP_START, new JSONObject(params_start).toString(), null, 0, 0);


        musicService.playWelcome();
    }

    //   EventListener  回调方法
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        // wakeup的场合
        if(name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_LOADED)) {
            LogUtils.i("EventName:wakeup已载入");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_STARTED)){
            LogUtils.i("EventName:wakeup已进入");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_READY)) {
            // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
            LogUtils.i("EventName:wakeup已准备好");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_STOPED)){
            // 识别结束
            LogUtils.i("EventName:wakeup已结束");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_UNLOADED)){
            LogUtils.i("EventName:wakeup已卸载");

            // asr的场合
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_LOADED)) {
            LogUtils.i("EventName:asr已载入");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
            // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
            LogUtils.i("EventName:asr已准备好");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN)){
            LogUtils.i("EventName:asr已开始");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
            // 识别结束
            LogUtils.i("EventName:asr已结束");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_EXIT)){
            LogUtils.i("EventName:asr已退出");
        } else if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_UNLOADED)){
            LogUtils.i("EventName:asr已卸载");
        } else {
            //LogUtils.i("其它的Name:"+name);
        }

        // wp的场合
        if(name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS)){
            try {
                JSONObject json = new JSONObject(params);
                int errorCode = json.getInt("errorCode");
                if(errorCode == 0){
                    //唤醒成功
                    dispHandler.sendEmptyMessage(Audio_DispHandler_WakeUpOnSuccess);
                } else {
                    //唤醒失败
                    //dispHandler.sendEmptyMessage(DispHandler_WakeUpOnFault);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String logTxt = "name: " + name;
        if (params != null && !params.isEmpty()) {
            logTxt += " ;params :" + params;
        } else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
        //LogUtils.i(logTxt);

        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            if (params.contains("\"result_type\":\"final_result\"")) {
                try {
                    JSONObject js = new JSONObject(params);
                    String resultCH = js.getString("best_result");
                    LogUtils.i("识别出的文字内容："+resultCH);

                    Message msg = dispHandler.obtainMessage(Audio_DispHandler_DispText);
                    msg.obj = resultCH;
                    dispHandler.sendMessage(msg);

                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }


//        String logTxt = "name: " + name;
//
//        if (params != null && !params.isEmpty()) {
//            logTxt += " ;params :" + params;
//        }
//        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
//            if (params.contains("\"nlu_result\"")) {
//                if (length > 0 && data.length > 0) {
//                    logTxt += ", 语义解析结果：" + new String(data, offset, length);
//
//                    voicetextdispet.setText(new String(data, offset, length));
//                }
//            }
//        } else if (data != null) {
//            logTxt += " ;data length=" + data.length;
//        }
//
//        Log.i(TAG,logTxt);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent=new Intent("com.mengdeman.vmselfstart.monitor.STOP");
        sendBroadcast(intent);
        finish();

        System.exit(0);

    }

//    @Override
//    public void onBackPressed() {
//        LogUtils.v("onBackPressed()");
//
//        super.onBackPressed();
//
//        // 关闭presenter的所有线程，然后跳转
//        lp.setStopThread();
//        long tgnow = System.currentTimeMillis();
//        while(tg.activeCount()>0){
//            for(int i=0;i<1000;i++);
//            if(System.currentTimeMillis() - tgnow >  3000) break;
//        }
//        tg.destroy();
//
//        Intent toMenu = new Intent(MainActivity.this, MenuActivity.class);
//        startActivity(toMenu);
//        MainActivity.this.finish();
//
//    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);

        //解除注册广播-----------------------------
        MainActivity.this.unregisterReceiver(modeswitch);
        //解除注册广播-----------------------------
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        //生成广播处理-----------------------------
        modeswitch = new ModeBroadCastReceiver();
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter("android.intent.action.ENG_MODE_SWITCH");
        //注册
        MainActivity.this.registerReceiver(modeswitch, intentFilter);
        //生成广播处理-----------------------------

        if(((MyApp)getApplication()).getMainMacID() == null || ((MyApp)getApplication()).getMainMacID().length()==0){
            // 说明系统内存崩溃了，那么要重启啊
            dispHandler.sendEmptyMessage(Goto_Loading);
        }
    }

    //内部类
    // 定义一个mode开关的接收器
    class ModeBroadCastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if (action.equals("android.intent.action.ENG_MODE_SWITCH")) {

                // 关闭presenter的所有线程，然后跳转
                lp.setStopThread();
                long tgnow = System.currentTimeMillis();
                while(tg.activeCount()>0){
                    for(int i=0;i<1000;i++);
                    if(System.currentTimeMillis() - tgnow >  3000) break;
                }
                tg.destroy();

                Intent toMenu = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(toMenu);
                MainActivity.this.finish();

            }
        }
    }

    @Override
    protected void onDestroy() {

        if(((MyApp) getApplication()).getMichave()==1) {
            // cancel 与stop的区别是 cancel在stop的基础上，完全停止整个识别流程，
            asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
            asr.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0);
            asr.unregisterListener(this);
            asr = null;

            // 停止，结束
            wp.send(SpeechConstant.WAKEUP_STOP, "{}", null, 0, 0);
            wp.unregisterListener(this);
            wp = null;
        }

        musicService = null;
        unbindService(conn);

        // 停止 支付宝和微信的等待推送的线程
        isAlipayWxpayWaitingGetuiRunning = false;

        for(int i = 0;i< goodsbmplist.size();i++) {
            if(goodsbmplist.get(i) != null) {
                if (!goodsbmplist.get(i).isRecycled()) {
                    goodsbmplist.get(i).recycle();
                }
            }
            goodsbmplist.set(i,null);
        }

        System.gc();
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.goodsbtn1:
                dispPayRL((pagenow-1)*9+0);
                break;

            case R.id.goodsbtn2:
                dispPayRL((pagenow-1)*9+1);
                break;

            case R.id.goodsbtn3:
                dispPayRL((pagenow-1)*9+2);
                break;

            case R.id.goodsbtn4:
                dispPayRL((pagenow-1)*9+3);
                break;

            case R.id.goodsbtn5:
                dispPayRL((pagenow-1)*9+4);
                break;

            case R.id.goodsbtn6:
                dispPayRL((pagenow-1)*9+5);
                break;

            case R.id.goodsbtn7:
                dispPayRL((pagenow-1)*9+6);
                break;

            case R.id.goodsbtn8:
                dispPayRL((pagenow-1)*9+7);
                break;

            case R.id.goodsbtn9:
                dispPayRL((pagenow-1)*9+8);
                break;

            case R.id.leftpagebtn:
                if(pagenow>1) pagenow--;
                goodsdisp_imageview(pagenow);
                break;

            case R.id.rightpagebtn:
                if(pagenow < pagetotal) pagenow++;
                goodsdisp_imageview(pagenow);
                break;

            case R.id.face_pay:
                LogUtils.i("客户按了人脸识别的按钮,重新开始倒计时");
                ((TextView)findViewById(R.id.facepayresult)).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.facepayresult)).setText("");

                paycountdown = PAYPAGE_COUNTDOWN;
                paycountdowntishi.setText(String.format("%d",paycountdown));
                ((MyApp)getApplication()).setPay_count(String.format("%d",paycountdown));

                Intent intent = new Intent(MainActivity.this, DetectActivity.class);
                startActivityForResult(intent, REQUEST_FACE_LOGIN);


                break;

            case R.id.exitsale:
                closePayRL();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FACE_LOGIN && data != null) {
            String code = data.getStringExtra("code");
            String msg = data.getStringExtra("msg");
            LogUtils.i("人脸支付的返回值,code:"+code+";msg:"+msg);

            if(code.equals("0")){
                // 说明已经识别到了人脸，继续获取参数
                facepay_uid = data.getStringExtra("uid");
                facepay_mobile = data.getStringExtra("user_info");
                facepay_score = "" + data.getDoubleExtra("score",0.834963);

                ((TextView)findViewById(R.id.facepayresult)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.facepayresult)).setText("人脸识别成功，用户手机号码："+facepay_mobile);

                if (mProgressDialog == null) {
                    mProgressDialog = ProgressDialog.createLoadingDialog(MainActivity.this, "请稍候...");
                }
                ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在验证余额,请稍候...");
                mProgressDialog.show();

                // 预定要出货的轨道 willouttrack
                lp.checkFaceUserMoney(facepay_uid,((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getPaymember());

            } else {
                if(msg.length()==0){
                    ((TextView)findViewById(R.id.facepayresult)).setVisibility(View.INVISIBLE);
                    ((TextView)findViewById(R.id.facepayresult)).setText("");
                } else {
                    // 识别识别，显示出来就可以了
                    ((TextView) findViewById(R.id.facepayresult)).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.facepayresult)).setText(msg);
                }
            }
        }
    }

    @Override
    public void rtnFaceMoneyCheck(String rtnstr){
        if(rtnstr.length()==0){
            dispHandler.sendEmptyMessage(dispHandler_facepay_moneyok);
        } else {
            Message message = dispHandler.obtainMessage(dispHandler_facepay_moneyng);
            message.obj = rtnstr;
            dispHandler.sendMessage(message);
        }
    }

    @Override
    public void rtnDispStatus(String status){
        Message msg = dispHandler.obtainMessage(Toast_Display);
        msg.obj = status;
        dispHandler.sendMessage(msg);
    }


    private synchronized void dispPayRL(int indexgoods){
        if(isDispPayRL) return;

        isDispPayRL = true;

        ((TextView)findViewById(R.id.facepayresult)).setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.facepayresult)).setText("");

        int cashprice = goodslist.get(indexgoods).paycash;
        // 获取一个随机的轨道值
        String havestr = goodslist.get(indexgoods).mainsaleing;

        int rand = (int)(Math.random() * havestr.split(",").length);
        // 预定要出货的轨道
        willouttrack = Integer.parseInt(havestr.split(",")[rand]);

        LogUtils.i("用户选择的商品的index-从0或9或18开始："+indexgoods+";对应的可售轨道全部："+havestr+";随机选一个出来的是："+willouttrack);

        payrl.setVisibility(View.VISIBLE);

        goodsimage_sale.setImageBitmap(goodsbmplist.get(indexgoods));
        payrl_goodsname.setText(goodslist.get(indexgoods).goodsname);
        payrl_cashprice.setText(String.format("￥%s元",Gmethod.tranFenToFloat1(goodslist.get(indexgoods).paycash)));
        payrl_memberprice.setText(String.format("IC卡或微信会员 ￥%s元",Gmethod.tranFenToFloat2(goodslist.get(indexgoods).paymember)));

        // 开始倒计时
        paycountdown = PAYPAGE_COUNTDOWN;
        paycountdowntishi.setText(String.format("%d",paycountdown));
        ((MyApp)getApplication()).setPay_count(String.format("%d",paycountdown));
        isAlreadyPayOrCancel = false;
        new Thread(new PayCountTimeThread()).start();

        payinfo = "";

        if(((MyApp) getApplication()).getHaveUms() == 0 || ((MyApp) getApplication()).getHaveMember() == 1 ) {
            qrictishi.setText("请刷会员卡或微信会员付款二维码...");
            Thread qricThread = new QrICThread_Self("" + willouttrack, goodslist.get(indexgoods).goodscode,
                    goodslist.get(indexgoods).paymember, goodslist.get(indexgoods).payali, goodslist.get(indexgoods).paywx);
            qricThread.setPriority(Thread.MIN_PRIORITY);
            qricThread.setName("付款二维码IC卡支付线程：" + formathms.format(new Date(System.currentTimeMillis())));
            qricThread.start();
        }

        if(((MyApp) getApplication()).getHaveUms() == 1) {
            // 使用银联支付的方式
            alipaycodetishi.setText(String.format("银联一码付  ￥%s元", Gmethod.tranFenToFloat2(goodslist.get(indexgoods).paymember)));
            Thread umspayCodeThread = new UmspayCodeThread_Self("" + willouttrack, goodslist.get(indexgoods).goodscode,
                    goodslist.get(indexgoods).payali);
            umspayCodeThread.setPriority(Thread.MIN_PRIORITY);
            umspayCodeThread.setName("银联二维码支付线程：" + formathms.format(new Date(System.currentTimeMillis())));
            umspayCodeThread.start();
        } else {
            if(((MyApp)getApplication()).getHaveAlipay() == 1) {
                alipaycodetishi.setText(String.format("支付宝扫一扫  ￥%s元", Gmethod.tranFenToFloat2(goodslist.get(indexgoods).payali)));
                Thread alipayCodeThread = new AlipayCodeThread_Self("" + willouttrack, goodslist.get(indexgoods).goodscode,
                        goodslist.get(indexgoods).payali);
                alipayCodeThread.setPriority(Thread.MIN_PRIORITY);
                alipayCodeThread.setName("支付宝二维码支付线程：" + formathms.format(new Date(System.currentTimeMillis())));
                alipayCodeThread.start();
            }
            if(((MyApp)getApplication()).getHaveZXAlipay() == 1) {
                alipaycodetishi.setText(String.format("支付宝扫一扫  ￥%s元", Gmethod.tranFenToFloat2(goodslist.get(indexgoods).payali)));
                Thread alipayCodeThread = new ZXAlipayCodeThread_Self("" + willouttrack, goodslist.get(indexgoods).goodscode,
                        goodslist.get(indexgoods).payali);
                alipayCodeThread.setPriority(Thread.MIN_PRIORITY);
                alipayCodeThread.setName("中信银行支付宝二维码支付线程：" + formathms.format(new Date(System.currentTimeMillis())));
                alipayCodeThread.start();
            }
            if(((MyApp)getApplication()).getHavaWeixin() == 1) {
                weixintishi.setText(String.format("微信扫一扫  ￥%s元", Gmethod.tranFenToFloat2(goodslist.get(indexgoods).paywx)));
                // 启动微信二维码支付的线程
                Thread weixinCodeThread = new WeixinThread_Self("" + willouttrack, goodslist.get(indexgoods).goodscode,
                        goodslist.get(indexgoods).paywx);
                weixinCodeThread.setPriority(Thread.MIN_PRIORITY);
                weixinCodeThread.setName("微信支付线程：" + formathms.format(new Date(System.currentTimeMillis())));
                weixinCodeThread.start();
            }
            if(((MyApp)getApplication()).getHaveZXWxpay() == 1) {
                weixintishi.setText(String.format("微信扫一扫  ￥%s元", Gmethod.tranFenToFloat2(goodslist.get(indexgoods).paywx)));
                // 启动微信二维码支付的线程
                Thread weixinCodeThread = new ZXWeixinThread_Self("" + willouttrack, goodslist.get(indexgoods).goodscode,
                        goodslist.get(indexgoods).paywx);
                weixinCodeThread.setPriority(Thread.MIN_PRIORITY);
                weixinCodeThread.setName("微信支付线程：" + formathms.format(new Date(System.currentTimeMillis())));
                weixinCodeThread.start();
            }
        }

    }

    private void closePayRL(){
        willouttrack = 0; // 饮料机为1-21，综合机为10-79

        payrl.setVisibility(View.GONE);

        isDispPayRL = false;

        isAlreadyPayOrCancel = true;

        goodsimage_sale.setImageBitmap(null);

        qrcodealipay.setImageBitmap(null);
        qrcodeweixin.setImageBitmap(null);
        if(qrBitMapalipay != null){
            if(!qrBitMapalipay.isRecycled()){
                qrBitMapalipay.recycle();
                qrBitMapalipay=null;
            }
        }
        if(qrBitMapweixin != null){
            if(!qrBitMapweixin.isRecycled()){
                qrBitMapweixin.recycle();
                qrBitMapweixin=null;
            }
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
                case Audio_DispHandler_WakeUpOnSuccess:
                    ((ImageView)findViewById(R.id.iv_voice)).setVisibility(View.VISIBLE);

                    // 此处 开始正常识别流程
                    Map<String, Object> params_start = new LinkedHashMap<String, Object>();
                    params_start.put(SpeechConstant.ACCEPT_AUDIO_DATA, false);
                    params_start.put(SpeechConstant.DISABLE_PUNCTUATION, false);

                    params_start.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
                    params_start.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);

                    params_start.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT,0);
                    params_start.put(SpeechConstant.PID, 1536);

                    String key = "PUDONGHUA" + "_" + "SEARCH" + "_" + 0;  //
                    params_start.put(key, 1536);

                    if (backTrackInMs > 0) { // 方案1， 唤醒词说完后，直接接句子，中间没有停顿。
                        params_start.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - backTrackInMs);

                    }

                    asr.send(SpeechConstant.ASR_START, new JSONObject(params_start).toString(), null, 0, 0);

                    break;

                case Audio_DispHandler_DispText:
                    String voice_txt = msg.obj.toString();
                    voice_txt = voice_txt.replace("小新","").replace("小心","");
                    Toast.makeText(MainActivity.this,voice_txt,Toast.LENGTH_LONG).show();

                    asr.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
                    asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);

                    // 识别到了语音文字了，那么要判断
                    if(!isDispPayRL) {
                        // 没有显示支付页面，那么判断哪个最合适
                        int[] score = new int[goodslist.size()];
                        for(int i=0;i<score.length;i++){
                            score[i] = 0;
                            String name = goodslist.get(i).goodsname;
                            for(int k=0;k<name.length();k++){
                                if(voice_txt.contains(name.substring(k,k+1))){
                                    score[i]++;
                                }
                            }
                        }
                        int maxindex = 0;
                        int maxscore = 0;
                        for(int i=0;i<score.length;i++){
                            if(maxscore <= score[i]){
                                maxscore = score[i];
                                maxindex = i;
                            }
                        }
                        LogUtils.i("语音文字："+voice_txt+";得分最高的为："+goodslist.get(maxindex).goodsname+";得分为："+maxscore);
                        if(maxscore>3){
                            dispPayRL(maxindex);
                            musicService.playBuy();
                        } else {
                            musicService.playSayAgain();
                        }
                    }

                    break;

                case Goto_Loading:
                    // 关闭presenter的所有线程，然后跳转
                    lp.setStopThread();
                    long tgnow = System.currentTimeMillis();
                    while(tg.activeCount()>0){
                        for(int i=0;i<1000;i++);
                        if(System.currentTimeMillis() - tgnow >  3000) break;
                    }
                    tg.destroy();

                    Intent toLoading = new Intent(MainActivity.this, LoadingActivity.class);
                    startActivity(toLoading);
                    MainActivity.this.finish();
                    break;

                case Toast_Display:
                    Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_LONG);
                    break;

                case Goto_Menu:
                    // 关闭presenter的所有线程，然后跳转
                    lp.setStopThread();
                    long tgnow2 = System.currentTimeMillis();
                    while(tg.activeCount()>0){
                        for(int i=0;i<1000;i++);
                        if(System.currentTimeMillis() - tgnow2 >  3000) break;
                    }
                    tg.destroy();

                    Intent toMenu = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(toMenu);
                    MainActivity.this.finish();
                    break;


                case dispHandler_Disp_GoodsImg_First:
                    // 和主机的交互结束，开始进入正常销售模式
                    tvhintinfo.setText("欢迎光临，机器正常销售中......");

                    // 启动心跳
                    if(!isSendHeart) {
                        isSendHeart = true;
                        lp.heart_thread(0,((MyApp) getApplication()).getTrackplanid(),
                                Gmethod.getAppVersion(MainActivity.this));
                    }

                    // 支付宝和微信的等待推送的线程
                    if(!isAlipayWxpayWaitingGetuiRunning) {
                        isAlipayWxpayWaitingGetuiRunning = true;
                        AlipayWxpayNext_Self nextThread = new AlipayWxpayNext_Self();
                        nextThread.setPriority(Thread.MIN_PRIORITY);
                        nextThread.setName("支付宝和微信的等待推送的线程：" + formathms.format(new Date(System.currentTimeMillis())));
                        nextThread.start();
                    }

                    // 准备图片
                    setGoodsPicture_Bitmap_First(true);

                    // 显示组合好的内容：
                    for(int i=0;i<goodslist.size();i++){
                        GoodsBean gb = goodslist.get(i);
                        LogUtils.i("商品列表中 goodslist--从0开始： " + i + ":" + gb.goodscode+":"+gb.goodsname+":有货轨道"+gb.mainsaleing+":无货轨道" +gb.mainsaleout);
                    }


                    // 总页面，现在的页码
                    pagetotal = goodslist.size()/9 + (goodslist.size()%9>0?1:0);
                    pagenow = 1;
                    // 开始显示图片等信息
                    goodsdisp_imageview(pagenow);

                    break;

                case dispHandler_Disp_GoodsImg_Refresh:
                    // VMC过来库存有变化（有到无，无到有）
                    ArrayList<GoodsBean> goodslist_tmp = new ArrayList<>();
                    for(int i=0;i<goodslist.size();i++){
                        GoodsBean oldgb = goodslist.get(i);
                        GoodsBean newgb = new GoodsBean();
                        newgb.goodscode = oldgb.goodscode;
                        newgb.goodsname = oldgb.goodsname;
                        newgb.paycash = oldgb.paycash;
                        newgb.payali = oldgb.payali;
                        newgb.paywx = oldgb.paywx;
                        newgb.paymember = oldgb.paymember;
                        newgb.mainsaleing = oldgb.mainsaleing;
                        newgb.mainsaleout = oldgb.mainsaleout;
                        goodslist_tmp.add(newgb);
                    }
                    // 重新来一遍数据的汇总,但是不需要读取bmp的商品图片
                    setGoodsPicture_Bitmap_First(false);
                    // 再次比较是否有变化
                    boolean isRefreshDispView = false;
                    for(int jk = (pagenow-1)*9;jk < (pagenow*9 > goodslist.size()?goodslist.size():pagenow*9);jk++){
                        if(!goodslist.get(jk).mainsaleing.equals(goodslist_tmp.get(jk).mainsaleing)){
                            isRefreshDispView = true;
                            break;
                        }
                    }
                    if(isRefreshDispView){
                        goodsdisp_imageview(pagenow);
                    }

                    break;



                case dispHandler_OutTrack_End:
                    isOuting = false;
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    // 出货结束，恢复初始值
                    willouttrack = 0;

                    // 判断4的index的商品的图片是否有问题
//                    if(goodsbmplist.size()>4){
//                        LogUtils.e("第5个商品的图片的大小:"+goodsbmplist.get(4).getByteCount()+";H:"+goodsbmplist.get(4).getHeight() + ";W:"+goodsbmplist.get(4).getWidth());
//
//                        goodsimagedisp[4].setImageBitmap(goodsbmplist.get(4));
//
//                    }

                    break;

                case dispHandler_OutTrack_NG3:
                    ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText(msg.obj.toString());
                    if(payinfo.length() > 0){
                        // 说明刚才出货的时手机支付的
                        // 银联二维码：// 交易号,支付者buyer_logon_id,fen
                        // 支付宝：// 商户交易号,支付宝交易号,支付者buyer_logon_id,fen
                        // 微信： // 商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注),fen
                        if(payinfo.split(",").length == 3) {
                            lp.refund_umspay(payinfo.split(",")[0], Integer.parseInt(payinfo.split(",")[2]));
                        } else if(payinfo.split(",").length == 4){
                            lp.refund_alipay(payinfo.split(",")[0],Integer.parseInt(payinfo.split(",")[3]));
                        } else if(payinfo.split(",").length == 6){
                            lp.refund_wxpay(payinfo.split(",")[0],Integer.parseInt(payinfo.split(",")[5]));
                        }
                        payinfo = "";
                    }
                    break;

                case dispHandler_OutTrack_NG:
                    ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText(msg.obj.toString());
                    break;

                case dispHandler_paying_count:
                    paycountdowntishi.setText(String.format("%d",Integer.parseInt(msg.obj.toString())));
                    ((MyApp)getApplication()).setPay_count(String.format("%d",Integer.parseInt(msg.obj.toString())));
                    break;

                case dispHandler_Close_PayRL:
                    closePayRL();
                    break;

                case dispHandler_qric_tishi:
                    qrictishi.setText(msg.obj.toString());
                    break;

                case dispHandler_qric_checkok:
                    payinfo = msg.obj.toString();

                    if (mProgressDialog == null) {
                        mProgressDialog = ProgressDialog.createLoadingDialog(MainActivity.this, "请稍候...");
                    }
                    ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在出货,请稍候...");
                    mProgressDialog.show();

                    // 去出货(1表示现金) 52：ic卡，53：微信公众号
                    isOuting = true; // 出货中

                    int qric_paymoneyfen = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getPaymember();
                    String qric_goodscode = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getGoodscode();
                    if(payinfo.contains("wxpaycode")){
                        LogUtils.i("微信会员二维码支付成功，出货轨道："+willouttrack+";价格分："+qric_paymoneyfen+";商品code："+qric_goodscode);

                        lp.vendoutind(willouttrack/10,willouttrack%10,qric_paymoneyfen,qric_goodscode,"wxpaycode",payinfo);
                    } else {
                        LogUtils.i("IC卡会员二维码支付成功，出货轨道："+willouttrack+";价格分："+qric_paymoneyfen+";商品code："+qric_goodscode);

                        lp.vendoutind(willouttrack/10,willouttrack%10,qric_paymoneyfen,qric_goodscode,"iccard",payinfo);

                    }


                    closePayRL();
                    break;

                case dispHandler_alipaycode_readyok:

                    try {
                        qrBitMapalipay = Create2DCodeUtil.Create2DCode(msg.obj.toString(), 360, 360);
                        qrcodealipay.setImageBitmap(qrBitMapalipay);
                        qrcodealipay.setVisibility(View.VISIBLE);
                        //alipaycodetishi.setText("请用支付宝钱包的扫一扫支付");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case dispHandler_alipaycode_readyng_nonet:
                    // 支付宝的提示语
                    alipaycodetishi.setText(msg.obj.toString());
                    break;

                case dispHandler_alipaycode_payok:
                    payinfo = msg.obj.toString();

                    if (mProgressDialog == null) {
                        mProgressDialog = ProgressDialog.createLoadingDialog(MainActivity.this, "请稍候...");
                    }
                    ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在出货,请稍候...");
                    mProgressDialog.show();

                    // 去出货(1表示现金)
                    isOuting = true; // 出货中

                    int alipay_paymoneyfen  = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getPayali();
                    String alipay_goodscode = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getGoodscode();
                    LogUtils.i("支付宝支付成功，出货轨道："+willouttrack+";价格分："+alipay_paymoneyfen+";商品code："+alipay_goodscode);

                    lp.vendoutind(willouttrack/10,willouttrack%10,alipay_paymoneyfen,alipay_goodscode,"alipay",payinfo);

                    closePayRL();

                    break;

                case dispHandler_umspaycode_readyok:

                    try {
                        qrBitMapalipay = Create2DCodeUtil.Create2DCode(msg.obj.toString(), 360, 360);
                        qrcodealipay.setImageBitmap(qrBitMapalipay);
                        qrcodealipay.setVisibility(View.VISIBLE);
                        //alipaycodetishi.setText("请用支付宝钱包的扫一扫支付");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case dispHandler_umspaycode_readyng_nonet:
                    // 支付宝的提示语
                    alipaycodetishi.setText(msg.obj.toString());
                    break;

                case dispHandler_umspaycode_payok:
                    payinfo = msg.obj.toString();

                    if (mProgressDialog == null) {
                        mProgressDialog = ProgressDialog.createLoadingDialog(MainActivity.this, "请稍候...");
                    }
                    ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在出货,请稍候...");
                    mProgressDialog.show();

                    // 去出货(1表示现金)
                    isOuting = true; // 出货中

                    int umspay_paymoneyfen  = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getPaymember();
                    String umspay_goodscode = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getGoodscode();
                    LogUtils.i("银联二维码支付成功，出货轨道："+willouttrack+";价格分："+umspay_paymoneyfen+";商品code："+umspay_goodscode);

                    lp.vendoutind(willouttrack/10,willouttrack%10,umspay_paymoneyfen,umspay_goodscode,"umspay",payinfo);

                    closePayRL();

                    break;

                case dispHandler_weixin_readyok:
                    try {
                        qrBitMapweixin = Create2DCodeUtil.Create2DCode(msg.obj.toString(), 360, 360);
                        qrcodeweixin.setImageBitmap(qrBitMapweixin);
                        qrcodeweixin.setVisibility(View.VISIBLE);
                        //weixintishi.setText("请用微信的扫一扫支付");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case dispHandler_weixin_readyng_nonet:
                    // 微信的提示语
                    weixintishi.setText(msg.obj.toString());

                    break;

                case dispHandler_weixin_payok:
                    payinfo = msg.obj.toString();


                    if (mProgressDialog == null) {
                        mProgressDialog = ProgressDialog.createLoadingDialog(MainActivity.this, "请稍候...");
                    }
                    ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在出货,请稍候...");
                    mProgressDialog.show();

                    // 去出货(1表示现金)
                    isOuting = true; // 出货中
                    int wxpay_paymoneyfen = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getPaywx();
                    String wxpay_goodscode = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getGoodscode();
                    LogUtils.i("微信支付成功，出货轨道："+willouttrack+";价格分："+wxpay_paymoneyfen+";商品code："+wxpay_goodscode);

                    lp.vendoutind(willouttrack/10,willouttrack%10,wxpay_paymoneyfen,wxpay_goodscode,"wxpay",payinfo);

                    closePayRL();
                    break;

                case dispHandler_Disp_NetStatus:
                    ((TextView)findViewById(R.id.netstatus)).setText(msg.obj.toString());
                    if(msg.obj.toString().contains("4G")){
                        findViewById(R.id.imageViewsignal).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.imageViewsignal).setVisibility(View.INVISIBLE);
                    }
                    break;

                case dispHandler_facepay_moneyng:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    // 识别识别，显示出来就可以了
                    ((TextView) findViewById(R.id.facepayresult)).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.facepayresult)).setText(msg.obj.toString());
                    break;

                case dispHandler_facepay_moneyok:
                    // 人脸的用户余额够的
                    ((TextView) mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("正在出货(人脸支付者:"+facepay_mobile.substring(0,3)+"****"+facepay_mobile.substring(7,11)+"),请稍候...");

                    // 去出货(1表示现金)
                    isOuting = true; // 出货中

                    int facepay_paymoneyfen  = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getPaymember();
                    String facepay_goodscode = ((MyApp)getApplication()).getTrackMainGeneral()[willouttrack-10].getGoodscode();
                    LogUtils.i("人脸支付余额检查成功，出货轨道："+willouttrack+";价格分："+facepay_paymoneyfen+";商品code："+facepay_goodscode);

                    lp.vendoutind(willouttrack/10,willouttrack%10,facepay_paymoneyfen,facepay_goodscode,"facepay",
                            ""+facepay_uid+"_"+facepay_mobile+"_"+facepay_score);

                    closePayRL();
                    break;

                case dispHandler_Disp_Temp:
                    // 收到温度了，然后显示出来
                    ((TextView)findViewById(R.id.tv_temp)).setText(msg.obj.toString());
                    break;

                default:
                    break;
            }
            return  false;
        }
    });


    private Handler handler = new Handler(Looper.getMainLooper());
    // 出货结果（空表示成功出货）
    @Override
    public void rtnVendOutInd(final String[] resultinfo){
        if(resultinfo[0].length()==0){
            // 说明出货成功了的。
            dispHandler.sendEmptyMessage(dispHandler_OutTrack_End);

            handler.post(new Runnable() {
                 @Override
                 public void run() {
                    // rtn,""+hang+lie,""+fen,goodscode,payway,payinfo
                    // String payway,String saletime,String pricefen,String trackno,String goodscode,String paypara
                    lp.send_SaleDataThread(resultinfo[4],
                            (new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()),
                            resultinfo[2],"0"+resultinfo[1],resultinfo[3],resultinfo[5]);
                 }
            });
        } else {
            boolean isAliOrWx = false;
            // 出货失败了
            if(resultinfo[4].equals("alipay") || resultinfo[4].equals("wxpay") || resultinfo[4].equals("umspay")){
                isAliOrWx = true;
            }
            Message msg3 = dispHandler.obtainMessage(dispHandler_OutTrack_NG3);
            msg3.obj = "抱歉，出货发生错误" + (!isAliOrWx?"":"(已通知后台退款)")+"3..."+resultinfo[0];
            dispHandler.sendMessage(msg3);
            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

            Message msg2 = dispHandler.obtainMessage(dispHandler_OutTrack_NG);
            msg2.obj = "抱歉，出货发生错误" + (!isAliOrWx?"":"(已通知后台退款)")+"2..."+resultinfo[0];
            dispHandler.sendMessage(msg2);
            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

            Message msg1 = dispHandler.obtainMessage(dispHandler_OutTrack_NG);
            msg1.obj = "抱歉，出货发生错误" + (!isAliOrWx?"":"(已通知后台退款)")+"1..."+resultinfo[0];
            dispHandler.sendMessage(msg1);
            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

            dispHandler.sendEmptyMessage(dispHandler_OutTrack_End);
        }
    }


    // 取得状态的结果
    @Override
    public void rtnGetStatus(String statusinfo,String doorstatus){
//        int temp1 = Integer.parseInt(statusinfo[0]);
//        int temp2 = Integer.parseInt(statusinfo[1]);
//        int temp3 = Integer.parseInt(statusinfo[2]);
        String temp1 = statusinfo;
        //String temp2 = statusinfo[1];
        //String temp3 = statusinfo[2];

        Message msg = dispHandler.obtainMessage(dispHandler_Disp_Temp);
        if(!doorstatus.equals("0")){
            msg.obj = "温度：" + temp1 + "°C，门状态：" + (doorstatus.equals("-")?"-":"开着");
        } else {
            msg.obj = "温度：" + temp1+ "°C";
        }
        dispHandler.sendMessage(msg);
    }

    @Override
    public void HaveErrorFreshDisp(){
        dispHandler.sendEmptyMessage(dispHandler_Disp_GoodsImg_Refresh);
    }

    /**
     * 把能否销售信息给显示的list
     * @param track 轨道1,10,10 （2个都是10，通过drinkmainsub来区分）
     * @param iscansale 能否销售 true表示能销售
     * @param goodscode 商品编号
     * @param goodsname 商品名称
     * @param cash 现金分
     * @param alipay 支付支付分
     * @param wxpay 微信支付分
     * @param member 会员支付分
     */
    private void addGoodtoList(String track,boolean iscansale,String goodscode,String goodsname,int cash,int alipay,int wxpay,int member,boolean needloadbmp){
        int havegood = -1; // 表示没有找到

        for(int i=0;i<goodslist.size();i++){
            if(goodslist.get(i).goodscode.equals(goodscode)){
                havegood = i;
                break;
            }
        }

        if(havegood > -1){
            // 表示找到了
            if(iscansale){
                if (goodslist.get(havegood).mainsaleing.length() == 0) {
                    goodslist.get(havegood).mainsaleing = track;
                } else {
                    goodslist.get(havegood).mainsaleing = goodslist.get(havegood).mainsaleing + "," + track;
                }
            } else {
                if (goodslist.get(havegood).mainsaleout.length() == 0) {
                    goodslist.get(havegood).mainsaleout = track;
                } else {
                    goodslist.get(havegood).mainsaleout = goodslist.get(havegood).mainsaleout + "," + track;
                }
            }
        } else {
            GoodsBean newGoodsBean = new GoodsBean();
            newGoodsBean.goodscode = goodscode;
            newGoodsBean.goodsname = goodsname;
            newGoodsBean.paycash = cash;
            newGoodsBean.payali = alipay;
            newGoodsBean.paywx = wxpay;
            newGoodsBean.paymember = member;
            if(iscansale){
                newGoodsBean.mainsaleing = track;
            } else {
                newGoodsBean.mainsaleout = track;
            }
            goodslist.add(newGoodsBean);

            // 刷新时，是不需要导入商品图片的bmp文件的
            if(needloadbmp) {
                // 读取商品图片
                String pngfilename = Environment.getExternalStorageDirectory() + "/goodspng/" + goodscode + ".png";
                File file = new File(pngfilename);
                if (!file.exists()) {
                    goodsbmplist.add(null);
                } else {
                    goodsbmplist.add(BitmapUtil.getBitmapFromFile(file, 180, 220));
                }
            }
        }
    }

    // 首次时设置商品图片和价格图片
    private void setGoodsPicture_Bitmap_First(boolean needloadbmp){
        // 没有商品
        goodslist.clear();

        int[] levelnum = new int[]{((MyApp)getApplication()).getMainGeneralLevel1TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel2TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel3TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel4TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel5TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel6TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel7TrackCount()};
        for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
            for(int k=0;k<levelnum[i];k++){
                if(((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getCansale()==0) continue;

                String stri = "" + (i+1) + k;
                if(((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getGoodscode().length() > 0) {
                    if (((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getNumnow() > 0
                            && ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getErrorcode().length() == 0) {
                        addGoodtoList( stri, (((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getCansale()==0?false:true)
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getGoodscode()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getGoodsname()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPaycash()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPayali()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPaywx()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPaymember(),needloadbmp);
                    } else {
                        addGoodtoList(stri, false
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getGoodscode()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getGoodsname()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPaycash()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPayali()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPaywx()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPaymember(),needloadbmp);
                    }
                }
            }
        }
    }

    // disppage:要显示的页面，比如刚开始时为1，翻到第二页后要显示第二页该值为2
    private void goodsdisp_imageview(int disppage){
        int starti = (disppage-1)*9;
        int loopnum = goodslist.size() - (disppage-1)*9;
        if(loopnum > 9) loopnum = 9;
        for(int i=starti;i<starti+loopnum;i++){
            goodsupdown_btn[i - starti].setVisibility(View.VISIBLE);
            goodsimagedisp[i - starti].setVisibility(View.VISIBLE);
            goodsname[i - starti].setVisibility(View.VISIBLE);
            goodsprice[i - starti].setVisibility(View.VISIBLE);

            goodsimagedisp[i - starti].setImageBitmap(goodsbmplist.get(i));
            goodsname[i - starti].setText(goodslist.get(i).goodsname);
            goodsprice[i - starti].setText("￥"+Gmethod.tranFenToFloat2(goodslist.get(i).paycash));

            if( goodslist.get(i).mainsaleing.length()>0 ){
                goodsupdown_btn[i - starti].setOnClickListener(this);
                goodsupdown_btn[i - starti].setImageResource(R.drawable.gs_selector);
            } else {
                goodsupdown_btn[i - starti].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this,"已售空，请选择其它商品",Toast.LENGTH_SHORT).show();
                    }
                });
                goodsupdown_btn[i - starti].setImageResource(R.drawable.saleover);
            }
        }
        for(int i=starti+loopnum;i<disppage*9;i++){
            goodsupdown_btn[i - starti].setVisibility(View.INVISIBLE);
            goodsimagedisp[i - starti].setVisibility(View.INVISIBLE);
            goodsname[i - starti].setVisibility(View.INVISIBLE);
            goodsprice[i - starti].setVisibility(View.INVISIBLE);
        }
        if(disppage <= 1){
            leftpageicon.setColorFilter(filter0);
            leftpageicon.setOnClickListener(null);
        } else {
            leftpageicon.setColorFilter(filter1);
            leftpageicon.setOnClickListener(this);
        }
        if(disppage >= pagetotal){
            rightpageicon.setColorFilter(filter0);
            rightpageicon.setOnClickListener(null);
        } else {
            rightpageicon.setColorFilter(filter1);
            rightpageicon.setOnClickListener(this);
        }

    }

    // 右下角的时间的每秒刷新的线程
    private class PayCountTimeThread extends Thread {
        @Override
        public void run() {
            timethreadisrunning = true;
            LogUtils.i( "开始时间线程");

            while (!isAlreadyPayOrCancel) {
                // 1秒刷新1次
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                paycountdown--;

                Message countdownmsg = dispHandler.obtainMessage(dispHandler_paying_count);
                countdownmsg.obj = paycountdown;
                dispHandler.sendMessage(countdownmsg);

                if (paycountdown <= 0) {
                    //isFromVMSelected = -1;
                    dispHandler.sendEmptyMessage(dispHandler_Close_PayRL);

                    break;
                }
            }

            timethreadisrunning = false;
        }
    }

    // 扫码IC卡的支付的线程
    private class QrICThread_Self extends Thread {
        // 用户选择的轨道编号（10，，，A0，A1，，，K0，，，）
        private String t_selectedTrackNo = "";
        private String t_goodscode = "";
        private int t_pricefen = 1;
        private int alipay_pricefen = 1;
        private int wxpay_pricefen = 1;

        private QrICThread_Self(String selectedTrackNo, String goodscode, int price,int alipay_pricefen,int wxpay_pricefen) {
            this.t_selectedTrackNo = "0" + (selectedTrackNo.length() == 1 ? "0" + selectedTrackNo : selectedTrackNo);
            this.t_goodscode = goodscode;
            this.t_pricefen = price;
            this.alipay_pricefen = alipay_pricefen;
            this.wxpay_pricefen = wxpay_pricefen;
        }

        public void run() {
            qricthreadisrunning = true;

            LogUtils.i("开始扫码IC卡支付处理线程");

            // 清空缓存
            String receiveStr = lp.getReceivedASCII();
            boolean isPayOK = false;

            String weixin_selfcheck = "";
            // 打开LED灯
            lp.openLED();

            while (!isAlreadyPayOrCancel && !isPayOK) {
                // 还在倒计时中
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(weixin_selfcheck.length() > 0){
                    // 需要微信轮询的
                    String soaprtn = (new MyHttp()).post("https://api.mch.weixin.qq.com/pay/orderquery", weixin_selfcheck);

                    if (soaprtn.length() > 0) {  // 表示有返回的啦
                        String return_code = Gmethod.xuan_zhi(soaprtn, "<return_code><![CDATA[", "]]></return_code>");

                        if (return_code.equals("SUCCESS")) {
                            String result_code = Gmethod.xuan_zhi(soaprtn, "<result_code><![CDATA[", "]]></result_code>");
                            if (result_code.equals("SUCCESS")) {
                                String endresult = Gmethod.xuan_zhi(soaprtn, "<trade_state><![CDATA[", "]]></trade_state>");
                                LogUtils.i( "轮询结果1：" + endresult);
                                if (endresult.equals("SUCCESS")) {

                                    // 商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注),fen
                                    String paysuccess = Gmethod.xuan_zhi(soaprtn, "<out_trade_no><![CDATA[","]]></out_trade_no>") + "," +
                                            Gmethod.xuan_zhi(soaprtn, "<transaction_id><![CDATA[","]]></transaction_id>") + "," +
                                            Gmethod.xuan_zhi(soaprtn, "<openid><![CDATA[","]]></openid>") + "," +
                                            Gmethod.xuan_zhi(soaprtn, "<appid><![CDATA[","]]></appid>") + "," +
                                            (Gmethod.xuan_zhi(soaprtn, "<is_subscribe><![CDATA[","]]></is_subscribe>").equals("Y")?"1":"0")
                                            +","+Gmethod.xuan_zhi(soaprtn, "<cash_fee>","</cash_fee>");
                                    LogUtils.i("--QrICThread_Self----微信self------"+paysuccess);

                                    //  微信支付成功了，要开始出货啦
                                    Message msg2 = dispHandler.obtainMessage();
                                    msg2.what = dispHandler_weixin_payok;
                                    msg2.obj = paysuccess;
                                    dispHandler.sendMessage(msg2);

                                    // 跳出循环
                                    break;
                                }

                            }
                            //else {
                            //LogUtils.i( "weixin轮询结果2：" + "err_code:" + xuan_zhi(soaprtn, "<err_code><![CDATA[", "]]></err_code>"));
                            //}
                        }
                        //else {
                        //LogUtils.i( "weixin轮询结果3：" + xuan_zhi(soaprtn, "<return_msg><![CDATA[", "]]></return_msg>"));
                        //}
                    }

                    // 后面的就不去执行啦
                    continue;
                }

                receiveStr = lp.getReceivedASCII();
                if (receiveStr.length() > 2) {
                    // 说明有收到数据了
                    if (receiveStr.substring(0, 2).equals("QR")) {
                        Message msg = dispHandler.obtainMessage(dispHandler_qric_tishi);
                        msg.obj = "已接收到付款二维码，正在验证...";
                        dispHandler.sendMessage(msg);

                        String realpaycode = receiveStr.substring(2);
                        LogUtils.i("realpaycode:"+realpaycode);

                        if(realpaycode.length() == 33 && realpaycode.substring(0,3).equals("A20") && ((MyApp) getApplication()).getHaveMember()==1) {
                            // 微信公众号付款码 2018........
                            long timestamp = System.currentTimeMillis();
                            String str = "macid=" + ((MyApp) getApplication()).getMainMacID() + "&payqrcode=" + receiveStr.substring(2) +
                                    "&timestamp=" + timestamp + "&accesskey=" + ((MyApp) getApplication()).getAccessKey();
                            String md5 = MD5.GetMD5Code(str);

                            String rtnstr = (new MyHttp()).post(((MyApp) getApplication()).getServerurl() + "/balancewx",
                                    "macid=" + ((MyApp) getApplication()).getMainMacID() + "&payqrcode=" + receiveStr.substring(2) +
                                            "&timestamp=" + timestamp + "&md5=" + md5);

                            LogUtils.i("收到付款二维码，连接服务后台验证，返回结果：" + rtnstr);

                            if (rtnstr.length() > 0) {
                                // 说明和后台联网正常的
                                try {
                                    JSONObject soapJson = new JSONObject(rtnstr);
                                    if (soapJson.getString("code").equals("0")) {
                                        // 取到了正确的结果啦
                                        int balance = soapJson.getInt("balance");
                                        if (balance >= t_pricefen) {
                                            isPayOK = true;
                                            break;
                                        } else {
                                            // 清除缓存
                                            receiveStr = lp.getReceivedASCII();
                                            Message msg1 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                            msg1.obj = "已接收到付款二维码，正在验证...收到错误信息：余额不足";
                                            dispHandler.sendMessage(msg1);
                                        }
                                    } else {
                                        // 清除缓存
                                        receiveStr = lp.getReceivedASCII();
                                        Message msg2 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                        if (msg.equals("付款码已失效")) {
                                            msg2.obj = "已接收到付款二维码，正在验证...收到错误信息：" + soapJson.getString("msg") + ",请刷新二维码";
                                        } else {
                                            msg2.obj = "已接收到付款二维码，正在验证...收到错误信息：" + soapJson.getString("msg") + ",请重试";
                                        }
                                        dispHandler.sendMessage(msg2);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // 清除缓存
                                receiveStr = lp.getReceivedASCII();
                                Message msg3 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                msg3.obj = "已接收到付款二维码，正在验证...联网失败，请重试";
                                dispHandler.sendMessage(msg3);
                            }
                        } else if(realpaycode.length() == 18 &&
                                (realpaycode.substring(0,2).equals("10") || realpaycode.substring(0,2).equals("11")
                                        || realpaycode.substring(0,2).equals("12") || realpaycode.substring(0,2).equals("13")
                                        || realpaycode.substring(0,2).equals("14") || realpaycode.substring(0,2).equals("15"))
                                && ((MyApp) getApplication()).getHaveUms() == 0) {
                            // 微信付款码
                            long timestamp = System.currentTimeMillis();
                            String str = "goodscode="+t_goodscode+
                                    "&macid="+ ((MyApp)getApplication()).getMainMacID()+
                                    "&paycode="+realpaycode+
                                    "&price="+wxpay_pricefen+
                                    "&track="+t_selectedTrackNo+
                                    "&timestamp="+timestamp  +
                                    "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                            String md5 = MD5.GetMD5Code(str);

                            String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/wxqrpay",
                                    "goodscode="+t_goodscode+
                                            "&macid="+ ((MyApp)getApplication()).getMainMacID()+
                                            "&paycode="+realpaycode+
                                            "&price="+wxpay_pricefen+
                                            "&track="+t_selectedTrackNo+
                                            "&timestamp="+timestamp +
                                            "&md5=" + md5);

                            LogUtils.i("收到微信付款二维码"+realpaycode+"，连接服务后台验证，返回结果：" + rtnstr);

                            if (rtnstr.length() > 0) {
                                // 说明和后台联网正常的
                                try {
                                    JSONObject soapJson = new JSONObject(rtnstr);
                                    if (soapJson.getString("code").equals("0")) {
                                        //isPayOK = true; 不是IC卡或微信公众号支付成功的，所以这里要注解
                                        // 取到了正确的结果啦
                                        String paysuccess = soapJson.getString("paysuccess");

                                        //  微信支付成功了，要开始出货啦
                                        Message msg2 = dispHandler.obtainMessage();
                                        msg2.what = dispHandler_weixin_payok;
                                        msg2.obj = paysuccess;
                                        dispHandler.sendMessage(msg2);

                                        // 跳出循环
                                        break;

                                    } else {
                                        if (soapJson.getString("code").equals("999")) {
                                            Message msg2 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                            msg2.obj = "已接收到微信付款二维码，验证后提示：" + soapJson.getString("msg") + "";
                                            dispHandler.sendMessage(msg2);

                                            weixin_selfcheck = soapJson.getString("selfcheck");
                                            LogUtils.i("微信付款码_weixin_selfcheck:"+weixin_selfcheck);
                                        } else {
                                            // 清除缓存
                                            receiveStr = lp.getReceivedASCII();
                                            Message msg2 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                            msg2.obj = "已接收到微信付款二维码，验证后收到错误：" + soapJson.getString("msg") + ",请重试";
                                            dispHandler.sendMessage(msg2);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // 清除缓存
                                receiveStr = lp.getReceivedASCII();
                                Message msg3 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                msg3.obj = "已接收到微信付款二维码，正在验证...联网失败，请重试";
                                dispHandler.sendMessage(msg3);
                            }
                        } else if((realpaycode.length() >= 16 && realpaycode.length() <= 24) &&
                                (realpaycode.substring(0,2).equals("25") || realpaycode.substring(0,2).equals("26")
                                        || realpaycode.substring(0,2).equals("27") || realpaycode.substring(0,2).equals("28")
                                        || realpaycode.substring(0,2).equals("29") || realpaycode.substring(0,2).equals("30"))
                                && ((MyApp) getApplication()).getHaveUms() == 0) {
                            // 支付宝付款码
                            long timestamp = System.currentTimeMillis();
                            String str = "goodscode="+t_goodscode+
                                    "&macid="+ ((MyApp)getApplication()).getMainMacID()+
                                    "&paycode="+realpaycode+
                                    "&price="+alipay_pricefen+
                                    "&track="+t_selectedTrackNo+
                                    "&timestamp="+timestamp  +
                                    "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                            String md5 = MD5.GetMD5Code(str);

                            String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/aliqrpay",
                                    "goodscode="+t_goodscode+
                                            "&macid="+ ((MyApp)getApplication()).getMainMacID()+
                                            "&paycode="+realpaycode+
                                            "&price="+alipay_pricefen+
                                            "&track="+t_selectedTrackNo+
                                            "&timestamp="+timestamp +
                                            "&md5=" + md5);

                            LogUtils.i("收到支付宝付款二维码"+realpaycode+"，连接服务后台验证，返回结果：" + rtnstr);

                            if (rtnstr.length() > 0) {
                                // 说明和后台联网正常的
                                try {
                                    JSONObject soapJson = new JSONObject(rtnstr);
                                    if (soapJson.getString("code").equals("0")) {
                                        //isPayOK = true; 不是IC卡或微信公众号支付成功的，所以这里要注解
                                        // 取到了正确的结果啦
                                        String paysuccess = soapJson.getString("paysuccess");

                                        //  微信支付成功了，要开始出货啦
                                        Message msg2 = dispHandler.obtainMessage();
                                        msg2.what = dispHandler_alipaycode_payok;
                                        msg2.obj = paysuccess;
                                        dispHandler.sendMessage(msg2);

                                        // 跳出循环
                                        break;

                                    } else {

                                        // 清除缓存
                                        receiveStr = lp.getReceivedASCII();
                                        Message msg2 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                        msg2.obj = "已接收到支付宝付款二维码，验证后收到错误：" + soapJson.getString("msg") + ",请重试";
                                        dispHandler.sendMessage(msg2);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // 清除缓存
                                receiveStr = lp.getReceivedASCII();
                                Message msg3 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                msg3.obj = "已接收到支付宝付款二维码，正在验证...联网失败，请重试";
                                dispHandler.sendMessage(msg3);
                            }
                        } else {
                            // 清除缓存
                            receiveStr = lp.getReceivedASCII();

                            Message msg22 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                            msg22.obj = "已接收到付款二维码，验证结果为非法二维码，请使用合法二维码重试.";
                            dispHandler.sendMessage(msg22);
                        }

                    } else if (receiveStr.substring(0, 2).equals("IC") && ((MyApp) getApplication()).getHaveMember()==1) {
                        Message msg = dispHandler.obtainMessage(dispHandler_qric_tishi);
                        msg.obj = "已接收到会员卡信息，正在验证...";
                        dispHandler.sendMessage(msg);

                        long timestamp = System.currentTimeMillis();
                        String str = "innercardno=" + receiveStr.substring(2) + "&macid=" + ((MyApp) getApplication()).getMainMacID() +
                                "&timestamp=" + timestamp + "&accesskey=" + ((MyApp) getApplication()).getAccessKey();
                        String md5 = MD5.GetMD5Code(str);

                        String rtnstr = (new MyHttp()).post(((MyApp) getApplication()).getServerurl() + "/balancecard",
                                "macid=" + ((MyApp) getApplication()).getMainMacID() + "&innercardno=" + receiveStr.substring(2) +
                                        "&timestamp=" + timestamp + "&md5=" + md5);

                        LogUtils.i("收到付款会员卡信息（物理卡号），连接服务后台验证，返回结果：" + rtnstr+";receiveStr:"+receiveStr);

                        if (rtnstr.length() > 0) {
                            // 说明和后台联网正常的
                            try {
                                JSONObject soapJson = new JSONObject(rtnstr);
                                if (soapJson.getString("code").equals("0")) {
                                    // 取到了正确的结果啦
                                    int balance = soapJson.getInt("balance");
                                    if (balance >= t_pricefen) {
                                        String effdate = soapJson.getString("effectivedate");
                                        String today = (new SimpleDateFormat("yyyyMMdd", Locale.CHINA)).format(System.currentTimeMillis());
                                        if(today.compareTo(effdate) > 0){
                                            // 说明今天的日子比有效期要大
                                            // 清除缓存
                                            receiveStr = lp.getReceivedASCII();
                                            Message msg1 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                            msg1.obj = "已接收到付款二维码，正在验证...收到错误信息：此卡已过有效期";
                                            dispHandler.sendMessage(msg1);
                                        } else {
                                            isPayOK = true;
                                            break;
                                        }
                                    } else {
                                        // 清除缓存
                                        receiveStr = lp.getReceivedASCII();
                                        Message msg1 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                        msg1.obj = "已接收到付款二维码，正在验证...收到错误信息：余额不足";
                                        dispHandler.sendMessage(msg1);
                                    }
                                } else {
                                    // 清除缓存
                                    receiveStr = lp.getReceivedASCII();
                                    Message msg2 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                                    msg2.obj = "已接收到会员卡信息，正在验证...收到错误信息：" + soapJson.getString("msg") + ",请重试";
                                    dispHandler.sendMessage(msg2);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // 清除缓存
                            receiveStr = lp.getReceivedASCII();
                            Message msg3 = dispHandler.obtainMessage(dispHandler_qric_tishi);
                            msg3.obj = "已接收到会员卡信息，正在验证...联网失败，请重试";
                            dispHandler.sendMessage(msg3);
                        }
                    } else {
                        // 符合要求，直接放弃
                        // 清除缓存
                        receiveStr = lp.getReceivedASCII();
                    }
                }
            }
            if(isPayOK){
                // 服务器端验证通过，余额也足够
                Message msg2 = dispHandler.obtainMessage();
                msg2.what = dispHandler_qric_checkok;
                if(receiveStr.substring(0,2).equals("QR")){
                    msg2.obj = receiveStr.substring(2)+",wxpaycode";
                } else {
                    msg2.obj = receiveStr.substring(2)+",iccard";
                }
                dispHandler.sendMessage(msg2);
            }
            // 关灯
            lp.closeLED();

            qricthreadisrunning = false;
        }
    }


    // 支付宝二维码支付的线程
    private class AlipayCodeThread_Self extends Thread{
        // 用户选择的轨道编号（10，，，A0，A1，，，K0，，，）
        private String t_selectedTrackNo = "";
        private String t_goodscode = "";
        private int t_pricefen = 1;

        private AlipayCodeThread_Self(String selectedTrackNo,String goodscode,int price){
            this.t_selectedTrackNo = "0"+(selectedTrackNo.length()==1?"0"+selectedTrackNo:selectedTrackNo);
            this.t_goodscode = goodscode;
            this.t_pricefen = price;
        }

        public void run(){
            alipaythreadisrunning = true;
            String temp_outtradeno =  "";

            LogUtils.i("开始支付宝二维码支付处理线程");

            String qrcodestr = "联网失败";  // 返回二维码字符串
            //String checkstre = ""; // 商户版的支付
            boolean alipayCodeReady = false;

            queuealipay.clear();

            long timestamp = System.currentTimeMillis();

            String str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+ "&price="+t_pricefen+"&track="+t_selectedTrackNo+
                    "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
            String md5 = MD5.GetMD5Code(str);

            String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/alipayqrcode",
                    "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                            "&timestamp="+timestamp  + "&md5=" + md5);

            LogUtils.i(  "连接服务后台第一次，支付宝二维码准备，返回结果：" + rtnstr);

            //{"msg":"","twocode":"https://qr.alipay.com/bax00822jurjljgvdwg0806e",
            // "checkstr":"_input_charset=utf-8&alipay_ca_request=2&out_trade_no=000000011111A0620170223190738&partner=2088511624563106&service=alipay.acquire.query&sign=b7c2e77cf8e3ece3724f2c95bbb1375d&sign_type=MD5",
            // "code":"1",
            // "checkstre":""}

            if(isAlreadyPayOrCancel){
                alipaythreadisrunning = false;
                return;  // 当用户主动切换支付方式时，则结束线程
            }

            if(rtnstr.length() > 0){
                // 说明和后台联网正常的
                try {
                    JSONObject soapJson = new JSONObject(rtnstr);
                    if (soapJson.getString("code").equals("0")) {
                        // 取到了正确的结果啦
                        alipayCodeReady = true;
                        qrcodestr = soapJson.getString("qrcode");
                        temp_outtradeno = soapJson.getString("outtradeno");

                        AlipayCheckStr = soapJson.getString("checkstre");
                    } else {
                        qrcodestr = soapJson.getString("msg");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                timestamp = System.currentTimeMillis();

                str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                        "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                md5 = MD5.GetMD5Code(str);

                rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/alipayqrcode",
                        "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                                "&timestamp="+timestamp  + "&md5=" + md5);

                LogUtils.i(  "连接服务后台第二次，支付宝二维码准备，返回结果：" + rtnstr);

                if(isAlreadyPayOrCancel) {
                    alipaythreadisrunning = false;
                    return;  // 当用户主动切换支付方式时，则结束线程
                }

                if(rtnstr.length() > 0){
                    // 说明和后台联网正常的
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        if (soapJson.getString("code").equals("0")) {
                            // 取到了正确的结果啦
                            alipayCodeReady = true;
                            qrcodestr = soapJson.getString("qrcode");
                            temp_outtradeno = soapJson.getString("outtradeno");

                            AlipayCheckStr = soapJson.getString("checkstre");
                        } else {
                            qrcodestr = soapJson.getString("msg");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(alipayCodeReady){
                LogUtils.v( "alipaypaystr:[" + qrcodestr + "]");


                // 要显示该二维码
                LogUtils.i( " 支付宝扫码时，取得二维码内容了");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_alipaycode_readyok;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);


                String AlipayCodeCustomerOpenID = "";
                boolean ispayOK = false;

                if(AlipayCheckStr.length() > 0) {
                    LogUtils.i("------支付宝V4------isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                    while (!isAlreadyPayOrCancel && !ispayOK) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (isAlreadyPayOrCancel) {
                            LogUtils.i("------支付宝V4--2----isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                            alipaythreadisrunning = false;
                            return;  // 当用户主动切换支付方式时，则结束线程
                        }
                        String temp_alipaypush = "";
                        if (paycountdown > 10 && paycountdown % 10 != 0) {
                            String alipaypush = queuealipay.poll();
                            // 支付宝alipay：商户交易号,支付宝交易号,支付者buyer_logon_id  最前面加track,最后加上分
                            if (alipaypush == null || alipaypush.length() == 0) {
                                continue;
                            } else {
                                LogUtils.i("---收到推送alipay:" + alipaypush);
                                temp_alipaypush = alipaypush;
                            }
                        }

                        // 为了提高速度：直接推送，就出货啦
                        if (temp_outtradeno.length() > 0 && temp_alipaypush.length() > 0) {
                            String[] temp = temp_alipaypush.split(",");
                            if (temp.length == 5 && temp[1].equals(temp_outtradeno)) {
                                LogUtils.i("收到支付宝推送信息和现在相符！");
                                ispayOK = true;
                                // 商户交易号,支付宝交易号,支付者buyer_logon_id,分
                                AlipayCodeCustomerOpenID = temp_outtradeno + "," + temp[2] + ","
                                        + temp[3] + "," + temp[4];

                            }
                        }

                        if (!ispayOK) {

                            String soaprtn = (new MyHttp()).post("https://openapi.alipay.com/gateway.do", AlipayCheckStr);
                            Log.i("alipay2=", soaprtn);
                            if (isAlreadyPayOrCancel) {
                                alipaythreadisrunning = false;
                                return;  // 当用户主动切换支付方式时，则结束线程
                            }

                            if (soaprtn.length() > 0) {  // 表示有返回的啦
                                try {
                                    JSONObject soapJson = new JSONObject(soaprtn);
                                    String alipay_trade_query_response = soapJson.getString("alipay_trade_query_response");
                                    JSONObject responseJson = new JSONObject(alipay_trade_query_response);
                                    String msg2 = responseJson.getString("msg");
                                    //LogUtils.i( "轮询结果msg2：" + msg2);
                                    if (msg2.equals("Success")) {
                                        String end = responseJson.getString("trade_status");
                                        LogUtils.i("轮询结果end：" + end);
                                        if (end.equals("TRADE_SUCCESS")) {
                                            // {"alipay_trade_query_response":
                                            // {"code":"10000","msg":"Success","buyer_logon_id":"ten***@139.com","buyer_pay_amount":"0.01","buyer_user_id":"2088002093332363",
                                            // "fund_bill_list":[{"amount":"0.01","fund_channel":"ALIPAYACCOUNT"}],
                                            // "invoice_amount":"0.01","out_trade_no":"0test201708000120171019154304003","point_amount":"0.00",
                                            // "receipt_amount":"0.01","send_pay_date":"2017-10-19 15:44:29","total_amount":"0.01",
                                            // "trade_no":"2017101921001004360294517606","trade_status":"TRADE_SUCCESS"},
                                            // "sign":"Gg41Um7DFAighrOYPJZ1jR4YJijUkLxNeEzwVZW7tAAkLQROBrKaBTme965GHmgAsMHiA+dYWwTqD9d8p50PhQ0SodcE3xj6vRHJe0izq/r+79SyQaUad+ODx8ljxt6PTF4onl3+dI8seHcd7ewl1Q7A6BlpLCpty6bKgqDl4Tnho207Si/A7iWQzDSLd6vKreniOrhXPITbZOTeucarnVMFk1vhkSL/fr5T+6Z642NglMyTt2CCL/mXX+p58Xlx7HhxNwVMR4qlmetuqX06AxUgSxTQdsq2xNJOVbpbKxYXcjYs6se8TR1LOLumylYJ3poEB08sJdEWNBkitM5T9Q=="}
                                            ispayOK = true;
                                            // 商户交易号,支付宝交易号,支付者buyer_logon_id,分
                                            AlipayCodeCustomerOpenID = responseJson.getString("out_trade_no") + "," + responseJson.getString("trade_no") + ","
                                                    + responseJson.getString("buyer_logon_id") + "," + Gmethod.tranYuanToFen(responseJson.getString("total_amount"));

                                            LogUtils.i("------支付宝V4------" + AlipayCodeCustomerOpenID);
                                        }
                                    } else {
                                        if (temp_alipaypush.length() > 0) {
                                            // 说明有推送过来的，那么有可能是上一个二维码的，要启动退款流程。
                                            // 023,000829AVM00006620180720070348023,2018072021001004360537932631,jie***@yahoo.com.cn,600
                                            String outtradeno = "";
                                            int fen = 0;
                                            if (temp_alipaypush.split(",").length == 5) {
                                                outtradeno = temp_alipaypush.split(",")[1];
                                                fen = Integer.parseInt(temp_alipaypush.split(",")[4]);
                                            }
                                            try {
                                                Thread.sleep(1000);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (lp != null && outtradeno.length() > 0) {
                                                lp.refund_alipay(outtradeno, fen);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {

                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                            Message msg1 = dispHandler.obtainMessage();
                            msg1.what = dispHandler_alipaycode_readcount;
                            if (soaprtn.length() > 0)
                                msg1.obj = "";
                            else
                                msg1.obj = "" + "(网络异常)";
                            dispHandler.sendMessage(msg1);
                        }
                    }
                }

                if(ispayOK){
                    AlipayCheckStr = "";
                    //  支付宝扫码支付成功了，要开始出货啦
                    Message msg2 = dispHandler.obtainMessage();
                    msg2.what = dispHandler_alipaycode_payok;
                    msg2.obj = AlipayCodeCustomerOpenID;
                    dispHandler.sendMessage(msg2);

                }

            } else {

                // 联网失败，提示客户用现金付款
                LogUtils.i( " 支付宝扫码准备时联网失败");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_alipaycode_readyng_nonet;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);

            }

            alipaythreadisrunning = false;
        }
    }

    // 中信银行的支付宝二维码支付的线程
    private class ZXAlipayCodeThread_Self extends Thread{
        // 用户选择的轨道编号（10，，，A0，A1，，，K0，，，）
        private String t_selectedTrackNo = "";
        private String t_goodscode = "";
        private int t_pricefen = 1;

        private ZXAlipayCodeThread_Self(String selectedTrackNo,String goodscode,int price){
            this.t_selectedTrackNo = "0"+(selectedTrackNo.length()==1?"0"+selectedTrackNo:selectedTrackNo);
            this.t_goodscode = goodscode;
            this.t_pricefen = price;
        }

        public void run(){
            alipaythreadisrunning = true;

            LogUtils.i("开始中信银行的支付宝二维码支付处理线程");

            String qrcodestr = "联网失败";  // 返回二维码字符串

            boolean alipayCodeReady = false;

            queuealipay.clear();

            long timestamp = System.currentTimeMillis();

            String str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+ "&price="+t_pricefen+"&track="+t_selectedTrackNo+
                    "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
            String md5 = MD5.GetMD5Code(str);

            String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/zxalipayqrcode",
                    "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                            "&timestamp="+timestamp  + "&md5=" + md5);

            LogUtils.i(  "连接服务后台第一次，中信银行的支付宝二维码准备，返回结果：" + rtnstr);

            if(isAlreadyPayOrCancel){
                alipaythreadisrunning = false;
                return;  // 当用户主动切换支付方式时，则结束线程
            }

            if(rtnstr.length() > 0){
                // 说明和后台联网正常的
                try {
                    JSONObject soapJson = new JSONObject(rtnstr);
                    if (soapJson.getString("code").equals("0")) {
                        // 取到了正确的结果啦
                        alipayCodeReady = true;
                        qrcodestr = soapJson.getString("qrcode");
                        queryzxalipayqueryurl = soapJson.getString("queryurl");

                        AlipayCheckStr = soapJson.getString("checkstre");
                    } else {
                        qrcodestr = soapJson.getString("msg");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                timestamp = System.currentTimeMillis();

                str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                        "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                md5 = MD5.GetMD5Code(str);

                rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/zxalipayqrcode",
                        "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                                "&timestamp="+timestamp  + "&md5=" + md5);

                LogUtils.i(  "连接服务后台第二次，中信银行的支付宝二维码准备，返回结果：" + rtnstr);

                if(isAlreadyPayOrCancel) {
                    alipaythreadisrunning = false;
                    return;  // 当用户主动切换支付方式时，则结束线程
                }

                if(rtnstr.length() > 0){
                    // 说明和后台联网正常的
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        if (soapJson.getString("code").equals("0")) {
                            // 取到了正确的结果啦
                            alipayCodeReady = true;
                            qrcodestr = soapJson.getString("qrcode");
                            queryzxalipayqueryurl = soapJson.getString("queryurl");

                            AlipayCheckStr = soapJson.getString("checkstre");
                        } else {
                            qrcodestr = soapJson.getString("msg");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(alipayCodeReady){
                //LogUtils.v( "alipaypaystr:[" + qrcodestr + "]");


                // 要显示该二维码
                //LogUtils.i( " 支付宝扫码时，取得二维码内容了");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_alipaycode_readyok;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);


                String AlipayCodeCustomerOpenID = "";
                boolean ispayOK = false;

                if(AlipayCheckStr.length() > 0) {
                    //LogUtils.i("------支付宝V4------isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                    while (!isAlreadyPayOrCancel && !ispayOK) {
                        try{Thread.sleep(500);}catch (Exception e){e.printStackTrace();}
                        if (isAlreadyPayOrCancel) {
                            //LogUtils.i("------支付宝V4--2----isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                            alipaythreadisrunning = false;
                            return;  // 当用户主动切换支付方式时，则结束线程
                        }
                        String temp_alipaypush = "";
                        if(paycountdown > 10 && paycountdown%10 != 0) {
                            String alipaypush = queuealipay.poll();
                            // 支付宝alipay：商户交易号,支付宝交易号,支付者buyer_logon_id  最前面加track,最后加上分
                            if (alipaypush == null || alipaypush.length() == 0) {
                                continue;
                            } else {
                                LogUtils.i("---收到推送alipay:" + alipaypush);
                                temp_alipaypush = alipaypush;
                            }
                        }

                        String soaprtn = (new MyHttp()).post(queryzxalipayqueryurl, AlipayCheckStr);
                        //LogUtils.i( "alipay2=" + soaprtn);
                        if (isAlreadyPayOrCancel) {
                            alipaythreadisrunning = false;
                            return;  // 当用户主动切换支付方式时，则结束线程
                        }

                        if (soaprtn.length() > 9) {  // 表示有返回的啦 sendData=
                            try {
                                String respB64Str = soaprtn.substring(9);
                                String jsonstr = getFromBase64(respB64Str.replaceAll("#","+"));
                                //Log.i(TAG,jsonstr);

                                JSONObject responseJson = new JSONObject(jsonstr);
                                String respCode = responseJson.getString("respCode");
                                String txnState = responseJson.getString("txnState");
                                String respMsg = responseJson.getString("respMsg");
                                if(respCode.equals("0000") && txnState.equals("00")){
                                    // 表示交易成功了
                                    ispayOK = true;
                                    // 商户交易号,支付宝交易号,支付者buyer_logon_id,分
                                    AlipayCodeCustomerOpenID = responseJson.getString("origSeqId")
                                            + "," + responseJson.getString("origZfbId")
                                            + "," + responseJson.getString("orderOpenId")
                                            + "," + responseJson.getString("txnAmt");

                                } else {
                                    if(temp_alipaypush.length()>0){
                                        // 说明有推送过来的，那么有可能是上一个二维码的，要启动退款流程。
                                        // 023,000829AVM00006620180720070348023,2018072021001004360537932631,jie***@yahoo.com.cn,600
                                        String outtradeno = "";
                                        int fen = 0;
                                        if(temp_alipaypush.split(",").length == 5){
                                            outtradeno = temp_alipaypush.split(",")[1];
                                            fen = Integer.parseInt(temp_alipaypush.split(",")[4]);
                                        }
                                        try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
                                        if(lp != null && outtradeno.length()>0){
                                            lp.refund_zxalipay(outtradeno,fen);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
                        }

                        Message msg1 = dispHandler.obtainMessage();
                        msg1.what = dispHandler_alipaycode_readcount;
                        if(soaprtn.length() > 0)
                            msg1.obj = "" ;
                        else
                            msg1.obj = "" +"(网络异常)";
                        dispHandler.sendMessage(msg1);
                    }
                }

                if(ispayOK){
                    AlipayCheckStr = "";
                    //  支付宝扫码支付成功了，要开始出货啦
                    Message msg2 = dispHandler.obtainMessage();
                    msg2.what = dispHandler_alipaycode_payok;
                    msg2.obj = AlipayCodeCustomerOpenID;
                    dispHandler.sendMessage(msg2);

                }

            } else {

                // 联网失败，提示客户用现金付款
                LogUtils.i( " 支付宝扫码准备时联网失败");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_alipaycode_readyng_nonet;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);

            }

            alipaythreadisrunning = false;
        }
    }

    // 微信处理的线程
    private class WeixinThread_Self extends Thread{
        // 用户选择的轨道编号（10，，，A0，A1，，，K0，，，）
        private String t_selectedTrackNo = "";
        private String t_goodscode = "";
        private int t_pricefen = 1;

        /**
         *
         * @param selectedTrackNo 用户选择的轨道编号（10，，，A0，A1，，，K0，，，）
         */
        private WeixinThread_Self(String selectedTrackNo,String goodscode,int price) {
            this.t_selectedTrackNo = "0"+(selectedTrackNo.length()==1?"0"+selectedTrackNo:selectedTrackNo);;
            this.t_goodscode = goodscode;
            this.t_pricefen = price;
        }

        public void run(){
            weixinthreadisrunning = true;
            String temp_out_trade_no = "";

            LogUtils.i( "开始微信处理线程");

            String qrcodestr = "联网失败";  // 返回二维码字符串
            //String checkstr = "";
            boolean weixinCodeReady = false;

            queueweixin.clear();

            long timestamp = System.currentTimeMillis();

            String str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                    "&timestamp="+timestamp  + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
            String md5 = MD5.GetMD5Code(str);

            String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/wxpayqrcode",
                    "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                            "&timestamp="+timestamp + "&md5=" + md5);

            LogUtils.i( "连接服务后台第一次，微信准备，返回结果：" + rtnstr);
            //{"msg":"","checkstr":"<xml><appid>wx6d37f5d05a8d301f<\/appid><mch_id>1219661201<\/mch_id><nonce_str>PQaIr3yc37pFoVHp<\/nonce_str><out_trade_no>000000011111A0720170223193353<\/out_trade_no><sign>B8FC93D8717B61D9F8618FBF3592FB5A<\/sign><\/xml>","code":"1","twocode":"weixin://wxpay/bizpayurl?pr=OJuQQz3"}

            if(isAlreadyPayOrCancel) {
                weixinthreadisrunning = false;
                return;  // 当用户主动切换支付方式时，则结束线程
            }

            if(rtnstr.length() > 0){
                try {
                    JSONObject soapJson = new JSONObject(rtnstr);
                    if (soapJson.getString("code").equals("0")) {
                        // 取到了正确的结果啦
                        weixinCodeReady = true;
                        qrcodestr = soapJson.getString("qrcode");
                        temp_out_trade_no = soapJson.getString("outtradeno");

                        WxpayCheckStr = soapJson.getString("selfcheck");
                    } else {
                        qrcodestr = soapJson.getString("msg");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {
                // 再试一次
                timestamp = System.currentTimeMillis();

                str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                        "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                md5 = MD5.GetMD5Code(str);

                rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/wxpayqrcode",
                        "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                                "&timestamp="+timestamp  + "&md5=" + md5);

                LogUtils.i(  "连接服务后台第二次，微信准备，返回结果：" + rtnstr);

                if(isAlreadyPayOrCancel) {
                    weixinthreadisrunning = false;
                    return;  // 当用户主动切换支付方式时，则结束线程
                }

                if(rtnstr.length() > 0){
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        if (soapJson.getString("code").equals("0")) {
                            // 取到了正确的结果啦
                            weixinCodeReady = true;
                            qrcodestr = soapJson.getString("qrcode");
                            temp_out_trade_no = soapJson.getString("outtradeno");

                            WxpayCheckStr = soapJson.getString("selfcheck");
                        } else {
                            qrcodestr = soapJson.getString("msg");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(weixinCodeReady){

                LogUtils.v( "weixinpaystr:[" + qrcodestr + "]");

                // 要显示该二维码
                LogUtils.i( " 微信准备时联网成功");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_weixin_readyok;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);

                //SystemClock.sleep(10000);

                String WeixinCustomerOpenID = "";
                boolean ispayOK = false;

                if(WxpayCheckStr.length() > 0) {
                    LogUtils.i("------微信self------isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                    while (!isAlreadyPayOrCancel && !ispayOK) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (isAlreadyPayOrCancel) {
                            LogUtils.i("------微信self--2----isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                            weixinthreadisrunning = false;
                            return;  // 当用户主动切换支付方式时，则结束线程
                        }
                        String temp_wxpaypush = "";
                        if (paycountdown > 10 && paycountdown % 10 != 0) {
                            String weixinpush = queueweixin.poll();
                            // 微信wxpay：商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注)     最前面加track,最后加上分
                            if (weixinpush == null || weixinpush.length() == 0) {
                                continue;
                            } else {
                                LogUtils.i("---收到推送weixinpush:" + weixinpush);
                                temp_wxpaypush = weixinpush;
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // track+","+out_trade_no+","+transaction_id+","+openid+","+appid+","+(is_subscribe.equals("Y")?"1":"0")+","+total_fee);
                        // 为了提高速度：直接推送，就出货啦
                        if (temp_out_trade_no.length() > 0 && temp_wxpaypush.length() > 0) {
                            String[] temp = temp_wxpaypush.split(",");
                            if (temp.length == 7 && temp[1].equals(temp_out_trade_no)) {
                                LogUtils.i("收到微信推送信息和现在相符！");
                                ispayOK = true;
                                WeixinCustomerOpenID = temp_out_trade_no + "," +
                                        temp[2] + "," +
                                        temp[3] + "," +
                                        temp[4] + "," +
                                        temp[5] + "," + temp[6];

                            }
                        }

                        if (!ispayOK) {

                            String soaprtn = (new MyHttp()).post("https://api.mch.weixin.qq.com/pay/orderquery", WxpayCheckStr);

                            if (isAlreadyPayOrCancel) {
                                weixinthreadisrunning = false;
                                return;  // 当用户主动切换支付方式时，则结束线程
                            }

                            if (soaprtn.length() > 0) {  // 表示有返回的啦
                                String return_code = Gmethod.xuan_zhi(soaprtn, "<return_code><![CDATA[", "]]></return_code>");

                                if (return_code.equals("SUCCESS")) {
                                    String result_code = Gmethod.xuan_zhi(soaprtn, "<result_code><![CDATA[", "]]></result_code>");
                                    if (result_code.equals("SUCCESS")) {
                                        String endresult = Gmethod.xuan_zhi(soaprtn, "<trade_state><![CDATA[", "]]></trade_state>");
                                        LogUtils.i("轮询结果1：" + endresult);
                                        if (endresult.equals("SUCCESS")) {
                                            //<xml>
                                            // <return_code><![CDATA[SUCCESS]]></return_code>
                                            // <return_msg><![CDATA[OK]]></return_msg>
                                            // <appid><![CDATA[wx09587b46d66adac6]]></appid>
                                            // <mch_id><![CDATA[1366646502]]></mch_id>
                                            // <nonce_str><![CDATA[XhNXwFTVQXlgiWHg]]></nonce_str>
                                            // <sign><![CDATA[B77054EC5515B277932EDE86551503CE]]></sign>
                                            // <result_code><![CDATA[SUCCESS]]></result_code>
                                            // <openid><![CDATA[oDyIJwmLIFVZrrWXO7WekwYBkdCs]]></openid>
                                            // <is_subscribe><![CDATA[Y]]></is_subscribe>
                                            // <trade_type><![CDATA[NATIVE]]></trade_type>
                                            // <bank_type><![CDATA[CFT]]></bank_type>
                                            // <total_fee>1</total_fee>
                                            // <fee_type><![CDATA[CNY]]></fee_type>
                                            // <transaction_id><![CDATA[4200000002201710199011315664]]></transaction_id>
                                            // <out_trade_no><![CDATA[0test201708000120171019153643003]]></out_trade_no>
                                            // <attach><![CDATA[test2017080001_003_11004]]></attach>
                                            // <time_end><![CDATA[20171019153801]]></time_end>
                                            // <trade_state><![CDATA[SUCCESS]]></trade_state>
                                            // <cash_fee>1</cash_fee>
                                            // </xml>

                                            //LogUtils.i( "Weixin---------------OK--------------" + xuan_zhi(soaprtn, "<transaction_id><![CDATA[", "]]></transaction_id>"));
                                            ispayOK = true;
                                            // 商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注),fen
                                            WeixinCustomerOpenID = Gmethod.xuan_zhi(soaprtn, "<out_trade_no><![CDATA[", "]]></out_trade_no>") + "," +
                                                    Gmethod.xuan_zhi(soaprtn, "<transaction_id><![CDATA[", "]]></transaction_id>") + "," +
                                                    Gmethod.xuan_zhi(soaprtn, "<openid><![CDATA[", "]]></openid>") + "," +
                                                    Gmethod.xuan_zhi(soaprtn, "<appid><![CDATA[", "]]></appid>") + "," +
                                                    (Gmethod.xuan_zhi(soaprtn, "<is_subscribe><![CDATA[", "]]></is_subscribe>").equals("Y") ? "1" : "0")
                                                    + "," + Gmethod.xuan_zhi(soaprtn, "<cash_fee>", "</cash_fee>");
                                            LogUtils.i("------微信self------" + WeixinCustomerOpenID);
                                        } else {
                                            if (temp_wxpaypush.length() > 0) {
                                                // 说明有推送过来的，那么有可能是上一个二维码的，要启动退款流程。
                                                // 018,00000881800000620180628115839018,4200000142201806281057793980,ozkLOv0k4rfOvEW3uthqXjGiyJN4,wx4f51327af3699869,1,150
                                                String outtradeno = "";
                                                int fen = 0;
                                                if (temp_wxpaypush.split(",").length == 7) {
                                                    outtradeno = temp_wxpaypush.split(",")[1];
                                                    fen = Integer.parseInt(temp_wxpaypush.split(",")[6]);
                                                }
                                                try {
                                                    Thread.sleep(1000);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                if (lp != null && outtradeno.length() > 0) {
                                                    lp.refund_wxpay(outtradeno, fen);
                                                }
                                            }
                                        }

                                    }
                                    //else {
                                    //LogUtils.i( "weixin轮询结果2：" + "err_code:" + xuan_zhi(soaprtn, "<err_code><![CDATA[", "]]></err_code>"));
                                    //}
                                }
                                //else {
                                //LogUtils.i( "weixin轮询结果3：" + xuan_zhi(soaprtn, "<return_msg><![CDATA[", "]]></return_msg>"));
                                //}

                            } else {

                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            Message msg1 = dispHandler.obtainMessage();
                            msg1.what = dispHandler_weixin_readcount;
                            if (soaprtn.length() > 0)
                                msg1.obj = "";
                            else
                                msg1.obj = "" + "(网络异常)";
                            dispHandler.sendMessage(msg1);
                        }
                    }
                }

                if(ispayOK){
                    WxpayCheckStr = "";
                    //  微信支付成功了，要开始出货啦
                    Message msg2 = dispHandler.obtainMessage();
                    msg2.what = dispHandler_weixin_payok;
                    msg2.obj = WeixinCustomerOpenID;
                    dispHandler.sendMessage(msg2);

                }

            } else {
                // 联网失败，提示客户用现金付款
                LogUtils.i( " 微信准备时联网失败");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_weixin_readyng_nonet;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);
            }

            weixinthreadisrunning = false;
        }
    }


    // 微信处理的线程
    private class ZXWeixinThread_Self extends Thread{
        // 用户选择的轨道编号（10，，，A0，A1，，，K0，，，）
        private String t_selectedTrackNo = "";
        private String t_goodscode = "";
        private int t_pricefen = 1;

        /**
         *
         * @param selectedTrackNo 用户选择的轨道编号（10，，，A0，A1，，，K0，，，）
         */
        private ZXWeixinThread_Self(String selectedTrackNo,String goodscode,int price) {
            this.t_selectedTrackNo = "0"+(selectedTrackNo.length()==1?"0"+selectedTrackNo:selectedTrackNo);;
            this.t_goodscode = goodscode;
            this.t_pricefen = price;
        }

        public void run(){
            weixinthreadisrunning = true;

            LogUtils.i( "开始中信银行的微信处理线程");

            String qrcodestr = "联网失败";  // 返回二维码字符串

            boolean weixinCodeReady = false;

            queueweixin.clear();

            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

            long timestamp = System.currentTimeMillis();

            String str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                    "&timestamp="+timestamp  + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
            String md5 = MD5.GetMD5Code(str);

            String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/zxwxpayqrcode",
                    "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                            "&timestamp="+timestamp + "&md5=" + md5);

            LogUtils.i( "连接服务后台第一次，微信准备，返回结果：" + rtnstr);
            //{"msg":"","checkstr":"<xml><appid>wx6d37f5d05a8d301f<\/appid><mch_id>1219661201<\/mch_id><nonce_str>PQaIr3yc37pFoVHp<\/nonce_str><out_trade_no>000000011111A0720170223193353<\/out_trade_no><sign>B8FC93D8717B61D9F8618FBF3592FB5A<\/sign><\/xml>","code":"1","twocode":"weixin://wxpay/bizpayurl?pr=OJuQQz3"}

            if(isAlreadyPayOrCancel) {
                weixinthreadisrunning = false;
                return;  // 当用户主动切换支付方式时，则结束线程
            }

            if(rtnstr.length() > 0){
                try {
                    JSONObject soapJson = new JSONObject(rtnstr);
                    if (soapJson.getString("code").equals("0")) {
                        // 取到了正确的结果啦
                        weixinCodeReady = true;
                        qrcodestr = soapJson.getString("qrcode");
                        queryzxwxpayqueryurl = soapJson.getString("queryurl");
                        WxpayCheckStr = soapJson.getString("selfcheck");
                    } else {
                        qrcodestr = soapJson.getString("msg");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {
                // 再试一次
                timestamp = System.currentTimeMillis();

                str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                        "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                md5 = MD5.GetMD5Code(str);

                rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/zxwxpayqrcode",
                        "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                                "&timestamp="+timestamp  + "&md5=" + md5);

                LogUtils.i(  "连接服务后台第二次，微信准备，返回结果：" + rtnstr);

                if(isAlreadyPayOrCancel) {
                    weixinthreadisrunning = false;
                    return;  // 当用户主动切换支付方式时，则结束线程
                }

                if(rtnstr.length() > 0){
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        if (soapJson.getString("code").equals("0")) {
                            // 取到了正确的结果啦
                            weixinCodeReady = true;
                            qrcodestr = soapJson.getString("qrcode");
                            queryzxwxpayqueryurl = soapJson.getString("queryurl");
                            WxpayCheckStr = soapJson.getString("selfcheck");
                        } else {
                            qrcodestr = soapJson.getString("msg");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(weixinCodeReady){

                //LogUtils.v( "weixinpaystr:[" + qrcodestr + "]");

                // 要显示该二维码
                //LogUtils.i( " 微信准备时联网成功");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_weixin_readyok;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);

                //SystemClock.sleep(10000);

                String WeixinCustomerOpenID = "";
                boolean ispayOK = false;

                if(WxpayCheckStr.length() > 0) {
                    //LogUtils.i("------微信self------isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                    while (!isAlreadyPayOrCancel && !ispayOK) {
                        try{Thread.sleep(500);}catch (Exception e){e.printStackTrace();}
                        if (isAlreadyPayOrCancel) {
                            //LogUtils.i("------微信self--2----isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                            weixinthreadisrunning = false;
                            return;  // 当用户主动切换支付方式时，则结束线程
                        }

                        String temp_wxpaypush = "";
                        if(paycountdown > 10 && paycountdown%10 != 0) {
                            String weixinpush = queueweixin.poll();
                            // 微信wxpay：商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注)     最前面加track,最后加上分
                            if (weixinpush == null || weixinpush.length() == 0) {
                                continue;
                            } else {
                                temp_wxpaypush = weixinpush;
                                LogUtils.i("---收到推送weixinpush:" + weixinpush);
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        String soaprtn = (new MyHttp()).post(queryzxwxpayqueryurl, WxpayCheckStr);

                        if (isAlreadyPayOrCancel) {
                            weixinthreadisrunning = false;
                            return;  // 当用户主动切换支付方式时，则结束线程
                        }

                        if (soaprtn.length() > 9) {  // 表示有返回的啦 sendData=
                            try {
                                String respB64Str = soaprtn.substring(9);
                                String jsonstr = getFromBase64(respB64Str.replaceAll("#","+"));
                                //Log.i(TAG,jsonstr);

                                JSONObject responseJson = new JSONObject(jsonstr);
                                String origRespCode = responseJson.getString("origRespCode");
                                //String txnState = responseJson.getString("txnState");
                                //String respMsg = responseJson.getString("respMsg");
                                if(origRespCode.equals("SUCCESS")){
                                    // 表示交易成功了
                                    ispayOK = true;
                                    // 商户交易号,支付宝交易号,支付者buyer_logon_id,分
                                    WeixinCustomerOpenID = responseJson.getString("origSeqId")
                                            + "," + responseJson.getString("transactionId")
                                            + "," + responseJson.getString("orderOpenId")+",none,0"
                                            + "," + responseJson.getString("txnAmt");
                                    ispayOK = true;
                                    // 商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注),fen

                                } else {
                                    if(temp_wxpaypush.length()>0){
                                        try{Thread.sleep(2000);}catch (Exception e){e.printStackTrace();}
                                        // 说明有推送过来的，那么有可能是上一个二维码的，要启动退款流程。
                                        // 018,00000881800000620180628115839018,4200000142201806281057793980,ozkLOv0k4rfOvEW3uthqXjGiyJN4,wx4f51327af3699869,1,150
                                        String outtradeno = "";
                                        int fen = 0;
                                        if(temp_wxpaypush.split(",").length == 7){
                                            outtradeno = temp_wxpaypush.split(",")[1];
                                            fen = Integer.parseInt(temp_wxpaypush.split(",")[6]);
                                        }
                                        try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
                                        if(lp != null && outtradeno.length()>0){
                                            lp.refund_zxwxpay(outtradeno,fen);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
                        }

                        Message msg1 = dispHandler.obtainMessage();
                        msg1.what = dispHandler_weixin_readcount;
                        if (soaprtn.length() > 0)
                            msg1.obj = "";
                        else
                            msg1.obj = "" + "(网络异常)";
                        dispHandler.sendMessage(msg1);
                    }
                }

                if(ispayOK){
                    WxpayCheckStr = "";
                    //  微信支付成功了，要开始出货啦
                    Message msg2 = dispHandler.obtainMessage();
                    msg2.what = dispHandler_weixin_payok;
                    msg2.obj = WeixinCustomerOpenID;
                    dispHandler.sendMessage(msg2);

                }

            } else {
                // 联网失败，提示客户用现金付款
                LogUtils.i( " 微信准备时联网失败");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_weixin_readyng_nonet;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);
            }

            weixinthreadisrunning = false;
        }
    }

    // 银联二维码支付的线程
    private class UmspayCodeThread_Self extends Thread{
        // 用户选择的轨道编号（10，，，A0，A1，，，K0，，，）
        private String t_selectedTrackNo = "";
        private String t_goodscode = "";
        private int t_pricefen = 1;

        private UmspayCodeThread_Self(String selectedTrackNo,String goodscode,int price){
            this.t_selectedTrackNo = "0"+(selectedTrackNo.length()==1?"0"+selectedTrackNo:selectedTrackNo);
            this.t_goodscode = goodscode;
            this.t_pricefen = price;
        }

        public void run(){
            umspaythreadisrunning = true;

            LogUtils.i("开始银联二维码支付处理线程");

            String qrcodestr = "联网失败";  // 返回二维码字符串
            //String checkstre = ""; // 商户版的支付
            boolean umspayCodeReady = false;

            queueumspay.clear();

            long timestamp = System.currentTimeMillis();

            String str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+ "&price="+t_pricefen+"&track="+t_selectedTrackNo+
                    "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
            String md5 = MD5.GetMD5Code(str);

            String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/umspayqrcode",
                    "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                            "&timestamp="+timestamp  + "&md5=" + md5);

            LogUtils.i(  "连接服务后台第一次，银联二维码准备，返回结果：" + rtnstr);


            if(isAlreadyPayOrCancel){
                umspaythreadisrunning = false;
                return;  // 当用户主动切换支付方式时，则结束线程
            }

            if(rtnstr.length() > 0){
                // 说明和后台联网正常的
                try {
                    JSONObject soapJson = new JSONObject(rtnstr);
                    if (soapJson.getString("code").equals("0")) {
                        // 取到了正确的结果啦
                        umspayCodeReady = true;
                        qrcodestr = soapJson.getString("billQRCode");

                        UmspayCheckStr = soapJson.getString("querystr");
                    } else {
                        qrcodestr = soapJson.getString("msg");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                timestamp = System.currentTimeMillis();

                str = "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                        "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                md5 = MD5.GetMD5Code(str);

                rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/umspayqrcode",
                        "goodscode="+t_goodscode+"&macid="+ ((MyApp)getApplication()).getMainMacID()+"&price="+t_pricefen+"&track="+t_selectedTrackNo+
                                "&timestamp="+timestamp  + "&md5=" + md5);

                LogUtils.i(  "连接服务后台第二次，银联二维码准备，返回结果：" + rtnstr);

                if(isAlreadyPayOrCancel) {
                    umspaythreadisrunning = false;
                    return;  // 当用户主动切换支付方式时，则结束线程
                }

                if(rtnstr.length() > 0){
                    // 说明和后台联网正常的
                    try {
                        JSONObject soapJson = new JSONObject(rtnstr);
                        if (soapJson.getString("code").equals("0")) {
                            // 取到了正确的结果啦
                            umspayCodeReady = true;
                            qrcodestr = soapJson.getString("billQRCode");
                            UmspayCheckStr = soapJson.getString("querystr");
                        } else {
                            qrcodestr = soapJson.getString("msg");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(umspayCodeReady){
                LogUtils.v( "umspaypaystr:[" + qrcodestr + "]");


                // 要显示该二维码
                LogUtils.i( " 银联扫码时，取得二维码内容了");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_umspaycode_readyok;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);


                String UmspayCodeCustomerOpenID = "";
                boolean ispayOK = false;

                if(UmspayCheckStr.length() > 0) {
                    LogUtils.i("------银联二维码------isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                    while (!isAlreadyPayOrCancel && !ispayOK) {
                        try{Thread.sleep(500);}catch (Exception e){e.printStackTrace();}
                        if (isAlreadyPayOrCancel) {
                            LogUtils.i("------银联二维码--2----isAlreadyPayOrCancel=" + isAlreadyPayOrCancel);
                            umspaythreadisrunning = false;
                            return;  // 当用户主动切换支付方式时，则结束线程
                        }
                        if(paycountdown > 10 && paycountdown%10 != 0) {
                            String umspaypush = queueumspay.poll();
                            // 支付宝alipay：商户交易号,支付宝交易号,支付者buyer_logon_id  最前面加track,最后加上分
                            if (umspaypush == null || umspaypush.length() == 0) {
                                continue;
                            } else {
                                LogUtils.i("---收到推送umspay:" + umspaypush);
                            }
                        }

                        timestamp = System.currentTimeMillis();
                        // 强制去查询支付状态
                        str = "macid="+ ((MyApp)getApplication()).getMainMacID()+"&querystr="+UmspayCheckStr+
                                "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                        md5 = MD5.GetMD5Code(str);

                        String soaprtn = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/umspayquery",
                                "macid="+ ((MyApp)getApplication()).getMainMacID()+"&querystr="+UmspayCheckStr+
                                        "&timestamp="+timestamp  + "&md5=" + md5);

                        if (isAlreadyPayOrCancel) {
                            umspaythreadisrunning = false;
                            return;  // 当用户主动切换支付方式时，则结束线程
                        }

                        if (soaprtn.length() > 0) {  // 表示有返回的啦
                            try {
                                JSONObject soapJson = new JSONObject(soaprtn);
                                String billstatus = soapJson.getString("billstatus");

                                if (billstatus.equals("PAID")) {

                                    UmspayCodeCustomerOpenID = soapJson.getString("info");
                                    ispayOK = true;

                                    LogUtils.i( "------银联二维码查询结果------" + UmspayCodeCustomerOpenID);


                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
                        }

                        Message msg1 = dispHandler.obtainMessage();
                        msg1.what = dispHandler_umspaycode_readcount;
                        if(soaprtn.length() > 0)
                            msg1.obj = "" ;
                        else
                            msg1.obj = "" +"(网络异常)";
                        dispHandler.sendMessage(msg1);
                    }
                }

                if(ispayOK){
                    AlipayCheckStr = "";
                    //  支付宝扫码支付成功了，要开始出货啦
                    Message msg2 = dispHandler.obtainMessage();
                    msg2.what = dispHandler_umspaycode_payok;
                    msg2.obj = UmspayCodeCustomerOpenID;
                    dispHandler.sendMessage(msg2);

                }

            } else {

                // 联网失败，提示客户用现金付款
                LogUtils.i( " 银联扫码准备时联网失败");
                Message msg = dispHandler.obtainMessage();
                msg.what = dispHandler_umspaycode_readyng_nonet;
                msg.obj = qrcodestr;
                dispHandler.sendMessage(msg);

            }

            umspaythreadisrunning = false;
        }
    }

    // 支付宝，微信处理的线程--后续收到个推的出货线程
    private class AlipayWxpayNext_Self extends Thread{
        public void run(){
            while(isAlipayWxpayWaitingGetuiRunning){
                // 等待0.5秒
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(!alipaythreadisrunning && !weixinthreadisrunning){

                    if(AlipayCheckStr.length() > 0  && ((MyApp)getApplication()).getHaveAlipay()==1){
                        String alipaypush = queuealipay.poll();
                        // 支付宝alipay：商户交易号,支付宝交易号,支付者buyer_logon_id  最前面加track,最后加上分
                        if (alipaypush != null && alipaypush.length() > 0) {
                            LogUtils.i("后续收到个推的出货线程---收到推送alipay:" + alipaypush);

                            String soaprtn = (new MyHttp()).post("https://openapi.alipay.com/gateway.do", AlipayCheckStr);
                            //LogUtils.i( "alipay2=" + soaprtn);
                            if (!alipaythreadisrunning && soaprtn.length() > 0) {  // 表示有返回的啦
                                try {
                                    JSONObject soapJson = new JSONObject(soaprtn);
                                    String alipay_trade_query_response = soapJson.getString("alipay_trade_query_response");
                                    JSONObject responseJson = new JSONObject(alipay_trade_query_response);
                                    String msg2 = responseJson.getString("msg");
                                    //LogUtils.i( "轮询结果msg2：" + msg2);
                                    if (msg2.equals("Success")) {
                                        String end = responseJson.getString("trade_status");
                                        LogUtils.i( "轮询结果end：" + end);
                                        if (end.equals("TRADE_SUCCESS")) {
                                            // {"alipay_trade_query_response":
                                            // {"code":"10000","msg":"Success","buyer_logon_id":"ten***@139.com","buyer_pay_amount":"0.01","buyer_user_id":"2088002093332363",
                                            // "fund_bill_list":[{"amount":"0.01","fund_channel":"ALIPAYACCOUNT"}],
                                            // "invoice_amount":"0.01","out_trade_no":"0test201708000120171019154304003","point_amount":"0.00",
                                            // "receipt_amount":"0.01","send_pay_date":"2017-10-19 15:44:29","total_amount":"0.01",
                                            // "trade_no":"2017101921001004360294517606","trade_status":"TRADE_SUCCESS"},
                                            // "sign":"Gg41Um7DFAighrOYPJZ1jR4YJijUkLxNeEzwVZW7tAAkLQROBrKaBTme965GHmgAsMHiA+dYWwTqD9d8p50PhQ0SodcE3xj6vRHJe0izq/r+79SyQaUad+ODx8ljxt6PTF4onl3+dI8seHcd7ewl1Q7A6BlpLCpty6bKgqDl4Tnho207Si/A7iWQzDSLd6vKreniOrhXPITbZOTeucarnVMFk1vhkSL/fr5T+6Z642NglMyTt2CCL/mXX+p58Xlx7HhxNwVMR4qlmetuqX06AxUgSxTQdsq2xNJOVbpbKxYXcjYs6se8TR1LOLumylYJ3poEB08sJdEWNBkitM5T9Q=="}

                                            // 商户交易号,支付宝交易号,支付者buyer_logon_id,分
                                            String AlipayCodeCustomerOpenID = responseJson.getString("out_trade_no")  + "," + responseJson.getString("trade_no") + ","
                                                    + responseJson.getString("buyer_logon_id")+","+Gmethod.tranYuanToFen(responseJson.getString("total_amount"));

                                            LogUtils.i( "------支付宝V4------" + AlipayCodeCustomerOpenID);

                                            AlipayCheckStr = "";
                                            willouttrack = Integer.parseInt(responseJson.getString("out_trade_no").substring(30,32));
                                            //  支付宝扫码支付成功了，要开始出货啦
                                            Message msg = dispHandler.obtainMessage();
                                            msg.what = dispHandler_alipaycode_payok;
                                            msg.obj = AlipayCodeCustomerOpenID;
                                            dispHandler.sendMessage(msg);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    if(AlipayCheckStr.length() > 0
                            && ((MyApp)getApplication()).getHaveZXAlipay()==1
                            && queryzxalipayqueryurl.length()>0){
                        String alipaypush = queuealipay.poll();
                        // 支付宝alipay：商户交易号,支付宝交易号,支付者buyer_logon_id  最前面加track,最后加上分
                        if (alipaypush != null && alipaypush.length() > 0) {
                            LogUtils.i("后续收到个推的出货线程---收到推送alipay:" + alipaypush);


                            String soaprtn = (new MyHttp()).post(queryzxalipayqueryurl, AlipayCheckStr);
                            //LogUtils.i( "alipay2=" + soaprtn);
                            if (!alipaythreadisrunning && soaprtn.length() > 9) {  // 表示有返回的啦
                                try {
                                    String respB64Str = soaprtn.substring(9);
                                    String jsonstr = getFromBase64(respB64Str.replaceAll("#","+"));
                                    //Log.i(TAG,jsonstr);

                                    JSONObject responseJson = new JSONObject(jsonstr);
                                    String respCode = responseJson.getString("respCode");
                                    String txnState = responseJson.getString("txnState");
                                    String respMsg = responseJson.getString("respMsg");
                                    String orderId = responseJson.getString("orderId");//::::000829AVM00012320181128100954099
                                    if(respCode.equals("0000") && txnState.equals("00")){
                                        // 商户交易号,支付宝交易号,支付者buyer_logon_id,分
                                        String AlipayCodeCustomerOpenID_next = responseJson.getString("origSeqId")
                                                + "," + responseJson.getString("origZfbId")
                                                + "," + responseJson.getString("orderOpenId")
                                                + "," + responseJson.getString("txnAmt");

                                        LogUtils.i( "------支付宝V4------" + AlipayCodeCustomerOpenID_next);

                                        AlipayCheckStr = "";
                                        willouttrack = Integer.parseInt(orderId.substring(30,32));

                                        //  支付宝扫码支付成功了，要开始出货啦
                                        Message msg = dispHandler.obtainMessage();
                                        msg.what = dispHandler_alipaycode_payok;
                                        msg.obj = AlipayCodeCustomerOpenID_next;
                                        dispHandler.sendMessage(msg);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    if(WxpayCheckStr.length() > 0 && ((MyApp)getApplication()).getHavaWeixin()==1){
                        String weixinpush = queueweixin.poll();
                        // 微信wxpay：商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注)     最前面加track,最后加上分
                        if (weixinpush != null && weixinpush.length() > 0) {
                            LogUtils.i("后续收到个推的出货线程---收到推送weixinpush:" + weixinpush);

                            String soaprtn = (new MyHttp()).post("https://api.mch.weixin.qq.com/pay/orderquery", WxpayCheckStr);

                            if (!weixinthreadisrunning && soaprtn.length() > 0) {  // 表示有返回的啦
                                String return_code = Gmethod.xuan_zhi(soaprtn, "<return_code><![CDATA[", "]]></return_code>");

                                if (return_code.equals("SUCCESS")) {
                                    String result_code = Gmethod.xuan_zhi(soaprtn, "<result_code><![CDATA[", "]]></result_code>");
                                    if (result_code.equals("SUCCESS")) {
                                        String endresult = Gmethod.xuan_zhi(soaprtn, "<trade_state><![CDATA[", "]]></trade_state>");
                                        LogUtils.i( "轮询结果1：" + endresult);
                                        if (endresult.equals("SUCCESS")) {
                                            //<xml>
                                            // <return_code><![CDATA[SUCCESS]]></return_code>
                                            // <return_msg><![CDATA[OK]]></return_msg>
                                            // <appid><![CDATA[wx09587b46d66adac6]]></appid>
                                            // <mch_id><![CDATA[1366646502]]></mch_id>
                                            // <nonce_str><![CDATA[XhNXwFTVQXlgiWHg]]></nonce_str>
                                            // <sign><![CDATA[B77054EC5515B277932EDE86551503CE]]></sign>
                                            // <result_code><![CDATA[SUCCESS]]></result_code>
                                            // <openid><![CDATA[oDyIJwmLIFVZrrWXO7WekwYBkdCs]]></openid>
                                            // <is_subscribe><![CDATA[Y]]></is_subscribe>
                                            // <trade_type><![CDATA[NATIVE]]></trade_type>
                                            // <bank_type><![CDATA[CFT]]></bank_type>
                                            // <total_fee>1</total_fee>
                                            // <fee_type><![CDATA[CNY]]></fee_type>
                                            // <transaction_id><![CDATA[4200000002201710199011315664]]></transaction_id>
                                            // <out_trade_no><![CDATA[0test201708000120171019153643003]]></out_trade_no>
                                            // <attach><![CDATA[test2017080001_003_11004]]></attach>
                                            // <time_end><![CDATA[20171019153801]]></time_end>
                                            // <trade_state><![CDATA[SUCCESS]]></trade_state>
                                            // <cash_fee>1</cash_fee>
                                            // </xml>

                                            //LogUtils.i( "Weixin---------------OK--------------" + xuan_zhi(soaprtn, "<transaction_id><![CDATA[", "]]></transaction_id>"));

                                            // 商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注),fen
                                            String WeixinCustomerOpenID = Gmethod.xuan_zhi(soaprtn, "<out_trade_no><![CDATA[","]]></out_trade_no>") + "," +
                                                    Gmethod.xuan_zhi(soaprtn, "<transaction_id><![CDATA[","]]></transaction_id>") + "," +
                                                    Gmethod.xuan_zhi(soaprtn, "<openid><![CDATA[","]]></openid>") + "," +
                                                    Gmethod.xuan_zhi(soaprtn, "<appid><![CDATA[","]]></appid>") + "," +
                                                    (Gmethod.xuan_zhi(soaprtn, "<is_subscribe><![CDATA[","]]></is_subscribe>").equals("Y")?"1":"0")
                                                    +","+Gmethod.xuan_zhi(soaprtn, "<cash_fee>","</cash_fee>");
                                            LogUtils.i("------微信self------"+WeixinCustomerOpenID);

                                            WxpayCheckStr = "";
                                            willouttrack = Integer.parseInt(Gmethod.xuan_zhi(soaprtn, "<out_trade_no><![CDATA[","]]></out_trade_no>").substring(30,32));

                                            //  微信支付成功了，要开始出货啦
                                            Message msg2 = dispHandler.obtainMessage();
                                            msg2.what = dispHandler_weixin_payok;
                                            msg2.obj = WeixinCustomerOpenID;
                                            dispHandler.sendMessage(msg2);
                                        }

                                    }
                                    //else {
                                    //LogUtils.i( "weixin轮询结果2：" + "err_code:" + xuan_zhi(soaprtn, "<err_code><![CDATA[", "]]></err_code>"));
                                    //}
                                }
                                //else {
                                //LogUtils.i( "weixin轮询结果3：" + xuan_zhi(soaprtn, "<return_msg><![CDATA[", "]]></return_msg>"));
                                //}

                            }
                        }
                    }
                    if(WxpayCheckStr.length() > 0 && ((MyApp)getApplication()).getHaveZXWxpay()==1 && queryzxwxpayqueryurl.length()>0){
                        String weixinpush = queueweixin.poll();
                        // 微信wxpay：商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注)     最前面加track,最后加上分
                        if (weixinpush != null && weixinpush.length() > 0) {
                            LogUtils.i("后续收到个推的出货线程---收到推送weixinpush:" + weixinpush);

                            String soaprtn = (new MyHttp()).post(queryzxwxpayqueryurl, WxpayCheckStr);

                            if (!weixinthreadisrunning && soaprtn.length() > 9) {  // 表示有返回的啦
                                try {
                                    String respB64Str = soaprtn.substring(9);
                                    String jsonstr = getFromBase64(respB64Str.replaceAll("#","+"));
                                    //Log.i(TAG,jsonstr);

                                    JSONObject responseJson = new JSONObject(jsonstr);
                                    String origRespCode = responseJson.getString("origRespCode");
                                    //String txnState = responseJson.getString("txnState");
                                    //String respMsg = responseJson.getString("respMsg");
                                    String orderId = responseJson.getString("origOrderId");//::::000829AVM00012320181128100954099
                                    if(origRespCode.equals("SUCCESS")){
                                        // 商户交易号,微信交易号,支付者openid,收单的微信appid,是否关注(1为关注着,0为未关注),fen
                                        String WeixinCustomerOpenID_next = responseJson.getString("origSeqId")
                                                + "," + responseJson.getString("transactionId")
                                                + "," + responseJson.getString("orderOpenId")+",none,0"
                                                + "," + responseJson.getString("txnAmt");

                                        LogUtils.i("------微信self------"+WeixinCustomerOpenID_next);

                                        WxpayCheckStr = "";
                                        willouttrack = Integer.parseInt(orderId.substring(30,32));
                                        //willoutismain = orderId.substring(29,30).equals("0");

                                        //  微信支付成功了，要开始出货啦
                                        Message msg2 = dispHandler.obtainMessage();
                                        msg2.what = dispHandler_weixin_payok;
                                        msg2.obj = WeixinCustomerOpenID_next;
                                        dispHandler.sendMessage(msg2);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // 收到 调拨入的消息
                    String tranin_data = queuetraninmac.poll();
                    if (tranin_data == null || tranin_data.length() == 0) {
                        // 没有收到，啥也不处理
                    } else {
                        LogUtils.i("---收到推送调拨入的消息tranin_data:" + tranin_data);
                        int index = tranin_data.indexOf(",");
                        String supplycode = tranin_data.substring(11,index);

                        LogUtils.i("---收到推送调拨入的消息,格式化后的数据:" + supplycode+":"+tranin_data.substring(index+1));

                        lp.traninmac(supplycode,tranin_data.substring(index+1));
                    }

                    // 收到 调拨出的消息
                    String tranout_data = queuetranoutmac.poll();
                    if (tranout_data == null || tranout_data.length() == 0) {
                        // 没有收到，啥也不处理
                    } else {
                        LogUtils.i("---收到推送调拨出的消息tranout_data:" + tranout_data);
                        int index = tranout_data.indexOf(",");
                        String supplycode = tranout_data.substring(11,index);

                        LogUtils.i("---收到推送调拨出的消息,格式化后的数据:" + supplycode+":"+tranout_data.substring(index+1));

                        lp.tranoutmac(supplycode,tranout_data.substring(index+1));
                    }

                    // 收到 下架的消息
                    String stockout_data = queuestockoutmac.poll();
                    if (stockout_data == null || stockout_data.length() == 0) {
                        // 没有收到，啥也不处理
                    } else {
                        LogUtils.i("---收到推送下架的消息stockout_data:" + stockout_data);
                        int index = stockout_data.indexOf(",");
                        String supplycode = stockout_data.substring(11,index);

                        LogUtils.i("---收到推送下架的消息,格式化后的数据:" + supplycode+":"+stockout_data.substring(index+1));

                        lp.stockoutmac(supplycode,stockout_data.substring(index+1));
                    }

                    // 收到 盘点结果的消息
                    String checkstock_data = queuecheckstockmac.poll();
                    if (checkstock_data == null || checkstock_data.length() == 0) {
                        // 没有收到，啥也不处理
                    } else {
                        LogUtils.i("---收到推送盘点结果的消息checkstock_data:" + checkstock_data);
                        int index = checkstock_data.indexOf(",");
                        String supplycode = checkstock_data.substring(11,index);

                        LogUtils.i("---收到推送盘点结果的消息,格式化后的数据:" + supplycode+":"+checkstock_data.substring(index+1));

                        lp.checkstockmac(supplycode,checkstock_data.substring(index+1));
                    }

                    // 收到 补货结果的消息
                    String supplyresult_data = queuesupplyresultmac.poll();
                    if (supplyresult_data == null || supplyresult_data.length() == 0) {
                        // 没有收到，啥也不处理
                    } else {
                        LogUtils.i("---收到推送补货结果的消息supplyresult_data:" + supplyresult_data);
                        int index = supplyresult_data.indexOf(",");
                        String supplycode = supplyresult_data.substring(11,index);

                        supplyresult_data = supplyresult_data.substring(index+1);
                        index = supplyresult_data.indexOf(",");
                        long trackplanid = Long.parseLong(supplyresult_data.substring(11,index));

                        LogUtils.i("---收到推送补货结果的消息,格式化后的数据:" + supplycode+":"+ trackplanid+":"+supplyresult_data.substring(index+1));

                        long nowtime = System.currentTimeMillis();
                        lp.supplyresultmac(supplycode,trackplanid,supplyresult_data.substring(index+1));
                        LogUtils.i("执行supplyresultmac("+supplycode+","+trackplanid+","+supplyresult_data.substring(index+1)+")的时间（单位：秒）："+(System.currentTimeMillis()-nowtime)/1000);

                        dispHandler.sendEmptyMessage(Goto_Loading);

                    }

                    // 收到 下推机器的消息
                    String push_data = queuepush.poll();
                    if(push_data == null || push_data.length() == 0){
                        // 没有收到，啥也不处理
                    } else {
                        LogUtils.i("---收到下推推送的消息push_data:" + push_data);

                        // pushmode+","+id+","+pushdata
                        int doti = push_data.indexOf(",");
                        int pushmode = Integer.parseInt(push_data.substring(0,doti));

                        push_data = push_data.substring(doti+1);
                        doti = push_data.indexOf(",");
                        long id_long = Long.parseLong(push_data.substring(0,doti));

                        String pushdata = push_data.substring(doti+1);

                        if(pushmode == 1) {
                            // 1：货道商品(价格)变更			10/batch/20180203/11002;11///11003/100_200_200_201
                            lp.pushdata_track_goods_price(pushdata);
                        } else if(pushmode == 2){
                            // 2:货道在库数变更			10/3;13/4;14/2
                            lp.pushdata_track_nownum(pushdata);
                        } else if(pushmode == 3){
                            // 3:商品价格变更			11002/100_101_101_100;11003/150_1_1_1
                            lp.pushdata_goods_price(pushdata);
                        } else if(pushmode == 4){
                            // 4:货道停售可售			10/0;11/0;13/1;20/0;49/1
                            lp.pushdata_track_cansale(pushdata);
                        } else if(pushmode == 5){
                            // 5:整机停售可售			0或者1
                            lp.pushdata_cansale(pushdata);
                        } else if(pushmode == 6){
                            // 6:更新广告方案			（无数据）

                        }
                        // 告诉后台，我处理完了
                        long timestamp = System.currentTimeMillis();
                        String str = "id="+id_long+"&macid=" + ((MyApp)getApplication()).getMainMacID()+
                                "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
                        String md5 = MD5.GetMD5Code(str);

                        String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/pushmacok",
                                "macid=" + ((MyApp)getApplication()).getMainMacID() + "&id="+id_long+ "&timestamp="+timestamp + "&md5=" + md5);
                        LogUtils.i("下推推送的处理完成，报告接口,pushmode："+pushmode+";id="+id_long+";pushdata="+pushdata+";接口返回结果:"+rtnstr);

                        // 程序重启
                        dispHandler.sendEmptyMessage(Goto_Loading);
                    }
                }

            }
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
            LogUtils.i("netType="+netType + ";netSubtype="+netSubtype);

            if (netType == ConnectivityManager.TYPE_ETHERNET){
                netstatus_txt = "上网: 网线";
            } else if (netType == ConnectivityManager.TYPE_WIFI) {  //WIFI
                netstatus_txt = "上网: 无线";
            } else if (netType == ConnectivityManager.TYPE_MOBILE){// && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS && !mTelephony.isNetworkRoaming()) {   //MOBILE
                netstatus_txt = "上网: 4G";
            } else {
                netstatus_txt = "上网: 无";
            }
        }

        Message msg = dispHandler.obtainMessage(dispHandler_Disp_NetStatus);
        msg.obj = netstatus_txt;
        dispHandler.sendMessage(msg);

        if(netstatus_txt.equals("上网: 无") || netstatus_txt.equals("上网: 未知")){
            return "0";
        } else if(netstatus_txt.equals("上网: 网线")){
            return "网线";
        } else if(netstatus_txt.equals("上网: 无线")) {
            return "无线";
        } else {
            return ""+MyPhoneStateListener.signallevel;
        }
    }

    // 主柜的货道在库状况状态  '4,5,6,0,0,0...' 货道售空状态 0表示未售空1表示售空
    public void MainGeneralNumNow(String mainNumNow){
        // 启动时有同步的处理，所以忽略

            String[] ishaveGoods = mainNumNow.split(",");
            int[] ishave = new int[ishaveGoods.length];
            for(int i=0;i<ishave.length;i++){
                ishave[i] = Integer.parseInt(ishaveGoods[i]);
                if(ishave[i] == 0){
                    // 单片机认为这个轨道是没有商品的
                    if(((MyApp)getApplication()).getTrackMainGeneral()[i].getCansale() == 0
                            || ((MyApp)getApplication()).getTrackMainGeneral()[i].getNumnow() == 0){
                        // 单片机的数据没有错
                    } else {
                        ((MyApp)getApplication()).getTrackMainGeneral()[i].setNumnow(0);
                    }
                } else {
                    ((MyApp)getApplication()).getTrackMainGeneral()[i].setCansale(1);
                    ((MyApp)getApplication()).getTrackMainGeneral()[i].setNumnow(ishave[i]);
                }
            }
            // 更新页面商品的有无货物的显示
            dispHandler.sendEmptyMessage(dispHandler_Disp_GoodsImg_Refresh);

    }

    public void RtnOutTrackResult(String errorinfo){
        if(errorinfo.length()>0){
            LogUtils.e("出货失败："+errorinfo);
        }
        boolean isAliOrWx = false;
        if(payinfo.split(",").length > 2) isAliOrWx = true;
        if(errorinfo.length() == 0){
            dispHandler.sendEmptyMessage(dispHandler_OutTrack_End);
        } else {
            Message msg3 = dispHandler.obtainMessage(dispHandler_OutTrack_NG3);
            msg3.obj = "抱歉，出货发生错误" + (!isAliOrWx?"":"(已通知后台退款)")+"3..."+errorinfo;
            dispHandler.sendMessage(msg3);
            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

            Message msg2 = dispHandler.obtainMessage(dispHandler_OutTrack_NG);
            msg2.obj = "抱歉，出货发生错误" + (!isAliOrWx?"":"(已通知后台退款)")+"2..."+errorinfo;
            dispHandler.sendMessage(msg2);
            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

            Message msg1 = dispHandler.obtainMessage(dispHandler_OutTrack_NG);
            msg1.obj = "抱歉，出货发生错误" + (!isAliOrWx?"":"(已通知后台退款)")+"1..."+errorinfo;
            dispHandler.sendMessage(msg1);
            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

            dispHandler.sendEmptyMessage(dispHandler_OutTrack_End);
        }
    }

    /**
     * 根据选择后信息来反推选择的是哪个商品index
     * @param willouttrack 轨道
     * @param willoutismain 是否为主机
     * @return 显示的商品索引
     */
    private int getGoodsIndex(int willouttrack,boolean willoutismain) {
        for(int i=0;i<goodslist.size();i++){
            GoodsBean gb = goodslist.get(i);
            if(willoutismain){

                    String saleings = ","+gb.mainsaleing+",";
                    if(saleings.contains(","+willouttrack+",")){
                        return i;
                    }

            }
        }
        return -1;
    }

    private String getFromBase64(String str) {
        String result = "";
        if (str != null) {
            try {
                result = new String(Base64.decode(str, Base64.NO_WRAP), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
