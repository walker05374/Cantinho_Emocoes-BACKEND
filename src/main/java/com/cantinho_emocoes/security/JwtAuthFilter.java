package com.cantinho_emocoes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // ✅ CORREÇÃO: Removido @Autowired

    // ✅ CORREÇÃO: Construtor único para injetar todas as dependências
    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        log.debug("Processando requisição para URI: {}", request.getRequestURI());
        log.debug("Authorization Header recebido: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Header de Autorização ausente ou não começa com 'Bearer '. Continuando sem autenticação.");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        log.debug("Token JWT extraído: {}", jwt);

        try {
            userEmail = jwtService.extractUsername(jwt);
            log.debug("Email extraído do token: {}", userEmail);
        } catch (Exception e) {
            log.warn("Falha ao extrair username do JWT: {}. Causa: {}", e.getClass().getSimpleName(), e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("UserDetailsService carregou usuário: {}", userDetails.getUsername());
            } catch (UsernameNotFoundException e) {
                log.warn("Usuário do token não encontrado no banco de dados: {}", userEmail);
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtService.isTokenValid(jwt, userDetails)) {
                log.debug("Token JWT é VÁLIDO para o usuário: {}", userDetails.getUsername());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Usuário {} autenticado e SecurityContextHolder atualizado.", userDetails.getUsername());
            } else {
                log.warn("Token JWT INVÁLIDO para o usuário: {}", userDetails.getUsername());
            }
        }
        filterChain.doFilter(request, response);
    }
}