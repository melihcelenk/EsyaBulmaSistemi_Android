package com.melihcelenk.seslekontrol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class cihazlarAdapter extends RecyclerView.Adapter<cihazlarAdapter.MyViewHolder> {
    private ArrayList<String> mDataset;


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView cihazIPTxt;

        public MyViewHolder(View itemView) {
            super(itemView);
            cihazIPTxt = itemView.findViewById(R.id.cihazIPTxt);

        }
    }

    public cihazlarAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public cihazlarAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String simdiki = mDataset.get(position);
        holder.cihazIPTxt.setText(simdiki);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
