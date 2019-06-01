package com.mnevent.hz.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnevent.hz.R;
import com.mnevent.hz.bean.PriceDatailsBean;

/**
 * Created by Administrator on 2018/12/14.
 */

public class ParameterAdapter extends RecyclerView.Adapter<ParameterAdapter.ViewHolder>{

    private Context context;
    private PriceDatailsBean probean;
    public ParameterAdapter(Context mainActivity, PriceDatailsBean probean) {
        this.context = mainActivity;
        this.probean = probean;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_par, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.text.setText(probean.getGood().getTitles().get(position).getTitle()+":"+probean.getGood().getTexts().get(position).getText());
    }

    @Override
    public int getItemCount() {
        return probean.getGood().getTitles().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        public ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }
}
