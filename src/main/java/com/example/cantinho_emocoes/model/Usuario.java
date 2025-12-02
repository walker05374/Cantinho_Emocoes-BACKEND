package com.example.cantinho_emocoes.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... (todos os seus campos existentes: nome, email, etc.)
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Column(unique = true)
    private String matricula;

    @Column
    private String codigoDisciplina;

    @Enumerated(EnumType.STRING)
    private ModalidadeTipo tipoAtividadeGerenciada;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Certificado> certificados;

    @OneToMany(mappedBy = "professorRevisor", fetch = FetchType.LAZY)
    private List<Certificado> certificadosRevisados;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Aviso> avisos;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuarios_avisos_lidos",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "aviso_id")
    )
    private Set<Aviso> avisosLidos = new HashSet<>();

    @Column(name = "reset_token", unique = true)
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Column(name = "avatar_filename")
    private String avatarFilename;

    @Column(name = "xp")
    private int xp = 0;

    @Column(name = "nivel")
    private int nivel = 1;
    
    // --- CAMPO ADICIONADO PARA PROFESSORES ---
    @Column(name = "total_revisoes")
    private int totalRevisoes = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_medalhas",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "medalha_id")
    )
    private Set<Medalha> medalhas = new HashSet<>();

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public void setCodigoDisciplina(String codigoDisciplina) { this.codigoDisciplina = codigoDisciplina; }
    public ModalidadeTipo getTipoAtividadeGerenciada() { return tipoAtividadeGerenciada; }
    public void setTipoAtividadeGerenciada(ModalidadeTipo tipoAtividadeGerenciada) { this.tipoAtividadeGerenciada = tipoAtividadeGerenciada; }
    public List<Certificado> getCertificados() { return certificados; }
    public void setCertificados(List<Certificado> certificados) { this.certificados = certificados; }
    public List<Certificado> getCertificadosRevisados() { return certificadosRevisados; }
    public void setCertificadosRevisados(List<Certificado> certificadosRevisados) { this.certificadosRevisados = certificadosRevisados; }
    public List<Aviso> getAvisos() { return avisos; }
    public void setAvisos(List<Aviso> avisos) { this.avisos = avisos; }
    public Set<Aviso> getAvisosLidos() { return avisosLidos; }
    public void setAvisosLidos(Set<Aviso> avisosLidos) { this.avisosLidos = avisosLidos; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }
    public String getAvatarFilename() { return avatarFilename; }
    public void setAvatarFilename(String avatarFilename) { this.avatarFilename = avatarFilename; }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    public Set<Medalha> getMedalhas() { return medalhas; }
    public void setMedalhas(Set<Medalha> medalhas) { this.medalhas = medalhas; }


    public int getTotalRevisoes() { return totalRevisoes; }
    public void setTotalRevisoes(int totalRevisoes) { this.totalRevisoes = totalRevisoes; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.perfil.name()));
    }
    @Override
    public String getPassword() { return this.senha; }
    @Override
    public String getUsername() { return this.email; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
