package com.ues.dam.pits360.services;

import java.util.List;

import com.ues.dam.pits360.entities.Servicio;

/**
 * Contrato del recurso {@code /servicios}: CRUD completo del catálogo.
 *
 * <p>
 * ADR-10: sin capa DTO; la entidad {@link Servicio} entra y sale tal cual.
 * Los fallos se señalan con {@link com.ues.dam.pits360.exceptions.ApiException}.
 */
public interface ServiciosServices {

	/** Lista todos los servicios; si no hay, devuelve lista vacía (HTTP 200 con {@code []}). */
	List<Servicio> getAll();

	/**
	 * Busca un servicio por su id.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Servicio getById(String id);

	/**
	 * Crea un servicio y devuelve la entidad guardada.
	 *
	 * <p>
	 * El {@code id} que venga en el body se ignora: lo genera el servidor
	 * con {@code UUID.randomUUID()} (ADR-8).
	 */
	Servicio create(Servicio servicio);

	/**
	 * Actualiza un servicio existente.
	 *
	 * @param id id del path (manda siempre sobre el del body)
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Servicio update(String id, Servicio servicio);

	/**
	 * Elimina un servicio.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	void delete(String id);
}
