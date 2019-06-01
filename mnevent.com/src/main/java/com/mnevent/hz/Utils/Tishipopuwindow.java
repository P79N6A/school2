package com.mnevent.hz.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mnevent.hz.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/12/14.
 * 提示
 */

public class Tishipopuwindow extends PopupWindow {


    Context context;
    View view;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.text1)
    TextView text1;
    @BindView(R.id.text2)
    TextView text2;



    public Tishipopuwindow(Activity activity) {

        this.context = activity;


        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.popu_tishi, null);

        ButterKnife.bind(this,view);
        // 设置SelectPicPopupWindow的View
        this.setContentView(view);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimBottom);
        /**
         * 设置点击外边可以消失
         */
        this.setFocusable(false);
        this.setOutsideTouchable(true);
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

    @OnClick(R.id.back)
    public void onViewClicked() {
        dismiss();
    }

    //图片
    public void setimager(int tupian){
        if(tupian != 0){
            Glide.with(context).load(tupian).into(image);
        }
    }

    //第一行
    public void setText1(String txt){
        if(!TextUtils.isEmpty(txt)){
            text1.setText(txt);
        }
    }

    //第二行
    public void setText2(String txt){
        if(!TextUtils.isEmpty(txt)){
            text2.setText(txt);
        }
    }
}
