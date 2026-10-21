package com.ues.dam.pits360.exceptions;

import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.utilities.HandlerResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Punto único de traducción de excepciones a respuestas HTTP.
 *
 * <p>
 * Éxitos y errores comparten el mismo envelope {@link ApiResponse}
 * ({@code data, message, method}): los errores llevan {@code data = null}, un
 * mensaje legible y el código HTTP real. Nunca se devuelve al cliente el stack
 * trace ni el mensaje interno de la excepción (se loguea con {@code log.error}).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** Cualquier excepción de dominio (y las futuras) con su propio estado. */
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Object>> handleApi(ApiException e) {
		log.debug("Excepción de API ({}): {}", e.getStatus(), e.getMessage());
		return HandlerResponse.error(e.getStatus(), msj(e.getMessage()));
	}

	/** Fallos de {@code @Valid} sobre el cuerpo de la petición. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException e) {
		String msj = e.getBindingResult().getFieldErrors().stream()
				.sorted(Comparator.comparing(FieldError::getField, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return HandlerResponse.error(HttpStatus.BAD_REQUEST,
				msj.isBlank() ? "Los datos enviados no son válidos." : msj);
	}

	/** Cuerpo de la petición ausente o JSON malformado. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Object>> handleNotReadable(HttpMessageNotReadableException e) {
		log.debug("Cuerpo de petición no legible", e);
		return HandlerResponse.error(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido.");
	}

	/** Ruta inexistente: responde 404 en lugar de propagar un 500. */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handleNoRoute(NoResourceFoundException e) {
		return HandlerResponse.error(HttpStatus.NOT_FOUND, "Ruta no encontrada.");
	}

	/** Violación de restricción en la base de datos (único, FK, not null, ...). */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException e) {
		log.debug("Violación de integridad de datos", e);
		return HandlerResponse.error(HttpStatus.CONFLICT,
				"El recurso ya existe o viola una restricción de la base de datos.");
	}

	/** Verbo HTTP no permitido sobre una ruta existente: 405 en lugar de 500. */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.debug("Método no soportado: {}", e.getMethod());
		return HandlerResponse.error(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no soportado para esta ruta.");
	}

	/** Parámetro de ruta o query con formato inválido (p. ej. {@code /products/abc}). */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.debug("Tipo de argumento inválido en '{}'", e.getName());
		return HandlerResponse.error(HttpStatus.BAD_REQUEST,
				"El parámetro '" + e.getName() + "' tiene un formato inválido.");
	}

	/** Cuerpo enviado con un Content-Type distinto de {@code application/json}. */
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ApiResponse<Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
		log.debug("Content-Type no soportado: {}", e.getContentType());
		return HandlerResponse.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
				"Tipo de contenido no soportado. Usa application/json.");
	}

	/** Archivo o petición mayor que el límite multipart de la API: 413 en vez de 500. */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponse<Object>> handleMaxUpload(MaxUploadSizeExceededException e) {
		log.debug("Carga multipart mayor que el límite configurado");
		return HandlerResponse.error(HttpStatus.PAYLOAD_TOO_LARGE,
				"El archivo o la petición supera el tamaño máximo permitido.");
	}

	/** Cualquier otro fallo: se loguea completo y se responde genérico. */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception e) {
		log.error("Error no controlado", e);
		return HandlerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocurrió un error inesperado en el servidor.");
	}

	/** Mensaje por defecto cuando la excepción no trae texto útil. */
	private static String msj(String message) {
		return message == null || message.isBlank()
				? "Ocurrió un error inesperado en el servidor."
				: message;
	}
}
