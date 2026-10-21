package com.ues.dam.pits360.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.dtos.AuthResponse;
import com.ues.dam.pits360.dtos.LoginRequest;
import com.ues.dam.pits360.dtos.RegisterDTO;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.AuthServices;
import com.ues.dam.pits360.utilities.HandlerResponse;
import com.ues.dam.pits360.validations.OnCreate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de autenticación. Todas las respuestas salen envueltas en
 * {@link ApiResponse} vía {@link HandlerResponse} ({@code data, message, method}).
 *
 * <p>
 * No hay {@code try/catch}: los errores de dominio ({@code ApiException}) los
 * traduce {@code GlobalExceptionHandler} al mismo envelope.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private static final String BEARER_PREFIX = "Bearer ";

	private final AuthServices authServices;

	/** Autentica por email y contraseña y devuelve token + datos del usuario (200). */
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		return HandlerResponse.ok(authServices.login(request), "Inicio de sesión exitoso");
	}

	/** Registra un usuario nuevo y devuelve token + datos (201). */
	@PostMapping("/register")
	public ResponseEntity<ApiResponse<AuthResponse>> register(
			@Validated(OnCreate.class) @RequestBody RegisterDTO dto) {
		return HandlerResponse.created(authServices.register(dto), "Usuario registrado exitosamente");
	}

	/** Devuelve el perfil del usuario autenticado (token va null) (200). */
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<AuthResponse>> me() {
		return HandlerResponse.ok(authServices.me(currentEmail()), "Perfil obtenido");
	}

	/** Verifica el token ya validado por el JwtFilter: lee el email del contexto (200). */
	@PostMapping("/verify-token")
	public ResponseEntity<ApiResponse<Map<String, Object>>> verifyToken() {
		return HandlerResponse.ok(authServices.verifyToken(currentEmail()), "Token válido");
	}

	/** Cierra la sesión del token del header {@code Authorization} (200). */
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		return HandlerResponse.ok(authServices.logout(bearer(authorization)), "Sesión cerrada");
	}

	/** Email que el JwtFilter dejó en el SecurityContextHolder; 401 si no hay. */
	private String currentEmail() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof String email && !email.isBlank()) {
			return email;
		}
		throw new ApiException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
	}

	/** Quita el prefijo {@code Bearer } del header si viene (insensible a mayúsculas). */
	private String bearer(String authorization) {
		if (authorization == null) {
			return null;
		}
		return authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())
				? authorization.substring(BEARER_PREFIX.length()).trim()
				: authorization.trim();
	}
}
