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
import com.mnevent.hz.bean.PreLibBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zyand on 2018/12/28.
 */

public class NameContextAdapter extends RecyclerView.Adapter<NameContextAdapter.ViewHolder> {



    private Context context;

    private List<PreLibBean.PngurlarrayBean> libes;

    public Onclike onclikes;


    public NameContextAdapter(Activity productoptionActivity, List<PreLibBean.PngurlarrayBean> libes) {
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
        Glide.with(context).load(libes.get(position).getPngurl()).into(holder.image);
        holder.name.setText(libes.get(position).getGoodsname());
        holder.price.setText("￥" + ":" + libes.get(position).getPricefen() / 100 + "." + libes.get(position).getPricefen() % 100);

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

    public void setUpDate(List<PreLibBean.PngurlarrayBean> upDate) {
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
