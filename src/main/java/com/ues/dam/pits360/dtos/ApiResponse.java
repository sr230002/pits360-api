package com.ues.dam.pits360.dtos;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Envoltorio único de todas las respuestas HTTP con cuerpo.
 *
 * <p>
 * Tanto los éxitos como los errores usan esta misma forma: los errores llevan
 * {@code data = null} y el código HTTP real de la respuesta. El campo
 * {@code method} lleva siempre el verbo HTTP de la petición en curso.
 *
 * @param <T> tipo de {@code data}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
	private T data;
	private String message;
	private String method;

	/** Verbo HTTP de la petición en curso; {@code "UNKNOWN"} si no hay contexto servlet. */
	public static String currentMethod() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attrs != null ? attrs.getRequest().getMethod() : "UNKNOWN";
	}
}
