package com.mnevent.hz.Activity;

import android.app.Activity;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mnevent.hz.R;
import com.mnevent.hz.Utils.BasicparameterPopuwindow;
import com.mnevent.hz.Utils.DateUtils;
import com.mnevent.hz.Utils.Log;
import com.mnevent.hz.Utils.LogUtils;
import com.mnevent.hz.Utils.ToastUtil;
import com.mnevent.hz.litemolder.PathwayMolder;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import android_serialport_api.ComTrackMagex;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestMagexActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "TestMagexActivity";

    protected static final int xian_shi_data = 0x418;
    protected static final int xian_shi_error = 0x419;
    @BindView(R.id.move)
    Button move;
    @BindView(R.id.btn_clearaway)
    Button btnClearaway;

    private Spinner spinnerOneLevel;
    private Spinner spinnerForLevel;
    private Spinner spinnerOneTrack;

    private String[] onelevelarray = null;
    private String selectedlevel = null;

    private String[] forarray = new String[]{"1", "2", "3", "4", "5", "10"};
    private String selectedforlevel = "1";

    private String[] onetrackarray = null;
    private String selectedtrack = null;

    private String test_resultstr = "";
    private EditText test_result = null;

    private TextView errotrack = null;

    private String hinttvstr = "";
    private TextView hinttv = null;

    private boolean isOuting = false;

    // 强制停止
    private boolean exit = false;
    // 测试类别
    private String testtype = "";

    private String mtype;
    int selectedlevelint = 0;

    private String[] textes = new String[]{"第一层", "第二层", "第三层", "第四层", "第五层", "第六层", "第七层", "第八层"};

    List<PathwayMolder> pwmolder = new ArrayList<>();

    List<PathwayMolder> pwmolderes = new ArrayList<>();

    String classname = "";
    //private String[] textes = new String[]{"ground floor","second floor","The third layer","the fourth floor" ,"The fifth floor","The sixth layer","The seventh floor","The eighth floor"};
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case 1:
                    test_result.setText("正在移动轨道...");
                    // test_result.setText("Moving orbit...");
                    break;

                case 2:
                    String moverobot = (String) msg.obj;
                    LogUtils.d("TestMagexActivity:::" + moverobot);
                    /*if(moverobot){

                    }*/
                    test_result.setText("正在移动轨道..." + "\n" + moverobot);
                    //test_result.setText("Moving orbit..."+"\n"+moverobot);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_magex);
        ButterKnife.bind(this);
        classname  = getClass().getSimpleName();
        LogUtils.d("TestMagexActivity：" + getClass().getSimpleName());
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏

        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        Button cleartbn = findViewById(R.id.btn_clear);
        cleartbn.setOnClickListener(this);

        Button getstatusbn = findViewById(R.id.btn_getstatus);
        getstatusbn.setOnClickListener(this);

        Button gettempbn = findViewById(R.id.btn_gettemp);
        gettempbn.setOnClickListener(this);

        Button onelevel_bt = findViewById(R.id.onelevel_bt);
        onelevel_bt.setOnClickListener(this);

        Button onetrack_bt = findViewById(R.id.onetrack_bt);
        onetrack_bt.setOnClickListener(this);

        Button onetrackrest_bt = findViewById(R.id.onetrackreset_bt);
        onetrackrest_bt.setOnClickListener(this);

        Button onetrackallreset_bt = findViewById(R.id.onetrackallreset_bt);
        onetrackallreset_bt.setOnClickListener(this);

        btnClearaway.setOnClickListener(this);

        spinnerOneLevel = (Spinner) findViewById(R.id.SpinnerOneLevel);
        spinnerForLevel = (Spinner) findViewById(R.id.SpinnerFor);
        spinnerOneTrack = (Spinner) findViewById(R.id.SpinnerOneTrack);

        test_result = (EditText) findViewById(R.id.test_result_text);
        hinttv = findViewById(R.id.hintinfo);

        // 单层
        String trackfloor = "全部,第1层,第2层,第3层,第4层,第5层,第6层,第7层";
        //String trackfloor = "all,ground floor,second floor,The third layer,the fourth floor ,The fifth floor,The sixth layer,The seventh floor,The eighth floor";
        onelevelarray = trackfloor.split(",");


        ArrayAdapter<String> oneleveladapter = new ArrayAdapter<String>(this, R.layout.myspinnertext, onelevelarray);
        //设置下拉列表的风格
        oneleveladapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinnerOneLevel.setAdapter(oneleveladapter);
        //添加事件Spinner事件监听
        spinnerOneLevel.setOnItemSelectedListener(new OneLevelSpinnerSelectedListener());
        //设置默认值
        spinnerOneLevel.setVisibility(View.VISIBLE);

        ArrayAdapter<String> foradapter = new ArrayAdapter<String>(this, R.layout.myspinnertext, forarray);
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
        List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getMergecode().equals("01")) {
                tracksingle += all.get(i).getCode() + ",";
            }
        }
        LogUtils.d("TestMagexActivity:::tracksingle:" + tracksingle);
        onetrackarray = tracksingle.split(",");

        ArrayAdapter<String> onetrackadapter = new ArrayAdapter<String>(this, R.layout.myspinnertext, onetrackarray);
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
        if (errorinfo.length() == 0) {
            // errotrack.setText("Fault track: none");
            errotrack.setText("故障轨道:无");
        } else {
            errotrack.setText("故障轨道:" + errorinfo);
            //errotrack.setText("Fault track:" + errorinfo);
        }

        mtype = Build.MODEL; // 手机型号
    }


    @OnClick(R.id.move)
    public void onViewClicked() {
        final BasicparameterPopuwindow popuwindow = new BasicparameterPopuwindow(TestMagexActivity.this, textes, 0);
        popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_test_magex, null), Gravity.CENTER, 0, 0);
        popuwindow.SetOnclike(new BasicparameterPopuwindow.Onckile() {
            @Override
            public void onclike(final int position) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ComTrackMagex magex = new ComTrackMagex("");

                        magex.openSerialPort();

                        handler.sendEmptyMessage(1);
                        String moverobot = magex.moverobot(position + 1);
                        magex.closeSerialPort();
                        LogUtils.d("TestMagexActivity:moverobot:" + moverobot);
                        Message message = new Message();
                        message.what = 2;
                        message.obj = moverobot;
                        handler.sendMessage(message);
                    }
                }).start();

                popuwindow.dismiss();
            }
        });
    }


    /**
     * @return 有错误的轨道编号
     */
    protected String getError() {
        String errortrack = "";

//        int[] levelnum = new int[]{((MyApp)getApplication()).getMainGeneralLevel1TrackCount(),
//                ((MyApp)getApplication()).getMainGeneralLevel2TrackCount(),
//                ((MyApp)getApplication()).getMainGeneralLevel3TrackCount(),
//                ((MyApp)getApplication()).getMainGeneralLevel4TrackCount(),
//                ((MyApp)getApplication()).getMainGeneralLevel5TrackCount(),
//                ((MyApp)getApplication()).getMainGeneralLevel6TrackCount(),
//                ((MyApp)getApplication()).getMainGeneralLevel7TrackCount()};
//        for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
//            for(int j=0;j<levelnum[i];j++) {
//                if(((MyApp)getApplication()).getTrackMainGeneral()[i*10+j].getErrorcode().length() > 0){
//                    errortrack = errortrack + "," + (i*10+j+10);
//                }
//            }
//        }


        if (errortrack.length() == 0) {
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
            selectedlevelint = arg2;
            LogUtils.i("TestMagexActivity::::售货单层电机：" + selectedlevel);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    //使用数组形式操作
    class forLevelSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            selectedforlevel = forarray[arg2];
            LogUtils.i(TAG + ":::循环次数：" + selectedforlevel);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    //使用数组形式操作
    class OneTrackSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            selectedtrack = onetrackarray[arg2];
            LogUtils.i(TAG + ":::售货单个电机：" + selectedtrack);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_clearaway:
                pwmolder.clear();
                pwmolder = LitePal.findAll(PathwayMolder.class);
                for (int i = 0;i<pwmolder.size();i++){
                    PathwayMolder molder = new PathwayMolder();
                    molder.setErrorcode("1");
                    molder.setErrortime("");
                    molder.updateAll("code = ?",pwmolder.get(i).getCode());
                }
                ToastUtil.showToast(TestMagexActivity.this,"清除成功！");
                break;
            case R.id.btn_back:

                exit = true;

               /* Intent toLoad = new Intent(TestMagexActivity.this, LoadingActivity.class);
                startActivity(toLoad);*/
                finish();
                break;

            case R.id.btn_clear:
                test_resultstr = "";

                test_result.setText(test_resultstr);
                test_result.setSelection(test_result.getText().length(), test_result.getText().length());
                test_result.setMovementMethod(ScrollingMovementMethod.getInstance());
                break;

            case R.id.btn_getstatus:
                if (isOuting) {
                    Toast.makeText(TestMagexActivity.this, "已经在出货中...", Toast.LENGTH_SHORT).show();
                    // Toast.makeText(TestMagexActivity.this, "It is already in shipment...", Toast.LENGTH_SHORT).show();
                } else {
                    isOuting = true;

                    test_resultstr = "";
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);

                    testtype = "getstatus";
                    new Thread(new myThread()).start();

                }
                break;

            case R.id.btn_gettemp:
                if (isOuting) {
                    Toast.makeText(TestMagexActivity.this, "已经在出货中...", Toast.LENGTH_SHORT).show();
                    // Toast.makeText(TestMagexActivity.this, "It is already in shipment...", Toast.LENGTH_SHORT).show();
                } else {
                    isOuting = true;

                    test_resultstr = "";
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);

                    testtype = "gettemp";
                    new Thread(new myThread()).start();

                }
                break;


            case R.id.onelevel_bt:
                if (isOuting) {
                    Toast.makeText(TestMagexActivity.this, "已经在出货中...", Toast.LENGTH_SHORT).show();
                    //  Toast.makeText(TestMagexActivity.this, "It is already in shipment...", Toast.LENGTH_SHORT).show();
                } else {
                    isOuting = true;

                    test_resultstr = "";
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);

                    testtype = "onelevel";
                    new Thread(new myThread()).start();

                }
                break;

            case R.id.onetrack_bt:
                if (isOuting) {
                    Toast.makeText(TestMagexActivity.this, "已经在出货中...", Toast.LENGTH_SHORT).show();
                    // Toast.makeText(TestMagexActivity.this, "It is already in shipment...", Toast.LENGTH_SHORT).show();
                } else {
                    isOuting = true;

                    test_resultstr = "";
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);

                    testtype = "onetrack";
                    new Thread(new myThread()).start();

                }
                break;

            case R.id.onetrackreset_bt:
                if (isOuting) {
                    Toast.makeText(TestMagexActivity.this, "已经在出货中...", Toast.LENGTH_SHORT).show();
                    //  Toast.makeText(TestMagexActivity.this, "It is already in shipment...", Toast.LENGTH_SHORT).show();
                } else {
                    isOuting = true;

                    test_resultstr = "";
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);

                    testtype = "onetrackreset";
                    new Thread(new myThread()).start();

                }
                break;

            case R.id.onetrackallreset_bt:
                if (isOuting) {
                    Toast.makeText(TestMagexActivity.this, "已经在出货中...", Toast.LENGTH_SHORT).show();
                    // Toast.makeText(TestMagexActivity.this, "It is already in shipment...", Toast.LENGTH_SHORT).show();
                } else {
                    isOuting = true;

                    test_resultstr = "";
                    hinttvstr = "";
                    hinttv.setText(hinttvstr);

                    testtype = "onetrackallreset";
                    new Thread(new myThread()).start();

                }
                break;


            default:
                break;
        }
    }

    class myThread implements Runnable {
        public void run() {
            String disp = "";
            try {


                if ("gettemp".equalsIgnoreCase(testtype)) {
                    test_resultstr = "正在获取温度，请稍候..." + "\n";
                    // test_resultstr = "Please wait while obtaining the temperature..." + "\n";
                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = test_resultstr;
                    msgHandler.sendMessage(msg);

                    ComTrackMagex comTrack = new ComTrackMagex("");
                    comTrack.openSerialPort();


                    String rtn = comTrack.gettemp();
                    comTrack.closeSerialPort();

                    test_resultstr = test_resultstr + (rtn.length() == 0 ? "没有返回" : rtn) + "\n";
                    // test_resultstr = test_resultstr + (rtn.length() == 0 ? "There is no return" : rtn) + "\n";

                    Message msg2 = new Message();
                    msg2.what = xian_shi_data;
                    msg2.obj = test_resultstr;
                    msgHandler.sendMessage(msg2);

                } else if ("getstatus".equalsIgnoreCase(testtype)) {
                    test_resultstr = "正在获取状态，请稍候..." + "\n";
                    // test_resultstr = "Getting status, please wait..." + "\n";
                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = test_resultstr;
                    msgHandler.sendMessage(msg);

                    ComTrackMagex comTrack = new ComTrackMagex("");
                    comTrack.openSerialPort();

                    String rtn = comTrack.getStatus();
                    comTrack.closeSerialPort();

                    test_resultstr = test_resultstr + (rtn.length() == 0 ? "没有返回" : rtn) + "\n";
                    //test_resultstr = test_resultstr + (rtn.length() == 0 ? "There is no return" : rtn) + "\n";


                    Message msg2 = new Message();
                    msg2.what = xian_shi_data;
                    msg2.obj = test_resultstr;
                    msgHandler.sendMessage(msg2);

                } else if ("onetrackreset".equalsIgnoreCase(testtype)) {
                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = "正在复位：" + selectedtrack + "，请稍候..." + "\n";
                    //  msg.obj = "Is reset：" + selectedtrack + "，please wait..." + "\n";
                    msgHandler.sendMessage(msg);

                    ComTrackMagex comTrack = new ComTrackMagex("");
                    comTrack.openSerialPort();

                    String rtn = comTrack.motorrest(
                            Integer.parseInt(selectedtrack.substring(0, 1)), Integer.parseInt(selectedtrack.substring(1, 2)));

                    comTrack.closeSerialPort();

                    disp = "单货道复位：" + selectedtrack + ",结果：" + rtn + "\n";
                    // disp = "Single channel reset：" + selectedtrack + ",result：" + rtn + "\n";

                    test_resultstr = test_resultstr + disp;

                    Message msg2 = new Message();
                    msg2.what = xian_shi_data;
                    msg2.obj = test_resultstr;
                    msgHandler.sendMessage(msg2);

                } else if ("onetrackallreset".equalsIgnoreCase(testtype)) {
                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = "正在复位：" + selectedtrack + "，请稍候..." + "\n";
                    // msg.obj = "Is reset：" + selectedtrack + "，please wait..." + "\n";
                    msgHandler.sendMessage(msg);

                    ComTrackMagex comTrack = new ComTrackMagex("");
                    comTrack.openSerialPort();

                    String rtn = comTrack.motorrest(
                            Integer.parseInt(selectedtrack.substring(0, 1)), 15);

                    comTrack.closeSerialPort();

                    disp = "层货道复位：" + selectedtrack.substring(0, 1) + ",结果：" + rtn + "\n";
                    // disp = "Layer cargo channel reset：" + selectedtrack.substring(0, 1) + ",result：" + rtn + "\n";

                    test_resultstr = test_resultstr + disp;

                    Message msg2 = new Message();
                    msg2.what = xian_shi_data;
                    msg2.obj = test_resultstr;
                    msgHandler.sendMessage(msg2);

                } else if ("onetrack".equalsIgnoreCase(testtype)) {
                    Message msg = new Message();
                    msg.what = xian_shi_data;
                    msg.obj = "正在测试：" + selectedtrack + "，请稍候..." + "\n";
                    // msg.obj = "under test ：" + selectedtrack + "，please wait..." + "\n";

                    msgHandler.sendMessage(msg);
                  /*  pwmolder.clear();
                    pwmolder = LitePal.where("code = ?", selectedtrack).find(PathwayMolder.class);*/
                   /* if(pwmolder.get(0).getErrorcode().equals("0")){
                        Message msg1 = new Message();
                        msg1.what = xian_shi_data;
                        msg1.obj = "货道" + selectedtrack + "已被锁定，请解除！"+ "\n";
                        // msg.obj = "under test ：" + selectedtrack + "，please wait..." + "\n";

                        msgHandler.sendMessage(msg1);
                        return;
                    }*/
                    LogUtils.d("TestMagexActivity:测试电机:::" + selectedtrack.substring(0, 1) + "," + Integer.parseInt(selectedtrack.substring(1, 2)));
                    ComTrackMagex comTrack = new ComTrackMagex("");
                    comTrack.openSerialPort();

                    String rtn[] = comTrack.vend_out_ind(
                            Integer.parseInt(selectedtrack.substring(0, 1)), Integer.parseInt(selectedtrack.substring(1, 2)),

                            1, "", "", "");

                    comTrack.closeSerialPort();
                    if (rtn[0].length() != 0) {
                        PathwayMolder molder = new PathwayMolder();
                        molder.setErrorcode("0");
                        molder.setErrortime(DateUtils.getCurrentTime_Today());
                        molder.updateAll("code = ?", selectedtrack);
                    } else {
                        PathwayMolder molder = new PathwayMolder();
                        molder.setErrorcode("1");
                        molder.setErrortime("");
                        molder.updateAll("code = ?", selectedtrack);
                    }

//                    if (rtn[0].length() == 0) {
//                        // 修改数据库
//                        SQLiteDatabase db = openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
//
//                        db.execSQL("update trackmaingeneral set errorcode='',errortime='' where id=" + selectedtrack);
//                        //修改Global的值
//                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(selectedtrack) - 10].setErrorcode("");
//                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(selectedtrack) - 10].setErrortime("");
//
//                        db.close();
//                    }
                    LogUtils.d(classname+"测试电机"+selectedtrack+"rtn[0]:::" + rtn[0]);
                    disp = "出货电机：" + selectedtrack + ",测试结果：" + (rtn[0].length() == 0 ? "成功" : rtn[0]) + "\n";
                    //  disp = "Shipment motor：" + selectedtrack + ",test result ：" + (rtn[0].length() == 0 ? "succeed" : rtn[0]) + "\n";

                    test_resultstr = test_resultstr + disp;

                    Message msg2 = new Message();
                    msg2.what = xian_shi_data;
                    msg2.obj = test_resultstr;
                    msgHandler.sendMessage(msg2);

                } else if ("onelevel".equalsIgnoreCase(testtype)) {
                    pwmolder.clear();
                    pwmolderes.clear();
                    pwmolder = LitePal.findAll(PathwayMolder.class);
                    for (int i = 0;i < pwmolder.size();i++){
                        if(pwmolder.get(i).getMergecode().equals("01")){
                            pwmolderes.add(pwmolder.get(i));
                        }
                    }
                    int[] leveldetail;
                    List<String> codes = new ArrayList<>();
                    if (selectedlevelint == 0) {
                        // 那就是要全部的轨道啦
                        //int testlevel = ((MyApp) getApplication()).getMainGeneralLevelNum();
                        leveldetail = new int[]{0,0,0,0,0,0,0};

                        for (int i = 0;i < pwmolderes.size();i++){
                            if(pwmolderes.get(i).getCode().substring(0,1).equals("1")){
                                leveldetail[0] = leveldetail[0]+1;
                            } else if(pwmolderes.get(i).getCode().substring(0,1).equals("2")){
                                leveldetail[1] = leveldetail[1]+1;
                            } else if(pwmolderes.get(i).getCode().substring(0,1).equals("3")){
                                leveldetail[2] = leveldetail[2]+1;
                            } else if(pwmolderes.get(i).getCode().substring(0,1).equals("4")){
                                leveldetail[3] = leveldetail[3]+1;
                            } else if(pwmolderes.get(i).getCode().substring(0,1).equals("5")){
                                leveldetail[4] = leveldetail[4]+1;
                            } else if(pwmolderes.get(i).getCode().substring(0,1).equals("6")){
                                leveldetail[5] = leveldetail[5]+1;
                            } else if(pwmolderes.get(i).getCode().substring(0,1).equals("7")){
                                leveldetail[6] = leveldetail[6]+1;
                            }
                        }
                    } else {
                        leveldetail = new int[1];
                        codes.clear();
                        //leveldetail[0] = levelnum[selectedlevelint-1];
                        for (int i = 0;i < pwmolderes.size();i++){
                            if(pwmolderes.get(i).getCode().substring(0,1).equals(selectedlevelint)){
                                leveldetail[0] = leveldetail[0]+1;
                                codes.add(pwmolderes.get(i).getCode());
                            }
                        }
                    }

                    ComTrackMagex comTrack = new ComTrackMagex("");
                    comTrack.openSerialPort();

                   /* Log.d("zlc","zlc:selectedforlevel-----"+selectedforlevel);

                    return;*/

                    for (int k = 0; k < Integer.parseInt(selectedforlevel); k++) {
                        // 循环次数

                        boolean everymotorok = true;
                        int number = 0;
                        for (int l = 0; l < leveldetail.length; l++) {
                            // 层数

                            //if (l == 0) {
                                test_resultstr = "";
                            //}

                            for (int j = 0; j < leveldetail[l]; j++) {
                                // 每一层就几个轨道

                                int trackno = 0;
                                Log.d("zlc","zlc:selectedlevelint-----"+selectedlevelint);
                               if(selectedlevelint != 0){

                                       trackno = Integer.parseInt(codes.get(j))+1;

                               }else{
                                   Log.d("zlc","zlc:number-----"+number);
                                   trackno = Integer.parseInt(pwmolderes.get(number).getCode());
                                }
                                number++;
                                Log.d("zlc","zlc:trackno-----"+trackno);
                                // 中途退出
                                if (exit) {
                                    k = 99;
                                    l = 9;
                                    break;
                                }

                                test_resultstr = test_resultstr + "正在测试：" + trackno + "，请稍候..." + "\n";
                                // test_resultstr = test_resultstr + "under test：" + trackno + "，please wait..." + "\n";

                                Message msg = new Message();
                                msg.what = xian_shi_data;
                                msg.obj = test_resultstr;
                                msgHandler.sendMessage(msg);

                                String[] rtn = comTrack.vend_out_ind(trackno / 10, trackno % 10, 1, "", "", "");

                                if (rtn[0].length() == 0) {
                                    test_resultstr = test_resultstr + "循环：" + (k + 1) + ";出货电机：" + trackno + ",测试结果：正常！\n";
                                    // test_resultstr = test_resultstr + "circulation：" + (k + 1) + ";Shipment motor：" + trackno + ",Test results: normal！\n";

//                                    if(((MyApp) getApplication()).getTrackMainGeneral()[trackno - 10].getErrorcode().length() > 0) {
//                                        // 修改数据库
//                                        SQLiteDatabase db = openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
//
//                                        db.execSQL("update trackmaingeneral set errorcode='',errortime='' where id=" + selectedtrack);
//                                        //修改Global的值
//                                        ((MyApp) getApplication()).getTrackMainGeneral()[trackno - 10].setErrorcode("");
//                                        ((MyApp) getApplication()).getTrackMainGeneral()[trackno - 10].setErrortime("");
//
//                                        db.close();
//                                    }
                                    PathwayMolder molder = new PathwayMolder();
                                    molder.setErrorcode("1");
                                    molder.setErrortime("");
                                    molder.updateAll("code = ?", trackno + "");
                                } else {
                                    test_resultstr = test_resultstr + "循环：" + (k + 1) + ";出货电机：" + trackno + ",测试结果：" + rtn[0] + "\n";
                                    PathwayMolder molder = new PathwayMolder();
                                    molder.setErrorcode("0");
                                    molder.setErrortime(DateUtils.getCurrentTime_Today());
                                    molder.updateAll("code = ?", trackno + "");
                                    everymotorok = false;
                                }

                                Message msg2 = new Message();
                                msg2.what = xian_shi_data;
                                msg2.obj = test_resultstr;
                                msgHandler.sendMessage(msg2);

                                SystemClock.sleep(5000); // 延时 5秒后继续下一个轨道

                            }
                        }
                        if (!everymotorok) break;
                    }
                    comTrack.closeSerialPort();

                    if (!exit) {
                        Message msg = new Message();
                        msg.what = xian_shi_data;
                        //  msg.obj = test_resultstr + "测试结束！";
                        msg.obj = test_resultstr + "EOT EndOfTest！";
                        msgHandler.sendMessage(msg);
                    }
                }

            } catch (SQLException e) {
                LogUtils.e(TAG + ":::执行SQL命令" + e.toString());
                Message msg = new Message();
                msg.what = xian_shi_data;
                msg.obj = e.toString();
                msgHandler.sendMessage(msg);
            } catch (Exception ex) {
                LogUtils.e(TAG + "::: 异常命令" + ex.toString());
                Message msg = new Message();
                msg.what = xian_shi_data;
                msg.obj = ex.toString();
                msgHandler.sendMessage(msg);
            }

            isOuting = false; // 表示处理已完成了
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
                    LogUtils.d("TestMagexActivity:::CESHI" + msg.obj.toString());
                    test_result.setSelection(test_result.getText().length(), test_result.getText().length());
                    test_result.setMovementMethod(ScrollingMovementMethod.getInstance());


                    String errorinfo = getError();
                    if (errorinfo.length() == 0) {
                        errotrack.setText("" + "_" + ComTrackMagex.activeThreadCount + "_故障轨道:无");
                        //errotrack.setText("" + "_" + ComTrackMagex.activeThreadCount + "_Fault track: none");
                    } else {
                        errotrack.setText("" + "_" + ComTrackMagex.activeThreadCount + "_故障轨道:" + errorinfo);
                        // errotrack.setText("" + "_" + ComTrackMagex.activeThreadCount + "Fault track:" + errorinfo);
                    }

                    break;

                case xian_shi_error:
                    if (hinttvstr.length() > 0) {
                        hinttv.setText("" + "_" + ComTrackMagex.activeThreadCount + "_错误:" + hinttvstr.substring(1));
                        // hinttv.setText("" + "_" + ComTrackMagex.activeThreadCount + "_mistake:" + hinttvstr.substring(1));
                    } else {
                        hinttv.setText("" + "_" + ComTrackMagex.activeThreadCount + "_错误:无");
                        // hinttv.setText("" + "_" + ComTrackMagex.activeThreadCount + "_Error: no");
                    }
                    break;

                default:
                    break;

            }
            return false;
        }
    });

}
