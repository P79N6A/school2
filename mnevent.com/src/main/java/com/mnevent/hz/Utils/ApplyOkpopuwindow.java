package com.mnevent.hz.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mnevent.hz.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/12/14.
 */

public class ApplyOkpopuwindow extends PopupWindow {


    Context context;
    View view;
    @BindView(R.id.text)
    TextView text;
    int time = 10;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                  if(time >= 0){
                      time = time -1;
                      text.setText("Please wait a moment, the goods are being shipped...("+time+")");
                      handler.sendEmptyMessage(1);
                  }
                    break;
            }
        }
    };

    public ApplyOkpopuwindow(Activity activity) {

        this.context = activity;

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.popu_apply_ok, null);
        ButterKnife.bind(this,view);
     //   handler.sendEmptyMessage(1);
        // 设置SelectPicPopupWindow的View
        this.setContentView(view);
        // 设置SelectPicPopupWindow弹出窗体的宽
        // this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        // this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimBottom);
        /**
         * 设置点击外边可以消失
         */
      /*  this.setFocusable(false);
        this.setOutsideTouchable(true);*/
        /**
         *设置可以触摸
         */
        // setTouchable(true);


        /**
         * 设置点击外部可以消失
         */


        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
    }
}
