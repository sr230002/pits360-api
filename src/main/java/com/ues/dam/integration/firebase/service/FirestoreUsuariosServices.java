package com.ues.dam.integration.firebase.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.UsuariosServices;

import lombok.extern.slf4j.Slf4j;

/**
 * Lectura de usuarios sobre la colección {@code usuarios} de Firestore.
 *
 * <p>
 * Sustituye a la implementación de PostgreSQL cuando el perfil activo no es
 * {@code jpa}. Mismo contrato que {@code UsuariosServicesImpl}: la entidad
 * {@link Usuario} entra y sale pelada ({@code passwordHash} queda oculto por
 * {@code @JsonIgnore}).
 */
@Slf4j
@Service
@Profile("!jpa")
public class FirestoreUsuariosServices implements UsuariosServices {

	private static final Set<String> ROLES_VALIDOS = Set.of("cliente", "tecnico", "admin");

	private final FirestoreRepository<Usuario> repo;

	/** Constructor de Spring: arma el repositorio con el cliente de Firestore. */
	@Autowired
	public FirestoreUsuariosServices(Firestore db) {
		this(new FirestoreRepository<Usuario>(db, FirestoreCollections.USUARIOS, Usuario.class, Usuario::getId,
				Usuario::setId));
	}

	/** Constructor de test: permite inyectar un doble del repositorio. */
	public FirestoreUsuariosServices(FirestoreRepository<Usuario> repo) {
		this.repo = repo;
	}

	@Override
	public List<Usuario> getAll(String rol) {
		if (rol == null || rol.isBlank()) {
			return repo.findAll();
		}
		String normalizado = rol.trim().toLowerCase();
		if (!ROLES_VALIDOS.contains(normalizado)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Rol inválido: usa cliente|tecnico|admin");
		}
		return repo.findBy("rol", normalizado);
	}

	@Override
	public Usuario getById(String id) {
		return repo.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + id));
	}

}
