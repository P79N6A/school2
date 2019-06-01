package com.freshtribes.icecream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.freshtribes.icecream.app.MyApp;

import android_serialport_api.ComTrackMagex;

public class EnableActivity extends Activity implements View.OnClickListener{
    private String TAG = "Enable~~~";
    private ImageView[] ivt = new ImageView[70];
    private TextView[] steptv = new TextView[70];
    private Button backBtn;
    private Button getMagexBtn;
    private Button setMagexBtn;
    private TextView strTV;
    private TextView allStepTV;

    private MyApp myApp;
    private int[] mainTrack = {0,0,0,0,0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable);

        myApp = ((MyApp)getApplication());

        ivt[0] = findViewById(R.id.t10);
        ivt[1] = findViewById(R.id.t11);
        ivt[2] = findViewById(R.id.t12);
        ivt[3] = findViewById(R.id.t13);
        ivt[4] = findViewById(R.id.t14);
        ivt[5] = findViewById(R.id.t15);
        ivt[6] = findViewById(R.id.t16);
        ivt[7] = findViewById(R.id.t17);
        ivt[8] = findViewById(R.id.t18);
        ivt[9] = findViewById(R.id.t19);
        ivt[10] = findViewById(R.id.t20);
        ivt[11] = findViewById(R.id.t21);
        ivt[12] = findViewById(R.id.t22);
        ivt[13] = findViewById(R.id.t23);
        ivt[14] = findViewById(R.id.t24);
        ivt[15] = findViewById(R.id.t25);
        ivt[16] = findViewById(R.id.t26);
        ivt[17] = findViewById(R.id.t27);
        ivt[18] = findViewById(R.id.t28);
        ivt[19] = findViewById(R.id.t29);
        ivt[20] = findViewById(R.id.t30);
        ivt[21] = findViewById(R.id.t31);
        ivt[22] = findViewById(R.id.t32);
        ivt[23] = findViewById(R.id.t33);
        ivt[24] = findViewById(R.id.t34);
        ivt[25] = findViewById(R.id.t35);
        ivt[26] = findViewById(R.id.t36);
        ivt[27] = findViewById(R.id.t37);
        ivt[28] = findViewById(R.id.t38);
        ivt[29] = findViewById(R.id.t39);
        ivt[30] = findViewById(R.id.t40);
        ivt[31] = findViewById(R.id.t41);
        ivt[32] = findViewById(R.id.t42);
        ivt[33] = findViewById(R.id.t43);
        ivt[34] = findViewById(R.id.t44);
        ivt[35] = findViewById(R.id.t45);
        ivt[36] = findViewById(R.id.t46);
        ivt[37] = findViewById(R.id.t47);
        ivt[38] = findViewById(R.id.t48);
        ivt[39] = findViewById(R.id.t49);
        ivt[40] = findViewById(R.id.t50);
        ivt[41] = findViewById(R.id.t51);
        ivt[42] = findViewById(R.id.t52);
        ivt[43] = findViewById(R.id.t53);
        ivt[44] = findViewById(R.id.t54);
        ivt[45] = findViewById(R.id.t55);
        ivt[46] = findViewById(R.id.t56);
        ivt[47] = findViewById(R.id.t57);
        ivt[48] = findViewById(R.id.t58);
        ivt[49] = findViewById(R.id.t59);
        ivt[50] = findViewById(R.id.t60);
        ivt[51] = findViewById(R.id.t61);
        ivt[52] = findViewById(R.id.t62);
        ivt[53] = findViewById(R.id.t63);
        ivt[54] = findViewById(R.id.t64);
        ivt[55] = findViewById(R.id.t65);
        ivt[56] = findViewById(R.id.t66);
        ivt[57] = findViewById(R.id.t67);
        ivt[58] = findViewById(R.id.t68);
        ivt[59] = findViewById(R.id.t69);
        ivt[60] = findViewById(R.id.t70);
        ivt[61] = findViewById(R.id.t71);
        ivt[62] = findViewById(R.id.t72);
        ivt[63] = findViewById(R.id.t73);
        ivt[64] = findViewById(R.id.t74);
        ivt[65] = findViewById(R.id.t75);
        ivt[66] = findViewById(R.id.t76);
        ivt[67] = findViewById(R.id.t77);
        ivt[68] = findViewById(R.id.t78);
        ivt[69] = findViewById(R.id.t79);
        for(int i=0;i<70;i++){
            if(i%10 != 0) {
                ivt[i].setOnClickListener(this);
            }
        }

