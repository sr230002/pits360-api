package com.ues.dam.pits360.repositories;

import com.ues.dam.pits360.entities.Servicio;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@code servicios}. La clave primaria es un {@code String}
 * UUID, por eso el tipo de id es {@code String}.
 */
public interface ServicioRepository extends JpaRepository<Servicio, String> {
}
