package com.freshtribes.icecream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.freshtribes.icecream.app.MyApp;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android_serialport_api.ComLift;

public class TestActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "TestActivity";
    private SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    protected static final int xian_shi_data = 0x418;
    protected static final int xian_shi_error = 0x419;

    // 对串口正在操作中
    private boolean isComLifting = false;
    // 强制停止
    private boolean isStopping = false;
    // 测试类别
    private String testtype = "";

    private Spinner spinnerOneLevel;
    private Spinner spinnerForLevel;
    private Spinner spinnerOneTrack;

    private String[] onelevelarray = null;
    private String selectedlevel = null;

    private String[] forarray = new String[]{"1","2","3","4","5","10"};
    private String selectedforlevel = "1";

    private String[] onetrackarray = null;
    private String selectedtrack = null;

    private String test_resultstr = "";
    private EditText test_result = null;

    private TextView errotrack = null;

    private String hinttvstr = "";
    private TextView hinttv = null;

    private String mtype;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        Button cleartbn = findViewById(R.id.btn_clear);
        cleartbn.setOnClickListener(this);

        Button getstatus = findViewById(R.id.getstatuebtn);
        getstatus.setOnClickListener(this);


        Button onelevel_bt = findViewById(R.id.onelevel_bt);
        onelevel_bt.setOnClickListener(this);

        Button onetrack_bt = findViewById(R.id.onetrack_bt);
        onetrack_bt.setOnClickListener(this);


        spinnerOneLevel = (Spinner) findViewById(R.id.SpinnerOneLevel);
        spinnerForLevel = (Spinner) findViewById(R.id.SpinnerFor);
        spinnerOneTrack = (Spinner) findViewById(R.id.SpinnerOneTrack);

        test_result = (EditText)findViewById(R.id.test_result_text);
        hinttv = findViewById(R.id.hintinfo);

        // 单层
        String trackfloor = ""+ "全部";
        for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
            trackfloor = trackfloor + "," + "第" + (i+1) + "层";
        }
        onelevelarray = trackfloor.split(",");

        ArrayAdapter<String> oneleveladapter = new ArrayAdapter<String>(this,R.layout.myspinnertext,onelevelarray);
        //设置下拉列表的风格
        oneleveladapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinnerOneLevel.setAdapter(oneleveladapter);
        //添加事件Spinner事件监听
        spinnerOneLevel.setOnItemSelectedListener(new OneLevelSpinnerSelectedListener());
        //设置默认值
        spinnerOneLevel.setVisibility(View.VISIBLE);

        // 循环次数
        ArrayAdapter<String> foradapter = new ArrayAdapter<String>(this,R.layout.myspinnertext,forarray);
        //设置下拉列表的风格
        foradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinnerForLevel.setAdapter(foradapter);
        //添加事件Spinner事件监听
        spinnerForLevel.setOnItemSelectedListener(new forLevelSpinnerSelectedListener());
        //设置默认值
        spinnerForLevel.setVisibility(View.VISIBLE);

        // 单个电机
        String tracksingle = "";
        int[] levelnum = new int[]{((MyApp)getApplication()).getMainGeneralLevel1TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel2TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel3TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel4TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel5TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel6TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel7TrackCount()};
        for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
            for(int j=0;j<levelnum[i];j++) {
                tracksingle = tracksingle + "," + (i + 1) + j;
            }
        }
        tracksingle = tracksingle.substring(1);
        onetrackarray = tracksingle.split(",");
        ArrayAdapter<String> onetrackadapter = new ArrayAdapter<String>(this,R.layout.myspinnertext,onetrackarray);
        //设置下拉列表的风格
        onetrackadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinnerOneTrack.setAdapter(onetrackadapter);
        //添加事件Spinner事件监听
        spinnerOneTrack.setOnItemSelectedListener(new OneTrackSpinnerSelectedListener());
        //设置默认值
        spinnerOneTrack.setVisibility(View.VISIBLE);

        errotrack = findViewById(R.id.errotrack);
        String errorinfo = getError();
        if(errorinfo.length()==0){
            errotrack.setText("故障轨道:无");
        } else {
            errotrack.setText("故障轨道:" + errorinfo);
        }

        mtype = android.os.Build.MODEL; // 手机型号
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     *
     * @return 有错误的轨道编号
     */
    protected String getError(){
        String errortrack = "";

        int[] levelnum = new int[]{((MyApp)getApplication()).getMainGeneralLevel1TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel2TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel3TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel4TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel5TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel6TrackCount(),
                ((MyApp)getApplication()).getMainGeneralLevel7TrackCount()};
        for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
            for(int j=0;j<levelnum[i];j++) {
                if(((MyApp)getApplication()).getTrackMainGeneral()[i*10+j].getErrorcode().length() > 0){
                    errortrack = errortrack + "," + (i*10+j+10);
                }
            }
        }


        if(errortrack.length() == 0){
            return "";
        } else {
            return errortrack.substring(1);   // 把第一个，去掉
        }
    }

    //使用数组形式操作
    class OneLevelSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            selectedlevel = onelevelarray[arg2];
            if(selectedlevel.equals("全部")) selectedlevel = "0";
            else selectedlevel = selectedlevel.replace("第","").replace("层","");

            Log.i(TAG,"售货单层电机：" + selectedlevel);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    //使用数组形式操作
    class forLevelSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            selectedforlevel = forarray[arg2];
            Log.i(TAG,"循环次数：" + selectedforlevel);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    //使用数组形式操作
    class OneTrackSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            selectedtrack = onetrackarray[arg2];
            Log.i(TAG,"售货单个电机：" + selectedtrack);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                isStopping = true;

                Intent toMenu = new Intent(TestActivity.this, MenuActivity.class);
                startActivity(toMenu);
                TestActivity.this.finish();
                break;

            case R.id.btn_clear:
                test_resultstr =  "";

                test_result.setText(test_resultstr);
                test_result.setSelection(test_result.getText().length(), test_result.getText().length());
                test_result.setMovementMethod(ScrollingMovementMethod.getInstance());
                break;

            case R.id.getstatuebtn:
                if(isComLifting){
                    Toast.makeText(TestActivity.this, "有测试正在处理中...",Toast.LENGTH_LONG).show();
                } else {
                    isComLifting = true;
                    test_resultstr = "";
                    test_result.setText(test_resultstr);
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);
                    testtype = "getstatus";
                    new Thread(new myThread()).start();
                }
                break;


            case R.id.onelevel_bt:
                if(isComLifting){
                    Toast.makeText(TestActivity.this,"有测试正在处理中...",Toast.LENGTH_SHORT).show();
                } else {
                    isComLifting = true;
                    test_resultstr =  "";
                    test_result.setText(test_resultstr);
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);
                    testtype = "onelevel";
                    new Thread(new myThread()).start();
                }
                break;

            case R.id.onetrack_bt:
                if(isComLifting){
                    Toast.makeText(TestActivity.this,"有测试正在处理中...",Toast.LENGTH_SHORT).show();
                } else {
                    isComLifting = true;
                    test_resultstr =  "";
                    test_result.setText(test_resultstr);
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);
                    testtype = "onetrack";

                    new Thread(new myThread()).start();
                }
                break;


            default:
                break;
        }
    }

    /**
     * Handle线程，接收线程过来的消息
     */
    Handler msgHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // 处理消息
            switch (msg.what) {

                case xian_shi_data:
                    test_result.setText(msg.obj.toString());
                    test_result.setSelection(test_result.getText().length(), test_result.getText().length());
                    test_result.setMovementMethod(ScrollingMovementMethod.getInstance());

                    String errorinfo = getError();
                    if(errorinfo.length()==0){
                        errotrack.setText("故障轨道:无");
                    } else {
                        errotrack.setText("故障轨道:" + errorinfo);
                    }

                    break;

                case xian_shi_error:
                    if(hinttvstr.length() > 0){
                        hinttv.setText("错误:"+hinttvstr.substring(1));
                    } else {
                        hinttv.setText("错误:无");
                    }
                    break;

                default:
                    break;

            }
            return false;
        }
    });


    class myThread implements Runnable{
        public void run(){
            String disp = "";
            try {
                if("getstatus".equalsIgnoreCase(testtype)){
                    ComLift comlift = null;
                    if (mtype.contains("310")) {
                        comlift = new ComLift("");
                    } else if (mtype.contains("300")) {
                        comlift = new ComLift("/dev/ttyO7");
                    } else {
                        comlift = new ComLift("/dev/ttyS4");
                    }
                    comlift.openSerialPort();

                    String[] rtn = comlift.getStatus();
                    comlift.closeSerialPort();

                    // 温度1,温度2,温度3,加热管1,加热管2,加热管3,加热管4,1号门开关,2号门开关,其它故障（有的话）,串口发送相关错误（有的话）
                    if(rtn[10].length()>0){
                        // 出错了
                        disp = rtn[10];
                    } else {
                        disp = "温度1："+rtn[0]+"  "+"温度2："+rtn[1]+"  "+"温度3："+rtn[2]+"  ";
                        disp += "加热管1："+(rtn[3].equals("1")?"加热中":"停止加热")+"  "+
                                "加热管2："+(rtn[4].equals("1")?"加热中":"停止加热")+"  "+
                                "加热管3："+(rtn[5].equals("1")?"加热中":"停止加热")+"  "+
                                "加热管4："+(rtn[6].equals("1")?"加热中":"停止加热")+"  ";
                        disp += "1号门开关："+(rtn[7].equals("1")?"开着":"关着")+"  "+
                                "2号门开关："+(rtn[8].equals("1")?"开着":"关着")+"  ";
                        disp += rtn[9]+"\n";
                    }
                    test_resultstr = test_resultstr + disp;

                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = test_resultstr;
                    msgHandler.sendMessage(msg);

                } else if("onetrack".equalsIgnoreCase(testtype)) {
                    ComLift comlift = null;
                    if (mtype.contains("310")) {
                        comlift = new ComLift("");
                    } else if (mtype.contains("300")) {
                        comlift = new ComLift("/dev/ttyO7");
                    } else {
                        comlift = new ComLift("/dev/ttyS4");
                    }
                    comlift.openSerialPort();

                    String rtn[] = comlift.vend_out_ind(Integer.parseInt(selectedtrack.substring(0, 1)), Integer.parseInt(selectedtrack.substring(1, 2)),
                            0,"","","");
                    comlift.closeSerialPort();

                    if (rtn[0].length() == 0) {
                        // 修改数据库
                        SQLiteDatabase db = openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                        db.execSQL("update trackmaingeneral set errorcode='',errortime='' where id=" + selectedtrack);
                        //修改Global的值
                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(selectedtrack) - 10].setErrorcode("");
                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(selectedtrack) - 10].setErrortime("");

                        db.close();
                    }

                    disp = "出货电机：" + selectedtrack + ",测试结果：" + (rtn[0].length() == 0 ? "成功" : rtn[0]) + "\n";

                    test_resultstr = test_resultstr + disp;

                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = test_resultstr;
                    msgHandler.sendMessage(msg);

                } else  if("onelevel".equalsIgnoreCase(testtype)){

                    // 每层的轨道数量
                    int[] levelnum = new int[]{((MyApp)getApplication()).getMainGeneralLevel1TrackCount(),
                            ((MyApp)getApplication()).getMainGeneralLevel2TrackCount(),
                            ((MyApp)getApplication()).getMainGeneralLevel3TrackCount(),
                            ((MyApp)getApplication()).getMainGeneralLevel4TrackCount(),
                            ((MyApp)getApplication()).getMainGeneralLevel5TrackCount(),
                            ((MyApp)getApplication()).getMainGeneralLevel6TrackCount(),
                            ((MyApp)getApplication()).getMainGeneralLevel7TrackCount()};


                    // 你选择的层
                    int selectedlevelint = Integer.parseInt(selectedlevel);

                    int[] leveldetail;
                    if(selectedlevelint==0){
                        // 那就是要全部的轨道啦
                        int testlevel = ((MyApp) getApplication()).getMainGeneralLevelNum();
                        leveldetail = new int[testlevel];
                        for(int i=1;i<=testlevel;i++){
                            leveldetail[i-1] = levelnum[i-1];
                        }
                    } else {
                        leveldetail = new int[1];
                        leveldetail[0] = levelnum[selectedlevelint-1];
                    }

                    ComLift comlift = null;
                    if (mtype.contains("310")) {
                        comlift = new ComLift("");
                    } else if (mtype.contains("300")) {
                        comlift = new ComLift("/dev/ttyO7");
                    } else {
                        comlift = new ComLift("/dev/ttyS4");
                    }
                    comlift.openSerialPort();

                    for(int k=0;k<Integer.parseInt(selectedforlevel);k++) {
                        boolean everymotorok = true;
                        for (int l = 0; l < leveldetail.length; l++) {
                            if(l==0){
                                test_resultstr = "";
                            }
                            for (int j = 0; j < leveldetail[l]; j++) {
                                int trackno;
                                if (leveldetail.length == 1) {
                                    trackno = selectedlevelint * 10 + j;
                                } else {
                                    trackno = (l + 1) * 10 + j;
                                }
                                // 中途退出
                                if(isStopping){
                                    k = 99;
                                    l = 9;
                                    break;
                                }

                                String[] rtn = comlift.vend_out_ind(trackno/10, trackno%10,0,"","","");

                                if (rtn[0].length() == 0) {
                                    test_resultstr = test_resultstr + "循环：" + (k+1)+";出货电机：" + trackno + ",测试结果：正常！\n";

                                    if(((MyApp) getApplication()).getTrackMainGeneral()[trackno - 10].getErrorcode().length() > 0) {
                                        // 修改数据库
                                        SQLiteDatabase db = openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                                        db.execSQL("update trackmaingeneral set errorcode='',errortime='' where id=" + selectedtrack);
                                        //修改Global的值
                                        ((MyApp) getApplication()).getTrackMainGeneral()[trackno - 10].setErrorcode("");
                                        ((MyApp) getApplication()).getTrackMainGeneral()[trackno - 10].setErrortime("");

                                        db.close();
                                    }

                                } else {
                                    test_resultstr = test_resultstr +  "循环：" + (k+1)+";出货电机：" + trackno + ",测试结果：" + rtn[0] + "\n";
                                    everymotorok = false;
                                }

                                Message msg = new Message();
                                msg.what = xian_shi_data;
                                msg.obj = test_resultstr;
                                msgHandler.sendMessage(msg);

                                SystemClock.sleep(5000); // 延时 5秒后继续下一个轨道

                            }
                        }
                        if (!everymotorok) break;
                    }
                    comlift.closeSerialPort();

                    if(!isStopping) {
                        Message msg = new Message();
                        msg.what = xian_shi_data;
                        msg.obj = test_resultstr + "测试结束！";
                        msgHandler.sendMessage(msg);
                    }
                }

            } catch (SQLException e) {
                Log.e(TAG, " 执行SQL命令" + e.toString());
                Message msg = new Message();
                msg.what = xian_shi_data;
                msg.obj = e.toString();
                msgHandler.sendMessage(msg);
            } catch (Exception ex) {
                Log.e(TAG, " 异常命令" + ex.toString());
                Message msg = new Message();
                msg.what = xian_shi_data;
                msg.obj = ex.toString();
                msgHandler.sendMessage(msg);
            }

            isComLifting = false; // 表示处理已完成了
        }
    }

}