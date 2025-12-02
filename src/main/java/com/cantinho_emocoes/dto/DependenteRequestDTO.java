package com.cantinho_emocoes.dto;

import java.time.LocalDate;

public record DependenteRequestDTO(
    String nome,
    LocalDate dataNascimento,
    String avatarUrl
) {}