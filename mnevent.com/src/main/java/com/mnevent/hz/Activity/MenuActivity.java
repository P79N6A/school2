package com.mnevent.hz.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.mnevent.hz.R;
import com.mnevent.hz.Utils.CommonUtils2;
import com.mnevent.hz.Utils.HttpUtils;
import com.mnevent.hz.Utils.LogUtils;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.Utils.ToastUtil;
import com.mnevent.hz.bean.PreLibBean;
import com.mnevent.hz.litemolder.LocalgoodsMolder;

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

public class MenuActivity extends Activity implements View.OnClickListener {

    @BindView(R.id.topst)
    Button topst;
    @BindView(R.id.basic)
    Button basic;
    @BindView(R.id.pndian)
    Button pndian;
    @BindView(R.id.buhuo)
    Button buhuo;
    @BindView(R.id.banben)
    Button banben;

    String drive;
    @BindView(R.id.local)
    Button local;
    @BindView(R.id.updete)
    Button updete;
   /* @BindView(R.id.on_off)
    Button onOff;*/
    String serialNumber,password;

    PromptDialog promptDialog;

    PreLibBean preLibBean;

    List<LocalgoodsMolder> goodslist;

    LocalgoodsMolder molder;

    String classname = "";

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:

                    if (preLibBean.getCode()==0) {
                        goodslist = LitePal.findAll(LocalgoodsMolder.class);
                        LitePal.deleteAll(LocalgoodsMolder.class);
                            for (int i = 0;i<preLibBean.getPngurlarray().size();i++){
                                molder = new LocalgoodsMolder();
                                molder.setGoodscode(preLibBean.getPngurlarray().get(i).getGoodscode());
                                molder.setName(preLibBean.getPngurlarray().get(i).getGoodsname());
                                molder.setImage(preLibBean.getPngurlarray().get(i).getPngurl());
                                molder.setPd(0);
                                molder.setUp("1");

                                molder.setPrice(preLibBean.getPngurlarray().get(i).getPricefen()+"");

                                molder.save();
                            }

                        promptDialog.dismiss();
                        ToastUtil.showToast(MenuActivity.this,"更新商品成功");

                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        classname = getClass().getSimpleName();
        String on_offs = PrefheUtils.getString(MenuActivity.this, "on_off", "");
       // Log.d("zlc", "on_offs:::" + on_offs);
       /* if(!TextUtils.isEmpty(on_offs) && on_offs.equals("1")){
            onOff.setText("Pay the switch(on)");
        }else{
            onOff.setText("Pay the switch(off)");
        }*/

        Log.d("ACTIVITY：", getClass().getSimpleName());
        ((Button) findViewById(R.id.toTest)).setOnClickListener(this);

        ((Button) findViewById(R.id.toName)).setOnClickListener(this);
        ((Button) findViewById(R.id.toStock)).setOnClickListener(this);
        ((Button) findViewById(R.id.toBack)).setOnClickListener(this);

        ((Button) findViewById(R.id.toFinish)).setOnClickListener(this);


    }


   /* @Override
    protected void onStop() {
        super.onStop();

    }*/

