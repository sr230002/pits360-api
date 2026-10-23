package com.ues.dam.integration.firebase.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.entities.Taller;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.TalleresServices;

import lombok.extern.slf4j.Slf4j;

/**
 * CRUD de talleres sobre la coleccion {@code talleres} de Firestore.
 *
 * <p>
 * Sustituye a {@code TalleresServicesImpl} cuando el perfil activo no es
 * {@code jpa}. El repositorio generico se construye aqui porque
 * {@link FirestoreRepository} no es un bean: cada servicio declara su
 * coleccion y como se lee y escribe el id.
 *
 * <p>
 * ADR-8: el id que venga en el body se ignora; solo el servidor genera UUIDs.
 * ADR-10: sin capa DTO, la entidad entra y sale tal cual.
 */
@Slf4j
@Service
@Profile("!jpa")
public class FirestoreTalleresServices implements TalleresServices {

	private final FirestoreRepository<Taller> repo;

	/** Constructor de Spring: arma el repositorio con el cliente de Firestore. */
	@Autowired
	public FirestoreTalleresServices(Firestore db) {
		this(new FirestoreRepository<Taller>(db, FirestoreCollections.TALLERES, Taller.class, Taller::getId,
				Taller::setId));
	}

	/** Constructor de test: permite inyectar un doble del repositorio. */
	public FirestoreTalleresServices(FirestoreRepository<Taller> repo) {
		this.repo = repo;
	}

	@Override
	public List<Taller> getAll() {
		return repo.findAll();
	}

	@Override
	public Taller getById(String id) {
		return findByIdOrThrow(id);
	}

	@Override
	public Taller create(Taller taller) {
		taller.setId(UUID.randomUUID().toString());
		Taller saved = repo.save(taller);
		log.debug("Taller creado en Firestore: id={}", saved.getId());
		return saved;
	}

	@Override
	public Taller update(String id, Taller taller) {
		Taller existing = findByIdOrThrow(id);
		existing.setNombre(taller.getNombre());
		existing.setDireccion(taller.getDireccion());
		existing.setTelefono(taller.getTelefono());
		existing.setRating(taller.getRating());
		existing.setLatitud(taller.getLatitud());
		existing.setLongitud(taller.getLongitud());
		Taller saved = repo.save(existing);
		log.debug("Taller actualizado en Firestore: id={}", id);
		return saved;
	}

	@Override
	public void delete(String id) {
		findByIdOrThrow(id);
		repo.delete(id);
		log.debug("Taller eliminado de Firestore: id={}", id);
	}

	private Taller findByIdOrThrow(String id) {
		return repo.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Taller no encontrado: " + id));
	}

}
