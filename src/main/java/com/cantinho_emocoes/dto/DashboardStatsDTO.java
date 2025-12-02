package com.cantinho_emocoes.dto;

import java.util.Map;

public record DashboardStatsDTO(
    Map<String, Long> emocoesSemanal, // Ex: "FELIZ": 5, "TRISTE": 2
    Map<String, Long> emocoesMensal,
    int totalRegistros
) {}