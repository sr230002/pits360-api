package com.ues.dam.pits360.repositories;

import com.ues.dam.pits360.entities.Vehiculo;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@code vehiculos}. La clave primaria es un {@code String}
 * UUID, por eso el tipo de id es {@code String}.
 */
public interface VehiculoRepository extends JpaRepository<Vehiculo, String> {
}
