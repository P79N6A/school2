package com.mnevent.hz.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zyand on 2018/12/28.
 */

public class PriceContextAdapter extends RecyclerView.Adapter<PriceContextAdapter.ViewHolder> {



    private Context context;

    private  List<PathwayMolder> libes;

    public Onclike onclikes;


    public PriceContextAdapter(Activity productoptionActivity, List<PathwayMolder> libes) {
        this.context = productoptionActivity;

        this.libes = libes;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_namecontext, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Glide.with(context).load(libes.get(position).getImage()).into(holder.image);
        holder.name.setText(libes.get(position).getName());
        holder.price.setText("￥:" + Integer.parseInt(libes.get(position).getPrice())/100+"."+Integer.parseInt(libes.get(position).getPrice())%100);

        holder.thePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclikes.btonclike(position);
            }
        });
    }


    public interface Onclike {
        void btonclike(int position);
    }

    public void SetOnclike(Onclike onclikes) {
        this.onclikes = onclikes;
    }

    @Override
    public int getItemCount() {
        return libes.size();
    }

    public void setUpDate(List<PathwayMolder> upDate) {
        this.libes = upDate;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.price)
        TextView price;
        @BindView(R.id.the_price)
        Button thePrice;


        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
