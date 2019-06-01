package com.mnevent.hz.Adapter;

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
import com.mnevent.hz.Utils.ToastUtil;

import java.util.List;

/**
 * Created by Administrator on 2018/12/20.
 */

public class MergeaislcksAdapter extends RecyclerView.Adapter<MergeaislcksAdapter.ViewHolder>{

    private Context context;

    private List<PathwayMolder> proall;
    String type = "1";
    Onclike onclike;
    private int post;
    public MergeaislcksAdapter(Context mergeaisleActivity, List<PathwayMolder> proall, int position) {

        this.context = mergeaisleActivity;

        this.proall = proall;

        this.post = position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_mergeuslck, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {


        if(proall.get(post*9+position).getPds().equals("0")){
            holder.t40.setVisibility(View.GONE);
            holder.ts40.setVisibility(View.GONE);
        }
        if(proall.get(post*9+position).getMergecode().equals("01")){
            Glide.with(context).load(R.drawable.unincorporated).into(holder.t40);
        }else if(proall.get(post*9+position).getMergecode().equals("00")){
            Glide.with(context).load(R.drawable.merge).into(holder.t40);
        }else if(proall.get(post*9+position).getMergecode().equals("02")){
            Glide.with(context).load(R.drawable.forbidden).into(holder.t40);
        }

        holder.ts40.setText(Integer.parseInt(proall.get(post*9+position).getSteps())+":"+proall.get(post*9+position).getNummax());
        holder.ts40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclike.textsonclike(post*9+position);

            }
        });
        holder.t40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(proall.get(post*9+position).getMergecode().equals("01")){
                    proall.get(post*9+position).setMergecode("02");
                    type = "02";
                }else  if(proall.get(post*9+position).getMergecode().equals("02")){
                    type = "00";
                    proall.get(post*9+position).setMergecode("00");
                }else  if(proall.get(post*9+position).getMergecode().equals("00")){
                    type = "01";
                    proall.get(post*9+position).setMergecode("01");
                }else{
                    ToastUtil.showToast(context,"This cargo channel is out of order");
                }
                notifyItemChanged(post*9+position);

                onclike.imageonclike(post*9+position,type);
            }
        });
    }

    public void update(int i, int positions) {
        Log.d("zlc","点击了"+i+"zz"+positions+1);
        proall.get(positions).setSteps(i+"");
        int i1 = proall.get(positions).getChain() / i;
        proall.get(positions).setNummax(i1);
        if(positions < (post)*9){

            notifyDataSetChanged();
        }else {

            notifyDataSetChanged();
        }

    }

    public void updatees(int position, String type) {
        proall.get(position).setMergecode(type);
        if(position < (post)*9){

            notifyDataSetChanged();
        }else {

            notifyDataSetChanged();
        }
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
        if(proall.size() < (post+1)*9){

            return proall.size()%9;
        }else {
            return 9;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView t40;
        TextView ts40;

        public ViewHolder(View itemView) {
            super(itemView);
            t40 = itemView.findViewById(R.id.t40);
            ts40 = itemView.findViewById(R.id.ts40);
        }
    }
}
