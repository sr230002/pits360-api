package com.ues.dam.pits360.services;

import java.util.List;

import com.ues.dam.pits360.entities.Vehiculo;

/**
 * Contrato del recurso {@code /vehiculos}: CRUD completo del catálogo.
 *
 * <p>
 * ADR-10: sin capa DTO; la entidad {@link Vehiculo} entra y sale tal cual.
 * Los fallos se señalan con {@link com.ues.dam.pits360.exceptions.ApiException}.
 */
public interface VehiculosServices {

	/** Lista todos los vehículos; si no hay, devuelve lista vacía (HTTP 200 con {@code []}). */
	List<Vehiculo> getAll();

	/**
	 * Busca un vehículo por su id.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Vehiculo getById(String id);

	/**
	 * Crea un vehículo y devuelve la entidad guardada.
	 *
	 * <p>
	 * El {@code id} que venga en el body se ignora: lo genera el servidor
	 * con {@code UUID.randomUUID()} (ADR-8).
	 */
	Vehiculo create(Vehiculo vehiculo);

	/**
	 * Actualiza un vehículo existente.
	 *
	 * @param id id del path (manda siempre sobre el del body)
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Vehiculo update(String id, Vehiculo vehiculo);

	/**
	 * Elimina un vehículo.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	void delete(String id);
}
