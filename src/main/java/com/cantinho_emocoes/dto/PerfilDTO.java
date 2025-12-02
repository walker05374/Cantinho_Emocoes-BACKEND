package com.cantinho_emocoes.dto;

import java.util.Set;

public record PerfilDTO(
    String nome,
    String email,
    String avatarUrl,
    int nivel,
    int xp,
    int metaXp, // Mantido para compatibilidade, podemos usar para gamificação futura
    Set<String> conquistas // Simplificado para Strings por enquanto
) {}