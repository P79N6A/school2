package com.mnevent.hz.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.mnevent.hz.Adapter.NameContextAdapter;
import com.mnevent.hz.Adapter.NameTitleAdapter;
import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.R;
import com.mnevent.hz.Utils.CommonUtils2;
import com.mnevent.hz.Utils.Gmethod;
import com.mnevent.hz.Utils.HttpUtils;
import com.mnevent.hz.Utils.Namepopuwindow;
import com.mnevent.hz.Utils.Picepopuwindow;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.Utils.ToastUtil;
import com.mnevent.hz.bean.PreLibBean;
import com.mnevent.hz.bean.PriceUpdeteBean;
import com.mnevent.hz.litemolder.ProductlibMolder;


import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.leefeng.promptlibrary.PromptDialog;


/**
 * 服务端价格修改
 */

public class NameActivity extends Activity {
    private static String TAG = "Name" + Gmethod.TAG_plus;
    @BindView(R.id.titlename)
    TextView titlename;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.topbar_title)
    RelativeLayout topbarTitle;
    /*@BindView(R.id.titlerecy)
    RecyclerView titlerecy;*/
    @BindView(R.id.contentrecy)
    RecyclerView contentrecy;
    PreLibBean preLibBean;
    PromptDialog dialog;

    //编号
    String serialNumber;
    //密码
    String password;

    private MyApp myApp;
    PriceUpdeteBean priceUpdeteBean;

    /**
     * 返回
     */
    private Button btnReturn;

    // 显示的列表
    ArrayList<Map<String, String>> list = null;

    List<ProductlibMolder> proall;
    //具体商品适配器
    NameContextAdapter contextadapter;

    Picepopuwindow picepopuwindow;
    Namepopuwindow namepopuwindow;
    int titlenumber;
    int contextnumber;
    int price;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    dialog.dismiss();
                    if(preLibBean.getCode()==0){
                       /*preLibBean.getGoodsInfoVo().getAllGoods().get(0).setPd("1");
                        LinearLayoutManager manager = new LinearLayoutManager(NameActivity.this);
                        titlerecy.setLayoutManager(manager);
                        final NameTitleAdapter adapter = new NameTitleAdapter(NameActivity.this,preLibBean);
                        titlerecy.setAdapter(adapter);*/

                        LinearLayoutManager manager1 = new LinearLayoutManager(NameActivity.this);
                        contentrecy.setLayoutManager(manager1);
                        contextadapter = new NameContextAdapter(NameActivity.this, preLibBean.getPngurlarray());
                        contentrecy.setAdapter(contextadapter);

                        /*adapter.SetOnclike(new NameTitleAdapter.Onclike() {
                            @Override
                            public void backles(int position) {

                                contextadapter.setUpDate(preLibBean.getGoodsInfoVo().getAllGoods().get(position));
                                titlenumber = position;
                            }
                        });
*/
                        contextadapter.SetOnclike(new NameContextAdapter.Onclike() {
                            @Override
                            public void btonclike(final int position) {


                                final PreLibBean.PngurlarrayBean goodsBean = preLibBean.getPngurlarray().get(position);
                                picepopuwindow = new Picepopuwindow(NameActivity.this);
                                picepopuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_name, null), Gravity.CENTER, 0, -200);
                                picepopuwindow.SetOnclike(new Picepopuwindow.Onclike() {
                                    @Override
                                    public void okclike(String s) {
                                        if (!TextUtils.isEmpty(s)) {

                                            /*price = (int) (Double.parseDouble(s)*100);
                                            contextnumber = position;
                                            Update(goodsBean.getGoodscode(),(int) (Double.parseDouble(s)*100));
                                            hintKeyBoard();*/
                                            ToastUtil.showToast(NameActivity.this,"暂时无法修改数据源价格，请修改本地价格");
                                        }
                                    }
                                });


                            }
                        });
                    }else{
                         ToastUtil.showToast(NameActivity.this,preLibBean.getMsg());
                    }
                    break;

                case 2:
                    /*dialog.dismiss();
                    if(priceUpdeteBean.getCode().equals("1")){

                        PreLibBean.GoodsInfoVoBean.AllGoodsBean.GoodsBean goodsBean = preLibBean.getGoodsInfoVo().getAllGoods().get(titlenumber).getGoods().get(contextnumber);
                        goodsBean.setPrice(price);
                        contextadapter.setUpDate(preLibBean.getGoodsInfoVo().getAllGoods().get(titlenumber));
                        ToastUtil.showToast(NameActivity.this,priceUpdeteBean.getMessage());

                    }else{
                        ToastUtil.showToast(NameActivity.this,priceUpdeteBean.getMessage());
                    }*/
                    break;

            }
        }
    };

    /**
     * 更新价格
     * @param goodscode 商品编号（id）
     * @param price 商品价格
     */
    private void Update(String goodscode, int price) {
        dialog.showLoading("loading...");
        long time = System.currentTimeMillis();
        OkGo.<String>post(HttpUtils.editGoods).tag(NameActivity.this)
                .params("maccode", serialNumber)
                .params("goodscode",goodscode)
                .params("price",price)
                .params("timestamp",time)
                .params("md5", CommonUtils2.GetMD5Code("maccode="+serialNumber+"&goodscode="+goodscode+"&timestamp="+time+"&accesskey="+password))
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Updatejson(response.body().toString());
                    }
                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dialog.dismiss();

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ToastUtil.showToast(NameActivity.this,"Network failure!");
                    }
                });

    }

    private void Updatejson(String json) {

        Gson gson = new Gson();
        priceUpdeteBean = gson.fromJson(json, PriceUpdeteBean.class);

        handler.sendEmptyMessage(2);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);
        ButterKnife.bind(this);
        Log.d("ACTIVITY：", getClass().getSimpleName());
        myApp = (MyApp) getApplication();
        serialNumber = PrefheUtils.getString(NameActivity.this, "serialNumber", "");
        password = PrefheUtils.getString(NameActivity.this, "password", "");
        dialog = new PromptDialog(NameActivity.this);
        proall = LitePal.findAll(ProductlibMolder.class);

        btnReturn = (Button) findViewById(R.id.btn_back);
        // 返回到主菜单
        btnReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                Intent toMenu = new Intent(NameActivity.this, MenuActivity.class);
                startActivity(toMenu);

                NameActivity.this.finish();
            }
        });

        /*listView = (ListView) findViewById(R.id.list);

        //调用显示的函数，把结果显示出来
        setListAdapter();
*/
        initDate();
        initView();
    }

    private void initDate() {
        dialog.showLoading("loading...");
        Log.d("zlc", "response.body().toString()");
        long time = System.currentTimeMillis();
        OkGo.<String>post(HttpUtils.allGoods).tag(NameActivity.this)
                .params("macid", serialNumber)
                .params("timestamp",time)
                .params("md5", CommonUtils2.GetMD5Code("macid=" + serialNumber + "&timestamp=" + time + "&accesskey=" + password))
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.d("zlc", response.body().toString());
                        analysisJson(response.body().toString());
                    }
                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dialog.dismiss();

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ToastUtil.showToast(NameActivity.this,"Network failure!");
                    }
                });



    }


    private void analysisJson(String json) {

        Gson gson = new Gson();
        preLibBean = gson.fromJson(json, PreLibBean.class);
        handler.sendEmptyMessage(1);
    }


    /**
     * 关闭软键盘
     */
    public void hintKeyBoard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        //  if (imm.isActive() && getCurrentFocus() != null) {
        //拿到view的token 不为空
        //  if (getCurrentFocus().getWindowToken() != null) {
        //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        //    }
        // }
    }


    private void initView() {
      /*  LinearLayoutManager manager = new LinearLayoutManager(NameActivity.this);
        titlerecy.setLayoutManager(manager);
        final NameAdapter adapter = new NameAdapter(NameActivity.this, proall);
        titlerecy.setAdapter(adapter);
        adapter.SetItemclike(new NameAdapter.Onclike() {
          *//*  @Override
            public void nameonclike(final int position) {
                namepopuwindow = new Namepopuwindow(NameActivity.this);
                namepopuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_name, null),Gravity.CENTER,0,-200);
                namepopuwindow.SetOnclike(new Namepopuwindow.Onclike() {
                    @Override
                    public void okclike(String s) {
                        if(!TextUtils.isEmpty(s)){
                            ProductlibMolder molder = new ProductlibMolder();
                            molder.setName(s);
                            molder.updateAll("post = ?",position+"");
                            proall = LitePal.findAll(ProductlibMolder.class);
                            adapter.update(proall,position);
                            hintKeyBoard();
                        }
                    }
                });
            }*//*

            @Override
            public void pricclike(final int position) {
                picepopuwindow = new Picepopuwindow(NameActivity.this);
                picepopuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_name, null), Gravity.CENTER, 0, -200);
                picepopuwindow.SetOnclike(new Picepopuwindow.Onclike() {
                    @Override
                    public void okclike(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            ProductlibMolder molder = new ProductlibMolder();
                            molder.setPrice(s);
                            molder.updateAll("post = ?", position + "");
                            proall = LitePal.findAll(ProductlibMolder.class);
                            adapter.update(proall, position);
                            hintKeyBoard();
                        }
                    }
                });
            }
        });*/
        //消失监听听


    }


    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed()");

        // 返回按钮的处理，不让程序退出的话，可以注解下面这行代码
        //super.onBackPressed();

    }
}
