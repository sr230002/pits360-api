package com.ues.dam.pits360.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Cuerpo del login: email (que hace de username) y contraseña en claro.
 */
@Data
public class LoginRequest {

	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email debe tener un formato válido")
	private String email;

	@NotBlank(message = "La contraseña es obligatoria")
	private String password;
}
