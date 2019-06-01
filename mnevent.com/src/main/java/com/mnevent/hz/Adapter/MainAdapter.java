package com.mnevent.hz.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mnevent.hz.R;
import com.mnevent.hz.Utils.Log;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.litemolder.ProductdisplayMolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zyand on 2019/5/21.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.Viewholder> {



    private Context context;

    private List<PathwayMolder> pathwayall;

    public MainAdapter(Activity activity, List<PathwayMolder> pathwayall) {

        this.context = activity;
        this.pathwayall = pathwayall;

    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_main, parent, false);
        Viewholder viewholder = new Viewholder(inflate);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, final int position) {
        Glide.with(context).load(pathwayall.get(position).getImage()).into(holder.image);
        holder.name.setText(pathwayall.get(position).getName());
        holder.pice.setText("￥："+Integer.parseInt(pathwayall.get(position).getPrice())/100+"."+Integer.parseInt(pathwayall.get(position).getPrice())%100);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Log.d("zlc","轨道号："+pathwayall.get(position).getGoodscode());

            }
        });
    }

    @Override
    public int getItemCount() {
        return pathwayall.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.pice)
        TextView pice;
        public Viewholder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
