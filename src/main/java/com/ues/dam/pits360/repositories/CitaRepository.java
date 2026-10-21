package com.ues.dam.pits360.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ues.dam.pits360.entities.Cita;

/**
 * Acceso a datos de citas de servicio.
 */
public interface CitaRepository extends JpaRepository<Cita, String> {

	List<Cita> findByEstado(String estado);
}
