package com.cantinho_emocoes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diarios")
public class Diario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String emocao; // FELIZ, TRISTE, BRAVO...

    private int intensidade; // 1 a 5
    
    @Column(columnDefinition = "TEXT")
    private String relato;

    // --- NOVO CAMPO: Armazena o desenho em formato Base64 (Texto longo) ---
    @Column(columnDefinition = "TEXT")
    private String desenhoBase64;

    private LocalDateTime dataRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependente_id", nullable = false)
    @JsonIgnore 
    private Usuario dependente;

    public Diario() {}

    public Diario(String emocao, int intensidade, String relato, LocalDateTime dataRegistro, Usuario dependente) {
        this.emocao = emocao;
        this.intensidade = intensidade;
        this.relato = relato;
        this.dataRegistro = dataRegistro;
        this.dependente = dependente;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmocao() { return emocao; }
    public void setEmocao(String emocao) { this.emocao = emocao; }
    
    public int getIntensidade() { return intensidade; }
    public void setIntensidade(int intensidade) { this.intensidade = intensidade; }
    
    public String getRelato() { return relato; }
    public void setRelato(String relato) { this.relato = relato; }
    
    public String getDesenhoBase64() { return desenhoBase64; }
    public void setDesenhoBase64(String desenhoBase64) { this.desenhoBase64 = desenhoBase64; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }
    
    public Usuario getDependente() { return dependente; }
    public void setDependente(Usuario dependente) { this.dependente = dependente; }
}