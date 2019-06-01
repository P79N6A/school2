package com.freshtribes.icecream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.presenter.MaxPresenter;
import com.freshtribes.icecream.presenter.StockCheckPresenter;
import com.freshtribes.icecream.util.BitmapUtil;
import com.freshtribes.icecream.util.LogUtils;
import com.freshtribes.icecream.util.ProgressDialog;
import com.freshtribes.icecream.view.IStockCheckView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StockCheckActivity extends Activity implements IStockCheckView, View.OnClickListener{
    private final static SimpleDateFormat sdfl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ThreadGroup tg = new ThreadGroup("StockCheckActivityThreadGroup");

    private Dialog mProgressDialog;

    private StockCheckPresenter lp;
    private static final int dispHandler_rtnSaveOK = 0x101;
    private static final int dispHandler_rtnSaveError = 0x102;
    private static final int dispHandler_addone = 0x103;
    private static final int dispHandler_subone = 0x104;

    private ListView mListView;
    private MyListAdapter myListAdapter;
    private ArrayList<Map<String,String>> arrayList;

    private int totalchange = 0;
    private TextView changenumtv;

    private HashMap<String,Bitmap> goodspng = new HashMap<>();

    @Override
    protected void onDestroy() {
        Iterator iter = goodspng.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            Bitmap val = (Bitmap)entry.getValue();
            if(val.isRecycled()) {
                val.recycle();
            }
            val = null;
            goodspng.put(key,val);
        }
        System.gc();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_check);

        // 生成Presenter
        lp = new StockCheckPresenter(this,tg,(MyApp)getApplication());

        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        Button btnallcansale = findViewById(R.id.btn_submit);
        btnallcansale.setOnClickListener(this);

        changenumtv = findViewById(R.id.changenumtv);
        changenumtv.setText("");

        TextView stockchecktimetv = findViewById(R.id.stockchecktimetv);
        String propertystockchecktime = ((MyApp)getApplication()).getStockchecktime();
        if(propertystockchecktime.length() == 0){
            stockchecktimetv.setText("上次盘点时间：无");
        } else if(propertystockchecktime.length() > 10){
            String nowtime = sdfl.format(System.currentTimeMillis());
            if(nowtime.substring(0,10).equals(propertystockchecktime.substring(0,10))){
                stockchecktimetv.setText("上次盘点时间：今天 "+propertystockchecktime.substring(10));
            } else {
                stockchecktimetv.setText("上次盘点时间："+propertystockchecktime);
            }
        } else {
            stockchecktimetv.setText("上次盘点时间："+propertystockchecktime);
        }


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
                if(((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getCansale()!=1) continue;

                map = new HashMap<>();
                map.put("trackno","轨道："+(i+1)+k);
                map.put("trackgoodscode",((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getGoodscode());
                map.put("trackgoodsname",((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getGoodsname());
                map.put("xitongnum",""+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getNumnow());
                map.put("realnum",""+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getNumnow());
                map.put("batch",""+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getBatchinfo());
                map.put("endday",""+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getEndday());

                map.put("addsubnum","");
                arrayList.add(map);
            }
        }


        // 图片文件读取
        for(int i=0;i<arrayList.size();i++){
            String code = arrayList.get(i).get("trackgoodscode");
            if(goodspng.get(code) == null){
                File file = new File(Environment.getExternalStorageDirectory() + "/goodspng/" + code + ".png");
                if (!file.exists()) {
                    LogUtils.e("没有找到文件：" + Environment.getExternalStorageDirectory() + "/goodspng/" + code + ".png");
                } else {
                    goodspng.put(code, BitmapUtil.getBitmapFromFile(file, 90, 110));
                }
            }
        }

        mListView = findViewById(R.id.listview);
        myListAdapter = new MyListAdapter(this,R.layout.item_stockcheck);
        mListView.setAdapter(myListAdapter);
    }

    private boolean isChange(){
        boolean ischange = false;
        for(int i=0;i<arrayList.size();i++) {
            int xitongnum = Integer.parseInt(arrayList.get(i).get("xitongnum"));
            int realnum = Integer.parseInt(arrayList.get(i).get("realnum"));
            if (xitongnum != realnum) {
                ischange = true;
                break;
            }
        }
        return ischange;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:

                if(!isChange()){
                    AlertDialog okad = new AlertDialog.Builder(StockCheckActivity.this)
                            .setTitle("提示消息")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage("实际库存没有做任何变更，你确定要保存盘点结果吗？")
                            .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(mProgressDialog == null) {
                                        mProgressDialog = ProgressDialog.createLoadingDialog(StockCheckActivity.this, "正在进行盘点结果保存,请稍候...");
                                    }
                                    //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
                                    mProgressDialog.show();

                                    lp.saveStockCheck(arrayList);
                                }
                            })
                            .setNegativeButton("返回",  new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create();
                    okad.setCancelable(false);
                    okad.show();
                } else {
                    if (mProgressDialog == null) {
                        mProgressDialog = ProgressDialog.createLoadingDialog(StockCheckActivity.this, "正在进行盘点结果保存,请稍候...");
                    }
                    //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
                    mProgressDialog.show();

                    lp.saveStockCheck(arrayList);
                }
                break;

            case R.id.btn_back:

                if(isChange()){
                    AlertDialog okad = new AlertDialog.Builder(StockCheckActivity.this)
                            .setTitle("提示消息")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage("实际库存已做过变更了，你需要保存吗？")
                            .setPositiveButton("不保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 关闭presenter的所有线程，然后跳转
                                    lp.setStopThread();
                                    long tgnow = System.currentTimeMillis();
                                    while(tg.activeCount()>0){
                                        for(int i=0;i<1000;i++);
                                        if(System.currentTimeMillis() - tgnow >  3000) break;
                                    }
                                    tg.destroy();

                                    Intent toMenu = new Intent(StockCheckActivity.this, MenuActivity.class);
                                    startActivity(toMenu);
                                    StockCheckActivity.this.finish();
                                }
                            })
                            .setNegativeButton("保存",  new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create();
                    okad.setCancelable(false);
                    okad.show();

                } else {
                    // 关闭presenter的所有线程，然后跳转
                    lp.setStopThread();
                    long tgnow = System.currentTimeMillis();
                    while(tg.activeCount()>0){
                        for(int i=0;i<1000;i++);
                        if(System.currentTimeMillis() - tgnow >  3000) break;
                    }
                    tg.destroy();

                    Intent toMenu = new Intent(StockCheckActivity.this, MenuActivity.class);
                    startActivity(toMenu);
                    StockCheckActivity.this.finish();
                }


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

            ImageView im = convertView.findViewById(R.id.imageView);
            TextView trackno =  convertView.findViewById(R.id.trackno);
            TextView trackgoodscode =  convertView.findViewById(R.id.trackgoodscode);
            TextView trackgoodsname =  convertView.findViewById(R.id.trackgoodsname);
            TextView xitongnum =  convertView.findViewById(R.id.xitongnum);
            TextView realnum =  convertView.findViewById(R.id.realnum);
            TextView batchenddaytxt = convertView.findViewById(R.id.batchenddaytxt);
            TextView addsubnum =  convertView.findViewById(R.id.addsubnum);
            Button addbtn = convertView.findViewById(R.id.addbtn);
            addbtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Map<String,String> nowhm = arrayList.get(position);
                    String trackno = nowhm.get("trackno");
                    int realnow = Integer.parseInt(nowhm.get("realnum"));
                    int addsubnumdisp = Integer.parseInt(nowhm.get("addsubnum").length()==0?"0":nowhm.get("addsubnum").replace("+",""));
//                    if(trackno.contains("轨道：")){
                        int inttrackno = Integer.parseInt(trackno.replace("轨道：",""));
                        int max = ((MyApp)getApplication()).getTrackMainGeneral()[inttrackno-10].getNummax();
                        if(max > realnow){
                            nowhm.put("realnum",""+(realnow+1));

                            int newaddsubnum = addsubnumdisp + 1;
                            if(newaddsubnum > 0){
                                nowhm.put("addsubnum","+"+newaddsubnum);
                            } else if(newaddsubnum < 0){
                                nowhm.put("addsubnum",""+newaddsubnum);
                            } else {
                                nowhm.put("addsubnum","");
                            }

                            dispHandler.sendEmptyMessage(dispHandler_addone);
                            myListAdapter.notifyDataSetChanged();
                        }
//                    }
                }
            });

            Button subbtn = convertView.findViewById(R.id.subbtn);
            subbtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Map<String,String> nowhm = arrayList.get(position);
                    String trackno = nowhm.get("trackno");
                    int realnow = Integer.parseInt(nowhm.get("realnum"));
                    int addsubnumdisp = Integer.parseInt(nowhm.get("addsubnum").length()==0?"0":nowhm.get("addsubnum").replace("+",""));
                    if(realnow > 0){
                        nowhm.put("realnum",""+(realnow - 1));

                        int newaddsubnum = addsubnumdisp - 1;
                        if(newaddsubnum > 0){
                            nowhm.put("addsubnum","+"+newaddsubnum);
                        } else if(newaddsubnum < 0){
                            nowhm.put("addsubnum",""+newaddsubnum);
                        } else {
                            nowhm.put("addsubnum","");
                        }

                        dispHandler.sendEmptyMessage(dispHandler_subone);
                        myListAdapter.notifyDataSetChanged();
                    }
                }
            });

            int colorPos = position % colors.length;
            convertView.setBackgroundColor(colors[colorPos]);

            Map<String,String> nowmap = arrayList.get(position);
            trackno.setText(nowmap.get("trackno"));
            trackgoodscode.setText(nowmap.get("trackgoodscode"));
            trackgoodsname.setText(nowmap.get("trackgoodsname"));
            xitongnum.setText(nowmap.get("xitongnum"));
            realnum.setText(nowmap.get("realnum"));
            addsubnum.setText(nowmap.get("addsubnum"));

            batchenddaytxt.setText((nowmap.get("batch").length()>0?"批次："+nowmap.get("batch"):"") + (nowmap.get("endday").length()>0?"      到期："+nowmap.get("endday"):""));

            if(goodspng.get(nowmap.get("trackgoodscode")) != null) {
                im.setImageBitmap(goodspng.get(nowmap.get("trackgoodscode")));
            }

            return convertView;
        }
    }

    @Override
    public void saveError(String errormsg){
        Message msg = dispHandler.obtainMessage(dispHandler_rtnSaveError);
        msg.obj = errormsg;
        dispHandler.sendMessage(msg);
    }

    @Override
    public void saveOK(){
        dispHandler.sendEmptyMessage(dispHandler_rtnSaveOK);
    }


    /**
     * Handle线程，显示处理结果
     */
    Handler dispHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // 处理消息
            switch (msg.what) {
                case dispHandler_rtnSaveOK:
                    // 保存到配置文件
                    SharedPreferences sp =
                            StockCheckActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();//获取编辑器
                    String nowtime = sdfl.format(System.currentTimeMillis());
                    editor.putString("stockchecktime", nowtime);
                    editor.apply();//提交修改
                    ((MyApp)getApplication()).setStockchecktime(nowtime);

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    AlertDialog okad = new AlertDialog.Builder(StockCheckActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("提示")
                            .setMessage("盘点结果保存成功")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 关闭presenter的所有线程，然后跳转
                                    lp.setStopThread();
                                    long tgnow = System.currentTimeMillis();
                                    while(tg.activeCount()>0){
                                        for(int i=0;i<1000;i++);
                                        if(System.currentTimeMillis() - tgnow >  3000) break;
                                    }
                                    tg.destroy();

                                    Intent toMenu = new Intent(StockCheckActivity.this, MenuActivity.class);
                                    startActivity(toMenu);
                                    StockCheckActivity.this.finish();
                                }
                            })
                            .create();
                    okad.setCancelable(false);
                    okad.show();
                    break;

                case dispHandler_rtnSaveError:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    AlertDialog al = new AlertDialog.Builder(StockCheckActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("错误信息：" + msg.obj)
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                    break;

                case dispHandler_addone:
                    totalchange ++;
                    if(totalchange == 0){
                        changenumtv.setText("");
                    } else if(totalchange > 0){
                        changenumtv.setText("+"+totalchange);
                    } else {
                        changenumtv.setText(""+totalchange);
                    }
                    break;

                case dispHandler_subone:
                    totalchange --;
                    if(totalchange == 0){
                        changenumtv.setText("");
                    } else if(totalchange > 0){
                        changenumtv.setText("+"+totalchange);
                    } else {
                        changenumtv.setText(""+totalchange);
                    }
                    break;


                default:
                    break;
            }
            return false;
        }
    });


}
