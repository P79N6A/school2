package com.mnevent.hz.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.ruffian.library.widget.RImageView;
import com.ruffian.library.widget.helper.RImageViewHelper;

import java.util.List;

/**
 * Created by Administrator on 2018/12/13.
 * main产品展示子目录
 */

class YanRecyAdapter extends RecyclerView.Adapter<YanRecyAdapter.ViewHolder> {


    private Context context;
    List<PathwayMolder> probean;
    int pos;
    Onclikes onclikes;
    RImageViewHelper helper;
    //详情
    private int xq = 0;
    //是否对比
    private int dbw;
    //去重
    private int qc;



    public YanRecyAdapter(Context context, List<PathwayMolder> probean, int position, int db, int number) {
        this.context = context;
        this.probean = probean;
        this.pos = position;
        this.dbw = db;
        Log.d("zlc","db"+db);



    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_yanductes, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        Glide.with(context).load(probean.get(pos * 8 + position).getImage()).into(holder.image);
        holder.price.setText(probean.get(pos * 8 + position).getMonetaryunit() + ":" + probean.get(pos * 8 + position).getPrice());
        holder.name.setText(probean.get(pos * 8 + position).getName());
        helper = holder.image.getHelper();
        // holder.image.setImageResource(probean.getDate().getListdate().get(position).getImage());
        int w = 10;
        if (probean.size() < (pos + 1) * 8) {

            w = probean.size() % 8;
        } else {
            w = 10;
        }

        final int finalW = w;
        holder.bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("zlc", "db" + dbw);

                notifyDataSetChanged();

                onclikes.btonclikes(pos * 8 + position);
            }

        });

        if (probean.get(pos * 8 + position).getPd() == 1) {
            holder.image.setBackgroundResource(R.drawable.textview_lanbianneibaikuang);
          //  holder.buy.setVisibility(View.VISIBLE);
            if (dbw == 0) {
                holder.zhezhao.setVisibility(View.VISIBLE);
            } else {
                holder.zhezhao.setVisibility(View.GONE);
            }
            helper.setBorderWidth(4);
            helper.setBorderColor(Color.parseColor("#AEABEC"));

            Log.d("zlc", "1" + position);
        } else {
            holder.image.setBackgroundResource(R.drawable.textview_neibaikuang);
           // holder.buy.setVisibility(View.GONE);
            holder.zhezhao.setVisibility(View.GONE);
            helper.setBorderWidth(2);
            helper.setBorderColor(Color.parseColor("#E6E6E6"));
            Log.d("zlc", "2" + position);
        }

     /*   holder.buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ZLC","POST:"+position);
                onclikes.gmonclikes(pos * 10 + position);
                Log.d("ZLC","POST1:"+pos * 10 );
            }
        });*/

     if(probean.get(pos * 8 + position).getInventory().equals("0")){
         holder.sellout.setVisibility(View.VISIBLE);
     }else{
         holder.sellout.setVisibility(View.GONE);
     }

    }

    public void setupdate(int update, List<PathwayMolder> proall) {

        this.dbw = update;
        this.probean = proall;
        Log.d("zlc", "li" + this.dbw);
        notifyDataSetChanged();
    }

    public void setupdates(List<PathwayMolder> proall) {
        this.probean = proall;
        notifyDataSetChanged();
    }

    public void setupinventory(List<PathwayMolder> upinventory) {
        this.probean = upinventory;
        notifyDataSetChanged();
    }

    public interface Onclikes {
        void btonclikes(int i);

      //  void gmonclikes(int position);
    }

    public void SetOnclikes(Onclikes onclikes) {
        this.onclikes = onclikes;
    }

    @Override
    public int getItemCount() {
        if (probean.size() < (pos + 1) * 8) {

            return probean.size() % 8;
        } else {
            return 8;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RImageView image;
        TextView price;
        TextView name;
       // RelativeLayout buy;
        View bt;
        TextView zhezhao;
        RelativeLayout sellout;

        public ViewHolder(View itemView) {
            super(itemView);
            bt = itemView;
            image = itemView.findViewById(R.id.image);
            price = itemView.findViewById(R.id.price);
            name = itemView.findViewById(R.id.name);
            //buy = itemView.findViewById(R.id.buy);
            zhezhao = itemView.findViewById(R.id.zhezhao);
            sellout = itemView.findViewById(R.id.sell_out);

        }
    }
}
