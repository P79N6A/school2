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
import com.mnevent.hz.litemolder.PathwayMolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/12/25.
 */

public class ReplenishmentAdapter extends RecyclerView.Adapter<ReplenishmentAdapter.ViewHolder> {


    private Context context;

    private List<PathwayMolder> all;

    private Onclike onclike;
    public ReplenishmentAdapter(Activity replenishmentActivity, List<PathwayMolder> all) {

        this.all = all;

        this.context = replenishmentActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_replenishment, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Glide.with(context).load(all.get(position).getImage()).into(holder.image);
        holder.guidao.setText("轨道号："+all.get(position).getCode());
        holder.code.setText(all.get(position).getPrecode());
        holder.name.setText(all.get(position).getName());
        holder.libs.setText(all.get(position).getInventory()+"");
        holder.libes.setText(all.get(position).getNumnow()+"");
        holder.huanpro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.libes.setText(holder.libs.getText().toString());
                PathwayMolder molder = new PathwayMolder();
                molder.setNumnow(Integer.parseInt(holder.libs.getText().toString()));
                molder.updateAll("code = ?",all.get(position).getCode());
            }
        });
    }

    public interface Onclike{
        void bonclike();
    }

    public void SetOnclike(Onclike onclike){
        this.onclike = onclike;
    }
    @Override
    public int getItemCount() {
        return all.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.guidao)
        TextView guidao;
        @BindView(R.id.code)
        TextView code;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.libs)
        TextView libs;
        @BindView(R.id.libes)
        TextView libes;
        @BindView(R.id.huanpro)
        TextView huanpro;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
