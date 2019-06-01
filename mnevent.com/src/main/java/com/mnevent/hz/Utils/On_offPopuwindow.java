package com.mnevent.hz.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mnevent.hz.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2019/2/20.
 */

public class On_offPopuwindow extends PopupWindow {

    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.cancel)
    TextView cancel;
    @BindView(R.id.affirm)
    TextView affirm;
    private Context context;

    private Onclike onclike;

    @SuppressLint("ServiceCast")
    public On_offPopuwindow(Activity activity) {
        this.context = activity;
        LayoutInflater systemService = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inflate = systemService.inflate(R.layout.popu_on_off, null);
        ButterKnife.bind(this, inflate);
        // 设置SelectPicPopupWindow的View
        this.setContentView(inflate);
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


    @OnClick({R.id.cancel, R.id.affirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                onclike.setcancel();
                break;
            case R.id.affirm:
                onclike.setaffirm();
                break;
        }
    }

    public interface Onclike{
        void setcancel();

        void setaffirm();
    }

    public void SetOnclike(Onclike onclike){
        this.onclike = onclike;
    }

    /**
     * 设置内容
     * @param texts
     */
    public void setText(String texts){
        text.setText(texts);
    }
}
