package com.zhongyagroup.school;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhongyagroup.school.app.MyApp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MaxActivity extends Activity implements View.OnClickListener{
    private MyApp myApp;

    private static final int dispHandler_rtnChangeMax = 0x101;

    private ListView mListView;
    private MyListAdapter myListAdapter;
    private ArrayList<Map<String,String>> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_max);

        myApp = (MyApp)getApplication();

        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        // 准备数据
        arrayList = new ArrayList<>();

        Map<String,String> map;
        for(int i=0;i<((MyApp)getApplication()).getMainGeneralLevelNum();i++){
            int kmax = 0;
            if(i==0) {
                kmax = ((MyApp) getApplication()).getMainGeneralLevel1TrackCount();
            } else if(i==1){
                kmax = ((MyApp) getApplication()).getMainGeneralLevel2TrackCount();
            } else if(i==2){
                kmax = ((MyApp) getApplication()).getMainGeneralLevel3TrackCount();
            } else if(i==3){
                kmax = ((MyApp) getApplication()).getMainGeneralLevel4TrackCount();
            } else if(i==4){
                kmax = ((MyApp) getApplication()).getMainGeneralLevel5TrackCount();
            } else if(i==5){
                kmax = ((MyApp) getApplication()).getMainGeneralLevel6TrackCount();
            } else if(i==6){
                kmax = ((MyApp) getApplication()).getMainGeneralLevel7TrackCount();
            }
            for (int k = 0; k < kmax; k++) {
                map = new HashMap<>();
                map.put("trackno","轨道："+(i+1)+k);
                map.put("trackmax","满仓数："+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getNummax());
                arrayList.add(map);
            }
        }

        mListView = findViewById(R.id.listview);
        myListAdapter = new MyListAdapter(this,R.layout.item_max);
        mListView.setAdapter(myListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                Intent toMenu = new Intent(MaxActivity.this, MenuActivity.class);
                startActivity(toMenu);
                MaxActivity.this.finish();
                break;

            default:
                break;
        }
    }

    public class MyListAdapter extends ArrayAdapter<Object> {
        private int mTextViewResourceID = 0;
        private Context mContext;

        public MyListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            mTextViewResourceID = textViewResourceId;
            mContext = context;
        }

        private int[] colors = new int[] { 0xff626569, 0xff4f5257 };

        public int getCount() {
            return arrayList.size();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(mTextViewResourceID, null);
            }

            TextView trackno =  convertView.findViewById(R.id.trackno);
            TextView trackmax =  convertView.findViewById(R.id.trackmax);
            Button changemaxbtn = convertView.findViewById(R.id.changemaxbtn);
            //Button sameasupbtn = convertView.findViewById(R.id.sameasupbtn);

            changemaxbtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    // 弹出一个对话框来选择最大数
                    String[] nstr = new String[] {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16"};
                    int dispinit = 0;

                    String dbtrackno_disp = arrayList.get(position).get("trackno").replace("轨道：","").replace("货道：","");

                    dispinit = ((MyApp)getApplication()).getTrackMainGeneral()[Integer.parseInt(dbtrackno_disp)-10].getNummax();

                    AlertDialog al = new AlertDialog.Builder(MaxActivity.this)
                            .setTitle("请选择最大数（轨道："+dbtrackno_disp+")")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setSingleChoiceItems(nstr, dispinit,
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int which) {

                                            int selectnummax = which;

                                            String dbtrackno = arrayList.get(position).get("trackno").replace("轨道：","").replace("货道：","");
                                            //lp.changeMax(true,false,position,,selectnummax);

                                            // 要对整层进行替换
                                            int leveli = Integer.parseInt(arrayList.get(position).get("trackno").replace("轨道：", ""))/10-1;
                                            int kmax = 0;
                                            if(leveli==0) {
                                                kmax = ((MyApp) getApplication()).getMainGeneralLevel1TrackCount();
                                            } else if(leveli==1){
                                                kmax = ((MyApp) getApplication()).getMainGeneralLevel2TrackCount();
                                            } else if(leveli==2){
                                                kmax = ((MyApp) getApplication()).getMainGeneralLevel3TrackCount();
                                            } else if(leveli==3){
                                                kmax = ((MyApp) getApplication()).getMainGeneralLevel4TrackCount();
                                            } else if(leveli==4){
                                                kmax = ((MyApp) getApplication()).getMainGeneralLevel5TrackCount();
                                            } else if(leveli==5){
                                                kmax = ((MyApp) getApplication()).getMainGeneralLevel6TrackCount();
                                            } else if(leveli==6){
                                                kmax = ((MyApp) getApplication()).getMainGeneralLevel7TrackCount();
                                            }
                                            for (int k = 0; k < kmax; k++) {
                                                Map<String,String> hm = arrayList.get(position+k);
                                                hm.put("trackmax","满仓数："+selectnummax);

                                                final int f_dbtrackno = Integer.parseInt(dbtrackno+k);
                                                final int f_dbtrackno_index = Integer.parseInt(dbtrackno)+k - 10;
                                                final int f_selectnummax = selectnummax;
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                                                            db.execSQL("update trackmaingeneral set nummax=" + f_selectnummax + " where id=" + f_dbtrackno );
                                                            myApp.getTrackMainGeneral()[f_dbtrackno_index].setNummax(f_selectnummax);

                                                            db.close();

                                                        } catch (SQLException e) {
                                                            e.printStackTrace();
                                                        }

                                                        Map<String,String> hm = arrayList.get(position);
                                                        hm.put("trackmax","满仓数："+f_selectnummax);

                                                    }
                                                }).start();
                                            }
                                            dispHandler.sendEmptyMessage(dispHandler_rtnChangeMax);




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


