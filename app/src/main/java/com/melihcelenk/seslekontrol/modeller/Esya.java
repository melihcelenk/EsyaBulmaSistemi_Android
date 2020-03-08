package com.melihcelenk.seslekontrol.modeller;

public class Esya {
    private int _esyaId;
    private int _bolgeId;
    private String _esyaAdi;

    public Esya(){ }
    public Esya(int _bolgeId, String _esyaAdi) {
        this._bolgeId = _bolgeId;
        this._esyaAdi = _esyaAdi;
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


}
