package com.melihcelenk.seslekontrol.modeller;

import java.util.ArrayList;

public class Esya {
    private int _esyaId;
    private int _bolgeId;
    private String _esyaAdi;
    private ArrayList<String> _esyaAnahtarKelimeler;

    public Esya(){ }
    public Esya(int _bolgeId, String _esyaAdi) {
        this._bolgeId = _bolgeId;
        this._esyaAdi = _esyaAdi;
        _esyaAnahtarKelimeler = new ArrayList<String>();
    }

    public int get_esyaId() {
        return _esyaId;
    }

    public void set_esyaId(int _esyaId) {
        this._esyaId = _esyaId;
    }

    public int get_bolgeId() {
        return _bolgeId;
    }

    public void set_bolgeId(int _bolgeId) {
        this._bolgeId = _bolgeId;
    }

    public String get_esyaAdi() {
        return _esyaAdi;
    }

    public void set_esyaAdi(String _esyaAdi) {
        this._esyaAdi = _esyaAdi;
    }


    public ArrayList<String> get_esyaAnahtarKelimeler() {
        return _esyaAnahtarKelimeler;
    }
    public String get_esyaAnahtarKelimelerString() {
        String hepsi="";
        try{
            for(String s : get_esyaAnahtarKelimeler()){
                hepsi+= s + " ";
            }
        }
        catch(Exception e){

        }

        return hepsi;
    }

    public void set_esyaAnahtarKelimeler(ArrayList<String> _esyaAnahtarKelimeler) {
        this._esyaAnahtarKelimeler = _esyaAnahtarKelimeler;
    }
}
