package com.cantinho_emocoes.repository;

import com.cantinho_emocoes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    
    // --- NOVO MÉTODO NECESSÁRIO PARA RECUPERAÇÃO DE SENHA ---
    Optional<Usuario> findByResetToken(String resetToken);
}