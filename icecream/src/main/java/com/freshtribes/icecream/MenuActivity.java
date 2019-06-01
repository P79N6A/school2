package com.freshtribes.icecream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.presenter.MenuPresenter;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.ProgressDialog;
import com.freshtribes.icecream.view.IMenuView;

import java.text.SimpleDateFormat;

public class MenuActivity extends Activity implements IMenuView,View.OnClickListener {

    private ThreadGroup tg = new ThreadGroup("MenuActivityThreadGroup");

    private Dialog mProgressDialog;

    private MenuPresenter lp;
    private static final int dispHandler_rtnGetServerError = 0x101;
    private static final int dispHandler_rtnGetServerOK = 0x102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // 生成Presenter
        lp = new MenuPresenter(this, tg, (MyApp) getApplication());

        TextView mainmacidtv = findViewById(R.id.mainmacid);
        mainmacidtv.setText("主机编号：" + ((MyApp) getApplication()).getMainMacID());

        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        Button stocksupplybtn = findViewById(R.id.btn_stocksupply);
        stocksupplybtn.setOnClickListener(this);

        Button stockcheckbtn = findViewById(R.id.btn_stockcheck);
        stockcheckbtn.setOnClickListener(this);

        Button trackplanbtn = findViewById(R.id.btn_trackpaln);
        trackplanbtn.setOnClickListener(this);

        Button salecountbtn = findViewById(R.id.btn_salecount);
        salecountbtn.setOnClickListener(this);

        Button checkversionbtn = findViewById(R.id.btn_checkversion);
        checkversionbtn.setOnClickListener(this);

        Button testbtn = findViewById(R.id.btn_test);
        testbtn.setOnClickListener(this);

        Button macparabtn = findViewById(R.id.btn_macpara);
        macparabtn.setOnClickListener(this);

        Button cansalebtn = findViewById(R.id.btn_cansale);
        cansalebtn.setOnClickListener(this);

        Button mainmaxbtn = findViewById(R.id.btn_mainmax);
        mainmaxbtn.setOnClickListener(this);

        Button goodspricebtn = findViewById(R.id.btn_goodsprice);
        goodspricebtn.setOnClickListener(this);

        Button uploadbtn = findViewById(R.id.btn_upload);
        uploadbtn.setOnClickListener(this);

        Button exitbtn = findViewById(R.id.btn_exit);
        exitbtn.setOnClickListener(this);

        TextView tv = findViewById(R.id.nowstoptracknum);
        int count = getCansaleStop();
        if(count == 0){
            tv.setText("");
        } else {
            tv.setText("被合并的轨道数量："+count);
        }

        String laststockcheck = ((MyApp) getApplication()).getStockchecktime();
        if (laststockcheck.length() == 19) {
            // 计算和现在时间的差距
            SimpleDateFormat sdfl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long chamin = Gmethod.getDistanceMin(laststockcheck, sdfl.format(System.currentTimeMillis()));
            if (chamin < 120) {
                TextView tv444 = findViewById(R.id.last2hourstockcheck);
                tv444.setText("最近盘点时间：" + laststockcheck);
            }
        }
        SimpleDateFormat sdfSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        ((MyApp) getApplication()).setTempSupplyCode(sdfSSS.format(System.currentTimeMillis()));

