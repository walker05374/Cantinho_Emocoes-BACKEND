package com.cantinho_emocoes.controller;

import com.cantinho_emocoes.model.Diario;
import com.cantinho_emocoes.model.Usuario;
import com.cantinho_emocoes.repository.DiarioRepository;
import com.cantinho_emocoes.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diario")
public class DiarioController {

    private final DiarioRepository diarioRepository;
    private final UsuarioRepository usuarioRepository;

    public DiarioController(DiarioRepository diarioRepository, UsuarioRepository usuarioRepository) {
        this.diarioRepository = diarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // LISTAR DIÁRIOS
    @GetMapping("/meus")
    public ResponseEntity<?> listarMeusDiarios(
            @RequestHeader("x-child-id") Long childId, 
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (childId == null) return ResponseEntity.badRequest().body("ID da criança não informado.");
        
        List<Diario> diarios = diarioRepository.findByDependenteIdOrderByDataRegistroDesc(childId);
        return ResponseEntity.ok(diarios);
    }

    // SALVAR TEXTO/EMOÇÃO
    @PostMapping
    public ResponseEntity<?> salvarDiario(
            @RequestHeader("x-child-id") Long childId,
            @RequestBody Map<String, Object> payload) {

        Usuario crianca = usuarioRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Criança não encontrada"));

        Diario diario = new Diario();
        diario.setEmocao((String) payload.get("emocao"));
        diario.setIntensidade((Integer) payload.get("intensidade"));
        diario.setRelato((String) payload.get("relato"));
        diario.setDataRegistro(LocalDateTime.now());
        diario.setDependente(crianca);

        diarioRepository.save(diario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Diário salvo com sucesso!"));
    }

    // --- CORREÇÃO AQUI: SALVAR DESENHO ---
    @PostMapping("/desenho")
    public ResponseEntity<?> salvarDesenho(
            @RequestHeader("x-child-id") Long childId,
            @RequestBody Map<String, String> payload) {

        Usuario crianca = usuarioRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Criança não encontrada"));

        String imagemBase64 = payload.get("imagem");

        // Cria SEMPRE um novo registro para garantir que múltiplos desenhos no mesmo dia sejam salvos
        Diario novoDiario = new Diario();
        
        // Define como atividade criativa (neutra, não conta como Triste/Feliz no gráfico se filtrado)
        novoDiario.setEmocao("CRIATIVO"); 
        
        // Intensidade apenas para registro
        novoDiario.setIntensidade(5); 
        
        // Relato simples e bonito, sem avisos estranhos
        novoDiario.setRelato("Um lindo desenho!"); 
        
        novoDiario.setDesenhoBase64(imagemBase64);
        novoDiario.setDataRegistro(LocalDateTime.now());
        novoDiario.setDependente(crianca);
        
        diarioRepository.save(novoDiario);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Desenho salvo na galeria!"));
    }
}