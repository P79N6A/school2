package com.zhongyagroup.school;

import android.app.Activity;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import android.graphics.Color;
import android.widget.Toast;

import com.zhongyagroup.school.app.MyApp;
import com.zhongyagroup.school.util.BitmapUtil;
import com.zhongyagroup.school.util.Gmethod;
import com.zhongyagroup.school.util.MD5;
import com.zhongyagroup.school.util.MyHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoodspriceActivity extends Activity {
    private static final String TAG = "GoodspriceActivity";

    /** 返回 */
    private Button btnReturn;
    /** 重新取得商品名称和图片 */
    private Button getaginbtn;
    /** 删除 */
    private Button delbtn;

    private MyApp myApp;

    protected static final int xian_shi_error = 0x418;
    protected static final int xian_shi_disp_info = 0x419; // 显示处理过程中的信息

    protected static final int xian_shi_goodsname = 0x420;
    protected static final int xian_shi_get_goodsname = 0x421;
    protected static final int xian_shi_get_goodsname_zero = 0x422;
    protected static final int xian_shi_del_ok = 0x423;
    protected static final int xian_shi_filter = 0x424;

    SimpleDateFormat formathms = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    /* 显示的数据列表 */
    private String[] codeArray;
    private String[] goodsnameArray;
    private String[] goodsnameArrayFirst;
    private int[] priceArray;
    //private Bitmap[] bmArray;
    private HashMap<String,Bitmap> bmHashMap = new HashMap<>();

    private ListView listView;
    private ListAdapter listAdapter;
    // 显示的列表
//    ArrayList<Map<String,String>> listAll = null;
    ArrayList<Map<String,String>> listFilter = null;

    // --------------------------------------------------- first ---------------------------
    private ListView listViewFirst;
    private ListAdapterFirst listAdapterFirst;
    // 显示的列表
    ArrayList<Map<String,String>> listFirst = null;

    private TextView goodsnum;

    private TextView didinfo;

    private ProgressDialog mProgressDialog;

    private String firstName = "(全)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goodsprice);

        myApp = (MyApp)getApplication();

        btnReturn = (Button) findViewById(R.id.btn_back);
        // 返回到主菜单
        btnReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent toMenu = new Intent(GoodspriceActivity.this, MenuActivity.class);
                startActivity(toMenu);

                GoodspriceActivity.this.finish();
            }
        });

        getaginbtn = (Button) findViewById(R.id.getagain);


        delbtn = (Button) findViewById(R.id.btndelete);
        delbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(GoodspriceActivity.this).setTitle("错误提示")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("你确定要删除吗？")
                        .setPositiveButton("确定", new onClickListernerSubmit())
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //啥也不处理
                            }
                        })
                        .create()
                        .show();


            }
        });

        didinfo = (TextView)findViewById(R.id.didinfo);

        goodsnum =  (TextView)findViewById(R.id.localgoodsnum);

        listView = (ListView)findViewById(R.id.list);

        listViewFirst = (ListView)findViewById(R.id.listleft);


        goodsnum.setText("");
        didinfo.setText("正在读取本地数据......");

        mProgressDialog =  ProgressDialog.show(GoodspriceActivity.this, "读取本地数据...", "请稍后...", true, false);
        getaginbtn.setVisibility(View.GONE);
        delbtn.setVisibility(View.GONE);

        // 取得现在的商品名称
        Thread getGoodsNameThread = new GetLocalGoodsNameThread();
        getGoodsNameThread.setPriority(Thread.MIN_PRIORITY);
        getGoodsNameThread.setName("getGoodsNameThread线程：" + formathms.format(new Date(System.currentTimeMillis())));
        getGoodsNameThread.start();

        // 下载所有货品名称和图片
        getaginbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 判断是否有文件夹存在
                File file = new File(Environment.getExternalStorageDirectory() + "/goodspng/");
                if(!file.exists()){
                    boolean irtn=  file.mkdirs();
                    Log.i(TAG,  "文件夹" + Environment.getExternalStorageDirectory() + "/goodspng/" + "的mkdir结果:" + irtn);
                }

                didinfo.setText("联网取得中...");
                getaginbtn.setVisibility(View.GONE);
                btnReturn.setVisibility(View.GONE);
                delbtn.setVisibility(View.GONE);

                mProgressDialog =  ProgressDialog.show(GoodspriceActivity.this, "联网读取商品名称和下载商品图片...", "请稍后...", true, false);

                Thread getAllGoodsNamethread = new GetAllGoodsNamethread();
                getAllGoodsNamethread.setPriority(Thread.MIN_PRIORITY);
                getAllGoodsNamethread.setName("getAllGoodsNamethread线程：" + formathms.format(new Date(System.currentTimeMillis())));
                getAllGoodsNamethread.start();

            }
        });

    }
    @Override
    public void onBackPressed() {
        Log.v(TAG,"onBackPressed()");

        // 返回按钮的处理，不让程序退出的话，可以注解下面这行代码
        //super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        Log.v(TAG,"onDestroy()");

        if(!bmHashMap.isEmpty()) {
            Iterator iter = bmHashMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Bitmap val = (Bitmap) entry.getValue();

                if (val != null && !val.isRecycled()) {
                    val.recycle();
                    val = null;
                }
            }
            bmHashMap = new HashMap<>();
        }

        super.onDestroy();

    }

    class onClickListernerSubmit implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            btnReturn.setVisibility(View.GONE);
            getaginbtn.setVisibility(View.GONE);
            delbtn.setVisibility(View.GONE);

            new Thread(){
                public void run(){

                    try {
                        SQLiteDatabase db = openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                        db.execSQL("delete from goodsname");

                        db.close();

                        delFolder(Environment.getExternalStorageDirectory() + "/goodspng");

                        Message msg = msgHandler.obtainMessage();
                        msg.what = xian_shi_del_ok;
                        msgHandler.sendMessage(msg);

                    } catch (SQLException e) {
                        Log.e(TAG, " 执行SQL命令" + e.toString());
                    }
                }
            }.start();
        }
    }


    // 取得所有货品名称（从数据库）
    class GetLocalGoodsNameThread extends Thread {
        public void run() {
            ArrayList<String> codelist = new ArrayList<>();
            ArrayList<String> goodsnamelist = new ArrayList<>();
            ArrayList<String> goodsnamelistfirst = new ArrayList<>();

            ArrayList<Integer> pricelist = new ArrayList<>();

            if(!bmHashMap.isEmpty()) {
                Iterator iter = bmHashMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object key = entry.getKey();
                    Bitmap val = (Bitmap) entry.getValue();

                    if (val != null && !val.isRecycled()) {
                        val.recycle();
                        val = null;
                    }
                }
                bmHashMap = new HashMap<>();
            }
            //提醒系统及时回收
            System.gc();

            //获取DB中的值
            try {
                SQLiteDatabase db = openOrCreateDatabase("vmdata.db", Context.MODE_PRIVATE, null);

                Cursor c = db.rawQuery("SELECT goodscode,goodsname,payprice FROM goodsname ORDER BY goodsname COLLATE LOCALIZED ASC", null);

                String firstname = "";
                while (c.moveToNext())
                {
                    codelist.add(c.getString(c.getColumnIndex("goodscode")));
                    goodsnamelist.add(c.getString(c.getColumnIndex("goodsname")));

                    if(c.getString(c.getColumnIndex("goodsname")).length()>0){
                        if(!c.getString(c.getColumnIndex("goodsname")).substring(0,1).equals(firstname)){
                            firstname = c.getString(c.getColumnIndex("goodsname")).substring(0,1);
                            goodsnamelistfirst.add(firstname);
                        }
                    }

                    pricelist.add(c.getInt(c.getColumnIndex("payprice")));
                }

                c.close();

                if (db.isOpen()) {
                    db.close();
                }

                //File file = new File(Environment.getExternalStorageDirectory() + "/goodspng/");
                if(goodsnamelist.size() > 0){
                    //codefiveArray = new String[goodsnamelist.size()];
                    codeArray = new String[goodsnamelist.size()];
                    goodsnameArray  = new String[goodsnamelist.size()];
                    priceArray  = new int[goodsnamelist.size()];


                    Log.v(TAG,"bmArray--");
                    //bmArray = new Bitmap[goodsnamelist.size()];
                    Log.v(TAG,"bmArray--------");
                    for(int i=0;i<goodsnamelist.size();i++){
                        codeArray[i] = codelist.get(i);
                        goodsnameArray[i] = goodsnamelist.get(i);
                        priceArray[i] = pricelist.get(i);


                        if(bmHashMap.get(codelist.get(i)) == null){
                            Bitmap bitmap = null;
                            try {
                                bitmap = BitmapUtil.getBitmapFromFile(new File(Environment.getExternalStorageDirectory() + "/goodspng/" + codelist.get(i)+".png"), 90, 110);// 直接从文件中打开
                            } catch (Exception e){
                                e.printStackTrace();
                                bitmap = null;
                            }
                            bmHashMap.put(codelist.get(i),bitmap);
                        }


                    }

                    goodsnameArrayFirst = new String[goodsnamelistfirst.size()+1];
                    goodsnameArrayFirst[0] = "(全)";
                    for(int i=1;i<goodsnamelistfirst.size()+1;i++){
                        goodsnameArrayFirst[i] = goodsnamelistfirst.get(i-1);
                    }

                } else {
                    codeArray = new String[0];
                    goodsnameArray  = new String[0];
                    priceArray  = new int[0];



                    goodsnameArrayFirst = new String[0];

                    //bmArray = new Bitmap[0];
                    bmHashMap = new HashMap<>();
                }

                Log.i(TAG, " GetGoodsNameThread 线程结束!");

                msgHandler.sendEmptyMessage(xian_shi_goodsname);

            } catch (SQLException e) {
                Log.e(TAG, " 执行SQL命令" + e.toString());
            }

        }
    }

    class GetAllGoodsNamethread extends Thread {
        public void run() {
            int goodsnum = 0;
            ArrayList<HashMap<String,Object>> arrayList = new ArrayList<>();

            long timestamp = System.currentTimeMillis();
            String str = "macid="+ ((MyApp)getApplication()).getMainMacID() +
                    "&timestamp="+timestamp + "&accesskey=" + ((MyApp)getApplication()).getAccessKey();
            String md5 = MD5.GetMD5Code(str);
            Log.v("Goodsprice",str + ":"+md5);

            String rtnstr = (new MyHttp()).post(((MyApp)getApplication()).getServerurl()+"/getallgoods",
                    "macid="+((MyApp)getApplication()).getMainMacID() +
                            "&timestamp="+timestamp + "&md5=" + md5);
            if(rtnstr.length() > 0){
                try {
                    JSONObject soapJson = new JSONObject(rtnstr);
                    int code = soapJson.getInt("code");
                    String msgstr = soapJson.getString("msg");
                    if(code != 0) {
                        // 说明返回有错误信息
                        Message msg =  msgHandler.obtainMessage();
                        msg.what = xian_shi_error;
                        msg.obj = "服务器返回："+msgstr;
                        msgHandler.sendMessage(msg);

                        return;
                    } else {
                        // 收到了，保存
                        goodsnum = soapJson.getInt("goodsnum");

                        JSONArray goodpricearray = soapJson.getJSONArray("pngurlarray");
                        for(int i=0;i<goodpricearray.length();i++){
                            JSONObject member = goodpricearray.getJSONObject(i);
                            String goodscode = member.getString("goodscode");
                            String goodsname = member.getString("goodsname");
                            String pngurl = member.getString("pngurl");
                            int pricefen = member.getInt("pricefen");
                            if(pricefen==0) pricefen = 1000;

                            HashMap<String,Object> hm = new HashMap<>();
                            hm.put("goodscode",goodscode);
                            hm.put("goodsname",goodsname);
                            hm.put("pngurl",pngurl);
                            hm.put("pricefen",pricefen);

                            arrayList.add(hm);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    Message msg =  msgHandler.obtainMessage();
                    msg.what = xian_shi_error;
                    msg.obj = "处理返回值时程序异常";
                    msgHandler.sendMessage(msg);

                    return;
                }
            } else {
                Message msg =  msgHandler.obtainMessage();
                msg.what = xian_shi_error;
                msg.obj = "联网失败";
                msgHandler.sendMessage(msg);

                return;
            }

            // 开始下载
            SQLiteDatabase db = openOrCreateDatabase("vmdata.db",Context.MODE_PRIVATE, null);
            File file;

            // 对返回的数据不处理（ ok:表示插入成功  ng:表示插入失败  ex:表示系统异常 )
            for(int i=0;i<arrayList.size();i++) {
                Message msg = msgHandler.obtainMessage();
                msg.what = xian_shi_disp_info;
                msg.obj = "已取到商品名称和图片地址信息，共" + goodsnum + "个;正在取第" + (i+1) + "个商品信息...";
                msgHandler.sendMessage(msg);


                HashMap<String,Object> hm = arrayList.get(i);
                String goodscode = (String)hm.get("goodscode");
                String goodsname = (String)hm.get("goodsname");
                String pngurl = (String)hm.get("pngurl");
                int pricefen = (Integer)hm.get("pricefen");


                // 如果不存在，那么插入
                Cursor c = db.rawQuery("SELECT count(*) FROM goodsname where goodscode='" + goodscode + "'", null);
                c.moveToNext();
                int count = c.getInt(0);
                c.close();

                if (count > 0) {
                    // 已经存在了
                    db.execSQL("update goodsname set goodsname = '" + goodsname + "' where goodscode='" + goodscode + "'");

                    // 已经存在了,那么先删除,再下载图片
                    file = new File(Environment.getExternalStorageDirectory() + "/goodspng/" + goodscode + ".png");
                    if (file.exists()) {
                        boolean deleteresult = file.delete();
                        Log.i(TAG,  "文件" + Environment.getExternalStorageDirectory() + "/goodspng/" + goodscode + ".png" + "的delete结果:" + deleteresult);
                    }
                    try {
                        URL url = new URL(pngurl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        if (conn.getResponseCode() == 200) {

                            InputStream is = conn.getInputStream();
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            is.close();
                            fos.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    int pricefencash = pricefen;


                    // 查看一下，现在是否在用，在用的话，那就有价格的。

                    for(int k=0;k<70;k++){
                        if(((MyApp)getApplication()).getTrackMainGeneral()[k].getGoodscode().equals(goodscode)){
                            pricefencash = ((MyApp)getApplication()).getTrackMainGeneral()[k].getPayprice();
                            break;
                        }
                    }

                    // 该商品不存在
                    db.execSQL("INSERT INTO goodsname(goodscode,goodsname,payprice) VALUES('"
                            + goodscode + "','" + goodsname + "'," + pricefencash + ")");

                    //下载文件
                    file = new File(Environment.getExternalStorageDirectory() + "/goodspng/" + goodscode + ".png");

                    try {
                        //开始下载
                        // 从网络上获取图片
                        URL url = new URL(pngurl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        if (conn.getResponseCode() == 200) {

                            InputStream is = conn.getInputStream();
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int len ;
                            while ((len = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            is.close();
                            fos.close();

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();

                    }
                }
            }

            db.close();

            Message msg =  msgHandler.obtainMessage();
            msg.what = xian_shi_get_goodsname;
            msgHandler.sendMessage(msg);

        }
    }

    // 取得这个文件的扩张名
    private String getFileName(String url){
        int i = url.indexOf("/");
        if(i == -1){
            return "";
        }

        String extend = url.substring(i + 1);

        while(extend.contains("/")){
            extend = extend.substring(extend.indexOf("/") + 1);
        }

        return extend;
    }


    /**
     * Handle线程，接收线程过来的消息
     */
    Handler msgHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // 处理消息
            switch (msg.what) {

                case xian_shi_goodsname:  // 本地数据读出来后，进行刷新
                    //关闭ProgressDialog
                    mProgressDialog.dismiss();

                    // 显示
                    getaginbtn.setVisibility(View.VISIBLE);
                    delbtn.setVisibility(View.VISIBLE);
                    goodsnum.setText(String.format("现在本地:%s",goodsnameArray.length));
                    didinfo.setText("");

                    Log.i(TAG,"-------------显示开始------------");

                    setListAdapterFirst();

                    //调用显示的函数，把结果显示出来
                    setListAdapter();

                    break;


                case xian_shi_del_ok:
                    btnReturn.setVisibility(View.VISIBLE);
                    getaginbtn.setVisibility(View.VISIBLE);
                    delbtn.setVisibility(View.VISIBLE);

                    goodsnum.setText(String.format("现在本地:0"));

                    //listAll = new ArrayList<>();
                    listFirst = new ArrayList<>();
                    listView.removeAllViewsInLayout();
                    listAdapter = new ListAdapter(GoodspriceActivity.this, listFirst );
                    listView.setAdapter(listAdapter);

                    listFirst = new ArrayList<>();
                    listViewFirst.removeAllViewsInLayout();
                    listAdapterFirst = new ListAdapterFirst(GoodspriceActivity.this, listFirst );
                    listViewFirst.setAdapter(listAdapterFirst);

                    break;



                case xian_shi_error:
                    btnReturn.setVisibility(View.VISIBLE);
                    getaginbtn.setVisibility(View.VISIBLE);
                    delbtn.setVisibility(View.VISIBLE);

                    didinfo.setText(msg.obj.toString());

                    mProgressDialog.dismiss();

                    break;

                case xian_shi_disp_info:

                    didinfo.setText(msg.obj.toString());

                    break;

                case xian_shi_get_goodsname:

                    btnReturn.setVisibility(View.VISIBLE);
                    delbtn.setVisibility(View.VISIBLE);

                    // list需要remove
                    // 显示的列表
                    //listAll = new ArrayList<>();
                    listFilter = new ArrayList<>();
                    listView.removeAllViewsInLayout();
                    listAdapter = new ListAdapter(GoodspriceActivity.this, listFilter );
                    listView.setAdapter(listAdapter);

                    listFirst = new ArrayList<>();
                    listViewFirst.removeAllViewsInLayout();
                    listAdapterFirst = new ListAdapterFirst(GoodspriceActivity.this, listFirst );
                    listViewFirst.setAdapter(listAdapterFirst);


                    Log.i(TAG, "-------------显示开始get------------");
                    // 取得现在的商品名称
                    Thread getGoodsNameThread = new GetLocalGoodsNameThread();
                    getGoodsNameThread.setPriority(Thread.MIN_PRIORITY);
                    getGoodsNameThread.setName("getGoodsNameThread线程：" + formathms.format(new Date(System.currentTimeMillis())));
                    getGoodsNameThread.start();

                    getaginbtn.setVisibility(View.GONE);



                    break;



                case xian_shi_get_goodsname_zero:
                    // 显示
                    didinfo.setText("服务器端没有数据！");
                    btnReturn.setVisibility(View.VISIBLE);
                    getaginbtn.setVisibility(View.VISIBLE);
                    delbtn.setVisibility(View.VISIBLE);

                    mProgressDialog.dismiss();

                    break;

                case xian_shi_filter:

                    listFilter = new ArrayList<>();
                    listView.removeAllViewsInLayout();
                    listAdapter = new ListAdapter(GoodspriceActivity.this, listFilter );
                    listView.setAdapter(listAdapter);

                    //调用显示的函数，把结果显示出来
                    setListAdapter();

                    break;

                default:
                    break;

            }
            return false;
        }
    });


    private void setListAdapter(){
        // 显示的列表
        listFilter = new ArrayList<>();

        // 计算有多少条需要显示的，准备内容
        Map<String,String> map;

        int filteri = 0;
        for(int i=0;i<goodsnameArray.length;i++){
            map = new HashMap<>();

            if(firstName.equals("(全)") || goodsnameArray[i].substring(0,1).equals(firstName)){
                if(filteri<9)
                    map.put("no", "0" + (filteri+1));
                else
                    map.put("no", "" + (filteri+1));

                map.put("code", codeArray[i]);
                map.put("goodsname", goodsnameArray[i]);

                //图片直接用bm数组取得
                map.put("price","" + Gmethod.tranYuanToSimple(Gmethod.tranFenToFloat2(priceArray[i])));

                listFilter.add(map);

                filteri++;
            }
        }

//        for(int i=0;i<listFilter.size();i++){
//            Log.i(TAG,""+listFilter.get(i).get("code")+":"+listFilter.get(i).get("goodsname")+":"+listFilter.get(i).get("pricecash"));
//        }


        listView.removeAllViewsInLayout();
        listAdapter = new ListAdapter(this, listFilter );
        listView.setAdapter( listAdapter );
    }

    public class ListAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<Map<String,String>> localList;
        //这就是adapter关联的List，用来存储数据的ArrayList　要往里传参数吗？　传的也是这个类型啊．呵呵

        public ListAdapter(Context context, ArrayList<Map<String,String>> list ) {
            this.context = context;
            this.localList = list;
        }

        public int getCount() {
            return localList.size();
        }

        public Object getItem(int position) {
            return localList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Map<String,String> map = localList.get(position);
            return new adapterView(this.context, map );
        }

    }

    class adapterView extends LinearLayout {
        //public static final String LOG_TAG = "adapterView";

        public adapterView(Context context, Map<String,String> map ) {
            super( context );

            this.setOrientation(HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 2, 2, 2);

            LinearLayout mLineLayout = new LinearLayout(context);

            TextView notextControl = new TextView( context );
            LayoutParams notextparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            notextparams.setMargins(0, 34, 0, 4);
            notextControl.setTextColor(Color.BLACK);
            notextControl.setTextSize(20);
            notextControl.setText(map.get("no") );
            notextControl.setLayoutParams(notextparams);
            mLineLayout.addView(notextControl);

            ImageView imageView = new ImageView( context );
            LayoutParams viewparams = new LayoutParams(90, 110);//标准:360*440 的 16分之1
            viewparams.setMargins(10, 4, 0, 4);
            imageView.setLayoutParams(viewparams);
            //imageView.setImageBitmap(bmArray[Integer.parseInt(map.get("no"))-1]);
            imageView.setImageBitmap(bmHashMap.get(map.get("code")));
            mLineLayout.addView(imageView);

            TextView codetextControl = new TextView( context );
            LayoutParams codetextparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            codetextparams.setMargins(0, 34, 0, 0);
            codetextControl.setTextColor(Color.BLUE);
            codetextControl.setTextSize(20);
            codetextControl.setText(map.get("code") );
            codetextControl.setLayoutParams(codetextparams);
            mLineLayout.addView(codetextControl);

            TextView nametextControl = new TextView( context );
            LayoutParams nametextparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            nametextparams.setMargins(10, 34, 0, 0);
            nametextControl.setTextColor(Color.BLACK);
            nametextControl.setTextSize(20);
            nametextControl.setText(map.get("goodsname") );
            nametextControl.setLayoutParams(nametextparams);
            mLineLayout.addView(nametextControl);

            TextView pricetextControl = new TextView( context );
            LayoutParams pricetextparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            pricetextparams.setMargins(10, 34, 0, 0);
            pricetextControl.setTextColor(Color.RED);
            pricetextControl.setTextSize(20);
            pricetextControl.setText( "" + map.get("price") );
            pricetextControl.setLayoutParams(pricetextparams);
            mLineLayout.addView(pricetextControl);


            Button pricebtn = new Button( context );
            LayoutParams priceparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            priceparams.setMargins(-40, 70, 0, 6);
            //priceparams.gravity = Gravity.CENTER;
            pricebtn.setText(" 价格 ");
            pricebtn.setTextSize(20);
            pricebtn.setId(Integer.parseInt(map.get("no")));
            pricebtn.setLayoutParams(priceparams);
            pricebtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //do something
                    final int v_id = v.getId();

                    //String title = "请修改" + goodsnameArray[v_id - 1] + "的价格(现金#支付宝价格#微信价格#会员价)(相同时只输一个价格)(机器上有纸硬币器时请注意现金价格的最小单位为5角)";
                    String title = "请修改【" + listFilter.get(v_id - 1).get("goodsname")+ "】的价格";

                    final EditText inputprice = new EditText(GoodspriceActivity.this);

                    final String goodscode = listFilter.get(v_id - 1).get("code");


                    inputprice.setText("" + listFilter.get(v_id - 1).get("price"));


                    inputprice.setMaxEms(24);
                    inputprice.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
                    inputprice.setTextSize(20);
                    inputprice.setKeyListener(new NumberKeyListener(){
                        @Override
                        protected char[] getAcceptedChars(){
                            return new char[]{'1','2','3','4','5','6','7','8','9','0','.'};
                        }
                        @Override
                        public int getInputType(){
                            return android.text.InputType.TYPE_CLASS_PHONE;
                        }
                    });

                    //光标停到最后的位置
                    CharSequence text = inputprice.getText();
                    if (text != null) {
                        Spannable spanText = (Spannable)text;
                        Selection.setSelection(spanText, text.length());
                    }

                    new AlertDialog.Builder(GoodspriceActivity.this)
                            .setTitle(title)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(inputprice)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String inputtext = inputprice.getText().toString();
                                    Log.i(TAG,"nameindex:" + (v_id - 1) + ";原来的价格:" + inputtext);

                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(inputprice.getWindowToken(), 0); //强制隐藏键盘

                                    boolean ispriceok = true;//价格合法？


                                    // 说明没有“#”，那就是一个价格
                                    if(!inputtext.contains(".")){
                                        //表示没有小数点
                                        inputtext =  inputtext + ".00";
                                    }
                                    if(inputtext.indexOf(".") == inputtext.length() - 1){
                                        //最后一位是.
                                        inputtext =  inputtext + "00";
                                    }
                                    if(inputtext.indexOf(".") == inputtext.length() - 2){
                                        //最后第二位是.
                                        inputtext =  inputtext + "0";
                                    }
                                    Log.i(TAG,"规整后的价格(相同价格):" + inputtext);
                                    if(inputtext.length() - inputtext.indexOf(".") > 3 ){
                                        //123.045:7-3=4
                                        ispriceok = false;
                                    } else {
                                        if(Integer.parseInt(inputtext.replace(".","")) == 0){
                                            ispriceok = false;
                                        } else {
                                            for(int k=0;k<priceArray.length;k++){
                                                if(codeArray[k].equals(goodscode)){
                                                    priceArray[k] = Gmethod.tranYuanToFen(inputtext);
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if(ispriceok){
                                        int cashfen = 0;
                                        for(int k=0;k<priceArray.length;k++){
                                            if(codeArray[k].equals(goodscode)){
                                                cashfen = priceArray[k];
                                                break;
                                            }
                                        }

                                        Map<String,String> map = listFilter.get(v_id - 1);
                                        map.put("price","" + Gmethod.tranYuanToSimple(Gmethod.tranFenToFloat2(cashfen)));

                                        //显示出来
                                        listAdapter.notifyDataSetChanged();


                                        didinfo.setText("处理中...");
                                        // 启动一个线程来进行处理
                                        Thread setPriceThread = new SetPriceThread(goodscode,cashfen);
                                        setPriceThread.setPriority(Thread.MIN_PRIORITY);
                                        setPriceThread.setName("setPriceThread线程：" + formathms.format(new Date(System.currentTimeMillis())));
                                        setPriceThread.start();

                                    } else {
                                        Log.i(TAG, "您输入的价格有误:" + inputtext);

                                        Toast.makeText(GoodspriceActivity.this, "您输入的价格有误！应该为(3)或(3.0#3.01#2.01#3)的格式", Toast.LENGTH_LONG).show();
                                    }


                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {

                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(inputprice.getWindowToken(), 0); //强制隐藏键盘

                                    //dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
            mLineLayout.addView(pricebtn);

            addView(mLineLayout, params);

//            if(firstName.equals("(全)")) {
//                addView(mLineLayout, params);
//            } else {
//                if(map.get("goodsname").substring(0,1).equals(firstName)){
//                    addView(mLineLayout, params);
//                } else {
//
//                }
//            }
        }
    }

    // 启动线程修改价格
    class SetPriceThread extends Thread {
        private int t_cashfen = 0;
        private String t_code = "";

        public SetPriceThread(String code,int cashfen){
            t_code = code;
            t_cashfen = cashfen;
        }

        public void run() {

            //修改DB中的值
            try {
                SQLiteDatabase db = openOrCreateDatabase("vmdata.db",Context.MODE_PRIVATE, null);

                db.execSQL("update goodsname set payprice=" + t_cashfen
                        + " where goodscode='" + t_code + "'");


                db.execSQL("update trackmaingeneral set payprice=" + t_cashfen
                        + " where goodscode='" + t_code + "'");


                for(int i=0;i<70;i++){
                    if(((MyApp)getApplication()).getTrackMainGeneral()[i].getGoodscode().equals(t_code)){
                        ((MyApp)getApplication()).getTrackMainGeneral()[i].setPayprice(t_cashfen);
                    }

                }

                db.close();

                Message msg = msgHandler.obtainMessage();
                msg.what = xian_shi_disp_info;
                msg.obj = "修改成功！";
                msgHandler.sendMessage(msg);

            } catch (SQLException e) {
                Log.e(TAG, " 执行SQL命令" + e.toString());
            }

        }
    }


    private void setListAdapterFirst(){
        // 显示的列表
        listFirst = new ArrayList<>();

        // 计算有多少条需要显示的，准备内容
        Map<String,String> map;

        for(int i=0;i<goodsnameArrayFirst.length;i++){
            map = new HashMap<>();

            map.put("index",""+i);
            map.put("goodsnamefirst", goodsnameArrayFirst[i]);


            listFirst.add(map);
        }


        listViewFirst.removeAllViewsInLayout();
        listAdapterFirst = new ListAdapterFirst(this, listFirst );
        listViewFirst.setAdapter( listAdapterFirst );
    }

    public class ListAdapterFirst extends BaseAdapter {

        private Context context;
        private ArrayList<Map<String,String>> localList;
        //这就是adapter关联的List，用来存储数据的ArrayList　要往里传参数吗？　传的也是这个类型啊．呵呵

        public ListAdapterFirst(Context context, ArrayList<Map<String,String>> list ) {
            this.context = context;
            this.localList = list;
        }

        public int getCount() {
            return localList.size();
        }

        public Object getItem(int position) {
            return localList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Map<String,String> map = localList.get(position);
            return new adapterViewFirst(this.context, map );
        }

    }

    class adapterViewFirst extends LinearLayout {
        //public static final String LOG_TAG = "adapterView";

        public adapterViewFirst(Context context, Map<String,String> map ) {
            super( context );

            this.setOrientation(HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 2, 2, 2);

            LinearLayout mLineLayout = new LinearLayout(context);
            mLineLayout.setId(Integer.parseInt(map.get("index")));

            TextView notextControl = new TextView( context );
            LayoutParams notextparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            notextparams.setMargins(0, 20, 0, 4);
            notextControl.setTextColor(Color.BLACK);
            notextControl.setTextSize(22);
            notextControl.setText(map.get("goodsnamefirst") );
            notextControl.setLayoutParams(notextparams);
            mLineLayout.addView(notextControl);

//            notextControl.setClickable(true);
//            notextControl.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Log.d(TAG,((TextView)v).getText().toString());
//                }
//            });

            mLineLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    firstName = listFirst.get(v.getId()).get("goodsnamefirst");
                    msgHandler.sendEmptyMessage(xian_shi_filter);
                }
            });

            addView( mLineLayout, params );
        }
    }


    private void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除指定文件夹下所有文件
//param path 文件夹完整绝对路径
    private boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
}
