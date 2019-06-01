package com.mnevent.hz.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.mnevent.hz.MainActivity;
import com.mnevent.hz.R;
import com.mnevent.hz.bean.ComparisonBean;
import com.mnevent.hz.litemolder.PathwayMolder;

import java.util.List;

/**
 * Created by Administrator on 2018/12/13.
 * main产品展示
 */

public class YanpagerAdapter extends PagerAdapter {
    private Context context;
    List<PathwayMolder> probean;
    Onitemclikes onitemclikes;
    int db;
    YanRecyAdapter adapter;
    int number;
    List<ComparisonBean> listbean;
    View view;

    public YanpagerAdapter(MainActivity mainActivity, List<PathwayMolder> probean, int db, int number, List<ComparisonBean> listbean) {
        this.context = mainActivity;
        this.probean = probean;
        this.db = db;
        this.number = number;
        this.listbean = listbean;
    }

    @Override
    public int getCount() {

        return probean.size()/8+1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
         view = View.inflate(context, R.layout.item_yanduct, null);
        RecyclerView recy = view.findViewById(R.id.recy);
        GridLayoutManager manager = new GridLayoutManager(context,4);

        adapter = new YanRecyAdapter(context,probean,position,db,number);
        recy.setLayoutManager(manager);
        recy.setAdapter(adapter);
        adapter.setupdate(db,probean);
        adapter.SetOnclikes(new YanRecyAdapter.Onclikes() {
            @Override
            public void btonclikes(int positions) {
                onitemclikes.btitemoclikes(positions);
                notifyDataSetChanged();
            }

         /*   @Override
            public void gmonclikes(int positions) {

                onitemclikes.gmitemoclikes(positions);
            }*/
        });

        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        container.addView(view);


        return view;
    }

    public void setUpdate(int update, List<PathwayMolder> proall) {
        Log.d("zlc","wai");
        //adapter.setupdate(update,proall);
        this.db = update;
        this.probean = proall;

        notifyDataSetChanged();
        adapter.setupdate(db,probean);
    }

    public void setUpdates(List<PathwayMolder> proall) {
        adapter.setupdates(proall);
        notifyDataSetChanged();
    }

    public void setupinventory(List<PathwayMolder> upinventory) {
        this.probean = upinventory;
        notifyDataSetChanged();
        adapter.setupinventory(upinventory);
    }

    public interface Onitemclikes{
        void btitemoclikes(int positions);
      //  void gmitemoclikes(int positions);
    }

    public void SteItemOnclikes(Onitemclikes onitemclikes){
        this.onitemclikes = onitemclikes;
    }
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        // 最简单解决 notifyDataSetChanged() 页面不刷新问题的方法
        return POSITION_NONE;
    }
}
