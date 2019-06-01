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
import com.mnevent.hz.bean.PressBean;

import java.util.List;

/**
 * Created by Administrator on 2018/12/24.
 */

public class PressAdapter extends RecyclerView.Adapter<PressAdapter.ViewHolder>{

    private Context context;

    List<PressBean> press;

    private Onclike onclike;
    public PressAdapter(Activity basicparameterActivity, List<PressBean> press) {

        this.context = basicparameterActivity;

        this.press = press;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_press, parent,false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.text.setText(press.get(position).getPres());

        if(press.get(position).getPd() == 0){
            Glide.with(context).load(R.drawable.flclick).into(holder.image);
        }else{
            Glide.with(context).load(R.drawable.trclick).into(holder.image);
        }

        holder.bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(press.get(position).getPd() == 0){
                    press.get(position).setPd(1);
                }else{
                    press.get(position).setPd(0);
                }
                onclike.btonclike(position);
                notifyDataSetChanged();
            }
        });
    }

    public interface Onclike{
        void btonclike(int position);
    }

    public void SetOnclike(Onclike onclike){
        this.onclike = onclike;
    }

    @Override
    public int getItemCount() {
        return press.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View bt;
        private TextView text;
        private ImageView image;
        public ViewHolder(View itemView) {
            super(itemView);
            bt = itemView;
            text = itemView.findViewById(R.id.text);
            image = itemView.findViewById(R.id.image);
        }
    }
}
