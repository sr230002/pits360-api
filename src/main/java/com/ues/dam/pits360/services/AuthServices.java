package com.ues.dam.pits360.services;

import java.util.Map;

import com.ues.dam.pits360.dtos.AuthResponse;
import com.ues.dam.pits360.dtos.LoginRequest;
import com.ues.dam.pits360.dtos.RegisterDTO;

/**
 * Casos de uso de autenticación: login, registro, logout y verificación de
 * token. Los fallos se señalan siempre con
 * {@link com.ues.dam.pits360.exceptions.ApiException} para que
 * {@code GlobalExceptionHandler} los convierta en el envelope
 * {@code {data,message,method}}.
 */
public interface AuthServices {

	/** Autentica por email y contraseña y devuelve el token con los datos del usuario. */
	AuthResponse login(LoginRequest request);

	/** Registra un usuario nuevo (contraseña ya codificada) y devuelve token + datos. */
	AuthResponse register(RegisterDTO dto);

	/** Comprueba el token ya validado por el JwtFilter: {@code {valid,email}}. */
	Map<String, Object> verifyToken(String email);

	/** Cierra la sesión: valida el token y devuelve el mensaje de confirmación. */
	String logout(String token);

	/** Devuelve el perfil del usuario autenticado; el token va null en la respuesta. */
	AuthResponse me(String email);
}
