package com.mnevent.hz.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
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


import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import android_serialport_api.ComTrack;


public class TestActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "TestActivity";
    private ThreadGroup tg = new ThreadGroup("TestActivityThreadGroup");
/*    private SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    private static final int drink_out_ge = 5000;
    private static final int general_out_ge = 20000;*/

    protected static final int xian_shi_data = 0x418;
    protected static final int xian_shi_error = 0x419;

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

    private int level_hui = 0;
    private int level_track = 0;
    private String hinttvstr = "";
    private TextView hinttv = null;

    private boolean isOuting = false;

    int selectedlevelint = 0;

    // 强制停止
    private boolean exit = false;
    // 测试类别
    private String testtype = "";

    private String tracknow = "";
    List<PathwayMolder> pathwaymolder;
    List<PathwayMolder> molder = new ArrayList<>();
    List<PathwayMolder> tiermolder = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        Button cleartbn = findViewById(R.id.btn_clear);
        cleartbn.setOnClickListener(this);

        Button onelevel_bt = findViewById(R.id.onelevel_bt);
        onelevel_bt.setOnClickListener(this);

        Button onetrack_bt = findViewById(R.id.onetrack_bt);
        onetrack_bt.setOnClickListener(this);



        (findViewById(R.id.setvmcdate)).setVisibility(View.GONE);
        (findViewById(R.id.getvmcdate)).setVisibility(View.GONE);
        (findViewById(R.id.getvmcstock)).setVisibility(View.GONE);
        (findViewById(R.id.getvmccash)).setVisibility(View.GONE);
        (findViewById(R.id.getvmcgoodscode)).setVisibility(View.GONE);
        (findViewById(R.id.getstatusbtn)).setOnClickListener(this);


        spinnerOneLevel = (Spinner) findViewById(R.id.SpinnerOneLevel);
        spinnerForLevel = (Spinner) findViewById(R.id.SpinnerFor);
        spinnerOneTrack = (Spinner) findViewById(R.id.SpinnerOneTrack);

        test_result = (EditText)findViewById(R.id.test_result_text);
        hinttv = findViewById(R.id.hintinfo);

        // 单层
        String trackfloor = "全部";
       // String trackfloor = "all";

        String ax_ = "";

        for(int i=0;i<8;i++){
            trackfloor = trackfloor + "," + ax_ + "第" + (i+1) + "层";
            //trackfloor = trackfloor + "," + ax_ + "First left" + (i+1) + "tier";
        }

      /*  if(((MyApp)getApplication()).getSubMacID().length() > 0) {
            ax_ = "副柜";
            trackfloor = trackfloor + "," + ax_ + "全部";
            for(int i=0;i<((MyApp)getApplication()).getSubGeneralLevelNum();i++){
                trackfloor = trackfloor + "," + ax_ + "第" + (i+1) + "层";
            }
        }*/
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
        ax_ = "";
     /*   if(((MyApp)getApplication()).getSubMacID().length() > 0) {
            ax_ = "主柜";
        }
        if(((MyApp)getApplication()).getMainMacType().equals("drink")){
            for(int i=0;i<((MyApp)getApplication()).getMainDrinkTrackNum();i++){
                tracksingle = tracksingle + "," + ax_ + (i+1);
            }
            tracksingle = tracksingle.substring(1);
        } else {*/
        molder.clear();
        pathwaymolder = LitePal.findAll(PathwayMolder.class);
        for (int i = 0;i<pathwaymolder.size();i++){
            if(pathwaymolder.get(i).getPds().equals("1")){
                molder.add(pathwaymolder.get(i));
            }
        }
        onetrackarray = new String[molder.size()];
        for(int j=0;j<molder.size();j++) {
            onetrackarray[j] = molder.get(j).getCode();
        }

        // tracksingle = tracksingle.substring(1);SpinnerOneLevel
        //}
       /* if(((MyApp)getApplication()).getSubMacID().length() > 0) {
            ax_ = "副柜";
            int[] levelnum = new int[]{((MyApp)getApplication()).getSubGeneralLevel1TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel2TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel3TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel4TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel5TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel6TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel7TrackCount()};
            for(int i=0;i<((MyApp)getApplication()).getSubGeneralLevelNum();i++){
                for(int j=0;j<levelnum[i];j++) {
                    tracksingle = tracksingle + "," + ax_ + (i + 1) + j;
                }
            }
        }*/
        // onetrackarray = tracksingle.split(",");

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
           // errotrack.setText("故障轨道:无");
            errotrack.setText("Fault track: none");
        } else {
            //errotrack.setText("故障轨道:" + errorinfo);
            errotrack.setText("Fault track:" + errorinfo);
        }


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

        String ax_ = "";


        for (int j = 0; j < molder.size(); j++) {
            if (molder.get(j).getErrorcode().equals("0")) {
                errortrack = errortrack + "," + molder.get(j).getCode();
            }
        }


        /*if(((MyApp)getApplication()).getSubMacID().length() > 0) {
            ax_ = "副柜";
            int[] levelnum = new int[]{((MyApp)getApplication()).getSubGeneralLevel1TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel2TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel3TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel4TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel5TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel6TrackCount(),
                    ((MyApp)getApplication()).getSubGeneralLevel7TrackCount()};
            for(int i=0;i<((MyApp)getApplication()).getSubGeneralLevelNum();i++){
                for(int j=0;j<levelnum[i];j++) {
                    if(((MyApp)getApplication()).getTrackSubGeneral()[i*10+j].getErrorcode().length() > 0){
                        errortrack = errortrack + "," + ax_ + (i*10+j+10);
                    }
                }
            }
        }*/

        if(errortrack.length() == 0){
            return "";
        } else {
            return errortrack.substring(1);   // 把最后一个，去掉
        }
    }


    //使用数组形式操作
    class OneLevelSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            selectedlevel = onelevelarray[arg2];
            selectedlevelint = arg2;
            Log.i("zlc","售货单层电机：" + selectedlevel);
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
               /* if(lp != null) {
                    // 关闭presenter的所有线程，然后跳转
                    lp.setStopThread();
                    while (tg.activeCount() > 0) for (int i = 0; i < 1000; i++) ;
                    tg.destroy();
                }*/

                exit = true;

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

            //  直接连驱动板的！
            case R.id.getstatusbtn:
                if(isOuting){
                   // Toast.makeText(TestActivity.this, "有测试正在处理中...",Toast.LENGTH_LONG).show();
                    Toast.makeText(TestActivity.this, "There are tests in progress...",Toast.LENGTH_LONG).show();
                } else {
                    isOuting = true;

                    test_resultstr = "";
                    test_result.setText(test_resultstr);
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);
                    testtype = "getstatus";
                    new Thread(new myThread()).start();
                }
                break;

            case R.id.onelevel_bt:
                if(isOuting){
                   // Toast.makeText(TestActivity.this,"已经在出货中...",Toast.LENGTH_SHORT).show();
                    Toast.makeText(TestActivity.this,"It is already in shipment...",Toast.LENGTH_SHORT).show();
                } else {
                    isOuting = true;

                    test_resultstr =  "";
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);

                        testtype = "onelevel";
                        new Thread(new myThread()).start();

                }
                break;

            case R.id.onetrack_bt:
                if(isOuting){
                  //  Toast.makeText(TestActivity.this,"已经在出货中...",Toast.LENGTH_SHORT).show();
                    Toast.makeText(TestActivity.this,"It is already in shipment...",Toast.LENGTH_SHORT).show();
                } else {
                    isOuting = true;

                    level_hui = 0;
                    level_track = 0;

                    test_resultstr =  "";
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

    class myThread implements Runnable{
        public void run(){
            String disp = "";
            try {
                if("getstatus".equalsIgnoreCase(testtype)){
                    ComTrack comTrack = new ComTrack("");
                    comTrack.openSerialPort();

                    String[] rtn = comTrack.getStatus();
                    comTrack.closeSerialPort();

                    // 温度,门开关,其它故障（有的话）,串口发送相关错误（有的话）
                    if(rtn[rtn.length - 1].length()>0){
                        // 出错了
                        disp += rtn[rtn.length - 1]+"\n";
                    } else {
                        //disp = "库室温度："+rtn[0]+";化霜温度："+rtn[1]+";制冷开始温度："+rtn[2]+";制冷停止温度："+rtn[3]+";门状态："+rtn[4]+" ";
                        disp = "Library room temperature："+rtn[0]+";Frost temperature："+rtn[1]+";Initial temperature of refrigeration："+rtn[2]+";Freezing temperature："+rtn[3]+";State of the door："+rtn[4]+" ";
                       // disp += (rtn[5].length()>0?"故障:"+rtn[5]:"")+"\n";
                        disp += (rtn[5].length()>0?"The fault:"+rtn[5]:"")+"\n";
                    }
                    test_resultstr = test_resultstr + disp;

                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = test_resultstr;
                    msgHandler.sendMessage(msg);

                } else if("onetrack".equalsIgnoreCase(testtype)) {
                    ComTrack comTrack = new ComTrack("");
                    comTrack.openSerialPort();

                    String rtn[] = comTrack.vend_out_ind(
                            Integer.parseInt(selectedtrack.substring(0, 1)), Integer.parseInt(selectedtrack.substring(1, 2)),
                            0,"","","");
                    comTrack.closeSerialPort();

                    if (rtn[0].length() != 0) {
                        // 修改数据库
                       /* SQLiteDatabase db = openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                        db.execSQL("update trackmaingeneral set errorcode='',errortime='' where id=" + selectedtrack);
                        //修改Global的值
                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(selectedtrack) - 10].setErrorcode("");
                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(selectedtrack) - 10].setErrortime("");

                        db.close();*/
                        PathwayMolder molder = new PathwayMolder();
                        molder.setErrorcode("0");
                        molder.updateAll("code = ?",selectedtrack);

                    }

                   // disp = "出货电机：" + selectedtrack + ",测试结果：" + (rtn[0].length() == 0 ? "成功" : rtn[0]) + "\n";
                    disp = "Shipment motor：" + selectedtrack + ",The test results：" + (rtn[0].length() == 0 ? "successful" : rtn[0]) + "\n";
                 //   Log.d("zlc","test_resultstr:"+test_resultstr+"disp:"+disp);
                    test_resultstr = test_resultstr + disp;

                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = test_resultstr;
                    msgHandler.sendMessage(msg);

                } else  if("onelevel".equalsIgnoreCase(testtype)){
                  //  Log.d("zlc","zhixinle");


                    int[] levelnum = new int[]{0,0,0,0,0,0,0,0,0};
                    for (int i = 0;i<molder.size()/10;i++){
                        for (int j = 0;j<10;j++){

                            levelnum[i] += levelnum[i];

                        }
                    }
                    // 每层的轨道数量



                    // 你选择的层
                    /*selectedlevelint = 0;
                    if(!selectedlevel.contains("全部")){
                        selectedlevelint = Integer.parseInt(selectedlevel.replace("副柜","").replace("第","").replace("层",""));
                    }*/
                 //   Log.d("zlc","selectedlevelint"+selectedlevelint);
                    int[] leveldetail;
                    if(selectedlevelint==0){
                        // 那就是要全部的轨道啦
                        /*int testlevel = 8;
                        leveldetail = new int[testlevel];
                        for(int i=1;i<=testlevel;i++){
                            leveldetail[i-1] = levelnum[i-1];
                        }*/

                        ComTrack comTrack = new ComTrack("");
                        comTrack.openSerialPort();
                        for(int j = 0;j<Integer.parseInt(selectedforlevel);j++) {

                            for (int i = 0; i <molder.size(); i++) {
                                if (exit) {

                                    break;
                                }
                               Log.d("zlc","Code::::::::::::::::::"+molder.get(i).getCode());
                                String[] rtn = comTrack.vend_out_ind(Integer.parseInt(molder.get(i).getCode().substring(0,1)), Integer.parseInt(molder.get(i).getCode().substring(1, 2)), 0, "", "", "");

                                if (rtn[0].length() == 0) {
                                   // test_resultstr = test_resultstr + "循环：" + (j + 1) + ";出货电机：" + molder.get(i).getCode() + ",测试结果：正常！\n";
                                    test_resultstr = test_resultstr + "cycle：" + (j + 1) + ";Shipment motor：" + molder.get(i).getCode() + ",Test results: normal！\n";

                                    if ((molder.get(j).getErrorcode().equals("0"))) {

                                        PathwayMolder molders = new PathwayMolder();
                                        molders.setErrorcode("1");
                                        molders.updateAll("code = ?", molder.get(i).getCode());
                                    }

                                } else {
                                   // test_resultstr = test_resultstr + "循环：" + (j + 1) + ";出货电机：" + molder.get(i).getCode() + ",测试结果：" + rtn[0] + "\n";
                                    test_resultstr = test_resultstr + "cycle：" + (j + 1) + ";Shipment motor：" + molder.get(i).getCode() + ",Test results：" + rtn[0] + "\n";

                                    //everymotorok = false;
                                    Log.d("zlc","test_resultstrq:"+test_resultstr+"disp:"+disp);
                                }
                                Message msg = new Message();
                                msg.what = xian_shi_data;
                                msg.obj = test_resultstr;
                                msgHandler.sendMessage(msg);

                                SystemClock.sleep(3000); // 延时 5秒后继续下一个轨道

                            }
                        }
                        comTrack.closeSerialPort();
                    } else {
                       /* leveldetail = new int[1];
                        leveldetail[0] = levelnum[selectedlevelint-1];*/
                        tiermolder.clear();

                        ComTrack comTrack = new ComTrack("");
                        comTrack.openSerialPort();
                        for(int j = 0;j<Integer.parseInt(selectedforlevel);j++) {

                            for (int a = 0;a<molder.size();a++){
                                if(molder.get(a).getCode().startsWith(selectedlevelint+"")){
                                   tiermolder.add(molder.get(a));
                                }
                            }

                            for (int i = 0; i < tiermolder.size(); i++) {
                                //int c = selectedlevelint * 10 + i;
                                if(tiermolder.get(i).getMergecode().equals("01")){
                                    if (exit) {

                                        break;
                                    }

                                    String[] rtn = comTrack.vend_out_ind(Integer.parseInt(tiermolder.get(i).getCode().substring(0, 1)), Integer.parseInt(tiermolder.get(i).getCode().substring( 1, 2)), 0, "", "", "");

                                    if (rtn[0].length() == 0) {
                                        //test_resultstr = test_resultstr + "循环：" + (j + 1) + ";出货电机：" + tiermolder.get(i).getCode() + ",测试结果：正常！\n";
                                        test_resultstr = test_resultstr + "cycle：" + (j + 1) + ";Shipment motor：" + tiermolder.get(i).getCode() + ",Test results: normal！\n";
                                        Log.d("zlc","test_resultstrc:"+test_resultstr+"disp:"+disp);
                                        if ((tiermolder.get(i).getErrorcode().equals("0"))) {

                                            PathwayMolder molders = new PathwayMolder();
                                            molders.setErrorcode("1");
                                            molders.updateAll("code = ?", tiermolder.get(i).getCode());
                                        }

                                    } else {
                                        test_resultstr = test_resultstr + "cycle：" + (j + 1) + ";Shipment motor：" + tiermolder.get(i).getCode() + ",Test results：" + rtn[0] + "\n";
                                        //everymotorok = false;
                                    }
                                    Message msg = new Message();
                                    msg.what = xian_shi_data;
                                    msg.obj = test_resultstr;
                                    msgHandler.sendMessage(msg);

                                    SystemClock.sleep(5000); // 延时 5秒后继续下一个轨道

                                }
                            }
                                }

                        comTrack.closeSerialPort();

                    }

                   /* ComTrack comTrack = new ComTrack("");
                    comTrack.openSerialPort();
                    Log.d("zlc","selectedforlevel"+selectedforlevel);
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
                                Log.d("zlc","trackno"+trackno+":"+k+":"+l);
                                if(exit){
                                    k = 99;
                                    l = 9;
                                    break;
                                }

                                String[] rtn = comTrack.vend_out_ind(trackno/10, trackno%10,0,"","","");

                                if (rtn[0].length() == 0) {
                                    test_resultstr = test_resultstr + "循环：" + (k+1)+";出货电机：" + trackno + ",测试结果：正常！\n";

                                    if((pathwaymolder.get(trackno - 10).getErrorcode().equals("0"))){

                                        PathwayMolder molder = new PathwayMolder();
                                        molder.setErrorcode("1");
                                        molder.updateAll("code",selectedtrack);
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
                    comTrack.closeSerialPort();
*/
                    if(!exit) {
                        Message msg = new Message();
                        msg.what = xian_shi_data;
                       // msg.obj = test_resultstr + "测试结束！";
                        msg.obj = test_resultstr + "End of the test！";
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

            isOuting = false; // 表示处理已完成了
        }
    }

    /*class myLevelThread implements Runnable {
        public void run() {
            isOuting = true;
            for(int k=0;k<Integer.parseInt(selectedforlevel);k++){
                //selectedlevel
                // 全部，第1层，第2层...
                // 主柜全部，主柜第1层，主柜第2层...
                // 副柜全部，副柜第1层，副柜第2层...
                if(selectedlevel.contains("副柜")){
                    int[] levelnum = new int[]{((MyApp)getApplication()).getSubGeneralLevel1TrackCount(),
                            ((MyApp)getApplication()).getSubGeneralLevel2TrackCount(),
                            ((MyApp)getApplication()).getSubGeneralLevel3TrackCount(),
                            ((MyApp)getApplication()).getSubGeneralLevel4TrackCount(),
                            ((MyApp)getApplication()).getSubGeneralLevel5TrackCount(),
                            ((MyApp)getApplication()).getSubGeneralLevel6TrackCount(),
                            ((MyApp)getApplication()).getSubGeneralLevel7TrackCount()};

                    if(selectedlevel.contains("全部")){
                        for(int i=0;i<((MyApp)getApplication()).getSubGeneralLevelNum();i++){
                            for(int j=0;j<levelnum[i];j++) {
                                if(exit) return;

                                test_resultstr = test_resultstr + "第"+(k+1)+"回：出货货道：副柜" + (i*10+j+10) + "\n";
                                tracknow = "" + (i*10+j+10);

                                level_hui = k+1;
                                level_track = i*10+j+10;

                                lp.outTrack(false,false,(i*10+j+10),64,1);

                                int c = 0;
                                while (tracknow.length() > 0 && c < general_out_ge) {
                                    try{ Thread.sleep(1000); } catch (Exception e){}
                                    c = c + 1000;
                                }

                                try{ Thread.sleep(2000); } catch (Exception e){}
                            }
                        }
                    } else {
                        int one = Integer.parseInt(selectedlevel.replace("副柜","").replace("第","").replace("层",""));
                        for(int j=0;j<levelnum[one-1];j++) {
                            if(exit) return;

                            test_resultstr = test_resultstr + "第"+(k+1)+"回：出货货道：副柜" + (one*10+j) + "\n";
                            tracknow = "" + (one*10+j);

                            level_hui = k+1;
                            level_track = one*10+j;

                            lp.outTrack(false,false,(one*10+j),64,1);

                            int c = 0;
                            while (tracknow.length() > 0 && c < general_out_ge) {
                                try{ Thread.sleep(1000); } catch (Exception e){}
                                c = c + 1000;
                            }

                            try{ Thread.sleep(2000); } catch (Exception e){}
                        }
                    }
                } else {
                    //selectedlevel = selectedlevel.replace("主柜","");
                    if(((MyApp)getApplication()).getMainMacType().equals("drink")){
                        if(selectedlevel.contains("全部")){
                            for(int i=0;i<((MyApp)getApplication()).getMainDrinkTrackNum();i++ ){
                                if(exit) return;

                                level_hui = k+1;
                                level_track = i+1;

                                test_resultstr = test_resultstr + "第"+(k+1)+"回：出货货道：" + (i+1) + "\n";
                                lp.outTrack(true,true,i+1,64,1);
                                try{ Thread.sleep(drink_out_ge); } catch (Exception e){}
                            }
                        }
                    } else {
                        int[] levelnum = new int[]{((MyApp)getApplication()).getMainGeneralLevel1TrackCount(),
                                ((MyApp)getApplication()).getMainGeneralLevel2TrackCount(),
                                ((MyApp)getApplication()).getMainGeneralLevel3TrackCount(),
                                ((MyApp)getApplication()).getMainGeneralLevel4TrackCount(),
                                ((MyApp)getApplication()).getMainGeneralLevel5TrackCount(),
                                ((MyApp)getApplication()).getMainGeneralLevel6TrackCount(),
                                ((MyApp)getApplication()).getMainGeneralLevel7TrackCount()};
                        if(selectedlevel.contains("全部")){
                            // 综合机
                            for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
                                for(int j=0;j<levelnum[i];j++) {
                                    if(exit) return;

                                    level_hui = k+1;
                                    level_track = i*10+j+10;

                                    test_resultstr = test_resultstr + "第"+(k+1)+"回：出货货道：" + (i*10+j+10) + "\n";
                                    tracknow = "" + (i*10+j+10);
                                    lp.outTrack(true,false,(i*10+j+10),64,1);

                                    int c = 0;
                                    while (tracknow.length() > 0 && c < general_out_ge) {
                                        try{ Thread.sleep(1000); } catch (Exception e){}
                                        c = c + 1000;
                                    }

                                    try{ Thread.sleep(2000); } catch (Exception e){}
                                }
                            }
                        } else {
                            int one = Integer.parseInt(selectedlevel.replace("主柜","").replace("第","").replace("层",""));
                            for(int j=0;j<levelnum[one-1];j++) {
                                if(exit) return;

                                level_hui = k+1;
                                level_track = one*10+j;

                                test_resultstr = test_resultstr + "第"+(k+1)+"回：出货货道：" + (one*10+j) + "\n";
                                tracknow = "" + (one*10+j);

                                lp.outTrack(true,false,(one*10+j),64,1);

                                int c = 0;
                                while (tracknow.length() > 0 && c < general_out_ge) {
                                    try{ Thread.sleep(1000); } catch (Exception e){}
                                    c = c + 1000;
                                }

                                try{ Thread.sleep(2000); } catch (Exception e){}
                            }
                        }
                    }
                }
            }
            isOuting = false;
        }
    }

    class mySingleThread implements Runnable {
        public void run() {
            isOuting = true;
            // selectedtrack
            if(selectedtrack.contains("副柜")){
                test_resultstr =  test_resultstr + "出货货道：" + selectedtrack + "\n";
                lp.outTrack(false,false,Integer.parseInt(selectedtrack.replace("副柜","")),64,1);
            } else {
                if(((MyApp)getApplication()).getMainMacType().equals("drink")) {
                    test_resultstr = test_resultstr + "出货货道：" + selectedtrack + "\n";
                    lp.outTrack(true, true, Integer.parseInt(selectedtrack.replace("主柜", "")), 64, 1);
                } else {
                    test_resultstr = test_resultstr + "出货货道：" + selectedtrack + "\n";
                    lp.outTrack(true, false, Integer.parseInt(selectedtrack.replace("主柜", "")), 64, 1);
                }
            }
            isOuting = false;
        }
    }*/

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
                      //  errotrack.setText(""+ "ComVMC.activeThreadCount"+"_"+ComTrack.activeThreadCount+"_故障轨道:无");
                        errotrack.setText(""+ "ComVMC.activeThreadCount"+"_"+ ComTrack.activeThreadCount+"_Fault track: none");
                    } else {
                        errotrack.setText(""+ "ComVMC.activeThreadCount"+"_"+ ComTrack.activeThreadCount+"_Fault track:" + errorinfo);
                    }

                    break;

                case xian_shi_error:
                    if(hinttvstr.length() > 0){
                       // hinttv.setText(""+ "ComVMC.activeThreadCount"+"_"+ComTrack.activeThreadCount+"_错误:"+hinttvstr.substring(1));
                        hinttv.setText(""+ "ComVMC.activeThreadCount"+"_"+ ComTrack.activeThreadCount+"error:"+hinttvstr.substring(1));
                    } else {
                        hinttv.setText(""+ "ComVMC.activeThreadCount"+"_"+ ComTrack.activeThreadCount+"_error:none");
                    }
                    break;

                default:
                    break;

            }
            return false;
        }
    });


    // 直接连接驱动板
    public void rtnGetStatus(String[] status){

    }
    // 直接连接驱动板
    public void rtnVendOutInd(String[] outresult){

    }
    // 直接连接驱动板
    public void rtnFaceMoneyCheck(String rtnstr){

    }
}
