package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.dto.*;
import com.cantinho_emocoes.model.*;
import com.cantinho_emocoes.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/responsavel")
@CrossOrigin(origins = "http://localhost:5173")
public class ResponsavelController {

    private final UsuarioRepository usuarioRepository;
    private final DiarioRepository diarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponsavelController(UsuarioRepository u, DiarioRepository dr, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = u;
        this.diarioRepository = dr;
        this.passwordEncoder = passwordEncoder;
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    // 1. Cadastrar Filho
    @PostMapping("/dependentes")
    public ResponseEntity<?> criarDependente(@RequestBody DependenteDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        
        Usuario filho = new Usuario();
        filho.setNome(dto.nome());
        filho.setDataNascimento(dto.dataNascimento());
        filho.setAvatarUrl(dto.avatarUrl());
        filho.setPerfil(Perfil.CRIANCA);
        filho.setResponsavel(pai);
        filho.setDataCadastro(LocalDate.now());
        
        // Gera senha aleatória interna
        filho.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        
        usuarioRepository.save(filho);
        return ResponseEntity.ok(Map.of("message", "Filho cadastrado com sucesso!"));
    }

    // 2. Listar Meus Filhos
    @GetMapping("/dependentes")
    public ResponseEntity<List<DependenteDTO>> listarDependentes(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        
        List<DependenteDTO> lista = pai.getDependentes().stream()
                .map(filho -> new DependenteDTO(
                    filho.getId(), 
                    filho.getNome(), 
                    filho.getDataNascimento(), 
                    "M", 
                    filho.getAvatarUrl()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(lista);
    }

    // 3. Validar PIN
    // CORREÇÃO: Retorna 200 OK com "valid: false" em vez de 403 Forbidden para não derrubar a sessão no frontend.
    @PostMapping("/validar-pin")
    public ResponseEntity<?> validarPin(@RequestBody Map<String, String> payload, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        String pinDigitado = payload.get("pin");

        if (pai.getPin() != null && passwordEncoder.matches(pinDigitado, pai.getPin())) {
            return ResponseEntity.ok(Map.of("valid", true));
        }
        
        // Alterado de status(403) para ok() para evitar o logout automático
        return ResponseEntity.ok(Map.of("valid", false, "error", "PIN incorreto."));
    }

    // 4. Dados para o Dashboard
    @GetMapping("/dependentes/{id}/dashboard")
    public ResponseEntity<?> getDadosGrafico(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario pai = getUsuario(userDetails.getUsername());
        
        Usuario filho = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Filho não encontrado"));

        if (filho.getResponsavel() == null || !filho.getResponsavel().getId().equals(pai.getId())) {
            return ResponseEntity.status(403).body("Acesso negado. Esta criança não é sua dependente.");
        }

        List<Diario> diarios = diarioRepository.findByDependenteIdOrderByDataRegistroDesc(id);

        // Filtra desenhos (CRIATIVO) para não quebrar o gráfico de emoções
        List<DiarioDTO> historicoGrafico = diarios.stream()
                .filter(d -> !"CRIATIVO".equalsIgnoreCase(d.getEmocao())) 
                .limit(20)
                .sorted((d1, d2) -> d1.getDataRegistro().compareTo(d2.getDataRegistro()))
                .map(d -> new DiarioDTO(
                    d.getId(), 
                    d.getEmocao(), 
                    d.getIntensidade(), 
                    d.getRelato(), 
                    d.getDesenhoBase64(),
                    d.getDataRegistro()
                ))
                .collect(Collectors.toList());

        // Mantém todos os registros (incluindo desenhos) na lista de atividades recentes
        List<DiarioDTO> ultimosRegistros = diarios.stream()
                .limit(5)
                .map(d -> new DiarioDTO(
                    d.getId(), 
                    d.getEmocao(), 
                    d.getIntensidade(), 
                    d.getRelato(), 
                    d.getDesenhoBase64(),
                    d.getDataRegistro()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "totalRegistros", diarios.size(), 
            "historicoGrafico", historicoGrafico,
            "ultimosRegistros", ultimosRegistros
        ));
    }
}