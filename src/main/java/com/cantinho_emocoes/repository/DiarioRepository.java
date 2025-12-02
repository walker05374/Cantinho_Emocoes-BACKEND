package com.cantinho_emocoes.repository;

import com.cantinho_emocoes.model.Diario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiarioRepository extends JpaRepository<Diario, Long> {
    // Busca o diário de uma criança específica, ordenado do mais recente
    List<Diario> findByDependenteIdOrderByDataRegistroDesc(Long dependenteId);
}