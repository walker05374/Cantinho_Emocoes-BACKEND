package com.example.cantinho_emocoes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cantinho_emocoes.dto.MedalhaDTO;
import com.example.cantinho_emocoes.dto.PerfilDTO;
import com.example.cantinho_emocoes.dto.UsuarioRequestDTO;
import com.example.cantinho_emocoes.model.Perfil;
import com.example.cantinho_emocoes.model.Usuario;
import com.example.cantinho_emocoes.repository.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int META_XP_ALUNO = 200;

    // Dependências para a recuperação de senha
    private final GmailEmailService emailService;
    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    // Construtor completo com todas as dependências
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, GmailEmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ==========================================================
    // == INÍCIO DO SEU CÓDIGO ORIGINAL (MANTIDO INTACTO) ==
    // ==========================================================
    @Transactional
    public Usuario criarUsuario(UsuarioRequestDTO request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("O e-mail fornecido já está em uso.");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.getNome());
        novoUsuario.setEmail(request.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(request.getSenha()));
        novoUsuario.setPerfil(request.getPerfil());
        novoUsuario.setDataCadastro(LocalDate.now());

        if (request.getAvatarUrl() == null || request.getAvatarUrl().trim().isEmpty()) {
            String nomeParaSeed = novoUsuario.getNome().replaceAll("\\s+", "");
            if (request.getPerfil() == Perfil.PROFESSOR) {
                novoUsuario.setAvatarFilename("https://api.dicebear.com/8.x/micah/svg?seed=" + nomeParaSeed);
            } else {
                novoUsuario.setAvatarFilename("https://api.dicebear.com/8.x/adventurer/svg?seed=" + nomeParaSeed);
            }
        } else {
            novoUsuario.setAvatarFilename(request.getAvatarUrl());
        }

        if (request.getPerfil() == Perfil.ALUNO) {
            novoUsuario.setMatricula(request.getMatricula());
        } else if (request.getPerfil() == Perfil.PROFESSOR) {
            novoUsuario.setCodigoDisciplina(request.getCodigoDisciplina());
            novoUsuario.setTipoAtividadeGerenciada(request.getTipoAtividadeGerenciada());
        }

        return usuarioRepository.save(novoUsuario);
    }

    @Transactional(readOnly = true)
    public PerfilDTO getPerfilDoUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        Set<MedalhaDTO> medalhasDTO = usuario.getMedalhas().stream()
                .map(m -> new MedalhaDTO(m.getNome(), m.getDescricao(), m.getImagemUrl()))
                .collect(Collectors.toSet());

        String avatarUrl = construirAvatarUrl(usuario);

        return new PerfilDTO(
            usuario.getNome(),
            usuario.getEmail(),
            avatarUrl,
            usuario.getNivel(),
            usuario.getXp(),
            META_XP_ALUNO,
            medalhasDTO
        );
    }

    @Transactional
    public void atualizarAvatarUrl(String email, String avatarUrl) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        usuario.setAvatarFilename(avatarUrl);
        usuarioRepository.save(usuario);
    }

    public String construirAvatarUrl(Usuario usuario) {
        String filename = usuario.getAvatarFilename();
        if (filename == null || filename.trim().isEmpty() || "null".equalsIgnoreCase(filename)) {
            return null;
        }
        if (filename.startsWith("http")) {
            return filename;
        }
        if (filename.startsWith("/avatares")) {
             return filename;
        }
        return null;
    }
    // ==========================================================
    // == FIM DO SEU CÓDIGO ORIGINAL ==
    // ==========================================================


    // ==========================================================
    // == INÍCIO DOS MÉTODOS DE RECUPERAÇÃO DE SENHA ==
    // ==========================================================
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
            .orElseThrow(() -> new RuntimeException("Token de redefinição inválido ou não encontrado."));

        if (usuario.getResetTokenExpiresAt() == null || usuario.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            usuario.setResetToken(null);
            usuario.setResetTokenExpiresAt(null);
            usuarioRepository.save(usuario);
            throw new RuntimeException("Seu token de redefinição de senha expirou. Por favor, solicite um novo.");
        }

        usuario.setSenha(passwordEncoder.encode(newPassword));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiresAt(null);
        usuarioRepository.save(usuario);
    }
    // ==========================================================
    // == FIM DOS MÉTODOS DE RECUPERAÇÃO DE SENHA ==
    // ==========================================================
}