        steptv[0] = findViewById(R.id.ts10);
        steptv[1] = findViewById(R.id.ts11);
        steptv[2] = findViewById(R.id.ts12);
        steptv[3] = findViewById(R.id.ts13);
        steptv[4] = findViewById(R.id.ts14);
        steptv[5] = findViewById(R.id.ts15);
        steptv[6] = findViewById(R.id.ts16);
        steptv[7] = findViewById(R.id.ts17);
        steptv[8] = findViewById(R.id.ts18);
        steptv[9] = findViewById(R.id.ts19);
        steptv[10] = findViewById(R.id.ts20);
        steptv[11] = findViewById(R.id.ts21);
        steptv[12] = findViewById(R.id.ts22);
        steptv[13] = findViewById(R.id.ts23);
        steptv[14] = findViewById(R.id.ts24);
        steptv[15] = findViewById(R.id.ts25);
        steptv[16] = findViewById(R.id.ts26);
        steptv[17] = findViewById(R.id.ts27);
        steptv[18] = findViewById(R.id.ts28);
        steptv[19] = findViewById(R.id.ts29);
        steptv[20] = findViewById(R.id.ts30);
        steptv[21] = findViewById(R.id.ts31);
        steptv[22] = findViewById(R.id.ts32);
        steptv[23] = findViewById(R.id.ts33);
        steptv[24] = findViewById(R.id.ts34);
        steptv[25] = findViewById(R.id.ts35);
        steptv[26] = findViewById(R.id.ts36);
        steptv[27] = findViewById(R.id.ts37);
        steptv[28] = findViewById(R.id.ts38);
        steptv[29] = findViewById(R.id.ts39);
        steptv[30] = findViewById(R.id.ts40);
        steptv[31] = findViewById(R.id.ts41);
        steptv[32] = findViewById(R.id.ts42);
        steptv[33] = findViewById(R.id.ts43);
        steptv[34] = findViewById(R.id.ts44);
        steptv[35] = findViewById(R.id.ts45);
        steptv[36] = findViewById(R.id.ts46);
        steptv[37] = findViewById(R.id.ts47);
        steptv[38] = findViewById(R.id.ts48);
        steptv[39] = findViewById(R.id.ts49);
        steptv[40] = findViewById(R.id.ts50);
        steptv[41] = findViewById(R.id.ts51);
        steptv[42] = findViewById(R.id.ts52);
        steptv[43] = findViewById(R.id.ts53);
        steptv[44] = findViewById(R.id.ts54);
        steptv[45] = findViewById(R.id.ts55);
        steptv[46] = findViewById(R.id.ts56);
        steptv[47] = findViewById(R.id.ts57);
        steptv[48] = findViewById(R.id.ts58);
        steptv[49] = findViewById(R.id.ts59);
        steptv[50] = findViewById(R.id.ts60);
        steptv[51] = findViewById(R.id.ts61);
        steptv[52] = findViewById(R.id.ts62);
        steptv[53] = findViewById(R.id.ts63);
        steptv[54] = findViewById(R.id.ts64);
        steptv[55] = findViewById(R.id.ts65);
        steptv[56] = findViewById(R.id.ts66);
        steptv[57] = findViewById(R.id.ts67);
        steptv[58] = findViewById(R.id.ts68);
        steptv[59] = findViewById(R.id.ts69);
        steptv[60] = findViewById(R.id.ts70);
        steptv[61] = findViewById(R.id.ts71);
        steptv[62] = findViewById(R.id.ts72);
        steptv[63] = findViewById(R.id.ts73);
        steptv[64] = findViewById(R.id.ts74);
        steptv[65] = findViewById(R.id.ts75);
        steptv[66] = findViewById(R.id.ts76);
        steptv[67] = findViewById(R.id.ts77);
        steptv[68] = findViewById(R.id.ts78);
        steptv[69] = findViewById(R.id.ts79);
        for(int i=0;i<70;i++){
            steptv[i].setOnClickListener(this);
        }

