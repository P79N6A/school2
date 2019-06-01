package com.mnevent.hz.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mnevent.hz.Adapter.ProductoptiontwoAdapter;
import com.mnevent.hz.R;
import com.mnevent.hz.Utils.Log;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.Utils.ToastUtil;
import com.mnevent.hz.bean.PreLibBean;
import com.mnevent.hz.bean.ProductoptiononeBean;
import com.mnevent.hz.litemolder.LocalgoodsMolder;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.litemolder.ProductdisplayMolder;
import com.mnevent.hz.litemolder.ProductlibMolder;
import com.mnevent.hz.litemolder.TrackstatusMolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

/**
 * Created by zyand on 2018/12/28.
 * 商品选择
 */

public class ProductoptionActivity extends Activity {

    @BindView(R.id.back)
    TextView back;
    /* @BindView(R.id.recy1)
     RecyclerView recy1;*/
    @BindView(R.id.recy2)
    RecyclerView recy2;


    List<LocalgoodsMolder> goodslist;
    List<LocalgoodsMolder> goodslistes = new ArrayList<>();
    //显示页
    int size = 1;
    @BindView(R.id.smart)
    SmartRefreshLayout smart;

    private String[] types = {"Eyebrow", "Blush", "Mouth", "Eyeline", "Eyeshadow", "Eyelash", "Powdery", "Contactlenses", "Glasses"};
    private List<ProductoptiononeBean> bean = new ArrayList<>();
    List<ProductlibMolder> libes = new ArrayList<>();

    PreLibBean preLibBean;
    int titlenumber = 0;
    int number;
    String text;
    PromptDialog promptDialog;

    ProductdisplayMolder productdisplayMolder;
    ProductlibMolder productlibMolder;
    TrackstatusMolder trackstatusMolder;

    //编号
    String serialNumber;
    //密码
    String password;
    //新商品库id
    int ids;
    LinearLayoutManager manager1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //  promptDialog.dismiss();

                       /* preLibBean.getGoodsInfoVo().getAllGoods().get(0).setPd("1");
                        //类别列
                        final LinearLayoutManager manager = new LinearLayoutManager(ProductoptionActivity.this);
                        recy1.setLayoutManager(manager);
                        ProductoptiononeAdapter adapter = new ProductoptiononeAdapter(ProductoptionActivity.this, preLibBean);
                        recy1.setAdapter(adapter);*/
                    //商品列
                    manager1 = new LinearLayoutManager(ProductoptionActivity.this);
                    recy2.setLayoutManager(manager1);
                    final ProductoptiontwoAdapter adapter1 = new ProductoptiontwoAdapter(ProductoptionActivity.this, goodslistes);
                    recy2.setAdapter(adapter1);
                    MoveToPosition((size-1)*20+1);

                    //类别选择
                       /* adapter.SetOnclike(new ProductoptiononeAdapter.Onclike() {
                            @Override
                            public void backles(int position) {

                                adapter1.setUpDate(preLibBean.getGoodsInfoVo().getAllGoods().get(position));
                                titlenumber = position;
                            }
                        });*/
                    //商品选择
                    adapter1.SetOnclike(new ProductoptiontwoAdapter.Onclike() {
                        @Override
                        public void btonclike(int position) {

                               /* Log.d("zlc","productlist.size:"+productlist.size());
                                Log.d("zlc","productlist:"+productlist.get(0).getPathway().get(0).getCode());
                                Log.d("zlc","productlist:"+productlist.get(0).getPathwayMolder().getGoodscode());*/
                            Intent intent = new Intent(ProductoptionActivity.this, StockActivity.class);
                            //PreLibBean.PngurlarrayBean goodsBean = preLibBean.getPngurlarray().get(position);
                            LocalgoodsMolder goodsBean = goodslistes.get(position);
                            PathwayMolder molders = new PathwayMolder();
                            molders.setGoodscode(goodsBean.getGoodscode());
                            molders.setName(goodsBean.getName());
                            molders.setImage(goodsBean.getImage());
                            molders.setPd(0);
                            molders.setUp("1");

                            molders.setPrice(goodsBean.getPrice());


                            molders.setPds("1");

                            molders.updateAll("code = ?", text);


                            Bundle bundle = new Bundle();
                            bundle.putInt("number", number);
                            intent.putExtras(bundle);
                            setResult(1, intent);
                            finish();
                        }
                    });

                    break;
                case 2:
                    if (msg.obj == null) {
                        promptDialog.dismiss();
                        ToastUtil.showToast(ProductoptionActivity.this, "Network failure!");
                    }
                    break;
            }
        }
    };

    public void MoveToPosition(int n) {
        manager1.scrollToPosition(n);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_productoption);
        ButterKnife.bind(this);
        Log.d("zlc", "打开了.........");

        //promptDialog.showLoading("加载中...");
        Bundle extras = getIntent().getExtras();
        number = extras.getInt("number");
        text = extras.getString("text");
        serialNumber = PrefheUtils.getString(ProductoptionActivity.this, "serialNumber", "");
        password = PrefheUtils.getString(ProductoptionActivity.this, "password", "");
        goodslist = LitePal.findAll(LocalgoodsMolder.class);
        Log.d("ceshi","++++++"+goodslist.size());
        if (goodslist.size() == 0) {
            Log.d("ceshi","++++++---"+goodslist.size());
            finish();
            ToastUtil.showToast(ProductoptionActivity.this, "请先更新商品");
            return;
        }

        initDate();
        initView();

    }

    private void initDate() {



      /*  long time = System.currentTimeMillis();
        OkGo.<String>post(HttpUtils.allGoods).tag(ProductoptionActivity.this)
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
                        ToastUtil.showToast(ProductoptionActivity.this, "Network failure!");
                    }
                });*/
        /*promptDialog = new PromptDialog(ProductoptionActivity.this);*/


        Log.d("zlc", "打开了.........wwwwww");

        for (int i = (size-1)*20;i<size*20;i++){
            goodslistes.add(goodslist.get(i));
        }
        handler.sendEmptyMessage(1);

    }


    private void analysisJson(String json) {

        Gson gson = new Gson();
        preLibBean = gson.fromJson(json, PreLibBean.class);
        handler.sendEmptyMessage(1);
    }

    private void initView() {

        smart.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                smart.finishRefresh(100);
                size = 1;
                goodslistes.clear();
                initDate();
            }
        });

        smart.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                smart.finishLoadMore(100);
                size = size+1;
                initDate();
            }
        });
    }

    @OnClick(R.id.back)
    public void onViewClicked() {
        finish();
    }
}
