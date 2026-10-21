package com.ues.dam.pits360.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ues.dam.pits360.entities.Usuario;

/**
 * Acceso a datos de usuarios. El email es único y es el que se usa para autenticar.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

	/** Busca un usuario por su email (usado por el login). */
	Optional<Usuario> findByEmail(String email);

	/** Indica si ya existe un usuario con ese email (evita duplicados en el registro). */
	boolean existsByEmail(String email);

	/** Lista usuarios filtrados por rol (cliente|tecnico|admin). */
	List<Usuario> findByRol(String rol);
}
