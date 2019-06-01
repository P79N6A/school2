package com.mnevent.hz.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.Button;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.mnevent.hz.Adapter.ProselectAdapter;
import com.mnevent.hz.App.MyApp;
import com.mnevent.hz.R;
import com.mnevent.hz.Utils.CommonUtils2;
import com.mnevent.hz.Utils.DateUtils;
import com.mnevent.hz.Utils.Gmethod;
import com.mnevent.hz.Utils.HttpUtils;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.Utils.PronumberPopuwindow;
import com.mnevent.hz.Utils.ToastUtil;
import com.mnevent.hz.bean.UploadInformationBean;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.litemolder.ProductdisplayMolder;
import com.mnevent.hz.litemolder.ProductlibMolder;
import com.mnevent.hz.litemolder.TrackstatusMolder;


import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockActivity extends Activity {
    private static String TAG = "Stock" + Gmethod.TAG_plus;
    @BindView(R.id.recy)
    RecyclerView recy;
    private MyApp myApp;

    /**
     * 返回
     */
    private Button btnReturn;

    // private ListView listView;

    // 显示的列表
    ArrayList<Map<String, String>> list = null;


    //产品库
    List<ProductlibMolder> parliball;

    //轨道库
    List<PathwayMolder> patall;
    List<PathwayMolder> patalls = new ArrayList<>();



    UploadInformationBean uploadInformationBean;


    List<ProductdisplayMolder> productlist;
    ProductdisplayMolder productdisplayMolder;
    ProductlibMolder productlibMolder;
    TrackstatusMolder trackstatusMolder;

    PathwayMolder molders;

    ProselectAdapter adapter;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        ButterKnife.bind(this);

        myApp = (MyApp) getApplication();
        parliball = LitePal.findAll(ProductlibMolder.class);

        patall = LitePal.findAll(PathwayMolder.class);
        btnReturn = (Button) findViewById(R.id.btn_back);
        // 返回到主菜单
        btnReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                String drive = PrefheUtils.getString(StockActivity.this, "drive", "");

                String serialNumbers = PrefheUtils.getString(StockActivity.this, "serialNumber", "");

                String passwords = PrefheUtils.getString(StockActivity.this, "password", "");

                String phones = PrefheUtils.getString(StockActivity.this, "phone", "");

                long time = System.currentTimeMillis();
                String ymds = DateUtils.getDateTimeByMillisecond(time + "", "yyyyMMddHHmmss");
                Intent toMenu = new Intent(StockActivity.this, MenuActivity.class);
                startActivity(toMenu);
                //商品编号
                String Product_id = "";
                //满仓数
                String Full_number = "";
                //库存
                String inventory = "";
                //价格
                String price ="";

                if(patall.size() == 80){
                    for (int i = 0;i<patall.size();i++){
                        if(TextUtils.isEmpty(patall.get(i).getPrecode())){
                            Product_id+=patall.get(i).getPrecode()+"a,";

                        }else{
                            Product_id+=patall.get(i).getPrecode()+",";

                        }

                        inventory += patall.get(i).getInventory()+",";
                        price += patall.get(i).getMergecode()+",";
                        Full_number += patall.get(i).getNummax()+",";
                    }
                }else{
                    for (int i = 0;i<patall.size();i++){
                        if(TextUtils.isEmpty(patall.get(i).getPrecode())){
                            Product_id+=patall.get(i).getPrecode()+"a,";

                        }else{
                            Product_id+=patall.get(i).getPrecode()+",";

                        }
                        inventory += patall.get(i).getInventory()+",";
                        price += patall.get(i).getMergecode()+",";
                        Full_number += patall.get(i).getNummax()+",";
                    }
                    Product_id = Product_id+"a,"+"a,"+"a,"+"a,"+"a,"+"a,"+"a,"+"a";

                    inventory = inventory+0+","+0+","+0+","+0+","+0+","+0+","+0+","+0;
                    price = price+00+","+00+","+00+","+00+","+00+","+00+","+00+","+00;

                    Full_number = Full_number+0+","+0+","+0+","+0+","+0+","+0+","+0+","+0;
                }
                Log.d("zlc","Product_id:"+Product_id);
                Log.d("zlc","Full_number:"+Full_number);
                Log.d("zlc","price:"+price);
                Log.d("zlc","inventory:"+inventory);
                OkGo.<String>post(HttpUtils.getTrack).tag(StockActivity.this)
                        .params("maccode",serialNumbers)
                        .params("accesskey",passwords)
                        .params("macnowtime",ymds)
                        .params("mobile",phones)
                        .params("tc",Product_id)
                        .params("tf",Full_number)
                        .params("tn",inventory)
                        .params("ts",price)
                        .params("timestamp",time)
                        .params("md5", CommonUtils2.GetMD5Code("maccode="+serialNumbers+"&accesskey="+passwords+"&macnowtime="+ymds
                        +"&timestamp="+time))
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                uploadBean(response.body().toString());
                            }
                            @Override
                            public void onFinish() {
                                super.onFinish();

                            }

                            @Override
                            public void onError(Response<String> response) {
                                super.onError(response);
                                ToastUtil.showToast(StockActivity.this,"Network failure!");
                            }
                        });

                StockActivity.this.finish();
            }
        });


        initDate();
    }
    /**
     * 上传数据解析
     * @param json
     */
    private void uploadBean(String json) {
        Gson gson = new Gson();
        uploadInformationBean = gson.fromJson(json, UploadInformationBean.class);
        handler.sendEmptyMessage(2);
    }

    private void initDate() {





        for (int i = 0; i < patall.size(); i++) {
            if (patall.get(i).getMergecode().equals("01") && patall.get(i).getPds().equals("1")) {
               /* if(TextUtils.isEmpty(patall.get(i).getMergecode())){
                  patall
                }*/
                patalls.add(patall.get(i));
            }
        }


        final LinearLayoutManager manager = new LinearLayoutManager(StockActivity.this);
        recy.setLayoutManager(manager);
        adapter = new ProselectAdapter(StockActivity.this, patalls);
        recy.setAdapter(adapter);

        adapter.SetItemOnclike(new ProselectAdapter.ItemOnclike() {
            @Override
            public void proclike(final int positions, String s, final String text) {
              /*  final ProselectPopuwindow popuwindow = new ProselectPopuwindow(StockActivity.this, parliball, text);
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_stock, null), Gravity.CENTER, 0, 0);
                popuwindow.SetOnclike(new ProselectPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {
                        Log.d("zlc", "positions:" + position);
                        Log.d("zlc", "code:" + text);
                        molders = new PathwayMolder();
                        molders.setPrecode(parliball.get(position).getCode());
                        molders.setName(parliball.get(position).getName());
                        molders.setImage(parliball.get(position).getImage());
                        molders.setPd(0);
                        molders.setPrice(parliball.get(position).getPrice());
                        molders.setTexts(parliball.get(position).getTexts());
                        molders.setPretype(parliball.get(position).getType());
                        molders.setPds("1");
                        molders.updateAll("code = ?", text);
                        patall = LitePal.findAll(PathwayMolder.class);
                        patalls.clear();
                        for (int i = 0; i < patall.size(); i++) {
                            if (patall.get(i).getPds().equals("1")) {
                                patalls.add(patall.get(i));
                            }
                        }
                        adapter.update(patalls, positions);

                      *//*  adapter = new ProselectAdapter(StockActivity.this,proall,patall);
                        recy.setAdapter(adapter);*//*
                        popuwindow.dismiss();
                    }
                });*/

                Intent intent = new Intent(StockActivity.this,ProductoptionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("number",positions);
                bundle.putString("text",text);
                intent.putExtras(bundle);
                startActivityForResult(intent,1);
                Log.d("ceshi","进入");
            }

            @Override
            public void invclike(final int positions, String s, final String text) {

                final PronumberPopuwindow popuwindows = new PronumberPopuwindow(StockActivity.this, parliball, s);
                popuwindows.showAtLocation(getLayoutInflater().inflate(R.layout.activity_stock, null), Gravity.CENTER, 0, 0);
                popuwindows.SetOnclike(new PronumberPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {
                        molders = new PathwayMolder();
                        List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);

                        if (position > all.get(positions).getNummax()) {
                           // ToastUtil.showToast(StockActivity.this, "库存不能大于满仓数");
                            ToastUtil.showToast(StockActivity.this, "Inventory cannot be larger than full stock");
                        } else {

                            molders.setInventory(position + "");
                            molders.updateAll("code = ?", text);
                            patall = LitePal.findAll(PathwayMolder.class);
                            patalls.clear();
                            for (int i = 0; i < patall.size(); i++) {
                                if (patall.get(i).getMergecode().equals("01") && patall.get(i).getPds().equals("1")) {
                                    patalls.add(patall.get(i));
                                }
                            }
                            adapter.update(patalls, positions);

                            popuwindows.dismiss();
                        }

                    }
                });
            }

            @Override
            public void setonclike(final int positions, String toString, final String s) {
                final PronumberPopuwindow popuwindows = new PronumberPopuwindow(StockActivity.this, parliball, toString);
                popuwindows.showAtLocation(getLayoutInflater().inflate(R.layout.activity_stock, null), Gravity.CENTER, 0, 0);
                popuwindows.SetOnclike(new PronumberPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {
                        molders = new PathwayMolder();
                        List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class, positions + 1);
                        molders.setNummax(position);
                        molders.updateAll("code = ?", s);
                        patall = LitePal.findAll(PathwayMolder.class);
                        patalls.clear();
                        for (int i = 0; i < patall.size(); i++) {
                            if (patall.get(i).getMergecode().equals("01")&&patall.get(i).getPds().equals("1")) {
                                patalls.add(patall.get(i));
                            }
                        }
                        adapter.update(patalls, positions);
                        popuwindows.dismiss();
                    }
                });
            }

            @Override
            public void getonclike(int positions, String code) {
                patall = LitePal.findAll(PathwayMolder.class);
                molders = new PathwayMolder();

                if (positions == 0) {

                } else {
                    if(patall.get(positions-1).getName().equals("")){
                        return;
                    }
                    molders.setNummax(patall.get(positions - 1).getNummax());
                    molders.setPd(patall.get(positions - 1).getNummax());
                    molders.setPds(patall.get(positions-1).getPds());
                    molders.setPost(patall.get(positions-1).getPost());

                    molders.setNumnow(patall.get(positions-1).getNumnow());

                    molders.setSteps(patall.get(positions-1).getSteps());
                    molders.setImage(patall.get(positions-1).getImage());
                    molders.setTexts(patall.get(positions-1).getTexts());
                    molders.setPretype(patall.get(positions-1).getPretype());
                    molders.setPrecode(patall.get(positions-1).getPrecode());
                    molders.setName(patall.get(positions-1).getName());
                    molders.setPrice(patall.get(positions-1).getPrice());
                    molders.setInventory(patall.get(positions-1).getInventory());
                    molders.setUp(patall.get(positions-1).getUp());
                    molders.setGoodscode(patall.get(positions-1).getGoodscode());


                    }

                molders.updateAll("code = ?",code);
                patall = LitePal.findAll(PathwayMolder.class);
                patalls.clear();
                for (int i = 0; i < patall.size(); i++) {
                    if (patall.get(i).getPds().equals("1")) {
                        patalls.add(patall.get(i));
                    }
                }
                adapter.update(patalls, positions);



            }
        });
    }


    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed()");

        // 返回按钮的处理，不让程序退出的话，可以注解下面这行代码
        //super.onBackPressed();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            switch (resultCode){
                case 1:
                    int number = data.getExtras().getInt("number");

                    patall = LitePal.findAll(PathwayMolder.class);
                    patalls.clear();
                    for (int i = 0; i < patall.size(); i++) {
                        if (patall.get(i).getMergecode().equals("01")&& patall.get(i).getPds().equals("1")) {
                            patalls.add(patall.get(i));
                        }
                    }
                    adapter.update(patalls, number);
                    break;
            }
        }
    }
}
