package com.freshtribes.icecream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.presenter.TrackPlanPresenter;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.ProgressDialog;
import com.freshtribes.icecream.view.ITrackPlanView;

import java.net.Inet4Address;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TrackPlanActivity extends Activity implements ITrackPlanView, View.OnClickListener {
    private ThreadGroup tg = new ThreadGroup("TrackPlanActivityThreadGroup");

    private Dialog mProgressDialog;

    private TrackPlanPresenter lp;
    private static final int dispHandler_rtnGetServerTrackPlan = 0x101;
    private static final int dispHandler_rtnDownPngOK = 0x102;
    private static final int dispHandler_rtnDownPngNG = 0x103;
    private static final int dispHandler_rtnDownPngDoing = 0x104;

    // 获取的结果
    private long planid = 0l;
    private String planname;
    private String maintrackflag;
    private String subtrackflag;
    private ArrayList<HashMap<String,Object>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_plan);

        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        // 生成Presenter
        lp = new TrackPlanPresenter(this, tg, (MyApp) getApplication());

        TextView localversiontv = findViewById(R.id.localversiontv);
        if (((MyApp) getApplication()).getTrackplanid() == 0l) {
            localversiontv.setText("目前本机的货道方案编号与名称：(未设置)");
        } else {
            localversiontv.setText("目前本机的货道方案编号与名称：" + ((MyApp) getApplication()).getTrackplanid() + "/" + ((MyApp) getApplication()).getTrackplanname());
        }

        Button btn_get = findViewById(R.id.btn_get);
        btn_get.setOnClickListener(this);

        Button btn_reset = findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(this);

        RelativeLayout dlrl = findViewById(R.id.getserverversion);
        dlrl.setVisibility(View.INVISIBLE);

        RelativeLayout inforl = findViewById(R.id.inforl);
        inforl.setVisibility(View.INVISIBLE);

        Button btn_getserverfile = findViewById(R.id.btn_getserverfile);
        btn_getserverfile.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                // 关闭presenter的所有线程，然后跳转
                lp.setStopThread();
                while (tg.activeCount() > 0) for (int i = 0; i < 1000; i++) ;
                tg.destroy();

                Intent toMenu = new Intent(TrackPlanActivity.this, MenuActivity.class);
                startActivity(toMenu);
                TrackPlanActivity.this.finish();
                break;

            case R.id.btn_reset:
                SharedPreferences sp =
                        TrackPlanActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器

                editor.putLong("trackplanid", 0l);
                editor.putString("trackplanname", "");
                editor.apply();//提交修改、

                ((MyApp) getApplication()).setTrackplanid(0l);
                ((MyApp) getApplication()).setTrackplanname("");

                TextView localversiontv = findViewById(R.id.localversiontv);
                localversiontv.setText("目前本机的货道方案编号与名称：(未设置)");

                break;

            case R.id.btn_get:
                if(mProgressDialog == null) {
                    mProgressDialog = ProgressDialog.createLoadingDialog(TrackPlanActivity.this, "联网获取服务器数据,请稍候...");
                }
                //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
                mProgressDialog.show();

                lp.getServerTrackPlan();
                break;

            case R.id.btn_getserverfile:
                if(planid != ((MyApp) getApplication()).getTrackplanid()){

                    SimpleDateFormat sdfl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String laststockcheck = ((MyApp)getApplication()).getStockchecktime();
                    if(laststockcheck.length() == 0
                            || getDistanceMin(laststockcheck,sdfl.format(System.currentTimeMillis())) > 120l) {

                        AlertDialog okad = new AlertDialog.Builder(TrackPlanActivity.this)
                                .setTitle("提示消息")
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setMessage("你最近没有进行库存盘点，下载新货道方案会覆盖原有的商品信息，继续吗？")
                                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(mProgressDialog == null) {
                                            mProgressDialog = ProgressDialog.createLoadingDialog(TrackPlanActivity.this, "联网获取信息,请稍候...");
                                        }
                                        mProgressDialog.show();

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
                                        lp.downpng(sb.substring(1,sb.length()-1).split(","),arrayList);
                                    }
                                })
                                .setNegativeButton("返回",  new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create();
                        okad.setCancelable(false);
                        okad.show();

                    } else {
                        if(mProgressDialog == null) {
                            mProgressDialog = ProgressDialog.createLoadingDialog(TrackPlanActivity.this, "联网获取信息,请稍候...");
                        }
                        mProgressDialog.show();

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
                        lp.downpng(sb.substring(1,sb.length()-1).split(","),arrayList);
                    }
                }

            default:
                break;
        }
    }

    // 取得服务器端的最新货道方案
    public void rtnServerTrackPlan(int code, String msg, long planid, String planname,
                                   String maintrackflag, String subtrackflag, ArrayList<HashMap<String,Object>> arrayList){
        this.planid = planid;
        this.planname = planname;
        this.maintrackflag = maintrackflag;
        this.subtrackflag = subtrackflag;
        this.arrayList = arrayList;

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("code", code);
        hm.put("msg", msg);
        Message message = dispHandler.obtainMessage(dispHandler_rtnGetServerTrackPlan);
        message.obj = hm;
        dispHandler.sendMessage(message);
    }

    // 下载图片失败出错了
    public void downgoodsinfoerror(String msgstr){
        Message msg = dispHandler.obtainMessage(dispHandler_rtnDownPngNG);
        msg.obj = msgstr;
        dispHandler.sendMessage(msg);
    }

    // 下载完成
    public void downgoodsinfook(){

        dispHandler.sendEmptyMessage(dispHandler_rtnDownPngOK);
    }

    // 下载中的信息
    public void downgoodsinfodoing(String msgstr){
        Message msg = dispHandler.obtainMessage(dispHandler_rtnDownPngDoing);
        msg.obj = msgstr;
        dispHandler.sendMessage(msg);
    }

    // 对应的商品名称
    public void setgoodsname(String goodscode,String goodsname){
        for(int i=0;i<arrayList.size();i++){
            HashMap<String,Object> hmarray = arrayList.get(i);
            String code = (String)hmarray.get("code");
            if(code.equals(goodscode)){
                hmarray.put("name",goodsname);
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
                case dispHandler_rtnGetServerTrackPlan:

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    HashMap<String, Object> hm = (HashMap<String, Object>) msg.obj;
                    int rtn_code = (Integer) hm.get("code");
                    String rtn_msg = (String) hm.get("msg");

                    if (rtn_code != 0) {
                        AlertDialog al = new AlertDialog.Builder(TrackPlanActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("提示")
                                .setMessage("错误信息：" + rtn_msg)
                                .setPositiveButton("确定", null)
                                .create();
                        al.setCancelable(false);
                        al.show();
                    } else if(maintrackflag.length() != 80 || subtrackflag.length() != 70){
                        AlertDialog al = new AlertDialog.Builder(TrackPlanActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("提示")
                                .setMessage("错误信息：收到的货道方案数据不对" )
                                .setPositiveButton("确定", null)
                                .create();
                        al.setCancelable(false);
                        al.show();
                    } else {
                        // 收到的数据是否自己匹配
                        boolean ischeck = true;
                        for(int i=0;i<arrayList.size();i++){
                            HashMap<String,Object> hmarray = arrayList.get(i);
                            int index = (Integer)hmarray.get("index");
                            if(index >= 110){
                                if(!subtrackflag.substring(index-110,index-110+1).equals("1")){
                                    ischeck = false;
                                    break;
                                }
                            } else if(index <= 79){
                                if(!maintrackflag.substring(index-1,index).equals("1")){
                                    ischeck = false;
                                    break;
                                }
                            } else {
                                ischeck = false;
                                break;
                            }
                        }

                        if(!ischeck){
                            AlertDialog al = new AlertDialog.Builder(TrackPlanActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("提示")
                                    .setMessage("错误信息：收到的货道方案数据不对" )
                                    .setPositiveButton("确定", null)
                                    .create();
                            al.setCancelable(false);
                            al.show();
                        } else {
                            // 和本机
                            ischeck = true;
                            String mainisdrink = ((MyApp)getApplication()).getMainMacType();
                            for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
                                int kmax = 0;
                                if(i==0) {
                                    kmax = ((MyApp) getApplication()).getMainGeneralLevel1TrackCount();
                                } else if(i==1){
                                    kmax = ((MyApp) getApplication()).getMainGeneralLevel2TrackCount();
                                } else if(i==2){
                                    kmax = ((MyApp) getApplication()).getMainGeneralLevel3TrackCount();
                                } else if(i==3){
                                    kmax = ((MyApp) getApplication()).getMainGeneralLevel4TrackCount();
                                } else if(i==4){
                                    kmax = ((MyApp) getApplication()).getMainGeneralLevel5TrackCount();
                                } else if(i==5){
                                    kmax = ((MyApp) getApplication()).getMainGeneralLevel6TrackCount();
                                } else if(i==6){
                                    kmax = ((MyApp) getApplication()).getMainGeneralLevel7TrackCount();
                                }
                                for (int k = 0; k < kmax; k++) {
                                    if(!maintrackflag.substring(i*10+k+9,i*10+k+1+9).equals(
                                            ""+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getCansale())){
                                        //LogUtils.i("general maintrackflag i="+(i*10+k+9));
                                        ischeck = false;
                                        break;
                                    }
                                }
                                if(!ischeck) break;
                            }

                            if(ischeck) {
                                // 副柜处理，暂时没有
                            }

                            TextView newadverttv = findViewById(R.id.serverversiontv);
                            newadverttv.setText("服务器端的最新货道方案编号与名称："+planid+"/"+planname);

                            if(ischeck){
                                if(planid != ((MyApp) getApplication()).getTrackplanid()) {
                                    RelativeLayout dlrl = findViewById(R.id.getserverversion);
                                    dlrl.setVisibility(View.VISIBLE);
                                }

                                RelativeLayout inforl = findViewById(R.id.inforl);
                                inforl.setVisibility(View.GONE);

                                // 发现不一致的地方，然后显示出来
                                if(((MyApp) getApplication()).getTrackplanid() == 0){
                                    TextView changetv = findViewById(R.id.changegoodstv);
                                    changetv.setText("");
                                } else {
                                    TextView changetv = findViewById(R.id.changegoodstv);
                                    changetv.setText("商品有变化的轨道：\n" + different());
                                }

                            } else {
                                RelativeLayout inforl = findViewById(R.id.inforl);
                                inforl.setVisibility(View.VISIBLE);

                                TextView infotv = findViewById(R.id.infotv);
                                infotv.setText("收到的货道方案数据和本机的货道设定不一致，无法适用");
                            }

                        }


                    }
                    break;

                case dispHandler_rtnDownPngNG:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    AlertDialog al = new AlertDialog.Builder(TrackPlanActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("错误信息：" + msg.obj)
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();

                    break;

                case dispHandler_rtnDownPngDoing:
                    ((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText((String)msg.obj);
                    break;

                case dispHandler_rtnDownPngOK:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    SharedPreferences sp =
                            TrackPlanActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();//获取编辑器

                    editor.putLong("trackplanid", planid);
                    editor.putString("trackplanname", planname);

                    editor.apply();//提交修改

                    ((MyApp) getApplication()).setTrackplanid(planid);
                    ((MyApp) getApplication()).setTrackplanname(planname);

                    AlertDialog okad = new AlertDialog.Builder(TrackPlanActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("提示")
                            .setMessage("下载成功")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 关闭presenter的所有线程，然后跳转
                                    lp.setStopThread();
                                    long tgnow = System.currentTimeMillis();
                                    while(tg.activeCount()>0){
                                        for(int i=0;i<1000;i++);
                                        if(System.currentTimeMillis() - tgnow >  3000) break;
                                    }
                                    tg.destroy();

                                    Intent toMenu = new Intent(TrackPlanActivity.this, MenuActivity.class);
                                    startActivity(toMenu);
                                    TrackPlanActivity.this.finish();
                                }
                            })
                            .create();
                    okad.setCancelable(false);
                    okad.show();
                    break;

                default:
                    break;
            }
            return  false;
        }
    });

    private String different(){
        String rtn =  "（无）";

        Map<String,String> diffmap = new TreeMap<>();

        for(int i=0;i<arrayList.size();i++){
            HashMap<String,Object> hmarray = arrayList.get(i);
            String code = (String)hmarray.get("code");
            int index = (Integer)hmarray.get("index");
            //String name = (String)hmarray.get("name");
            //String price = (String)hmarray.get("price");


            if(index >= 110){
                // 副柜处理，暂时没有
            } else {

                if(!((MyApp)getApplication()).getTrackMainGeneral()[index - 10].getGoodscode().equals(code)) {
                    diffmap.put("轨道：" + index+"("+ ((MyApp) getApplication()).getTrackMainGeneral()[index - 10].getNumnow()+"件)",
                            ((MyApp) getApplication()).getTrackMainGeneral()[index - 10].getGoodscode() +
                                    " " +
                                    ((MyApp) getApplication()).getTrackMainGeneral()[index - 10].getGoodsname() +
                                    " > " + code);
                }

            }

        }

        if(diffmap.size() > 0){
            rtn = "";

            Iterator iter = diffmap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();

                rtn = rtn + "\n" + key +  " " + val;
            }
            return rtn;
        } else {
            return rtn;
        }
    }


    /**
     * 两个时间之间相差距离多少天
     * @param str1 时间参数 1：
     * @param str2 时间参数 2：
     * @return 相差分钟
     */
    private long getDistanceMin(String str1, String str2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(str1);
        System.out.println(str2);
        Date one;
        Date two;
        long min = 0l;
        try {
            one = df.parse(str1);
            two = df.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff ;
            if(time1<time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            min = diff / (1000 * 60);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return min;
    }
}
