package com.dragoonssoft.apps.caronacap;

/**
 * Created by wiler on 29/07/2017.
 */

public class Mensagem {

    private String msg, tipo, from;
    private boolean visto;
    private long horario;

    public Mensagem(){

    }

    public Mensagem(String msg, String tipo, boolean visto, long horario, String from) {
        this.msg = msg;
        this.tipo = tipo;
        this.visto = visto;
        this.horario = horario;
        this.from = from;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean getVisto() {
        return visto;
    }

    public void setVisto(boolean visto) {
        this.visto = visto;
    }

    public long getHorario() {
        return horario;
    }

    public void setHorario(long horario) {
        this.horario = horario;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
