package com.freshtribes.icecream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.presenter.MacParaPresenter;
import com.freshtribes.icecream.util.ProgressDialog;
import com.freshtribes.icecream.view.IMacParaView;

import java.util.HashMap;

public class MacParaActivity extends Activity implements IMacParaView,View.OnClickListener{
    private ThreadGroup tg = new ThreadGroup("MacParaActivityThreadGroup");

    private Dialog mProgressDialog;

    private MacParaPresenter lp;
    private static final int dispHandler_rtnGetCarrier = 0x101;

    private Button backBtn;
    private Button getCarrierBtn;

    private EditText accesskeyTV;
    private EditText maincodeTV;

    private EditText carrierET;

    private Spinner maingeneraltrack;
    private String[] mainsubgeneral = {"3","4","5","6","7"};

    private String[] mainsublevel = {"2","3","4","5","6","7","8","9","10"};
    private Spinner mainlevel1;
    private Spinner mainlevel2;
    private Spinner mainlevel3;
    private Spinner mainlevel4;
    private Spinner mainlevel5;
    private Spinner mainlevel6;
    private Spinner mainlevel7;

    private RelativeLayout mainlevel4rl;
    private RelativeLayout mainlevel5rl;
    private RelativeLayout mainlevel6rl;
    private RelativeLayout mainlevel7rl;

    private String initFour;

    private CheckBox cbfacepay;
    private CheckBox cbmic;

    private CheckBox cbmember;


    private CheckBox cbalipay;
    private CheckBox cbweixin;

    private CheckBox cbums;

    private CheckBox cbzxalipay;
    private CheckBox cbzxweixin;

    private Spinner spinnerTrack;
    private String[] oneTrack = {"0:中亚冰激凌升降机","1:Magex有主控_皮带或弹簧","2:Magex有主控_链板"};

    private Spinner spinnerType;
    private String[] oneType = {"冰激凌","综合机"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mac_para);

        // 生成Presenter
        lp = new MacParaPresenter(this,tg,(MyApp)getApplication());

        initFour = ((MyApp)getApplication()).getAccessKey() + ";" +
                ((MyApp)getApplication()).getMainMacID() + ";" +
                ((MyApp)getApplication()).getMainMacType() + ";" +"";

        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(this);
        getCarrierBtn = findViewById(R.id.btn_getcarrier);
        getCarrierBtn.setOnClickListener(this);

        accesskeyTV = findViewById(R.id.accesskeyet);
        accesskeyTV.setText(((MyApp)getApplication()).getAccessKey());
        //光标停到最后的位置
        CharSequence text = accesskeyTV.getText();
        if (text != null) {
            Spannable spanText = (Spannable)text;
            Selection.setSelection(spanText, text.length());
        }

        maincodeTV = findViewById(R.id.main_nameinput);
        maincodeTV.setText(((MyApp)getApplication()).getMainMacID());

        carrierET = findViewById(R.id.carriername);
        carrierET.setText("");

