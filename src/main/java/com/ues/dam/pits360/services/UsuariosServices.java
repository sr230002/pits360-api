package com.ues.dam.pits360.services;

import java.util.List;

import com.ues.dam.pits360.entities.Usuario;

/**
 * Contrato del recurso {@code /usuarios}: lectura de usuarios.
 *
 * <p>
 * ADR-10: sin capa DTO; la entidad {@link Usuario} entra y sale tal cual
 * (con {@code passwordHash} oculto por {@code @JsonIgnore}). Los fallos se
 * señalan con {@link com.ues.dam.pits360.exceptions.ApiException}.
 */
public interface UsuariosServices {

	/**
	 * Lista usuarios. Si {@code rol} no es null, filtra por rol
	 * (cliente|tecnico|admin).
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 400 si el rol es inválido
	 */
	List<Usuario> getAll(String rol);

	/**
	 * Busca un usuario por su id.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Usuario getById(String id);
}