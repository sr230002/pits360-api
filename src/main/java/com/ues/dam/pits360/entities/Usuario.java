package com.ues.dam.pits360.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Usuario del sistema, mapea la tabla {@code usuarios} de PostgreSQL (Neon).
 * El id es un UUID generado en el servidor al registrar (no lo asigna la BD).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false, unique = true)
	private String email;

	/** Hash BCrypt de la contraseña; nunca sale en una respuesta. */
	@JsonIgnore
	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	/** {@code cliente | tecnico | admin}. */
	@Column(nullable = false)
	private String rol;

	/** Teléfono de contacto; opcional. */
	@Column(length = 40)
	private String telefono;

	/** Ciudad de residencia; opcional. */
	@Column(length = 120)
	private String ciudad;

	/** URL de la foto de perfil (Cloudinary); opcional. */
	@Column(length = 512)
	private String fotoUrl;
}
