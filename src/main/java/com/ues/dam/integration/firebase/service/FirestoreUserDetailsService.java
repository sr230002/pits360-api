package com.ues.dam.integration.firebase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.entities.Usuario;

import lombok.extern.slf4j.Slf4j;

/**
 * Carga de usuarios para Spring Security cuando el backend es Firestore: el
 * email normalizado (trim + minúsculas) es el username.
 *
 * <p>
 * Devuelve el rol real del usuario como autoridad {@code ROLE_<ROL>} para que
 * los permisos se puedan filtrar por rol sin tocar la colección.
 *
 * <p>
 * Es el único {@link UserDetailsService} bajo el perfil {@code !jpa}: el loader
 * de PostgreSQL queda fuera por su propio perfil.
 */
@Slf4j
@Service
@Profile("!jpa")
public class FirestoreUserDetailsService implements UserDetailsService {

	private final FirestoreRepository<Usuario> repo;

	/** Constructor de Spring: arma el repositorio con el cliente de Firestore. */
	@Autowired
	public FirestoreUserDetailsService(Firestore db) {
		this(new FirestoreRepository<Usuario>(db, FirestoreCollections.USUARIOS, Usuario.class, Usuario::getId,
				Usuario::setId));
	}

	/** Constructor de test: permite inyectar un doble del repositorio. */
	public FirestoreUserDetailsService(FirestoreRepository<Usuario> repo) {
		this.repo = repo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String email = normalizeEmail(username);
		Usuario usuario = repo.findBy("email", email).stream().findFirst()
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
		log.debug("Usuario cargado desde Firestore: {}", email);
		return User.withUsername(usuario.getEmail())
				.password(usuario.getPasswordHash())
				.roles(usuario.getRol().toUpperCase())
				.build();
	}

	/** Trim + minúsculas: el email se guarda y se busca normalizado. */
	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

}
