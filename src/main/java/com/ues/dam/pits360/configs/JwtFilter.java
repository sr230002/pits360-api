package com.ues.dam.pits360.configs;

import java.io.IOException;
import java.util.List;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.utilities.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro JWT: añade cabeceras CORS, responde el preflight OPTIONS y valida el
 * token Bearer de cada petición. Se registra en SecurityConfig antes de
 * UsernamePasswordAuthenticationFilter.
 *
 * <p>
 * Las rutas públicas ({@code /auth/login}, {@code /auth/register} y
 * {@code /auth/logout}) se dejan pasar sin validar el token: así un token
 * caducado que el cliente arrastre en la cabecera no rompe un login, un
 * registro o un logout.
 *
 * <p>
 * {@code /auth/verify-token} NO se salta: en este filtro se valida el token y
 * se popula el SecurityContextHolder; el controlador lee de ahí el email. Sin
 * token, la petición sigue la cadena sin autenticar y el controlador responde
 * 401 con el envelope.
 *
 * <p>
 * Los 401 que escribe el propio filtro usan el mismo envelope que el resto de
 * la API: {@code {"data":null,"message":"...","method":"..."}}.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		setCorsHeaders(response);

		// Preflight CORS: se responde 200 sin pasar por la cadena
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		// Rutas públicas: son las mismas que SecurityConfig marca como permitAll, así
		// que un token viejo que el cliente arrastre en la cabecera NO debe provocar un
		// 401 en el login, el registro o el logout.
		if (isPublicPath(request.getServletPath())) {
			chain.doFilter(request, response);
			return;
		}

		String token = extractToken(request);
		if (token == null) {
			// Sin token: sigue la cadena sin autenticar; las rutas protegidas
			// responderán 401 desde el AuthenticationEntryPoint de SecurityConfig
			chain.doFilter(request, response);
			return;
		}

		try {
			if (!jwtUtil.isValid(token)) {
				writeError(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
				return;
			}

			String email = jwtUtil.extractEmail(token);
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (JwtException e) {
			log.debug("Token JWT rechazado: {}", e.getMessage());
			writeError(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
			return;
		}

		chain.doFilter(request, response);
	}

	/**
	 * Rutas públicas que no exigen ni validan token. Un token viejo en la
	 * cabecera no debe provocar un 401 aquí: son las mismas rutas que
	 * SecurityConfig deja pasar. (/auth/verify-token valida su token en el
	 * filtro para popular el SecurityContextHolder.)
	 */
	private boolean isPublicPath(String path) {
		return path.startsWith("/auth/login")
				|| path.startsWith("/auth/register")
				|| path.startsWith("/auth/logout");
	}

	/** Lee el header Authorization, con o sin prefijo Bearer. */
	private String extractToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header == null || header.isBlank()) {
			return null;
		}
		return header.regionMatches(true, 0, "Bearer ", 0, 7)
				? header.substring(7).trim()
				: header.trim();
	}

	private void setCorsHeaders(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "*");
		response.setHeader("Access-Control-Allow-Headers", "*");
		response.setHeader("Access-Control-Allow-Credentials", "false");
		response.setHeader("Access-Control-Allow-Max-Age", "3600");
	}

	/** Escribe el error con el envelope {@code {data:null, message, method}}. */
	private void writeError(HttpServletRequest request, HttpServletResponse response, int status, String msj)
			throws IOException {
		ApiResponse<Object> body = new ApiResponse<>(null, msj, request.getMethod());
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), body);
	}
}
