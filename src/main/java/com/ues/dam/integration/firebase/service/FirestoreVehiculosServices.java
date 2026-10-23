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
import com.ues.dam.pits360.entities.Vehiculo;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.VehiculosServices;

import lombok.extern.slf4j.Slf4j;

/**
 * CRUD de vehículos sobre la colección {@code vehiculos} de Firestore.
 *
 * <p>
 * Sustituye a la implementación de PostgreSQL cuando el perfil activo no es
 * {@code jpa}. El repositorio genérico se construye aquí porque
 * {@link FirestoreRepository} no es un bean: cada servicio declara su
 * colección y cómo se lee y escribe el id.
 *
 * <p>
 * Mismo contrato que la implementación JPA: la entidad entra y sale pelada y
 * los fallos se señalan con {@link ApiException}.
 */
@Slf4j
@Service
@Profile("!jpa")
public class FirestoreVehiculosServices implements VehiculosServices {

	private final FirestoreRepository<Vehiculo> repo;

	/** Constructor de Spring: arma el repositorio con el cliente de Firestore. */
	@Autowired
	public FirestoreVehiculosServices(Firestore db) {
		this(new FirestoreRepository<Vehiculo>(db, FirestoreCollections.VEHICULOS, Vehiculo.class, Vehiculo::getId,
				Vehiculo::setId));
	}

	/** Constructor de test: permite inyectar un doble del repositorio. */
	public FirestoreVehiculosServices(FirestoreRepository<Vehiculo> repo) {
		this.repo = repo;
	}

	@Override
	public List<Vehiculo> getAll() {
		return repo.findAll();
	}

	@Override
	public Vehiculo getById(String id) {
		return findByIdOrThrow(id);
	}

	@Override
	public Vehiculo create(Vehiculo vehiculo) {
		if (vehiculo.getId() == null || vehiculo.getId().isBlank()) {
			vehiculo.setId(UUID.randomUUID().toString());
		}
		Vehiculo saved = repo.save(vehiculo);
		log.debug("Vehiculo creado en Firestore: id={}", saved.getId());
		return saved;
	}

	@Override
	public Vehiculo update(String id, Vehiculo vehiculo) {
		Vehiculo existing = findByIdOrThrow(id);
		existing.setPlaca(vehiculo.getPlaca());
		existing.setMarca(vehiculo.getMarca());
		existing.setModelo(vehiculo.getModelo());
		existing.setAnio(vehiculo.getAnio());
		existing.setKilometraje(vehiculo.getKilometraje());
		Vehiculo saved = repo.save(existing);
		log.debug("Vehiculo actualizado en Firestore: id={}", id);
		return saved;
	}

	@Override
	public void delete(String id) {
		findByIdOrThrow(id);
		repo.delete(id);
		log.debug("Vehiculo eliminado de Firestore: id={}", id);
	}

	private Vehiculo findByIdOrThrow(String id) {
		return repo.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vehículo no encontrado: " + id));
	}

}
