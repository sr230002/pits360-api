package com.ues.dam.pits360.repositories;

import java.util.List;

import com.ues.dam.pits360.entities.OrdenAdjunto;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@code orden_adjunto}: vínculo orden ↔ adjunto con
 * metadatos por fila ({@code descripcion}, {@code visible}, {@code esPrincipal}).
 */
public interface OrdenAdjuntoRepository extends JpaRepository<OrdenAdjunto, String> {

	List<OrdenAdjunto> findByOrdenId(String ordenId);

	long countByOrdenId(String ordenId);

	/** Max. 1 fila por orden con {@code esPrincipal = true}. */
	List<OrdenAdjunto> findByOrdenIdAndEsPrincipalTrue(String ordenId);

	void deleteByAdjuntoId(String adjuntoId);
}
