package com.ues.dam.pits360.services;

import java.util.List;

import com.ues.dam.pits360.entities.Taller;

/**
 * Contrato del recurso {@code /talleres}: CRUD completo del catálogo.
 *
 * <p>
 * ADR-10: sin capa DTO; la entidad {@link Taller} entra y sale tal cual.
 * Los fallos se señalan con {@link com.ues.dam.pits360.exceptions.ApiException}.
 */
public interface TalleresServices {

	/** Lista todos los talleres; si no hay, devuelve lista vacía (HTTP 200 con {@code []}). */
	List<Taller> getAll();

	/**
	 * Busca un taller por su id.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Taller getById(String id);

	/**
	 * Crea un taller y devuelve la entidad guardada.
	 *
	 * <p>
	 * El {@code id} que venga en el body se ignora: lo genera el servidor
	 * con {@code UUID.randomUUID()} (ADR-8).
	 */
	Taller create(Taller taller);

	/**
	 * Actualiza un taller existente.
	 *
	 * @param id id del path (manda siempre sobre el del body)
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Taller update(String id, Taller taller);

	/**
	 * Elimina un taller.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	void delete(String id);
}
