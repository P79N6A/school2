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

import com.mnevent.hz.Adapter.PriceContextAdapter;
import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.R;
import com.mnevent.hz.bean.PreLibBean;
import com.mnevent.hz.bean.PriceUpdeteBean;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.Utils.Gmethod;
import com.mnevent.hz.Utils.Namepopuwindow;
import com.mnevent.hz.Utils.Picepopuwindow;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.Utils.ToastUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.leefeng.promptlibrary.PromptDialog;


/**
 * 本地数据库价格修改
 */

public class PriceActivity extends Activity {
    private static String TAG = "Name" + Gmethod.TAG_plus;
    @BindView(R.id.titlename)
    TextView titlename;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.topbar_title)
    RelativeLayout topbarTitle;
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


    //具体商品适配器
    PriceContextAdapter contextadapter;

    Picepopuwindow picepopuwindow;
    Namepopuwindow namepopuwindow;
    int titlenumber;
    int contextnumber;
    int price;

    List<PathwayMolder> all;
    List<PathwayMolder> pathwayall = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    dialog.dismiss();



                        LinearLayoutManager manager1 = new LinearLayoutManager(PriceActivity.this);
                        contentrecy.setLayoutManager(manager1);
                        contextadapter = new PriceContextAdapter(PriceActivity.this, pathwayall);
                        contentrecy.setAdapter(contextadapter);


                        contextadapter.SetOnclike(new PriceContextAdapter.Onclike() {
                            @Override
                            public void btonclike(final int position) {



                                picepopuwindow = new Picepopuwindow(PriceActivity.this);
                                picepopuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_name, null), Gravity.CENTER, 0, -200);
                                picepopuwindow.SetOnclike(new Picepopuwindow.Onclike() {
                                    @Override
                                    public void okclike(String s) {
                                        if (!TextUtils.isEmpty(s)) {

                                            contextnumber = position;
                                            pathwayall.get(position).setPrice(s);
                                            PathwayMolder molder = new PathwayMolder();
                                            molder.setPrice(s);
                                            molder.updateAll("code=?",pathwayall.get(position).getCode()+"");
                                            contextadapter.setUpDate(pathwayall);
                                            hintKeyBoard();
                                        }
                                    }
                                });


                            }
                        });

                    break;



            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price);
        ButterKnife.bind(this);
        Log.d("ACTIVITY：", getClass().getSimpleName());
        myApp = (MyApp) getApplication();
        serialNumber = PrefheUtils.getString(PriceActivity.this, "serialNumber", "");
        password = PrefheUtils.getString(PriceActivity.this, "password", "");
        dialog = new PromptDialog(PriceActivity.this);


        btnReturn = (Button) findViewById(R.id.btn_back);
        // 返回到主菜单
        btnReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                Intent toMenu = new Intent(PriceActivity.this, MenuActivity.class);
                startActivity(toMenu);

                PriceActivity.this.finish();
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

        all = LitePal.findAll(PathwayMolder.class);
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getPds().equals("1") && Integer.parseInt(all.get(i).getInventory()) > 0 && all.get(i).getUp().equals("1")) {
                Log.d("zlc", "Proalle.get(i).getUp():" + all.get(i).getUp() + i);
                pathwayall.add(all.get(i));
            }
        }
        if(pathwayall.size() == 0){
            ToastUtil.showToast(PriceActivity.this,"没有在本地添加任何项");
            finish();

            return;
        }
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
