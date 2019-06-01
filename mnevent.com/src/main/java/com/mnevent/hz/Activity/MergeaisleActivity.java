package com.mnevent.hz.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mnevent.hz.Adapter.MergeaisleAdapter;
import com.mnevent.hz.R;
import com.mnevent.hz.Utils.Log;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.Utils.CustomVRecyclerView;
import com.mnevent.hz.Utils.LogUtils;
import com.mnevent.hz.Utils.MergenumberPopuwindow;
import com.mnevent.hz.Utils.PrefheUtils;
import com.mnevent.hz.Utils.ToastUtil;

import org.litepal.LitePal;

import java.util.List;

import android_serialport_api.ComTrackMagex;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

/**
 * Created by Administrator on 2018/12/20.
 * 合并货道
 */

public class MergeaisleActivity extends Activity {


    @BindView(R.id.titlename)
    TextView titlename;
    @BindView(R.id.titlename2)
    TextView titlename2;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.topbar_title)
    RelativeLayout topbarTitle;
    @BindView(R.id.alllianban)
    TextView alllianban;
    @BindView(R.id.btn_alllianban)
    Button btnAlllianban;
    @BindView(R.id.title1)
    RelativeLayout title1;
    @BindView(R.id.tt)
    TextView tt;
    @BindView(R.id.title)
    RelativeLayout title;
    @BindView(R.id.line1)
    View line1;
    @BindView(R.id.get)
    TextView get;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.set)
    TextView set;
    @BindView(R.id.textlog)
    TextView textlog;
    @BindView(R.id.title4)
    RelativeLayout title4;
    @BindView(R.id.tt771tv44)
    TextView tt771tv44;
    @BindView(R.id.tt771)
    ImageView tt771;
    @BindView(R.id.tt771tv)
    TextView tt771tv;
    @BindView(R.id.tt7712)
    ImageView tt7712;
    @BindView(R.id.tt7712tv)
    TextView tt7712tv;
    @BindView(R.id.tt7713)
    ImageView tt7713;
    @BindView(R.id.tt7713tv)
    TextView tt7713tv;


    /*@BindView(R.id.recy)
    CustomVRecyclerView recy;*/
    @BindView(R.id.recy1)
    CustomVRecyclerView recy1;

    List<PathwayMolder> patall;

    MergeaisleAdapter adapter;
    //链板
    int allianban = 19;
    String refresh;




    String motorAddress14;
    String motorAddress58;
    String motorSteps14;
    String motorSteps58;

    String Addressok14;
    String Addressok58;
    String Steps14;
    String Steps58;

    int times = 3;

    PromptDialog dialog;

    String classname = "";