        TextView supplycodetv = findViewById(R.id.supplycode);
        supplycodetv.setText("补货单号_临时：" + (((MyApp) getApplication()).getTempSupplyCode()==null?"无":((MyApp) getApplication()).getTempSupplyCode()));

        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.createLoadingDialog(MenuActivity.this, "正在连接服务器查看最新版本信息,请稍候...");
        }
        //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
        mProgressDialog.show();

        if(getError().length()>0){
            findViewById(R.id.test_newnotice).setVisibility(View.VISIBLE);
        }

        lp.getVersionInfo(Gmethod.getPackageName(MenuActivity.this));

        if(((MyApp)getApplication()).getDevicetype() != 2){
            findViewById(R.id.enable_rl).setVisibility(View.GONE);
        }
    }


    /**
     *
     * @return 有错误的轨道编号
     */
    protected String getError(){
        String errortrack = "";

        if(((MyApp)getApplication()).getMainMacType().equals("general")){
            int[] levelnum = new int[]{((MyApp)getApplication()).getMainGeneralLevel1TrackCount(),
                    ((MyApp)getApplication()).getMainGeneralLevel2TrackCount(),
                    ((MyApp)getApplication()).getMainGeneralLevel3TrackCount(),
                    ((MyApp)getApplication()).getMainGeneralLevel4TrackCount(),
                    ((MyApp)getApplication()).getMainGeneralLevel5TrackCount(),
                    ((MyApp)getApplication()).getMainGeneralLevel6TrackCount(),
                    ((MyApp)getApplication()).getMainGeneralLevel7TrackCount()};
            for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
                for(int j=0;j<levelnum[i];j++) {
                    if(((MyApp)getApplication()).getTrackMainGeneral()[i*10+j].getCansale()==0) continue;

                    if(((MyApp)getApplication()).getTrackMainGeneral()[i*10+j].getErrorcode().length() > 0){
                        errortrack = errortrack + "," +  (i*10+j+10);
                    }
                }
            }
        }


        if(errortrack.length() == 0){
            return "";
        } else {
            return errortrack.substring(1);   // 把最后一个，去掉
        }
    }

    @Override
    public void getVersionError(String errormsg) {
        Message msg = dispHandler.obtainMessage(dispHandler_rtnGetServerError);
        msg.obj = errormsg;
        dispHandler.sendMessage(msg);
    }

    @Override
    public void rtnGetVersionInfo(long advertplanid, long trackplanid, String apkserverversion) {
        Message msg = dispHandler.obtainMessage(dispHandler_rtnGetServerOK);
        msg.obj = "" + advertplanid + "," + trackplanid + "," + apkserverversion;
        dispHandler.sendMessage(msg);
    }

    /**
     * Handle线程，显示处理结果
     */
    Handler dispHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // 处理消息
            switch (msg.what) {
                case dispHandler_rtnGetServerOK:

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    String[] idthree = msg.obj.toString().split(",");

                    if (Long.parseLong(idthree[1]) > ((MyApp) getApplication()).getTrackplanid()) {
                        ImageView iv = findViewById(R.id.trackplan_newnotice);
                        iv.setVisibility(View.VISIBLE);
                    }

                    if (Integer.parseInt(idthree[2].replace(".", "")) > Integer.parseInt(Gmethod.getAppVersion(MenuActivity.this).replace(".", ""))) {
                        ImageView iv = findViewById(R.id.checkversion_newnotice);
                        iv.setVisibility(View.VISIBLE);
                    }

                    break;

                case dispHandler_rtnGetServerError:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    AlertDialog al = new AlertDialog.Builder(MenuActivity.this)
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
            return false;
        }
    });

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                Intent toLoading = new Intent(MenuActivity.this, LoadingActivity.class);
                Bundle dataLoading = new Bundle();
                dataLoading.putString("param", "menurestart");
                toLoading.putExtras(dataLoading);
                startActivity(toLoading);
                MenuActivity.this.finish();
                break;

            case R.id.btn_stocksupply:

                TextView tv = findViewById(R.id.last2hourstockcheck);
                if(tv.getText().toString().replace("最近盘点时间：无","").length() == 0){
                    AlertDialog al = new AlertDialog.Builder(MenuActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("你还没有库存盘点，进去后只能查看，继续吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent toStockSupply = new Intent(MenuActivity.this, StockSupplyActivity.class);
                                    Bundle data = new Bundle();
                                    data.putString("param", "readonly");
                                    toStockSupply.putExtras(data);
                                    startActivity(toStockSupply);
                                    MenuActivity.this.finish();
                                }
                            })
                            .setNegativeButton("取消",null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                } else {
                    Intent toStockSupply = new Intent(MenuActivity.this, StockSupplyActivity.class);
                    Bundle data = new Bundle();
                    data.putString("param", "modify");
                    toStockSupply.putExtras(data);
                    startActivity(toStockSupply);
                    MenuActivity.this.finish();
                }
                break;

            case R.id.btn_stockcheck:
                Intent toStockCheck = new Intent(MenuActivity.this,StockCheckActivity.class);
                startActivity(toStockCheck);
                MenuActivity.this.finish();
                break;

            case R.id.btn_trackpaln:
                Intent totrackplan = new Intent(MenuActivity.this,TrackPlanActivity.class);
                startActivity(totrackplan);
                MenuActivity.this.finish();
                break;

            case R.id.btn_cansale:
                //Intent tocansale = new Intent(MenuActivity.this,StockCansaleActivity.class);
                Intent tocansale = new Intent(MenuActivity.this,EnableActivity.class);
                startActivity(tocansale);
                MenuActivity.this.finish();
                break;

            case R.id.btn_salecount:
                Intent tosalecount = new Intent(MenuActivity.this,SaleCountActivity.class);
                startActivity(tosalecount);
                MenuActivity.this.finish();
                break;

            case R.id.btn_checkversion:
                Intent tocheckversion = new Intent(MenuActivity.this,CheckVersionActivity.class);
                startActivity(tocheckversion);
                MenuActivity.this.finish();
                break;

            case R.id.btn_test:
                if(((MyApp)getApplication()).getDevicetype() == 0){
                    // 中亚自己的升降机
                    Intent totest = new Intent(MenuActivity.this,TestActivity.class);
                    startActivity(totest);
                    MenuActivity.this.finish();
                } else if(((MyApp)getApplication()).getDevicetype() == 1 || ((MyApp)getApplication()).getDevicetype() == 2){
                    // Magex的有主控的升降系统
                    Intent totest = new Intent(MenuActivity.this,TestMagexActivity.class);
                    startActivity(totest);
                    MenuActivity.this.finish();
                } else {

                }

                break;

            case R.id.btn_macpara:
                Intent toMacPara = new Intent(MenuActivity.this, MacParaActivity.class);
                startActivity(toMacPara);
                MenuActivity.this.finish();
                break;

            case R.id.btn_mainmax:
                Intent toMainMax = new Intent(MenuActivity.this, MaxActivity.class);
                startActivity(toMainMax);
                MenuActivity.this.finish();
                break;

            case R.id.btn_goodsprice:
                Intent togoodsprice = new Intent(MenuActivity.this,GoodspriceActivity.class);
                startActivity(togoodsprice);
                MenuActivity.this.finish();
                break;

            case R.id.btn_upload:
                Intent toUpload = new Intent(MenuActivity.this, UploadActivity.class);
                startActivity(toUpload);
                MenuActivity.this.finish();
                break;

            case R.id.btn_exit:
                Intent intent=new Intent("com.mengdeman.vmselfstart.monitor.STOP");
                sendBroadcast(intent);

                MenuActivity.this.finish();

                // 日志关闭
                LogUtils.closePrintWritter();

                System.exit(0);

                break;

            default:
                break;
        }
    }

    private int getCansaleStop(){
        int count = 0;

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
                if(((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getCansale() == 0){
                    count ++;
                }
            }
        }

        return count;
    }

}
