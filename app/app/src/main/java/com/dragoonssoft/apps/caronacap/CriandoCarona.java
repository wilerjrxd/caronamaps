package com.dragoonssoft.apps.caronacap;

/**
 * Created by wiler on 02/06/2017.
 */

public class CriandoCarona {
    private String nome, partidaDestino, horario, vagas;

    public CriandoCarona(String nome, String partidaDestino, String horario, String vagas) {
        this.nome = nome;
        this.partidaDestino = partidaDestino;
        this.horario = horario;
        this.vagas = vagas;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPartidaDestino() {
        return partidaDestino;
    }

    public void setPartidaDestino(String partidaDestino) {
        this.partidaDestino = partidaDestino;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getVagas() {
        return vagas;
    }

    public void setVagas(String vagas) {
        this.vagas = vagas;
    }
}
