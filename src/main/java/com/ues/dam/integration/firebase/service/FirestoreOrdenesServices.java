package com.ues.dam.integration.firebase.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;

import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.OrdenesServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Órdenes sobre Firestore. Sustituye a {@code OrdenesServicesImpl} cuando el
 * perfil activo no es {@code jpa}: los controllers no cambian de firma.
 *
 * <p>
 * Los filtros ({@code vehiculoId}, {@code tecnicoId}) usan consultas por
 * igualdad de un solo campo, sin índice compuesto.
 */
@Slf4j
@Service
@Profile("!jpa")
@RequiredArgsConstructor
public class FirestoreOrdenesServices implements OrdenesServices {

	private final FirestoreRepository<Orden> repository;

	/** Constructor de Spring: arma el repositorio sobre la colección {@code ordenes}. */
	@Autowired
	public FirestoreOrdenesServices(Firestore firestore) {
		this(new FirestoreRepository<>(firestore, FirestoreCollections.ORDENES, Orden.class, Orden::getId,
				Orden::setId));
	}

	/** {@inheritDoc} */
	@Override
	public List<Orden> getAll() {
		return repository.findAll();
	}

	/** {@inheritDoc} */
	@Override
	public Orden getById(String id) {
		return repository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada: " + id));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * El id lo genera el servidor (ADR-8): el que venga en el body se ignora.
	 */
	@Override
	public Orden create(Orden orden) {
		orden.setId(UUID.randomUUID().toString());
		return repository.save(orden);
	}

	/** {@inheritDoc} */
	@Override
	public Orden update(String id, Orden orden) {
		Orden existente = getById(id);
		BeanUtils.copyProperties(orden, existente, "id");
		return repository.save(existente);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(String id) {
		getById(id);
		repository.delete(id);
		log.debug("Orden eliminada: id={}", id);
	}

	/** {@inheritDoc} */
	@Override
	public List<Orden> getByVehiculo(String vehiculoId) {
		return repository.findBy("vehiculoId", vehiculoId);
	}

	/** {@inheritDoc} */
	@Override
	public List<Orden> getByTecnico(String tecnicoId) {
		return repository.findBy("tecnicoId", tecnicoId);
	}

}
