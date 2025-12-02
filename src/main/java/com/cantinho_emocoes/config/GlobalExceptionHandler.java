package com.cantinho_emocoes.config; // Ou o pacote onde você preferir colocar suas exceções

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * Classe centralizada para tratamento de exceções em toda a aplicação.
 * Captura exceções específicas e as transforma em respostas HTTP claras.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura erros de autenticação (ex: email ou senha inválidos).
     * @param ex A exceção de autenticação lançada pelo Spring Security.
     * @return Uma resposta HTTP 401 Unauthorized com uma mensagem de erro clara.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        Map<String, String> errorResponse = Map.of("error", "Credenciais inválidas. Por favor, verifique seu email e senha.");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED); // 401 é mais apropriado para falha de login
    }

    /**
     * Captura qualquer outra exceção inesperada que possa ocorrer.
     * @param ex A exceção genérica.
     * @return Uma resposta HTTP 500 Internal Server Error com os detalhes do erro.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex, WebRequest request) {
        // Logar a exceção é uma boa prática para depuração no servidor
        ex.printStackTrace(); 
        
        Map<String, String> errorResponse = Map.of(
            "error", "Ocorreu um erro inesperado no servidor.",
            "message", ex.getMessage() // A mensagem da exceção original
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
