package com.ues.dam.pits360.utilities;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para emitir y validar tokens JWT (jjwt 0.13.0, API moderna).
 * La clave HMAC se deriva de {@code app.jwt.secret}; debe medir al menos 256 bits.
 */
@Slf4j
@Service
public class JwtUtil {

	private final SecretKey key;

	private final long expirationMs;

	public JwtUtil(@Value("${app.jwt.secret}") String jwtSecret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		if (jwtSecret == null || jwtSecret.isBlank()) {
			throw new IllegalStateException("app.jwt.secret no está configurada");
		}
		this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	/** Genera un token JWT con el email como subject y la expiración configurada. */
	public String generateToken(String email) {
		Date now = new Date();
		return Jwts.builder()
				.subject(email)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMs))
				.signWith(key, Jwts.SIG.HS256) // algoritmo explícito (lo documenta la guía)
				.compact();
	}

	/** Parsea el token y devuelve sus claims. Lanza excepción si es inválido o expiró. */
	public Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	/** Email (subject) contenido en el token. */
	public String extractEmail(String token) {
		return parseClaims(token).getSubject();
	}

	/** true si el token es válido: bien formado, firma correcta y no expirado. */
	public boolean isValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.debug("Token JWT inválido: {}", e.getMessage());
			return false;
		}
	}
}
