package com.ues.dam.pits360.repositories;

import java.util.List;

import com.ues.dam.pits360.entities.Adjunto;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@code adjunto}. La clave primaria es un {@code String}
 * UUID, por eso el tipo de id es {@code String}.
 */
public interface AdjuntoRepository extends JpaRepository<Adjunto, String> {

	/** Adjuntos vinculados a un vehículo (foto/s del mismo). */
	List<Adjunto> findByVehiculoId(String vehiculoId);

	/** Adjuntos archivados históricamente en una orden. */
	List<Adjunto> findByOrdenId(String ordenId);
}
