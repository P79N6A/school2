package com.zhongyagroup.school;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.zhongyagroup.school.app.MyApp;
import com.zhongyagroup.school.bean.GoodsBean;
import com.zhongyagroup.school.util.BitmapUtil;
import com.zhongyagroup.school.util.Gmethod;
import com.zhongyagroup.school.util.ICCard;
import com.zhongyagroup.school.util.LogUtils;
import com.zhongyagroup.school.util.MD5;
import com.zhongyagroup.school.util.MyExceptionHandler;
import com.zhongyagroup.school.util.MyHttp;
//import com.zhongyagroup.school.util.MyPhoneStateListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android_serialport_api.CallBack;
import android_serialport_api.ComButton;
import android_serialport_api.ComTrack;

public class MainActivity extends Activity implements View.OnClickListener, CallBack {
    private static String TAG = "Main~~~";

    private SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    //mode按键-------------------
    protected static final int Goto_Menu = 0x540; // Handle的消息标志：跳转到维护页面
    protected static final int Goto_Loading = 0x541;
    // 广播
    private ModeBroadCastReceiver modeswitch;

    // 心跳线程用
    private boolean isHeartThreadRunning = false;
    private int heart_secondcount = -1;
    // 是否发送了基本设置信息
    private boolean sentSetpara = false;

//    //TelephonyManager类的对象
//    private TelephonyManager Tel;
//    //MyPhoneStateListener类的对象，即设置一个监听器对象
//    private MyPhoneStateListener MyListener;
    // handler
    protected static final int dispHandler_Disp_NetStatus = 0x530;
    protected static final int dispHandler_Disp_SimSignalLevel = 0x531;

    private MyApp myApp;

    // 工控机类型
    private String mtype;

    // 个推
    private String clientID = "";

    // 取得状态的值
    // 温度,化霜温度，制冷开始温度，制冷停止温度，门（closed，openning）,其它故障（有的话）,串口发送相关错误（有的话）
    private String[] statusdetail5 = new String[]{"-","-","-","-","-","其它故障（有的话）","串口发送相关错误（有的话）"};
    private static final int dispHandler_Disp_Temp = 0x109;


    // 要显示的商品图片及所在货道的列表，是一个页面变量
    private ArrayList<GoodsBean> goodslist = new ArrayList<>();
    private ArrayList<Bitmap> goodsbmplist = new ArrayList<>();

    private ImageView[] goodsupdown_btn = new ImageView[6];
    private ImageView[] goodsupdown_saleover = new ImageView[6];
    private ImageView[] goodsimagedisp = new ImageView[6];  // 商品图片
    private TextView[] goodsname = new TextView[6];
    private TextView[] goodsprice = new TextView[6];

    private static final int STATUS_READY = 0;   // 显示商品等待用户选择商品或按钮选货中
    private static final int STATUS_SELECTED = 1;// 已选择商品了等待刷卡（有倒计时的）
    private static final int STATUS_PAYING = 2;  // 已刷卡正在联网确认中（余额是否够，次数有没有超出，当日消费金额有没有超出等）
    private static final int STATUS_OUTING = 3;  // 出货中

    private int pagestatus = STATUS_READY;


    // 倒计时相关
    private boolean isCountDownRunning = false;
    private static final int SELECTPAGE_COUNTDOWN = 30; // 支付选择的倒计时
    private int selectcountdown = 0;
    private TextView selectcountdowntv;
    protected static final int dispHandler_selecting_count = 0x480;
    protected static final int dispHandler_return = 0x481;
    protected static final int dispHandler_return_refresh = 0x482;

    private TextView hinttv;
    private TextView tracknotv;
    private ImageView goodsselectiv;
    protected static final int dispHandler_disperror = 0x490;
    private String cardno = "";
    private int willouttrack;
    private int willoutprice;
    protected static final int dispHandler_getcardno = 0x491;

    // 卡的确认OK了，那么开始出货
    protected static final int dispHandler_startdelivery = 0x492;
    // 出货成功，要发销售记录
    protected static final int dispHandler_delivery_ok = 0x493;

    private ComButton comButton;

    protected static final int dispHandler_buttonSelect = 0x494;
    protected static final int dispHandler_buttonCancel = 0x495;


    // 图片变灰
    ColorMatrix matrix0;
    private ColorMatrixColorFilter filter0;
    // 图片原图
    ColorMatrix matrix1;
    private ColorMatrixColorFilter filter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 抛异常时写日志
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(MainActivity.this));

        // SellerPushService 为第三方自定义推送服务
        PushManager.getInstance().initialize(this.getApplicationContext(), com.zhongyagroup.school.service.SellerPushService.class);
        // com.getui.demo.DemoIntentService 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), com.zhongyagroup.school.service.SellerIntentService.class);

        myApp = (MyApp)getApplication();

        ((TextView)findViewById(R.id.macidtextview)).setText("本机编号："+myApp.getMainMacID()+" \n程序版本："+ Gmethod.getAppVersion(MainActivity.this));

