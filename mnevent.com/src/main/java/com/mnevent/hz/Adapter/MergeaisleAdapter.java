package com.mnevent.hz.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.Utils.CustomHRecyclerView;

import java.util.List;

/**
 * Created by Administrator on 2018/12/20.
 * 货道合并适配器
 */

public class MergeaisleAdapter extends RecyclerView.Adapter<MergeaisleAdapter.ViewHolder>{

    private Context context;

    private List<PathwayMolder> proall;

    MergeaislcksAdapter adapter1;

    Onclike onclike;
    public MergeaisleAdapter(Activity mergeaisleActivity, List<PathwayMolder> proall) {
        this.context = mergeaisleActivity;
        this.proall = proall;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_mergeaisle, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tt.setText("第"+(position+1)+"层");

        LinearLayoutManager manager1 = new LinearLayoutManager(context, OrientationHelper.HORIZONTAL,false);
        holder.recy.setLayoutManager(manager1);
        adapter1 = new MergeaislcksAdapter(context, proall,position);
        holder.recy.setAdapter(adapter1);

        adapter1.SetOnclike(new MergeaislcksAdapter.Onclike() {
            @Override
            public void imageonclike(int position, String type) {
                onclike.imageonclike(position,type);

            }

            @Override
            public void textsonclike(final int positions) {
               onclike.textsonclike(positions);
            }
        });

    }

    public void update(int i, int positions) {
        adapter1.update(i,positions);
        notifyDataSetChanged();
    }

    public void updatees(int position, String type) {
        adapter1.updatees(position,type);
        notifyDataSetChanged();
    }

    public interface Onclike{
        void imageonclike(int position, String type);

        void textsonclike(int position);
    }

    public void SetOnclike(Onclike onclike){
        this.onclike = onclike;
    }
    @Override
    public int getItemCount() {

        if(proall.size()%9 == 0){
            return proall.size()/9;
        }else{
            return proall.size()/9+1;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tt;
        CustomHRecyclerView recy;
        public ViewHolder(View itemView) {
            super(itemView);
            tt = itemView.findViewById(R.id.tt);
            recy = itemView.findViewById(R.id.recy);
        }
    }
}
