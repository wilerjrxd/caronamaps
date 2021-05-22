package com.dragoonssoft.apps.caronacap;

/**
 * Created by wiler on 01/06/2017.
 */

public class CaronaInfo {
    private String idusuario, nome, horario, vagas, partida, destino, partidaDestino, data, img, situacao,
            parada1, parada2,
            idCaroneiro1, nomeCaroneiro1, imgCaroneiro1, idCaroneiro2, nomeCaroneiro2, imgCaroneiro2,
            idCaroneiro3, nomeCaroneiro3, imgCaroneiro3, idCaroneiro4, nomeCaroneiro4, imgCaroneiro4,
            preferencias;

    public CaronaInfo(){

    }

    public CaronaInfo(String idusuario, String nome, String horario, String vagas, String partida, String destino, String parada1, String parada2, String partidaDestino, String data, String img, String situacao, String idCaroneiro1, String nomeCaroneiro1, String imgCaroneiro1, String idCaroneiro2, String nomeCaroneiro2, String imgCaroneiro2, String idCaroneiro3, String nomeCaroneiro3, String imgCaroneiro3, String idCaroneiro4, String nomeCaroneiro4, String imgCaroneiro4, String preferencias) {
        this.idusuario = idusuario;
        this.nome = nome;
        this.horario = horario;
        this.vagas = vagas;
        this.partida = partida;
        this.destino = destino;
        this.parada1 = parada1;
        this.parada2 = parada2;
        this.partidaDestino = partidaDestino;
        this.data = data;
        this.img = img;
        this.situacao = situacao;
        this.idCaroneiro1 = idCaroneiro1;
        this.nomeCaroneiro1 = nomeCaroneiro1;
        this.imgCaroneiro1 = imgCaroneiro1;
        this.idCaroneiro2 = idCaroneiro2;
        this.nomeCaroneiro2 = nomeCaroneiro2;
        this.imgCaroneiro2 = imgCaroneiro2;
        this.idCaroneiro3 = idCaroneiro3;
        this.nomeCaroneiro3 = nomeCaroneiro3;
        this.imgCaroneiro3 = imgCaroneiro3;
        this.idCaroneiro4 = idCaroneiro4;
        this.nomeCaroneiro4 = nomeCaroneiro4;
        this.imgCaroneiro4 = imgCaroneiro4;
        this.preferencias = preferencias;
    }

    public String getPartidaDestino() {
        return partidaDestino;
    }

    public void setPartidaDestino(String partidaDestino) {
        this.partidaDestino = partidaDestino;
    }

    public String getIdcaroneiro1() {
        return idCaroneiro1;
    }

    public void setIdcaroneiro1(String idCaroneiro1) {
        this.idCaroneiro1 = idCaroneiro1;
    }

    public String getNomecaroneiro1() {
        return nomeCaroneiro1;
    }

    public void setNomecaroneiro1(String nomeCaroneiro1) {
        this.nomeCaroneiro1 = nomeCaroneiro1;
    }

    public String getImgcaroneiro1() {
        return imgCaroneiro1;
    }

    public void setImgcaroneiro1(String imgCaroneiro1) {
        this.imgCaroneiro1 = imgCaroneiro1;
    }

    public String getIdcaroneiro2() {
        return idCaroneiro2;
    }

    public void setIdcaroneiro2(String idCaroneiro2) {
        this.idCaroneiro2 = idCaroneiro2;
    }

    public String getNomecaroneiro2() {
        return nomeCaroneiro2;
    }

    public void setNomecaroneiro2(String nomeCaroneiro2) {
        this.nomeCaroneiro2 = nomeCaroneiro2;
    }

    public String getImgcaroneiro2() {
        return imgCaroneiro2;
    }

    public void setImgcaroneiro2(String imgCaroneiro2) {
        this.imgCaroneiro2 = imgCaroneiro2;
    }

    public String getIdcaroneiro3() {
        return idCaroneiro3;
    }

    public void setIdcaroneiro3(String idCaroneiro3) {
        this.idCaroneiro3 = idCaroneiro3;
    }

    public String getNomecaroneiro3() {
        return nomeCaroneiro3;
    }

    public void setNomecaroneiro3(String nomeCaroneiro3) {
        this.nomeCaroneiro3 = nomeCaroneiro3;
    }

    public String getImgcaroneiro3() {
        return imgCaroneiro3;
    }

    public void setImgcaroneiro3(String imgCaroneiro3) {
        this.imgCaroneiro3 = imgCaroneiro3;
    }

    public String getIdcaroneiro4() {
        return idCaroneiro4;
    }

    public void setIdcaroneiro4(String idCaroneiro4) {
        this.idCaroneiro4 = idCaroneiro4;
    }

    public String getNomecaroneiro4() {
        return nomeCaroneiro4;
    }

    public void setNomecaroneiro4(String nomeCaroneiro4) {
        this.nomeCaroneiro4 = nomeCaroneiro4;
    }

    public String getImgcaroneiro4() {
        return imgCaroneiro4;
    }

    public void setImgcaroneiro4(String imgCaroneiro4) {
        this.imgCaroneiro4 = imgCaroneiro4;
    }

    public String getIdusuario() {
        return idusuario;
    }

    public void setIdusuario(String idusuario) {
        this.idusuario = idusuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getPartida() {
        return partida;
    }

    public void setPartida(String partida) {
        this.partida = partida;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getParada1() {
        return parada1;
    }

    public void setParada1(String parada1) {
        this.parada1 = parada1;
    }

    public String getParada2() {
        return parada2;
    }

    public void setParada2(String parada2) {
        this.parada2 = parada2;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getPreferencias() {
        return preferencias;
    }

    public void setPreferencias(String preferencias) {
        this.preferencias = preferencias;
    }
}