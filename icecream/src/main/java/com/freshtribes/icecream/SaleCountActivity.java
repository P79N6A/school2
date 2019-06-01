package com.freshtribes.icecream;

import android.app.Activity;
import android.os.Bundle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.freshtribes.icecream.app.MyApp;
import com.freshtribes.icecream.presenter.SaleCountPresenter;
import com.freshtribes.icecream.util.Gmethod;
import com.freshtribes.icecream.util.ProgressDialog;
import com.freshtribes.icecream.view.ISaleCountView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SaleCountActivity extends Activity implements ISaleCountView,View.OnClickListener{

    private final static SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private ThreadGroup tg = new ThreadGroup("TrackPlanActivityThreadGroup");

    private Dialog mProgressDialog;

    private SaleCountPresenter lp;
    private static final int dispHandler_rtnGetOK = 0x101;
    private static final int dispHandler_rtnGetError = 0x102;

    private TextView datetimetv;
    private TextView sumtotaltv;
    private String sumformat = "总计：totalsum元（totalcount笔）\n其中：现金：cashsum元（cashcount笔）、IC卡+会员：membersum元（membercount笔）\n            支付宝：alisum元（alicount笔）、微信：wxsum元（wxcount笔）、人脸支付：facesum元（facecount笔）";

    private String lastsupplytime = "";
    //获取一个日历对象
    private Calendar dateAndTime = Calendar.getInstance(Locale.CHINA);
    //获取日期格式器对象
    private DateFormat fmtDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
    private DateFormat fmtTime = new java.text.SimpleDateFormat("HH:mm");

    private ListView listView;
    private List<String> list;
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_count);

        // 生成Presenter
        lp = new SaleCountPresenter(this, tg, (MyApp) getApplication());

        Button backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(this);

        datetimetv = findViewById(R.id.datetimetv);
        datetimetv.setText(sd.format(System.currentTimeMillis()));

        sumtotaltv = findViewById(R.id.sumtotaltv);
        sumtotaltv.setText(sumformat.replace("totalsum","0").replace("totalcount","0")
                .replace("cashsum","0").replace("cashcount","0")
                .replace("membersum","0").replace("membercount","0")
                .replace("alisum","0").replace("alicount","0")
                .replace("wxsum","0").replace("wxcount","0")
                .replace("facesum","0").replace("facecount","0"));

        Button changedate = findViewById(R.id.btn_changedate);
        changedate.setOnClickListener(this);

        Button changetime = findViewById(R.id.btn_changetime);
        changetime.setOnClickListener(this);

        Button lastsupply = findViewById(R.id.btn_lastsupply);
        lastsupply.setOnClickListener(this);

        Button submit = findViewById(R.id.btn_submit);
        submit.setOnClickListener(this);

        SharedPreferences sp =
                SaleCountActivity.this.getSharedPreferences("vmsetting", Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sp.edit();//获取编辑器
        //String nowtime = sdfl.format(System.currentTimeMillis());
        //editor.putString("stocksupplytime", nowtime);
        //editor.apply();//提交修改
        lastsupplytime = sp.getString("stocksupplytime","");
        if(lastsupplytime.length() == 0){
            lastsupply.setVisibility(View.INVISIBLE);
        }

        list = new ArrayList<>();
        listView = findViewById(R.id.listview);
        arrayAdapter = new ArrayAdapter<>(this,R.layout.item_salecount,list);
        listView.setAdapter(arrayAdapter);
    }

    //当点击DatePickerDialog控件的设置按钮时，调用该方法
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            //修改日历控件的年，月，日
            //这里的year,monthOfYear,dayOfMonth的值与DatePickerDialog控件设置的最新值一致
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            //将页面TextView的显示更新为最新时间
            String nowtime = datetimetv.getText().toString();
            datetimetv.setText(fmtDate.format(dateAndTime.getTime()) + nowtime.substring(10));

            sumtotaltv.setText(sumformat.replace("totalsum","0").replace("totalcount","0")
                    .replace("cashsum","0").replace("cashcount","0")
                    .replace("membersum","0").replace("membercount","0")
                    .replace("alisum","0").replace("alicount","0")
                    .replace("wxsum","0").replace("wxcount","0")
                    .replace("facesum","0").replace("facecount","0"));
            list.clear();
            arrayAdapter.notifyDataSetChanged();
        }
    };

    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {

        //同DatePickerDialog控件
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);

            String nowtime = datetimetv.getText().toString();
            datetimetv.setText(nowtime.substring(0,11) + fmtTime.format(dateAndTime.getTime()));

            sumtotaltv.setText(sumformat.replace("totalsum","0").replace("totalcount","0")
                    .replace("cashsum","0").replace("cashcount","0")
                    .replace("membersum","0").replace("membercount","0")
                    .replace("alisum","0").replace("alicount","0")
                    .replace("wxsum","0").replace("wxcount","0")
                    .replace("facesum","0").replace("facecount","0"));
            list.clear();
            arrayAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                // 关闭presenter的所有线程，然后跳转
                lp.setStopThread();
                while (tg.activeCount() > 0) for (int i = 0; i < 1000; i++) ;
                tg.destroy();

                Intent toMenu = new Intent(SaleCountActivity.this, MenuActivity.class);
                startActivity(toMenu);
                SaleCountActivity.this.finish();
                break;

            case R.id.btn_changedate:
                DatePickerDialog  dateDlg = new DatePickerDialog(SaleCountActivity.this,
                        d,
                        dateAndTime.get(Calendar.YEAR),
                        dateAndTime.get(Calendar.MONTH),
                        dateAndTime.get(Calendar.DAY_OF_MONTH));
                dateDlg.show();
                break;

            case R.id.btn_changetime:
                TimePickerDialog timeDlg = new TimePickerDialog(SaleCountActivity.this,
                        t,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE),
                        true);
                timeDlg.show();
                break;

            case R.id.btn_lastsupply:
                datetimetv.setText(lastsupplytime.substring(0,16));
                sumtotaltv.setText(sumformat.replace("totalsum","0").replace("totalcount","0")
                        .replace("cashsum","0").replace("cashcount","0")
                        .replace("membersum","0").replace("membercount","0")
                        .replace("alisum","0").replace("alicount","0")
                        .replace("wxsum","0").replace("wxcount","0")
                        .replace("facesum","0").replace("facecount","0"));
                list.clear();
                arrayAdapter.notifyDataSetChanged();
                break;

            case R.id.btn_submit:
                if (mProgressDialog == null) {
                    mProgressDialog = ProgressDialog.createLoadingDialog(SaleCountActivity.this, "正在数据查询,请稍候...");
                }
                //((TextView)mProgressDialog.getWindow().findViewById(R.id.tipTextView)).setText("联网获取服务器数据,请稍候...");
                mProgressDialog.show();

                list.clear();
                lp.getSaleData(list,datetimetv.getText().toString()+":00");
                break;

            default:
                break;
        }
    }

    @Override
    public void rtnSaleCountError(String errormsg){
        Message msg = dispHandler.obtainMessage(dispHandler_rtnGetError);
        msg.obj = errormsg;
        dispHandler.sendMessage(msg);
    }

    @Override
    public void rtnSaleCount(long totalsum,long totalcount,long cashsum,long cashcount,
                             long alisum,long alicount,long wxsum,long wxcount,long facesum,long facecount,long membersum,long membercount){
        Message msg = dispHandler.obtainMessage(dispHandler_rtnGetOK);
        msg.obj = sumformat.replace("totalsum", Gmethod.tranFenToFloat2(totalsum)).replace("totalcount",""+totalcount)
                .replace("cashsum",Gmethod.tranFenToFloat2(cashsum)).replace("cashcount",""+cashcount)
                .replace("membersum",Gmethod.tranFenToFloat2(membersum)).replace("membercount",""+membercount)
                .replace("alisum",Gmethod.tranFenToFloat2(alisum)).replace("alicount",""+alicount)
                .replace("wxsum",Gmethod.tranFenToFloat2(wxsum)).replace("wxcount",""+wxcount)
                .replace("facesum",Gmethod.tranFenToFloat2(facesum)).replace("facecount",""+facecount);
        dispHandler.sendMessage(msg);

    }

    /**
     * Handle线程，显示处理结果
     */
    Handler dispHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // 处理消息
            switch (msg.what) {
                case dispHandler_rtnGetOK:

                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    sumtotaltv.setText(""+msg.obj);

                    arrayAdapter.notifyDataSetChanged();

                    break;

                case dispHandler_rtnGetError:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }

                    AlertDialog al = new AlertDialog.Builder(SaleCountActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("提示")
                            .setMessage("错误信息：" + msg.obj)
                            .setPositiveButton("确定", null)
                            .create();
                    al.setCancelable(false);
                    al.show();
                    break;

                default:
                    break;
            }
            return false;
        }
    });


}
