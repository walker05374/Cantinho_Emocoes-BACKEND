package com.cantinho_emocoes.dto;

import com.cantinho_emocoes.model.Perfil;
import java.time.LocalDate;
import java.util.List;

public record AdminUsuarioDTO(
    Long id,
    String nome,
    String email,
    Perfil perfil,
    LocalDate dataCadastro,
    List<AdminUsuarioDTO> dependentes // Novo campo para aninhamento
) {}