//        //MyPhoneStateListener类的对象，即设置一个监听器对象
//        MyListener = new MyPhoneStateListener((ImageView)findViewById(R.id.imageViewsignal));
//        //Return the handle to a system-level service by name.通过名字获得一个系统级服务
//        //TelephonyManager类的对象
//        Tel = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//        //Registers a listener object to receive notification of changes in specified telephony states.设置监听器监听特定事件的状态
//        //Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); 这个语句放在onResume中

        mtype = android.os.Build.MODEL; // 手机型号
        LogUtils.i("工控机model:"+mtype);

        goodsupdown_btn[0] = findViewById(R.id.goodsbtn1);
        goodsupdown_btn[1] = findViewById(R.id.goodsbtn2);
        goodsupdown_btn[2] = findViewById(R.id.goodsbtn3);
        goodsupdown_btn[3] = findViewById(R.id.goodsbtn4);
        goodsupdown_btn[4] = findViewById(R.id.goodsbtn5);
        goodsupdown_btn[5] = findViewById(R.id.goodsbtn6);

        goodsimagedisp[0] = findViewById(R.id.goods1);
        goodsimagedisp[1] = findViewById(R.id.goods2);
        goodsimagedisp[2] = findViewById(R.id.goods3);
        goodsimagedisp[3] = findViewById(R.id.goods4);
        goodsimagedisp[4] = findViewById(R.id.goods5);
        goodsimagedisp[5] = findViewById(R.id.goods6);

        goodsupdown_saleover[0] = findViewById(R.id.goodsbtn1saleover);
        goodsupdown_saleover[1] = findViewById(R.id.goodsbtn2saleover);
        goodsupdown_saleover[2] = findViewById(R.id.goodsbtn3saleover);
        goodsupdown_saleover[3] = findViewById(R.id.goodsbtn4saleover);
        goodsupdown_saleover[4] = findViewById(R.id.goodsbtn5saleover);
        goodsupdown_saleover[5] = findViewById(R.id.goodsbtn6saleover);

        goodsname[0] = findViewById(R.id.goodsname1);
        goodsname[1] = findViewById(R.id.goodsname2);
        goodsname[2] = findViewById(R.id.goodsname3);
        goodsname[3] = findViewById(R.id.goodsname4);
        goodsname[4] = findViewById(R.id.goodsname5);
        goodsname[5] = findViewById(R.id.goodsname6);

        goodsprice[0] = findViewById(R.id.goodsprice1);
        goodsprice[1] = findViewById(R.id.goodsprice2);
        goodsprice[2] = findViewById(R.id.goodsprice3);
        goodsprice[3] = findViewById(R.id.goodsprice4);
        goodsprice[4] = findViewById(R.id.goodsprice5);
        goodsprice[5] = findViewById(R.id.goodsprice6);

        for (int i = 0; i < 6; i++) {
            goodsupdown_btn[i].setVisibility(View.GONE);
            goodsupdown_btn[i].setOnClickListener(this);
            goodsupdown_btn[i].setImageAlpha(150);
            goodsupdown_saleover[i].setVisibility(View.GONE);
            goodsimagedisp[i].setVisibility(View.GONE);
            goodsname[i].setVisibility(View.GONE);
            goodsprice[i].setVisibility(View.GONE);
        }

        // 提示区域的2个textview
        hinttv = findViewById(R.id.hinttv);
        selectcountdowntv = findViewById(R.id.counttv);
        tracknotv = findViewById(R.id.tracknotv);
        goodsselectiv = findViewById(R.id.goodsselect);
        goodsselectiv.setImageBitmap(null);

        matrix0 = new ColorMatrix();
        matrix0.setSaturation(0);
        filter0 = new ColorMatrixColorFilter(matrix0);
        matrix1 = new ColorMatrix();
        matrix1.setSaturation(1);
        filter1 = new ColorMatrixColorFilter(matrix1);

        // 准备数据，然后显示出来（第一页）
        setGoodsPicture_Bitmap_First(true);

        // 显示组合好的内容：
        for(int i=0;i<goodslist.size();i++){
            GoodsBean gb = goodslist.get(i);
            LogUtils.i("商品列表中 goodslist--从0开始： " + i + ":" + gb.goodscode+":"+gb.goodsname+":有货轨道{"+gb.mainsaleing+"}:无货轨道(" +gb.mainsaleout+")");
        }

        // 心跳线程
        isHeartThreadRunning = true;
        Thread heardThread = new HeardThread();
        heardThread.setPriority(Thread.MIN_PRIORITY);
        heardThread.setName("心跳线程：" + formathms.format(new Date(System.currentTimeMillis())));
        heardThread.start();

        comButton = new ComButton("",MainActivity.this);
        comButton.openSerialPort();

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
                String stri = "" + (i+1) + k;
                if(((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getGoodscode().length() > 0) {
                    if (((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getNumnow() > 0
                            && ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getErrorcode().length() == 0) {
                        addGoodtoList( stri, true
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getGoodscode()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getGoodsname()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPayprice(),needloadbmp);
                    } else {
                        addGoodtoList(stri, false
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getGoodscode()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getGoodsname()
                                , ((MyApp) getApplication()).getTrackMainGeneral()[i * 10 + k].getPayprice(),needloadbmp);
                    }
                }
            }
        }
    }

    /**
     * 把能否销售信息给显示的list
     * @param track 轨道1,10,10 （2个都是10，通过drinkmainsub来区分）
     * @param iscansale 能否销售 true表示能销售
     * @param goodscode 商品编号
     * @param goodsname 商品名称
     * @param cash 现金分
     */
    private void addGoodtoList(String track,boolean iscansale,String goodscode,String goodsname,
                               int cash,boolean needloadbmp){
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
            newGoodsBean.payprice = cash;
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

    // disppage:要显示的页面，比如刚开始时为1，翻到第二页后要显示第二页该值为2
    private void goodsdisp_imageview(){

        for(int i=0;i<goodslist.size();i++){
            goodsupdown_btn[i].setVisibility(View.VISIBLE);
            goodsimagedisp[i].setVisibility(View.VISIBLE);
            goodsname[i].setVisibility(View.VISIBLE);
            goodsprice[i].setVisibility(View.VISIBLE);

            goodsimagedisp[i].setImageBitmap(goodsbmplist.get(i));
            goodsname[i].setText(goodslist.get(i).goodsname);

            if(goodslist.get(i).payprice%10 != 0){
                goodsprice[i].setText("￥"+ Gmethod.tranFenToFloat2(goodslist.get(i).payprice));
            } else {
                goodsprice[i].setText("￥"+ Gmethod.tranFenToFloat1(goodslist.get(i).payprice));
            }



            if( goodslist.get(i).mainsaleing.length()>0 ){
                goodsupdown_btn[i].setOnClickListener(this);
                //goodsupdown_btn[i - starti].setImageResource(R.drawable.gs_selector);
                goodsupdown_saleover[i].setVisibility(View.INVISIBLE);

                goodsimagedisp[i].setColorFilter(filter1);
            } else {
                goodsupdown_btn[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this,"已售空，请选择其它商品",Toast.LENGTH_SHORT).show();
                    }
                });
                //goodsupdown_btn[i - starti].setImageResource(R.drawable.saleover);
                goodsupdown_saleover[i].setVisibility(View.VISIBLE);

                goodsimagedisp[i].setColorFilter(filter0);
            }
        }
        for(int i=goodslist.size();i<6;i++){
            goodsupdown_btn[i].setVisibility(View.INVISIBLE);
            goodsupdown_saleover[i].setVisibility(View.INVISIBLE);
            goodsimagedisp[i].setVisibility(View.INVISIBLE);
            goodsname[i].setVisibility(View.INVISIBLE);
            goodsprice[i].setVisibility(View.INVISIBLE);

            goodsimagedisp[i].setColorFilter(filter0);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goodsbtn1:
                dispPayRL(0);
                break;

            case R.id.goodsbtn2:
                dispPayRL(1);
                break;

            case R.id.goodsbtn3:
                dispPayRL(2);
                break;

            case R.id.goodsbtn4:
                dispPayRL(3);
                break;

            case R.id.goodsbtn5:
                dispPayRL(4);
                break;

            case R.id.goodsbtn6:
                dispPayRL(5);
                break;

            default:
                break;
        }
    }

    private synchronized void dispPayRL(int indexgoods) {
        if(pagestatus != STATUS_READY) return;

        // 状态发生变化啦
        tranToStatus(STATUS_SELECTED);

        // 获取一个随机的轨道值
        String havestr = goodslist.get(indexgoods).mainsaleing;

        //int rand = (int) (Math.random() * havestr.split(",").length);
        int rand = 0;
        if(((MyApp)getApplication()).getTrackouttype() == 0) {
            // 取最多的那个轨道
            String[] maxStr = havestr.split(",");
            int[] maxInt = new int[maxStr.length];
            String havaStrNum = "";
            for (int x = 0; x < maxInt.length; x++) {
                maxInt[x] = ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(maxStr[x]) - 10].getNumnow();

                havaStrNum = havaStrNum + maxInt[x] + ",";
            }
            if (havaStrNum.length() > 0) {
                havaStrNum = havaStrNum.substring(0, havaStrNum.length() - 1);
            }
            int rand_stock = maxInt[0];
            for (int x = 1; x < maxInt.length; x++) {
                if (maxInt[x] > rand_stock) {
                    rand_stock = maxInt[x];
                    rand = x;
                }
            }
        } else if(((MyApp)getApplication()).getTrackouttype() == 2) {
            // 后面的先出
            rand = havestr.split(",").length - 1;
        } else {
            // 前面的先出
            rand = 0;
        }

        // 预定要出货的轨道
        willouttrack = Integer.parseInt(havestr.split(",")[rand]);
        willoutprice = myApp.getTrackMainGeneral()[willouttrack-10].getPayprice();

        LogUtils.i("用户选择的商品的index-从0开始：" + indexgoods + ";对应的可售轨道：" + havestr+ ";出货选择："+myApp.getTrackouttype() + "(0多1前2后);选一个出来的是：" + willouttrack);

        goodsselectiv.setImageBitmap(goodsbmplist.get(indexgoods));
        tracknotv.setText("货道:"+willouttrack);

