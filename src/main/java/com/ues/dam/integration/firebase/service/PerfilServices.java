package com.ues.dam.integration.firebase.service;

import com.ues.dam.integration.firebase.dto.PerfilRequest;
import com.ues.dam.integration.firebase.dto.PerfilResultado;

/**
 * Caso de uso de actualización del perfil del usuario autenticado.
 */
public interface PerfilServices {

	/**
	 * Aplica los cambios del perfil del usuario con ese email.
	 *
	 * @param email email del usuario autenticado
	 * @param request cambios solicitados (campos opcionales)
	 * @return el usuario ya guardado y el token nuevo (solo si cambió el correo)
	 */
	PerfilResultado actualizar(String email, PerfilRequest request);

}
