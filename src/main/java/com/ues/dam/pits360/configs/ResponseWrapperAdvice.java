package com.ues.dam.pits360.configs;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.ues.dam.pits360.dtos.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Red de seguridad del envelope {@link ApiResponse}: envuelve en
 * {@code {data, message, method}} cualquier cuerpo devuelto por un controlador
 * que aún no esté envuelto.
 *
 * <p>
 * Es solo una red: los controladores que quieren mensaje o código propios ya
 * devuelven {@link ApiResponse} construido con {@code HandlerResponse}, y ese
 * cuerpo llega tal cual a este advice sin re-envolverse. Los tipos que los
 * convertidores escriben como texto plano o binario ({@code String},
 * {@code byte[]}, {@link Resource}) se excluyen para no romperlos.
 */
@RestControllerAdvice
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		Class<?> type = returnType.getParameterType();
		return type != ApiResponse.class
				&& type != String.class
				&& type != byte[].class
				&& !Resource.class.isAssignableFrom(type);
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		// Sin cuerpo o ya envuelto por el controlador: se devuelve intacto.
		if (body == null || body instanceof ApiResponse) {
			return body;
		}
		if (isNoContent(response)) {
			return body;
		}
		HttpMethod verb = request.getMethod();
		return new ApiResponse<>(body, "Operación realizada con éxito",
				verb != null ? verb.name() : "UNKNOWN");
	}

	private boolean isNoContent(ServerHttpResponse response) {
		return response instanceof ServletServerHttpResponse servlet
				&& servlet.getServletResponse().getStatus() == HttpServletResponse.SC_NO_CONTENT;
	}
}
