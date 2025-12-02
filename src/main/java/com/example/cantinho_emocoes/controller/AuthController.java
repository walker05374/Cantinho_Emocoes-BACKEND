package com.example.cantinho_emocoes.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.cantinho_emocoes.dto.*;
import com.example.cantinho_emocoes.model.Perfil;
import com.example.cantinho_emocoes.model.Usuario;
import com.example.cantinho_emocoes.security.JwtService;
import com.example.cantinho_emocoes.service.UsuarioService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    // Adicionado um logger para registrar erros de forma segura
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UsuarioService usuarioService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UsuarioRequestDTO request) {
        try {
            Usuario novoUsuario = usuarioService.criarUsuario(request);
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
                usuarioService.atualizarAvatarUrl(novoUsuario.getEmail(), request.getAvatarUrl());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Usuário registrado com sucesso: " + novoUsuario.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
            Usuario usuario = (Usuario) authentication.getPrincipal();
            String token = jwtService.generateToken(usuario);
            String avatarUrlResponse = usuarioService.construirAvatarUrl(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(
                token,
                usuario.getNome(),
                usuario.getPerfil(),
                usuario.getPerfil() == Perfil.PROFESSOR ? usuario.getTipoAtividadeGerenciada() : null,
                avatarUrlResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciais inválidas."));
        }
    }

    @GetMapping("/meu-perfil")
    public ResponseEntity<PerfilDTO> getMeuPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = userDetails.getUsername();
        PerfilDTO perfilDTO = usuarioService.getPerfilDoUsuario(email);
        return ResponseEntity.ok(perfilDTO);
    }

    @PutMapping("/meu-perfil/avatar")
    public ResponseEntity<?> updateAvatar(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> payload) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String userEmail = userDetails.getUsername();
            String newAvatarUrl = payload.get("avatarUrl");
            usuarioService.atualizarAvatarUrl(userEmail, newAvatarUrl);
            return ResponseEntity.ok(Map.of(
                "message", "Avatar atualizado com sucesso",
                "avatarUrl", newAvatarUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Falha ao atualizar o avatar."));
        }
    }

    // ========================================================================
    // == ✅ INÍCIO DA SEÇÃO DE RECUPERAÇÃO DE SENHA ADICIONADA E CORRIGIDA ✅ ==
    // ========================================================================

    /**
     * Endpoint para solicitar a recuperação de senha.
     * Sempre retorna uma mensagem genérica de sucesso para segurança.
     */
    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> solicitarRecuperacaoSenha(@RequestBody ForgotPasswordRequest request) {
        try {
            // Supondo que seu serviço tenha o método 'requestPasswordReset'
            usuarioService.requestPasswordReset(request.getEmail());
        } catch (Exception e) {
            // O erro real é registrado apenas no log do servidor, nunca exposto ao cliente.
            log.error("Falha na solicitação de recuperação de senha para o e-mail: {}", request.getEmail(), e);
        }
        // A resposta para o cliente é sempre a mesma, evitando que se descubra e-mails válidos.
        return ResponseEntity.ok(Map.of("message", "Se um usuário com o e-mail informado existir, um link para redefinição de senha foi enviado."));
    }

    /**
     * Endpoint para efetivamente redefinir a senha com um token válido.
     */
    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetarSenha(@RequestBody ResetPasswordRequest request) {
        try {
            // Supondo que seu serviço tenha o método 'resetPassword' que lança uma exceção em caso de falha.
            usuarioService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
        
        } catch (RuntimeException e) { 
             // Captura erros esperados (ex: token inválido) e retorna a mensagem de erro do serviço.
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura erros inesperados do servidor.
            log.error("Erro inesperado ao redefinir a senha com o token: {}", request.getToken(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocorreu um erro interno ao redefinir a senha."));
        }
    }
}