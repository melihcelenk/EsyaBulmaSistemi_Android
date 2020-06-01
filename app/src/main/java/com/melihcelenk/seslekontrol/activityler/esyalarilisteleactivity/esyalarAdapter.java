package com.melihcelenk.seslekontrol.activityler.esyalarilisteleactivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.melihcelenk.seslekontrol.R;
import com.melihcelenk.seslekontrol.modeller.Esya;

import java.util.ArrayList;

// Bu Adapter EsyalariListeleActivity üzerinde çalışmak üzere res/layout/esyalar_list_item.xml layoutunu kullanarak çalışmaktadır
// Çalışma mantığı için bkz: https://youtu.be/Nw9JF55LDzE

public class esyalarAdapter extends RecyclerView.Adapter<esyalarAdapter.MyViewHolder> {
    private ArrayList<Esya> mDataset;

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onSinyalBtnClick(int position);
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView esyaAdi;
        public TextView esyaAnahtarKelimeler;
        public Button esyayaSinyalBtn;

        public MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            esyaAdi = itemView.findViewById(R.id.esyaAdiTV);
            esyaAnahtarKelimeler = itemView.findViewById(R.id.esyaAnahtarKelimelerTV);
            esyayaSinyalBtn = itemView.findViewById(R.id.esyayaSinyalBtn);

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
            esyayaSinyalBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onSinyalBtnClick(position);
                        }
                    }
                }
            });
        }
    }

    public esyalarAdapter(ArrayList<Esya> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public esyalarAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // Yeni bir View oluştur
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.esyalar_list_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v, mListener);
        return vh;
    }

    //View içeriğini değiştir
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String simdiki = mDataset.get(position).get_esyaAdi();
        holder.esyaAdi.setText(simdiki);

        String simdikiAnahtar = mDataset.get(position).get_esyaAnahtarKelimelerString();
        holder.esyaAnahtarKelimeler.setText(simdikiAnahtar);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
