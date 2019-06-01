package com.mnevent.hz.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mnevent.hz.R;
import com.mnevent.hz.bean.PriceDatailsBean;

import java.text.DecimalFormat;

/**
 * Created by Administrator on 2018/12/14.
 */

public class DetailsfigureAdapter extends RecyclerView.Adapter<DetailsfigureAdapter.ViewHolder>{

    private Context context;
    private PriceDatailsBean probean;
    ViewGroup.LayoutParams layoutParams;
    DecimalFormat df;
    int width = 0;// 得到图片宽
    int height = 0;// 得到图片高
    public DetailsfigureAdapter(Context mainActivity, PriceDatailsBean probean) {
        this.context = mainActivity;
        this.probean = probean;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_detailsfigure, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("zl",position+"wwwwww::::"+probean.getGood().getImages().get(position).getImage());

        layoutParams = holder.image.getLayoutParams();
        //df=new DecimalFormat("0.00");//设置保留位数
    /*    Glide.with(context).load(probean.getGood().getImages().get(position).getImage()).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                Log.d("zlc","宽::::"+resource.getIntrinsicWidth()+";"+resource.getMinimumWidth());
                Log.d("zlc","高::::"+resource.getIntrinsicHeight()+";"+resource.getMinimumHeight());
                //String format = df.format((float) resource.getIntrinsicHeight() / 440);
                float v = (float) resource.getIntrinsicHeight() / 440;
                float s = (float)resource.getIntrinsicWidth()/440;

                layoutParams.height = new Float(resource.getIntrinsicHeight()/v).intValue();
                layoutParams.width = new Float(resource.getIntrinsicWidth()/s).intValue();
                holder.image.setLayoutParams(layoutParams);
            }
        });
        layoutParams = holder.image.getLayoutParams();*/
       // Log.d("zlc","元::::"+layoutParams.height+";"+layoutParams.width);
       if(TextUtils.isEmpty(probean.getGood().getImages().get(position).getImage())){
           Glide.with(context).load(R.drawable.warning).into(holder.image);
       }else {
           Glide.with(context).load(probean.getGood().getImages().get(position).getImage())
                   .apply(new RequestOptions().placeholder(R.drawable.noimage))
                   .apply(new RequestOptions().error(R.drawable.warning))
                   .into(holder.image);
       }
        //holder.image.setBackgroundResource(R.drawable.g03042);
      //  Log.d("zld",width+"");
    }

    @Override
    public int getItemCount() {
       /* if(probean == null){
            return 0;
        }*/
        return probean.getGood().getImages().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }
}
