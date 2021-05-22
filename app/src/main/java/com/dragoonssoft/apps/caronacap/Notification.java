package com.dragoonssoft.apps.caronacap;

/**
 * Created by wiler on 26/07/2017.
 */

public class Notification {

    String from, type, nome, img, telefone, key;
    long horario;

    public Notification(){

    }

    public Notification(String from, String type, String nome, String img, String telefone, String key, long horario) {
        this.from = from;
        this.type = type;
        this.nome = nome;
        this.img = img;
        this.telefone = telefone;
        this.key = key;
        this.horario = horario;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getHorario() {
        return horario;
    }

    public void setHorario(long horario) {
        this.horario = horario;
    }
}
