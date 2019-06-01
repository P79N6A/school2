package com.mnevent.hz.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.mnevent.hz.R;
import com.mnevent.hz.bean.PreLibBean;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zyand on 2018/12/28.
 * 商品选择
 */

public class NameTitleAdapter extends RecyclerView.Adapter<NameTitleAdapter.ViewHolder> {


    private Context context;

    private PreLibBean bean;

    private Onclike onclike;

    public NameTitleAdapter(Activity productoptionActivity, PreLibBean bean) {

        this.context = productoptionActivity;
        this.bean = bean;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_productoptionone, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

      /*  if(bean.getGoodsInfoVo().getAllGoods().get(position).getPd().equals("1")){
            holder.text.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.textview_lanbianneibaikuang));
            holder.text.setTextSize(22);
        }else{
            holder.text.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.textview_wubianbianneibaikuang));
            holder.text.setTextSize(20);
        }
        holder.text.setText(bean.getGoodsInfoVo().getAllGoods().get(position).getGoodstypename());
        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                for (int i = 0;i<bean.getGoodsInfoVo().getAllGoods().size();i++){
                if(position == i){
                    bean.getGoodsInfoVo().getAllGoods().get(i).setPd("1");
                }else{
                    bean.getGoodsInfoVo().getAllGoods().get(i).setPd("0");
                }
                }

                onclike.backles(position);
                notifyDataSetChanged();
            }
        });*/
    }


    public interface Onclike{
        void backles(int position);
    }

    public void SetOnclike(Onclike onclike){
        this.onclike = onclike;
    }
    @Override
    public int getItemCount() {

        return 0;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text)
        TextView text;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
