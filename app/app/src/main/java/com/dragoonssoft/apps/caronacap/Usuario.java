package com.dragoonssoft.apps.caronacap;

import android.net.Uri;

/**
 * Created by wiler on 04/06/2017.
 */

public class Usuario {
    String id,nome,matricula,telefone, cadastrado;
    String imgURL, status, device_token, registrado_desde;

    public Usuario(){

    }

    public Usuario(String id, String nome, String matricula, String telefone, String cadastrado, String imgURL, String status, String device_token, String registrado_desde) {
        this.id = id;
        this.nome = nome;
        this.matricula = matricula;
        this.telefone = telefone;
        this.cadastrado = cadastrado;
        this.imgURL = imgURL;
        this.status = status;
        this.device_token = device_token;
        this.registrado_desde = registrado_desde;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCadastrado() {
        return cadastrado;
    }

    public void setCadastrado(String cadastrado) {
        this.cadastrado = cadastrado;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getRegistrado_desde() {
        return registrado_desde;
    }

    public void setRegistrado_desde(String registrado_desde) {
        this.registrado_desde = registrado_desde;
    }
}
