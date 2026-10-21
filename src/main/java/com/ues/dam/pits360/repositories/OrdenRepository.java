package com.ues.dam.pits360.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ues.dam.pits360.entities.Orden;

/**
 * Acceso a datos de órdenes de trabajo.
 */
public interface OrdenRepository extends JpaRepository<Orden, String> {

	List<Orden> findByVehiculoId(String vehiculoId);

	List<Orden> findByTecnicoId(String tecnicoId);
}
