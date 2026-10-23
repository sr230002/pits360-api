package com.ues.dam.pits360.implementations;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ues.dam.pits360.entities.Taller;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.TallerRepository;
import com.ues.dam.pits360.services.TalleresServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del CRUD de talleres.
 *
 * <p>
 * ADR-8: el id del body se sobreescribe siempre; solo el servidor genera UUIDs.
 * La entidad sale pelada (ADR-10, sin capa DTO).
 */
@Slf4j
@Service
@Profile("jpa")
@RequiredArgsConstructor
public class TalleresServicesImpl implements TalleresServices {

	private final TallerRepository tallerRepository;

	@Override
	@Transactional(readOnly = true)
	public List<Taller> getAll() {
		return tallerRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Taller getById(String id) {
		return findTaller(id);
	}

	@Override
	@Transactional
	public Taller create(Taller taller) {
		taller.setId(UUID.randomUUID().toString());
		Taller saved = tallerRepository.save(taller);
		log.debug("Taller creado: id={}", saved.getId());
		return saved;
	}

	@Override
	@Transactional
	public Taller update(String id, Taller taller) {
		Taller existing = findTaller(id);
		existing.setNombre(taller.getNombre());
		existing.setDireccion(taller.getDireccion());
		existing.setTelefono(taller.getTelefono());
		existing.setRating(taller.getRating());
		return tallerRepository.save(existing);
	}

	@Override
	@Transactional
	public void delete(String id) {
		Taller taller = findTaller(id);
		tallerRepository.delete(taller);
		log.debug("Taller eliminado: id={}", id);
	}

	/** Busca la entidad o lanza 404. Usa la caché de la transacción en curso. */
	private Taller findTaller(String id) {
		return tallerRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
						"Taller no encontrado: " + id));
	}
}
