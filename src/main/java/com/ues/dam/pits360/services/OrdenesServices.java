package com.ues.dam.pits360.services;

import java.util.List;

import com.ues.dam.pits360.entities.Orden;

/**
 * Contrato del recurso {@code /ordenes}: CRUD completo.
 *
 * <p>
 * Sin capa DTO (ADR-10): la entidad cruza las capas tal cual y sale en JSON
 * plano. Los fallos se señalan con
 * {@link com.ues.dam.pits360.exceptions.ApiException}.
 */
public interface OrdenesServices {

	/** Lista todas las órdenes; si no hay, devuelve lista vacía (HTTP 200 con {@code []}). */
	List<Orden> getAll();

	/**
	 * Busca una orden por su id.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Orden getById(String id);

	/**
	 * Crea una orden y devuelve la entidad con el id UUID generado por el
	 * servidor (ADR-8): el {@code id} que venga en el body se ignora.
	 */
	Orden create(Orden orden);

	/**
	 * Actualiza una orden existente.
	 *
	 * @param id id del path (manda siempre sobre el del body)
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Orden update(String id, Orden orden);

	/**
	 * Elimina una orden.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	void delete(String id);

	/** Lista las órdenes de un vehículo (HTTP 200 con {@code []} si no hay). */
	List<Orden> getByVehiculo(String vehiculoId);

	/** Lista las órdenes asignadas a un técnico (HTTP 200 con {@code []} si no hay). */
	List<Orden> getByTecnico(String tecnicoId);
}
