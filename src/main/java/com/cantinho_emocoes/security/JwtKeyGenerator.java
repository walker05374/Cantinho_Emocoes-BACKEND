package com.cantinho_emocoes.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.security.Key;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // Gera uma chave segura para o algoritmo HS256
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        
        // Converte a chave gerada para uma string Base64
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        
        System.out.println("Sua chave secreta Base64 (copie e cole no application.properties):");
        System.out.println(base64Key);
    }
}
