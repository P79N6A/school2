package com.mnevent.hz.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mnevent.hz.MainActivity;
import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;

import java.util.List;

/**
 * Created by Administrator on 2018/12/12.
 */

public class ProPagerAdapter extends PagerAdapter {
    private Context mContext;
    private Onitemclikes onitemclikes;
    private List<PathwayMolder> images;
    public ProPagerAdapter(MainActivity dispActivity, List<PathwayMolder> images) {
        this.mContext = dispActivity;
        this.images = images;
    }

    @Override
    public int getCount() {

        return images.size()/9+1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = View.inflate(mContext, R.layout.item_product,null);
        RecyclerView recy = view.findViewById(R.id.recy);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
        recy.setLayoutManager(gridLayoutManager);
        ProRecyAdapter adapter = new ProRecyAdapter(mContext,images,position);
        recy.setAdapter(adapter);
        adapter.SetOnitemClike(new ProRecyAdapter.Onitemclike() {
            @Override
            public void Btonclike(int positions) {
                onitemclikes.onclikes(position,positions);
            }
        });
        container.addView(view);
        return view;
    }

    public interface Onitemclikes{
        void onclikes(int position, int positions);
    }
    public void SetOnitemclikes(Onitemclikes onitemclikes){
        this.onitemclikes = onitemclikes;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container,position,object); 这一句要删除，否则报错
        container.removeView((View)object);
    }

}
