package com.cantinho_emocoes.service;

import com.cantinho_emocoes.dto.AdminUsuarioDTO; 
import com.cantinho_emocoes.dto.DependenteRequestDTO;
import com.cantinho_emocoes.dto.PerfilDTO;
import com.cantinho_emocoes.model.Perfil;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors; 

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GmailEmailService emailService;
    
    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, GmailEmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public PerfilDTO getPerfilDoUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return new PerfilDTO(
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getAvatarUrl(),
            1, // Nivel fictício
            0, // XP fictício
            100,
            Collections.emptySet()
        );
    }

    // --- GESTÃO DE DEPENDENTES ---

    @Transactional
    public Usuario criarDependente(Usuario responsavel, DependenteRequestDTO dto) {
        Usuario crianca = new Usuario();
        crianca.setNome(dto.nome());
        
        // Gera senha aleatória pois a criança usa o login do pai
        crianca.setSenha(passwordEncoder.encode(UUID.randomUUID().toString())); 
        
        crianca.setPerfil(Perfil.CRIANCA);
        
        crianca.setAvatarUrl(dto.avatarUrl());
        crianca.setDataNascimento(dto.dataNascimento());
        crianca.setResponsavel(responsavel);
        crianca.setDataCadastro(LocalDate.now());

        return usuarioRepository.save(crianca);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarDependentes(Usuario responsavel) {
        return responsavel.getDependentes();
    }
    
    public boolean validarPin(Usuario usuario, String pinDigitado) {
        if (usuario.getPin() == null) return false;
        return passwordEncoder.matches(pinDigitado, usuario.getPin());
    }

    // --- RECUPERAÇÃO DE SENHA ---

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(usuario);

            String resetLink = frontendBaseUrl + "/resetar-senha?token=" + token;
            emailService.sendPasswordResetEmail(usuario.getEmail(), usuario.getNome(), resetLink);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByResetToken(token)
            .orElseThrow(() -> new RuntimeException("Token inválido."));
            
        if (usuario.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
             throw new RuntimeException("Token expirado.");
        }

        usuario.setSenha(passwordEncoder.encode(newPassword));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiresAt(null);
        usuarioRepository.save(usuario);
    }
    
    @Transactional
    public void atualizarNome(String email, String novoNome) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        
        usuario.setNome(novoNome);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluirContaFamilia(Usuario responsavel) {
        usuarioRepository.delete(responsavel);
    }

    @Transactional
    public void excluirContaFamilia(String email) {
        Usuario responsavel = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        usuarioRepository.delete(responsavel);
    }

    // --- NOVOS MÉTODOS PARA O ADMINISTRADOR (CORRIGIDOS) ---

    @Transactional(readOnly = true)
    public List<AdminUsuarioDTO> listarTodosUsuarios() {
        // Busca todos os usuários do banco
        List<Usuario> todos = usuarioRepository.findAll();
        
        // Filtra apenas quem NÃO tem responsável (ou seja, Pais e Admins)
        // Isso evita que as crianças apareçam soltas na lista principal
        return todos.stream()
                .filter(u -> u.getResponsavel() == null) 
                .map(this::converterParaAdminDTO)
                .collect(Collectors.toList());
    }

    // Método auxiliar para converter e preencher os filhos recursivamente
    private AdminUsuarioDTO converterParaAdminDTO(Usuario u) {
        // Converte a lista de dependentes (filhos) para DTO também
        List<AdminUsuarioDTO> filhosDTO = u.getDependentes().stream()
                .map(this::converterParaAdminDTO) // Recursão simples
                .collect(Collectors.toList());

        return new AdminUsuarioDTO(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getPerfil(),
                u.getDataCadastro(),
                filhosDTO // Passa a lista de filhos para ficar aninhado
        );
    }
    
    @Transactional
    public void atualizarAvatar(String email, String novaAvatarUrl) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        
        usuario.setAvatarUrl(novaAvatarUrl);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletarUsuarioPeloAdmin(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com ID: " + id));

        // --- TRAVA DE SEGURANÇA: Não permitir excluir contas de Administrador ---
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) {
            throw new RuntimeException("Não é permitido excluir contas de Administrador.");
        }

        usuarioRepository.delete(usuario);
    }
}