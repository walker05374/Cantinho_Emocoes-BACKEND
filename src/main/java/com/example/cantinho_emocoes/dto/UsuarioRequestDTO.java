package com.example.cantinho_emocoes.dto;

import com.example.cantinho_emocoes.model.ModalidadeTipo;
import com.example.cantinho_emocoes.model.Perfil;

public class UsuarioRequestDTO {
    private String nome;
    private String email;
    private String senha;
    private Perfil perfil;
    private String matricula;
    private String codigoDisciplina;
    private ModalidadeTipo tipoAtividadeGerenciada;
    private String avatarUrl;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public void setCodigoDisciplina(String codigoDisciplina) { this.codigoDisciplina = codigoDisciplina; }
    public ModalidadeTipo getTipoAtividadeGerenciada() { return tipoAtividadeGerenciada; }
    public void setTipoAtividadeGerenciada(ModalidadeTipo tipoAtividadeGerenciada) { this.tipoAtividadeGerenciada = tipoAtividadeGerenciada; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}