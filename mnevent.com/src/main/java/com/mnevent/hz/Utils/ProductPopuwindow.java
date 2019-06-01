package com.mnevent.hz.Utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mnevent.hz.MainActivity;
import com.mnevent.hz.R;
import com.mnevent.hz.Adapter.DetailsfigureAdapter;
import com.mnevent.hz.Adapter.ParameterAdapter;
import com.mnevent.hz.bean.PriceDatailsBean;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.ruffian.library.widget.RImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/12/17.
 */

public class ProductPopuwindow extends PopupWindow {

    @BindView(R.id.shou)
    ImageView shou;
    @BindView(R.id.image)
    RImageView image;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.price)
    TextView price;
    @BindView(R.id.recy1)
    RecyclerView recy1;
    @BindView(R.id.recy2)
    RecyclerView recy2;
    @BindView(R.id.re2)
    RelativeLayout re2;
    private Context context;
    View view;
    List<PathwayMolder> proall;
    int post;
    Onclikes onclikes;


    public ProductPopuwindow(MainActivity mainActivity, List<PathwayMolder> proall, int post) {
        this.context = mainActivity;
        this.proall = proall;

        this.post = post;
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.popu_product, null);
        ButterKnife.bind(this, view);
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
        this.setAnimationStyle(R.style.BottomDialogWindowAnim);
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




/*
        if (recy2.getRecycledViewPool() != null) {
            recy2.getRecycledViewPool().setMaxRecycledViews(0, 10);
        }
        LinearLayoutManager manager1 = new LinearLayoutManager(context);
        DetailsfigureAdapter adapter1 = new DetailsfigureAdapter(context, proall);
        recy2.setLayoutManager(manager1);
        recy2.setAdapter(adapter1);*/

        shou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclikes.onclike();
            }
        });

    }

    public interface Onclikes{
        void onclike();
    }
    public void SetOnclike(Onclikes onclikes){
        this.onclikes = onclikes;
    }

    /**
     * 设置图片
     * @param images
     */
    public void SetImage(String images){
        Glide.with(context).load(images).into(image);
    }

    /**
     * 设置名称
     * @param names
     */
    public void Setname(String names){
        name.setText(names);
    }

    /**
     *
     * 文字详情
     * @param priceDatailsBean
     */
    public void settitle(PriceDatailsBean priceDatailsBean){
        LinearLayoutManager manager = new LinearLayoutManager(context);
        ParameterAdapter adapter = new ParameterAdapter(context, priceDatailsBean);
        recy1.setLayoutManager(manager);
        recy1.setAdapter(adapter);
    }

    /**
     * 图片详情
     * @param priceDatailsBean
     */
    public void setImageRecy(PriceDatailsBean priceDatailsBean){

      /*  if (recy2.getRecycledViewPool() != null) {
            recy2.getRecycledViewPool().setMaxRecycledViews(0, 10);
        }*/
     //   LinearLayoutManager manager1 = new LinearLayoutManager(context);
        StaggeredGridLayoutManager manager1 =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        DetailsfigureAdapter adapter1;
        adapter1 = new DetailsfigureAdapter(context, priceDatailsBean);
        recy2.setLayoutManager(manager1);
        recy2.setAdapter(adapter1);
    }

    public void SetPrice(String prices){
        price.setText(prices);
    }
}
