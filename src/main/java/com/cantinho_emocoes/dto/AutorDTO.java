package com.cantinho_emocoes.dto;

public class AutorDTO {
    private String nome;

    // Construtores
    public AutorDTO() {
    }

    public AutorDTO(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}