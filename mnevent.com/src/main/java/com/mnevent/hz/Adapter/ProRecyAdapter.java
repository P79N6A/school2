package com.mnevent.hz.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.ruffian.library.widget.RImageView;

import java.util.List;

/**
 * Created by Administrator on 2018/12/12.
 */

class ProRecyAdapter extends RecyclerView.Adapter<ProRecyAdapter.ViewHolder> {


    private Onitemclike onitemclike;
    private Context context;
    private List<PathwayMolder> images;
    private int pos;

    public ProRecyAdapter(Context mContext, List<PathwayMolder> images, int position) {
        this.context = mContext;
        this.images = images;
        this.pos = position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_productes, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
       // Log.d("zlc/", 9 / 9 + "");

      //  Log.d("sssss", images.get(pos * 9 + position).getName()+ "");

        Glide.with(context).load(images.get(pos * 9 + position).getImage()).into(holder.image);
        holder.price.setText(images.get(pos * 9 + position).getMonetaryunit()+":"+images.get(pos * 9 + position).getPrice());
        holder.name.setText(images.get(pos * 9 + position).getName());

        holder.Bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onitemclike.Btonclike(pos * 9 + position);


            }
        });
    }

    interface Onitemclike {
        void Btonclike(int position);
    }

    public void SetOnitemClike(Onitemclike onitemclike) {
        this.onitemclike = onitemclike;
    }

    ;

    @Override
    public int getItemCount() {

        if (images.size() < (pos + 1) * 9) {

            return images.size() % 9;
        } else {
            return 9;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RImageView image;
        TextView price;
        TextView name;
        View Bt;

        public ViewHolder(View itemView) {
            super(itemView);
            Bt = itemView;
            image = itemView.findViewById(R.id.image);
            price = itemView.findViewById(R.id.price);
            name = itemView.findViewById(R.id.name);

        }
    }
}
