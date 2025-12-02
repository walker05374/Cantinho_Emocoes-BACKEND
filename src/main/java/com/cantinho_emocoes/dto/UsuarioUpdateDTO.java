package com.cantinho_emocoes.dto;

public class UsuarioUpdateDTO {
    private String nome;

    public UsuarioUpdateDTO() {
    }

    public UsuarioUpdateDTO(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}