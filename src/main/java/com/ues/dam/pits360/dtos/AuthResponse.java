package com.ues.dam.pits360.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta de login y registro: token JWT emitido y datos del usuario, sin
 * exponer nunca la contraseña.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
	private String token;
	private String id;
	private String nombre;
	private String email;
	private String rol;
}