//        // 收银台
//        Intent intent = new Intent(AlimainActivity.this,AlifacepayActivity.class);
//        Bundle data = new Bundle();
//        data.putInt("trackno", willouttrack);
//        intent.putExtras(data);
//        startActivity(intent);
        // startActivityForResult(intent,1); 这是要跟踪返回数据的
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if(myApp.getMainMacID() == null || myApp.getMainMacID().length()==0){
            // 说明系统内存崩溃了，那么要重启啊
            dispHandler.sendEmptyMessage(Goto_Loading);
            return;
        }

        // 重新刷新库存信息
        setGoodsPicture_Bitmap_First(false);
        // 显示图片等信息
        goodsdisp_imageview();

        tranToStatus(STATUS_READY);

        // 监听网络信号
//        Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        //生成广播处理-----------------------------
        modeswitch = new ModeBroadCastReceiver();
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter("android.intent.action.ENG_MODE_SWITCH");
        //注册
        MainActivity.this.registerReceiver(modeswitch, intentFilter);
        //生成广播处理-----------------------------
        Log.i(TAG,"接收mode广播");
    }

    private void tranToStatus(int newstatus){
        pagestatus = newstatus;
        switch(newstatus){
            case STATUS_READY:
                isCountDownRunning = false;
                selectcountdown = 0;
                selectcountdowntv.setText("");
                tracknotv.setText("");
                hinttv.setText("请按数字按钮选择商品......");
                goodsselectiv.setImageBitmap(null);
                break;

            case STATUS_SELECTED:
                cardno = "";
                hinttv.setText("请刷卡......");
                isCountDownRunning = true;
                selectcountdown = SELECTPAGE_COUNTDOWN;
                selectcountdowntv.setText(String.format("%dS", selectcountdown));
                new Thread(new SelectPayCountTimeThread(), "倒计时线程").start();
                break;

            case STATUS_PAYING:
                LogUtils.i("卡号:"+cardno);
                hinttv.setText("已读到卡，正在联网确认中......");
                selectcountdowntv.setText("");
                new Thread(new ServerCheckCardInfoThread(),"联网确认卡信息线程").start();
                break;

            case STATUS_OUTING:
                hinttv.setText("正在出货，请稍候......");
                selectcountdowntv.setText("");
                new Thread(new OutGoodsThread(),"出货线程").start();
                break;

            default:
                System.out.println("default");break;
        }
    }

    // 出货
    private class OutGoodsThread extends Thread{
        @Override
        public void run(){
            // 每隔10秒一次
            // 驱动板过来的温度和门开关
            while(ComTrack.isOpenning){
                // 如果 串口已经被打开了，那就继续等待
                try{ Thread.sleep(500); } catch(Exception e){ e.printStackTrace();}
            }
            ComTrack comTrack;
            if(mtype.contains("070") || mtype.contains("310") || mtype.contains("300")){
                comTrack = new ComTrack("/dev/ttyO2");
            } else {
                comTrack = new ComTrack("");
            }
            comTrack.openSerialPort();
            String rtn = comTrack.vend_out_ind(willouttrack/10,willouttrack%10);
            comTrack.closeSerialPort();

            String errormsg = rtn;
            if (rtn.length() > 0) {
                if (rtn.contains("出货命令发送3次对方无应答")) rtn = "11";
                else if (rtn.contains("返回错误应答非法功能")) rtn = "12";
                else if (rtn.contains("返回错误应答非法地址")) rtn = "13";
                else if (rtn.contains("返回错误应答非法数据")) rtn = "14";
                else if (rtn.contains("返回错误应答未知")) rtn = "15";
                else if (rtn.contains("正确应答的命令状态字系统正忙")) rtn = "16";
                else if (rtn.contains("正确应答的命令状态字货道故障")) rtn = "17";
                else if (rtn.contains("正确应答的命令状态字红外检测故障")) rtn = "18";
                else if (rtn.contains("正确应答的命令状态字其它故障")) rtn = "19";
                else if (rtn.contains("收到应答的字节长度不对")) rtn = "20";
                else rtn = "99";
            }

            if(rtn.length()>0){
                // 那么有轨道故障了，要保存到数据库.
                LogUtils.e("出货时发生错误(出货失败啦)轨道："+willouttrack+":错误信息：" + rtn);
                String saleTime = "";
                try {
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    saleTime = sdf.format(date);
                } catch (Exception e) {}

                myApp.getTrackMainGeneral()[willouttrack - 10].setErrorcode(rtn);
                myApp.getTrackMainGeneral()[willouttrack - 10].setErrortime(saleTime);

                try {
                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                    db.execSQL("update trackmaingeneral set errorcode='" + myApp.getTrackMainGeneral()[willouttrack - 10].getErrorcode()
                            + "',errortime='" + myApp.getTrackMainGeneral()[willouttrack - 10].getErrortime()
                            + "' where id=" + willouttrack);
                    LogUtils.i("update trackmaingeneral set errorcode='" + myApp.getTrackMainGeneral()[willouttrack - 10].getErrorcode()
                            + "',errortime='" + myApp.getTrackMainGeneral()[willouttrack - 10].getErrortime()
                            + "' where id=" + willouttrack);

                    db.close();
                } catch (SQLException e) {
                    LogUtils.e(" 执行SQL命令" + e.toString());
                }

                Message msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "抱歉，出货失败（"+errormsg+"）！......3";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "抱歉，该卡已过有效期！......2";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "抱歉，该卡已过有效期！......1";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                dispHandler.sendEmptyMessage(dispHandler_return_refresh);

                heart_secondcount = 175;// 5秒后，发送心跳
            } else {
                boolean iszero = false;
                // 正常出货
                myApp.getTrackMainGeneral()[willouttrack - 10].setNumnow(myApp.getTrackMainGeneral()[willouttrack - 10].getNumnow() - 1);
                myApp.getTrackMainGeneral()[willouttrack - 10].setErrorcode("");
                myApp.getTrackMainGeneral()[willouttrack - 10].setErrortime("");
                if (myApp.getTrackMainGeneral()[willouttrack - 10].getNumnow() < 0) {
                    myApp.getTrackMainGeneral()[willouttrack - 10].setNumnow(0);
                }
                if (myApp.getTrackMainGeneral()[willouttrack - 10].getNumnow() == 0) {
                    iszero = true;
                }

                try {
                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                    db.execSQL("update trackmaingeneral set errorcode='',errortime='',numnow=" + myApp.getTrackMainGeneral()[willouttrack - 10].getNumnow()
                            + " where id=" + willouttrack);
                    LogUtils.i("update trackmaingeneral set errorcode='',errortime='',numnow=" + myApp.getTrackMainGeneral()[willouttrack - 10].getNumnow()
                            + " where id=" + willouttrack);

                    db.close();
                } catch (SQLException e) {
                    LogUtils.e(" 执行SQL命令" + e.toString());
                }

                // 显示一下出货成功
                dispHandler.sendEmptyMessage(dispHandler_delivery_ok);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                // 如果数量变为0的话，那么要让画面刷新的
                if(iszero){
                    dispHandler.sendEmptyMessage(dispHandler_return_refresh);
                } else {
                    dispHandler.sendEmptyMessage(dispHandler_return);
                }

                heart_secondcount = 175;// 5秒后，发送心跳
            }

        }
    }

    // 右下角的时间的每秒刷新的线程
    private class ServerCheckCardInfoThread extends Thread {
        @Override
        public void run() {
            LogUtils.i( "开始时间线程");

            long timestamp = System.currentTimeMillis();
            String str = "innercardno=" + cardno + "&macid=" + ((MyApp) getApplication()).getMainMacID() +
                    "&timestamp=" + timestamp + "&accesskey=" + ((MyApp) getApplication()).getAccessKey();
            String md5 = MD5.GetMD5Code(str);

            String rtnstr = (new MyHttp()).post(((MyApp) getApplication()).getServerurl() + "/balancecard",
                    "macid=" + ((MyApp) getApplication()).getMainMacID() + "&innercardno=" + cardno +
                            "&timestamp=" + timestamp + "&md5=" + md5);

            LogUtils.i("收到付款会员卡信息（物理卡号），连接服务后台验证，返回结果：" + rtnstr+";cardno="+cardno);

            if (rtnstr.length() > 0) {
                // 说明和后台联网正常的
                try {
                    JSONObject soapJson = new JSONObject(rtnstr);
                    if (soapJson.getString("code").equals("0")) {
                        // 取到了正确的结果啦
                        int balance = soapJson.getInt("balance");
                        if (balance >= willoutprice) {
                            String effdate = soapJson.getString("effectivedate");
                            String today = (new SimpleDateFormat("yyyyMMdd", Locale.CHINA)).format(System.currentTimeMillis());
                            if(today.compareTo(effdate) > 0){

                                Message msg = dispHandler.obtainMessage(dispHandler_disperror);
                                msg.obj = "抱歉，该卡已过有效期！......3";
                                dispHandler.sendMessage(msg);
                                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                                msg = dispHandler.obtainMessage(dispHandler_disperror);
                                msg.obj = "抱歉，该卡已过有效期！......2";
                                dispHandler.sendMessage(msg);
                                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                                msg = dispHandler.obtainMessage(dispHandler_disperror);
                                msg.obj = "抱歉，该卡已过有效期！......1";
                                dispHandler.sendMessage(msg);
                                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                                dispHandler.sendEmptyMessage(dispHandler_return);

                            } else {
                                dispHandler.sendEmptyMessage(dispHandler_startdelivery);
                            }
                        } else {

                            Message msg = dispHandler.obtainMessage(dispHandler_disperror);
                            msg.obj = "抱歉，该卡余额不足！......3";
                            dispHandler.sendMessage(msg);
                            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                            msg = dispHandler.obtainMessage(dispHandler_disperror);
                            msg.obj = "抱歉，该卡余额不足！......2";
                            dispHandler.sendMessage(msg);
                            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                            msg = dispHandler.obtainMessage(dispHandler_disperror);
                            msg.obj = "抱歉，该卡余额不足！......1";
                            dispHandler.sendMessage(msg);
                            try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                            dispHandler.sendEmptyMessage(dispHandler_return);

                        }
                    } else {
                        Message msg = dispHandler.obtainMessage(dispHandler_disperror);
                        msg.obj = "抱歉，"+soapJson.getString("msg")+"！......3";
                        dispHandler.sendMessage(msg);
                        try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                        msg = dispHandler.obtainMessage(dispHandler_disperror);
                        msg.obj = "抱歉，"+soapJson.getString("msg")+"！......2";
                        dispHandler.sendMessage(msg);
                        try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                        msg = dispHandler.obtainMessage(dispHandler_disperror);
                        msg.obj = "抱歉，"+soapJson.getString("msg")+"！......1";
                        dispHandler.sendMessage(msg);
                        try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                        dispHandler.sendEmptyMessage(dispHandler_return);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {

                Message msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "抱歉，联网失败！......3";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "抱歉，联网失败！......2";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "抱歉，联网失败！......1";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                dispHandler.sendEmptyMessage(dispHandler_return);
            }
        }
    }


    // 右下角的时间的每秒刷新的线程
    private class SelectPayCountTimeThread extends Thread {
        @Override
        public void run() {
            LogUtils.i( "开始倒计时线程");

            ICCard iccard = new ICCard();
            boolean isopen = iccard.open(MainActivity.this);
            if(!isopen){
                Message msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "打开USB的读卡器失败了！......3";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "打开USB的读卡器失败了！......2";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                msg = dispHandler.obtainMessage(dispHandler_disperror);
                msg.obj = "打开USB的读卡器失败了！......1";
                dispHandler.sendMessage(msg);
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                dispHandler.sendEmptyMessage(dispHandler_return);

                isCountDownRunning = false;
            }

            while (isCountDownRunning) {
                // 1秒刷新1次
                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}

                if(selectcountdown >= 0) {
                    selectcountdown--;

                    if(isopen) {
                        String ten = iccard.getSerialNo();
                        if (ten.equals("n")) {
                            Message msg = dispHandler.obtainMessage(dispHandler_disperror);
                            msg.obj = "请放一张卡！！！";
                            dispHandler.sendMessage(msg);
                        }
                        if (ten.length() == 10) {
                            isCountDownRunning = false;
                            // 读到10位的卡了，那么
                            iccard.beep();
                            cardno = ten;

                            Message msg = dispHandler.obtainMessage(dispHandler_getcardno);
                            msg.obj = cardno;
                            dispHandler.sendMessage(msg);

                            continue;
                        }
                    }

                    if (selectcountdown <= 0) {
                        // 相当于点击返回
                        dispHandler.sendEmptyMessage(dispHandler_return);

                        break;
                    } else {
                        dispHandler.sendEmptyMessage(dispHandler_selecting_count);
                    }
                }
            }

            if(isopen){
                iccard.close();
            }

            LogUtils.i( "倒计时线程结束");
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        // 不监听网络信号
//        Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);

        //解除注册广播-----------------------------
        MainActivity.this.unregisterReceiver(modeswitch);
        //解除注册广播-----------------------------
        Log.i(TAG,"不接收mode广播");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isHeartThreadRunning = false;
        isCountDownRunning = false;

        comButton.closeSerialPort();

        for(int i=0;i<6;i++){
            goodsimagedisp[i].setImageBitmap(null);
        }
        goodsselectiv.setImageBitmap(null);
        try {
            for (int i = 0; i < goodsbmplist.size(); i++) {
                if (!goodsbmplist.get(i).isRecycled()) {
                    goodsbmplist.get(i).recycle();
                }
                goodsbmplist.set(i, null);
            }
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    // api 17  4.2
    // api 18  4.3
    // api 19  4.4
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
            int dbmlevel = getMobileDbmlevel();

            Message msgSignal = dispHandler.obtainMessage(dispHandler_Disp_SimSignalLevel);
            msgSignal.obj = "" + dbmlevel;
            dispHandler.sendMessage(msgSignal);

            return ""+dbmlevel;
        }
    }

    //内部类
    // 定义一个mode开关的接收器
    class ModeBroadCastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if (action.equals("android.intent.action.ENG_MODE_SWITCH")) {
                Log.i(TAG,"收到mode开关的广播了！");
                dispHandler.sendEmptyMessage(Goto_Menu);
            }
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
     * Handle线程，显示处理结果
     */
    Handler dispHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // 处理消息
            switch (msg.what) {

                case dispHandler_Disp_NetStatus:
                    ((TextView)findViewById(R.id.netstatus)).setText(msg.obj.toString());
                    if(msg.obj.toString().contains("4G")){
                        findViewById(R.id.imageViewsignal).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.imageViewsignal).setVisibility(View.GONE);
                    }
                    break;

                case dispHandler_Disp_SimSignalLevel:
                    String dbmlevel = msg.obj.toString();
                    if(dbmlevel.equals("5")){
                        ((ImageView)findViewById(R.id.imageViewsignal)).setImageResource(R.drawable.signal_type4_s4);
                    } else if(dbmlevel.equals("4")){
                        ((ImageView)findViewById(R.id.imageViewsignal)).setImageResource(R.drawable.signal_type4_s4);
                    } else if(dbmlevel.equals("3")){
                        ((ImageView)findViewById(R.id.imageViewsignal)).setImageResource(R.drawable.signal_type4_s3);
                    } else if(dbmlevel.equals("2")){
                        ((ImageView)findViewById(R.id.imageViewsignal)).setImageResource(R.drawable.signal_type4_s2);
                    } else if(dbmlevel.equals("1")){
                        ((ImageView)findViewById(R.id.imageViewsignal)).setImageResource(R.drawable.signal_type4_s1);
                    } else {
                        ((ImageView)findViewById(R.id.imageViewsignal)).setImageResource(R.drawable.signal_type4_s0);
                    }
                    break;

                case dispHandler_Disp_Temp:
                    // 收到温度了，然后显示出来
                    ((TextView)findViewById(R.id.statusdata)).setText(msg.obj.toString());
                    //Log.i(TAG,""+msg.obj.toString());
                    break;

                case Goto_Loading:
                    Intent toLoading = new Intent(MainActivity.this, LoadingActivity.class);
                    startActivity(toLoading);
                    MainActivity.this.finish();
                    break;

                case Goto_Menu:
                    Intent menu = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(menu);
                    MainActivity.this.finish();
                    break;

                case dispHandler_selecting_count:
                    selectcountdowntv.setText(String.format("%dS",selectcountdown));
                    break;

                case dispHandler_return:
                    tranToStatus(STATUS_READY);
                    break;

                case dispHandler_return_refresh:
                    tranToStatus(STATUS_READY);
                    // 同时刷新屏幕的库存等信息
                    // 重新刷新库存信息
                    setGoodsPicture_Bitmap_First(false);
                    // 显示图片等信息
                    goodsdisp_imageview();
                    break;

                case dispHandler_disperror:
                    //Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_LONG).show();
                    hinttv.setText(msg.obj.toString());
                    break;

                case dispHandler_getcardno:
                    // 已取到卡号了，开始联网确认
                    tranToStatus(STATUS_PAYING);
                    break;

                case dispHandler_startdelivery:
                    // 卡的确认OK了，接下老要出货
                    tranToStatus(STATUS_OUTING);
                    break;

                case dispHandler_delivery_ok:
                    new Thread(new sendSaleData(willouttrack,willoutprice,cardno),"发送销售记录").start();
                    break;

                case dispHandler_buttonSelect:
                    int indexg = Integer.parseInt(msg.obj.toString());
                    dispPayRL(indexg);
                    break;

                case dispHandler_buttonCancel:
                    tranToStatus(STATUS_READY);
                    break;

                default:
                    break;
            }
            return  false;
        }
    });

    class sendSaleData extends Thread{
        private int trackno;
        private int price;
        private String card;

        sendSaleData(int trackno,int price,String card){
            this.trackno = trackno;
            this.price = price;
            this.card = card;
        }

        @Override
        public void run(){
            String t_code = myApp.getTrackMainGeneral()[trackno-10].getGoodscode();
            String l_saletime = (new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis());
            // 所有参数合法
            long timestamp = System.currentTimeMillis();
            String str = "batch=" + "" + "&endday=" + "" + "&goodscode=" + t_code +
                    "&macid=" + myApp.getMainMacID() + "&mactime=" + l_saletime +
                    "&payinfo=" + card + "&payway=" + "iccard" + "&price=" + price + "&track=" + trackno +
                    "&timestamp=" + timestamp + "&accesskey=" + myApp.getAccessKey();
            String md5 = MD5.GetMD5Code(str);

            String soaprtn = (new MyHttp()).post(myApp.getServerurl() + "/sale",
                    "batch=" + "" + "&endday=" + "" + "&goodscode=" + t_code +
                            "&macid=" + myApp.getMainMacID() + "&mactime=" + l_saletime +
                            "&payinfo=" + card + "&payway=" + "iccard" + "&price=" + price + "&track=" + trackno +
                            "&timestamp=" + timestamp + "&md5=" + md5);

            LogUtils.i("发送销售记录的结果返回1:" + soaprtn);

            if (soaprtn.length() > 0) {
                try {
                    JSONObject soapJson = new JSONObject(soaprtn);

                    String code = soapJson.getString("code");
                    if (!code.equals("0")) {
                        timestamp = System.currentTimeMillis();
                        str = "batch=" + "" + "&endday=" + "" + "&goodscode=" + t_code +
                                "&macid=" + myApp.getMainMacID() + "&mactime=" + l_saletime +
                                "&payinfo=" + card + "&payway=" + "iccard" + "&price=" + price + "&track=" + trackno +
                                "&timestamp=" + timestamp + "&accesskey=" + myApp.getAccessKey();
                        md5 = MD5.GetMD5Code(str);

                        soaprtn = (new MyHttp()).post(myApp.getServerurl() + "/sale",
                                "batch=" + "" + "&endday=" + "" + "&goodscode=" + t_code +
                                        "&macid=" + myApp.getMainMacID() + "&mactime=" + l_saletime +
                                        "&payinfo=" + card + "&payway=" + "iccard" + "&price=" + price + "&track=" + trackno +
                                        "&timestamp=" + timestamp + "&md5=" + md5);

                        LogUtils.e("发送销售记录的结果返回2:" + soaprtn);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    class HeardThread extends Thread{
        public void run(){

            // 5秒之后开始实际处理
            if(isHeartThreadRunning){
                dispNetStatus();
                try{ Thread.sleep(1000); } catch(Exception e){ e.printStackTrace();}
            }
            if(isHeartThreadRunning){
                dispNetStatus();
                try{ Thread.sleep(1000); } catch(Exception e){ e.printStackTrace();}
            }
            if(isHeartThreadRunning){
                dispNetStatus();
                try{ Thread.sleep(1000); } catch(Exception e){ e.printStackTrace();}
            }
            if(isHeartThreadRunning){
                dispNetStatus();
                try{ Thread.sleep(1000); } catch(Exception e){ e.printStackTrace();}
            }
            if(isHeartThreadRunning){
                dispNetStatus();
                try{ Thread.sleep(1000); } catch(Exception e){ e.printStackTrace();}
            }


            // 取得状态的值 中亚驱动板
            String[] statusdetail = new String[]{"99","99","99","加热管1","加热管2","加热管3","加热管4","1号门开关","2号门开关","其它故障（有的话）","串口发送相关错误（有的话）"};

            String upsoftver = Gmethod.getAppVersion(MainActivity.this);

            while(isHeartThreadRunning){
                // 1秒
                try{ Thread.sleep(1000); } catch(Exception e){ e.printStackTrace();}

                if(heart_secondcount%30 ==0){
                    // 每隔10秒一次
                    // 驱动板过来的温度和门开关
                    while(isHeartThreadRunning && ComTrack.isOpenning){
                        // 如果 串口已经被打开了，那就继续等待
                        try{ Thread.sleep(500); } catch(Exception e){ e.printStackTrace();}
                    }
                    ComTrack comTrack;
                    if(mtype.contains("070") || mtype.contains("310") || mtype.contains("300")){
                        comTrack = new ComTrack("/dev/ttyO2");
                    } else {
                        comTrack = new ComTrack("");
                    }
                    comTrack.openSerialPort();
                    statusdetail5 = comTrack.getStatus();
                    comTrack.closeSerialPort();

                    String temp = statusdetail5[0];
                    String door = statusdetail5[4];
                    String error = statusdetail5[6];

                    // 温度,化霜温度，制冷开始温度，制冷停止温度，门（closed，openning）,其它故障（有的话）,串口发送相关错误（有的话）

                    Message msg = dispHandler.obtainMessage(dispHandler_Disp_Temp);
                    if(error.length()>0)
                        msg.obj = "取得状态时有错误："+error;
                    else
                        msg.obj = "温度:"+temp+"°C "+(door.equals("开着")?" 门:"+door:"") + (statusdetail5[5].length()>0?" 故障:"+statusdetail5[5]:"");
                    dispHandler.sendMessage(msg);
                }

                heart_secondcount = heart_secondcount + 1;
                if (heart_secondcount > 180 || heart_secondcount == 0 ) { // 180秒，3分钟  或者首次进来时
                    heart_secondcount = 0;

                    // 每隔3分钟刷新联网方式
                    String signale = dispNetStatus();
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
                        mdsrc = "adplanid="+0
                                + "&bills="+bills
                                + "&coins="+coins
                                + "&driverver="+"1.0.0"
                                + "&macerror="+mainError_tmp
                                + "&macid="+myApp.getMainMacID()
                                + "&macidsub="+""
                                + "&macstop="+"0"
                                + "&mactime="+((new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()))
                                + "&mainver="+"1.0.0"
                                + "&pushid="+getClientID()
                                + "&signalabc="+signale
                                + "&temp1="+statusdetail5[0]
                                + "&temp2="+"_";

                        mdsrc = mdsrc + "&track="+mainTrack[0]+","+mainTrack[1]+","+mainTrack[2]+","+mainTrack[3]+","+mainTrack[4]+","+mainTrack[5]+","+mainTrack[6];
                        for(int i=1;i<=7;i++) {
                            for(int j=0;j<mainTrack[i-1];j++) {
                                mdsrc = mdsrc + "&track"+i+j+"="+myApp.getTrackMainGeneral()[i*10+j-10].getGoodscode()+","+
                                        myApp.getTrackMainGeneral()[i*10+j-10].getNummax()+","+
                                        myApp.getTrackMainGeneral()[i*10+j-10].getNumnow()+","+
                                        (myApp.getTrackMainGeneral()[i*10+j-10].getErrorcode().length()==0?"":
                                                myApp.getTrackMainGeneral()[i*10+j-10].getErrorcode()+"_"+myApp.getTrackMainGeneral()[i*10+j-10].getErrortime())+","+
                                        1+","+
                                        ""+","+
                                        ""+","+
                                        myApp.getTrackMainGeneral()[i*10+j-10].getPayprice()+"_"+
                                        myApp.getTrackMainGeneral()[i*10+j-10].getPayprice()+"_"+
                                        myApp.getTrackMainGeneral()[i*10+j-10].getPayprice()+"_"+
                                        myApp.getTrackMainGeneral()[i*10+j-10].getPayprice();

                                Gmethod.setNowStock(al,myApp.getTrackMainGeneral()[i*10+j-10].getGoodscode(),
                                        myApp.getTrackMainGeneral()[i*10+j-10].getNumnow(),
                                        myApp.getTrackMainGeneral()[i*10+j-10].getPayprice());
                            }
                        }
                        mdsrc = mdsrc
                                + "&trackplanid=0"
                                + "&upsoftver="+upsoftver;


                        long timestamp = System.currentTimeMillis();
                        String str = mdsrc.replace("&signalabc="+signale,"&signalabc="+signale_encode) +
                                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                        String md5 = MD5.GetMD5Code(str);

                        String dr = "-";
                        if(statusdetail5[4].contains("开着")) dr = "open";
                        else if(statusdetail5[4].contains("关着")) dr = "close";

                        String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/macpara",
                                mdsrc + "&timestamp="+timestamp + "&md5=" + md5+"&nowstock="+Gmethod.getNowStockStr(al)+"&door="+dr+"&pkg=school");
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
                                + "&pushid="+getClientID()
                                + "&signalabc="+signale
                                + "&temp1="+statusdetail5[0]
                                + "&temp2="+"-";

                        mdsrc = mdsrc + "&track="+mainTrack[0]+","+mainTrack[1]+","+mainTrack[2]+","+mainTrack[3]+","+mainTrack[4]+","+mainTrack[5]+","+mainTrack[6];
                        for(int i=1;i<=7;i++) {
                            for(int j=0;j<mainTrack[i-1];j++) {
                                mdsrc = mdsrc + "&track"+i+j+"="+myApp.getTrackMainGeneral()[i*10+j-10].getNumnow()+","+
                                        (myApp.getTrackMainGeneral()[i*10+j-10].getErrorcode().length()==0?"":
                                                myApp.getTrackMainGeneral()[i*10+j-10].getErrorcode()+"_"+myApp.getTrackMainGeneral()[i*10+j-10].getErrortime())+","+
                                        1;

                                Gmethod.setNowStock(al,myApp.getTrackMainGeneral()[i*10+j-10].getGoodscode(),
                                        myApp.getTrackMainGeneral()[i*10+j-10].getNumnow(),
                                        myApp.getTrackMainGeneral()[i*10+j-10].getPayprice());
                            }
                        }

                        long timestamp = System.currentTimeMillis();
                        String str = mdsrc.replace("&signalabc="+signale,"&signalabc="+signale_encode)  +
                                "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                        String md5 = MD5.GetMD5Code(str);

                        String dr = "-";
                        if(statusdetail5[4].contains("开着")) dr = "open";
                        else if(statusdetail5[4].contains("关着")) dr = "close";

                        LogUtils.i(mdsrc + "&timestamp="+timestamp + "&md5=" + md5+"&door="+dr+"&nowstock="+Gmethod.getNowStockStr(al));

                        String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/macstock",
                                mdsrc + "&timestamp="+timestamp + "&md5=" + md5+"&door="+dr+"&nowstock="+Gmethod.getNowStockStr(al));
                        LogUtils.i("心跳，发送库存的返回值："+rtnstr);

                        //应用程序最大可用内存
                        int maxMemory = ((int) Runtime.getRuntime().maxMemory())/1024/1024;
                        //应用程序已获得内存
                        long totalMemory = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
                        //应用程序已获得内存中未使用内存
                        long freeMemory = ((int) Runtime.getRuntime().freeMemory())/1024/1024;
                        LogUtils.v("Main OnResume---> 应用程序最大可用内存="+maxMemory+"M,应用程序已获得内存="+totalMemory+"M,应用程序已获得内存中未使用内存="+freeMemory+"M");

                    }
                }
            }
        }

    }



    @Override
    public void TranToActivity(String str){
        int buttoni = Integer.parseInt(str);
        if(buttoni == 7){
            // 确定，不处理
        } else if(buttoni == 8){
            // 取消
            if(pagestatus == STATUS_SELECTED){
                // 已经选择了商品了，要取消
                dispHandler.sendEmptyMessage(dispHandler_buttonCancel);
                //tranToStatus(STATUS_READY);
            } else {
                // 其它情况不处理
            }
        } else if(buttoni>=1 && buttoni<=6){
            // 对返回的按键事件做处理
            int goodsindex = -1;
            for(int i=0;i<goodslist.size();i++){
                GoodsBean db = goodslist.get(i);
                String havetrackno = db.mainsaleing;
                if(havetrackno.length()>0) havetrackno = ","+havetrackno;
                //String notrackno = db.mainsaleout;
                //if(notrackno.length()>0) notrackno = ","+notrackno;

                if(havetrackno.contains(","+buttoni)){
                    // 那就是找到有货了
                    goodsindex = i;
                    break;
                }
            }
            if(goodsindex > -1){
                Message msg = dispHandler.obtainMessage(dispHandler_buttonSelect);
                msg.obj = ""+goodsindex;
                dispHandler.sendMessage(msg);
                // 那就是找到商品了
                //dispPayRL(goodsindex);
            } else {
                // 没有找到有货的轨道，不处理
            }
        } else {
            // 其它的键值，不处理
        }
    }
}
