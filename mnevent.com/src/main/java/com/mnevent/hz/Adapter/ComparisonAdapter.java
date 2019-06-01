package com.mnevent.hz.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import com.mnevent.hz.MainActivity;
import com.mnevent.hz.R;
import com.mnevent.hz.bean.ComparisonBean;
import com.ruffian.library.widget.RImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zyand on 2018/12/29.
 */

public class ComparisonAdapter extends RecyclerView.Adapter<ComparisonAdapter.ViewHolder> {


    private Context context;

    private List<ComparisonBean> listbean;

    Onclike onclike;


    public ComparisonAdapter(MainActivity mainActivity, List<ComparisonBean> listbean) {
        this.context = mainActivity;

        this.listbean = listbean;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_comparison, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Glide.with(context).load(listbean.get(position).getImage()).into(holder.image);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclike.nolike(position);
            }
        });
    }

    public interface Onclike{
        void nolike(int position);
    }

    public void SetOnclike(Onclike onclike){
        this.onclike = onclike;
    }

    @Override
    public int getItemCount() {
        return listbean.size();
    }

    public void setUpdate(List<ComparisonBean> update) {
        this.listbean = update;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        RImageView image;
        @BindView(R.id.image1)
        ImageView image1;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
