package com.ues.dam.integration.firebase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.dto.PerfilRequest;
import com.ues.dam.integration.firebase.dto.PerfilResultado;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.utilities.JwtUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Actualización del perfil sobre la colección {@code usuarios} de Firestore.
 *
 * <p>
 * Solo cambia lo que llega informado: nombre, teléfono, ciudad, foto, correo
 * y/o contraseña. La contraseña actual se verifica siempre contra el hash
 * BCrypt guardado (401 si no coincide) y la nueva se valida contra la política
 * de contraseñas (400) antes de hashearse. Si el correo cambia se comprueba
 * que ningún otro documento lo use (409) y se emite un token nuevo para el
 * correo nuevo. El hash nunca sale del servicio.
 */
@Slf4j
@Service
@Profile("!jpa")
public class FirestorePerfilServices implements PerfilServices {

	private static final int MIN_PASSWORD_LENGTH = 8;

	private final FirestoreRepository<Usuario> repo;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	/** Constructor de Spring: arma el repositorio con el cliente de Firestore. */
	@Autowired
	public FirestorePerfilServices(Firestore db, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
		this(new FirestoreRepository<Usuario>(db, FirestoreCollections.USUARIOS, Usuario.class, Usuario::getId,
				Usuario::setId), passwordEncoder, jwtUtil);
	}

	/** Constructor de test: permite inyectar un doble del repositorio. */
	public FirestorePerfilServices(FirestoreRepository<Usuario> repo, PasswordEncoder passwordEncoder,
			JwtUtil jwtUtil) {
		this.repo = repo;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public PerfilResultado actualizar(String email, PerfilRequest request) {
		String correo = normalizeEmail(email);
		Usuario usuario = repo.findBy("email", correo).stream().findFirst()
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + correo));

		// La contraseña actual solo se exige cuando se va a cambiar la contraseña.
		if (informado(request.getPasswordNueva())) {
			verificarPasswordActual(usuario, request.getPasswordActual());
		}

		if (informado(request.getNombre())) {
			usuario.setNombre(request.getNombre().trim());
		}
		if (informado(request.getTelefono())) {
			usuario.setTelefono(request.getTelefono().trim());
		}
		if (informado(request.getCiudad())) {
			usuario.setCiudad(request.getCiudad().trim());
		}
		// La foto se borra si llega en blanco; si no llega, se conserva.
		if (request.getFotoUrl() != null) {
			usuario.setFotoUrl(request.getFotoUrl().isBlank() ? null : request.getFotoUrl().trim());
		}

		// El token solo se renueva si el correo cambia: el JWT lleva el email como subject.
		String tokenNuevo = null;
		if (informado(request.getEmail())) {
			String nuevo = normalizeEmail(request.getEmail());
			if (!nuevo.equals(correo)) {
				if (!repo.findBy("email", nuevo).isEmpty()) {
					throw new ApiException(HttpStatus.CONFLICT, "El correo ya está registrado");
				}
				usuario.setEmail(nuevo);
				tokenNuevo = jwtUtil.generateToken(nuevo);
			}
		}

		if (informado(request.getPasswordNueva())) {
			String nueva = request.getPasswordNueva();
			if (!cumplePolitica(nueva)) {
				throw new ApiException(HttpStatus.BAD_REQUEST,
						"La contraseña nueva debe tener al menos 8 caracteres, una letra y un número");
			}
			usuario.setPasswordHash(passwordEncoder.encode(nueva));
		}

		Usuario saved = repo.save(usuario);
		log.info("Perfil actualizado en Firestore: {}", saved.getEmail());
		return new PerfilResultado(saved, tokenNuevo);
	}

	/** 401 si la contraseña actual no coincide con el hash guardado. */
	private void verificarPasswordActual(Usuario usuario, String passwordActual) {
		if (passwordActual == null || passwordActual.isBlank()
				|| !passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "La contraseña actual no coincide");
		}
	}

	/** Política de contraseñas: 8+ caracteres, al menos una letra y un número. */
	private boolean cumplePolitica(String password) {
		return password.length() >= MIN_PASSWORD_LENGTH
				&& password.chars().anyMatch(Character::isLetter)
				&& password.chars().anyMatch(Character::isDigit);
	}

	/** true si el campo viene informado (no nulo ni en blanco). */
	private boolean informado(String valor) {
		return valor != null && !valor.isBlank();
	}

	/** Trim + minúsculas: el email se guarda y se busca normalizado. */
	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

}
