package com.mnevent.hz.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnevent.hz.R;


/**
 * Created by Administrator on 2018/12/19.
 */

public class BasicpopuAdapter extends RecyclerView.Adapter<BasicpopuAdapter.ViewHolder>{

    private Context context;

    Onckile onckile;


    String[] numbers;

    int i;
    public BasicpopuAdapter(Context context, String[] numbers, int i) {

        this.context = context;

        this.numbers = numbers;

        this.i = i;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_pronumberpopu, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {


        holder.text.setText(numbers[position]+"");
        holder.bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onckile.onclike(position);
            }
        });
    }

    public interface Onckile{
        void onclike(int position);
    }
    public void SetOnclike(Onckile onckile){
        this.onckile = onckile;
    }
    @Override
    public int getItemCount() {
        if(i == 0){
            return numbers.length;
        }else {
            return i;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        View bt;
        public ViewHolder(View itemView) {
            super(itemView);
            bt = itemView;
            text = itemView.findViewById(R.id.text);
        }
    }
}
