package com.melihcelenk.seslekontrol;

public class Bolge {
    private int _id;
    private String _etiket;
    private String _macAdresi;
    private String _ipAdresi;

    public Bolge(){   }
    public Bolge(int id, String name, String _macAdresi, String _ipAdresi){
        this._id = id;
        this._etiket = name;
        this._macAdresi = _macAdresi;
        this._ipAdresi = _ipAdresi;
    }

    public Bolge(String name, String _macAdresi, String _ipAdresi){
        this._etiket = name;
        this._macAdresi = _macAdresi;
        this._ipAdresi = _ipAdresi;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_etiket() {
        return _etiket;
    }

    public void set_etiket(String _etiket) {
        this._etiket = _etiket;
    }

    public String get_ipAdresi() {
        return _ipAdresi;
    }

    public void set_ipAdresi(String _ipAdresi) {
        this._ipAdresi = _ipAdresi;
    }

    public String get_macAdresi() {
        return _macAdresi;
    }

    public void set_macAdresi(String _macAdresi) {
        this._macAdresi = _macAdresi;
    }


}
