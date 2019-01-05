package com.example.familia.exemplofirebasestorage.model;

import com.example.familia.exemplofirebasestorage.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class Usuario {

    private String id;
    private String nome;
    private String id_foto;
    private String url_foto;

    public Usuario() {
    }

    public void salvar(){
        DatabaseReference reference = ConfiguracaoFirebase.getDatabase().child("usuario_imagem").child(getId());
        reference.setValue(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Deprecated
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Deprecated
    public String getId_foto() {
        return id_foto;
    }

    public void setId_foto(String id_foto) {
        this.id_foto = id_foto;
    }

    @Deprecated
    public String getUrl_foto() {
        return url_foto;
    }

    public void setUrl_foto(String url_foto) {
        this.url_foto = url_foto;
    }
}