    @Override
    protected void onStart() {
        super.onStart();
        drive = PrefheUtils.getString(MenuActivity.this, "drive", "");
        LogUtils.d(classname+"主控选择：：" + drive);

        if (!TextUtils.isEmpty(drive)) {
            if (drive.equals("0、无Magex主控，无升降驱动盘")) {
                topst.setVisibility(View.GONE);
                Log.d("ZLC", "ZHIXING1");
            } else if (drive.equals("1、有Magex主控制器")) {
                topst.setVisibility(View.VISIBLE);

                //  MenuActivity.this.finish();
                Log.d("ZLC", "ZHIXING1");
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent toMenu;
        switch (v.getId()) {

            case R.id.toFinish:

                MenuActivity.this.finish();
                System.exit(0);
                break;


            case R.id.toTest:

                //测试跳转
                if (!TextUtils.isEmpty(drive)) {
                    if (drive.equals("0、无Magex主控，无升降驱动盘")) {
                        toMenu = new Intent(MenuActivity.this, TestActivity.class);
                        startActivity(toMenu);
                    } else if (drive.equals("1、有Magex主控制器")) {
                        toMenu = new Intent(MenuActivity.this, TestMagexActivity.class);
                        startActivity(toMenu);
                        //  MenuActivity.this.finish();
                    }
                } else {
                    ToastUtil.showToast(MenuActivity.this, "请选择主控!");
                }


                break;

            case R.id.toName:
                //服务器产品价格修改
                if (!TextUtils.isEmpty(drive)) {
                    Intent toName = new Intent(MenuActivity.this, NameActivity.class);
                    startActivity(toName);
                    //MenuActivity.this.finish();
                } else {
                    ToastUtil.showToast(MenuActivity.this, "请选择主控！");
                }
                break;

            case R.id.toStock:
                //选择商品
                if (!TextUtils.isEmpty(drive)) {
                    Intent toStock = new Intent(MenuActivity.this, StockActivity.class);
                    startActivity(toStock);
                    MenuActivity.this.finish();
                } else {
                    ToastUtil.showToast(MenuActivity.this, "请选择主控！");
                }
                break;

            case R.id.toBack:

                Intent toBack = new Intent(MenuActivity.this, LoadingActivity.class);
                startActivity(toBack);
                MenuActivity.this.finish();

                break;


            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {

        // 返回按钮的处理，不让程序退出的话，可以注解下面这行代码
        //super.onBackPressed();
        Intent toBack = new Intent(MenuActivity.this, LoadingActivity.class);
        startActivity(toBack);
        MenuActivity.this.finish();
    }


    @OnClick({R.id.topst, R.id.basic, R.id.pndian, R.id.buhuo, R.id.banben, R.id.local,R.id.updete})
    public void onViewClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.topst:
                //有maxag主控，合并货道
                if (!TextUtils.isEmpty(drive)) {
                    intent = new Intent(MenuActivity.this, MergeaisleActivity.class);
                    startActivity(intent);
                } else {
                    ToastUtil.showToast(MenuActivity.this, "请选择主控！");
                }
                break;
            case R.id.basic:
                //设置基本信息
                intent = new Intent(MenuActivity.this, BasicparameterActivity.class);
                startActivity(intent);
                break;
            case R.id.pndian:
                //盘点，****暂时不用
                if (!TextUtils.isEmpty(drive)) {
                    intent = new Intent(MenuActivity.this, CheckActivity.class);
                    startActivity(intent);
                } else {
                    ToastUtil.showToast(MenuActivity.this, "请选择主控！");
                }
                break;
            case R.id.buhuo:
                intent = new Intent(MenuActivity.this, ReplenishmentActivity.class);
                startActivity(intent);
                break;
            case R.id.banben:
                //版本更新
                if (!TextUtils.isEmpty(drive)) {
                    intent = new Intent(MenuActivity.this, CheckVersionActivity.class);
                    startActivity(intent);
                } else {
                    ToastUtil.showToast(MenuActivity.this, "请选择主控！");
                }
                break;
            case R.id.local:
                //本地价格修改跳转
                if (!TextUtils.isEmpty(drive)) {
                    intent = new Intent(MenuActivity.this, PriceActivity.class);
                    startActivity(intent);
                } else {
                    ToastUtil.showToast(MenuActivity.this, "请选择主控！");
                }
                break;
            case R.id.updete:
                //更新本地商品
                promptDialog = new PromptDialog(MenuActivity.this);
                promptDialog.showLoading("loading...");
                if (!TextUtils.isEmpty(drive)) {
                    updatedate();
                } else {
                    ToastUtil.showToast(MenuActivity.this, "请选择主控！");
                }
                break;
            /*case R.id.on_off:
                final String on_offs = PrefheUtils.getString(MenuActivity.this, "on_off", "");
                final On_offPopuwindow popuwindow = new On_offPopuwindow(MenuActivity.this);
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_menu,null), Gravity.CENTER,0,-200);
                if(!TextUtils.isEmpty(on_offs) && on_offs.equals("1")){
                    popuwindow.setText("确认是否关闭付款!");

                }else{
                    popuwindow.setText("确认是否打开付款!");

                }
                popuwindow.SetOnclike(new On_offPopuwindow.Onclike() {
                    @Override
                    public void setcancel() {
                        popuwindow.dismiss();
                    }

                    @Override
                    public void setaffirm() {
                        if(!TextUtils.isEmpty(on_offs) && on_offs.equals("1")){
                            PrefheUtils.putString(MenuActivity.this,"on_off","0");
                            onOff.setText("Pay the switch(off)");
                        }else {
                            PrefheUtils.putString(MenuActivity.this,"on_off","1");
                            onOff.setText("Pay the switch(on)");
                        }
                        popuwindow.dismiss();
                    }
                });
                break;*/
        }
    }

    private void updatedate() {
        serialNumber = PrefheUtils.getString(MenuActivity.this, "serialNumber", "");
        long time = System.currentTimeMillis();
        password = PrefheUtils.getString(MenuActivity.this, "password", "");
        OkGo.<String>post(HttpUtils.allGoods).tag(MenuActivity.this)
                .params("macid", serialNumber)
                .params("timestamp", time)
                .params("md5", CommonUtils2.GetMD5Code("macid=" + serialNumber + "&timestamp=" + time + "&accesskey=" + password))
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        analysisJson(response.body().toString());
                    }
                    @Override
                    public void onFinish() {
                        super.onFinish();
                        promptDialog.dismiss();

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ToastUtil.showToast(MenuActivity.this, "网络故障!");
                    }
                });
    }


    private void analysisJson(String json) {

        Gson gson = new Gson();
        preLibBean = gson.fromJson(json, PreLibBean.class);
        handler.sendEmptyMessage(1);
    }
}
