package com.example.administrator.godmapdemo;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-01-26.
 */

public abstract class MyAdapter<T> extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ArrayList<T> mList;
    private LayoutInflater inflater;
    @LayoutRes
    private int layoutRes;
    private OnItemClickListener listener;

    public MyAdapter(Context context, ArrayList<T> list, @LayoutRes int res) {
        inflater = LayoutInflater.from(context);
        mList = list;
        layoutRes = res;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(layoutRes, null, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
        holder.bindView(position);
        final int mPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener!=null){
                    listener.onItemClick(mPosition,v);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public abstract void bindViewHolder(T t, View itemView);

    class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
        }

        public void bindView(int position) {
            bindViewHolder(mList.get(position), itemView);
        }
    }

    interface OnItemClickListener {
        void onItemClick(int position, View itemView);
    }
}
