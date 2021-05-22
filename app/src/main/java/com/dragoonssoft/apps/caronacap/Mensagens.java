package com.dragoonssoft.apps.caronacap;

/**
 * Created by wiler on 29/07/2017.
 */

public class Mensagens {

    private String msg, tipo, from;
    private boolean visto;
    private String horario;

    public Mensagens(){

    }

    public Mensagens(String msg, String tipo, boolean visto, String horario) {
        this.msg = msg;
        this.tipo = tipo;
        this.visto = visto;
        this.horario = horario;
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

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
