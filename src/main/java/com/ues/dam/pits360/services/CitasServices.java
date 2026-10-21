package com.ues.dam.pits360.services;

import java.util.List;

import com.ues.dam.pits360.entities.Cita;
import com.ues.dam.pits360.entities.Orden;

/**
 * Contrato del recurso {@code /citas}: CRUD completo.
 *
 * <p>
 * Sin capa DTO (ADR-10): la entidad cruza las capas tal cual y sale en JSON
 * plano. Los fallos se señalan con
 * {@link com.ues.dam.pits360.exceptions.ApiException}.
 */
public interface CitasServices {

	/** Lista todas las citas; si {@code estado} no es null, filtra por ese estado. */
	List<Cita> getAll(String estado);

	/** Alias sin filtro (compatibilidad). */
	default List<Cita> getAll() {
		return getAll(null);
	}

	/**
	 * Busca una cita por su id.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Cita getById(String id);

	/**
	 * Crea una cita y devuelve la entidad con el id UUID generado por el
	 * servidor (ADR-8): el {@code id} que venga en el body se ignora.
	 */
	Cita create(Cita cita);

	/**
	 * Actualiza una cita existente.
	 *
	 * @param id id del path (manda siempre sobre el del body)
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Cita update(String id, Cita cita);

	/**
	 * Elimina una cita.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	void delete(String id);

	/**
	 * Aprueba una cita (PROGRAMADA → CONFIRMADA).
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe, 409 si no está PROGRAMADA
	 */
	Cita aprobar(String id);

	/**
	 * Rechaza una cita (PROGRAMADA → CANCELADA).
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe, 409 si no está PROGRAMADA
	 */
	Cita rechazar(String id);

	/**
	 * Convierte una cita en orden de trabajo.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe, 409 si está CONVERTIDA o CANCELADA
	 */
	Orden convertir(String citaId, Orden datos);
}
