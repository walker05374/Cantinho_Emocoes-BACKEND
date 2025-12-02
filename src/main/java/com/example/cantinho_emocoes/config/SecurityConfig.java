package com.example.cantinho_emocoes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.cantinho_emocoes.security.JwtAuthFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(AuthenticationProvider authenticationProvider, JwtAuthFilter jwtAuthFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // --- Rotas Públicas ---
                .requestMatchers("/auth/**", "/api/health").permitAll()

                // --- Rotas de Usuários (ADMIN) ---
                // ✅ SEGURANÇA: Adiciona a regra para proteger as rotas de usuários
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                
                // --- Rotas de Certificados ---
                .requestMatchers(HttpMethod.GET, "/api/certificados/tipos").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/certificados/enviar").hasRole("ALUNO")
                .requestMatchers(HttpMethod.GET, "/api/certificados/meus").hasRole("ALUNO")
                .requestMatchers(HttpMethod.PUT, "/api/certificados/meus/{id}").hasRole("ALUNO")
                .requestMatchers(HttpMethod.DELETE, "/api/certificados/meus/{id}").hasRole("ALUNO")
                .requestMatchers(HttpMethod.GET, "/api/certificados/revisao-professor").hasRole("PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/api/certificados/revisoes-concluidas-professor/contagem").hasRole("PROFESSOR")
                .requestMatchers(HttpMethod.PUT, "/api/certificados/{id}/revisar").hasRole("PROFESSOR") // O path aqui estava errado na sua versão original
                .requestMatchers(HttpMethod.GET, "/api/certificados/{id}").authenticated()

                // --- Rotas de Avisos ---
                .requestMatchers(HttpMethod.GET, "/api/avisos").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/avisos").hasRole("PROFESSOR")
                .requestMatchers(HttpMethod.PUT, "/api/avisos/{id}").hasRole("PROFESSOR")
                .requestMatchers(HttpMethod.DELETE, "/api/avisos/{id}").hasRole("PROFESSOR")
                .requestMatchers(HttpMethod.POST, "/api/avisos/{id}/ler").hasRole("ALUNO")
                .requestMatchers(HttpMethod.GET, "/api/avisos/{id}").authenticated()

                // Regra final: Qualquer outra requisição precisa estar autenticada
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}