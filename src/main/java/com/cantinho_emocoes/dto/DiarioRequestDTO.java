package com.cantinho_emocoes.dto;
import com.cantinho_emocoes.model.Emocao;

public record DiarioRequestDTO(
    Emocao emocao,
    int intensidade,
    String relato,
    Long criancaId // Opcional: Se o pai estiver registrando pelo filho
) {}