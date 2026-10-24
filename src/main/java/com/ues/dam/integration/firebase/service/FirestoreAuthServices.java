package com.ues.dam.integration.firebase.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.dtos.AuthResponse;
import com.ues.dam.pits360.dtos.LoginRequest;
import com.ues.dam.pits360.dtos.RegisterDTO;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.AuthServices;
import com.ues.dam.pits360.utilities.JwtUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Autenticación y registro sobre la colección {@code usuarios} de Firestore.
 *
 * <p>
 * Sustituye a la implementación de PostgreSQL cuando el perfil activo no es
 * {@code jpa}. Mismo contrato que {@code AuthServicesImpl}: el login delega en
 * el {@link AuthenticationManager} (el hash BCrypt lo resuelve
 * {@link FirestoreUserDetailsService}), el registro codifica la contraseña,
 * genera el UUID en el servidor y asigna siempre el rol {@code cliente}.
 */
@Slf4j
@Service
@Profile("!jpa")
public class FirestoreAuthServices implements AuthServices {

	private static final String ROL_DEFAULT = "cliente";

	private final FirestoreRepository<Usuario> repo;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;

	/** Constructor de Spring: arma el repositorio con el cliente de Firestore. */
	@Autowired
	public FirestoreAuthServices(Firestore db, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			PasswordEncoder passwordEncoder) {
		this(new FirestoreRepository<Usuario>(db, FirestoreCollections.USUARIOS, Usuario.class, Usuario::getId,
				Usuario::setId), authenticationManager, jwtUtil, passwordEncoder);
	}

	/** Constructor de test: permite inyectar un doble del repositorio. */
	public FirestoreAuthServices(FirestoreRepository<Usuario> repo, AuthenticationManager authenticationManager,
			JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
		this.repo = repo;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
		} catch (AuthenticationException e) {
			log.debug("Login fallido ({}) para {}", e.getClass().getSimpleName(), email);
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
		}

		Usuario usuario = findByEmailOrThrow(email);
		log.info("Login correcto para {}", email);
		return new AuthResponse(jwtUtil.generateToken(usuario.getEmail()), usuario.getId(), usuario.getNombre(),
				usuario.getEmail(), usuario.getRol());
	}

	@Override
	public AuthResponse register(RegisterDTO dto) {
		String email = normalizeEmail(dto.getEmail());
		if (!repo.findBy("email", email).isEmpty()) {
			throw new ApiException(HttpStatus.CONFLICT, "El correo ya está registrado");
		}

		Usuario usuario = Usuario.builder()
				.id(UUID.randomUUID().toString())
				.nombre(dto.getNombre().trim())
				.email(email)
				.passwordHash(passwordEncoder.encode(dto.getPassword()))
				.telefono(dto.getTelefono())
				.rol(ROL_DEFAULT)
				.build();

		Usuario saved = repo.save(usuario);
		log.info("Usuario registrado en Firestore: {}", email);
		return new AuthResponse(jwtUtil.generateToken(saved.getEmail()), saved.getId(), saved.getNombre(),
				saved.getEmail(), saved.getRol());
	}

	@Override
	public AuthResponse me(String email) {
		Usuario usuario = findByEmailOrThrow(normalizeEmail(email));
		log.debug("Perfil obtenido para {}", usuario.getEmail());
		return new AuthResponse(null, usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
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

	/** Busca por email normalizado en la colección de usuarios. */
	private Usuario findByEmailOrThrow(String email) {
		return repo.findBy("email", email).stream().findFirst()
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + email));
	}

	/** Trim + minúsculas: el email se guarda y se busca normalizado. */
	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

}
