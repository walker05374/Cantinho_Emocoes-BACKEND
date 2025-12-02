package com.cantinho_emocoes.dto;

import java.time.LocalDate;

public record DependenteResponseDTO(
    Long id,
    String nome,
    String avatarUrl,
    LocalDate dataNascimento,
    int nivel,
    int xp
) {}