//            sameasupbtn.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View arg0) {
//                    if(position>0) {
//                        int upposition = position - 1;
//                        String uppositionmax = arrayList.get(upposition).get("trackmax").replace("满仓数：", "");
//
//                        int selectnummax = Integer.parseInt(uppositionmax);
//
//                        String dbtrackno = arrayList.get(position).get("trackno").replace("轨道：", "").replace("货道：", "");
//                        //lp.changeMax(true, false, position, Integer.parseInt(dbtrackno), selectnummax);
//
//                        final int f_dbtrackno = Integer.parseInt(dbtrackno);
//                        final int f_dbtrackno_index = Integer.parseInt(dbtrackno) - 10;
//                        final int f_selectnummax = selectnummax;
//
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);
//
//                                    db.execSQL("update trackmaingeneral set nummax=" + f_selectnummax + " where id=" + f_dbtrackno );
//                                    myApp.getTrackMainGeneral()[f_dbtrackno_index].setNummax(f_selectnummax);
//
//                                    db.close();
//
//                                } catch (SQLException e) {
//                                    e.printStackTrace();
//                                }
//
//                                Map<String,String> hm = arrayList.get(position);
//                                hm.put("trackmax","满仓数："+f_selectnummax);
//                                dispHandler.sendEmptyMessage(dispHandler_rtnChangeMax);
//                            }
//                        }).start();
//                    }
//
//                }
//            });
//            if(position==0){
//                sameasupbtn.setVisibility(View.INVISIBLE);
//            } else {
//                sameasupbtn.setVisibility(View.VISIBLE);
//            }

            int colorPos = position % colors.length;
            convertView.setBackgroundColor(colors[colorPos]);

            Map<String,String> nowmap = arrayList.get(position);
            trackno.setText(nowmap.get("trackno"));
            trackmax.setText(nowmap.get("trackmax"));

            if(position%10 == 0){
                changemaxbtn.setVisibility(View.VISIBLE);
            } else {
                changemaxbtn.setVisibility(View.INVISIBLE);
            }



            return convertView;
        }
    }


    /**
     * Handle线程，显示处理结果
     */
    Handler dispHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // 处理消息
            switch (msg.what) {
                case dispHandler_rtnChangeMax:

                    myListAdapter.notifyDataSetChanged();

                    break;


                default:
                    break;
            }
            return false;
        }
    });
}
