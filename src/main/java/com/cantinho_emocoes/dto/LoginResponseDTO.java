package com.cantinho_emocoes.dto;

import com.cantinho_emocoes.model.Perfil;

public record LoginResponseDTO(
    String token,
    String nome,
    String email,
    Perfil role, // Mudamos de 'perfil' para 'role' para padronizar com o front
    String avatarUrl
) {}