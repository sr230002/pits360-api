package com.ues.dam.pits360.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Única excepción de dominio de la API: lleva el {@link HttpStatus} con el que
 * debe salir la respuesta y lo traduce {@code GlobalExceptionHandler} al
 * envelope {@code ApiResponse}.
 *
 * <p>
 * Los servicios la lanzan directamente con el estado que corresponde, p. ej.
 * {@code new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado")}.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(String msj) {
		this(HttpStatus.BAD_REQUEST, msj);
	}

	public ApiException(HttpStatus status, String msj) {
		super(msj);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
