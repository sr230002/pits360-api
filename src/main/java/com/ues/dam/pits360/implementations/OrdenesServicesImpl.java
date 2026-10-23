package com.ues.dam.pits360.implementations;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.OrdenRepository;
import com.ues.dam.pits360.services.OrdenesServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del CRUD de órdenes.
 */
@Slf4j
@Service
@Profile("jpa")
@RequiredArgsConstructor
public class OrdenesServicesImpl implements OrdenesServices {

	private final OrdenRepository ordenRepository;

	/** {@inheritDoc} */
	@Override
	@Transactional(readOnly = true)
	public List<Orden> getAll() {
		return ordenRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Orden getById(String id) {
		return findOrden(id);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * El id lo genera siempre el servidor (ADR-8): el que venga en el body se
	 * sobreescribe con un UUID.
	 */
	@Override
	@Transactional
	public Orden create(Orden orden) {
		orden.setId(UUID.randomUUID().toString());
		return ordenRepository.save(orden);
	}

	/** {@inheritDoc} */
	@Override
	@Transactional
	public Orden update(String id, Orden orden) {
		Orden existente = findOrden(id);
		existente.setNumero(orden.getNumero());
		existente.setEstado(orden.getEstado());
		existente.setVehiculoId(orden.getVehiculoId());
		existente.setTallerId(orden.getTallerId());
		existente.setFecha(orden.getFecha());
		existente.setTotal(orden.getTotal());
		existente.setTecnicoId(orden.getTecnicoId());
		existente.setCitaId(orden.getCitaId());
		existente.setNota(orden.getNota());
		existente.setTrabajos(orden.getTrabajos());
		existente.setMetodoPago(orden.getMetodoPago());
		existente.setAvance(orden.getAvance());
		return ordenRepository.save(existente);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Se carga la entidad gestionada y se borra con {@code delete}: dentro de la
	 * transacción es un único {@code DELETE} (frente a {@code existsById} +
	 * {@code deleteById}, que serían dos consultas).
	 */
	@Override
	@Transactional
	public void delete(String id) {
		Orden orden = findOrden(id);
		ordenRepository.delete(orden);
		log.debug("Orden eliminada: id={}", id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Orden> getByVehiculo(String vehiculoId) {
		return ordenRepository.findByVehiculoId(vehiculoId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Orden> getByTecnico(String tecnicoId) {
		return ordenRepository.findByTecnicoId(tecnicoId);
	}

	/** Busca la entidad o lanza 404. Usa la caché de la transacción en curso. */
	private Orden findOrden(String id) {
		return ordenRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
						"Orden no encontrada: " + id));
	}
}