        mainTrack[0] = myApp.getMainGeneralLevel1TrackCount();
        if(myApp.getMainGeneralLevelNum() == 7){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
            mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
            mainTrack[6] = myApp.getMainGeneralLevel7TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 6){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
            mainTrack[5] = myApp.getMainGeneralLevel6TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 5){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
            mainTrack[4] = myApp.getMainGeneralLevel5TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 4){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
            mainTrack[3] = myApp.getMainGeneralLevel4TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 3){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
            mainTrack[2] = myApp.getMainGeneralLevel3TrackCount();
        } else if(myApp.getMainGeneralLevelNum() == 2){
            mainTrack[1] = myApp.getMainGeneralLevel2TrackCount();
        }
        for(int i=1;i<=7;i++) {
            // 第一层有5个轨道
            for(int j=0;j<mainTrack[i-1];j++) {
                if(myApp.getTrackMainGeneral()[(i-1)*10+j].getCansale()==1) {
                    ivt[(i-1)*10+j].setImageResource(R.drawable.icon_on);
                    steptv[(i-1)*10+j].setText(""+myApp.getMagexStep()[(i-1)*10+j]+":"+myApp.getTrackMainGeneral()[(i-1)*10+j].getNummax());
                } else if(myApp.getTrackMainGeneral()[(i-1)*10+j].getCansale()==0) {
                    ivt[(i - 1) * 10 + j].setImageResource(R.drawable.icon_off);
                    steptv[(i-1)*10+j].setVisibility(View.INVISIBLE);
                } else {
                    ivt[(i-1)*10+j].setImageResource(R.drawable.icon_no);
                    steptv[(i-1)*10+j].setVisibility(View.INVISIBLE);
                }
            }
            for(int j=mainTrack[i-1];j<10;j++){
                ivt[(i-1)*10+j].setVisibility(View.INVISIBLE);
                steptv[(i-1)*10+j].setVisibility(View.INVISIBLE);
            }
        }

        backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent toMenu = new Intent(EnableActivity.this, MenuActivity.class);
                startActivity(toMenu);
                EnableActivity.this.finish();

            }
        });

        strTV = findViewById(R.id.magexaddresstv);

        getMagexBtn = findViewById(R.id.btnGetMagex);
        getMagexBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.getmagexresulttv)).setText("");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ComTrackMagex comTrackMagex = new ComTrackMagex("");
                        comTrackMagex.openSerialPort();

                        String step14 = comTrackMagex.getMotorSteps14();
                        String step58 = comTrackMagex.getMotorSteps58();

                        String str14 = comTrackMagex.getMotorAddress14();
                        String str58 = comTrackMagex.getMotorAddress58();
                        if(str14.equals("error")
                                || str58.equals("error")
                                || step14.equals("error")
                                || step58.equals("error")) {
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            strTV.setText("获取硬件的货道信息：出错了！");
                                        }
                                    }
                            );
                        } else if(str14.split(",")[0].substring(0,(myApp.getMainGeneralLevel1TrackCount()>0?myApp.getMainGeneralLevel1TrackCount()*2:0)).contains("80")
                                || str14.split(",")[0].substring(0,(myApp.getMainGeneralLevel1TrackCount()>0?myApp.getMainGeneralLevel1TrackCount()*2:0)).contains("81")
                                || str14.split(",")[0].substring(0,(myApp.getMainGeneralLevel1TrackCount()>0?myApp.getMainGeneralLevel1TrackCount()*2:0)).contains("82")
                                || str14.split(",")[1].substring(0,(myApp.getMainGeneralLevel2TrackCount()>0?myApp.getMainGeneralLevel2TrackCount()*2:0)).contains("80")
                                || str14.split(",")[1].substring(0,(myApp.getMainGeneralLevel2TrackCount()>0?myApp.getMainGeneralLevel2TrackCount()*2:0)).contains("81")
                                || str14.split(",")[1].substring(0,(myApp.getMainGeneralLevel2TrackCount()>0?myApp.getMainGeneralLevel2TrackCount()*2:0)).contains("82")
                                || str14.split(",")[2].substring(0,(myApp.getMainGeneralLevel3TrackCount()>0?myApp.getMainGeneralLevel3TrackCount()*2:0)).contains("80")
                                || str14.split(",")[2].substring(0,(myApp.getMainGeneralLevel3TrackCount()>0?myApp.getMainGeneralLevel3TrackCount()*2:0)).contains("81")
                                || str14.split(",")[2].substring(0,(myApp.getMainGeneralLevel3TrackCount()>0?myApp.getMainGeneralLevel3TrackCount()*2:0)).contains("82")
                                || str14.split(",")[3].substring(0,(myApp.getMainGeneralLevel4TrackCount()>0?myApp.getMainGeneralLevel4TrackCount()*2:0)).contains("80")
                                || str14.split(",")[3].substring(0,(myApp.getMainGeneralLevel4TrackCount()>0?myApp.getMainGeneralLevel4TrackCount()*2:0)).contains("81")
                                || str14.split(",")[3].substring(0,(myApp.getMainGeneralLevel4TrackCount()>0?myApp.getMainGeneralLevel4TrackCount()*2:0)).contains("82")
                                || str58.split(",")[0].substring(0,(myApp.getMainGeneralLevel5TrackCount()>0?myApp.getMainGeneralLevel5TrackCount()*2:0)).contains("80")
                                || str58.split(",")[0].substring(0,(myApp.getMainGeneralLevel5TrackCount()>0?myApp.getMainGeneralLevel5TrackCount()*2:0)).contains("81")
                                || str58.split(",")[0].substring(0,(myApp.getMainGeneralLevel5TrackCount()>0?myApp.getMainGeneralLevel5TrackCount()*2:0)).contains("82")
                                || str58.split(",")[1].substring(0,(myApp.getMainGeneralLevel6TrackCount()>0?myApp.getMainGeneralLevel6TrackCount()*2:0)).contains("80")
                                || str58.split(",")[1].substring(0,(myApp.getMainGeneralLevel6TrackCount()>0?myApp.getMainGeneralLevel6TrackCount()*2:0)).contains("81")
                                || str58.split(",")[1].substring(0,(myApp.getMainGeneralLevel6TrackCount()>0?myApp.getMainGeneralLevel6TrackCount()*2:0)).contains("82")
                                || str58.split(",")[2].substring(0,(myApp.getMainGeneralLevel7TrackCount()>0?myApp.getMainGeneralLevel7TrackCount()*2:0)).contains("80")
                                || str58.split(",")[2].substring(0,(myApp.getMainGeneralLevel7TrackCount()>0?myApp.getMainGeneralLevel7TrackCount()*2:0)).contains("81")
                                || str58.split(",")[2].substring(0,(myApp.getMainGeneralLevel7TrackCount()>0?myApp.getMainGeneralLevel7TrackCount()*2:0)).contains("82")){
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            strTV.setText("获取硬件的货道信息：货道地址有错误信息！请通过内部键盘查看错误！");
                                        }
                                    }
                            );
                        } else {
                            // 地址
                            String address = str14 +","+str58;
                            address = address.replace("02"," *").replace("00"," -").replace("01"," 1");
                            String[] array = address.split(",");
                            for(int i=0;i<7;i++) {
                                array[i] = array[i].substring(0,mainTrack[i]*2);
                            }
                            // 货道走步
                            String allstep = step14 + "," + step58;
                            String[] steplevel = allstep.split(",");




                            String disp = "";
                            for(int i=0;i<7;i++) {
                                if(array[i].length()>0){
                                    disp = disp + "第"+(i+1)+"层：" ;
                                    for(int k=0;k<array[i].length()/2;k++){
                                        if(array[i].substring(k*2,k*2+2).equals(" 1")){
                                            disp = disp + array[i].substring(k*2,k*2+2)+"/"+Integer.parseInt(steplevel[i].substring(k*2,k*2+2),16);
                                        } else {
                                            disp = disp + array[i].substring(k*2,k*2+2)+"   ";
                                        }
                                    }
                                    disp = disp + "\n";
                                }
                            }

                            // 界面的设置是否和里面的设置是否一致
                            boolean isOK = true;
                            for(int i=1;i<=7;i++) {
                                for(int j=0;j<mainTrack[i-1];j++) {
                                    if(myApp.getTrackMainGeneral()[(i-1)*10+j].getCansale()==1 && array[i-1].substring(j*2,(j+1)*2).equals(" 1")) {
                                        // 说明是一致的
                                        if(myApp.getMagexStep()[(i-1)*10+j] != Integer.parseInt(steplevel[i-1].substring(j*2,j*2+2),16)){
                                            isOK = false;
                                        }
                                    } else  if(myApp.getTrackMainGeneral()[(i-1)*10+j].getCansale()==0 && array[i-1].substring(j*2,(j+1)*2).equals(" *")) {
                                        // 说明是一致的
                                    } else  if(myApp.getTrackMainGeneral()[(i-1)*10+j].getCansale()==-1 && array[i-1].substring(j*2,(j+1)*2).equals(" -")) {
                                        // 说明是一致的
                                    } else {
                                        isOK = false;
                                    }
                                }
                            }

                            final String final_disp = disp;
                            final boolean final_isOK = isOK;
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            strTV.setText(final_disp);
                                            if(!final_isOK){
                                                ((TextView)findViewById(R.id.getmagexresulttv)).setText("*设置不一致，需要再次设定！");
                                            } else {
                                                ((TextView)findViewById(R.id.getmagexresulttv)).setText("OK ! 设置一致的！");
                                            }
                                        }
                                    }
                            );
                        }

                        comTrackMagex.closeSerialPort();
                    }
                }).start();

            }
        });


        setMagexBtn = findViewById(R.id.btnSetMagex);
        setMagexBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                ((TextView)findViewById(R.id.getmagexresulttv)).setText("");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String str = "0101010101010101010101010101FFFF";
                        String str8 = str + str +str +str +str +str +str +str;
                        for(int i=1;i<=7;i++) {
                            for(int j=0;j<mainTrack[i-1];j++) {
                                if(myApp.getTrackMainGeneral()[(i-1)*10+j].getCansale()==0) {
                                    str8 = str8.substring(0,((i-1)*16+j)*2)
                                            + "02" + str8.substring((((i-1)*16+j)*2)+2);
                                }
                                if(myApp.getTrackMainGeneral()[(i-1)*10+j].getCansale()==-1) {
                                    str8 = str8.substring(0,((i-1)*16+j)*2)
                                            + "00" + str8.substring((((i-1)*16+j)*2)+2);
                                }
                            }
                        }
                        Log.i(TAG,"新的货道合并信息:"+str8);

                        String strStep = "03030303030303030303010101011313";
                        String strStep8 = strStep + strStep +strStep +strStep +strStep +strStep +strStep +strStep;
                        for(int i=1;i<=7;i++) {
                            for(int j=0;j<mainTrack[i-1];j++) {
                                if(myApp.getTrackMainGeneral()[(i-1)*10+j].getCansale()==1) {
                                    strStep8 = strStep8.substring(0,((i-1)*16+j)*2)
                                            + String.format("%02x", myApp.getMagexStep()[(i-1)*10+j]).toUpperCase() + strStep8.substring((((i-1)*16+j)*2)+2);
                                }
                            }
                        }
                        Log.i(TAG,"新的货道走步信息:"+strStep8);

                        ComTrackMagex comTrackMagex = new ComTrackMagex("");
                        comTrackMagex.openSerialPort();

                        String status = comTrackMagex.getStatus();
                        if(status.contains("Tech mode")){
                            comTrackMagex.closeSerialPort();
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            ((TextView)findViewById(R.id.getmagexresulttv)).setText("失败 ！必须在用户模式下！");
                                        }
                                    }
                            );
                            return;
                        }

                        String str14 = comTrackMagex.setMotorAddress14(str8.substring(0,64*2));
                        String str58 = comTrackMagex.setMotorAddress58(str8.substring(64*2));
                        String strstep14 = comTrackMagex.setMotorSteps14(strStep8.substring(0,64*2));
                        String strstep58 = comTrackMagex.setMotorSteps58(strStep8.substring(64*2));
                        String refresh = comTrackMagex.refreshEEPROM();
                        if(str14.equals("error") || str58.equals("error") || strstep14.equals("error") || strstep58.equals("error")) {
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            ((TextView)findViewById(R.id.getmagexresulttv)).setText("*设置失败，请联系管理员！");
                                        }
                                    }
                            );
                        } else {
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            ((TextView)findViewById(R.id.getmagexresulttv)).setText("OK ！ 设置成功！请再次获取进行验证！");
                                        }
                                    }
                            );
                        }

                        comTrackMagex.closeSerialPort();
                    }
                }).start();

            }
        });

        allStepTV = findViewById(R.id.alllianban);
        allStepTV.setText("当前链板总数："+myApp.getMagexallstep());
        Button allStepBtn = findViewById(R.id.btn_alllianban);
        allStepBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String[] nstr = new String[]{"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"};
                int dispinit = myApp.getMagexallstep() - 10;
                AlertDialog al = new AlertDialog.Builder(EnableActivity.this)
                        .setTitle("请选择链板总步数")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setSingleChoiceItems(nstr, dispinit,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {

                                        int selectnummax = which + 10;

                                        myApp.setMagexallstep(selectnummax);
                                        allStepTV.setText("当前链板总数："+myApp.getMagexallstep());

                                        // checked
                                        SharedPreferences sp =
                                                EnableActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();//获取编辑器
                                        editor.putInt("allstep", selectnummax);
                                        editor.apply();//提交修改

                                        dialog.dismiss();
                                    }
                                }
                        )
                        .setNegativeButton("取消", null)
                        .create();
                al.setCancelable(false);
                al.show();

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v instanceof ImageView) {
            for (int i = 0; i < 70; i++) {
                if (ivt[i] == (ImageView) v) {
                    if (myApp.getTrackMainGeneral()[i].getCansale() == 1) {
                        myApp.getTrackMainGeneral()[i].setCansale(0);
                        ivt[i].setImageResource(R.drawable.icon_off);
                        steptv[i].setVisibility(View.INVISIBLE);

                        final int trackno = i + 10;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                                db.execSQL("update trackmaingeneral set cansale=0 where id=" + trackno);
                                db.close();
                            }
                        }).start();
                    } else if (myApp.getTrackMainGeneral()[i].getCansale() == 0) {
                        myApp.getTrackMainGeneral()[i].setCansale(-1);
                        ivt[i].setImageResource(R.drawable.icon_no);
                        steptv[i].setVisibility(View.INVISIBLE);

                        final int trackno = i + 10;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                                db.execSQL("update trackmaingeneral set cansale=-1 where id=" + trackno);
                                db.close();
                            }
                        }).start();
                    } else {
                        myApp.getTrackMainGeneral()[i].setCansale(1);
                        ivt[i].setImageResource(R.drawable.icon_on);
                        steptv[i].setVisibility(View.VISIBLE);
                        steptv[i].setText("" + myApp.getMagexStep()[i]+":"+myApp.getTrackMainGeneral()[i].getNummax());

                        final int trackno = i + 10;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
                                db.execSQL("update trackmaingeneral set cansale=1 where id=" + trackno);
                                db.close();
                            }
                        }).start();
                    }
                    break;
                }
            }
        }
        if(v instanceof TextView){
            for (int i = 0; i < 70; i++) {
                if (steptv[i] == (TextView) v) {
                    // 弹出一个对话框来选择最大数
                    String[] nstr = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"};
                    int dispinit = myApp.getMagexStep()[i] - 1;
                    final int final_i = i;
                    AlertDialog al = new AlertDialog.Builder(EnableActivity.this)
                            .setTitle("请选择链板走步数（轨道：" + (i + 10) + ")")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setSingleChoiceItems(nstr, dispinit,
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int which) {

                                            int selectnummax = which + 1;
                                            myApp.getMagexStep()[final_i] = selectnummax;

                                            final int max = myApp.getMagexallstep() / selectnummax;

                                            steptv[final_i].setText("" + selectnummax+":"+max);

                                            // checked
                                            SharedPreferences sp =
                                                    EnableActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();//获取编辑器
                                            editor.putInt("step" + (final_i + 10), selectnummax);  // 0表示没有麦克风
                                            editor.apply();//提交修改

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                                                        db.execSQL("update trackmaingeneral set nummax=" + max + " where id=" + (final_i + 10) );
                                                        myApp.getTrackMainGeneral()[final_i].setNummax(max);

                                                        db.close();

                                                    } catch (SQLException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();

                                            dialog.dismiss();
                                        }
                                    }
                            )
                            .setNegativeButton("取消", null)
                            .create();
                    al.setCancelable(false);
                    al.show();

                    break;
                }
            }
        }

    }
}
