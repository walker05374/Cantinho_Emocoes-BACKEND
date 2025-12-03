package com.cantinho_emocoes.config;

import com.cantinho_emocoes.model.Perfil;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String emailAdmin = "admin@admin.com";
            Optional<Usuario> adminExistente = usuarioRepository.findByEmail(emailAdmin);

            if (adminExistente.isEmpty()) {
                // --- CRIAÇÃO DO ZERO ---
                Usuario admin = new Usuario();
                admin.setNome("Administrador Principal");
                admin.setEmail(emailAdmin);
                admin.setSenha(passwordEncoder.encode("admin123"));
                admin.setPerfil(Perfil.ADMINISTRADOR);
                admin.setDataCadastro(LocalDate.now());
                admin.setAvatarUrl("https://ui-avatars.com/api/?name=Admin&background=0D8ABC&color=fff");
                admin.setPin(passwordEncoder.encode("0000"));

                usuarioRepository.save(admin);
                System.out.println("✅ ADMIN CRIADO.");
            } else {
                // --- ATUALIZAÇÃO FORÇADA DE SENHA (PARA CORRIGIR SEU ERRO 401) ---
                Usuario admin = adminExistente.get();
                // Verifica se é admin mesmo, senão força ser
                if (admin.getPerfil() != Perfil.ADMINISTRADOR) {
                    admin.setPerfil(Perfil.ADMINISTRADOR);
                }
                // Reseta a senha para garantir
                admin.setSenha(passwordEncoder.encode("admin123"));
                usuarioRepository.save(admin);
                System.out.println("⚠️ ADMIN JÁ EXISTIA: SENHA E PERFIL FORAM RESETADOS PARA O PADRÃO.");
            }
        };
    }
}