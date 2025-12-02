package com.example.cantinho_emocoes.dto;

import java.util.Set;

public record PerfilDTO(
    String nome,
    String email,
    String avatarUrl,
    int nivel,
    int xp,
    int metaXp,
    Set<MedalhaDTO> medalhas
) {}

