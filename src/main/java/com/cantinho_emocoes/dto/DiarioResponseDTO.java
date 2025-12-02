package com.cantinho_emocoes.dto;
import com.cantinho_emocoes.model.Emocao;
import java.time.LocalDateTime;

public record DiarioResponseDTO(
    Long id,
    Emocao emocao,
    int intensidade,
    String relato,
    LocalDateTime dataRegistro
) {}