package com.cantinho_emocoes.dto;
import java.time.LocalDate;

public record DependenteDTO(
    Long id,
    String nome,
    LocalDate dataNascimento,
    String genero,
    String avatarUrl
) {}