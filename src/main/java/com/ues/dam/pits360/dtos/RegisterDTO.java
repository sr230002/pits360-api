package com.ues.dam.pits360.dtos;

import com.ues.dam.pits360.validations.OnCreate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo del registro de usuario. El controlador valida con el grupo
 * {@link OnCreate}; el rol no es elegible por el cliente y el servicio pone
 * siempre "cliente".
 */
@Data
public class RegisterDTO {

	@NotBlank(groups = OnCreate.class, message = "El nombre es obligatorio")
	private String nombre;

	@NotBlank(groups = OnCreate.class, message = "El email es obligatorio")
	@Email(groups = OnCreate.class, message = "El email debe tener un formato válido")
	private String email;

	@NotBlank(groups = OnCreate.class, message = "La contraseña es obligatoria")
	@Size(min = 6, groups = OnCreate.class, message = "La contraseña debe tener al menos 6 caracteres")
	private String password;

	/** Teléfono (opcional). */
	private String telefono;
}
