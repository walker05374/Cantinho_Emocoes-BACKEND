package com.example.cantinho_emocoes.dto;

import com.example.cantinho_emocoes.model.ModalidadeTipo;
import com.example.cantinho_emocoes.model.Perfil;

public record LoginResponseDTO(
    String token,
    String nome,
    Perfil perfil,
    ModalidadeTipo tipoAtividadeGerenciada,
    String avatarUrl
) {}