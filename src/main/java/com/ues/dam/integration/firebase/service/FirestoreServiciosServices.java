package com.ues.dam.integration.firebase.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.entities.Servicio;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.ServiciosServices;

import lombok.RequiredArgsConstructor;

/**
 * CRUD del catálogo de servicios sobre Firestore. Sustituye a
 * {@code ServiciosServicesImpl} cuando el perfil activo no es {@code jpa}.
 *
 * <p>
 * ADR-8: el id que venga en el body se ignora; solo el servidor genera UUIDs.
 * ADR-10: sin capa DTO, la entidad entra y sale tal cual.
 */
@Service
@Profile("!jpa")
@RequiredArgsConstructor
public class FirestoreServiciosServices implements ServiciosServices {

	private final FirestoreRepository<Servicio> repository;

	@Override
	public List<Servicio> getAll() {
		return repository.findAll();
	}

	@Override
	public Servicio getById(String id) {
		return repository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Servicio no encontrado: " + id));
	}

	@Override
	public Servicio create(Servicio servicio) {
		servicio.setId(UUID.randomUUID().toString());
		return repository.save(servicio);
	}

	@Override
	public Servicio update(String id, Servicio servicio) {
		Servicio existing = getById(id);
		existing.setNombre(servicio.getNombre());
		existing.setDescripcion(servicio.getDescripcion());
		existing.setPrecio(servicio.getPrecio());
		existing.setDuracion(servicio.getDuracion());
		return repository.save(existing);
	}

	@Override
	public void delete(String id) {
		getById(id);
		repository.delete(id);
	}

	/**
	 * El repositorio es genérico: cada servicio necesita el suyo, con su
	 * colección y su forma de leer y escribir el id.
	 */
	@Configuration(proxyBeanMethods = false)
	@Profile("!jpa")
	static class RepositorioConfig {

		@Bean
		FirestoreRepository<Servicio> serviciosRepository(Firestore firestore) {
			return new FirestoreRepository<>(firestore, FirestoreCollections.SERVICIOS, Servicio.class,
					Servicio::getId, Servicio::setId);
		}
	}
}
