package com.ues.dam.pits360.implementations;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ues.dam.pits360.entities.Servicio;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.ServicioRepository;
import com.ues.dam.pits360.services.ServiciosServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del CRUD de servicios.
 *
 * <p>
 * ADR-8: el id del body se sobreescribe siempre; solo el servidor genera UUIDs.
 * La entidad sale pelada (ADR-10, sin capa DTO).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiciosServicesImpl implements ServiciosServices {

	private final ServicioRepository servicioRepository;

	@Override
	@Transactional(readOnly = true)
	public List<Servicio> getAll() {
		return servicioRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Servicio getById(String id) {
		return findServicio(id);
	}

	@Override
	@Transactional
	public Servicio create(Servicio servicio) {
		servicio.setId(UUID.randomUUID().toString());
		Servicio saved = servicioRepository.save(servicio);
		log.debug("Servicio creado: id={}", saved.getId());
		return saved;
	}

	@Override
	@Transactional
	public Servicio update(String id, Servicio servicio) {
		Servicio existing = findServicio(id);
		existing.setNombre(servicio.getNombre());
		existing.setDescripcion(servicio.getDescripcion());
		existing.setPrecio(servicio.getPrecio());
		existing.setDuracion(servicio.getDuracion());
		return servicioRepository.save(existing);
	}

	@Override
	@Transactional
	public void delete(String id) {
		Servicio servicio = findServicio(id);
		servicioRepository.delete(servicio);
		log.debug("Servicio eliminado: id={}", id);
	}

	/** Busca la entidad o lanza 404. Usa la caché de la transacción en curso. */
	private Servicio findServicio(String id) {
		return servicioRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
						"Servicio no encontrado: " + id));
	}
}
