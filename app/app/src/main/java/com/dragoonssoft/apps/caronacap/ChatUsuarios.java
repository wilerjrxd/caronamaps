package com.dragoonssoft.apps.caronacap;

/**
 * Created by wiler on 09/07/2017.
 */

public class ChatUsuarios {
    Boolean visto;
    String horario, nome, img, telefone, idOutroUsuario;
    long horarioinv;

    public ChatUsuarios(){

    }

    public ChatUsuarios(Boolean visto, String horario, String nome, String img, String telefone, String idOutroUsuario, long horarioinv) {
        this.visto = visto;
        this.horario = horario;
        this.nome = nome;
        this.img = img;
        this.telefone = telefone;
        this.idOutroUsuario = idOutroUsuario;
        this.horarioinv = horarioinv;
    }

    public Boolean getVisto() {
        return visto;
    }

    public void setVisto(Boolean visto) {
        this.visto = visto;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
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

    public String getIdOutroUsuario() {
        return idOutroUsuario;
    }

    public void setIdOutroUsuario(String idOutroUsuario) {
        this.idOutroUsuario = idOutroUsuario;
    }

    public long getHorarioinv() {
        return horarioinv;
    }

    public void setHorarioinv(long horarioinv) {
        this.horarioinv = horarioinv;
    }
}
