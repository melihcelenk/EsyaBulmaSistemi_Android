package com.melihcelenk.seslekontrol.activityler.kurulumactivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.modeller.bulunanCihaz;

import java.util.ArrayList;

public class cihazlarAdapter extends RecyclerView.Adapter<cihazlarAdapter.MyViewHolder> {
    private ArrayList<bulunanCihaz> mDataset;

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onLedYakClick(int position);
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView cihazIPTxt;
        public Switch ledYakSw;

        public MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            cihazIPTxt = itemView.findViewById(R.id.cihazIPTV);
            ledYakSw = itemView.findViewById(R.id.ledYakSw);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
            ledYakSw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onLedYakClick(position);
                        }
                    }
                }
            });
        }
    }

    public cihazlarAdapter(ArrayList<bulunanCihaz> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public cihazlarAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.kurulum_list_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v, mListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String simdiki = mDataset.get(position).getIp();
        Boolean ledDurum = mDataset.get(position).getLedDurum();
        holder.cihazIPTxt.setText(simdiki);
        holder.ledYakSw.setChecked(ledDurum);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
