package com.cantinho_emocoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cantinho_emocoes.model.Notificacao;
import com.cantinho_emocoes.model.Usuario;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByUsuarioOrderByDataEnvioDesc(Usuario usuario);
    List<Notificacao> findByUsuarioAndLidoFalseOrderByDataEnvioDesc(Usuario usuario);
}