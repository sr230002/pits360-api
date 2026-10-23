package com.ues.dam.pits360.implementations;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ues.dam.pits360.entities.Vehiculo;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.VehiculoRepository;
import com.ues.dam.pits360.services.VehiculosServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del CRUD de vehículos.
 *
 * <p>
 * ADR-8: el id del body se sobreescribe siempre; solo el servidor genera UUIDs.
 * La entidad sale pelada (ADR-10, sin capa DTO).
 */
@Slf4j
@Service
@Profile("jpa")
@RequiredArgsConstructor
public class VehiculosServicesImpl implements VehiculosServices {

	private final VehiculoRepository vehiculoRepository;

	@Override
	@Transactional(readOnly = true)
	public List<Vehiculo> getAll() {
		return vehiculoRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Vehiculo getById(String id) {
		return findVehiculo(id);
	}

	@Override
	@Transactional
	public Vehiculo create(Vehiculo vehiculo) {
		vehiculo.setId(UUID.randomUUID().toString());
		Vehiculo saved = vehiculoRepository.save(vehiculo);
		log.debug("Vehiculo creado: id={}", saved.getId());
		return saved;
	}

	@Override
	@Transactional
	public Vehiculo update(String id, Vehiculo vehiculo) {
		Vehiculo existing = findVehiculo(id);
		existing.setPlaca(vehiculo.getPlaca());
		existing.setMarca(vehiculo.getMarca());
		existing.setModelo(vehiculo.getModelo());
		existing.setAnio(vehiculo.getAnio());
		existing.setKilometraje(vehiculo.getKilometraje());
		return vehiculoRepository.save(existing);
	}

	@Override
	@Transactional
	public void delete(String id) {
		Vehiculo vehiculo = findVehiculo(id);
		vehiculoRepository.delete(vehiculo);
		log.debug("Vehiculo eliminado: id={}", id);
	}

	/** Busca la entidad o lanza 404. Usa la caché de la transacción en curso. */
	private Vehiculo findVehiculo(String id) {
		return vehiculoRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
						"Vehiculo no encontrado: " + id));
	}
}
