package com.zhongyagroup.school;

import android.app.Activity;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhongyagroup.school.app.MyApp;
import com.zhongyagroup.school.util.BitmapUtil;
import com.zhongyagroup.school.util.Gmethod;
import com.zhongyagroup.school.util.LogUtils;
import com.zhongyagroup.school.util.MD5;
import com.zhongyagroup.school.util.MyHttp;
import com.zhongyagroup.school.util.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StockSupplyActivity extends Activity implements View.OnClickListener{
    private final static SimpleDateFormat sdfl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Dialog mProgressDialog;

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

    private String paratype;

    private String[] goodscodenameprice = new String[]{};

    private ArrayList<String> goods_code_name_list = new ArrayList<>();

    private MyApp myApp;

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
        setContentView(R.layout.activity_stock_supply);

        myApp = (MyApp)getApplication();


        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        Button btnsubmit = findViewById(R.id.btn_submit);
        btnsubmit.setOnClickListener(this);

        Button btnmax = findViewById(R.id.btn_max);
        btnmax.setOnClickListener(this);


        changenumtv = findViewById(R.id.changenumtv);
        changenumtv.setText("");

        // 获取参数
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        paratype = b.getString("param");
        if(paratype.equals("readonly")){
            // 只读打开的话
            btnsubmit.setVisibility(View.INVISIBLE);
            btnmax.setVisibility(View.INVISIBLE);
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

                map = new HashMap<>();
                map.put("trackno","轨道："+(i+1)+k);
                map.put("trackgoodscode",((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getGoodscode());
                map.put("trackgoodsname",((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getGoodsname());
                map.put("stocknum",""+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getNumnow());
                map.put("supplynum",""+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getNumnow());
                map.put("nummax",""+((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getNummax());
                map.put("batch","");
                map.put("endday","");

                map.put("paycash",""+ Gmethod.tranFenToFloat2(((MyApp)getApplication()).getTrackMainGeneral()[i*10+k].getPayprice()));

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
        myListAdapter = new MyListAdapter(this,R.layout.item_stocksupply);
        mListView.setAdapter(myListAdapter);

        // 从数据库的商品表中读取所有商品
        new Thread(new Runnable() {
            @Override
            public void run() {

                //获取DB中的值
                try {
                    SQLiteDatabase db = openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                    Cursor c = db.rawQuery("SELECT goodscode,goodsname,payprice FROM goodsname ORDER BY goodsname COLLATE LOCALIZED ASC", null);

                    while (c.moveToNext())
                    {
                        String str = c.getString(c.getColumnIndex("goodscode"))+" "+c.getString(c.getColumnIndex("goodsname"));
                        goods_code_name_list.add(str+","+c.getInt(c.getColumnIndex("payprice")));
                    }

                    c.close();

                    if (db.isOpen()) {
                        db.close();
                    }
                } catch (SQLException e) {
                    Log.e("StockSupply", " 执行SQL命令" + e.toString());
                }
                Object[] arr = goods_code_name_list.toArray();
                goodscodenameprice = new String[goods_code_name_list.size()];
                for (int i = 0; i < arr.length; i++) {
                    String e = (String) arr[i];
                    goodscodenameprice[i] = e.split(",")[0];
                }
            }
        }).start();
    }

    private boolean isChange(){
        boolean ischange = false;
        for(int i=0;i<arrayList.size();i++) {
            int supplynum = Integer.parseInt(arrayList.get(i).get("supplynum"));
            int stocknum = Integer.parseInt(arrayList.get(i).get("stocknum"));
            if (supplynum != stocknum) {
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
                    AlertDialog al = new AlertDialog.Builder(StockSupplyActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("补货后库存没有变化，不能保存")
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                } else {
                    if (mProgressDialog == null) {
                        mProgressDialog = ProgressDialog.createLoadingDialog(StockSupplyActivity.this, "正在进行补货结果保存,请稍候...");
                    }
                    //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
                    mProgressDialog.show();

                    //lp.saveStockSupply(arrayList);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Map<String,String>> newList = new ArrayList<>();

                            // 数据发送到服务器
                            for(int i=0;i<arrayList.size();i++){
                                Map<String,String> hm = arrayList.get(i);
                                int changelocal = Integer.parseInt(hm.get("supplynum")) - Integer.parseInt(hm.get("stocknum"));
                                if (changelocal != 0) {
                                    Gmethod.doHm(newList,hm.get("trackno").replace("副柜 > 轨道：","1").replace("轨道：","").replace("货道：",""),
                                            hm.get("trackgoodscode"),hm.get("trackgoodsname"),hm.get("batch"),hm.get("endday"),changelocal);
                                }

                            }

                            String data = "";
                            for(int i=0;i<newList.size();i++){
                                Map<String,String> hm = newList.get(i);
                                data = data + hm.get("trackgoodscode") + "," +
                                        hm.get("trackgoodsname") + "," +
                                        hm.get("batch") + "," +
                                        hm.get("endday") + "," +
                                        hm.get("change") + "," +
                                        hm.get("detail") + ";";
                            }
                            if(data.length() > 0){
                                data = data.substring(0,data.length()-1);
                            }

                            String data_encode = "";
                            try {
                                data_encode = URLEncoder.encode(data.replace(" ","N"),"UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            long timestamp = System.currentTimeMillis();
                            String str = "data="+data_encode + "&macid="+myApp.getMainMacID()  +"&supplycode=" + myApp.getTempSupplyCode() + "&userid=" + 0 +
                                    "&timestamp="+timestamp + "&accesskey=" + myApp.getAccessKey();
                            String md5 = MD5.GetMD5Code(str);

                            String rtnstr = (new MyHttp()).post(myApp.getServerurl()+"/sendsupplyresult",
                                    "macid="+myApp.getMainMacID() + "&data="+data.replace(" ","N") +"&supplycode=" + myApp.getTempSupplyCode() + "&userid=" + 0 +
                                            "&timestamp="+timestamp + "&md5=" + md5);

                            if(rtnstr.length()==0){
                                //iView.saveError("联网失败");

                                Message msg = dispHandler.obtainMessage(dispHandler_rtnSaveError);
                                msg.obj = "联网失败";
                                dispHandler.sendMessage(msg);

                            } else {
                                int code = 2;
                                String msg = "解析返回值出错啦";
                                try {
                                    JSONObject soapJson = new JSONObject(rtnstr);
                                    code = soapJson.getInt("code");
                                    msg = soapJson.getString("msg");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if(code == 0){
                                    // 数据保存到本机数据库

                                    try {
                                        SQLiteDatabase db = myApp.openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                                        for(int i=0;i<arrayList.size();i++) {
                                            Map<String, String> hm = arrayList.get(i);
                                            String trackno = hm.get("trackno");
                                            int supplynum = Integer.parseInt(hm.get("supplynum"));
                                            int changelocal = supplynum - Integer.parseInt(hm.get("stocknum"));
                                            if (changelocal != 0) {

                                                int inttrackno = Integer.parseInt(trackno.replace("轨道：", ""));

                                                db.execSQL("update trackmaingeneral set numnow=" + supplynum + " where id=" + inttrackno);

                                                myApp.getTrackMainGeneral()[inttrackno - 10].setNumnow(supplynum);

                                            }
                                        }
                                        db.close();

                                        //iView.saveOK();
                                        dispHandler.sendEmptyMessage(dispHandler_rtnSaveOK);

                                    } catch (SQLException e) {
                                        e.printStackTrace();

                                        //iView.saveError(e.getMessage());
                                        Message msg2 = dispHandler.obtainMessage(dispHandler_rtnSaveError);
                                        msg2.obj = e.getMessage();
                                        dispHandler.sendMessage(msg2);
                                    }

                                } else {
                                    //iView.saveError(msg);
                                    Message msg2 = dispHandler.obtainMessage(dispHandler_rtnSaveError);
                                    msg2.obj = msg;
                                    dispHandler.sendMessage(msg2);
                                }
                            }
                        }
                    },"补货结果保存线程").start();

                }
                break;

            case R.id.btn_back:

                if(isChange()){
                    AlertDialog okad = new AlertDialog.Builder(StockSupplyActivity.this)
                            .setTitle("提示消息")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage("实际库存已做过变更了，你需要保存吗？")
                            .setPositiveButton("不保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent toMenu = new Intent(StockSupplyActivity.this, MenuActivity.class);
                                    startActivity(toMenu);
                                    StockSupplyActivity.this.finish();
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

                    Intent toMenu = new Intent(StockSupplyActivity.this, MenuActivity.class);
                    startActivity(toMenu);
                    StockSupplyActivity.this.finish();
                }

                break;

            case R.id.btn_max:
                totalchange = 0;
                for(int i=0;i<arrayList.size();i++){
                    Map<String,String> hm = arrayList.get(i);
                    int changelocal = Integer.parseInt(hm.get("nummax")) - Integer.parseInt(hm.get("stocknum"));
                    hm.put("supplynum",hm.get("nummax"));
                    if(changelocal > 0){
                        hm.put("addsubnum","+"+changelocal);
                    } else if(changelocal < 0){
                        hm.put("addsubnum",""+changelocal);
                    } else {
                        hm.put("addsubnum","");
                    }
                    totalchange = totalchange + changelocal;
                }
                if(totalchange == 0){
                    changenumtv.setText("");
                } else if(totalchange > 0){
                    changenumtv.setText("+"+totalchange);
                } else {
                    changenumtv.setText(""+totalchange);
                }
                myListAdapter.notifyDataSetChanged();

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
            TextView stocknum =  convertView.findViewById(R.id.stocknum);
            TextView supplytxt = convertView.findViewById(R.id.supplytxt);
            TextView supplynum =  convertView.findViewById(R.id.supplynum);
            TextView addsubnum =  convertView.findViewById(R.id.addsubnum);
            TextView cashprice = convertView.findViewById(R.id.cashprice);


            Button addbtn = convertView.findViewById(R.id.addbtn);
            addbtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Map<String,String> nowhm = arrayList.get(position);
                    String trackno = nowhm.get("trackno");
                    int supplynum = Integer.parseInt(nowhm.get("supplynum"));
                    int addsubnumdisp = Integer.parseInt(nowhm.get("addsubnum").length()==0?"0":nowhm.get("addsubnum").replace("+",""));

                    int inttrackno = Integer.parseInt(trackno.replace("轨道：",""));
                    int max = ((MyApp)getApplication()).getTrackMainGeneral()[inttrackno-10].getNummax();
                    if(max > supplynum){
                        nowhm.put("supplynum",""+(supplynum+1));

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

                }
            });

            Button subbtn = convertView.findViewById(R.id.subbtn);
            subbtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Map<String,String> nowhm = arrayList.get(position);
                    String trackno = nowhm.get("trackno");
                    int supplynum = Integer.parseInt(nowhm.get("supplynum"));
                    int addsubnumdisp = Integer.parseInt(nowhm.get("addsubnum").length()==0?"0":nowhm.get("addsubnum").replace("+",""));
                    if(supplynum > 0){
                        nowhm.put("supplynum",""+(supplynum - 1));

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

            Button changebtn = convertView.findViewById(R.id.changegoodsbtn);
            // goodscodenameprice
            changebtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Map<String,String> nowhm = arrayList.get(position);
                    final String nowcode_name = nowhm.get("trackgoodscode")+" "+nowhm.get("trackgoodsname");

                    int dispinit = 0;
                    for(int k=0;k<goodscodenameprice.length;k++){
                        if(nowcode_name.equals(goodscodenameprice[k])){
                            dispinit = k;
                            break;
                        }
                    }

                    AlertDialog al = new AlertDialog.Builder(StockSupplyActivity.this)
                            .setTitle("请选择新的商品名称（原来："+nowcode_name+")")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setSingleChoiceItems(goodscodenameprice, dispinit,
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int which) {

                                            int selectk = which;

                                            String newcodename = goodscodenameprice[selectk];

                                            LogUtils.i("原来的："+nowcode_name+";新的："+newcodename);

                                            if(!nowcode_name.equals(newcodename)) {
                                                // 有变化
                                                String newcode = newcodename.split(" ")[0];

                                                for(String gnp:goods_code_name_list){
                                                    if(newcode.equals(gnp.split(",")[0].split(" ")[0])){
                                                        final String trackgoodscode = gnp.split(",")[0].split(" ")[0];
                                                        final String trackgoodsname = gnp.split(",")[0].split(" ")[1];
                                                        final String batch = "";
                                                        final String endday = "";
                                                        final String paycash = gnp.split(",")[1];

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

                                                        final String tracknostr = arrayList.get(position).get("trackno").replace("轨道：", "");

                                                        for (int k = 0; k < kmax; k++) {
                                                            arrayList.get(position+k).put("trackgoodscode", trackgoodscode);
                                                            arrayList.get(position+k).put("trackgoodsname", trackgoodsname);
                                                            arrayList.get(position+k).put("batch", batch);
                                                            arrayList.get(position+k).put("endday", endday);
                                                            arrayList.get(position+k).put("paycash", Gmethod.tranFenToFloat2(Integer.parseInt(paycash)));
                                                            final int f_k = k;
                                                            new Thread() {
                                                                public void run() {
                                                                    try {
                                                                        int paycashfen = Integer.parseInt(paycash);

                                                                        SQLiteDatabase db = ((MyApp) getApplication()).openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                                                                        db.execSQL("update trackmaingeneral set goodscode='" + trackgoodscode + "',mingcheng='" + trackgoodsname
                                                                                + "',payprice=" + paycashfen + " where id=" + (Integer.parseInt(tracknostr)+f_k));
                                                                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(tracknostr)+f_k - 10].setGoodscode(trackgoodscode);
                                                                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(tracknostr)+f_k - 10].setGoodsname(trackgoodsname);
                                                                        ((MyApp) getApplication()).getTrackMainGeneral()[Integer.parseInt(tracknostr)+f_k - 10].setPayprice(paycashfen);


                                                                        db.close();

                                                                    } catch (SQLException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }.start();
                                                        }

                                                        if(goodspng.get(trackgoodscode) == null){
                                                            File file = new File(Environment.getExternalStorageDirectory() + "/goodspng/" + trackgoodscode + ".png");
                                                            if (!file.exists()) {
                                                                LogUtils.e("没有找到文件：" + Environment.getExternalStorageDirectory() + "/goodspng/" + trackgoodscode + ".png");
                                                            } else {
                                                                goodspng.put(trackgoodscode, BitmapUtil.getBitmapFromFile(file, 90, 110));
                                                            }
                                                        }

                                                        break;
                                                    }
                                                }

                                                myListAdapter.notifyDataSetChanged();

                                            }

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




            int colorPos = position % colors.length;
            convertView.setBackgroundColor(colors[colorPos]);

            Map<String,String> nowmap = arrayList.get(position);
            trackno.setText(nowmap.get("trackno"));
            trackgoodscode.setText(nowmap.get("trackgoodscode"));
            trackgoodsname.setText(nowmap.get("trackgoodsname"));
            stocknum.setText(nowmap.get("stocknum"));
            supplynum.setText(nowmap.get("supplynum"));
            addsubnum.setText(nowmap.get("addsubnum"));
            cashprice.setText("¥ "+nowmap.get("paycash"));

            if(goodspng.get(nowmap.get("trackgoodscode")) != null) {
                im.setImageBitmap(goodspng.get(nowmap.get("trackgoodscode")));
            } else {
                im.setImageResource(R.drawable.nogoods);
            }

            if(paratype.equals("readonly")){
                addbtn.setVisibility(View.INVISIBLE);
                subbtn.setVisibility(View.INVISIBLE);
                supplytxt.setVisibility(View.INVISIBLE);
                supplynum.setVisibility(View.INVISIBLE);
            } else {
                addbtn.setVisibility(View.VISIBLE);
                subbtn.setVisibility(View.VISIBLE);
                supplytxt.setVisibility(View.VISIBLE);
                supplynum.setVisibility(View.VISIBLE);
            }

            if(position%10 == 0){
                changebtn.setVisibility(View.VISIBLE);
            } else {
                changebtn.setVisibility(View.INVISIBLE);
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
                case dispHandler_rtnSaveOK:
                    // 保存到配置文件
                    SharedPreferences sp =
                            StockSupplyActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();//获取编辑器
                    String nowtime = sdfl.format(System.currentTimeMillis());
                    editor.putString("stocksupplytime", nowtime);
                    editor.apply();//提交修改

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    AlertDialog okad = new AlertDialog.Builder(StockSupplyActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("提示")
                            .setMessage("补货结果保存成功")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent toMenu = new Intent(StockSupplyActivity.this, MenuActivity.class);
                                    startActivity(toMenu);
                                    StockSupplyActivity.this.finish();
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

                    AlertDialog al = new AlertDialog.Builder(StockSupplyActivity.this)
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
