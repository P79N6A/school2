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

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/12/24.
 */

public class CheckAdapter extends RecyclerView.Adapter<CheckAdapter.ViewHolder> {


    private Context context;
    private List<PathwayMolder> preall;

    public CheckAdapter(Activity checkActivity, List<PathwayMolder> preall) {
        this.context = checkActivity;
        this.preall = preall;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_check, parent, false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        Glide.with(context).load(preall.get(position).getImage()).into(holder.image);

        holder.code.setText(preall.get(position).getPrecode());
        holder.name.setText(preall.get(position).getName());
        holder.libs.setText(preall.get(position).getInventory()+"");
      //  Log.d("zlc","preall.get(position).getMolder().getNumnow()"+preall.get(position).getNumnow());
            holder.libstwo.setText(preall.get(position).getNumnow()+"");


        holder.guidao.setText("轨道："+preall.get(position).getCode());

        holder.jia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int s = Integer.parseInt(holder.libstwo.getText().toString());
                if(s < Integer.parseInt(preall.get(position).getInventory())){
                    holder.libstwo.setText(s+1+"");
                    PathwayMolder molder = new PathwayMolder();
                    molder.setNumnow(s+1);
                    molder.updateAll("post = ?",position+"");
                }

            }
        });
        holder.jian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int s = Integer.parseInt(holder.libstwo.getText().toString());
                if(s > 0){
                    holder.libstwo.setText(s-1+"");

                    PathwayMolder molder = new PathwayMolder();

                    molder.setNumnow(s-1);

                    molder.updateAll("post = ?",position+"");
                    List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);
          //          Log.d("zlc",all.get(position).getNumnow()+"");
                }else{

                }

            }
        });
    }


    @Override
    public int getItemCount() {
        return preall.size();
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
        @BindView(R.id.libstwo)
        TextView libstwo;
        @BindView(R.id.jia)
        TextView jia;
        @BindView(R.id.jian)
        TextView jian;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