        maingeneraltrack = findViewById(R.id.SpinnerMainGeneralTrack);
        ArrayAdapter<String> maingeneraladapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsubgeneral);
        //设置下拉列表的风格
        maingeneraladapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        maingeneraltrack.setAdapter(maingeneraladapter);
        //添加事件Spinner事件监听
        maingeneraltrack.setOnItemSelectedListener(new MainGeneralSpinnerSelectedListener());
        //设置默认值
        maingeneraltrack.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsubgeneral.length;i++){
            if(Integer.parseInt(mainsubgeneral[i]) == ((MyApp) getApplication()).getMainGeneralLevelNum()){
                maingeneraltrack.setSelection(i);
                break;
            }
        }

        mainlevel1 = findViewById(R.id.SpinnerMainGeneralTrackLevel1);
        ArrayAdapter<String> mainlevel1adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel1adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel1.setAdapter(mainlevel1adapter);
        //添加事件Spinner事件监听
        mainlevel1.setOnItemSelectedListener(new Mainlevel1SpinnerSelectedListener());
        //设置默认值
        mainlevel1.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel1TrackCount()){
                mainlevel1.setSelection(i);
                break;
            }
        }

        mainlevel2 = findViewById(R.id.SpinnerMainGeneralTrackLevel2);
        ArrayAdapter<String> mainlevel2adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel2adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel2.setAdapter(mainlevel2adapter);
        //添加事件Spinner事件监听
        mainlevel2.setOnItemSelectedListener(new Mainlevel2SpinnerSelectedListener());
        //设置默认值
        mainlevel2.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel2TrackCount()){
                mainlevel2.setSelection(i);
                break;
            }
        }

        mainlevel3 = findViewById(R.id.SpinnerMainGeneralTrackLevel3);
        ArrayAdapter<String> mainlevel3adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel3adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel3.setAdapter(mainlevel3adapter);
        //添加事件Spinner事件监听
        mainlevel3.setOnItemSelectedListener(new Mainlevel3SpinnerSelectedListener());
        //设置默认值
        mainlevel3.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel3TrackCount()){
                mainlevel3.setSelection(i);
                break;
            }
        }

        mainlevel4 = findViewById(R.id.SpinnerMainGeneralTrackLevel4);
        ArrayAdapter<String> mainlevel4adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel4adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel4.setAdapter(mainlevel4adapter);
        //添加事件Spinner事件监听
        mainlevel4.setOnItemSelectedListener(new Mainlevel4SpinnerSelectedListener());
        //设置默认值
        mainlevel4.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel4TrackCount()){
                mainlevel4.setSelection(i);
                break;
            }
        }

        mainlevel5 = findViewById(R.id.SpinnerMainGeneralTrackLevel5);
        ArrayAdapter<String> mainlevel5adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel5adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel5.setAdapter(mainlevel5adapter);
        //添加事件Spinner事件监听
        mainlevel5.setOnItemSelectedListener(new Mainlevel5SpinnerSelectedListener());
        //设置默认值
        mainlevel5.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel5TrackCount()){
                mainlevel5.setSelection(i);
                break;
            }
        }

        mainlevel6 = findViewById(R.id.SpinnerMainGeneralTrackLevel6);
        ArrayAdapter<String> mainlevel6adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel6adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel6.setAdapter(mainlevel6adapter);
        //添加事件Spinner事件监听
        mainlevel6.setOnItemSelectedListener(new Mainlevel6SpinnerSelectedListener());
        //设置默认值
        mainlevel6.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel6TrackCount()){
                mainlevel6.setSelection(i);
                break;
            }
        }

        mainlevel7 = findViewById(R.id.SpinnerMainGeneralTrackLevel7);
        ArrayAdapter<String> mainlevel7adapter = new ArrayAdapter<>(this,R.layout.myspinnertext,mainsublevel);
        //设置下拉列表的风格
        mainlevel7adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mainlevel7.setAdapter(mainlevel7adapter);
        //添加事件Spinner事件监听
        mainlevel7.setOnItemSelectedListener(new Mainlevel7SpinnerSelectedListener());
        //设置默认值
        mainlevel7.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<mainsublevel.length;i++){
            if(Integer.parseInt(mainsublevel[i]) == ((MyApp) getApplication()).getMainGeneralLevel7TrackCount()){
                mainlevel7.setSelection(i);
                break;
            }
        }

        mainlevel4rl = findViewById(R.id.maingeneraltrack4);
        mainlevel5rl = findViewById(R.id.maingeneraltrack5);
        mainlevel6rl = findViewById(R.id.maingeneraltrack6);
        mainlevel7rl = findViewById(R.id.maingeneraltrack7);


        switch (((MyApp)getApplication()).getMainGeneralLevelNum()){
            case 3:
                mainlevel4rl.setVisibility(View.INVISIBLE);
            case 4:
                mainlevel5rl.setVisibility(View.INVISIBLE);
            case 5:
                mainlevel6rl.setVisibility(View.INVISIBLE);
            case 6:
                mainlevel7rl.setVisibility(View.INVISIBLE);
            case 7:
            default:
        }

        cbfacepay = (CheckBox) findViewById(R.id.facechk);
        if(((MyApp)getApplication()).getHaveFacePay() == 1){
            cbfacepay.setChecked(true);
        } else {
            cbfacepay.setChecked(false);
        }
        cbfacepay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if (arg1) {
                    ((MyApp)getApplication()).setHaveFacePay(1);
                } else {
                    ((MyApp)getApplication()).setHaveFacePay(0);
                }

                // checked
                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("havefacepay", ((MyApp)getApplication()).getHaveFacePay());  // 0表示没有麦克风
                editor.apply();//提交修改
            }
        });

        cbmic = (CheckBox) findViewById(R.id.micchk);
        if(((MyApp)getApplication()).getMichave() == 1){
            cbmic.setChecked(true);
        } else {
            cbmic.setChecked(false);
        }
        cbmic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if (arg1) {
                    ((MyApp)getApplication()).setMichave(1);
                } else {
                    ((MyApp)getApplication()).setMichave(0);
                }

                // checked
                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("michave", ((MyApp)getApplication()).getMichave());  // 0表示没有麦克风
                editor.apply();//提交修改
            }
        });


        cbmember = (CheckBox) findViewById(R.id.memberchk);
        if(((MyApp)getApplication()).getHaveMember() == 1){
            cbmember.setChecked(true);
        } else {
            cbmember.setChecked(false);
        }
        cbmember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if (arg1) {
                    ((MyApp)getApplication()).setHaveMember(1);
                } else {
                    ((MyApp)getApplication()).setHaveMember(0);
                }

                // checked
                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("havemember", ((MyApp)getApplication()).getHaveMember());
                editor.apply();//提交修改
            }
        });

        cbalipay = findViewById(R.id.alipaychk);
        if(((MyApp)getApplication()).getHaveAlipay() == 1){
            cbalipay.setChecked(true);
        } else {
            cbalipay.setChecked(false);
        }
        cbalipay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (arg1) {
                    // 标准的支付宝被选中了
                    ((MyApp)getApplication()).setHaveAlipay(1);

                    cbums.setChecked(false);
                    ((MyApp)getApplication()).setHaveUms(0);
                    cbzxalipay.setChecked(false);
                    ((MyApp)getApplication()).setHaveZXAlipay(0);

                    SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();//获取编辑器
                    editor.putInt("haveums", ((MyApp)getApplication()).getHaveUms());
                    editor.putInt("havezxalipay", ((MyApp)getApplication()).getHaveZXAlipay());
                    editor.apply();//提交修改
                } else {
                    ((MyApp)getApplication()).setHaveAlipay(0);
                }

                // checked
                SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("havealipay", ((MyApp)getApplication()).getHaveAlipay());
                editor.apply();//提交修改
            }
        });

        cbweixin = findViewById(R.id.weixinchk);
        if(((MyApp)getApplication()).getHavaWeixin() == 1){
            cbweixin.setChecked(true);
        } else {
            cbweixin.setChecked(false);
        }
        cbweixin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (arg1) {
                    // 标准的微信被选中
                    ((MyApp)getApplication()).setHavaWeixin(1);

                    cbums.setChecked(false);
                    ((MyApp)getApplication()).setHaveUms(0);
                    cbzxweixin.setChecked(false);
                    ((MyApp)getApplication()).setHaveZXWxpay(0);

                    SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();//获取编辑器
                    editor.putInt("haveums", ((MyApp)getApplication()).getHaveUms());
                    editor.putInt("havezxwxpay", ((MyApp)getApplication()).getHaveZXWxpay());
                    editor.apply();//提交修改

                } else {
                    ((MyApp)getApplication()).setHavaWeixin(0);
                }

                // checked
                SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("haveweixin", ((MyApp)getApplication()).getHavaWeixin());  // 0表示不启用微信支付（含反扫）
                editor.apply();//提交修改
            }
        });

        cbums = (CheckBox) findViewById(R.id.chinaumschk);
        if(((MyApp)getApplication()).getHaveUms() == 1){
            cbums.setChecked(true);
        } else {
            cbums.setChecked(false);
        }
        cbums.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if (arg1) {
                    ((MyApp)getApplication()).setHaveUms(1);

                    cbalipay.setChecked(false);
                    ((MyApp)getApplication()).setHaveAlipay(0);
                    cbweixin.setChecked(false);
                    ((MyApp)getApplication()).setHavaWeixin(0);
                    cbzxalipay.setChecked(false);
                    ((MyApp)getApplication()).setHaveZXAlipay(0);
                    cbzxweixin.setChecked(false);
                    ((MyApp)getApplication()).setHaveZXWxpay(0);

                    SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();//获取编辑器
                    editor.putInt("havealipay", ((MyApp)getApplication()).getHaveAlipay());
                    editor.putInt("havezxalipay", ((MyApp)getApplication()).getHaveZXAlipay());
                    editor.putInt("haveweixin", ((MyApp)getApplication()).getHavaWeixin());  // 0表示不启用微信支付（含反扫）
                    editor.putInt("havezxwxpay", ((MyApp)getApplication()).getHaveZXWxpay());
                    editor.apply();//提交修改
                } else {
                    ((MyApp)getApplication()).setHaveUms(0);
                }

                // checked
                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("haveums", ((MyApp)getApplication()).getHaveUms());
                editor.apply();//提交修改
            }
        });

        cbzxalipay = findViewById(R.id.zxalipaychk);
        if(((MyApp)getApplication()).getHaveZXAlipay() == 1){
            cbzxalipay.setChecked(true);
        } else {
            cbzxalipay.setChecked(false);
        }
        cbzxalipay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (arg1) {
                    // 中信银行的支付宝被选中
                    ((MyApp)getApplication()).setHaveZXAlipay(1);

                    cbums.setChecked(false);
                    ((MyApp)getApplication()).setHaveUms(0);
                    cbalipay.setChecked(false);
                    ((MyApp)getApplication()).setHaveAlipay(0);

                    SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();//获取编辑器
                    editor.putInt("haveums", ((MyApp)getApplication()).getHaveUms());
                    editor.putInt("havealipay", ((MyApp)getApplication()).getHaveAlipay());
                    editor.apply();//提交修改
                } else {
                    ((MyApp)getApplication()).setHaveZXAlipay(0);
                }

                // checked
                SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("havezxalipay", ((MyApp)getApplication()).getHaveZXAlipay());
                editor.apply();//提交修改
            }
        });

        cbzxweixin = findViewById(R.id.zxweixinchk);
        if(((MyApp)getApplication()).getHaveZXWxpay() == 1){
            cbzxweixin.setChecked(true);
        } else {
            cbzxweixin.setChecked(false);
        }
        cbzxweixin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (arg1) {
                    // 杭州移领的微信被选中
                    ((MyApp)getApplication()).setHaveZXWxpay(1);

                    cbums.setChecked(false);
                    ((MyApp)getApplication()).setHaveUms(0);
                    cbweixin.setChecked(false);
                    ((MyApp)getApplication()).setHavaWeixin(0);

                    SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();//获取编辑器
                    editor.putInt("haveums", ((MyApp)getApplication()).getHaveUms());
                    editor.putInt("haveweixin", ((MyApp)getApplication()).getHavaWeixin());  // 0表示不启用微信支付（含反扫）
                    editor.apply();//提交修改
                } else {
                    ((MyApp)getApplication()).setHaveZXWxpay(0);
                }

                SharedPreferences sp = MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("havezxwxpay", ((MyApp)getApplication()).getHaveZXWxpay());  // 0表示不启用微信支付（含反扫）
                editor.apply();//提交修改
            }
        });


        spinnerTrack = (Spinner) findViewById(R.id.SpinnerTrack);
        ArrayAdapter<String> oneleveladapterMDB = new ArrayAdapter<String>(this,R.layout.myspinnertext,oneTrack);
        //设置下拉列表的风格
        oneleveladapterMDB.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinnerTrack.setAdapter(oneleveladapterMDB);
        //添加事件Spinner事件监听
        spinnerTrack.setOnItemSelectedListener(new OneLevelMDBSpinnerSelectedListener());
        //设置默认值
        spinnerTrack.setVisibility(View.VISIBLE);
        //设定初始值
        for(int i=0;i<oneTrack.length;i++){
            if(Integer.parseInt(oneTrack[i].split(":")[0]) == ((MyApp)getApplication()).getDevicetype()){
                spinnerTrack.setSelection(i);
                break;
            }
        }

        spinnerType = (Spinner) findViewById(R.id.SpinnerType);
        ArrayAdapter<String> oneleveladapterType = new ArrayAdapter<String>(this,R.layout.myspinnertext,oneType);
        //设置下拉列表的风格
        oneleveladapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinnerType.setAdapter(oneleveladapterType);
        //添加事件Spinner事件监听
        spinnerType.setOnItemSelectedListener(new OneLevelTypeSpinnerSelectedListener());
        //设置默认值
        spinnerType.setVisibility(View.VISIBLE);
        //设定初始值
        if(((MyApp)getApplication()).getMainMacType().equals("icecream")){
            spinnerType.setSelection(0);
        } else {
            spinnerType.setSelection(1);
        }
    }

    class OneLevelMDBSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selectedCom = Integer.parseInt(oneTrack[arg2].split(":")[0]);
            if(selectedCom != ((MyApp)getApplication()).getDevicetype()){
                ((MyApp)getApplication()).setDevicetype(selectedCom);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("devicetype", ((MyApp)getApplication()).getDevicetype());
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    class OneLevelTypeSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            if(arg2 == 0){
                ((MyApp)getApplication()).setMainMacType("icecream");

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putString("mactype", ((MyApp)getApplication()).getMainMacType());
                editor.apply();//提交修改
            } else {
                ((MyApp)getApplication()).setMainMacType("general");

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putString("mactype", ((MyApp)getApplication()).getMainMacType());
                editor.apply();//提交修改
            }


        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_back:
                String newaccesskey = accesskeyTV.getText().toString();

                if(newaccesskey.length() != 8){
                    AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("访问服务器密码必须为8位字母数字的混合")
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                } else {

                    String newFour = newaccesskey + ";" + maincodeTV.getText().toString() + ";" +
                            ((MyApp)getApplication()).getMainMacType() +";"+"";
                    if(initFour.equals(newFour)) {
                        SharedPreferences sp =
                                MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();//获取编辑器

                        editor.putString("accessKey", newaccesskey);
                        editor.putString("mainMacID", maincodeTV.getText().toString());
                        editor.apply();//提交修改

                        ((MyApp) getApplication()).setAccessKey(newaccesskey);
                        ((MyApp) getApplication()).setMainMacID(maincodeTV.getText().toString());

                        // 关闭presenter的所有线程，然后跳转
                        lp.setStopThread();
                        long tgnow = System.currentTimeMillis();
                        while(tg.activeCount()>0){
                            for(int i=0;i<1000;i++);
                            if(System.currentTimeMillis() - tgnow >  3000) break;
                        }
                        tg.destroy();

                        Intent toMenu = new Intent(MacParaActivity.this, MenuActivity.class);
                        startActivity(toMenu);
                        MacParaActivity.this.finish();
                    } else {
                        AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("提示")
                                .setMessage("请先点击【获取所属运营商】验证输入信息通过后，再【返回】")
                                .setPositiveButton("确定", null)
                                .create();
                        al.setCancelable(false);
                        al.show();
                    }

                }

                break;

            case R.id.btn_getcarrier:
                carrierET.setText("");
                String testaccesskey = accesskeyTV.getText().toString();

                if(testaccesskey.length() != 8){
                    AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("访问服务器密码必须为8位数字")
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                } else {
                    mProgressDialog = ProgressDialog.createLoadingDialog(MacParaActivity.this,"联网获取信息,请稍候...");
                    mProgressDialog.show();

                    lp.getCarrier(maincodeTV.getText().toString(),
                            "",
                            ((MyApp)getApplication()).getMainMacType(),
                            testaccesskey);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void getCarrier(int rtncode,String rtnmsg,String carriername){
        HashMap<String,Object> hm = new HashMap<>();
        hm.put("code",rtncode);
        hm.put("msg",rtnmsg);
        hm.put("carriername",carriername);
        Message message = dispHandler.obtainMessage(dispHandler_rtnGetCarrier);
        message.obj = hm;
        dispHandler.sendMessage(message);
    }

    /**
     * Handle线程，显示处理结果
     */
    Handler dispHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // 处理消息
            switch (msg.what) {
                case dispHandler_rtnGetCarrier:

                    if(mProgressDialog!=null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    HashMap<String,Object> hm = (HashMap<String,Object>)msg.obj;
                    int rtn_code = (Integer)hm.get("code");
                    String rtn_msg = (String)hm.get("msg");
                    String rtn_carriername = (String)hm.get("carriername");

                    if(rtn_code == 0){
                        carrierET.setText(rtn_carriername);
                        initFour = accesskeyTV.getText().toString() + ";" + maincodeTV.getText().toString() + ";" +
                                ((MyApp)getApplication()).getMainMacType() +";"+"";
                    } else {
                        AlertDialog al = new AlertDialog.Builder(MacParaActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("提示")
                                .setMessage("错误信息："+rtn_msg)
                                .setPositiveButton("确定", null)
                                .create();
                        al.setCancelable(false);
                        al.show();
                    }


                    break;


                default:
                    break;
            }
            return false;
        }
    });

    private class MainGeneralSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsubgeneral[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevelNum()){
                ((MyApp) getApplication()).setMainGeneralLevelNum(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevelNum", selected);
                editor.apply();//提交修改

                mainlevel4rl.setVisibility(View.VISIBLE);
                mainlevel5rl.setVisibility(View.VISIBLE);
                mainlevel6rl.setVisibility(View.VISIBLE);
                mainlevel7rl.setVisibility(View.VISIBLE);
                switch (((MyApp)getApplication()).getMainGeneralLevelNum()){
                    case 3:
                        mainlevel4rl.setVisibility(View.INVISIBLE);
                    case 4:
                        mainlevel5rl.setVisibility(View.INVISIBLE);
                    case 5:
                        mainlevel6rl.setVisibility(View.INVISIBLE);
                    case 6:
                        mainlevel7rl.setVisibility(View.INVISIBLE);
                    case 7:
                    default:
                }

            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel1SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel1TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel1TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel1TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel2SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel2TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel2TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel2TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel3SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel3TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel3TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel3TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel4SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel4TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel4TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel4TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel5SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel5TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel5TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel5TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel6SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel6TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel6TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel6TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class Mainlevel7SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            int selected = Integer.parseInt(mainsublevel[arg2]);
            if(selected != ((MyApp) getApplication()).getMainGeneralLevel7TrackCount()){
                ((MyApp) getApplication()).setMainGeneralLevel7TrackCount(selected);

                SharedPreferences sp =
                        MacParaActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                editor.putInt("mainGeneralLevel7TrackCount", selected);
                editor.apply();//提交修改
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

}
