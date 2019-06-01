package com.mnevent.hz.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.ruffian.library.widget.RImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/12/14.
 */

public class GoumaiPopuwindow extends PopupWindow {

    @BindView(R.id.cuo)
    ImageView cuo;
    @BindView(R.id.image)
    RImageView image;
    @BindView(R.id.zhifu)
    TextView zhifu;
    @BindView(R.id.picre)
    TextView picre;
    @BindView(R.id.jishi)
    TextView jishi;
    @BindView(R.id.name)
    TextView name;
    private Context activity;
    View view;
    Onclikes onclikes;
    int js = 90;
    PathwayMolder listDataBean;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:

                    if (js > 0) {
                        js = js - 1;
                        jishi.setText("(close after " + js + "seconds）");
                        handler.sendEmptyMessageDelayed(1, 1000);
                    } else {
                        handler.removeCallbacksAndMessages(null);
                        dismiss();
                    }

                    break;
            }
        }
    };

    public GoumaiPopuwindow(Activity activity, PathwayMolder listDataBean) {
        this.activity = activity;
        this.listDataBean = listDataBean;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.popu_goumai, null);
        ButterKnife.bind(this, view);
        handler.sendEmptyMessageDelayed(1, 1000);
        initView();
    }

    private void initView() {
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

        Glide.with(activity).load(listDataBean.getImage()).into(image);
    }

    @OnClick({R.id.cuo, R.id.zhifu})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cuo:

                dismiss();
                break;
            case R.id.zhifu:
                onclikes.poonclike();
                zhifu.setText("In the payment...");
              //  zhifu.setBackgroundColor(Color.argb(0,0,0,0));
                break;
        }
    }


    public interface Onclikes {
        void poonclike();
    }

    public void SetOnclikes(Onclikes onclikes) {
        this.onclikes = onclikes;
    }

    /**
     * 商品图片
     *
     * @param images
     */
    public void SetImage(String images) {
        Glide.with(activity).load(images).into(image);
    }

    /**
     * 商品名称
     * @param names
     */
    public void Setname(String names){
        name.setText(names);
    }

    /**
     * 商品价格
     * @param picres
     */
    public void Setpicre(String picres){
        picre.setText("$:"+picres);
    }
}
