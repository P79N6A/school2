package com.mnevent.hz.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.mnevent.hz.R;
import com.mnevent.hz.bean.PressBean;
import com.mnevent.hz.litemolder.PathwayMolder;

import java.util.List;

/**
 * Created by Administrator on 2018/12/24.
 */

public class HierarchyAdapter extends RecyclerView.Adapter<HierarchyAdapter.ViewHolder>{

    private Context context;
    private List<PressBean> hierarchy;
    private int hierar;
    private Onclike onclike;
    List<PathwayMolder> all;
    //private int[] xians = new int[]{0,0,0,0,0,0,0,0};
    private int number;


    public HierarchyAdapter(Activity basicparameterActivity, List<PressBean> hierarchy, int hierar, List<PathwayMolder> all, int i) {

        this.context = basicparameterActivity;
        this.hierarchy = hierarchy;
        this.hierar = hierar;
        this.all = all;
        this.number = i;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_hierarchy, parent,false);
        ViewHolder holder = new ViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d("zlc","number"+number);
        if(hierarchy!=null && all!=null){
            for (int i = position*number;i<(position+1)*number;i++){
                if(all.get(i).getPds().equals("1")){
                    //xians[position] = xians[position]+1;
                    hierarchy.get(position).setNumber(hierarchy.get(position).getNumber()+1);;
                }
            }
            holder.text.setText(hierarchy.get(position).getPres());
            holder.text1.setText(hierarchy.get(position).getNumber()+"");
            holder.text1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onclike.btonclike(position);
                }
            });
        }

    }

    public void setupdatees(List<PathwayMolder> i, int updatees) {
        hierarchy.get(updatees).setNumber(0);
        all = i;
        notifyItemChanged(updatees);
    }

    public void setupdatess(List<PathwayMolder> all, int i) {

        this.all = all;
        this.number = i;

        notifyDataSetChanged();
    }

    public void setupdateas(List<PressBean> hierarchy, int hierar, List<PathwayMolder> all, int i) {
        for(int s = 0; s<hierarchy.size();s++){
            hierarchy.get(s).setNumber(0);
        }
        this.hierarchy = hierarchy;
        this.hierar = hierar;
        this.all = all;
        this.number = i;
        notifyDataSetChanged();
    }

    public interface Onclike{
        void btonclike(int position);
    }

    public void SetOnclike(Onclike onclike){
        this.onclike = onclike;
    }


    @Override
    public int getItemCount() {
        return hierar;
    }

    public void setupdate(int update, List<PathwayMolder> all1, List<PressBean> hierarchy) {
        for(int i = 0; i<hierarchy.size();i++){
            hierarchy.get(i).setNumber(0);
        }
        this.hierar = update;
        this.all = all1;
        this.hierarchy = hierarchy;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text;
        private TextView text1;
        public ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            text1 = itemView.findViewById(R.id.text1);
        }
    }
}
