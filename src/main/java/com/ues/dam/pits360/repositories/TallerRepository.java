package com.ues.dam.pits360.repositories;

import com.ues.dam.pits360.entities.Taller;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@code talleres}. La clave primaria es un {@code String}
 * UUID, por eso el tipo de id es {@code String}.
 */
public interface TallerRepository extends JpaRepository<Taller, String> {
}
