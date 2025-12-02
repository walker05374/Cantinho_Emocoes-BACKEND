package com.cantinho_emocoes.dto;

// Este DTO é usado para receber a URL do avatar predefinido selecionado pelo usuário.
public class AvatarSelectionDTO {
    private String avatarUrl;

    // Getters e Setters são necessários para o Spring converter o JSON em objeto.
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
