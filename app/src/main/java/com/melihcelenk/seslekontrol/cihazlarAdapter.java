package com.melihcelenk.seslekontrol;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class cihazlarAdapter extends RecyclerView.Adapter<cihazlarAdapter.MyViewHolder> {
    private ArrayList<String> mDataset;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout LinearLayout;
        public MyViewHolder(LinearLayout v) {
            super(v);
            LinearLayout = v;
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
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
