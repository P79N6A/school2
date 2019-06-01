package com.mnevent.hz.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;

import java.util.List;

/**
 * Created by Administrator on 2018/12/19.
 */

public class ProselectAdapter extends RecyclerView.Adapter<ProselectAdapter.ViewHolder> {


    private Context context;

    ItemOnclike itemOnclike;
    List<PathwayMolder> patall;

    public ProselectAdapter(Activity stockActivity, List<PathwayMolder> patall) {
        this.context = stockActivity;

        this.patall = patall;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_proselect, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
       /* if(parliball.get(position).getMolder().getPd().equals("0")){
            holder.bt.setVisibility(View.GONE);
        }else {
            holder.bt.setVisibility(View.VISIBLE);
        }*/

        Glide.with(context).load(patall.get(position).getImage()).into(holder.image);

        holder.code.setText(patall.get(position).getPrecode());
        holder.name.setText(patall.get(position).getName());
        holder.libs.setText(patall.get(position).getInventory()+"");
        holder.libes.setText(patall.get(position).getNummax()+"");


        holder.guidao.setText("orbital："+patall.get(position).getCode());
        holder.huaninv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("zlc","holder.libs.getText().toString():"+holder.libs.getText().toString());

                    itemOnclike.invclike(position,holder.libs.getText().toString(),patall.get(position).getCode());


            }
        });
        holder.huanpro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("zlc","holder.name.getText().toString():"+holder.name.getText().toString());
                itemOnclike.proclike(position,holder.name.getText().toString(),patall.get(position).getCode());
            }
        });
        holder.set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("zlc","holder.libes.getText().toString():"+holder.libes.getText().toString());
                itemOnclike.setonclike(position,holder.libes.getText().toString(),patall.get(position).getCode());
            }
        });
        holder.gets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemOnclike.getonclike(position,patall.get(position).getCode());
            }
        });

    }

    public interface ItemOnclike{
        /**
         * 换商品
         * @param position
         * @param s
         * @param text
         */
        void proclike(int position, String s, String text);

        /**
         * 换库存
         * @param position
         * @param s
         * @param text
         */
        void invclike(int position, String s, String text);

        /**
         * 调整满仓数
         * @param position
         * @param toString
         * @param s
         */
        void setonclike(int position, String toString, String s);
        /**
         * 同上
         * @param position
         * @param code
         */
        void getonclike(int position, String code);
    }

    public void update(List<PathwayMolder> productMolders, int positions){

        this.patall = productMolders;
        Log.d("zlc","size:"+patall.size());
        notifyItemChanged(positions);
    }
    public void SetItemOnclike(ItemOnclike itemOnclike){
        this.itemOnclike = itemOnclike;
    }

    @Override
    public int getItemCount() {
        return patall.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView guidao;
        TextView code;
        TextView libs;
        TextView name;
        TextView huanpro;
        TextView huaninv;
        TextView set;
        TextView gets;
        TextView libes;
        View bt;

        public ViewHolder(View itemView) {
            super(itemView);
            bt = itemView;
            image = itemView.findViewById(R.id.image);
            guidao = itemView.findViewById(R.id.guidao);
            code = itemView.findViewById(R.id.code);
            libs = itemView.findViewById(R.id.libs);
            name = itemView.findViewById(R.id.name);
            huanpro = itemView.findViewById(R.id.huanpro);
            huaninv = itemView.findViewById(R.id.huaninv);
            set = itemView.findViewById(R.id.set);
            gets = itemView.findViewById(R.id.gets);
            libes = itemView.findViewById(R.id.libes);
        }
    }
}
