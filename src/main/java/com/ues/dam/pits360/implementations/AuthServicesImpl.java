package com.ues.dam.pits360.implementations;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ues.dam.pits360.dtos.AuthResponse;
import com.ues.dam.pits360.dtos.LoginRequest;
import com.ues.dam.pits360.dtos.RegisterDTO;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.UsuarioRepository;
import com.ues.dam.pits360.services.AuthServices;
import com.ues.dam.pits360.services.CustomUserDetailServices;
import com.ues.dam.pits360.utilities.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de los casos de uso de autenticación.
 *
 * <p>
 * El login delega en el {@link AuthenticationManager} de Spring Security (así
 * la contraseña se compara con el hash almacenado, sin prefijo {@code {bcrypt}}
 * porque el {@link PasswordEncoder} es BCrypt puro). El registro codifica la
 * contraseña, genera el UUID en el servidor y asigna siempre el rol
 * "cliente"; nunca devuelve el password.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServicesImpl implements AuthServices {

	private static final String ROL_DEFAULT = "cliente";

	private final AuthenticationManager authenticationManager;
	private final CustomUserDetailServices customUserDetailServices;
	private final JwtUtil jwtUtil;
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
		} catch (AuthenticationException e) {
			log.debug("Login fallido ({}) para {}", e.getClass().getSimpleName(), email);
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
		}

		Usuario entity = customUserDetailServices.findByEmail(email);
		log.info("Login correcto para {}", email);
		return new AuthResponse(jwtUtil.generateToken(entity.getEmail()),
				entity.getId(), entity.getNombre(), entity.getEmail(), entity.getRol());
	}

	@Override
	public AuthResponse register(RegisterDTO dto) {
		String email = normalizeEmail(dto.getEmail());
		if (usuarioRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "El email ya está registrado: " + email);
		}

		Usuario entity = Usuario.builder()
				.id(UUID.randomUUID().toString())
				.nombre(dto.getNombre().trim())
				.email(email)
				.passwordHash(passwordEncoder.encode(dto.getPassword()))
				.rol(ROL_DEFAULT)
				.build();

		Usuario saved = usuarioRepository.save(entity);
		log.info("Usuario registrado: {}", email);
		return new AuthResponse(jwtUtil.generateToken(saved.getEmail()),
				saved.getId(), saved.getNombre(), saved.getEmail(), saved.getRol());
	}

	@Override
	public AuthResponse me(String email) {
		String normalized = normalizeEmail(email);
		Usuario entity = usuarioRepository.findByEmail(normalized)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
						"Usuario no encontrado: " + normalized));
		log.debug("Perfil obtenido para {}", normalized);
		return new AuthResponse(null, entity.getId(), entity.getNombre(),
				entity.getEmail(), entity.getRol());
	}

	@Override
	public Map<String, Object> verifyToken(String email) {
		log.debug("Token verificado para {}", email);
		return Map.of("valid", true, "email", email);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Sin blacklist: la API es stateless, así que el token sigue siendo válido
	 * hasta que expira y es el cliente quien debe descartarlo.
	 */
	@Override
	public String logout(String token) {
		if (token == null || token.isBlank()) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "No se recibió el token de sesión.");
		}
		if (!jwtUtil.isValid(token)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado.");
		}
		log.debug("Logout para {}", jwtUtil.extractEmail(token));
		return "Sesión cerrada correctamente";
	}

	/** Trim + minúsculas: el email se guarda y se busca normalizado. */
	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}
}