/*
    测试
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            return false;
        }
    });

*/

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    LogUtils.d("MergeaisleActivity::motorSteps14:"+motorSteps14);
                    LogUtils.d("MergeaisleActivity::motorAddress14:"+motorAddress14);
                    LogUtils.d("MergeaisleActivity::motorAddress58:"+motorAddress58);
                    LogUtils.d("MergeaisleActivity::motorSteps58:"+motorSteps58);
                    if(dialog != null){
                        dialog.dismiss();
                    }


                    //此隐藏代码为获取信息的整理，因暂无机器测试，暂时隐藏
                 /*   String steps = motorSteps14 + motorSteps58;
                    String address = motorAddress14 + motorAddress58;
                    String gather = "";
                    for(int i = 0;i < 8;i++){
                        for(int j = 0;j < 9;j++){
                            gather += steps.split(",")[i].substring(j*2,j*2+2)+"*"+address.split(",")[i].substring(j*2,j*2+2) + ",";
                        }
                        gather = gather+"\n";
                    }
                    textlog.setText(gather);*/


                    if(!motorAddress14.equals("") && !motorAddress58.equals("") && !motorSteps14.equals("") && !motorSteps58.equals("")){
                        textlog.setText("1-4层合并的结果："+motorAddress14+"\n"
                                +"5-8层合并的结果"+motorAddress58+"\n"
                                +"1-4层走步数"+motorSteps14+"\n"+
                                "5-8层走步数"+motorSteps58);
                    }else{
                        ToastUtil.showToast(MergeaisleActivity.this,"未获取到数据！");
                       // ToastUtil.showToast(MergeaisleActivity.this,"No data was retrieved！");
                    }
                    break;
                case 2:
                    if(dialog != null){
                        dialog.dismiss();
                    }
                    if(Addressok14.equals("ok") && Addressok58.equals("ok") && Steps14.equals("ok") && Steps58.equals("ok") && refresh.equals("ok")){
                        ToastUtil.showToast(MergeaisleActivity.this,"设置成功");
                        //ToastUtil.showToast(MergeaisleActivity.this,"successfully set");
                    }else{
                        ToastUtil.showToast(MergeaisleActivity.this,"设置失败");
                        //ToastUtil.showToast(MergeaisleActivity.this,"Setup failed");
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtils.d("MergeaisleActivity::zlc"+ getClass().getSimpleName());

        setContentView(R.layout.activity_mergeais);
        ButterKnife.bind(this);

        dialog = new PromptDialog(MergeaisleActivity.this);
        patall = LitePal.findAll(PathwayMolder.class);
        initView();
    }

    private void initView() {
        //判断客户是否手动选择连扳条目，如手动选择则使用客户选择的连扳条目
        if(PrefheUtils.getInt(MergeaisleActivity.this,"allianban",0) != 0){
            allianban = PrefheUtils.getInt(MergeaisleActivity.this,"allianban",0);
        }
        alllianban.setText("当前链板总数:"+allianban);
        //alllianban.setText("Total number of current links:"+allianban);
        final LinearLayoutManager manager = new LinearLayoutManager(MergeaisleActivity.this);
        recy1.setLayoutManager(manager);
        adapter = new MergeaisleAdapter(MergeaisleActivity.this, patall);
        recy1.setAdapter(adapter);

      /*  GridLayoutManager manager1 = new GridLayoutManager(MergeaisleActivity.this,9);
        recy.setLayoutManager(manager1);
        final MergeaislcksAdapter adapter1 = new MergeaislcksAdapter(MergeaisleActivity.this, patall);
        recy.setAdapter(adapter1);*/

        adapter.SetOnclike(new MergeaisleAdapter.Onclike() {
            //选择合并状态
            @Override
            public void imageonclike(int position, String type) {
                LogUtils.d("MergeaisleActivity::合并状态::position"+position+"type"+type);

                PathwayMolder molder = new PathwayMolder();
                LogUtils.d("MergeaisleActivity::type"+type);
                molder.setMergecode(type);
                molder.updateAll("post = ?",position+"");
                adapter.updatees(position,type);
            }

            //选择链条的一次性走步数
            @Override
            public void textsonclike(final int positions) {
                final MergenumberPopuwindow popuwindow = new MergenumberPopuwindow(MergeaisleActivity.this,"0");
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_mergeais, null), Gravity.CENTER,0,0);
                popuwindow.SetOnclike(new MergenumberPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {
                        PathwayMolder molder = new PathwayMolder();
                            String ss= position+"";
                        if(ss.length() > 1){

                            molder.setSteps(position+"");
                        }else{

                            molder.setSteps("0"+position);
                        }

                        molder.setNummax(allianban/(position));

                        molder.updateAll("code = ?",patall.get(positions).getCode());

                        adapter.update(position,positions);
                        popuwindow.dismiss();
                    }
                });
            }
        });
    }

    @OnClick({R.id.btn_back, R.id.btn_alllianban, R.id.get, R.id.set})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_alllianban:
                //选择链板总数
                final MergenumberPopuwindow popuwindow = new MergenumberPopuwindow(MergeaisleActivity.this,"0");
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_mergeais, null), Gravity.CENTER,0,0);
                /*popuwindow.setWidth(600);
                popuwindow.setHeight(400);*/
                popuwindow.SetOnclike(new MergenumberPopuwindow.Onckile() {
                    @Override
                    public void onclike(int position) {
                        PathwayMolder molder = new PathwayMolder();
                        molder.setChain(position);
                        molder.updateAll();
                        PrefheUtils.putInt(MergeaisleActivity.this,"allianban",position);
                        alllianban.setText("当前链板总数:"+position);
                      //  alllianban.setText("Total number of current links:"+position);
                        popuwindow.dismiss();
                    }
                });
                break;
            case R.id.get:
                //获取合并的当前信息
                dialog.showLoading("loading...");
                ComTrackMagex magex = new ComTrackMagex("");

                magex.openSerialPort();

                String status = magex.getStatus();
                LogUtils.d("MergeaisleActivity::status:"+status);

                motorAddress14 = magex.getMotorAddress14();
                motorAddress58 = magex.getMotorAddress58();
                motorSteps14 = magex.getMotorSteps14();
                motorSteps58 = magex.getMotorSteps58();


                times = 3;
                handler.sendEmptyMessage(1);
                textlog.setText("");
                magex.closeSerialPort();


                break;
            case R.id.set:
                dialog.showLoading("loading...");
                patall = LitePal.findAll(PathwayMolder.class);
                LogUtils.d("MergeaisleActivity::diyyige:::"+patall.get(0).getSteps());
                String addres = "";
                String addreses = "";
                String steps = "";
                String stepses = "";
                for(int i = 0;i<7;i++){
                    for (int j = 0;j < 9;j++){
                        Log.d("ceshi",i*9+j+""+patall.get(i*9+j).getMergecode());
                      addres += patall.get(i*9+j).getMergecode();
                      steps += patall.get(i*9+j).getSteps();
                    }
                }
                LogUtils.d("MergeaisleActivity::steps:"+steps+"addres:"+addres);
                for (int c = 0;c<7;c++){
                    addreses += addres.substring(c*18,c*18+18) + "00000000000000";
                    stepses += steps.substring(c*18,c*18+18) + "00000000000000";
                }
                ComTrackMagex magex1 = new ComTrackMagex("");

                    magex1.openSerialPort();
                LogUtils.d("MergeaisleActivity::addreses:"+addreses);
                LogUtils.d("MergeaisleActivity::steps"+steps);
                Addressok14 = magex1.setMotorAddress14(addreses.substring(0, 128));
                Addressok58 = magex1.setMotorAddress58(addreses.substring(128, 192));
                Steps14 = magex1.setMotorSteps14(stepses.substring(0, 128));
                Steps58 = magex1.setMotorSteps58(stepses.substring(128, 192));
                refresh = magex1.refreshEEPROM();

                times = 3;

                handler.sendEmptyMessage(2);
                magex1.closeSerialPort();

                break;
        }
    }


}
