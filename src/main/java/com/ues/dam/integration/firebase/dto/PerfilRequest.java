package com.ues.dam.integration.firebase.dto;

import lombok.Data;

/**
 * Cuerpo de la actualización de perfil.
 *
 * <p>
 * Todos los campos son opcionales: solo se aplica lo que llega informado. Para
 * cambiar la contraseña hacen falta {@code passwordActual} (se verifica contra
 * el hash guardado) y {@code passwordNueva} (se valida y se hashea). Si
 * {@code email} cambia se comprueba que no esté en uso y se emite un token
 * nuevo con el correo nuevo.
 */
@Data
public class PerfilRequest {

	/** Nuevo nombre del usuario; si viene en blanco se deja el actual. */
	private String nombre;

	/** Nuevo teléfono de contacto; si viene en blanco se deja el actual. */
	private String telefono;

	/** Nueva ciudad de residencia; si viene en blanco se deja la actual. */
	private String ciudad;

	/** Nuevo correo; si es distinto del actual se valida y se emite token nuevo. */
	private String email;

	/** URL de la foto de perfil; si viene en blanco se borra la foto. */
	private String fotoUrl;

	/** Contraseña actual en claro: se compara con el hash BCrypt guardado. */
	private String passwordActual;

	/** Contraseña nueva en claro: se valida y se guarda hasheada. */
	private String passwordNueva;

}
