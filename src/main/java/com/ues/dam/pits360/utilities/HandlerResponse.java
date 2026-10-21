package com.ues.dam.pits360.utilities;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ues.dam.pits360.dtos.ApiResponse;

/**
 * Helper estático para que los controladores construyan respuestas con el
 * envelope {@link ApiResponse} ({@code data, message, method}) y su código HTTP.
 *
 * <p>
 * El campo {@code method} se rellena siempre con el verbo HTTP de la petición
 * en curso; nunca con una cabecera.
 */
public final class HandlerResponse {

	private HandlerResponse() {
	}

	/** 200 con {@code message = "OK"}. */
	public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
		return ok(data, "Operación realizada con éxito");
	}

	/** 200 con mensaje propio. */
	public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
		return build(HttpStatus.OK, data, message);
	}

	/** 201 Created. */
	public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
		return build(HttpStatus.CREATED, data, "Creado");
	}

	/** 201 Created con mensaje propio. */
	public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
		return build(HttpStatus.CREATED, data, message);
	}

	/** Error con {@code data = null} y el estado indicado (400/404/409/...). */
	public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
		return build(status, null, message);
	}

	private static <T> ResponseEntity<ApiResponse<T>> build(HttpStatus status, T data, String message) {
		return ResponseEntity.status(status)
				.body(new ApiResponse<>(data, message, ApiResponse.currentMethod()));
	}
}
